package pl.cheily.Actions.Authorization;

import java.util.ArrayList;

public record AuthLevel(
        int mask
) {
    public static final AuthLevel NONE = new AuthLevel( 0 );
    public static final AuthLevel USER = new AuthLevel( 1 );
    public static final AuthLevel MODERATOR = new AuthLevel( 1<<1 );
    public static final AuthLevel ADMINISTRATOR = new AuthLevel( 1<<2 );
    public static final AuthLevel OWNER = new AuthLevel( 1<<3 );
    public static final AuthLevel ALL = new AuthLevel( USER.mask | MODERATOR.mask | ADMINISTRATOR.mask | OWNER.mask );

    public boolean has(AuthLevel other) {
        return (this.mask & other.mask) != 0;
    }

    public AuthLevel add(AuthLevel other) {
        return new AuthLevel(this.mask | other.mask);
    }

    public AuthLevel subtract(AuthLevel other) {
        return new AuthLevel(this.mask - other.mask);
    }

    //test
    public AuthLevel below() {
        if ( mask == USER.mask || mask == NONE.mask ) return NONE;

        int cmpMask = 1;
        while ( (cmpMask & this.mask) == 0 ) {
            cmpMask >>= 1;
            cmpMask += 1;
        }

        return new AuthLevel(cmpMask);
    }

    public AuthLevel above() {
        if ( mask == OWNER.mask ) return NONE;

        int cmpMask = mask << 1;
        while ( (cmpMask & OWNER.mask) == 0 ) {
            cmpMask <<= 1;
            cmpMask += mask << 1;
        }

        return new AuthLevel(cmpMask);
    }

    @Override
    public String toString() {
        return "AuthLevel{" +
                "mask=" + Integer.toBinaryString(mask) +
                "contains=" + containedLevels() +
                '}';
    }

    public String containedLevels() {
        ArrayList<String> ret = new ArrayList<>();

        if ( has(USER) ) ret.add("USER");
        if ( has(MODERATOR) ) ret.add("MODERATOR");
        if ( has(ADMINISTRATOR) ) ret.add("ADMINISTRATOR");
        if ( has(OWNER) ) ret.add("OWNER");

        if ( ret.isEmpty() ) return "NONE";

        return ret.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthLevel authLevel = (AuthLevel) o;

        return mask == authLevel.mask;
    }

    @Override
    public int hashCode() {
        return mask;
    }
}
