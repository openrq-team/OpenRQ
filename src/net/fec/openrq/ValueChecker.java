package net.fec.openrq;


import java.nio.ByteBuffer;

import net.fec.openrq.util.numericaltype.SizeOf;
import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * This class provides static methods to check the validity of values with specific semantics.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ValueChecker {

    // the RFC specifies a non-negative value, but we force a positive value here
    private static final long MIN_F = 1L;
    private static final long MAX_F = 946270874880L;

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

    private static final int MIN_NUM_SYMBOLS = 1;
    private static final int MAX_NUM_SYMBOLS = 1 << 16;


    // =========== data length - F ========== //

    /**
     * @param dataLen
     * @return
     */
    public static boolean isValidDataLength(long dataLen) {

        return dataLen >= MIN_F && dataLen <= MAX_F;
    }

    static int numBytesOfDataLen() {

        return 5 * SizeOf.UNSIGNED_BYTE;
    }

    /*
     * Requires isValidDataLength(dataLen)
     */
    static long maskDataLength(long dataLen) {

        // 40-bit value
        return UnsignedTypes.getLongUnsignedBytes(dataLen, numBytesOfDataLen());
    }

    /*
     * Requires isValidDataLength(dataLen)
     */
    static void writeDataLength(long dataLen, ByteBuffer buffer) {

        // 40-bit value
        UnsignedTypes.writeLongUnsignedBytes(dataLen, buffer, numBytesOfDataLen());
    }

    static long readDataLength(ByteBuffer buffer) {

        // 40-bit value
        return UnsignedTypes.readLongUnsignedBytes(buffer, numBytesOfDataLen());
    }

    // =========== symbol size - T ========== //

    /**
     * @param symbolSize
     * @return
     */
    public static boolean isValidSymbolSize(int symbolSize) {

        return symbolSize >= MIN_T && symbolSize <= MAX_T;
    }

    static int numBytesOfSymbolSize() {

        return SizeOf.UNSIGNED_SHORT;
    }

    /*
     * Requires isValidSymbolSize(symbolSize)
     */
    static int maskSymbolSize(short symbolSize) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(symbolSize);
    }

    /*
     * Requires isValidSymbolSize(symbolSize)
     */
    static void writeSymbolSize(int symbolSize, ByteBuffer buffer) {

        // 16-bit value
        UnsignedTypes.writeUnsignedShort(symbolSize, buffer);
    }

    static int readSymbolSize(ByteBuffer buffer) {

        // 16-bit value
        return UnsignedTypes.readUnsignedShort(buffer);
    }

    // =========== number of source blocks - Z ========== //

    /**
     * @param numSourceBlocks
     * @return
     */
    public static boolean isValidNumSourceBlocks(int numSourceBlocks) {

        return numSourceBlocks >= MIN_Z && numSourceBlocks <= MAX_Z;
    }

    static int numBytesOfNumSourceBlocks() {

        return SizeOf.UNSIGNED_BYTE;
    }

    /*
     * Requires isValidNumSourceBlocks(numSourceBlocks)
     */
    static int maskNumSourceBlocks(byte numSourceBlocks) {

        // positive 8-bit value
        return UnsignedTypes.getPositiveUnsignedByte(numSourceBlocks);
    }

    /*
     * Requires isValidNumSourceBlocks(numSourceBlocks)
     */
    static void writeNumSourceBlocks(int numSourceBlocks, ByteBuffer buffer) {

        // The RFC specifies a minimum of 1 and a maximum of 2^8 for the number of source blocks.
        // An unsigned byte only fits values from [0, (2^8)-1].
        // So, consider value 0 as 2^8 (unsignedByte(2^8) == 0)

        // positive 8-bit value
        UnsignedTypes.writeUnsignedByte(numSourceBlocks, buffer);
    }

    static int readNumSourceBlocks(ByteBuffer buffer) {

        // positive 8-bit value
        return UnsignedTypes.readPositiveUnsignedByte(buffer);
    }

    // =========== number of sub-blocks - N ========== //

    /**
     * @param numSubBlocks
     * @return
     */
    public static boolean isValidNumSubBlocks(int numSubBlocks) {

        return numSubBlocks >= MIN_N && numSubBlocks <= MAX_N;
    }

    static int numBytesOfNumSubBlocks() {

        return SizeOf.UNSIGNED_SHORT;
    }

    /*
     * Requires isValidNumSubBlocks(numSubBlocks)
     */
    static int maskNumSubBlocks(short numSubBlocks) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(numSubBlocks);
    }

    /*
     * Requires isValidNumSubBlocks(numSubBlocks)
     */
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

    static int numBytesOfSymbolAlignment() {

        return SizeOf.UNSIGNED_BYTE;
    }

    static int maskSymbolAlignment(byte symbolAlignment) {

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

    /**
     * @param sourceBlockNum
     * @return
     */
    public static boolean isValidSourceBlockNumber(int sourceBlockNum) {

        return sourceBlockNum >= MIN_SBN && sourceBlockNum <= MAX_SBN;
    }

    static int numBytesOfSourceBlockNumber() {

        return SizeOf.UNSIGNED_BYTE;
    }

    /*
     * Requires isValidSourceBlockNumber(sourceBlockNum)
     */
    static int maskSourceBlockNumber(byte sourceBlockNum) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(sourceBlockNum);
    }

    /*
     * Requires isValidSourceBlockNumber(sourceBlockNum)
     */
    static void writeSourceBlockNumber(int sourceBlockNum, ByteBuffer buffer) {

        // 8-bit value
        UnsignedTypes.writeUnsignedByte(sourceBlockNum, buffer);
    }

    static int readSourceBlockNumber(ByteBuffer buffer) {

        // 8-bit value
        return UnsignedTypes.readUnsignedByte(buffer);
    }

    // =========== encoding symbol identifier - ESI ========== //

    public static boolean isValidEncodingSymbolID(int encSymbolID) {

        return encSymbolID >= MIN_ESI && encSymbolID <= MAX_ESI;
    }

    static int numBytesOfEncodingSymbolID() {

        return 3 * SizeOf.UNSIGNED_BYTE;
    }

    /*
     * Requires isValidEncodingSymbolID(encSymbolID)
     */
    static int maskEncodingSymbolID(int encSymbolID) {

        // 24-bit value
        return UnsignedTypes.getUnsignedBytes(encSymbolID, numBytesOfEncodingSymbolID());
    }

    /*
     * Requires isValidEncodingSymbolID(encSymbolID)
     */
    static void writeEncodingSymbolID(int encSymbolID, ByteBuffer buffer) {

        // 24-bit value
        UnsignedTypes.writeUnsignedBytes(encSymbolID, buffer, numBytesOfEncodingSymbolID());
    }

    static int readEncodingSymbolID(ByteBuffer buffer) {

        // 24-bit value
        return UnsignedTypes.readUnsignedBytes(buffer, numBytesOfEncodingSymbolID());
    }

    // =========== number of symbols in an encoding packet ========== //

    /**
     * @param numSymbols
     * @return
     */
    public static boolean isValidNumSymbols(int numSymbols) {

        return numSymbols >= MIN_NUM_SYMBOLS && numSymbols <= MAX_NUM_SYMBOLS;
    }

    static int numBytesOfNumSybols() {

        return SizeOf.UNSIGNED_SHORT;
    }

    /*
     * Requires isValidNumSymbols(numSymbols)
     */
    static int maskNumSymbols(short numSymbols) {

        // positive 16-bit value
        return UnsignedTypes.getPositiveUnsignedShort(numSymbols);
    }

    /*
     * Requires isValidNumSymbols(numSymbols)
     */
    static void writeNumSymbols(int numSymbols, ByteBuffer buffer) {

        // We specify a minimum of 1 and a maximum of 2^16 for the number of symbols.
        // An unsigned short only fits values from [0, (2^16)-1].
        // So, consider value 0 as 2^16 (unsignedShort(2^16) == 0)

        // positive 16-bit value
        UnsignedTypes.writeUnsignedShort(numSymbols, buffer);
    }

    static int readNumSymbols(ByteBuffer buffer) {

        // positive 16-bit value
        return UnsignedTypes.readPositiveUnsignedShort(buffer);
    }
}
