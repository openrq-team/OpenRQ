package net.fec.openrq;


import net.fec.openrq.encoder.EncodingPacket;
import net.fec.openrq.encoder.SourceBlockEncoder;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ArraySourceBlockEncoder implements SourceBlockEncoder {

    // requires valid arguments
    static ArraySourceBlockEncoder newSourceBlockEncoder(
        byte[] array,
        int offset,
        FECParameters fecParams,
        int Kprime,
        int sbn)
    {

        final int length = Kprime * fecParams.symbolSize();
        return new ArraySourceBlockEncoder(array, offset, length, fecParams, Kprime, sbn);
    }


    private final byte[] array;
    private final int offset;
    private final int length;

    private final FECParameters fecParams;
    private final int Kprime;
    private final int sbn;


    private ArraySourceBlockEncoder(byte[] array, int offset, int length, FECParameters fecParams, int Kprime, int sbn) {

        this.array = array;
        this.offset = offset;
        this.length = length;

        this.fecParams = fecParams;
        this.Kprime = Kprime;
        this.sbn = sbn;
    }

    @Override
    public int sourceBlockNumber() {

        return sbn;
    }

    @Override
    public int numberOfSourceSymbols() {

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public EncodingPacket getSourcePacket(int encSymbolID) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EncodingPacket getSourcePacket(int encSymbolID, int numSymbols) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EncodingPacket getRepairPacket(int encSymbolID) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EncodingPacket getRepairPacket(int encSymbolID, int numSymbols) {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns an array of bytes containing the data from the source block being encoded.
     * 
     * @return an array of bytes containing the data from the source block being encoded
     */
    public byte[] sourceBlockArray() {

        return array;
    }

    /**
     * Returns the index in the source block array of the first encodable byte.
     * 
     * @return the index in the source block of the first encodable byte
     */
    public int sourceBlockOffset() {

        return offset;
    }

    /**
     * Returns the length in number of bytes of the source block being encoded.
     * 
     * @return the length in number of bytes of the source block being encoded
     */
    public int sourceBlockLength() {

        return length;
    }
}
