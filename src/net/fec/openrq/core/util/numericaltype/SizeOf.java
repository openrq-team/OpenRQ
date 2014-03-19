package net.fec.openrq.core.util.numericaltype;

/**
 * Useful byte sizes for primitive java types.
 * 
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class SizeOf {

    // NOTE:
    // Despite the BYTE size being redundant, it provides better code readability when
    // we wish to say that some specific information is representable as one byte.
    // The same applies for all the unsigned sizes.

    /**
     * The size of a {@code byte} in bytes.
     */
    public static final int BYTE = Byte.SIZE / Byte.SIZE;

    /**
     * The size of an unsigned {@code byte} in bytes.
     */
    public static final int UNSIGNED_BYTE = BYTE;

    /**
     * The size of a {@code char} in bytes.
     */
    public static final int CHAR = Character.SIZE / Byte.SIZE;

    /**
     * The size of a {@code short} in bytes.
     */
    public static final int SHORT = Short.SIZE / Byte.SIZE;

    /**
     * The size of an unsigned {@code short} in bytes.
     */
    public static final int UNSIGNED_SHORT = SHORT;

    /**
     * The size of an unsigned 3-byte number in bytes.
     */
    public static final int UNSIGNED_3_BYTES = 3 * BYTE;

    /**
     * The size of an {@code int} in bytes.
     */
    public static final int INT = Integer.SIZE / Byte.SIZE;

    /**
     * The size of an unsigned {@code int} in bytes.
     */
    public static final int UNSIGNED_INT = INT;

    /**
     * The size of a {@code long} in bytes.
     */
    public static final int LONG = Long.SIZE / Byte.SIZE;

    /**
     * The size of a {@code float} in bytes.
     */
    public static final int FLOAT = Float.SIZE / Byte.SIZE;

    /**
     * The size of a {@code double} in bytes.
     */
    public static final int DOUBLE = Double.SIZE / Byte.SIZE;


    /**
     * Returns the size in number of bytes required to hold the given (non-negative) amount of bits.
     * 
     * @param numBits
     *            A non-negative amount of bits
     * @return the size in number of bytes required to hold the given non-negative number of bits
     * @exception IllegalArgumentException
     *                If {@code numBits} is negative
     */
    public static final int bitsInBytes(int numBits) {

        if (numBits < 0) throw new IllegalArgumentException("numBits < 0");

        // there is a cast to a long here in order to avoid integer overflow
        return (int)((numBits + (Byte.SIZE - 1L)) / Byte.SIZE);
    }

    /**
     * Returns the size in number of chars required to hold the given (non-negative) amount of bits.
     * 
     * @param numBits
     *            A non-negative amount of bits
     * @return the size in number of chars required to hold the given non-negative number of bits
     * @exception IllegalArgumentException
     *                If {@code numBits} is negative
     */
    public static final int bitsInChars(int numBits) {

        if (numBits < 0) throw new IllegalArgumentException("numBits < 0");

        // there is a cast to a long here in order to avoid integer overflow
        return (int)((numBits + (Character.SIZE - 1L)) / Character.SIZE);
    }

    /**
     * Returns the size in number of shorts required to hold the given (non-negative) amount of bits.
     * 
     * @param numBits
     *            A non-negative amount of bits
     * @return the size in number of shorts required to hold the given non-negative number of bits
     * @exception IllegalArgumentException
     *                If {@code numBits} is negative
     */
    public static final int bitsInShorts(int numBits) {

        if (numBits < 0) throw new IllegalArgumentException("numBits < 0");

        // there is a cast to a long here in order to avoid integer overflow
        return (int)((numBits + (Short.SIZE - 1L)) / Short.SIZE);
    }

    /**
     * Returns the size in number of ints required to hold the given (non-negative) amount of bits.
     * 
     * @param numBits
     *            A non-negative amount of bits
     * @return the size in number of ints required to hold the given non-negative number of bits
     * @exception IllegalArgumentException
     *                If {@code numBits} is negative
     */
    public static final int bitsInInts(int numBits) {

        if (numBits < 0) throw new IllegalArgumentException("numBits < 0");

        // there is a cast to a long here in order to avoid integer overflow
        return (int)((numBits + (Integer.SIZE - 1L)) / Integer.SIZE);
    }

    /**
     * Returns the size in number of longs required to hold the given (non-negative) amount of bits.
     * 
     * @param numBits
     *            A non-negative amount of bits
     * @return the size in number of longs required to hold the given non-negative number of bits
     * @exception IllegalArgumentException
     *                If {@code numBits} is negative
     */
    public static final int bitsInLongs(int numBits) {

        if (numBits < 0) throw new IllegalArgumentException("numBits < 0");

        // there is a cast to a long here in order to avoid integer overflow
        return (int)((numBits + (Long.SIZE - 1L)) / Long.SIZE);
    }

    /**
     * Returns the size in number of floats required to hold the given (non-negative) amount of bits.
     * 
     * @param numBits
     *            A non-negative amount of bits
     * @return the size in number of floats required to hold the given non-negative number of bits
     * @exception IllegalArgumentException
     *                If {@code numBits} is negative
     */
    public static final int bitsInFloats(int numBits) {

        if (numBits < 0) throw new IllegalArgumentException("numBits < 0");

        // there is a cast to a long here in order to avoid integer overflow
        return (int)((numBits + (Float.SIZE - 1L)) / Float.SIZE);
    }

    /**
     * Returns the size in number of doubles required to hold the given (non-negative) amount of bits.
     * 
     * @param numBits
     *            A non-negative amount of bits
     * @return the size in number of doubles required to hold the given non-negative number of bits
     * @exception IllegalArgumentException
     *                If {@code numBits} is negative
     */
    public static final int bitsInDoubles(int numBits) {

        if (numBits < 0) throw new IllegalArgumentException("numBits < 0");

        // there is a cast to a long here in order to avoid integer overflow
        return (int)((numBits + (Double.SIZE - 1L)) / Double.SIZE);
    }

    private SizeOf() {

        // not instantiable
    }
}
