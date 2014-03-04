package net.fec.openrq;


import java.nio.ByteBuffer;

import net.fec.openrq.packet.EncodingPacket;
import net.fec.openrq.packet.SymbolType;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class SourcePacket implements EncodingPacket {

    @Override
    public int getSourceBlockNumber() {

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getEncodingSymbolID() {

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfSymbols() {

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSymbolSize() {

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ByteBuffer getSymbolData() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SymbolType getSymbolType() {

        return SymbolType.SOURCE;
    }
}
