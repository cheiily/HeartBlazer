package pl.cheily.Actions.ActionCommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.cheily.Actions.*;
import pl.cheily.Actions.Authorization.AuthLevel;
import pl.cheily.Actions.Authorization.AuthResult;
import pl.cheily.Config;

import java.util.Set;

public class Sleep extends Action {
    private Sleep() {
        name = "sleep";
        acceptedRequestTypes = Set.of(ActionRequestType.MESSAGE_RECEIVED);
        requiredAuthLevel = AuthLevel.OWNER;
    }

    private static Sleep _instance;

    public static Sleep instance() {
        if ( _instance == null ) _instance = new Sleep();
        return _instance;
    }

    @Override
    public ActionResult invoke(GenericEvent request, ActionRequestType requestType) {
        ((MessageReceivedEvent) request).getMessage().reply("Commencing shutdown.").queue(
                message -> LogContext.minorSuccess(name, request, "Sent standard reply.").log(),
                throwable -> LogContext.minorFailure(name, request, "Couldn't send standard reply.", throwable).log()
        );

        request.getJDA().shutdown();

        return ActionResult.SUCCESS_ACCEPT;
    }
}
