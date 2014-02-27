package net.fec.openrq.parameters;


import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ParameterChecks {

    private static final long MAX_OBJECT_SIZE = 946270874880L;


    public static boolean isValidObjectSize(long objectSize) {

        // non-negative, so it may be 0
        return objectSize >= 0 && objectSize < MAX_OBJECT_SIZE;
    }

    public static boolean isValidSymbolSize(int symbolSize) {

        return symbolSize > 0 && symbolSize <= UnsignedTypes.MAX_UNSIGNED_SHORT_VALUE;
    }

    public static boolean isValidNumSourceBlocks(int numSourceBlocks) {

        return numSourceBlocks > 0 && numSourceBlocks <= UnsignedTypes.MAX_UNSIGNED_BYTE_VALUE;
    }

    public static boolean isValidNumSubBlocks(int numSubBlocks) {

        return numSubBlocks > 0 && numSubBlocks <= UnsignedTypes.MAX_UNSIGNED_SHORT_VALUE;
    }

    // TODO add symbol alignment check

    private ParameterChecks() {

        // not instantiable
    }
}
