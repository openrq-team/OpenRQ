package net.fec.openrq.core.util.numericaltype;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * This class provides useful methods for dealing with primitive unsigned values, including reading from and writing to
 * buffers.
 * 
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class UnsignedTypes {

    private static final long UNSIGNED_BYTE_MASK = (1L << (SizeOf.UNSIGNED_BYTE * Byte.SIZE)) - 1;
    private static final long UNSIGNED_SHORT_MASK = (1L << (SizeOf.UNSIGNED_SHORT * Byte.SIZE)) - 1;
    private static final long UNSIGNED_INT_MASK = (1L << (SizeOf.UNSIGNED_INT * Byte.SIZE)) - 1L;

    /**
     * The maximum value for an unsigned byte.
     */
    public static final int MAX_UNSIGNED_BYTE_VALUE = (int)UNSIGNED_BYTE_MASK;

    /**
     * The maximum value for an unsigned short.
     */
    public static final int MAX_UNSIGNED_SHORT_VALUE = (int)UNSIGNED_SHORT_MASK;

    /**
     * The maximum value for an unsigned int.
     */
    public static final long MAX_UNSIGNED_INT_VALUE = UNSIGNED_INT_MASK;


    // UNSIGNED BYTE //

    public static int getUnsignedByte(int i) {

        return (int)(i & UNSIGNED_BYTE_MASK);
    }

    public static int getExtendedUnsignedByte(int i) {

        int intValue = getUnsignedByte(i);
        if (intValue == 0) intValue = MAX_UNSIGNED_BYTE_VALUE + 1;
        return intValue;
    }

    public static int readUnsignedByte(ByteBuffer buffer) {

        return getUnsignedByte(buffer.get());
    }

    public static int readExtendedUnsignedByte(ByteBuffer buffer) {

        return getExtendedUnsignedByte(buffer.get());
    }

    public static void writeUnsignedByte(int ub, ByteBuffer buffer) {

        buffer.put((byte)ub);
    }

    // UNSIGNED SHORT //

    public static int getUnsignedShort(int i) {

        return (int)(i & UNSIGNED_SHORT_MASK);
    }

    public static int getExtendedUnsignedShort(int i) {

        int intValue = getUnsignedShort(i);
        if (intValue == 0) intValue = MAX_UNSIGNED_SHORT_VALUE + 1;
        return intValue;
    }

    public static int readUnsignedShort(ByteBuffer buffer) {

        return getUnsignedShort(buffer.getShort());
    }

    public static int readExtendedUnsignedShort(ByteBuffer buffer) {

        return getExtendedUnsignedShort(buffer.getShort());
    }

    public static void writeUnsignedShort(int us, ByteBuffer buffer) {

        buffer.putShort((short)us);
    }

    // UNSIGNED INT //

    public static long getUnsignedInt(long el) {

        return el & UNSIGNED_INT_MASK;
    }

    public static long getExtendedUnsignedInt(long el) {

        long longValue = getUnsignedInt(el);
        if (longValue == 0) longValue = MAX_UNSIGNED_INT_VALUE + 1L;
        return longValue;
    }

    public static long readUnsignedInt(ByteBuffer buffer) {

        return getUnsignedInt(buffer.getInt());
    }

    public static long readExtendedUnsignedInt(ByteBuffer buffer) {

        return getExtendedUnsignedInt(buffer.getInt());
    }

    public static void writeUnsignedInt(long ui, ByteBuffer buffer) {

        buffer.putInt((int)ui);
    }

    // UNSIGNED ARBITRARY //

    public static int getUnsignedBytes(int ubs, int numBytes) {

        return (int)getUnsignedArbitrary(ubs, numBytes, SizeOf.INT);
    }

    public static long getLongUnsignedBytes(long ubs, int numBytes) {

        return getUnsignedArbitrary(ubs, numBytes, SizeOf.LONG);
    }

    private static long getUnsignedArbitrary(long ubs, int numBytes, int maxNumBytes) {

        if (numBytes < 0 || numBytes >= maxNumBytes) throw new IllegalArgumentException("illegal number of bytes");

        final long mask = (1L << (numBytes * Byte.SIZE)) - 1L;
        return ubs & mask;
    }

    public static int readUnsignedBytes(ByteBuffer buffer, int numBytes) {

        return (int)readUnsignedArbitrary(buffer, numBytes, SizeOf.INT);
    }

    public static long readLongUnsignedBytes(ByteBuffer buffer, int numBytes) {

        return readUnsignedArbitrary(buffer, numBytes, SizeOf.LONG);
    }

    private static long readUnsignedArbitrary(ByteBuffer buffer, int numBytes, int maxNumBytes) {

        if (numBytes < 0 || numBytes >= maxNumBytes) throw new IllegalArgumentException("illegal number of bytes");

        long ret = 0L;
        if (buffer.order() == ByteOrder.BIG_ENDIAN) {
            for (int n = numBytes - 1; n >= 0; n--) {
                ret |= (buffer.get() & UNSIGNED_BYTE_MASK) << (n * Byte.SIZE);
            }
        }
        else {
            for (int n = 0; n < numBytes; n++) {
                ret |= (buffer.get() & UNSIGNED_BYTE_MASK) << (n * Byte.SIZE);
            }
        }

        return ret;
    }

    public static void writeUnsignedBytes(int ubs, ByteBuffer buffer, int numBytes) {

        writeUnsignedArbitrary(ubs, buffer, numBytes, SizeOf.INT);
    }

    public static void writeLongUnsignedBytes(long ubs, ByteBuffer buffer, int numBytes) {

        writeUnsignedArbitrary(ubs, buffer, numBytes, SizeOf.LONG);
    }

    private static void writeUnsignedArbitrary(long ubs, ByteBuffer buffer, int numBytes, int maxNumBytes) {

        if (numBytes < 0 || numBytes >= maxNumBytes) throw new IllegalArgumentException("illegal number of bytes");

        if (buffer.order() == ByteOrder.BIG_ENDIAN) {
            for (int n = numBytes - 1; n >= 0; n--) {
                buffer.put((byte)(ubs >>> (n * Byte.SIZE)));
            }
        }
        else {
            for (int n = 0; n < numBytes; n++) {
                buffer.put((byte)(ubs >>> (n * Byte.SIZE)));
            }
        }
    }

    private UnsignedTypes() {

        // not instantiable
    }
}
