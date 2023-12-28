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
import pl.cheily.Config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pl.cheily.Actions.AuthorizationResult.*;

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

        ActionResult result = action.call(request, ActionRequestType.MESSAGE_CONTEXT_INTERACTION);
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

        action.call(request, ActionRequestType.MESSAGE_EMOJI_REACTION)
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

        ActionResult result = action.call(request, ActionRequestType.SLASH_COMMAND);
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

        ActionResult result = action.call(request, ActionRequestType.MESSAGE_RECEIVED);
        result.log(action.name, request, request.getAuthor(), request.getChannel());
    }

    private boolean authorizeWithContext(GenericEvent request, User user, Member member, MessageChannelUnion channel, Action action, ActionRequestType requestType) {
        //Context check
        AuthorizationResult auth = action.authorizeContext(request, request.getJDA());

        if (requestType == ActionRequestType.MESSAGE_RECEIVED && !Config.messageCommandsOn)
            auth = auth.and(AuthorizationResult.DENY("Message commands off in configuration."));

        if ( !auth.isAccept() ) {
            LogContext.deny(
                    action.name, request, user, channel, auth.details()
            ).log();
            return false;
        }

        //User check
        auth = auth.and(authorizeUser(user));
        if ( member != null )
            auth = auth.and(authorizeMember(action, member, channel));

        //Maybe when there's an actual permission system
//        auth = auth.and(authorizeUser(request.getJDA().getSelfUser()));

        auth = auth.and(action.authorizeUser(request, request.getJDA()));
        auth = auth.and(action.authorizeUser(request, request.getJDA(), auth));

        if ( !auth.isAccept() ) {
            LogContext.deny(
                    action.name, request, user, channel, auth.details()
            ).log();
            return false;
        }
        return true;
    }


    private AuthorizationResult authorizeUser(User user) {
        if ( Config.ownerBypass && user.getId().equals(Config.ownerId) )
            return ADMIN_ACCEPT();
        if ( Config.administratorWhitelist.contains(user.getId()) )
            return ADMIN_ACCEPT();

        boolean isSelf = user.getId().equals(Config.selfId);
        if ( !isSelf ) {
            if ( user.isBot() || user.isSystem() )
                return DENY("User type not valid (bot or system)");
        }

        return ACCEPT();
    }

    private AuthorizationResult authorizeMember(Action action, Member member, MessageChannelUnion channel) {
        List<Permission> missingMemberPermissions = action.requiredUserPermissions.stream()
                .filter(p -> !member.getPermissions(channel.asGuildMessageChannel()).contains(p))
                .toList();

        boolean isSelf = member.getId().equals(Config.selfId);
        if ( !missingMemberPermissions.isEmpty() )
            return DENY((isSelf ? "Self-member" : "Author") + " missing permissions: " + missingMemberPermissions);


        return ACCEPT();
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
