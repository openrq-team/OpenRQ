package net.fec.openrq.parameters;


import java.nio.ByteBuffer;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public class ParameterIO {

    /**
     * @param dataLen
     * @param buffer
     */
    public static void writeDataLength(long dataLen, ByteBuffer buffer) {

        Params.writeDataLength(dataLen, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static long readDataLength(ByteBuffer buffer) {

        return Params.readDataLength(buffer);
    }

    /**
     * @param symbolSize
     * @return
     */
    public static int unsignSymbolSize(int symbolSize) {

        return Params.unsignSymbolSize(symbolSize);
    }

    /**
     * @param symbolSize
     * @param buffer
     */
    public static void writeSymbolSize(int symbolSize, ByteBuffer buffer) {

        Params.writeSymbolSize(symbolSize, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readSymbolSize(ByteBuffer buffer) {

        return Params.readSymbolSize(buffer);
    }

    /**
     * @param numSourceBlocks
     * @return
     */
    public static int unsignNumSourceBlocks(int numSourceBlocks) {

        return Params.unsignNumSourceBlocks(numSourceBlocks);
    }

    /**
     * @param numSourceBlocks
     * @param buffer
     */
    public static void writeNumSourceBlocks(int numSourceBlocks, ByteBuffer buffer) {

        Params.writeNumSourceBlocks(numSourceBlocks, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readNumSourceBlocks(ByteBuffer buffer) {

        return Params.readNumSourceBlocks(buffer);
    }

    /**
     * @param numSubBlocks
     * @return
     */
    public static int unsignNumSubBlocks(int numSubBlocks) {

        return Params.unsignNumSubBlocks(numSubBlocks);
    }

    /**
     * @param numSubBlocks
     * @param buffer
     */
    public static void writeNumSubBlocks(int numSubBlocks, ByteBuffer buffer) {

        Params.writeNumSubBlocks(numSubBlocks, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readNumSubBlocks(ByteBuffer buffer) {

        return Params.readNumSubBlocks(buffer);
    }

    /**
     * @param symbolAlignment
     * @return
     */
    public static int unsignSymbolAlignment(int symbolAlignment) {

        return Params.unsignSymbolAlignment(symbolAlignment);
    }

    /**
     * @param symbolAlignment
     * @param buffer
     */
    public static void writeSymbolAlignment(int symbolAlignment, ByteBuffer buffer) {

        Params.writeSymbolAlignment(symbolAlignment, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readSymbolAlignment(ByteBuffer buffer) {

        return Params.readSymbolAlignment(buffer);
    }

    /**
     * @param sourceBlockNum
     * @return
     */
    public static int unsignSourceBlockNumber(int sourceBlockNum) {

        return Params.unsignSourceBlockNumber(sourceBlockNum);
    }

    /**
     * @param fecPayloadID
     * @return
     */
    public static int extractSourceBlockNumber(int fecPayloadID) {

        return Params.extractSourceBlockNumber(fecPayloadID);
    }

    /**
     * @param encSymbolID
     * @return
     */
    public static int unsignEncodingSymbolID(int encSymbolID) {

        return Params.unsignEncodingSymbolID(encSymbolID);
    }

    /**
     * @param fecPayloadID
     * @return
     */
    public static int extractEncodingSymbolID(int fecPayloadID) {

        return Params.extractEncodingSymbolID(fecPayloadID);
    }

    /**
     * @param sourceBlockNum
     * @param encSymbolID
     * @return
     */
    public static int buildFECpayloadID(int sourceBlockNum, int encSymbolID) {

        return Params.buildFECpayloadID(sourceBlockNum, encSymbolID);
    }

    /**
     * @param fecPayloadID
     * @param buffer
     */
    public static void writeFECpayloadID(int fecPayloadID, ByteBuffer buffer) {

        Params.writeFECpayloadID(fecPayloadID, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readFECpayloadID(ByteBuffer buffer) {

        return Params.readFECpayloadID(buffer);
    }

    /**
     * @param numSymbols
     * @return
     */
    public static int unsignNumSymbols(short numSymbols) {

        return Params.unsignNumSymbols(numSymbols);
    }

    /**
     * @param numSymbols
     * @param buffer
     */
    public static void writeNumSymbols(int numSymbols, ByteBuffer buffer) {

        Params.writeNumSymbols(numSymbols, buffer);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readNumSymbols(ByteBuffer buffer) {

        return Params.readNumSymbols(buffer);
    }

    private ParameterIO() {

        // not instantiable
    }
}
