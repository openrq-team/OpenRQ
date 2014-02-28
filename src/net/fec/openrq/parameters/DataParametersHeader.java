package net.fec.openrq.parameters;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;


/**
 * This class facilitates a way to represent {@link DataParameter} instances as sequences of bytes, and provides methods
 * to read from and write to byte arrays and {@link ByteBuffer} objects.
 * <p>
 * The format of such a sequence of bytes is defined in RFC 6330 as the <i>encoded FEC Object Transmission
 * Information</i>.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class DataParametersHeader {

    // Enum state to avoid throwing exceptions when the user parses an illegal header.
    public static enum HeaderState {

        VALID,
        INVALID_DATA_LENGTH,
        INVALID_SYMBOL_SIZE,
        INVALID_NUM_SOURCE_BLOCKS,
        INVALID_NUM_SUB_BLOCKS;
    }


    // TODO add symbol alignment value

    /**
     * Writes in the provided buffer a header containing a sequence of bytes that represent the provided data
     * parameters.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
     * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
     * buffer will have advanced by 12 bytes.
     * 
     * @param params
     *            The data parameters associated to the encodable data
     * @param buffer
     *            A buffer on which the data header is written
     * @exception NullPointerException
     *                If the provided data parameters or the buffer are {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public static void writeHeader(DataParameters params, ByteBuffer buffer) {

        // write F, reserved, T
        DataParameters.writeDataLength(params.getDataLength(), buffer); // 5 bytes
        buffer.put((byte)0);                                            // 1 byte
        DataParameters.writeSymbolSize(params.getSymbolSize(), buffer); // 2 bytes

        // write Z, N, Al
        DataParameters.writeNumSourceBlocks(params.getNumberOfSourceBlocks(), buffer);       // 1 byte
        DataParameters.writeNumSubBlocks(params.getNumberOfSubBlocks(), buffer);             // 2 bytes
        // TODO replace default value with a proper symbol alignment value
        DataParameters.writeSymbolAlignment(ParametersDeriver.DEF_SYMBOL_ALIGNMENT, buffer); // 1 byte
    }

    /**
     * Reads from the provided buffer a {@code DataParametersHeader} instance.
     * <p>
     * The provided buffer must have at least 12 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 12 bytes.
     * <p>
     * The returned {@code DataParametersHeader} instance is only {@linkplain #isValid() valid} if the data parameters
     * contained inside the buffer have valid values. If some parameter value is invalid, the method
     * {@link DataParametersHeader#getState()} can be used to infer which parameter value is invalid.
     * 
     * @param buffer
     *            A buffer from which a {@code DataParametersHeader} instance is read
     * @return an encoded OTI
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public static DataParametersHeader readEncodedOTI(ByteBuffer buffer) {

        // read F, reserved, T
        final long dataLen = DataParameters.readDataLength(buffer); // 5 bytes
        buffer.get();                                                   // 1 byte
        final int symbolSize = DataParameters.readSymbolSize(buffer);  // 2 bytes

        // read Z, N, Al
        final int numSourceBlocks = DataParameters.readNumSourceBlocks(buffer); // 1 byte
        final int numSubBlocks = DataParameters.readNumSubBlocks(buffer);       // 2 bytes
        // TODO store and check the read symbol alignment value
        DataParameters.readSymbolAlignment(buffer);                             // 1 byte

        if (!DataParameters.isValidDataLength(dataLen)) {
            return new DataParametersHeader(HeaderState.INVALID_DATA_LENGTH, null);
        }
        else if (!DataParameters.isValidSymbolSize(symbolSize)) {
            return new DataParametersHeader(HeaderState.INVALID_SYMBOL_SIZE, null);
        }
        else if (!DataParameters.isValidNumSourceBlocks(numSourceBlocks)) {
            return new DataParametersHeader(HeaderState.INVALID_NUM_SOURCE_BLOCKS, null);
        }
        else if (!DataParameters.isValidNumSubBlocks(numSubBlocks)) {
            return new DataParametersHeader(HeaderState.INVALID_NUM_SUB_BLOCKS, null);
        }
        else {
            return new DataParametersHeader(
                HeaderState.VALID,
                DataParameters.makeDataParameters(dataLen, symbolSize, numSourceBlocks, numSubBlocks));
        }
    }


    private final HeaderState state;
    private final DataParameters dataParams;


    DataParametersHeader(HeaderState state, DataParameters dataParams) {

        this.state = state;
        this.dataParams = dataParams;
    }

    public boolean isValid() {

        return state == HeaderState.VALID;
    }

    public HeaderState getState() {

        return state;
    }

    public DataParameters getDataParameters() {

        checkValid();
        return dataParams;
    }

    private void checkValid() {

        if (!isValid()) {
            final String errorMsg;
            switch (state) {
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
