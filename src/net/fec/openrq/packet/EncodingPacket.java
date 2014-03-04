package net.fec.openrq.packet;


import java.nio.ByteBuffer;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface EncodingPacket {

    /**
     * Returns the <i>source block number</i> of this encoding packet.
     * 
     * @return the <i>source block number</i> of this encoding packet
     */
    public int getSourceBlockNumber();

    /**
     * Returns the <i>encoding symbol identifier</i> of the first symbol in this packet.
     * 
     * @return the <i>encoding symbol identifier</i> of the first symbol in this packet
     */
    public int getEncodingSymbolID();

    /**
     * Returns the number of symbols in this packet. This number is always positive.
     * 
     * @return the number of symbols in this packet
     */
    public int getNumberOfSymbols();

    /**
     * Return the size, in number of bytes, of a symbol in this packet.
     * 
     * @return the size, in number of bytes, of a symbol in this packet
     */
    public int getSymbolSize();

    /**
     * Returns the data from the symbol(s) in this packet. The returned symbol(s) have contiguous <i>encoding symbol
     * identifiers</i>.
     * <p>
     * The returned buffer is {@linkplain ByteBuffer#isReadOnly() read-only} and has a
     * {@linkplain ByteBuffer#position() position} of 0 and a {@linkplain ByteBuffer#limit() limit} equal to the size of
     * each symbol times the number of symbols in this packet.
     * 
     * @return a read-only buffer with the data from the symbol(s) in this packet
     */
    public ByteBuffer getSymbolData();

    /**
     * Returns the type of all the symbols in this packet.
     * 
     * @return the type of all the symbols in this packet
     */
    public SymbolType getSymbolType();
}
