package net.fec.openrq.util.checking;

/**
 * This exception should never be caught. Its purpose is only to indicate development time errors.
 * 
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 */
public final class InvariantsAssertionException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public InvariantsAssertionException() {

        super(getMyMessage());
    }

    public InvariantsAssertionException(Throwable cause) {

        super(getMyMessage(), cause);
    }

    private static String getMyMessage() {

        return "Invariants do not hold";
    }
}
