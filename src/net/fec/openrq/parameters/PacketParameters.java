package net.fec.openrq.parameters;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class PacketParameters {

    // minimal sized fields for space efficiency
    private final int fecPayloadID;
    private final short numSymbols;


    /**
     * Constructs an instance of {@code PacketParameters} with the provided values.
     * <p>
     * The following expressions must all be true so that a correct instance can be constructed:
     * <ul>
     * <li>{@code ParameterChecker.isValidSourceBlockNumber(sourceBlockNum)}</li>
     * <li>{@code ParameterChecker.isValidEncodingSymbolID(encSymbolID)}</li>
     * <li>{@code ParameterChecker.isValidNumSymbols(numSymbols)}</li>
     * </ul>
     * otherwise, an {@code IllegalArgumentException} is thrown.
     * <p>
     * 
     * @param sourceBlockNum
     *            The source block number associated to some source block
     * @param encSymbolID
     *            The encoding symbol identifier associated to the first symbol in an encoding packet
     * @param numSymbols
     *            The number of symbols in an encoding packet
     * @exception IllegalArgumentException
     *                If some parameter value is invalid
     */
    public PacketParameters(int sourceBlockNum, int encSymbolID, int numSymbols) {

        if (!ParameterChecker.isValidSourceBlockNumber(sourceBlockNum)) {
            throw new IllegalArgumentException("invalid source block number");
        }
        if (!ParameterChecker.isValidEncodingSymbolID(encSymbolID)) {
            throw new IllegalArgumentException("invalid encoding symbol identifier");
        }
        if (!ParameterChecker.isValidNumSymbols(numSymbols)) {
            throw new IllegalArgumentException("invalid number of symbols");
        }

        this.fecPayloadID = ParameterIO.buildFECpayloadID(sourceBlockNum, encSymbolID);
        this.numSymbols = (short)numSymbols;
    }

    /**
     * Constructs an instance of {@code PacketParameters} with the provided values.
     * <p>
     * The following expressions must all be true so that a correct instance can be constructed:
     * <ul>
     * <li>{@code ParameterChecker.isValidFECpayloadID(fecPayloadID)}</li>
     * <li>{@code ParameterChecker.isValidNumSymbols(numSymbols)}</li>
     * </ul>
     * otherwise, an {@code IllegalArgumentException} is thrown.
     * <p>
     * 
     * @param fecPayloadID
     *            A concatenation of a source block number and an encoding symbol identifier
     * @param numSymbols
     *            The number of symbols in an encoding packet
     * @exception IllegalArgumentException
     *                If some parameter value is invalid
     */
    public PacketParameters(int fecPayloadID, int numSymbols) {

        if (!ParameterChecker.isValidFECpayloadID(fecPayloadID)) {
            throw new IllegalArgumentException("invalid FEC payload ID");
        }
        if (!ParameterChecker.isValidNumSymbols(numSymbols)) {
            throw new IllegalArgumentException("invalid number of symbols");
        }

        this.fecPayloadID = fecPayloadID;
        this.numSymbols = (short)numSymbols;
    }

    /**
     * Returns the source block number associated to some source block.
     * 
     * @return the source block number associated to some source block
     */
    public int getSourceBlockNumber() {

        return ParameterIO.extractSourceBlockNumber(fecPayloadID);
    }

    /**
     * Returns the encoding symbol identifier associated to the first symbol in an encoding packet.
     * 
     * @return the encoding symbol identifier associated to the first symbol in an encoding packet
     */
    public int getEncodingSymbolID() {

        return ParameterIO.extractEncodingSymbolID(fecPayloadID);
    }

    /**
     * Returns the FEC Payload ID associated to the first symbol in an encoding packet. The returned value is a
     * concatenation of the source block number and encoding symbol identifier.
     * 
     * @return a concatenation of the source block number and encoding symbol identifier
     */
    public int getFECpayloadID() {

        return fecPayloadID;
    }

    /**
     * Returns the number of symbols in an encoding packet. This value is always positive.
     * 
     * @return the number of symbols in an encoding packet
     */
    public int getNumberOfSymbols() {

        return ParameterIO.unsignNumSymbols(numSymbols);
    }
}
