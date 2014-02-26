package net.fec.openrq;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Headers {

	/**
	 * Writes in the provided buffer the object header containing the provided common properties from an
	 * encoder/decoder. The header corresponds to the <i>encoded FEC Object Transmission Information</i>.
	 * <p>
	 * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
	 * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
	 * buffer will have advanced by 12 bytes.
	 * 
	 * @param buffer
	 *            A buffer on which the object header is written
	 * @exception NullPointerException
	 *                If the provided buffer is {@code null}
	 * @exception ReadOnlyBufferException
	 *                If the provided buffer is read-only
	 * @exception BufferOverflowException
	 *                If the provided buffer has less than 12 bytes remaining
	 */
	public void writeObjectHeader(RQCommonProperties props, ByteBuffer buffer) {

		// write F, reserved, T
		UnsignedTypes.writeUnsignedBytes(props.getObjectSize(), buffer, 5); 		// 5 bytes
		buffer.put((byte)0); 														// 1 byte
		UnsignedTypes.writeUnsignedShort(props.getSymbolSize(), buffer); 			// 2 bytes

		// write Z, N, Al
		UnsignedTypes.writeUnsignedByte(props.getNumberOfSourceBlocks(), buffer); 	// 1 byte
		UnsignedTypes.writeUnsignedShort(props.getNumberOfSubBlocks(), buffer);		// 2 bytes
		UnsignedTypes.writeUnsignedByte(props.getSymbolAlignment(), buffer); 		// 1 byte
	}

	/**
	 * Reads from the provided buffer the common properties for an encoder/decoder. The buffer must contain an object
	 * header, which corresponds to the <i>encoded FEC Object Transmission Information</i>.
	 * <p>
	 * The provided buffer must have at least 12 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
	 * returns normally, the position of the provided buffer will have advanced by 12 bytes.
	 * 
	 * @param buffer
	 *            A buffer from which the object header is read
	 * @return the common properties contained inside the read object header
	 * @exception NullPointerException
	 *                If the provided buffer is {@code null}
	 * @exception BufferUnderflowException
	 *                If the provided buffer has less than 12 bytes remaining
	 */
	public RQCommonProperties readProperties(ByteBuffer buffer) {

		// read F, reserved, T
		final long objectSize = UnsignedTypes.readUnsignedBytes(buffer, 5);	// 5 bytes
		buffer.get();														// 1 byte
		final int symbolSize = UnsignedTypes.readUnsignedShort(buffer);		// 2 bytes

		// read Z, N, Al
		final int numSourceBlocks = UnsignedTypes.readUnsignedByte(buffer);	// 1 byte
		final int numSubBlocks = UnsignedTypes.readUnsignedByte(buffer);	// 2 bytes
		final int symbolAlignment = UnsignedTypes.readUnsignedByte(buffer);	// 1 byte

		return new RQCommonProperties(objectSize, symbolSize, numSourceBlocks, numSubBlocks, symbolAlignment);
	}

	private Headers() {

		// not instantiable
	}
}
