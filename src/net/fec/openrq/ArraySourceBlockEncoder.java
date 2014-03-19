package net.fec.openrq;


import net.fec.openrq.encoder.EncodingPacket;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.util.bytevector.ByteVector;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class ArraySourceBlockEncoder implements SourceBlockEncoder {

    // requires valid arguments
    static ArraySourceBlockEncoder newEncoder(
        byte[] array,
        int offset,
        FECParameters fecParams,
        int sbn,
        int K
        )
    {

        final int Kprime = SystematicIndices.ceil(K);
        final int size = Kprime * fecParams.symbolSize();
        return new ArraySourceBlockEncoder(size, array, offset, fecParams, K, sbn);
    }


    private final ByteVector data;

    private final FECParameters fecParams;
    private final int sbn;

    private final int K;


    private ArraySourceBlockEncoder(
        int size,
        byte[] array,
        int offset,
        FECParameters fecParams,
        int K,
        int sbn)
    {

        this.data = PaddedByteVector.newVector(size, array, offset);

        this.fecParams = fecParams;
        this.sbn = sbn;

        this.K = K;
    }

    @Override
    public int sourceBlockNumber() {

        return sbn;
    }

    @Override
    public int numberOfSourceSymbols() {

        return K;
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
}
