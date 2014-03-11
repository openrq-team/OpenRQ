package net.fec.openrq.decoder;


import net.fec.openrq.FECParameters;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface DataDecoder {

    /**
     * Returns the FEC parameters associated to this decoder. The returned {@code FECParameters} instance is always
     * valid.
     * 
     * @return the FEC parameters associated to this decoder
     */
    public FECParameters fecParameters();

    /**
     * Returns {@code true} if, and only if, the original data is fully decoded.
     * 
     * @return {@code true} if, and only if, the original data is fully decoded
     */
    public boolean isDecoded();
    
    
}
