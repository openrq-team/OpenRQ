package net.fec.openrq.parameters;


import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class TransportParams {

    /**
     * Returns an instance of {@code TransportParams} with the provided values.
     * <p>
     * The class {@link ParameterChecks} is used to validate the provided values. The following expressions must all be
     * true so that a correct instance can be returned:
     * <ul>
     * <li>{@code ParameterChecks.isValidObjectSize(objectSize)}</li>
     * <li>{@code ParameterChecks.isValidSymbolSize(symbolSize)}</li>
     * <li>{@code ParameterChecks.isValidNumSourceBlocks(numSourceBlocks)}</li>
     * <li>{@code ParameterChecks.isValidNumSubBlocks(numSubBlocks)}</li>
     * </ul>
     * otherwise, an {@code IllegalArgumentException} is thrown.
     * <p>
     * 
     * @param objectSize
     *            The size in bytes of the encodable object
     * @param symbolSize
     *            The size in bytes of a symbol
     * @param numSourceBlocks
     *            The number of blocks into which the encodable object is partitioned
     * @param numSubBlocks
     *            The number of sub-blocks per source block into which the encodable object is partitioned
     * @return a new {@code TransportParams} instance
     * @exception IllegalArgumentException
     *                If some parameter value is invalid
     */
    public static TransportParams makeTransportParameters(
        long objectSize,
        int symbolSize,
        int numSourceBlocks,
        int numSubBlocks)
    {

        if (!ParameterChecks.isValidObjectSize(objectSize)) {
            throw new IllegalArgumentException("invalid object size");
        }
        if (!ParameterChecks.isValidSymbolSize(symbolSize)) {
            throw new IllegalArgumentException("invalid symbol size");
        }
        if (!ParameterChecks.isValidNumSourceBlocks(numSourceBlocks)) {
            throw new IllegalArgumentException("invalid number of source blocks");
        }
        if (!ParameterChecks.isValidNumSubBlocks(numSubBlocks)) {
            throw new IllegalArgumentException("invalid number of sub-blocks");
        }

        return new TransportParams(objectSize, symbolSize, numSourceBlocks, numSubBlocks);
    }


    // minimal sized fields for space efficiency
    private final long objectSize;
    private final short symbolSize;
    private final byte numSourceBlocks;
    private final short numSubBlocks;


    /*
     * Package-private constructor. No checks are done to the arguments, since those are the responsibility of the
     * public factory methods.
     */
    TransportParams(long objectSize, int symbolSize, int numSourceBlocks, int numSubBlocks) {

        this.objectSize = objectSize;
        this.symbolSize = (short)symbolSize;
        this.numSourceBlocks = (byte)numSourceBlocks;
        this.numSubBlocks = (short)numSubBlocks;
    }

    /**
     * Returns the size in bytes of the encodable object.
     * 
     * @return the size in bytes of the encodable object
     */
    public long getObjectSize() {

        return objectSize;
    }

    /**
     * Returns the size in bytes of a symbol.
     * 
     * @return the size in bytes of a symbol
     */
    public int getSymbolSize() {

        return UnsignedTypes.getUnsignedShort(symbolSize);
    }

    /**
     * Returns the number of blocks into which the encodable object is partitioned.
     * 
     * @return the number of blocks into which the encodable object is partitioned
     */
    public int getNumberOfSourceBlocks() {

        return UnsignedTypes.getUnsignedByte(numSourceBlocks);
    }

    /**
     * Returns the number of sub-blocks per source block into which the encodable object is partitioned.
     * 
     * @return the number of sub-blocks per source block into which the encodable object is partitioned
     */
    public int getNumberOfSubBlocks() {

        return UnsignedTypes.getUnsignedShort(numSubBlocks);
    }
}
