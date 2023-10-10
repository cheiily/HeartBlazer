package pl.cheily.Actions;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;

import static pl.cheily.HeartBlazer.logger;
import static pl.cheily.HeartBlazer.mBotFeedback;

public class LogContext {
    public static LogContext failure(String actionName, GenericEvent request, User author, MessageChannelUnion location, String details, Throwable throwable) {
        return new LogContext(ActionResult.FAILURE(details, throwable), actionName, request, author, location);
    }

    public static LogContext accept(String actionName, GenericEvent request, User author, MessageChannelUnion location, String details) {
        return new LogContext(ActionResult.SUCCESS_ACCEPT(details), actionName, request, author, location);
    }

    public static LogContext deny(String actionName, GenericEvent request, User author, MessageChannelUnion location, String details) {
        return new LogContext(ActionResult.SUCCESS_DENY(details), actionName, request, author, location);
    }

    public static LogContext minorSuccess(String actionName, GenericEvent request, String details) {
        return new LogContext(ActionResult.MINOR_SUCCESS(details), actionName, request, null, null);
    }

    public static LogContext minorFailure(String actionName, GenericEvent request, String details, Throwable throwable) {
        return new LogContext(ActionResult.MINOR_FAILURE(details, throwable), actionName, request, null, null);
    }

    public LogContext(ActionResult result, String actionName, GenericEvent request, User author, MessageChannelUnion location) {
        this.result = result;
        this.actionName = actionName;
        this.request = request;
        this.author = author;
        this.location = location;
    }

    public final ActionResult result;
    public final String actionName;
    public final GenericEvent request;
    public final User author;
    public final MessageChannelUnion location;

    @Override
    public String toString() {
        if ( result.type() == ActionResult.Type.MINOR_SUCCESS || result.type() == ActionResult.Type.MINOR_FAILURE ) {
            return "Minor task related to action " + actionName + " upon event " + request.getResponseNumber()
                    + "\n RESULT: " + result.description()
                    + (result.details().isEmpty() ? "" : "\n Details: " + result.details());
        } else
            return "Handling request " + actionName + " triggered by " + author.getName()
                    + " upon event " + request.getResponseNumber() + " (" + request.getClass().getName() + ")."
                    + "\n Where: " + (location != null ? location.getName() + " (" + location.getId() + ")" : "UNKNOWN")
                    + "\n RESULT: " + result.description()
                    + (result.details().isEmpty() ? "" : "\n Details: " + result.details());
    }

    public void log() {
        if ( result.type() == ActionResult.Type.FAILURE ) logger.error(
                mBotFeedback,
                this.toString(),
                result.throwable()
        );
        else if ( result.type() == ActionResult.Type.MINOR_FAILURE ) logger.warn(
                mBotFeedback,
                this.toString(),
                result.throwable()
        );
        else if ( result.type() == ActionResult.Type.MINOR_SUCCESS ) logger.debug(
                mBotFeedback,
                this.toString()
        );
        else logger.info(
                mBotFeedback,
                this.toString()
        );
    }
}
