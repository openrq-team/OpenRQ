package net.fec.openrq;


import net.fec.openrq.util.arithmetic.Ints;
import net.fec.openrq.util.arithmetic.Longs;


/**
 * Supplies the common properties for an encoder/decoder.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class RQCommonProperties {

	private final long objectSize;
	private final int symbolSize;
	private final int numSourceBlocks;
	private final int numSubBlocks;
	private final int symbolAlignment;


	public RQCommonProperties(long objectSize,
			int symbolSize,
			int numSourceBlocks,
			int numSubBlocks,
			int symbolAlignment) {

		this.objectSize = Longs.assertNonNegative(objectSize);
		this.symbolSize = Ints.assertNonNegative(symbolSize);
		this.numSourceBlocks = Ints.assertNonNegative(numSourceBlocks);
		this.numSubBlocks = Ints.assertNonNegative(numSubBlocks);
		this.symbolAlignment = Ints.assertNonNegative(symbolAlignment);
	}

	/**
	 * Returns the object size in number of bytes.
	 * 
	 * @return the object size
	 */
	public long getObjectSize() {

		return objectSize;
	}

	/**
	 * Returns the symbol size in number of bytes.
	 * 
	 * @return the symbol size
	 */
	public int getSymbolSize() {

		return symbolSize;
	}

	/**
	 * Returns the number of source blocks.
	 * 
	 * @return the number of source blocks
	 */
	public int getNumberOfSourceBlocks() {

		return numSourceBlocks;
	}

	/**
	 * Returns the number of sub-blocks.
	 * 
	 * @return the number of sub-blocks
	 */
	public int getNumberOfSubBlocks() {

		return numSubBlocks;
	}

	/**
	 * Returns the symbol alignment.
	 * 
	 * @return the symbol alignment
	 */
	public int getSymbolAlignment() {

		return symbolAlignment;
	}


	/**
	 * Builder class for the common properties.
	 * <p>
	 * Common properties can be built from the following assigned parameters:
	 * <ul>
	 * <li>maximum payload size</li>
	 * <li>maximum block size</li>
	 * <li>minimum sub-symbol size</li>
	 * </ul>
	 * <p>
	 * If some parameter is not assigned, a default value is automatically assigned to it. Default values for the
	 * parameters are defined in class {@link Defaults}.
	 * 
	 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
	 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
	 */
	public static final class Builder {

		private final long objectSize;

		private int maxPayload;			// P'
		private int maxBlock;			// WS
		private int minSubSymbol;		// SS


		// TODO add alignment parameter

		public Builder(long objectSize) {

			if (objectSize <= 0) throw new IllegalArgumentException("non-positive value");
			this.objectSize = objectSize;

			this.maxPayload = Defaults.MAX_PAYLOAD_SIZE;
			this.maxBlock = Defaults.MAX_BLOCK_SIZE;
			this.minSubSymbol = Defaults.MIN_SUB_SYMBOL;
		}

		/**
		 * Assigns to the provided value the maximum payload size in number of bytes.
		 * <p>
		 * This parameter affects the maximum size of an encoding symbol, which will be equal to the provided size
		 * rounded down to the closest multiple of {@code Al}, where {@code Al} is the symbol alignment parameter.
		 * 
		 * @param maxPayload
		 *            A number of bytes indicating the maximum payload size
		 * @return this builder
		 * @exception IllegalArgumentException
		 *                If {@code maxPayload} is non-positive
		 */
		public Builder maxPayload(int maxPayload) {

			if (maxPayload <= 0) throw new IllegalArgumentException("non-positive value");
			this.maxPayload = maxPayload;
			return this;
		}

		/**
		 * Assigns to the provided value the maximum block size in number of bytes that is decodable in working memory.
		 * <p>
		 * This parameter allows the decoder to work with a limited amount of memory in an efficient way.
		 * 
		 * @param maxBlock
		 *            A number of bytes indicating the maximum block size that is decodable in working memory
		 * @return this builder
		 * @exception IllegalArgumentException
		 *                If {@code maxBlock} is non-positive
		 */
		public Builder maxBlockInMemory(int maxBlock) {

			if (maxBlock <= 0) throw new IllegalArgumentException("non-positive value");
			this.maxBlock = maxBlock;
			return this;
		}

		/**
		 * Assigns to the provided value the lower bound on the sub-symbol size in units of {@code Al}, where {@code Al}
		 * is
		 * the symbol alignment parameter.
		 * <p>
		 * This parameter affects the amount of interleaving used by the partitioning of an object into source blocks
		 * and sub-blocks.
		 * 
		 * @param minSubSymbol
		 *            The lower bound on the sub-symbol size in units of {@code Al}
		 * @return this builder
		 * @exception IllegalArgumentException
		 *                If {@code minSubSymbol} is non-positive
		 */
		public Builder minSubSymbol(int minSubSymbol) {

			if (minSubSymbol <= 0) throw new IllegalArgumentException("non-positive value");
			this.minSubSymbol = minSubSymbol;
			return this;
		}

		/**
		 * Returns the common properties derived from the currently assigned parameters.
		 * 
		 * @return the common properties derived from the currently assigned parameters
		 */
		public RQCommonProperties build() {

			// TODO derive T, Z, N and return the properties
			return null;
		}
	}
}
