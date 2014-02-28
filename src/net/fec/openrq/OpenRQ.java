package net.fec.openrq;


import net.fec.openrq.parameters.TransportParams;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class OpenRQ {

    /**
     * @param params
     * @return
     */
    public static RQDecoder newEncoder(byte[] data, TransportParams params) {

        // TODO return a encoder from the object and provided properties
        return null;
    }

    /**
     * @param params
     * @return
     */
    public static RQDecoder newDecoder(TransportParams params) {

        // TODO return a decoder from the provided properties
        return null;
    }

    private OpenRQ() {

        // not instantiable
    }
}
