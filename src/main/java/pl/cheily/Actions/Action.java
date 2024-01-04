package pl.cheily.Actions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import pl.cheily.Actions.Authorization.AuthLevel;
import pl.cheily.Actions.Authorization.AuthResult;

import java.util.Set;

public abstract class Action {

    protected String name = "No name provided";
    protected Set<String> helpNames = Set.of();
    protected AuthLevel requiredAuthLevel = AuthLevel.NONE;

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

    /**
     * Asks the action to evaluate whether the current context allows for execution and returns a mask of all allowed auth levels for the requested context.
     * <p>
     * Context authorization can be multi-level in the sense that a context may be valid for some level of authorization but not others.
     * For example, it may be difficult to evaluate a user's permissions in a DM message compared to a server message
     * without some sort of local, pre-saved whitelist. If a command requires user validation
     * to access it, it may allow {@code AuthLevel.USER} in a guild, but not in a DM.
     * </p>
     * @implSpec The denied levels should be <b>failed</b> with a valid reason string, rather than not passed. Checking may start by passing {@link AuthLevel#ALL}.
     *
     * @param request - the command should parse the context according to its own requirements.
     * @param requestType - type of the incoming request.
     * @return a mask of all allowed auth levels for the requested context.
     */
    public AuthResult authorizeContext(GenericEvent request, ActionRequestType requestType) {
        AuthResult ret = new AuthResult();
        ret.pass(AuthLevel.USER, "Default implementation pass");
        return ret;
    }

    /**
     * Asks the action to evaluate under which authorization levels is the current user allowed to take the action and modifies the passed {@code AuthLevel} mask.
     * <p>
     *     With an example of {@link pl.cheily.Actions.ActionCommands.PinOrUnpin}:
     *     The command's authorization depends on thread-ownership.
     *     If the user is a thread owner, the command will pass the {@code AuthLevel.USER}, otherwise it will fail it.
     *     This function should be specifically concerned with its minimum, {@link Action#requiredAuthLevel}.
     *     The action may fail levels previously passed or pass levels previously failed, depending on its requirements.
     * </p>
     * @param request containing user or member to verify.
     * @param requestType type of request to simplify parsing.
     * @param currentAuthState to modify.
     */
    public void authorizeUser(GenericEvent request, ActionRequestType requestType, AuthResult currentAuthState) {
        currentAuthState.pass(AuthLevel.USER, "Default implementation pass");
    }

    public abstract ActionResult invoke(GenericEvent request, ActionRequestType requestType);

}
