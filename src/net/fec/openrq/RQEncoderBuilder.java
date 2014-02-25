package net.fec.openrq;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface RQEncoderBuilder {

	// TODO add symbol alignment parameter later

	/**
	 * Sets the maximum payload size in number of bytes.
	 * <p>
	 * This property affects the maximum size of an encoding symbol, which will be equal to the provided size rounded
	 * down to the closest multiple of {@code Al}, where {@code Al} is the symbol alignment parameter.
	 * 
	 * @param maxPayload
	 *            A number of bytes indicating the maximum payload size
	 * @return this builder
	 * @exception IllegalArgumentException
	 *                If {@code maxPayload} is non-positive
	 */
	public RQEncoderBuilder maxPayload(int maxPayload);

	/**
	 * Sets the maximum block size in number of bytes that is decodable in working memory.
	 * <p>
	 * This property allows the decoder to work with a limited amount of memory in an efficient way.
	 * 
	 * @param maxBlock
	 *            A number of bytes indicating the maximum block size that is decodable in working memory
	 * @return this builder
	 * @exception IllegalArgumentException
	 *                If {@code maxBlock} is non-positive
	 */
	public RQEncoderBuilder maxBlockInMemory(int maxBlock);

	/**
	 * Sets the lower bound on the sub-symbol size in units of {@code Al}, where {@code Al} is the symbol alignment
	 * parameter.
	 * <p>
	 * This property affects the amount of interleaving used by the partitioning of an object into source blocks and
	 * sub-blocks.
	 * 
	 * @param minSubSymbol
	 *            The lower bound on the sub-symbol size in units of {@code Al}
	 * @return this builder
	 * @exception IllegalArgumentException
	 *                If {@code minSubSymbol} is non-positive
	 */
	public RQEncoderBuilder minSubSymbol(int minSubSymbol);

	/**
	 * Returns an instance of an {@code RQEncoder} which has a number of source blocks and a number of sub-blocks
	 * derived from specific parameters.
	 * 
	 * @return an instance of an {@code RQEncoder}
	 */
	public RQEncoder build();
}
