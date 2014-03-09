package net.fec.openrq.encoder;


import java.util.List;

import net.fec.openrq.parameters.DataParameters;
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
     * Each source block is capable of producing encoding symbols independently from other source blocks. It is
     * possible, for example, to encode symbols from different source blocks in parallel.
     * <p>
     * A specific source block with a given {@linkplain SourceBlock#getSourceBlockNumber() source block number}
     * {@code SBN} can be retrieved from this list by passing {@code SBN} as an argument to the {@link List#get(int)}
     * method.
     * 
     * @return an immutable list containing all source blocks from this encoder
     * @exception UnsupportedOperationException
     *                If not all source blocks are immediately available
     * @see #allSourceBlocksAvailable()
     */
    public List<SourceBlock> getAllSourceBlocks();

    /*
     * Returns a stream of source blocks.
     * <p>
     * Each source block is capable of producing encoding symbols independently from other source blocks. Once the
     * stream is fully iterated, no more source blocks are returned by the stream. The source blocks are returned by the
     * stream in an ascending ordering by their {@linkplain SourceBlock#getSourceBlockNumber source block numbers}.
     * 
     * @return a stream of source blocks
     */
    /**
     * This method is unsupported for now. An {@code UnsupportedOperationException} is thrown if this method is invoked.
     * 
     * @return nothing, since an exception is always thrown
     * @exception UnsupportedOperationException
     *                Always
     */
    public SourceBlockStream getSourceBlocksStream();

    /**
     * Returns the data parameters associated to this encoder.
     * 
     * @return the data parameters associated to this encoder
     */
    public DataParameters getDataParameters();
}
