package net.fec.openrq.util.numericaltype;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * This class provides useful methods for dealing with primitive unsigned values, including reading from and writing to
 * buffers.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
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

	public static int readUnsignedByte(ByteBuffer buffer) {

		return (int)(buffer.get() & UNSIGNED_BYTE_MASK);
	}

	public static void writeUnsignedByte(int ub, ByteBuffer buffer) {

		buffer.put((byte)ub);
	}

	// UNSIGNED SHORT //

	public static int getUnsignedShort(int i) {

		return (int)(i & UNSIGNED_SHORT_MASK);
	}

	public static int readUnsignedShort(ByteBuffer buffer) {

		return (int)(buffer.getShort() & UNSIGNED_SHORT_MASK);
	}

	public static void writeUnsignedShort(int us, ByteBuffer buffer) {

		buffer.putShort((short)us);
	}

	// UNSIGNED INT //

	public static long getUnsignedInt(long l) {

		return l & UNSIGNED_INT_MASK;
	}

	public static long readUnsignedInt(ByteBuffer buffer) {

		return buffer.getInt() & UNSIGNED_INT_MASK;
	}

	public static void writeUnsignedInt(long ui, ByteBuffer buffer) {

		buffer.putInt((int)ui);
	}

	// UNSIGNED ARBITRARY //

	public static long readUnsignedBytes(ByteBuffer buffer, int numBytes) {

		if (numBytes < 0 || numBytes >= SizeOf.LONG) throw new IllegalArgumentException("illegal number of bytes");

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

	public static void writeUnsignedBytes(long ubs, ByteBuffer buffer, int numBytes) {

		if (numBytes < 0 || numBytes >= SizeOf.LONG) throw new IllegalArgumentException("illegal number of bytes");

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
