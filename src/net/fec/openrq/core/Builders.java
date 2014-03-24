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

package net.fec.openrq.core;


import net.fec.openrq.core.encoder.DataEncoder;
import net.fec.openrq.core.encoder.DataEncoderBuilder;
import net.fec.openrq.core.parameters.ParameterChecker;
import net.fec.openrq.core.util.arithmetic.ExtraMath;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class Builders {

    static DataEncoderBuilder<ArrayDataEncoder> newEncoderBuilder(byte[] array, int offset, int length) {

        if (offset < 0 || length < 0 || (array.length - offset) < length) {
            throw new IndexOutOfBoundsException();
        }

        return new ArrayEncBuilder(array, offset, length);
    }


    private static final class ArrayEncBuilder extends AbstractEncBuilder<ArrayDataEncoder> {

        private final byte[] array;
        private final int offset;
        private final int length;


        ArrayEncBuilder(byte[] array, int offset, int length) {

            super(length);
            this.array = array;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public ArrayDataEncoder build() {

            final long F = length;
            final int T = symbolSize;
            final int WS = maxDecBlock;
            final FECParameters fecParams = deriveParameters(F, T, WS);

            return ArrayDataEncoder.newEncoder(array, offset, fecParams);
        }
    }

    private static abstract class AbstractEncBuilder<T extends DataEncoder> implements DataEncoderBuilder<T> {

        protected final long dataLength;
        protected int symbolSize;
        protected int maxDecBlock;


        protected AbstractEncBuilder(long dataLength) {

            this.dataLength = dataLength;
            defaultMaxPayload();
            defaultMaxDecoderBlock();
        }

        @Override
        public DataEncoderBuilder<T> maxPayload(int maxPayloadLen) {

            if (maxPayloadLen <= 0) throw new IllegalArgumentException("non-positive maxPayloadLen");

            final int Al = ParameterChecker.symbolAlignmentValue();
            final int T = (maxPayloadLen / Al) * Al; // T = P'

            if (T < ParameterChecker.symbolSizeLowerBound(dataLength)) {
                throw new IllegalArgumentException("derived symbol size is less than its lower bound");
            }
            if (T > ParameterChecker.symbolSizeUpperBound(dataLength)) {
                throw new IllegalArgumentException("derived symbol size is less than its upper bound");
            }

            this.symbolSize = T;
            return this;
        }

        @Override
        public DataEncoderBuilder<T> defaultMaxPayload() {

            int T = DataEncoderBuilder.DEF_MAX_PAYLOAD_LENGTH;
            T = Math.min(T, ParameterChecker.symbolSizeUpperBound(dataLength));
            
            final int Al = ParameterChecker.symbolAlignmentValue();
            T = (T / Al) * Al;
            
            T = Math.max(T, ParameterChecker.symbolSizeLowerBound(dataLength));

            this.symbolSize = T;
            return this;
        }

        @Override
        public DataEncoderBuilder<T> maxDecoderBlock(int maxBlock) {

            if (maxBlock <= 0) throw new IllegalArgumentException("non-positive maxBlock");
            this.maxDecBlock = maxBlock;
            return this;
        }

        @Override
        public DataEncoderBuilder<T> defaultMaxDecoderBlock() {

            this.maxDecBlock = DataEncoderBuilder.DEF_MAX_DEC_BLOCK_SIZE;
            return this;
        }

        @Override
        public abstract T build();
    }


    // requires validated arguments
    private static FECParameters deriveParameters(long F, int T, int WS) {

        final int Al = ParameterChecker.symbolAlignmentValue();

        // interleaving is disabled for now
        final int SStimesAl = T;           // SS * Al = T

        // safe cast because F and T are appropriately bounded
        final int Kt = (int)ExtraMath.ceilDiv(F, T); // Kt = ceil(F/T)
        final int N_max = T / SStimesAl;   // N_max = floor(T/(SS*Al))

        final int Z = deriveZ(Kt, N_max, WS, Al, T);
        final int N = deriveN(Kt, Z, N_max, WS, Al, T);

        return FECParameters.makeFECParameters(F, T, Z, N);
    }

    private static int deriveZ(long Kt, int N_max, int WS, int Al, int T) {

        // Z = ceil(Kt/KL(N_max))
        return (int)ExtraMath.ceilDiv(Kt, SystematicIndices.KL(N_max, WS, Al, T));
    }

    private static int deriveN(long Kt, int Z, int N_max, int WS, int Al, int T) {

        // N is the minimum n=1, ..., N_max such that ceil(Kt/Z) <= KL(n)
        final int KtOverZ = (int)ExtraMath.ceilDiv(Kt, Z);
        int n;
        for (n = 1; n <= N_max && KtOverZ > SystematicIndices.KL(n, WS, Al, T); n++) {/* loop */}

        return n;
    }

    private Builders() {

        // not instantiable
    }
}
