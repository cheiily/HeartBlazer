package pl.cheily.Actions;

public record AuthorizationResult(
        String details,
        boolean success,
        boolean adminSuccess
) {
    private final static AuthorizationResult _ACCEPT = new AuthorizationResult("", true, false);
    private final static AuthorizationResult _ADMIN_ACCEPT = new AuthorizationResult("", true, true);

    public static AuthorizationResult ACCEPT() {
        return _ACCEPT;
    }

    public static AuthorizationResult ADMIN_ACCEPT() {
        return _ADMIN_ACCEPT;
    }

    public static AuthorizationResult DENY(String reason) {
        return new AuthorizationResult(reason, false, false);
    }

    public AuthorizationResult and(AuthorizationResult other) {
        if ( this.adminSuccess || other.adminSuccess )
            return _ADMIN_ACCEPT;

        if ( this.success && other.success )
            return _ACCEPT;

        return DENY((details + " " + other.details).trim());
    }

    public boolean isAccept() {
        return adminSuccess || success;
    }
}
