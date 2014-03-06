package net.fec.openrq;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class PacketParameters {

    /**
     * Returns an instance of {@code PacketParameters} with the provided values.
     * <p>
     * The following expressions must all be true so that a correct instance can be returned:
     * <ul>
     * <li>{@code ValueChecker.isValidSourceBlockNumber(sourceBlockNum)}</li>
     * <li>{@code ValueChecker.isValidEncodingSymbolID(encSymbolID)}</li>
     * <li>{@code ValueChecker.isValidNumSymbols(numSymbols)}</li>
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
     * @return a new {@code PacketParameters} instance
     * @exception IllegalArgumentException
     *                If some parameter value is invalid
     */
    public static PacketParameters makePacketParameters(
        int sourceBlockNum,
        int encSymbolID,
        int numSymbols)
    {

        if (!ValueChecker.isValidSourceBlockNumber(sourceBlockNum)) {
            throw new IllegalArgumentException("invalid source block number");
        }
        if (!ValueChecker.isValidEncodingSymbolID(encSymbolID)) {
            throw new IllegalArgumentException("invalid encoding symbol identifier");
        }
        if (!ValueChecker.isValidNumSymbols(numSymbols)) {
            throw new IllegalArgumentException("invalid number of symbols");
        }

        return new PacketParameters(sourceBlockNum, encSymbolID, numSymbols);
    }


    // minimal sized fields for space efficiency
    private final int sbn_esi;
    private final short numSymbols;


    /*
     * Package-private constructor. No checks are done to the arguments, since those are the responsibility of the
     * public factory methods.
     */
    PacketParameters(int sourceBlockNum, int encSymbolID, int numSymbols) {

        this.sbn_esi = buildSBN_ESI((byte)sourceBlockNum, encSymbolID);
        this.numSymbols = (short)numSymbols;
    }

    private static int buildSBN_ESI(byte sourceBlockNum, int encSymbolID) {

        final int sbn = ValueChecker.maskSourceBlockNumber(sourceBlockNum);
        final int esi = ValueChecker.maskEncodingSymbolID(encSymbolID);
        return (sbn << (ValueChecker.numBytesOfEncodingSymbolID() * Byte.SIZE)) | esi;
    }

    private static int extractSBN(int sbn_esi) {

        final byte sbn = (byte)(sbn_esi >>> (ValueChecker.numBytesOfEncodingSymbolID() * Byte.SIZE));
        return ValueChecker.maskSourceBlockNumber(sbn);
    }

    private static int extractESI(int sbn_esi) {

        return ValueChecker.maskEncodingSymbolID(sbn_esi);
    }

    /**
     * Returns the source block number associated to some source block.
     * 
     * @return the source block number associated to some source block
     */
    public int getSourceBlockNumber() {

        return extractSBN(sbn_esi);
    }

    /**
     * Returns the encoding symbol identifier associated to the first symbol in an encoding packet.
     * 
     * @return the encoding symbol identifier associated to the first symbol in an encoding packet
     */
    public int getEncodingSymbolID() {

        return extractESI(sbn_esi);
    }

    /**
     * Returns the number of symbols in an encoding packet. This value is always positive.
     * 
     * @return the number of symbols in an encoding packet
     */
    public int getNumberOfSymbols() {

        return ValueChecker.maskNumSymbols(numSymbols);
    }
}
