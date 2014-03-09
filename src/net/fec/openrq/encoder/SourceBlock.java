package net.fec.openrq.encoder;


import net.fec.openrq.parameters.PacketParameters;


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
     * Returns an encoding packet from the provided {@code PacketParameters}.
     * 
     * @param params Defines parameter values
     * @return
     */
    public EncodingPacket getEncodingPacket(PacketParameters params);
}
