package net.fec.openrq.decoder;


import net.fec.openrq.parameters.DataParameters;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface RQDecoder {

    /**
     * Returns the data parameters associated to this decoder.
     * 
     * @return the data parameters associated to this decoder
     */
    public DataParameters getDataParameters();

    /**
     * Returns {@code true} if, and only if, the original data is fully decoded.
     * 
     * @return {@code true} if, and only if, the original data is fully decoded
     */
    public boolean isDecoded();
}
