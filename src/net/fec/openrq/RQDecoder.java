package net.fec.openrq;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface RQDecoder {

	/**
	 * Returns the common properties associated to this decoder.
	 * 
	 * @return the common properties associated to this decoder
	 */
	public RQCommonProperties getProperties();
}
