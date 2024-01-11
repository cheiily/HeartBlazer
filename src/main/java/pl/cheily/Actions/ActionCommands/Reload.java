package pl.cheily.Actions.ActionCommands;

import net.dv8tion.jda.api.events.GenericEvent;
import pl.cheily.Actions.Action;
import pl.cheily.Actions.ActionRequestType;
import pl.cheily.Actions.ActionResult;
import pl.cheily.Actions.Authorization.AuthLevel;
import pl.cheily.Config;

import java.util.Set;

public class Reload extends Action {
    private Reload() {
        name = "reload";
        helpNames = Set.of("Reload");
        minimumRequiredAuthLevel = AuthLevel.OWNER;
    }

    private static Reload _instance;

    public static Reload instance() {
        if (_instance == null) _instance = new Reload();
        return _instance;
    }

    @Override
    public ActionResult invoke(GenericEvent request, ActionRequestType requestType) {
        Config.load();
        return null;
    }
}
