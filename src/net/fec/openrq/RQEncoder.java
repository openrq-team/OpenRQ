package net.fec.openrq;


import java.util.List;

import net.fec.openrq.parameters.TransportParams;
import net.fec.openrq.stream.SourceBlockStream;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface RQEncoder {

    /**
     * Returns {@code true} if, and only if, all source blocks from this encoder are immediately available.
     * 
     * @return {@code true} if, and only if, all source blocks from this encoder are immediately available
     */
    public boolean allSourceBlocksAvailable();

    /**
     * Returns an immutable list containing all source blocks from this encoder, if they are
     * {@linkplain #allSourceBlocksAvailable() available}.
     * <p>
     * Each source block is capable of producing encoding symbols independently from other source blocks. It is possible
     * to encode symbols from different source blocks in parallel.
     * 
     * @return an immutable list containing all source blocks from this encoder
     * @exception UnsupportedOperationException
     *                If not all source blocks are immediately available
     * @see #allSourceBlocksAvailable()
     */
    public List<SourceBlock> getAllSourceBlocks();

    /**
     * Returns a stream of source blocks.
     * <p>
     * Each source block is capable of producing encoding symbols independently from other source blocks. Once the
     * stream is fully iterated, that is, no more source blocks are returned.
     * 
     * @return a stream of source blocks
     */
    public SourceBlockStream getSourceBlocksStream();

    /**
     * Returns the transport parameters associated to this encoder.
     * 
     * @return the transport parameters associated to this encoder
     */
    public TransportParams getTransportParameters();
}
