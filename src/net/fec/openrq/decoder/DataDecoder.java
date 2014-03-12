package net.fec.openrq.decoder;


import net.fec.openrq.FECParameters;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface DataDecoder {

    /**
     * Returns the FEC parameters associated to this decoder. The returned {@code FECParameters} instance is always
     * valid.
     * 
     * @return the FEC parameters associated to this decoder
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
     * Returns {@code true} if, and only if, the original data is fully decoded. The original data is considered fully
     * decoded when each source block is fully decoded.
     * 
     * @return {@code true} if, and only if, the original data is fully decoded
     */
    public boolean isDataDecoded();

    /**
     * Returns a decoder object for a specific source block.
     * 
     * @param sourceBlockNum
     *            A source block number
     * @return a decoder object for a specific source block
     */
    public SourceBlockDecoder decoderForSourceBlock(int sourceBlockNum);
}
