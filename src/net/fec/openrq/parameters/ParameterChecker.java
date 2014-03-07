package net.fec.openrq.parameters;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ParameterChecker {

    /**
     * @param dataLen
     * @return
     */
    public static boolean isValidDataLength(long dataLen) {

        return Params.isValidDataLength(dataLen);
    }

    /**
     * @param symbolSize
     * @return
     */
    public static boolean isValidSymbolSize(int symbolSize) {

        return Params.isValidSymbolSize(symbolSize);
    }

    /**
     * @param numSourceBlocks
     * @return
     */
    public static boolean isValidNumSourceBlocks(int numSourceBlocks) {

        return Params.isValidNumSourceBlocks(numSourceBlocks);
    }

    /**
     * @param numSubBlocks
     * @return
     */
    public static boolean isValidNumSubBlocks(int numSubBlocks) {

        return Params.isValidNumSubBlocks(numSubBlocks);
    }

    // TODO add symbol alignment check

    /**
     * @param sourceBlockNum
     * @return
     */
    public static boolean isValidSourceBlockNumber(int sourceBlockNum) {

        return Params.isValidSourceBlockNumber(sourceBlockNum);
    }

    /**
     * @param encSymbolID
     * @return
     */
    public static boolean isValidEncodingSymbolID(int encSymbolID) {

        return Params.isValidEncodingSymbolID(encSymbolID);
    }

    /**
     * @param fecPayloadID
     * @return
     */
    public static boolean isValidFECpayloadID(int fecPayloadID) {

        return Params.isValidFECpayloadID(fecPayloadID);
    }

    /**
     * @param numSymbols
     * @return
     */
    public static boolean isValidNumSymbols(int numSymbols) {

        return Params.isValidNumSymbols(numSymbols);
    }

    private ParameterChecker() {

        // not instantiable
    }
}
