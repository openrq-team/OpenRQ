/*
 * Copyright 2014 Jose Lopes
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fec.openrq;


import static net.fec.openrq.util.arithmetic.ExtraMath.ceilDiv;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.DataEncoderBuilder;
import net.fec.openrq.parameters.ParameterChecker;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class Builders {

    static DataEncoderBuilder newEncoderBuilder(byte[] data) {

        return new ArrayEncBuilder(data);
    }


    private static final class ArrayEncBuilder extends AbstractEncBuilder {

        private final byte[] data;


        ArrayEncBuilder(byte[] data) {

            super(data.length);
            this.data = data;
        }

        @Override
        public DataEncoder build() {

            final long F = data.length;
            final int T = maxPayload; // T = P'
            final int WS = maxDecBlock;
            final FECParameters fecParams = deriveParameters(F, T, WS);

            // TODO return encoder instance
            return null;
        }
    }

    private static abstract class AbstractEncBuilder implements DataEncoderBuilder {

        protected final long dataLength;
        protected int maxPayload;
        protected int maxDecBlock;


        protected AbstractEncBuilder(long dataLength) {

            this.dataLength = dataLength;
            defaultMaxPayload();
            defaultMaxDecoderBlock();
        }

        @Override
        public DataEncoderBuilder maxPayload(int maxPayloadLen) {

            if (maxPayloadLen <= 0) throw new IllegalArgumentException("non-positive maxPayloadLen");
            this.maxPayload = roundDownMaxPayload(boundMaxPayload(maxPayloadLen, dataLength));
            return this;
        }

        private static int boundMaxPayload(int maxPayload, long dataLength) {

            // safe cast since dataLength is safely upper bounded
            final int lowerBound = (int)(dataLength / (SystematicIndices.K_max * ParameterChecker.maxNumSourceBlocks()));
            final int upperBound = (int)Math.min(dataLength, ParameterChecker.maxSymbolSize());

            if (maxPayload < lowerBound) return lowerBound;
            else if (maxPayload > upperBound) return upperBound;
            else return maxPayload;
        }

        private static int roundDownMaxPayload(int maxPayload) {

            final int Al = ParameterChecker.symbolAlignmentValue();
            return (maxPayload / Al) * Al;
        }

        @Override
        public DataEncoderBuilder defaultMaxPayload() {

            this.maxPayload = DataEncoderBuilder.DEF_MAX_PAYLOAD_LENGTH;
            return this;
        }

        @Override
        public DataEncoderBuilder maxDecoderBlock(int maxBlock) {

            if (maxBlock <= 0) throw new IllegalArgumentException("non-positive maxBlock");
            this.maxDecBlock = maxBlock;
            return this;
        }

        @Override
        public DataEncoderBuilder defaultMaxDecoderBlock() {

            this.maxDecBlock = DataEncoderBuilder.DEF_MAX_DEC_BLOCK_SIZE;
            return this;
        }

        @Override
        public abstract DataEncoder build();
    }


    // requires validated arguments
    private static FECParameters deriveParameters(long F, int T, int WS) {

        final int Al = ParameterChecker.symbolAlignmentValue();

        // interleaving is disabled for now
        final int SStimesAl = T;           // SS * Al = T

        // safe cast because F and T are appropriately bounded
        final int Kt = (int)ceilDiv(F, T); // Kt = ceil(F/T)
        final int N_max = T / SStimesAl;   // N_max = floor(T/(SS*Al))

        final int Z = deriveZ(Kt, N_max, WS, Al, T);
        final int N = deriveN(Kt, Z, N_max, WS, Al, T);

        return FECParameters.makeFECParameters(F, T, Z, N);
    }

    private static int deriveZ(long Kt, int N_max, int WS, int Al, int T) {

        // Z = ceil(Kt/KL(N_max))
        return (int)ceilDiv(Kt, SystematicIndices.KL(N_max, WS, Al, T));
    }

    private static int deriveN(long Kt, int Z, int N_max, int WS, int Al, int T) {

        // N is the minimum n=1, ..., N_max such that ceil(Kt/Z) <= KL(n)
        final int KtOverZ = (int)ceilDiv(Kt, Z);
        int n;
        for (n = 1; n <= N_max && KtOverZ > SystematicIndices.KL(n, WS, Al, T); n++) {/* loop */}

        return n;
    }

    private Builders() {

        // not instantiable
    }
}
