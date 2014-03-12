package net.fec.openrq.encoder;


import net.fec.openrq.FECParameters;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface DataEncoder {

    /**
     * Returns the FEC parameters associated to this encoder. The returned {@code FECParameters} instance is always
     * valid.
     * 
     * @return the FEC parameters associated to this encoder
     */
    public FECParameters fecParameters();

    /**
     * Returns the length of the data in number of bytes. This value is the one returned by
     * {@code this.fecParameters().dataLength()}.
     * 
     * @return the length of the data in number of bytes
     */
    public long dataLength();

    /**
     * Returns the size of a symbol in number of bytes. This value is the one returned by
     * {@code this.fecParameters().symbolSize()}.
     * 
     * @return the size of a symbol in number of bytes
     */
    public int symbolSize();

    /**
     * Returns the number of source blocks. This value is the one returned by
     * {@code this.fecParameters().numberOfSourceBlocks()}.
     * 
     * @return the number of source blocks
     */
    public int numberOfSourceBlocks();

    /**
     * Returns an encoder object for a specific source block.
     * 
     * @param sourceBlockNum
     *            A source block number
     * @return an encoder object for a specific source block
     */
    public SourceBlockEncoder encoderForSourceBlock(int sourceBlockNum);
}
