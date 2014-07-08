package net.fec.openrq.util.invariants;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Invariants {

    public static void assertInvariants(boolean invariants) {

        if (!invariants) {
            throw new InvariantsAssertionException();
        }
    }

    public static void assertInvariants(boolean invariants, Throwable cause) {

        if (!invariants) {
            throw new InvariantsAssertionException(cause);
        }
    }

    private Invariants() {

        // not used
    }
}
