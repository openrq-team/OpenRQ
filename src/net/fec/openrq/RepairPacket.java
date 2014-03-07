package net.fec.openrq;


import java.nio.ByteBuffer;

import net.fec.openrq.parameters.PacketParameters;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class RepairPacket implements EncodingPacket {

    @Override
    public PacketParameters getPacketParameters() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ByteBuffer getSymbolData() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SymbolType getSymbolType() {

        return SymbolType.REPAIR;
    }

}
