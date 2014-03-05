package net.fec.openrq;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;


/**
 * This class facilitates a way to represent {@link DataParameters} instances as sequences of bytes, and provides
 * methods to read from and write to byte arrays and {@link ByteBuffer} objects.
 * <p>
 * The format of such a sequence of bytes is defined in RFC 6330 as the <i>encoded FEC Object Transmission
 * Information</i>.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class DataHeader {

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
        ValueChecker.writeDataLength(params.getDataLength(), buffer); // 5 bytes
        buffer.put((byte)0);                                            // 1 byte
        ValueChecker.writeSymbolSize(params.getSymbolSize(), buffer); // 2 bytes

        // write Z, N, Al
        ValueChecker.writeNumSourceBlocks(params.getNumberOfSourceBlocks(), buffer);       // 1 byte
        ValueChecker.writeNumSubBlocks(params.getNumberOfSubBlocks(), buffer);             // 2 bytes
        // TODO replace default value with a proper symbol alignment value
        ValueChecker.writeSymbolAlignment(DataParametersDeriver.DEF_SYMBOL_ALIGNMENT, buffer); // 1 byte
    }

    /**
     * Reads from the provided buffer a {@code DataHeader} instance.
     * <p>
     * The provided buffer must have at least 12 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 12 bytes.
     * <p>
     * The returned {@code DataParametersHeader} instance is only {@linkplain #isValid() valid} if the data parameters
     * contained inside the buffer have valid values. If some parameter value is invalid, the method
     * {@link DataHeader#getState()} can be used to infer which parameter value is invalid.
     * 
     * @param buffer
     *            A buffer from which a {@code DataParametersHeader} instance is read
     * @return a {@code DataHeader} instance
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public static DataHeader readHeader(ByteBuffer buffer) {

        // read F, reserved, T
        final long dataLen = ValueChecker.readDataLength(buffer);   // 5 bytes
        buffer.get();                                               // 1 byte
        final int symbolSize = ValueChecker.readSymbolSize(buffer); // 2 bytes

        // read Z, N, Al
        final int numSourceBlocks = ValueChecker.readNumSourceBlocks(buffer); // 1 byte
        final int numSubBlocks = ValueChecker.readNumSubBlocks(buffer);       // 2 bytes
        // TODO store and check the read symbol alignment value
        ValueChecker.readSymbolAlignment(buffer);                             // 1 byte

        if (!ValueChecker.isValidDataLength(dataLen)) {
            return new DataHeader(DataHeader.HeaderState.INVALID_DATA_LENGTH, null);
        }
        else if (!ValueChecker.isValidSymbolSize(symbolSize)) {
            return new DataHeader(DataHeader.HeaderState.INVALID_SYMBOL_SIZE, null);
        }
        else if (!ValueChecker.isValidNumSourceBlocks(numSourceBlocks)) {
            return new DataHeader(DataHeader.HeaderState.INVALID_NUM_SOURCE_BLOCKS, null);
        }
        else if (!ValueChecker.isValidNumSubBlocks(numSubBlocks)) {
            return new DataHeader(DataHeader.HeaderState.INVALID_NUM_SUB_BLOCKS, null);
        }
        else {
            return new DataHeader(
                DataHeader.HeaderState.VALID,
                new DataParameters(dataLen, symbolSize, numSourceBlocks, numSubBlocks));
        }
    }


    private final DataHeader.HeaderState state;
    private final DataParameters dataParams;


    DataHeader(DataHeader.HeaderState state, DataParameters dataParams) {

        this.state = state;
        this.dataParams = dataParams;
    }

    /**
     * @return
     */
    public boolean isValid() {

        return state == DataHeader.HeaderState.VALID;
    }

    /**
     * @return
     */
    public DataHeader.HeaderState getState() {

        return state;
    }

    /**
     * @return
     */
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
