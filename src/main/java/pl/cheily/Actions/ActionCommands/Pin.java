package pl.cheily.Actions.ActionCommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import pl.cheily.Actions.*;
import pl.cheily.Config;

import java.util.Set;

import static pl.cheily.Actions.ActionRequestType.MESSAGE_CONTEXT_INTERACTION;
import static pl.cheily.Actions.ActionRequestType.MESSAGE_EMOJI_REACTION;
import static pl.cheily.Actions.AuthorizationResult.ACCEPT;
import static pl.cheily.Actions.AuthorizationResult.DENY;

public class Pin extends Action {
    private Pin() {
        name = "pin";
        helpNames = Set.of("Pin (thread-only)");
        acceptedRequestTypes = Set.of(
                MESSAGE_EMOJI_REACTION,
                MESSAGE_CONTEXT_INTERACTION
        );

        listenedEmoji = Set.of(pushpin);
    }

    private static Pin _instance;

    public static Pin instance() {
        if ( _instance == null ) _instance = new Pin();
        return _instance;
    }

    private static final Emoji pushpin = Emoji.fromUnicode("\uD83D\uDCCC");
    private static final Emoji white_checkmark = Emoji.fromUnicode("✅");
    private static final Emoji cross = Emoji.fromUnicode("❌");
    private static final Emoji white_exclamation_mark = Emoji.fromUnicode("❕");
    private static final Emoji exclamation_mark = Emoji.fromUnicode("❗");

    @Override
    public AuthorizationResult authorizeContext(GenericEvent request, JDA jda) {
        if ( request instanceof MessageReactionAddEvent cRequest ) {
            if ( !cRequest.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD)
                    && !cRequest.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD)
            ) {
                return DENY("Incorrect channel type - not a thread.");
            }
        } else if ( request instanceof MessageContextInteractionEvent cRequest ) {
            if ( !cRequest.getChannelType().equals(ChannelType.GUILD_PRIVATE_THREAD)
                    && !cRequest.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD)
            ) return DENY("Incorrect channel type - not a thread.");
        }

        return ACCEPT();
    }

    @Override
    public AuthorizationResult authorizeUser(GenericEvent request, JDA jda, AuthorizationResult currentAuthState) {
        if ( request instanceof MessageReactionAddEvent cRequest ) {
            String authorId = cRequest.getUserId();

            if ( !authorId.equals(cRequest.getChannel().asThreadChannel().getOwnerId()) ) {
                if ( !currentAuthState.and(DENY("")).isAccept() )
                    respond(cRequest.retrieveMessage().complete(), cross, request, cRequest.getReaction(), cRequest.getUser());

                return DENY("Author is not the thread owner.");
            }
        } else if ( request instanceof MessageContextInteractionEvent cRequest ) {
            String authorId = cRequest.getUser().getId();

            if ( !authorId.equals(cRequest.getChannel().asThreadChannel().getOwnerId()) )
                return DENY("Author is not the thread owner.");
        }
        return ACCEPT();
    }

    @Override
    public ActionResult call(GenericEvent request, ActionRequestType requestType) {
        Message message = null;
        User triggerUser = null;
        MessageReaction triggerReaction = null;
        switch (requestType) {
            case MESSAGE_EMOJI_REACTION -> {
                MessageReactionAddEvent cRequest = (MessageReactionAddEvent) request;
                message = cRequest.retrieveMessage().complete();
                triggerUser = cRequest.getUser();
                triggerReaction = cRequest.getReaction();
            }
            case MESSAGE_CONTEXT_INTERACTION -> {
                MessageContextInteractionEvent cRequest = ((MessageContextInteractionEvent) request);
                message = cRequest.getTarget();
                triggerUser = cRequest.getUser();
            }
        }

        if ( message == null )
            return ActionResult.FAILURE("Failed to retrieve target message.", null);


        MessageChannelUnion channel = message.getChannel();
        Message finalMessage = message;
        MessageReaction finalTriggerReaction = triggerReaction;
        User finalTriggerUser = triggerUser;

        if ( message.isPinned() && requestType == MESSAGE_EMOJI_REACTION ) {
            respond(message, white_exclamation_mark, request, finalTriggerReaction, triggerUser);
            return ActionResult.SUCCESS_ACCEPT("Message already pinned.");
        }


        message.pin().queue(
                unused -> {
                    LogContext.minorSuccess(name, request, "Pinned target message.").log();
                    respond(finalMessage, white_checkmark, request, finalTriggerReaction, finalTriggerUser);
                },
                throwable -> {
                    LogContext.failure(name, request, finalTriggerUser, channel, "Failed to pin target message.", throwable).log();
                    respond(finalMessage, exclamation_mark, request, finalTriggerReaction, finalTriggerUser);
                }
        );

        return ActionResult.SUCCESS_ACCEPT;
    }

    private void respond(Message message, Emoji emoji, GenericEvent request, MessageReaction trigger, User triggerUser) {
        new Thread(() -> message.addReaction(emoji).queue(
                unused -> {
                    LogContext.minorSuccess(name, request, "Added response reaction.").log();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        LogContext.minorFailure(name, request, "Response removal thread interrupted.", ex).log();
                    }
                    message.removeReaction(emoji).queue(
                            unused1 -> LogContext.minorSuccess(name, request, "Removed response reaction.").log(),
                            throwable -> LogContext.minorFailure(name, request, "Failed to remove response reaction.", throwable).log()
                    );
                    if ( trigger != null && triggerUser != null )
                        trigger.removeReaction(triggerUser).queue(
                                unused1 -> LogContext.minorSuccess(name, request, "Removed trigger reaction.").log(),
                                throwable -> LogContext.minorFailure(name, request, "Couldn't remove trigger reaction.", throwable).log()
                        );
                },
                throwable ->
                        LogContext.minorFailure(name, request, "Failed to add response reaction.", throwable).log()
        )).start();
    }
}
