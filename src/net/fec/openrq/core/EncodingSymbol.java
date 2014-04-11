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


import java.nio.ByteBuffer;
import java.util.Arrays;

import net.fec.openrq.core.util.rq.SystematicIndices;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
abstract class EncodingSymbol {

    /**
     * @param esi
     * @param data
     * @return
     */
    static EncodingSymbol newSourceSymbol(int esi, PaddedByteArray data) {

        return new SourceSymbol(esi, data);
    }

    /**
     * @param esi
     * @param data
     * @return
     */
    static EncodingSymbol newRepairSymbol(int esi, byte[] data) {

        return new RepairSymbol(esi, data);
    }


    private final int esi;


    private EncodingSymbol(int esi) {

        this.esi = esi;
    }

    /**
     * May have padding.
     * 
     * @return
     */
    abstract byte[] data();

    /**
     * Never has padding.
     * 
     * @return
     */
    abstract ByteBuffer transportData();

    /**
     * @return
     */
    abstract int transportSize();

    /**
     * @return
     */
    int esi() {

        return esi;
    }

    /**
     * @param K
     * @return
     */
    int getISI(int K) {

        int kLinha = SystematicIndices.ceil(K);

        return esi() + (kLinha - K); // yes, I know its commutative: it's just for a better code reading experience.
    }


    private static final class SourceSymbol extends EncodingSymbol {

        private final PaddedByteArray data;
        private final ByteBuffer transportBuffer;


        SourceSymbol(int esi, PaddedByteArray data) {

            super(esi);
            this.data = data;
            this.transportBuffer = prepareTransportBuffer(data);
        }

        private static ByteBuffer prepareTransportBuffer(PaddedByteArray data) {

            if (data.paddinglessLength() == data.length()) {
                // need to return a slice of the wrapped buffer,
                // otherwise the buffer position will be equal to data.arrayOffset()...
                return ByteBuffer.wrap(data.array(), data.arrayOffset(), data.length()).slice();
            }
            else {
                final byte[] paddedCopy = data.getBytes(new byte[data.length()]);
                return ByteBuffer.wrap(paddedCopy);
            }
        }

        @Override
        byte[] data() {

            return data.getBytes(new byte[data.length()]);
        }

        @Override
        ByteBuffer transportData() {

            return transportBuffer.asReadOnlyBuffer();
        }

        @Override
        int transportSize() {

            return transportBuffer.remaining();
        }
    }

    private static final class RepairSymbol extends EncodingSymbol {

        private final byte[] data;
        private final ByteBuffer transportBuffer;


        RepairSymbol(int esi, byte[] data) {

            super(esi);
            this.data = data;
            this.transportBuffer = prepareTransportBuffer(data);
        }

        private static ByteBuffer prepareTransportBuffer(byte[] data) {

            return ByteBuffer.wrap(data);
        }

        @Override
        byte[] data() {

            return Arrays.copyOf(data, data.length);
        }

        @Override
        ByteBuffer transportData() {

            return transportBuffer.asReadOnlyBuffer();
        }

        @Override
        int transportSize() {

            return transportBuffer.remaining();
        }
    }
}
