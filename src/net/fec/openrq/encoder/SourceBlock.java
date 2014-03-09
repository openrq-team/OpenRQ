package net.fec.openrq.encoder;


import net.fec.openrq.parameters.ParameterChecker;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface SourceBlock {

    /**
     * Returns the number identifier of this source block.
     * 
     * @return the number identifier of this source block
     */
    public int getSourceBlockNumber();

    /**
     * Returns the number of source symbols that are available in this source block.
     * 
     * @return the number of source symbols that are available in this source block
     */
    public int getNumberOfSourceSymbols();

    /**
     * Returns an encoding packet with a source symbol given the provided encoding symbol identifier.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}, and must also be between {@code 0} (inclusive) and
     * {@code K} (exclusive), where {@code K} is the {@linkplain #getNumberOfSourceSymbols() number of source symbols
     * that are available in this source block}.
     * 
     * @param encSymbolID
     *            The number identifier of the source symbol in the returned packet
     * @return an encoding packet with a source symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     */
    public EncodingPacket getSourcePacket(int encSymbolID);

    /**
     * Returns an encoding packet with source symbols given the provided encoding symbol identifier and number of
     * symbols.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}, and must also be between {@code 0} (inclusive) and
     * {@code K} (exclusive), where {@code K} is the {@linkplain #getNumberOfSourceSymbols() number of source symbols
     * that are available in this source block}. Additionally, the number of symbols must be positive and no greater
     * than ({@code K - encSymbolID}).
     * 
     * @param encSymbolID
     *            The number identifier of the first symbol in the returned packet
     * @param numSymbols
     *            The number of source symbols to be placed in the returned packet
     * @return an encoding packet with source symbols
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     */
    public EncodingPacket getSourcePacket(int encSymbolID, int numSymbols);

    /**
     * Returns an encoding packet with a repair symbol given the provided encoding symbol identifier.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}.
     * 
     * @param encSymbolID
     *            The number identifier of the repair symbol in the returned packet
     * @return an encoding packet with a repair symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     */
    public EncodingPacket getRepairPacket(int encSymbolID);

    /**
     * Returns an encoding packet with repair symbols given the provided encoding symbol identifier and number of
     * symbols.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}. Additionally, the number of symbols must be positive and
     * no greater than ({@code MAX_ESI - encSymbolID}), where {@code MAX_ESI} is the
     * {@linkplain ParameterChecker#maxEncodingSymbolID() maximum value for the encoding symbol identifier}.
     * 
     * @param encSymbolID
     *            The number identifier of the first symbol in the returned packet
     * @param numSymbols
     *            The number of repair symbols to be placed in the returned packet
     * @return an encoding packet with repair symbols
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     */
    public EncodingPacket getRepairPacket(int encSymbolID, int numSymbols);
}
