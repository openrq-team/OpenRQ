package net.fec.openrq;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.parameters.ParameterIO;


/**
 * This class represents FEC parameters and provides methods to read from and write to byte arrays and
 * {@link ByteBuffer} objects.
 * <p>
 * The format of the sequence of bytes containing the FEC parameters is defined in RFC 6330 as the <i>Encoded FEC Object
 * Transmission Information (OTI)</i>, which contains the <i>Encoded Common FEC OTI</i> and the <i>Encoded
 * Scheme-specific FEC OTI</i>.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class FECParameters {

    // Enum state to avoid throwing exceptions when the user parses illegal parameters.
    public static enum Validity {

        VALID,
        INVALID_DATA_LENGTH,
        INVALID_SYMBOL_SIZE,
        INVALID_NUM_SOURCE_BLOCKS,
        INVALID_NUM_SUB_BLOCKS;
    }


    /**
     * Reads from the provided buffer a {@code FECParameters} instance.
     * <p>
     * The provided buffer must have at least 12 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 12 bytes.
     * <p>
     * The returned {@code FECParameters} instance is only {@linkplain #isValid() valid} if the FEC parameters contained
     * inside the buffer have valid values. If some parameter value is invalid, the method {@link #getValidity()} can be
     * used to infer which parameter value is invalid.
     * 
     * @param buffer
     *            A buffer from which a {@code FECParameters} instance is read
     * @return a {@code FECParameters} instance
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public static FECParameters readFromBuffer(ByteBuffer buffer) {

        final long commonFecOTI = ParameterIO.readCommonFecOTI(buffer);        // 8 bytes
        final int schemeSpecFecOTI = ParameterIO.readSchemeSpecFecOTI(buffer); // 4 bytes

        if (!ParameterChecker.isValidDataLength(ParameterIO.extractDataLength(commonFecOTI))) {
            return new FECParameters(0L, 0, FECParameters.Validity.INVALID_DATA_LENGTH);
        }
        else if (!ParameterChecker.isValidSymbolSize(ParameterIO.extractSymbolSize(commonFecOTI))) {
            return new FECParameters(0L, 0, FECParameters.Validity.INVALID_SYMBOL_SIZE);
        }
        else if (!ParameterChecker.isValidNumSourceBlocks(ParameterIO.extractNumSourceBlocks(schemeSpecFecOTI))) {
            return new FECParameters(0L, 0, FECParameters.Validity.INVALID_NUM_SOURCE_BLOCKS);
        }
        else if (!ParameterChecker.isValidNumSubBlocks(ParameterIO.extractNumSubBlocks(schemeSpecFecOTI))) {
            return new FECParameters(0L, 0, FECParameters.Validity.INVALID_NUM_SUB_BLOCKS);
        }
        // TODO add symbol alignment check
        else {
            return new FECParameters(commonFecOTI, schemeSpecFecOTI, FECParameters.Validity.VALID);
        }
    }

    /**
     * Reads from the provided array starting in a specific index a {@code FECParameters} instance.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * <p>
     * The returned {@code FECParameters} instance is only {@linkplain #isValid() valid} if the FEC parameters contained
     * inside the buffer have valid values. If some parameter value is invalid, the method {@link #getValidity()} can be
     * used to infer which parameter value is invalid.
     * 
     * @param array
     *            An array from which a {@code FECParameters} instance is read
     * @param offset
     *            The starting array index at which a {@code FECParameters} instance is read
     * @return a {@code FECParameters} instance
     * @exception NullPointerException
     *                If the provided array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If a {@code FECParameters} instance cannot be read at the given index
     */
    public static FECParameters readFromArray(byte[] array, int offset) {

        if (offset < 0 || array.length - offset < 12) throw new IndexOutOfBoundsException();
        return readFromBuffer(ByteBuffer.wrap(array, offset, 12));
    }

    static FECParameters makeFECParameters(long F, int T, int Z, int N, int Al) {

        if (!ParameterChecker.isValidDataLength(F)) throw new IllegalArgumentException("invalid F");
        if (!ParameterChecker.isValidSymbolSize(T)) throw new IllegalArgumentException("invalid T");
        if (!ParameterChecker.isValidNumSourceBlocks(Z)) throw new IllegalArgumentException("invalid Z");
        if (!ParameterChecker.isValidNumSourceBlocks(N)) throw new IllegalArgumentException("invalid N");
        // TODO add symbol alignment check

        final long commonFecOTI = ParameterIO.buildCommonFecOTI(F, T);
        final int schemeSpecFecOTI = ParameterIO.buildSchemeSpecFecOTI(Z, N, Al);
        return new FECParameters(commonFecOTI, schemeSpecFecOTI, FECParameters.Validity.VALID);
    }


    private final long commonFecOTI;
    private final int schemeSpecFecOTI;
    private final FECParameters.Validity validity;


    private FECParameters(long commonFecOTI, int schemeSpecFecOTI, FECParameters.Validity validity) {

        this.commonFecOTI = commonFecOTI;
        this.schemeSpecFecOTI = schemeSpecFecOTI;
        this.validity = validity;
    }

    /**
     * Writes in the provided buffer a sequence of bytes that represent the FEC parameters.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
     * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
     * buffer will have advanced by 12 bytes.
     * 
     * @param buffer
     *            A buffer on which the FEC parameters are written
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     * @exception NullPointerException
     *                If the provided the buffer is {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public void writeToBuffer(ByteBuffer buffer) {

        checkValid();
        ParameterIO.writeCommonFecOTI(commonFecOTI, buffer);         // 8 bytes
        ParameterIO.writeSchemeSpecFecOTI(schemeSpecFecOTI, buffer); // 4 bytes
    }

    /**
     * Writes in the provided array starting in a specific index a sequence of bytes that represent the FEC parameters.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * 
     * @param array
     *            An array on which the FEC parameters are written
     * @param offset
     *            The starting array index at which the FEC parameters are written
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     * @exception NullPointerException
     *                If the provided the array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If the FEC parameters cannot be written at the given index
     */
    public void writeToArray(byte[] array, int offset) {

        checkValid();
        if (offset < 0 || array.length - offset < 12) throw new IndexOutOfBoundsException();
        writeToBuffer(ByteBuffer.wrap(array, offset, 12));
    }

    /**
     * Returns the length of the encodable data in number of bytes.
     * 
     * @return the length of the encodable data in number of bytes
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public long dataLength() {

        checkValid();
        return ParameterIO.extractDataLength(commonFecOTI);
    }

    /**
     * Returns the size of a symbol in number of bytes.
     * 
     * @return the size of a symbol in number of bytes
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public int symbolSize() {

        checkValid();
        return ParameterIO.extractSymbolSize(commonFecOTI);
    }

    /**
     * Returns the number of blocks into which the encodable data is partitioned.
     * 
     * @return the number of blocks into which the encodable data is partitioned
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public int numberOfSourceBlocks() {

        checkValid();
        return ParameterIO.extractNumSourceBlocks(schemeSpecFecOTI);
    }

    /**
     * Returns the number of sub-blocks per source block into which the encodable data is partitioned.
     * 
     * @return the number of sub-blocks per source block into which the encodable data is partitioned
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public int numberOfSubBlocks() {

        checkValid();
        return ParameterIO.extractNumSubBlocks(schemeSpecFecOTI);
    }

    /**
     * Returns the symbol alignment value.
     * 
     * @return the symbol alignment value
     */
    public int symbolAlignment() {

        checkValid();
        return ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);
    }

    /**
     * Returns {@code true} if, and only if, this {@code FECParameters} instance is valid, that is, if all FEC
     * parameters have valid values.
     * 
     * @return {@code true} if, and only if, this {@code FECParameters} instance is valid
     */
    public boolean isValid() {

        return validity == FECParameters.Validity.VALID;
    }

    /**
     * Returns an enum value indicating the (in)validity of this {@code FECParameters} instance.
     * 
     * @return an enum value indicating the (in)validity of this {@code FECParameters} instance
     */
    public FECParameters.Validity getValidity() {

        return validity;
    }

    private void checkValid() {

        if (!isValid()) {
            final String errorMsg;
            switch (validity) {
                case INVALID_DATA_LENGTH:
                    errorMsg = "invalid data length";
                break;
                case INVALID_SYMBOL_SIZE:
                    errorMsg = "invalid symbol size";
                break;
                case INVALID_NUM_SOURCE_BLOCKS:
                    errorMsg = "invalid number of source blocks";
                break;
                case INVALID_NUM_SUB_BLOCKS:
                    errorMsg = "invalid number of sub-blocks";
                break;
                default:
                    // should never happen
                    throw new AssertionError("unknown enum type");
            }
            throw new IllegalStateException(errorMsg);
        }
    }
}
