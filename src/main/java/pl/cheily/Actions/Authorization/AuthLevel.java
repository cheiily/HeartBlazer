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
    private static final AuthLevel _highest = OWNER;
    private static final AuthLevel _lowest = USER;

    public boolean has(AuthLevel other) {
        return (this.mask & other.mask) != 0;
    }

    public AuthLevel add(AuthLevel other) {
        return new AuthLevel(this.mask | other.mask);
    }

    public AuthLevel subtract(AuthLevel other) {
        int m = this.mask;
        int n = other.mask;
        //carryover-allowed bit mask
        int c = 0;

        //start at the highest bit, go down
        for (int i = _highest.mask; i > 0; i--) {
            //if n[i] is unset
            if ( (n & (1 << i)) == 0 ) {
                //allow m[i] to pass through
                c |= (1 << i);
            }
        }
        //filter m through c
        m &= c;

        return new AuthLevel(m);
    }

    public AuthLevel below() {
        if ( mask == _lowest.mask || mask == NONE.mask ) return NONE;

        int cmpMask = mask >> 1;
        int iterMask = mask >> 1;
        while ( iterMask > 0 ) {
            iterMask >>= 1;
            cmpMask >>= 1;
            cmpMask += (mask >> 1);
        }

        return new AuthLevel(cmpMask);
    }

    public AuthLevel andBelow() {
        if ( mask == NONE.mask ) return NONE;
        if ( mask == _lowest.mask ) return _lowest;

        return new AuthLevel( this.mask | below().mask );
    }

    public AuthLevel above() {
        if ( mask == NONE.mask ) return ALL;
        if ( mask == _highest.mask ) return NONE;

        int cmpMask = mask << 1;
        while ( (cmpMask & _highest.mask) == 0 ) {
            cmpMask <<= 1;
            cmpMask += mask << 1;
        }

        return new AuthLevel(cmpMask);
    }

    public AuthLevel andAbove() {
        return new AuthLevel( this.mask | above().mask );
    }

    @Override
    public String toString() {
        return "AuthLevel{" +
                "mask=" + Integer.toBinaryString(mask) +
                ", contains=" + containedLevels() +
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
