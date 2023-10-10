package pl.cheily.Actions;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;

public record ActionResult (
        String description,
        String details,
        Type type,
        Throwable throwable
) {
    public enum Type {
        SUCCESS_ACCEPT,
        SUCCESS_DENY,
        FAILURE,
        MINOR_SUCCESS,
        MINOR_FAILURE
    }

    public static final ActionResult SUCCESS_ACCEPT =
            new ActionResult("SUCCESS - ACTION ACCEPTED", Type.SUCCESS_ACCEPT);

    public static final ActionResult SUCCESS_DENY =
            new ActionResult("SUCCESS - ACTION DENIED", Type.SUCCESS_DENY);

    public static final ActionResult FAILURE =
            new ActionResult("FAILURE", Type.FAILURE);

    public static final ActionResult MINOR_SUCCESS =
            new ActionResult("SUCCESS - MINOR ACTION COMPLETED", Type.MINOR_SUCCESS);

    public static final ActionResult MINOR_FAILURE =
            new ActionResult("FAILURE - MINOR ACTION FAILURE", Type.MINOR_FAILURE);


    public static ActionResult SUCCESS_ACCEPT(String details) {
        return new ActionResult("SUCCESS - ACTION ACCEPTED", details, Type.SUCCESS_ACCEPT, null);
    }

    public static ActionResult SUCCESS_DENY(String details) {
        return new ActionResult("SUCCESS - ACTION DENIED", details, Type.SUCCESS_DENY, null);
    }

    public static ActionResult FAILURE(String details, Throwable throwable) {
        return new ActionResult("FAILURE", details, Type.FAILURE, throwable);
    }

    public static ActionResult MINOR_SUCCESS(String details) {
        return new ActionResult("SUCCESS - MINOR ACTION COMPLETED", details, Type.MINOR_SUCCESS, null);
    }

    public static ActionResult MINOR_FAILURE(String details, Throwable throwable) {
        return new ActionResult("FAILURE - MINOR ACTION FAILURE", details, Type.MINOR_FAILURE, throwable);
    }

    private ActionResult(String description, Type type) {
        this(description, "", type, null);
    }

    public void log(String actionName, GenericEvent request, User author, MessageChannelUnion channel) {
        new LogContext(this, actionName, request, author, channel).log();
    }
}
