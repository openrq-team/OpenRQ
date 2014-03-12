package net.fec.openrq.encoder;


import java.nio.ByteBuffer;

import net.fec.openrq.FECPayloadID;
import net.fec.openrq.SymbolType;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface EncodingPacket {

    /**
     * Returns the FEC payload identifier associated to this encoding packet.
     * 
     * @return the FEC payload identifier associated to this encoding packet
     */
    public FECPayloadID fecPayloadID();

    /**
     * Returns the data from the symbol(s) in this encoding packet. The returned symbol(s) have contiguous <i>encoding
     * symbol identifiers</i>.
     * <p>
     * The returned buffer is {@linkplain ByteBuffer#isReadOnly() read-only} and has a
     * {@linkplain ByteBuffer#position() position} of 0 and a {@linkplain ByteBuffer#limit() limit} less than or equal
     * to the size of each symbol times the number of symbols in this packet.
     * 
     * @return a read-only buffer with the data from the symbol(s) in this packet
     */
    public ByteBuffer getSymbolData();

    /**
     * Returns the number of symbols in this encoding packet. This value is always positive.
     * 
     * @return the number of symbols in this encoding packet
     */
    public int numberOfSymbols();

    /**
     * Returns the type of all the symbols in this encoding packet.
     * 
     * @return the type of all the symbols in this encoding packet
     */
    public SymbolType symbolType();
}
