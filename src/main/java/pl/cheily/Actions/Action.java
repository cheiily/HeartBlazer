package pl.cheily.Actions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.Set;

public abstract class Action {

    protected String name = "No name provided";
    protected Set<String> helpNames = Set.of();

    protected Set<ActionRequestType> acceptedRequestTypes = Set.of();
    protected Set<Permission> requiredUserPermissions = Set.of();
    protected Set<Permission> requiredBotPermissions = Set.of();
    protected Set<Emoji> listenedEmoji = Set.of();


    public String getName() {
        return name;
    }

    public Set<ActionRequestType> getAcceptedRequestTypes() {
        return acceptedRequestTypes;
    }

    public Set<Permission> getRequiredUserPermissions() {
        return requiredUserPermissions;
    }

    public Set<Permission> getRequiredBotPermissions() {
        return requiredBotPermissions;
    }

    public AuthorizationResult uniqueAuthorize(GenericEvent request, JDA jda) {
        return AuthorizationResult.ACCEPT();
    }

    public abstract ActionResult call(GenericEvent request, ActionRequestType requestType);

}
