package net.fec.openrq;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Defaults {

	/**
	 * Default value for the alignment parameter.
	 */
	public static final int ALIGN_PARAM = 4;		// Al

	/**
	 * Default value for the maximum payload size.
	 */
	public static final int MAX_PAYLOAD_SIZE = 1392;// P'

	/**
	 * Default value for the maximum block size.
	 */
	public static final int MAX_BLOCK_SIZE = 76800;	// WS // B

	/**
	 * Default value for the minimum sub-symbol size.
	 */
	public static final int MIN_SUB_SYMBOL = 8;		// SS


	private Defaults() {

		// not instantiable
	}
}
