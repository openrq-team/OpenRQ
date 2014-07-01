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
import java.util.Iterator;
import java.util.Objects;

import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.rq.IntermediateSymbolsDecoder;
import net.fec.openrq.util.rq.SystematicIndices;


/**
 */
final class ArraySourceBlockEncoder implements SourceBlockEncoder {

    // requires valid arguments
    static ArraySourceBlockEncoder newEncoder(
        ArrayDataEncoder dataEncoder,
        byte[] array,
        int arrayOff,
        FECParameters fecParams,
        int sbn,
        int K)
    {

        // account for padding in the last source symbol
        final EncodingSymbol[] sourceSymbols = prepareSourceSymbols(array, arrayOff, fecParams, K);
        return new ArraySourceBlockEncoder(dataEncoder, sourceSymbols, sbn, K);
    }

    private static final EncodingSymbol[] prepareSourceSymbols(
        byte[] array,
        int arrayOff,
        FECParameters fecParams,
        int K)
    {

        final int T = fecParams.symbolSize();

        final EncodingSymbol[] symbols = new EncodingSymbol[K];
        for (int esi = 0, symbolOff = arrayOff; esi < K; esi++, symbolOff += T) {
            // account for padding in the last source symbol
            final int symbolLen = Math.min(T, array.length - symbolOff);
            final PaddedByteArrayEncoder symbolData = PaddedByteArrayEncoder.newArray(array, symbolOff, symbolLen, T);

            symbols[esi] = EncodingSymbol.newSourceSymbol(esi, symbolData);
        }

        return symbols;
    }


    private final ArrayDataEncoder dataEncoder;
    private final EncodingSymbol[] sourceSymbols;
    private byte[][] intermediateSymbols = null;

    private final int sbn;
    private final int K;
    private final int Kprime;


    private ArraySourceBlockEncoder(
        ArrayDataEncoder dataEncoder,
        EncodingSymbol[] sourceSymbols,
        int sbn,
        int K)
    {

        this.dataEncoder = Objects.requireNonNull(dataEncoder);
        this.sourceSymbols = Objects.requireNonNull(sourceSymbols);

        this.sbn = sbn;
        this.K = K;
        this.Kprime = SystematicIndices.ceil(K);
    }

    private FECParameters fecParameters() {

        return dataEncoder.fecParameters();
    }

    // use only this method for access to the intermediate symbols
    private byte[][] getIntermediateSymbols() {

        // Note: if multiple threads call this method concurrently, then
        // no harm is done, only the fact that some threads may perform
        // useless work

        byte[][] is = intermediateSymbols;
        if (is == null) {
            is = generateIntermediateSymbols();
            intermediateSymbols = is;
        }

        return is;
    }

