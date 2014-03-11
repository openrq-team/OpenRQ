package net.fec.openrq.encoder;


import net.fec.openrq.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface RQEncoder {

    /**
     * Sets the source block with the given source block number as the source block currently being encoded.
     * <p>
     * Note that {@code sourceBlockNum} must be valid according to
     * {@link ParameterChecker#isValidSourceBlockNumber(int)}, and must also be between {@code 0} (inclusive) and
     * {@code Z} (exclusive), where {@code Z} is the value returned by
     * {@code this.fecParameters().getNumberOfSourceBlocks()}.
     * 
     * @param sourceBlockNum
     *            A source block number
     * @exception IllegalArgumentException
     *                If the provided source block number is invalid
     */
    public void setCurrentSourceBlock(int sourceBlockNum);

    /**
     * Returns the FEC parameters associated to this encoder. The returned {@code FECParameters} instance is always
     * valid.
     * 
     * @return the FEC parameters associated to this encoder
     */
    public FECParameters fecParameters();

    /**
     * Returns the source block number for the source block currently being encoded.
     * 
     * @return the source block number for the source block currently being encoded
     */
    public int sourceBlockNumber();

    /**
     * Returns the number of source symbols from the source block currently being encoded.
     * 
     * @return the number of source symbols from the source block currently being encoded
     */
    public int numberOfSourceSymbols();

    /**
     * Returns an encoding packet with a source symbol from the source block currently being encoded.
     * <p>
     * More specifically, if we assume that
     * <ul>
     * <li>{@code SBN} is the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} is the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a source symbol identified by {@code (SBN, ESI)}.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}, and must also be between {@code 0} (inclusive) and
     * {@code K} (exclusive), where {@code K} is the number of source symbols from the source block currently being
     * encoded.
     * 
     * @param encSymbolID
     *            The encoding symbol identifier of the source symbol in the returned packet
     * @return an encoding packet with a source symbol from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getSourcePacket(int encSymbolID);

    /**
     * Returns an encoding packet with multiple source symbols from the source block currently being encoded.
     * <p>
     * More specifically, if we assume that
     * <ul>
     * <li>{@code SBN} is the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} is the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a first source symbol identified by {@code (SBN, ESI)}, a second
     * symbol identified by {@code (SBN, ESI+1)}, a third symbol identified by {@code (SBN, ESI+2)}, etc.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}, and must also be between {@code 0} (inclusive) and
     * {@code K} (exclusive), where {@code K} is the number of source symbols from the source block currently being
     * encoded. Additionally, the number of symbols must be positive and no greater than ({@code K - ESI}).
     * 
     * @param encSymbolID
     *            The encoding symbol identifier of the first source symbol in the returned packet
     * @param numSymbols
     *            The number of source symbols to be placed in the returned packet
     * @return an encoding packet with multiple source symbols from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getSourcePacket(int encSymbolID, int numSymbols);

    /**
     * Returns an encoding packet with a repair symbol from the source block currently being encoded.
     * <p>
     * More specifically, if we assume that
     * <ul>
     * <li>{@code SBN} is the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} is the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a repair symbol identified by {@code (SBN, ESI)}.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}, and must also be greater than or equal to {@code K}, where
     * {@code K} is the number of source symbols from the source block currently being encoded.
     * 
     * @param encSymbolID
     *            The encoding symbol identifier of the repair symbol in the returned packet
     * @return an encoding packet with a repair symbol from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     */
    public EncodingPacket getRepairPacket(int encSymbolID);

    /**
     * Returns an encoding packet with multiple repair symbols from the source block currently being encoded.
     * <p>
     * More specifically, if we assume that
     * <ul>
     * <li>{@code SBN} is the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} is the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a first repair symbol identified by {@code (SBN, ESI)}, a second
     * symbol identified by {@code (SBN, ESI+1)}, a third symbol identified by {@code (SBN, ESI+2)}, etc.
     * <p>
     * Note that the encoding symbol identifier must be valid according to
     * {@link ParameterChecker#isValidEncodingSymbolID(int)}, and must also be greater than or equal to {@code K}, where
     * {@code K} is the number of source symbols from the source block currently being encoded. Additionally, the number
     * of symbols must be positive and no greater than ({@code MAX_ESI - ESI}), where {@code MAX_ESI} is the
     * {@linkplain ParameterChecker#maxEncodingSymbolID() maximum value for the encoding symbol identifier}.
     * 
     * @param encSymbolID
     *            The encoding symbol identifier of the first repair symbol in the returned packet
     * @param numSymbols
     *            The number of repair symbols to be placed in the returned packet
     * @return an encoding packet with multiple repair symbols from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getRepairPacket(int encSymbolID, int numSymbols);
}
