package pl.cheily.Actions.ActionCommands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.cheily.Actions.Action;
import pl.cheily.Actions.ActionRequestType;
import pl.cheily.Actions.ActionResult;
import pl.cheily.Actions.LogContext;

import java.time.temporal.ChronoUnit;
import java.util.Set;

import static pl.cheily.Actions.ActionRequestType.MESSAGE_RECEIVED;
import static pl.cheily.Actions.ActionRequestType.SLASH_COMMAND;
import static pl.cheily.Actions.ActionResult.SUCCESS_ACCEPT;

public class Ping extends Action {
    private Ping() {
        this.name = PROP_NAME;
        this.acceptedRequestTypes = Set.of(
                MESSAGE_RECEIVED,
                SLASH_COMMAND
        );
    }

    public static final String PROP_NAME = "ping";

    private static Ping _instance;

    public static Ping instance() {
        if ( _instance == null ) _instance = new Ping();
        return _instance;
    }

    @Override
    public ActionResult invoke(GenericEvent request, ActionRequestType requestType) {
        if ( requestType == SLASH_COMMAND ) {
            SlashCommandInteractionEvent cRequest = (SlashCommandInteractionEvent) request;

            long gateway = request.getJDA().getGatewayPing();
            cRequest.reply("Current gateway ping: " + gateway + " ms.").setEphemeral(true).queue(
                    interactionHook -> LogContext.minorSuccess(name, request, "Sent ephemeral reply.").log(),
                    throwable -> LogContext.failure(name, request, cRequest.getUser(), cRequest.getChannel(), "Failed to reply with ephemeral message (G: " + gateway + " ms).", throwable).log()
            );
            return SUCCESS_ACCEPT;

        } else if ( requestType == MESSAGE_RECEIVED ) {
            MessageReceivedEvent cRequest = (MessageReceivedEvent) request;

            Message requestMessage = cRequest.getMessage();
            long gateway = request.getJDA().getGatewayPing();
            String origContent = "Current gateway ping: " + gateway + " ms. ";
            requestMessage.reply(origContent).queue(
                    newMessage -> {
                        LogContext.minorSuccess(name, request, "Sent standard reply").log();

                        long deltaTime = requestMessage.getTimeCreated().until(newMessage.getTimeCreated(), ChronoUnit.MILLIS);
                        newMessage.editMessage(origContent + "Real reply time: " + deltaTime + " ms.").queue(
                                message -> LogContext.minorSuccess(name, request, "Edited standard reply.").log(),
                                throwable -> LogContext.failure(name, request, cRequest.getAuthor(), cRequest.getChannel(), "Failed to edit standard reply (G: " + gateway + "ms R: " + deltaTime + " ms).", throwable).log()
                        );
                    },
                    throwable -> LogContext.failure(name, request, cRequest.getAuthor(), cRequest.getChannel(), "Failed to send standard reply (G: " + gateway + "ms).", throwable).log()
            );
        }

        return SUCCESS_ACCEPT;
    }
}
