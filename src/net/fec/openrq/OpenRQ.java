package net.fec.openrq;


import java.util.Objects;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class OpenRQ {

	private static final int DEF_ALIGN_PARAM = 4;			// Al
	private static final int DEF_MAX_PAYLOAD_SIZE = 1392;	// P'
	private static final int DEF_MAX_BLOCK_SIZE = 76800;	// WS // B
	private static final int DEF_MIN_SUB_SYMBOL = 8;		// SS


	/**
	 * @param object
	 * @return
	 */
	public static RQEncoderBuilder newEncoderBuilder(byte[] object) {

		return new EncoderBuilder(object);
	}

	/**
	 * @return
	 */
	public static RQEncoder newDefaultEncoder(byte[] object) {

		return new EncoderBuilder(object).build();
	}

	/**
	 * @param object
	 * @param symbolSize
	 * @param numSourceBlocks
	 * @param numSubBlocks
	 * @return
	 */
	public static RQEncoder newExplicitEncoder(byte[] object, int symbolSize, int numSourceBlocks, int numSubBlocks) {

		// TODO return new encoder from the explicit parameters
		return null;
	}

	/**
	 * @return
	 */
	public static RQDecoder newDecoder() {

		// TODO complete method
		return null;
	}


	private static final class EncoderBuilder implements RQEncoderBuilder {

		private final byte[] object;

		private int maxPayload;			// P'
		private int maxBlock;			// WS
		private int minSubSymbol;		// SS


		EncoderBuilder(byte[] object) {

			this.object = Objects.requireNonNull(object);

			this.maxPayload = DEF_MAX_PAYLOAD_SIZE;
			this.maxBlock = DEF_MAX_BLOCK_SIZE;
			this.minSubSymbol = DEF_MIN_SUB_SYMBOL;
		}

		@Override
		public RQEncoderBuilder maxPayload(int maxPayload) {

			if (maxPayload <= 0) throw new IllegalArgumentException("non-positive value");
			this.maxPayload = maxPayload;
			return null;
		}

		@Override
		public RQEncoderBuilder maxBlockInMemory(int maxBlock) {

			if (maxBlock <= 0) throw new IllegalArgumentException("non-positive value");
			this.maxBlock = maxBlock;
			return this;
		}

		@Override
		public RQEncoderBuilder minSubSymbol(int minSubSymbol) {

			if (minSubSymbol <= 0) throw new IllegalArgumentException("non-positive value");
			this.minSubSymbol = minSubSymbol;
			return this;
		}

		@Override
		public RQEncoder build() {

			// TODO derive T, Z and N and return a new encoder
			return null;
		}
	}


	private OpenRQ() {

		// not instantiable
	}
}
