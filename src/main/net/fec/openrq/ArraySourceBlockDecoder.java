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
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.collection.BitSetIterators;
import net.fec.openrq.util.rq.SystematicIndices;


/**
 */
final class ArraySourceBlockDecoder implements SourceBlockDecoder {

    // requires valid arguments
    static ArraySourceBlockDecoder newDecoder(
        ArrayDataDecoder dataDecoder,
        byte[] array,
        int arrayOff,
        FECParameters fecParams,
        int sbn,
        int K,
        int extraSymbols)
    {

        final int paddedLen = K * fecParams.symbolSize();
        final int arrayLen = Math.min(paddedLen, array.length - arrayOff);
        final PaddedByteArray data = PaddedByteArray.newArray(array, arrayOff, arrayLen, paddedLen);

        return new ArraySourceBlockDecoder(dataDecoder, data, sbn, K, extraSymbols);
    }


    private final ArrayDataDecoder dataDecoder;
    private final PaddedByteArray data;

    private final int sbn;
    private final int K;
    private final SymbolsState symbolsState;


    private ArraySourceBlockDecoder(
        ArrayDataDecoder dataDecoder,
        PaddedByteArray data,
        int sbn,
        int K,
        int extraSymbols)
    {

        this.dataDecoder = Objects.requireNonNull(dataDecoder);
        this.data = Objects.requireNonNull(data);

        this.sbn = sbn;
        this.K = K;
        this.symbolsState = new SymbolsState(K, extraSymbols);
    }

    private FECParameters fecParameters() {

        return dataDecoder.fecParameters();
    }

