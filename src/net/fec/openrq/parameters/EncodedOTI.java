package net.fec.openrq.parameters;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class EncodedOTI {

    // Enum state to avoid throwing exceptions when the user parses an illegal encoded OTI.
    public static enum OTIState {

        VALID,
        INVALID_DATA_LENGTH,
        INVALID_SYMBOL_SIZE,
        INVALID_NUM_SOURCE_BLOCKS,
        INVALID_NUM_SUB_BLOCKS;
    }


    // TODO add symbol alignment value

    /**
     * Writes in the provided buffer the <i>encoded FEC Object Transmission Information (OTI)</i>, which contains the
     * provided transport parameters.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
     * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
     * buffer will have advanced by 12 bytes.
     * 
     * @param params
     *            The transport parameters associated to the encodable object
     * @param buffer
     *            A buffer on which the encoded OTI is written
     * @exception NullPointerException
     *                If the provided transport parameters or the buffer are {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public void writeEncodedOTI(TransportParams params, ByteBuffer buffer) {

        // write F, reserved, T
        ParameterChecks.writeDataLength(params.getDataLength(), buffer); // 5 bytes
        buffer.put((byte)0);                                             // 1 byte
        ParameterChecks.writeSymbolSize(params.getSymbolSize(), buffer); // 2 bytes

        // write Z, N, Al
        ParameterChecks.writeNumSourceBlocks(params.getNumberOfSourceBlocks(), buffer);      // 1 byte
        ParameterChecks.writeNumSubBlocks(params.getNumberOfSubBlocks(), buffer);            // 2 bytes
        // TODO replace default value with a proper symbol alignment value
        ParameterChecks.writeSymbolAlignment(TransportDeriver.DEF_SYMBOL_ALIGNMENT, buffer); // 1 byte
    }

    /**
     * Reads from the provided buffer the <i>encoded FEC Object Transmission Information (OTI)</i>, which contains the
     * provided transport parameters.
     * <p>
     * The provided buffer must have at least 12 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 12 bytes.
     * <p>
     * The returned {@code EncodedOTI} instance is only {@linkplain #isValid() valid} if the transport parameters
     * contained inside the buffer have valid values. If some transport parameter value is invalid, the method
     * {@link EncodedOTI#getState()} can be used to infer which parameter value is invalid.
     * 
     * @param buffer
     *            A buffer from which the encoded OTI is read
     * @return an encoded OTI
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public EncodedOTI readEncodedOTI(ByteBuffer buffer) {

        // read F, reserved, T
        final long dataLen = ParameterChecks.readDataLength(buffer); // 5 bytes
        buffer.get();                                                   // 1 byte
        final int symbolSize = ParameterChecks.readSymbolSize(buffer);  // 2 bytes

        // read Z, N, Al
        final int numSourceBlocks = ParameterChecks.readNumSourceBlocks(buffer); // 1 byte
        final int numSubBlocks = ParameterChecks.readNumSubBlocks(buffer);       // 2 bytes
        // TODO store and check the read symbol alignment value
        ParameterChecks.readSymbolAlignment(buffer);                             // 1 byte

        if (!ParameterChecks.isValidDataLength(dataLen)) {
            return new EncodedOTI(OTIState.INVALID_DATA_LENGTH, null);
        }
        else if (!ParameterChecks.isValidSymbolSize(symbolSize)) {
            return new EncodedOTI(OTIState.INVALID_SYMBOL_SIZE, null);
        }
        else if (!ParameterChecks.isValidNumSourceBlocks(numSourceBlocks)) {
            return new EncodedOTI(OTIState.INVALID_NUM_SOURCE_BLOCKS, null);
        }
        else if (!ParameterChecks.isValidNumSubBlocks(numSubBlocks)) {
            return new EncodedOTI(OTIState.INVALID_NUM_SUB_BLOCKS, null);
        }
        else {
            return new EncodedOTI(
                OTIState.VALID,
                TransportParams.makeTransportParameters(dataLen, symbolSize, numSourceBlocks, numSubBlocks));
        }
    }


    private final OTIState state;
    private final TransportParams transParams;


    EncodedOTI(OTIState state, TransportParams transParams) {

        this.state = state;
        this.transParams = transParams;
    }

    public boolean isValid() {

        return state == OTIState.VALID;
    }

    public OTIState getState() {

        return state;
    }

    public TransportParams getTransportParameters() {

        checkValid();
        return transParams;
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
