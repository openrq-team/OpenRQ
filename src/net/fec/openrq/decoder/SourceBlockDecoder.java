package net.fec.openrq.decoder;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface SourceBlockDecoder {

    /**
     * Returns the source block number for the source block currently being decoded.
     * 
     * @return the source block number for the source block currently being decoded
     */
    public int sourceBlockNumber();

    /**
     * Returns the number of source symbols from the source block currently being decoded.
     * 
     * @return the number of source symbols from the source block currently being decoded
     */
    public int numberOfSourceSymbols();
}
