package pl.cheily.Actions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import pl.cheily.Actions.ActionCommands.Sleep;
import pl.cheily.Actions.ActionCommands.PinOrUnpin;
import pl.cheily.Actions.ActionCommands.Ping;
import pl.cheily.Actions.Authorization.AuthLevel;
import pl.cheily.Actions.Authorization.AuthResult;
import pl.cheily.Config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActionHandler {
    private final Set<Action> actions = new HashSet<>();

    {
        actions.add(Ping.instance());
        actions.add(PinOrUnpin.instance());
        actions.add(Sleep.instance());
    }

    public void accept(MessageContextInteractionEvent request) {
        List<Action> found = actions.stream()
                .filter(action -> action.acceptedRequestTypes.contains(ActionRequestType.MESSAGE_CONTEXT_INTERACTION))
                .filter(action -> action.name.equals(request.getName()) || action.helpNames.contains(request.getName()))
                .toList();

        if ( found.isEmpty() ) return;
        Action action = found.get(0);

        if ( !authorizeWithContext(request, request.getUser(), request.isFromGuild() ? request.getMember() : null, request.getChannel(), action, ActionRequestType.MESSAGE_CONTEXT_INTERACTION) ) {
            ActionResult result = ActionResult.SUCCESS_DENY;
            request.reply(result.description()).setEphemeral(true).queue(
                    interactionHook -> LogContext.minorSuccess(action.name, request, "Sent general handler reply.").log(),
                    throwable -> LogContext.minorFailure(action.name, request, "Failed to send general handler reply.", throwable).log()
            );
            return;
        }

        ActionResult result = action.invoke(request, ActionRequestType.MESSAGE_CONTEXT_INTERACTION);
        result.log(action.name, request, request.getUser(), request.getChannel());
        request.reply(result.description()).setEphemeral(true).queue(
                interactionHook -> LogContext.minorSuccess(action.name, request, "Sent general handler reply.").log(),
                throwable -> LogContext.minorFailure(action.name, request, "Failed to send general handler reply.", throwable).log()
        );
    }

    public void accept(MessageReactionAddEvent request) {
        List<Action> found = actions.stream()
                .filter(action -> action.acceptedRequestTypes.contains(ActionRequestType.MESSAGE_EMOJI_REACTION))
                .filter(action -> action.listenedEmoji.contains(request.getEmoji()))
                .toList();

        if ( found.isEmpty() ) return;
        Action action = found.get(0);

        if ( !authorizeWithContext(request, request.getUser(), request.isFromGuild() ? request.getMember() : null, request.getChannel(), action, ActionRequestType.MESSAGE_EMOJI_REACTION) )
            return;

        action.invoke(request, ActionRequestType.MESSAGE_EMOJI_REACTION)
                .log(action.name, request, request.getUser(), request.getChannel());
    }

    public void accept(SlashCommandInteractionEvent request) {
        List<Action> found = actions.stream()
                .filter(action -> action.acceptedRequestTypes.contains(ActionRequestType.SLASH_COMMAND))
                .filter(action -> action.name.equals(request.getName()) || action.helpNames.contains(request.getName()))
                .toList();

        if ( found.isEmpty() ) return;
        Action action = found.get(0);

        if ( !authorizeWithContext(request, request.getUser(), request.isFromGuild() ? request.getMember() : null, request.getChannel(), action, ActionRequestType.SLASH_COMMAND) )
            return;

        ActionResult result = action.invoke(request, ActionRequestType.SLASH_COMMAND);
        result.log(action.name, request, request.getUser(), request.getChannel());
    }

    public void accept(MessageReceivedEvent request, String actionName) {
        List<Action> found = actions.stream()
                .filter(action -> action.acceptedRequestTypes.contains(ActionRequestType.MESSAGE_RECEIVED))
                .filter(action -> action.name.equals(actionName) || action.helpNames.contains(actionName))
                .toList();

        if ( found.isEmpty() ) return;
        Action action = found.get(0);

        if ( !authorizeWithContext(request, request.getAuthor(), request.isFromGuild() ? request.getMember() : null, request.getChannel(), action, ActionRequestType.MESSAGE_RECEIVED) )
            return;

        ActionResult result = action.invoke(request, ActionRequestType.MESSAGE_RECEIVED);
        result.log(action.name, request, request.getAuthor(), request.getChannel());
    }

    /**
     * Verifies whether the user is allowed to request the specified action under the current context.
     * The authorization steps are as follows:
     * <ol>
     *     <li>The AuthResult is initialized with the handler's own User/Member verification methods. {@link #authorizeUser}, {@link #authorizeMember}.
     *     These methods provide a general check of whether the user is on the {@link Config#administratorWhitelist}, whether they
     *     have the required discord permissions, and whether the self-user has those as well.</li>
     *     <li>Then the action is asked to authorize the context of the request. This provides a mask the acquired level will be checked against.</li>
     *     <li>The action is asked to authorize the user of the request under its own specific requirements.</li>
     *     <li>The acquired AuthResult is then verified against the action's context mask and its required minimum level.
     *     The result must be present in both of these masks or the request will be denied.</li>
     * </ol>
     * @param request
     * @param user
     * @param member
     * @param channel
     * @param action
     * @param requestType
     * @return
     */
    private boolean authorizeWithContext(GenericEvent request, User user, Member member, MessageChannelUnion channel, Action action, ActionRequestType requestType) {
        AuthResult result = new AuthResult();
        authorizeUser(user, result);
        if ( member != null )
            authorizeMember(action, member, channel, result);

        if (requestType == ActionRequestType.MESSAGE_RECEIVED && !Config.messageCommandsOn) {
            result.fail(AuthLevel.ADMINISTRATOR.below(), "Message commands off in configuration.");
        }

        AuthResult contextMask = action.authorizeContext(request, requestType);

        action.authorizeUser(request, requestType, result);

        if ( !contextMask.evaluate(result.getAuthLevel()) ) {
            LogContext.deny(
                    action.name, request, user, channel,
                    "Request not meeting context requirements for this user. Allowed: " + contextMask.getAuthLevel().containedLevels()
                    + ". Acquired: " + result.getAuthLevel().containedLevels() + ". Details in warn log."
            ).log();
            LogContext.minorFailure(
                    action.name, request, result.debugString(), null
            ).log();
            return false;
        }
        if ( !result.evaluate(action.minimumRequiredAuthLevel.andAbove()) ) {
            LogContext.deny(
                    action.name, request, user, channel,
                    "User not meeting minimum required permission level for this action. Required: " + action.minimumRequiredAuthLevel.containedLevels()
                    + ". Contained: " + result.getAuthLevel().containedLevels() + ". Details in warn log."
            ).log();
            LogContext.minorFailure(
                    action.name, request, result.debugString(), null
            ).log();
            return false;
        }

        LogContext.minorSuccess(
                action.name, request,
                " Action authorized. Allowed " + contextMask.getAuthLevel().containedLevels()
                + ". Required: " + action.minimumRequiredAuthLevel.containedLevels()
                + ". Acquired: " + result.debugString()
        ).log();
        return true;

    }


    private void authorizeUser(User user, AuthResult currentAuthState) {
        if ( Config.ownerBypass && user.getId().equals(Config.ownerId) )
            currentAuthState.pass(AuthLevel.OWNER, "Requester is bot-owner.");
        if ( Config.administratorWhitelist.contains(user.getId()) )
            currentAuthState.pass(AuthLevel.ADMINISTRATOR, "Requester is white-listed administrator.");
        if ( Config.moderatorWhitelist.contains(user.getId()) )
            currentAuthState.pass(AuthLevel.MODERATOR, "Requester is white-listed moderator.");

        boolean isSelf = user.getId().equals(Config.selfId);
        if ( !isSelf ) {
            if ( user.isBot() || user.isSystem() )
                currentAuthState.fail(AuthLevel.ALL, "Requester is bot or system.");
        }
    }

    private void authorizeMember(Action action, Member member, MessageChannelUnion channel, AuthResult currentAuthState) {
        List<Permission> missingMemberPermissions = action.requiredUserPermissions.stream()
                .filter(p -> !member.getPermissions(channel.asGuildMessageChannel()).contains(p))
                .toList();

        boolean isSelf = member.getId().equals(Config.selfId);

        if ( !missingMemberPermissions.isEmpty() ) {
            if (isSelf)
                currentAuthState.fail(AuthLevel.ALL, "Self user is missing permissions: " + missingMemberPermissions);
            else
                currentAuthState.fail(AuthLevel.USER, "Requester is missing permissions: " + missingMemberPermissions);
        }
    }

    //selfuser guild presence
    //selfuser status
    //selfuser userid
    //selfmember hasrole
    //selfmember channel presence
    //channel istype
    //channel channelid
    //channel incategory
    //channel
    //authoruser isbotowner
    //authoruser whitelisted/blacklisted
    //authoruser userid
    //authoruser guildpresence
    //authormember isguildowner
    //authormember ischannelowner
    //authormember channelpresence
    //authormember hasrole
    //authormember

}
