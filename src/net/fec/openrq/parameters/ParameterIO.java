package net.fec.openrq.parameters;


import java.nio.ByteBuffer;

import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public class ParameterIO {

    // =========== data length - F ========== //

    /**
     * @param dataLen
     * @param buffer
     */
    public static void writeDataLength(long dataLen, ByteBuffer buffer) {

        // 40-bit value
        UnsignedTypes.writeLongUnsignedBytes(dataLen, buffer, Params.NUM_BYTES_F);
    }

    /**
     * @param buffer
     * @return
     */
    public static long readDataLength(ByteBuffer buffer) {

        // 40-bit value
        return UnsignedTypes.readLongUnsignedBytes(buffer, Params.NUM_BYTES_F);
    }

    // =========== symbol size - T ========== //

    /**
     * @param symbolSize
     * @return
     */
    public static int unsignSymbolSize(int symbolSize) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(symbolSize);
    }

    /**
     * @param symbolSize
     * @param buffer
     */
    public static void writeSymbolSize(int symbolSize, ByteBuffer buffer) {

        // 16-bit value
        UnsignedTypes.writeUnsignedShort(symbolSize, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readSymbolSize(ByteBuffer buffer) {

        // 16-bit value
        return UnsignedTypes.readUnsignedShort(buffer);
    }

    // =========== number of source blocks - Z ========== //

    /*
     * The RFC specifies a minimum of 1 and a maximum of 2^8 for the number of source blocks.
     * An unsigned byte only fits values from [0, (2^8)-1].
     * So, consider value 0 as 2^8 (unsignedByte(2^8) == 0)
     */

    /**
     * @param numSourceBlocks
     * @return
     */
    public static int unsignNumSourceBlocks(int numSourceBlocks) {

        // positive 8-bit value
        return UnsignedTypes.getPositiveUnsignedByte(numSourceBlocks);
    }

    /**
     * @param numSourceBlocks
     * @param buffer
     */
    public static void writeNumSourceBlocks(int numSourceBlocks, ByteBuffer buffer) {

        // positive 8-bit value
        UnsignedTypes.writeUnsignedByte(numSourceBlocks, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readNumSourceBlocks(ByteBuffer buffer) {

        // positive 8-bit value
        return UnsignedTypes.readPositiveUnsignedByte(buffer);
    }

    // =========== number of sub-blocks - N ========== //

    /**
     * @param numSubBlocks
     * @return
     */
    public static int unsignNumSubBlocks(int numSubBlocks) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(numSubBlocks);
    }

    /**
     * @param numSubBlocks
     * @param buffer
     */
    public static void writeNumSubBlocks(int numSubBlocks, ByteBuffer buffer) {

        // 16-bit value
        UnsignedTypes.writeUnsignedShort(numSubBlocks, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readNumSubBlocks(ByteBuffer buffer) {

        // 16-bit value
        return UnsignedTypes.readUnsignedShort(buffer);
    }

    // =========== symbol alignment - Al ========== //

    /**
     * @param symbolAlignment
     * @return
     */
    public static int unsignSymbolAlignment(int symbolAlignment) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(symbolAlignment);
    }

    /**
     * @param symbolAlignment
     * @param buffer
     */
    public static void writeSymbolAlignment(int symbolAlignment, ByteBuffer buffer) {

        // 8-bit value
        UnsignedTypes.writeUnsignedByte(symbolAlignment, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readSymbolAlignment(ByteBuffer buffer) {

        // 8-bit value
        return UnsignedTypes.readUnsignedByte(buffer);
    }

    // =========== source block number - SBN ========== //

    /**
     * @param sourceBlockNum
     * @return
     */
    public static int unsignSourceBlockNumber(int sourceBlockNum) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(sourceBlockNum);
    }

    /**
     * @param fecPayloadID
     * @return
     */
    public static int extractSourceBlockNumber(int fecPayloadID) {

        return unsignSourceBlockNumber(fecPayloadID >>> (Params.NUM_BYTES_ESI * Byte.SIZE));
    }

    // =========== encoding symbol identifier - ESI ========== //

    /**
     * @param encSymbolID
     * @return
     */
    public static int unsignEncodingSymbolID(int encSymbolID) {

        // 24-bit value
        return UnsignedTypes.getUnsignedBytes(encSymbolID, Params.NUM_BYTES_ESI);
    }

    /**
     * @param fecPayloadID
     * @return
     */
    public static int extractEncodingSymbolID(int fecPayloadID) {

        return unsignEncodingSymbolID(fecPayloadID);
    }

    // =========== FEC payload ID - SBN|ESI ========== //

    /**
     * @param sourceBlockNum
     * @param encSymbolID
     * @return
     */
    public static int buildFECpayloadID(int sourceBlockNum, int encSymbolID) {

        final int unsignedSBN = unsignSourceBlockNumber(sourceBlockNum);
        final int unsignedESI = unsignEncodingSymbolID(encSymbolID);
        return (unsignedSBN << (Params.NUM_BYTES_ESI * Byte.SIZE)) | unsignedESI;
    }

    /**
     * @param fecPayloadID
     * @param buffer
     */
    public static void writeFECpayloadID(int fecPayloadID, ByteBuffer buffer) {

        // 32-bit value
        buffer.putInt(fecPayloadID);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readFECpayloadID(ByteBuffer buffer) {

        // 32-bit value
        return buffer.getInt();
    }

    private ParameterIO() {

        // not instantiable
    }
}
