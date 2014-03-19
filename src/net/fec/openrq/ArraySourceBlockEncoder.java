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


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.fec.openrq.encoder.EncodingPacket;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.util.bytevector.ByteArrayFacade;
import net.fec.openrq.util.bytevector.Facades;
import net.fec.openrq.util.collection.ImmutableList;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class ArraySourceBlockEncoder implements SourceBlockEncoder {

    // requires valid arguments
    static ArraySourceBlockEncoder newEncoder(
        byte[] array,
        int off,
        FECParameters fecParams,
        int sbn,
        int K)
    {

        final int Kprime = SystematicIndices.ceil(K);
        final int sourceLen = Math.min(K * fecParams.symbolSize(), array.length - off);
        final int extendedLen = Kprime * fecParams.symbolSize();

        return new ArraySourceBlockEncoder(array, off, sourceLen, extendedLen, fecParams, sbn, K);
    }


    private final PaddedByteVector data;
    private final EncodingSymbol[] sourceSymbols;

    private final FECParameters fecParams;
    private final int sbn;
    private final int K;


    private ArraySourceBlockEncoder(
        byte[] array,
        int off,
        int sourceLen,
        int extendedLen,
        FECParameters fecParams,
        int sbn,
        int K)
    {

        this.data = PaddedByteVector.newVector(Facades.wrapByteArray(array), off, sourceLen, extendedLen);
        this.sourceSymbols = prepareSourceSymbols(data, fecParams, K, sbn);

        this.fecParams = fecParams;
        this.sbn = sbn;
        this.K = K;
    }

    private static final EncodingSymbol[] prepareSourceSymbols(
        ByteArrayFacade data,
        FECParameters fecParams,
        int K,
        int sbn)
    {

        final int T = fecParams.symbolSize();

        final EncodingSymbol[] symbols = new EncodingSymbol[K];
        for (int esi = 0, symbolOff = 0; esi < K; esi++, symbolOff += T) {

            final PaddedByteVector symbolData = PaddedByteVector.newVector(data, symbolOff, T, T);
            final FECPayloadID fecPayloadID = FECPayloadID.makeFECPayloadID(sbn, esi, fecParams);

            symbols[esi] = new EncodingSymbol(symbolData, fecPayloadID);
        }

        return symbols;
    }

    @Override
    public int sourceBlockNumber() {

        return sbn;
    }

    @Override
    public int numberOfSourceSymbols() {

        return K;
    }

    @Override
    public EncodingPacket getSourcePacket(int encSymbolID) {

        checkESI(encSymbolID);

        final EncodingSymbol sourceSymbol = sourceSymbols[encSymbolID];
        final FECPayloadID fecPayloadID = sourceSymbol.getFECPayloadID();
        final ByteBuffer buf = retrieveBuffer(sourceSymbol);

        return new SourcePacket(fecPayloadID, ImmutableList.newList(buf));
    }

    @Override
    public EncodingPacket getSourcePacket(int encSymbolID, int numSymbols) {

        checkESI(encSymbolID);
        checkNumSymbols(encSymbolID, numSymbols);

        final EncodingSymbol firstSymbol = sourceSymbols[encSymbolID];
        final FECPayloadID fecPayloadID = firstSymbol.getFECPayloadID();

        final List<ByteBuffer> bufs = new ArrayList<>();
        bufs.add(retrieveBuffer(firstSymbol));
        for (int esi = encSymbolID + 1; esi < K; esi++) {
            bufs.add(retrieveBuffer(sourceSymbols[esi]));
        }

        return new SourcePacket(fecPayloadID, ImmutableList.copy(bufs));
    }

    @Override
    public EncodingPacket getRepairPacket(int encSymbolID) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EncodingPacket getRepairPacket(int encSymbolID, int numSymbols) {

        // TODO Auto-generated method stub
        return null;
    }

    private static final ByteBuffer retrieveBuffer(EncodingSymbol symbol) {

        final PaddedByteVector symbolData = symbol.getData();

        return ByteBuffer.wrap(
            symbolData.array(),
            symbolData.arrayOffset(),
            symbolData.paddinglessLength()
            ).asReadOnlyBuffer();
    }

    private final void checkESI(int esi) {

        if (esi < 0 || esi >= K) throw new IllegalArgumentException("invalid encoding symbol identifier");
    }

    // requires valid ESI
    private final void checkNumSymbols(int esi, int numSymbols) {

        if (numSymbols < 1 || numSymbols > K - esi) throw new IllegalArgumentException("invalid number of symbols");
    }


    private static abstract class AbstractEncodingPacket implements EncodingPacket {

        private final FECPayloadID fecPayloadID;
        private final ImmutableList<ByteBuffer> symbols;


        AbstractEncodingPacket(FECPayloadID fecPayloadID, ImmutableList<ByteBuffer> symbols) {

            this.fecPayloadID = fecPayloadID;
            this.symbols = symbols;
        }

        @Override
        public FECPayloadID fecPayloadID() {

            return fecPayloadID;
        }

        @Override
        public List<ByteBuffer> getSymbolData() {

            return symbols;
        }
    }

    private static final class SourcePacket extends AbstractEncodingPacket {

        SourcePacket(FECPayloadID fecPayloadID, ImmutableList<ByteBuffer> symbols) {

            super(fecPayloadID, symbols);
        }

        @Override
        public SymbolType symbolType() {

            return SymbolType.SOURCE;
        }
    }

    private static final class RepairPacket extends AbstractEncodingPacket {

        RepairPacket(FECPayloadID fecPayloadID, ImmutableList<ByteBuffer> symbols) {

            super(fecPayloadID, symbols);
        }

        @Override
        public SymbolType symbolType() {

            return SymbolType.REPAIR;
        }
    }
}
