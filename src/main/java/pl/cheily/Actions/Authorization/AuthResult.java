package pl.cheily.Actions.Authorization;

import pl.cheily.IDebugPrintable;

import java.util.ArrayList;

public class AuthResult implements IDebugPrintable {
    private AuthLevel acquired;
    private final ArrayList<String> details_pass;
    private final ArrayList<String> details_fail;


    public AuthResult() {
        acquired = AuthLevel.NONE;
        details_pass = new ArrayList<>();
        details_fail = new ArrayList<>();
    }

    public void pass(AuthLevel level, String reason) {
        acquired = acquired.add(level);
        details_pass.add(level + " : " + reason);
    }

    public void fail(AuthLevel level, String reason) {
        acquired = acquired.subtract(level);
        details_fail.add(level + " : " + reason);
    }

    public boolean evaluate(AuthLevel requested) {
        return acquired.has(requested);
    }

    public AuthLevel getAuthLevel() {
        return acquired;
    }

    @Override
    public String toString() {
        return details_fail.toString();
    }

    @Override
    public String debugString() {
        return "AuthorizationResult{" +
                "acquired=" + acquired +
                ", details_pass=" + details_pass +
                ", details_fail=" + details_fail +
                '}';
    }

}
