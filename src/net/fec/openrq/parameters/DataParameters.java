package net.fec.openrq.parameters;


import java.nio.ByteBuffer;

import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class DataParameters {

    private static final long MAX_DATA_LENGTH = 946270874880L;
    private static final int MAX_SYMBOL_SIZE = (1 << 16) - 1;
    private static final int MAX_NUM_SOURCE_BLOCKS = 1 << 8;
    private static final int MAX_NUM_SUB_BLOCKS = 56403;


    // =========== data length - F ========== //

    /**
     * @param dataLen
     * @return
     */
    public static boolean isValidDataLength(long dataLen) {

        // the RFC specifies a non-negative value, but we force a positive value here
        return dataLen > 0 && dataLen <= MAX_DATA_LENGTH;
    }

    /*
     * Requires isValidDataLength(dataLen)
     */
    static void writeDataLength(long dataLen, ByteBuffer buffer) {

        UnsignedTypes.writeUnsignedBytes(dataLen, buffer, 5);
    }

    static long readDataLength(ByteBuffer buffer) {

        return UnsignedTypes.readUnsignedBytes(buffer, 5);
    }

    // =========== symbol size - T ========== //

    /**
     * @param symbolSize
     * @return
     */
    public static boolean isValidSymbolSize(int symbolSize) {

        return symbolSize > 0 && symbolSize <= MAX_SYMBOL_SIZE;
    }

    /*
     * Requires isValidSymbolSize(symbolSize)
     */
    static void writeSymbolSize(int symbolSize, ByteBuffer buffer) {

        UnsignedTypes.writeUnsignedShort(symbolSize, buffer);
    }

    static int readSymbolSize(ByteBuffer buffer) {

        return UnsignedTypes.readUnsignedShort(buffer);
    }

    // =========== number of source blocks - Z ========== //

    /**
     * @param numSourceBlocks
     * @return
     */
    public static boolean isValidNumSourceBlocks(int numSourceBlocks) {

        // the RFC specifies 2^8 as the maximum number of source blocks
        return numSourceBlocks > 0 && numSourceBlocks <= MAX_NUM_SOURCE_BLOCKS;
    }

    /*
     * Requires isValidNumSourceBlocks(numSourceBlocks)
     */
    static void writeNumSourceBlocks(int numSourceBlocks, ByteBuffer buffer) {

        // The RFC specifies a minimum of 1 and a maximum of 2^8 for the number of source blocks.
        // An unsigned byte only fits values from [0, (2^8)-1].
        // So, consider value 0 as 2^8 (unsignedByte(2^8) == 0)

        UnsignedTypes.writeUnsignedByte(numSourceBlocks, buffer);
    }

    static int readNumSourceBlocks(ByteBuffer buffer) {

        // The RFC specifies a minimum of 1 and a maximum of 2^8 for the number of source blocks.
        // An unsigned byte only fits values from [0, (2^8)-1].
        // Accept the value 0 and convert it to 2^8

        int numSourceBlocks = UnsignedTypes.readUnsignedByte(buffer);
        if (numSourceBlocks == 0) numSourceBlocks = MAX_NUM_SOURCE_BLOCKS;
        return numSourceBlocks;
    }

    // =========== number of sub-blocks - N ========== //

    /**
     * @param numSubBlocks
     * @return
     */
    public static boolean isValidNumSubBlocks(int numSubBlocks) {

        return numSubBlocks > 0 && numSubBlocks <= MAX_NUM_SUB_BLOCKS;
    }

    /*
     * Requires isValidNumSubBlocks(numSubBlocks)
     */
    static void writeNumSubBlocks(int numSubBlocks, ByteBuffer buffer) {

        UnsignedTypes.writeUnsignedShort(numSubBlocks, buffer);
    }

    static int readNumSubBlocks(ByteBuffer buffer) {

        return UnsignedTypes.readUnsignedShort(buffer);
    }

    // =========== symbol alignment - Al ========== //

    // TODO add symbol alignment check

    static void writeSymbolAlignment(int symbolAlignment, ByteBuffer buffer) {

        UnsignedTypes.writeUnsignedByte(symbolAlignment, buffer);
    }

    static int readSymbolAlignment(ByteBuffer buffer) {

        return UnsignedTypes.readUnsignedByte(buffer);
    }

    /**
     * Returns an instance of {@code DataParameters} with the provided values.
     * <p>
     * The following expressions must all be true so that a correct instance can be returned:
     * <ul>
     * <li>{@code DataParameters.isValidDataLength(dataLen)}</li>
     * <li>{@code DataParameters.isValidSymbolSize(symbolSize)}</li>
     * <li>{@code DataParameters.isValidNumSourceBlocks(numSourceBlocks)}</li>
     * <li>{@code DataParameters.isValidNumSubBlocks(numSubBlocks)}</li>
     * </ul>
     * otherwise, an {@code IllegalArgumentException} is thrown.
     * <p>
     * 
     * @param dataLen
     *            The length of the encodable data in number of bytes
     * @param symbolSize
     *            The symbol size in number of bytes
     * @param numSourceBlocks
     *            The number of blocks into which the encodable data is partitioned
     * @param numSubBlocks
     *            The number of sub-blocks per source block into which the encodable data is partitioned
     * @return a new {@code DataParameters} instance
     * @exception IllegalArgumentException
     *                If some parameter value is invalid
     */
    public static DataParameters makeDataParameters(
        long dataLen,
        int symbolSize,
        int numSourceBlocks,
        int numSubBlocks)
    {

        if (!isValidDataLength(dataLen)) {
            throw new IllegalArgumentException("invalid data length");
        }
        if (!isValidSymbolSize(symbolSize)) {
            throw new IllegalArgumentException("invalid symbol size");
        }
        if (!isValidNumSourceBlocks(numSourceBlocks)) {
            throw new IllegalArgumentException("invalid number of source blocks");
        }
        if (!isValidNumSubBlocks(numSubBlocks)) {
            throw new IllegalArgumentException("invalid number of sub-blocks");
        }

        return new DataParameters(dataLen, symbolSize, numSourceBlocks, numSubBlocks);
    }


    // minimal sized fields for space efficiency
    private final long dataLen;
    private final short symbolSize;
    private final byte numSourceBlocks;
    private final short numSubBlocks;


    /*
     * Package-private constructor. No checks are done to the arguments, since those are the responsibility of the
     * public factory methods.
     */
    DataParameters(long dataLen, int symbolSize, int numSourceBlocks, int numSubBlocks) {

        this.dataLen = dataLen;
        this.symbolSize = (short)symbolSize;
        this.numSourceBlocks = (byte)numSourceBlocks;
        this.numSubBlocks = (short)numSubBlocks;
    }

    /**
     * Returns the length of the encodable data in number of bytes.
     * 
     * @return the length of the encodable data in number of bytes
     */
    public long getDataLength() {

        return dataLen;
    }

    /**
     * Returns the symbol size in number of bytes.
     * 
     * @return the symbol size in number of bytes
     */
    public int getSymbolSize() {

        return UnsignedTypes.getUnsignedShort(symbolSize);
    }

    /**
     * Returns the number of blocks into which the encodable data is partitioned.
     * 
     * @return the number of blocks into which the encodable data is partitioned
     */
    public int getNumberOfSourceBlocks() {

        return UnsignedTypes.getUnsignedByte(numSourceBlocks);
    }

    /**
     * Returns the number of sub-blocks per source block into which the encodable data is partitioned.
     * 
     * @return the number of sub-blocks per source block into which the encodable data is partitioned
     */
    public int getNumberOfSubBlocks() {

        return UnsignedTypes.getUnsignedShort(numSubBlocks);
    }
}
