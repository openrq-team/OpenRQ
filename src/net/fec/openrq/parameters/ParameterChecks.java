package net.fec.openrq.parameters;


import java.nio.ByteBuffer;

import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ParameterChecks {

    private static final long MAX_OBJECT_SIZE = 946270874880L;
    private static final int MAX_SYMBOL_SIZE = (1 << 16) - 1;
    private static final int MAX_NUM_SOURCE_BLOCKS = 1 << 8;
    private static final int MAX_NUM_SUB_BLOCKS = 56403;


    // =========== object size - F ========== //

    /**
     * @param objectSize
     * @return
     */
    public static boolean isValidObjectSize(long objectSize) {

        // the RFC specifies a non-negative value, but we force a positive value here
        return objectSize > 0 && objectSize <= MAX_OBJECT_SIZE;
    }

    /*
     * Requires isValidObjectSize(objectSize)
     */
    static void writeObjectSize(long objectSize, ByteBuffer buffer) {

        UnsignedTypes.writeUnsignedBytes(objectSize, buffer, 5);
    }

    static long readObjectSize(ByteBuffer buffer) {

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

    private ParameterChecks() {

        // not instantiable
    }
}
