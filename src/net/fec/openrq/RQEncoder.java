package net.fec.openrq;


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.List;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface RQEncoder {

	/**
	 * Returns an immutable list with the source blocks of this encoder.
	 * <p>
	 * Each source block is capable of producing encoding symbols independently from other source blocks. It is possible
	 * to encode symbols from different source blocks in parallel.
	 * 
	 * @return an immutable list with the source blocks of this encoder
	 */
	public List<SourceBlock> getSourceBlocks();

	/**
	 * Writes the header of the object in a buffer. The header contains the <i>encoded FEC Object Transmission
	 * Information</i>
	 * associated to this encoder's object.
	 * <p>
	 * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
	 * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
	 * buffer will have advanced by 12 bytes.
	 * 
	 * @param buffer
	 *            a buffer on which the object header is written
	 * @exception NullPointerException
	 *                If the provided buffer is {@code null}
	 * @exception ReadOnlyBufferException
	 *                If the provided buffer is read-only
	 * @exception BufferOverflowException
	 *                If the provided buffer has less than 12 bytes remaining
	 */
	public void writeObjectHeader(ByteBuffer buffer);

	/**
	 * Returns the header of the object as an array of bytes. The header contains the <i>encoded FEC Object Transmission
	 * Information</i>
	 * associated to this encoder's object.
	 * 
	 * @return an array of bytes containing the header of the object
	 */
	public byte[] getObjectHeader();
}
