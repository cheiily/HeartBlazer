package pl.cheily.Actions;

public record AuthorizationResult(
        String details,
        boolean success
) {
    private final static AuthorizationResult _ACCEPT = new AuthorizationResult("", true);

    public static AuthorizationResult ACCEPT() {
        return _ACCEPT;
    }

    public static AuthorizationResult DENY(String reason) {
        return new AuthorizationResult(reason, false);
    }

    public AuthorizationResult and(AuthorizationResult other) {
        if ( this.success && other.success )
            return _ACCEPT;

        return DENY((details + " " + other.details).trim());
    }
}
