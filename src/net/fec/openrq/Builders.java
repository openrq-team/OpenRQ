package net.fec.openrq;


import net.fec.openrq.encoder.EncoderBuilder;
import net.fec.openrq.encoder.RQEncoder;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class Builders {

    static EncoderBuilder newEncoderBuilder(byte[] data) {

        return new ArrayEncBuilder(data);
    }


    private static final class ArrayEncBuilder extends AbstractEncBuilder {

        private final byte[] data;


        ArrayEncBuilder(byte[] data) {

            this.data = data;
        }

        @Override
        public RQEncoder build() {

            // TODO derive T, Z, N, Al and return encoder instance
            return null;
        }

    }

    private static abstract class AbstractEncBuilder implements EncoderBuilder {

        protected int maxPayload;
        protected int maxDecBlock;
        protected int minSubSymbol;


        protected AbstractEncBuilder() {

            defaultMaxPayload();
            defaultMaxDecoderBlock();
            defaultMinSubSymbol();
        }

        @Override
        public EncoderBuilder maxPayload(int maxPayloadLen) {

            if (maxPayloadLen <= 0) throw new IllegalArgumentException("non-positive maxPayloadLen");
            this.maxPayload = maxPayloadLen;
            return this;
        }

        @Override
        public EncoderBuilder defaultMaxPayload() {

            this.maxPayload = EncoderBuilder.DEF_MAX_PAYLOAD_LENGTH;
            return this;
        }

        @Override
        public EncoderBuilder maxDecoderBlock(int maxBlock) {

            if (maxBlock <= 0) throw new IllegalArgumentException("non-positive maxBlock");
            this.maxDecBlock = maxBlock;
            return this;
        }

        @Override
        public EncoderBuilder defaultMaxDecoderBlock() {

            this.maxDecBlock = EncoderBuilder.DEF_MAX_DEC_BLOCK_SIZE;
            return this;
        }

        @Override
        public EncoderBuilder minSubSymbol(int minSubSymbol) {

            if (minSubSymbol <= 0) throw new IllegalArgumentException("non-positive minSubSymbol");
            this.minSubSymbol = minSubSymbol;
            return this;
        }

        @Override
        public EncoderBuilder defaultMinSubSymbol() {

            this.minSubSymbol = EncoderBuilder.DEF_MIN_SUB_SYMBOL;
            return this;
        }

        @Override
        public abstract RQEncoder build();

    }


    private Builders() {

        // not instantiable
    }
}
