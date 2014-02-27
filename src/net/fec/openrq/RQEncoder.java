package net.fec.openrq;


import net.fec.openrq.parameters.TransportParams;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface RQEncoder {

    /**
     * Returns a stream of source blocks.
     * <p>
     * Each source block is capable of producing encoding symbols independently from other source blocks. Once the
     * stream is fully iterated, that is, no more source blocks are returned.
     * 
     * @return a stream of source blocks
     */
    public SourceBlockStream getSourceBlocks();

    /**
     * Returns the transport parameters associated to this encoder.
     * 
     * @return the transport parameters associated to this encoder
     */
    public TransportParams getTransportParameters();
}
