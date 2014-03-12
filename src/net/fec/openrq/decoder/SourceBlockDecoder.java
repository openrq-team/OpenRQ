package net.fec.openrq.decoder;


import java.nio.ByteBuffer;
import java.util.Set;

import net.fec.openrq.parameters.ParameterChecker;


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
     * Returns the total number of expected source symbols from the source block currently being decoded.
     * 
     * @return the total number of expected source symbols from the source block currently being decoded
     */
    public int numberOfSourceSymbols();

    /**
     * Returns {@code true} if, and only if, the source block currently being decoded contains the source symbol with
     * the provided encoding symbol identifier.
     * <p>
     * Note that a valid encoding symbol identifier for a source symbol must be between {@code 0} (inclusive) and
     * {@code K} (exclusive), where {@code K} is the total number of expected source symbols for the source block
     * currently being decoded.
     * 
     * @param encSymbolID
     *            An encoding symbol identifier for a specific source symbol
     * @return {@code true} if, and only if, the source block currently being decoded contains the specified source
     *         symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier does not represent a valid source symbol
     * @see #numberOfSourceSymbols()
     */
    public boolean containsSourceSymbol(int encSymbolID);

    /**
     * Returns {@code true} if, and only if, the source block currently being decoded contains the repair symbol with
     * the provided encoding symbol identifier.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}, and must also be greater than or equal to {@code K}, where
     * {@code K} is the number of source symbols from the source block currently being decoded.
     * 
     * @param encSymbolID
     *            An encoding symbol identifier for a specific repair symbol
     * @return {@code true} if, and only if, the source block currently being decoded contains the specified repair
     *         symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier does not represent a valid repair symbol
     * @see #numberOfSourceSymbols()
     */
    public boolean containsRepairSymbol(int encSymbolID);

    /**
     * Returns {@code true} if, and only if, the source block currently being decoded is fully decoded. A source block
     * is considered fully decoded when it contains all of its source symbols.
     * 
     * @return {@code true} if, and only if, the source block currently being decoded is fully decoded
     * @see #containsSourceSymbol(int)
     */
    public boolean isSourceBlockDecoded();

    /**
     * Returns a set of integers containing the encoding symbol identifiers of the missing source symbols from the
     * source block currently being decoded. The returned set has an iteration ordering of ascending encoding symbol
     * identifiers.
     * 
     * @return a set of encoding symbol identifiers of missing source symbols
     */
    public Set<Integer> missingSourceSymbols();

    /**
     * @param encSymbolID
     * @param sourceSymbol
     * @return
     */
    public SourceBlockState putSourceSymbol(int encSymbolID, ByteBuffer sourceSymbol);

    /**
     * @param encSymbolID
     * @param sourceSymbol
     * @param offset
     * @return
     */
    public SourceBlockState putSourceSymbol(int encSymbolID, byte[] sourceSymbol, int offset);

    /**
     * @param encSymbolID
     * @param repairSymbol
     * @return
     */
    public SourceBlockState putRepairSymbol(int encSymbolID, ByteBuffer repairSymbol);

    /**
     * @param encSymbolID
     * @param repairSymbol
     * @param offset
     * @return
     */
    public SourceBlockState putRepairSymbol(int encSymbolID, byte[] repairSymbol, int offset);
}
