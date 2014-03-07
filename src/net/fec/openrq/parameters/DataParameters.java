package net.fec.openrq.parameters;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class DataParameters {

    // minimal sized fields for space efficiency
    private final long dataLen;
    private final short symbolSize;
    private final byte numSourceBlocks;
    private final short numSubBlocks;


    /**
     * Constructs an instance of {@code DataParameters} with the provided values.
     * <p>
     * The following expressions must all be true so that a correct instance can be constructed:
     * <ul>
     * <li>{@code ParameterChecker.isValidDataLength(dataLen)}</li>
     * <li>{@code ParameterChecker.isValidSymbolSize(symbolSize)}</li>
     * <li>{@code ParameterChecker.isValidNumSourceBlocks(numSourceBlocks)}</li>
     * <li>{@code ParameterChecker.isValidNumSubBlocks(numSubBlocks)}</li>
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
     * @exception IllegalArgumentException
     *                If some parameter value is invalid
     */
    public DataParameters(long dataLen, int symbolSize, int numSourceBlocks, int numSubBlocks) {

        if (!ParameterChecker.isValidDataLength(dataLen)) {
            throw new IllegalArgumentException("invalid data length");
        }
        if (!ParameterChecker.isValidSymbolSize(symbolSize)) {
            throw new IllegalArgumentException("invalid symbol size");
        }
        if (!ParameterChecker.isValidNumSourceBlocks(numSourceBlocks)) {
            throw new IllegalArgumentException("invalid number of source blocks");
        }
        if (!ParameterChecker.isValidNumSubBlocks(numSubBlocks)) {
            throw new IllegalArgumentException("invalid number of sub-blocks");
        }

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

        return ParameterIO.unsignSymbolSize(symbolSize);
    }

    /**
     * Returns the number of blocks into which the encodable data is partitioned.
     * 
     * @return the number of blocks into which the encodable data is partitioned
     */
    public int getNumberOfSourceBlocks() {

        return ParameterIO.unsignNumSourceBlocks(numSourceBlocks);
    }

    /**
     * Returns the number of sub-blocks per source block into which the encodable data is partitioned.
     * 
     * @return the number of sub-blocks per source block into which the encodable data is partitioned
     */
    public int getNumberOfSubBlocks() {

        return ParameterIO.unsignNumSubBlocks(numSubBlocks);
    }
}