    @Override
    public ArrayDataDecoder dataDecoder() {

        return dataDecoder;
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
    public boolean containsSourceSymbol(int esi) {

        checkSourceSymbolESI(esi);
        symbolsState.lock();
        try {
            return symbolsState.containsSourceSymbol(esi);
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public boolean containsRepairSymbol(int esi) {

        checkRepairSymbolESI(esi);
        symbolsState.lock();
        try {
            return symbolsState.containsRepairSymbol(esi);
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public boolean isSourceBlockDecoded() {

        symbolsState.lock();
        try {
            return symbolsState.isSourceBlockDecoded();
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public SourceBlockState latestState() {

        symbolsState.lock();
        try {
            return symbolsState.sourceBlockState();
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public Set<Integer> missingSourceSymbols() {

        symbolsState.lock();
        try {
            return getMissingSourceSymbols();
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public Set<Integer> availableRepairSymbols() {

        symbolsState.lock();
        try {
            return getAvailableRepairSymbols();
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public SourceBlockState putEncodingPacket(EncodingPacket packet) {

        // other than a different SBN, this method assumes a correct encoding packet
        if (packet.sourceBlockNumber() != sourceBlockNumber()) {
            throw new IllegalArgumentException("source block number does not match the expected");
        }

        symbolsState.lock();
        try {
            if (!symbolsState.isSourceBlockDecoded()) { // do nothing if already decoded
                final ByteBuffer symbols = packet.symbols();
                final int esi = packet.encodingSymbolID();
                boolean putNewSymbol = false;

                // put symbol data
                switch (packet.symbolType()) {
                    case SOURCE:
                        for (int i = 0; i < packet.numberOfSymbols(); i++) {
                            putNewSymbol |= putSourceData(esi + i, symbols);
                        }
                    break;

                    case REPAIR:
                        for (int i = 0; i < packet.numberOfSymbols(); i++) {
                            putNewSymbol |= putRepairData(esi + i, symbols);
                        }
                    break;

                    default:
                        throw new AssertionError("unknown enum value");
                }

                // 1. don't bother if no new symbols were added
                // 2. the addition of a source block may have decoded the source block
                // 3. enough (source/repair) symbols may have been received for a decode to start
                if (putNewSymbol &&
                    !symbolsState.isSourceBlockDecoded() &&
                    symbolsState.haveEnoughSymbolsToDecode())
                {
                    decode();
                }
            }

            return symbolsState.sourceBlockState();
        }
        finally {
            symbolsState.unlock();
        }
    }

    private void checkSourceSymbolESI(int esi) {

        if (esi < 0 || esi >= K) {
            throw new IllegalArgumentException("invalid encoding symbol ID");
        }
    }

    private void checkRepairSymbolESI(int esi) {

        if (esi < K || esi > ParameterChecker.maxEncodingSymbolID()) {
            throw new IllegalArgumentException("invalid encoding symbol ID");
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    private Set<Integer> getMissingSourceSymbols() {

        if (symbolsState.isSourceBlockDecoded()) {
            return Collections.emptySet();
        }
        else {
            final int numMissing = symbolsState.numMissingSourceSymbols();

            // linked hash set preserves insertion ordering (while not being sorted)
            final Set<Integer> missingSourceSymbols = new LinkedHashSet<>(numMissing);
            for (Integer esi : symbolsState.missingSourceSymbols()) {
                missingSourceSymbols.add(esi);
            }

            return missingSourceSymbols;
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    private Set<Integer> getAvailableRepairSymbols() {

        if (symbolsState.isSourceBlockDecoded()) {
            return Collections.emptySet();
        }
        else {
            // linked hash set preserves insertion ordering (while not being sorted)
            return new LinkedHashSet<>(symbolsState.repairSymbolsESIs());
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    private void decode() {

        // generate intermediate symbols -- watch out for decoding failure
        final byte[][] intermediate_symbols = generateIntermediateSymbols();

        if (intermediate_symbols == null) {
            symbolsState.setSourceBlockDecodingFailure();
        }
        else {
            /*
             * with the intermediate symbols calculated, one can recover
             * every missing source symbol
             */

            final int Kprime = SystematicIndices.ceil(K);

            // recover missing source symbols
            for (int esi : missingSourceSymbols()) {
                byte[] sourceSymbol = LinearSystem.enc(
                    Kprime, intermediate_symbols, new Tuple(Kprime, esi), fecParameters().symbolSize());

                // write to data buffer
                putSourceData(esi, sourceSymbol, 0);
            }
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    private final byte[][] generateIntermediateSymbols() {

        // constraint matrix parameters
        final int Kprime = SystematicIndices.ceil(K);
        int Ki = SystematicIndices.getKIndex(Kprime);
        int S = SystematicIndices.S(Ki);
        int H = SystematicIndices.H(Ki);
        int L = Kprime + S + H;
        int T = fecParameters().symbolSize();

        // number of extra repair symbols to be used for the decoding process
        int overhead = symbolsState.numRepairSymbols() - symbolsState.numMissingSourceSymbols();

        // number of rows in the decoding matrix
        int M = L + overhead;

        // allocate memory for the decoding matrix
        byte[][] constraint_matrix = new byte[M][];

        // generate the original constraint matrix
        byte[][] lConstraint = LinearSystem.generateConstraintMatrix(Kprime);

        // copy to our decoding matrix
        for (int row = 0; row < L; row++)
            constraint_matrix[row] = lConstraint[row];

        // initialize D
        byte[][] D = new byte[M][T];

        // populate D with the received source symbols
        for (int isi : symbolsState.receivedSourceSymbols()) {
            data.getBytes(isi * T, D[S + H + isi]);
        }

        /*
         * for every repair symbol received
         * - replace a missing source symbol's decoding matrix line for its corresponding line
         * - populate D accordingly
         */

        Iterator<EncodingSymbol> repair_symbol = symbolsState.repairSymbols().iterator();

        // identify missing source symbols and replace their lines with "repair lines"
        for (Integer missing_ESI : missingSourceSymbols()) {

            EncodingSymbol repair = repair_symbol.next();
            int row = S + H + missing_ESI;

            // replace line S + H + missing_ESI with the line for encIndexes
            Set<Integer> indexes = LinearSystem.encIndexes(Kprime, new Tuple(Kprime, repair.getISI(K)));

            byte[] newLine = new byte[L];

            for (Integer col : indexes)
                newLine[col] = 1;

            constraint_matrix[row] = newLine;

            // fill in missing source symbols in D with the repair symbols
            D[row] = repair.data();
        }

        // insert the values for overhead (repair) symbols
        for (int row = L; row < M; row++)
        {
            EncodingSymbol repair = repair_symbol.next();

            // generate the overhead lines
            Tuple tuple = new Tuple(Kprime, repair.getISI(K));

            Set<Integer> indexes = LinearSystem.encIndexes(Kprime, tuple);

            byte[] newLine = new byte[L];

            for (Integer col : indexes)
                newLine[col] = 1;

            constraint_matrix[row] = newLine;

            // update D with the data for that symbol
            D[row] = repair.data();
        }

        /*
         * with the decoding matrix created and vector D populated,
         * we have the system of linear equations ready to be solved
         */

        try {
            return LinearSystem.PInactivationDecoding(constraint_matrix, D, Kprime);
            // return Utilities.gaussElimination(constraint_matrix, D);
        }
        catch (SingularMatrixException e) {

            return null;
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    // requires valid ESI
    private boolean putSourceData(int esi, ByteBuffer symbolData) {

        final int T = fecParameters().symbolSize(); // TODO handle last symbol size (no padding)
        final int bufPos = symbolData.position();

        if (symbolsState.containsSourceSymbol(esi)) { // if already received, just advance the buffer position
            symbolData.position(bufPos + T);
            return false;
        }
        else {
            if (symbolData.hasArray()) { // avoid array allocation and copy
                final byte[] arr = symbolData.array();
                final int off = bufPos + symbolData.arrayOffset();
                data.putBytes(esi * T, arr, off, T);
                symbolData.position(bufPos + T); // don't forget to advance the buffer position
            }
            else { // must allocate and copy, no other way
                final byte[] arr = new byte[T];
                symbolData.get(arr); // this also advances the buffer position
                data.putBytes(esi * T, arr);
            }

            symbolsState.addSourceSymbol(esi);
            return true;
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    // requires valid ESI
    private void putSourceData(int esi, byte[] symbolData, int off) {

        if (!symbolsState.containsSourceSymbol(esi)) { // if already received, do nothing
            final int T = fecParameters().symbolSize(); // TODO handle last symbol size (no padding)
            data.putBytes(esi * T, symbolData, off, T);
            symbolsState.addSourceSymbol(esi);
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    // requires valid ESI
    private boolean putRepairData(int esi, ByteBuffer symbolData) {

        final int T = fecParameters().symbolSize();
        final int bufPos = symbolData.position();

        if (symbolsState.containsRepairSymbol(esi)) { // if already received, just advance the buffer position
            symbolData.position(bufPos + T);
            return false;
        }
        else {
            // generate repair symbol (cannot avoid copy)
            final byte[] repairData = new byte[T];
            symbolData.get(repairData); // this also advances buffer position
            final EncodingSymbol repair = EncodingSymbol.newRepairSymbol(esi, repairData);

            // add this repair symbol to the set of received repair symbols
            symbolsState.addRepairSymbol(repair);
            return true;
        }
    }


    private static final class SymbolsState {

        private SourceBlockState sbState;

        private final BitSet sourceSymbolsBitSet;
        private final Iterable<Integer> missingSourceSymbols;
        private final Iterable<Integer> receivedSourceSymbols;

        private final Map<Integer, EncodingSymbol> repairSymbols;

        private final int K;
        private final int symbolOverhead;

        private final Lock symbolsStateLock;


        SymbolsState(int K, int symbolOverhead) {

            this.sbState = SourceBlockState.INCOMPLETE;

            this.sourceSymbolsBitSet = new BitSet(K);

            this.repairSymbols = new LinkedHashMap<>(); // preserved receiving ordering
            this.missingSourceSymbols = new MissingSourceSymbolsIterable(sourceSymbolsBitSet, K);
            this.receivedSourceSymbols = new ReceivedSourceSymbolsIterable(sourceSymbolsBitSet);

            this.K = K;
            this.symbolOverhead = symbolOverhead;

            this.symbolsStateLock = new ReentrantLock(false); // non-fair lock
        }

        // Always call this method before accessing the symbols state!
        void lock() {

            symbolsStateLock.lock();
        }

        // Always call this method after using the symbols state!
        void unlock() {

            symbolsStateLock.unlock();
        }

        SourceBlockState sourceBlockState() {

            return sbState;
        }

        void setSourceBlockDecodingFailure() {

            sbState = SourceBlockState.DECODING_FAILURE;
        }

        boolean isSourceBlockDecoded() {

            return sbState == SourceBlockState.DECODED;
        }

        int numMissingSourceSymbols() {

            return K - sourceSymbolsBitSet.cardinality();
        }

        // requires valid parameter
        boolean containsSourceSymbol(int esi) {

            return sourceSymbolsBitSet.get(esi);
        }

        // requires valid parameter
        void addSourceSymbol(int esi) {

            sourceSymbolsBitSet.set(esi); // mark the symbol as received

            if (numMissingSourceSymbols() == 0) {
                sbState = SourceBlockState.DECODED;
                repairSymbols.clear(); // free memory
            }
        }

        Iterable<Integer> missingSourceSymbols() {

            return missingSourceSymbols;
        }

        Iterable<Integer> receivedSourceSymbols() {

            return receivedSourceSymbols;
        }

        int numRepairSymbols() {

            return repairSymbols.size();
        }

        // requires valid parameter
        boolean containsRepairSymbol(int esi) {

            return !isSourceBlockDecoded() && repairSymbols.containsKey(esi);
        }

        // requires valid parameter
        void addRepairSymbol(EncodingSymbol repair) {

            repairSymbols.put(repair.esi(), repair);
        }

        Iterable<EncodingSymbol> repairSymbols() {

            return repairSymbols.values();
        }

        Set<Integer> repairSymbolsESIs() {

            return repairSymbols.keySet();
        }

        boolean haveEnoughSymbolsToDecode() {

            return (sourceSymbolsBitSet.cardinality() + repairSymbols.size()) >= (K + symbolOverhead);
        }


        private static final class MissingSourceSymbolsIterable implements Iterable<Integer> {

            private final BitSet bitSet;
            private final int K;


            MissingSourceSymbolsIterable(BitSet bitSet, int K) {

                this.bitSet = Objects.requireNonNull(bitSet);
                this.K = K;
            }

            @Override
            public Iterator<Integer> iterator() {

                return BitSetIterators.newFalseIterator(bitSet, K);
            }
        }

        private static final class ReceivedSourceSymbolsIterable implements Iterable<Integer> {

            private final BitSet bitSet;


            ReceivedSourceSymbolsIterable(BitSet bitSet) {

                this.bitSet = bitSet;
            }

            @Override
            public Iterator<Integer> iterator() {

                return BitSetIterators.newTrueIterator(bitSet);
            }
        }
    }


    // ============================= TEST_CODE ============================= //

    static SourceBlockState forceDecode(ArraySourceBlockDecoder decoder) {

        decoder.symbolsState.lock();
        try {
            decoder.decode();
            return decoder.symbolsState.sourceBlockState();
        }
        finally {
            decoder.symbolsState.unlock();
        }
    }
}