    @Override
    public ArrayDataEncoder dataEncoder() {

        return dataEncoder;
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
    public EncodingPacket encodingPacket(int esi) {

        checkGenericEncodingSymbolESI(esi);

        if (esi < K) { // source symbol
            return EncodingPacket.newSourcePacket(sbn, esi, getSourceSymbol(esi).transportData(), 1);
        }
        else { // repair symbol
            return EncodingPacket.newRepairPacket(sbn, esi, getRepairSymbol(esi).transportData(), 1);
        }
    }

    @Override
    public EncodingPacket sourcePacket(int esi) {

        checkSourceSymbolESI(esi);
        return EncodingPacket.newSourcePacket(sbn, esi, sourceSymbols[esi].transportData(), 1);
    }

    @Override
    public EncodingPacket sourcePacket(int esi, int numSymbols) {

        checkSourceSymbolESI(esi);
        checkNumSourceSymbols(esi, numSymbols);

        // must calculate the size beforehand (total size may be less than numSymbols * T)
        int totalSize = 0;
        for (int n = 0, ii = esi; n < numSymbols; n++, ii++) {
            totalSize += sourceSymbols[ii].transportSize();
        }

        final ByteBuffer symbols = ByteBuffer.allocate(totalSize);
        for (int n = 0, ii = esi; n < numSymbols; n++, ii++) {
            symbols.put(sourceSymbols[ii].transportData());
        }
        symbols.flip();

        return EncodingPacket.newSourcePacket(sbn, esi, symbols.asReadOnlyBuffer(), numSymbols);
    }

    @Override
    public EncodingPacket repairPacket(int esi) {

        checkRepairSymbolESI(esi);
        return EncodingPacket.newRepairPacket(sbn, esi, getRepairSymbol(esi).transportData(), 1);
    }

    @Override
    public EncodingPacket repairPacket(int esi, int numSymbols) {

        checkRepairSymbolESI(esi);
        checkNumRepairSymbols(esi, numSymbols);

        // retrieve repair symbols data
        final ByteBuffer symbols = ByteBuffer.allocate(numSymbols * fecParameters().symbolSize());
        for (int i = 0; i < numSymbols; i++) {
            symbols.put(getRepairSymbol(esi + i).transportData());
        }
        symbols.flip();

        return EncodingPacket.newRepairPacket(sbn, esi, symbols.asReadOnlyBuffer(), numSymbols);
    }

    @Override
    public IterableBuilder newIterableBuilder() {

        return new IterBuilder(this);
    }

    @Override
    public Iterable<EncodingPacket> sourcePacketsIterable() {

        return newIterableBuilder()
            .startAtInitialSourceSymbol()
            .endAtFinalSourceSymbol()
            .build();
    }

    @Override
    public Iterable<EncodingPacket> repairPacketsIterable(int numRepairPackets) {

        if (numRepairPackets < 1 || numRepairPackets > (1 + ParameterChecker.maxEncodingSymbolID() - K)) {
            throw new IllegalArgumentException("invalid number of repair packets");
        }

        return newIterableBuilder()
            .startAtInitialRepairSymbol()
            .endAt(numberOfSourceSymbols() + numRepairPackets - 1)
            .build();
    }

    private void checkGenericEncodingSymbolESI(int esi) {

        if (esi < 0 || esi > ParameterChecker.maxEncodingSymbolID()) {
            throw new IllegalArgumentException("invalid encoding symbol ID");
        }
    }

    private void checkSourceSymbolESI(int esi) {

        if (esi < 0 || esi >= K) {
            throw new IllegalArgumentException("invalid source symbol ID");
        }
    }

    // requires valid ESI
    private void checkNumSourceSymbols(int esi, int numSymbols) {

        if (numSymbols < 1 || numSymbols > K - esi) {
            throw new IllegalArgumentException("invalid number of source symbols");
        }
    }

    private void checkRepairSymbolESI(int esi) {

        if (esi < K || esi > ParameterChecker.maxEncodingSymbolID()) {
            throw new IllegalArgumentException("invalid repair symbol ID");
        }
    }

    // requires valid ESI
    private void checkNumRepairSymbols(int esi, int numSymbols) {

        if (numSymbols < 1 || numSymbols > (1 + ParameterChecker.maxEncodingSymbolID() - esi)) {
            throw new IllegalArgumentException("invalid number of repair symbols");
        }
    }

    // requires valid ESI
    private EncodingSymbol getSourceSymbol(int esi) {

        return sourceSymbols[esi];
    }

    // requires valid ESI
    private EncodingSymbol getRepairSymbol(int esi) {

        // calculate ISI from ESI
        final int isi = esi + (Kprime - K);

        // generate the repair symbol data
        final int T = fecParameters().symbolSize();
        byte[] enc_data = LinearSystem.enc(Kprime, getIntermediateSymbols(), new Tuple(Kprime, isi), T);

        // TODO should we store the repair symbols generated?
        return EncodingSymbol.newRepairSymbol(esi, enc_data);
    }

    private byte[][] generateIntermediateSymbols() {

        // source block's parameters
        int Ki = SystematicIndices.getKIndex(Kprime);
        int S = SystematicIndices.S(Ki);
        int H = SystematicIndices.H(Ki);
        int L = Kprime + S + H;
        int T = fecParameters().symbolSize();

        // allocate and initialize vector D
        byte[][] D = new byte[L][T];
        for (int row = S + H, index = 0; row < K + S + H; row++, index++)
            D[row] = sourceSymbols[index].data();

        // first try to obtain an optimized decoder that supports Kprime
        final IntermediateSymbolsDecoder isd = ISDManager.get(Kprime);
        if (isd != null) {
            return isd.decode(D);
        }
        else { // if no optimized decoder is available, fall back to the standard decoding process

            // generate LxL Constraint Matrix
            byte[][] constraint_matrix = LinearSystem.generateConstraintMatrix(Kprime); // A

            // solve system of equations
            try {
                return LinearSystem.PInactivationDecoding(constraint_matrix, D, Kprime);
                // return Utilities.gaussElimination(constraint_matrix, D);
            }
            catch (SingularMatrixException e) {
                throw new RuntimeException(
                    "FATAL ERROR: Singular matrix for the encoding process. This should never happen.");
            }
        }
    }


    private static final class IterBuilder implements IterableBuilder {

        private final SourceBlockEncoder encoder;
        private int startingESI;
        private int endingESI;


        IterBuilder(SourceBlockEncoder encoder) {

            this.encoder = Objects.requireNonNull(encoder);
            this.startingESI = 0;
            this.endingESI = ParameterChecker.maxEncodingSymbolID();
        }

        @Override
        public IterableBuilder startAt(int esi) {

            if (esi < 0 || esi > ParameterChecker.maxEncodingSymbolID()) {
                throw new IllegalArgumentException("invalid encoding symbol identifier");
            }

            setStartingESI(esi);
            return this;
        }

        @Override
        public IterableBuilder startAtInitialSourceSymbol() {

            setStartingESI(0);
            return this;
        }

        @Override
        public IterableBuilder startAtInitialRepairSymbol() {

            setStartingESI(encoder.numberOfSourceSymbols());
            return this;
        }

        @Override
        public IterableBuilder endAt(int esi) {

            if (esi < 0 || esi > ParameterChecker.maxEncodingSymbolID()) {
                throw new IllegalArgumentException("invalid encoding symbol identifier");
            }

            setEndingESI(esi);
            return this;
        }

        @Override
        public IterableBuilder endAtFinalSourceSymbol() {

            setEndingESI(encoder.numberOfSourceSymbols() - 1);
            return this;
        }

        @Override
        public Iterable<EncodingPacket> build() {

            return new Iterable<EncodingPacket>() {

                @Override
                public Iterator<EncodingPacket> iterator() {

                    return new EncodingPacketIterator(encoder, startingESI, endingESI);
                }
            };
        }

        // requires valid ESI
        private void setStartingESI(int esi) {

            startingESI = esi;
            if (endingESI < esi) {
                endingESI = esi;
            }
        }

        // requires valid ESI
        private void setEndingESI(int esi) {

            endingESI = esi;
            if (esi < startingESI) {
                startingESI = esi;
            }
        }
    }

    private static final class EncodingPacketIterator implements Iterator<EncodingPacket> {

        private final SourceBlockEncoder encoder;
        private final int fence;
        private int nextESI;


        EncodingPacketIterator(SourceBlockEncoder encoder, int startingESI, int endingESI) {

            if (endingESI < startingESI) throw new IllegalArgumentException("ending ESI smaller than starting ESI");

            this.encoder = Objects.requireNonNull(encoder);
            this.fence = endingESI + 1;
            this.nextESI = startingESI;
        }

        @Override
        public boolean hasNext() {

            return nextESI < fence;
        }

        @Override
        public EncodingPacket next() {

            try {
                return encoder.encodingPacket(nextESI);
            }
            finally {
                this.nextESI++;
            }
        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();
        }
    }


    // ============================= TEST_CODE ============================= //

    static void forceInterSymbolsGen(ArraySourceBlockEncoder enc) {

        enc.generateIntermediateSymbols();
    }
}
