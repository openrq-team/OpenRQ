package net.fec.openrq.parameters;


import java.nio.ByteBuffer;

import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * This class provides static methods to check the validity of values with specific semantics.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class Params {

    // the RFC specifies a non-negative value, but we force a positive value here
    private static final long MIN_F = 1L;
    private static final long MAX_F = 946270874880L;
    private static final int NUM_BYTES_F = 5;

    private static final int MIN_T = 1;
    private static final int MAX_T = (1 << 16) - 1;

    private static final int MIN_Z = 1;
    private static final int MAX_Z = 1 << 8;

    private static final int MIN_N = 1;
    private static final int MAX_N = 56403;

    private static final int MIN_SBN = 0;
    private static final int MAX_SBN = (1 << 8) - 1;

    private static final int MIN_ESI = 0;
    private static final int MAX_ESI = (1 << 24) - 1;
    private static final int NUM_BYTES_ESI = 3;

    private static final int MIN_NUM_SYMBOLS = 1;
    private static final int MAX_NUM_SYMBOLS = 1 << 16;


    // =========== data length - F ========== //

    static boolean isValidDataLength(long dataLen) {

        return dataLen >= MIN_F && dataLen <= MAX_F;
    }

    static void writeDataLength(long dataLen, ByteBuffer buffer) {

        // 40-bit value
        UnsignedTypes.writeLongUnsignedBytes(dataLen, buffer, NUM_BYTES_F);
    }

    static long readDataLength(ByteBuffer buffer) {

        // 40-bit value
        return UnsignedTypes.readLongUnsignedBytes(buffer, NUM_BYTES_F);
    }

    // =========== symbol size - T ========== //

    static boolean isValidSymbolSize(int symbolSize) {

        return symbolSize >= MIN_T && symbolSize <= MAX_T;
    }

    static int unsignSymbolSize(int symbolSize) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(symbolSize);
    }

    static void writeSymbolSize(int symbolSize, ByteBuffer buffer) {

        // 16-bit value
        UnsignedTypes.writeUnsignedShort(symbolSize, buffer);
    }

    static int readSymbolSize(ByteBuffer buffer) {

        // 16-bit value
        return UnsignedTypes.readUnsignedShort(buffer);
    }

    // =========== number of source blocks - Z ========== //

    static boolean isValidNumSourceBlocks(int numSourceBlocks) {

        return numSourceBlocks >= MIN_Z && numSourceBlocks <= MAX_Z;
    }

    /*
     * The RFC specifies a minimum of 1 and a maximum of 2^8 for the number of source blocks.
     * An unsigned byte only fits values from [0, (2^8)-1].
     * So, consider value 0 as 2^8 (unsignedByte(2^8) == 0)
     */

    static int unsignNumSourceBlocks(int numSourceBlocks) {

        // positive 8-bit value
        return UnsignedTypes.getPositiveUnsignedByte(numSourceBlocks);
    }

    static void writeNumSourceBlocks(int numSourceBlocks, ByteBuffer buffer) {

        // positive 8-bit value
        UnsignedTypes.writeUnsignedByte(numSourceBlocks, buffer);
    }

    static int readNumSourceBlocks(ByteBuffer buffer) {

        // positive 8-bit value
        return UnsignedTypes.readPositiveUnsignedByte(buffer);
    }

    // =========== number of sub-blocks - N ========== //

    static boolean isValidNumSubBlocks(int numSubBlocks) {

        return numSubBlocks >= MIN_N && numSubBlocks <= MAX_N;
    }

    static int unsignNumSubBlocks(int numSubBlocks) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(numSubBlocks);
    }

    static void writeNumSubBlocks(int numSubBlocks, ByteBuffer buffer) {

        // 16-bit value
        UnsignedTypes.writeUnsignedShort(numSubBlocks, buffer);
    }

    static int readNumSubBlocks(ByteBuffer buffer) {

        // 16-bit value
        return UnsignedTypes.readUnsignedShort(buffer);
    }

    // =========== symbol alignment - Al ========== //

    // TODO add symbol alignment check

    static int unsignSymbolAlignment(int symbolAlignment) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(symbolAlignment);
    }

    static void writeSymbolAlignment(int symbolAlignment, ByteBuffer buffer) {

        // 8-bit value
        UnsignedTypes.writeUnsignedByte(symbolAlignment, buffer);
    }

    static int readSymbolAlignment(ByteBuffer buffer) {

        // 8-bit value
        return UnsignedTypes.readUnsignedByte(buffer);
    }

    // =========== source block number - SBN ========== //

    static boolean isValidSourceBlockNumber(int sourceBlockNum) {

        return sourceBlockNum >= MIN_SBN && sourceBlockNum <= MAX_SBN;
    }

    static int unsignSourceBlockNumber(int sourceBlockNum) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(sourceBlockNum);
    }

    static int extractSourceBlockNumber(int fecPayloadID) {

        return unsignSourceBlockNumber(fecPayloadID >>> (NUM_BYTES_ESI * Byte.SIZE));
    }

    // =========== encoding symbol identifier - ESI ========== //

    static boolean isValidEncodingSymbolID(int encSymbolID) {

        return encSymbolID >= MIN_ESI && encSymbolID <= MAX_ESI;
    }

    static int unsignEncodingSymbolID(int encSymbolID) {

        // 24-bit value
        return UnsignedTypes.getUnsignedBytes(encSymbolID, NUM_BYTES_ESI);
    }

    static int extractEncodingSymbolID(int fecPayloadID) {

        return unsignEncodingSymbolID(fecPayloadID);
    }

    // =========== FEC payload ID - SBN|ESI ========== //

    static int buildFECpayloadID(int sourceBlockNum, int encSymbolID) {

        final int unsignedSBN = unsignSourceBlockNumber(sourceBlockNum);
        final int unsignedESI = unsignEncodingSymbolID(encSymbolID);
        return (unsignedSBN << (NUM_BYTES_ESI * Byte.SIZE)) | unsignedESI;
    }

    static boolean isValidFECpayloadID(int fecPayloadID) {

        return isValidSourceBlockNumber(extractSourceBlockNumber(fecPayloadID))
               && isValidEncodingSymbolID(extractEncodingSymbolID(fecPayloadID));
    }

    static void writeFECpayloadID(int fecPayloadID, ByteBuffer buffer) {

        // 32-bit value
        buffer.putInt(fecPayloadID);
    }

    static int readFECpayloadID(ByteBuffer buffer) {

        // 32-bit value
        return buffer.getInt();
    }

    // =========== number of symbols in an encoding packet ========== //

    static boolean isValidNumSymbols(int numSymbols) {

        return numSymbols >= MIN_NUM_SYMBOLS && numSymbols <= MAX_NUM_SYMBOLS;
    }

    /*
     * We specify a minimum of 1 and a maximum of 2^16 for the number of symbols.
     * An unsigned short only fits values from [0, (2^16)-1].
     * So, consider value 0 as 2^16 (unsignedShort(2^16) == 0)
     */

    static int unsignNumSymbols(short numSymbols) {

        // positive 16-bit value
        return UnsignedTypes.getPositiveUnsignedShort(numSymbols);
    }

    static void writeNumSymbols(int numSymbols, ByteBuffer buffer) {

        // positive 16-bit value
        UnsignedTypes.writeUnsignedShort(numSymbols, buffer);
    }

    static int readNumSymbols(ByteBuffer buffer) {

        // positive 16-bit value
        return UnsignedTypes.readPositiveUnsignedShort(buffer);
    }
}
