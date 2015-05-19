/*
 * Copyright 2014 OpenRQ Team
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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.collection.BitSetIterators;
import net.fec.openrq.util.collection.ImmutableList;
import net.fec.openrq.util.io.ByteBuffers.BufferType;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.rq.SystematicIndices;


/**
 */
final class ArraySourceBlockDecoder implements SourceBlockDecoder {

    // requires valid arguments
    static ArraySourceBlockDecoder newDecoder(
        ArrayDataDecoder dataDecoder,
        final byte[] array,
        int arrayOff,
        FECParameters fecParams,
        int sbn,
        int symbOver)
    {

        ImmutableList<SourceSymbol> sourceSymbols = DataUtils.partitionSourceBlock(
            sbn,
            fecParams,
            arrayOff,
            SourceSymbol.class, new DataUtils.SourceSymbolSupplier<SourceSymbol>() {

                @Override
                public SourceSymbol get(int off, @SuppressWarnings("unused") int esi, int T) {

                    return ArraySourceSymbol.newSymbol(array, off, T);
                }
            });

        return new ArraySourceBlockDecoder(dataDecoder, sbn, sourceSymbols, symbOver);
    }


    private final ArrayDataDecoder dataDecoder;

    private final int sbn;

    private final SymbolsState symbolsState;


    private ArraySourceBlockDecoder(
        ArrayDataDecoder dataDecoder,
        int sbn,
        ImmutableList<SourceSymbol> sourceSymbols,
        int symbOver)
    {

        this.dataDecoder = Objects.requireNonNull(dataDecoder);

        this.sbn = sbn;

        this.symbolsState = new SymbolsState(sourceSymbols, symbOver);
    }

    private FECParameters fecParameters() {

        return dataDecoder.fecParameters();
    }

    private int K() {

        return symbolsState.K();
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

        return K();
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
    public SBDInfo information() {

        symbolsState.lock();
        try {
            return SBDInfo.newInformation(
                sbn,
                symbolsState.sourceBlockState(),
                getMissingSourceSymbols(),
                getAvailableRepairSymbols());
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public SourceBlockState putEncodingPacket(EncodingPacket packet) {

        // other than a different SBN, this method assumes a correct encoding packet
        if (packet.sourceBlockNumber() != sourceBlockNumber()) {
            throw new IllegalArgumentException("the provided packet is not compatible with this source block");
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
                            putNewSymbol |= putSourceData(esi + i, symbols, SourceSymbolDataType.TRANSPORT);
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
                // 2. the addition of a source symbol may have decoded the source block
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

    @Override
    public int symbolOverhead() {

        symbolsState.lock();
        try {
            return symbolsState.symbolOverhead();
        }
        finally {
            symbolsState.unlock();
        }
    }

    @Override
    public void setSymbolOverhead(int symbOver) {

        if (symbOver < 0) throw new IllegalArgumentException("symbol overhead must be non-negative");

        symbolsState.lock();
        try {
            symbolsState.setSymbolOverhead(symbOver);
        }
        finally {
            symbolsState.unlock();
        }
    }

    private void checkSourceSymbolESI(int esi) {

        if (esi < 0 || esi >= K()) {
            throw new IllegalArgumentException("invalid encoding symbol ID");
        }
    }

    private void checkRepairSymbolESI(int esi) {

        if (esi < K() || esi > ParameterChecker.maxEncodingSymbolID()) {
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

            final int Kprime = SystematicIndices.ceil(K());

            // recover missing source symbols
            for (int esi : missingSourceSymbols()) {
                byte[] sourceSymbol = LinearSystem.enc(
                    Kprime, intermediate_symbols, new Tuple(Kprime, esi), fecParameters().symbolSize());

                // write to data buffer
                putSourceData(esi, ByteBuffer.wrap(sourceSymbol), SourceSymbolDataType.CODE);
            }
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    private final byte[][] generateIntermediateSymbols() {

        // constraint matrix parameters
        final int Kprime = SystematicIndices.ceil(K());
        int Ki = SystematicIndices.getKIndex(Kprime);
        int S = SystematicIndices.S(Ki);
        int H = SystematicIndices.H(Ki);
        int L = Kprime + S + H;
        int T = fecParameters().symbolSize();

        // number of extra repair symbols to be used for the decoding process
        int overhead = symbolsState.numRepairSymbols() - symbolsState.numMissingSourceSymbols();

        // number of rows in the decoding matrix
        int M = L + overhead;

        // generate the original constraint matrix and allocate memory for overhead rows
        ByteMatrix A = LinearSystem.generateConstraintMatrix(Kprime, overhead);

        // initialize D
        byte[][] D = new byte[M][T];

        // populate D with the received source symbols
        for (int esi : symbolsState.receivedSourceSymbols()) {
            symbolsState.getSourceSymbol(esi).getCodeData(ByteBuffer.wrap(D[S + H + esi]));
        }

        /*
         * for every repair symbol received
         * - replace a missing source symbol's decoding matrix line for its corresponding line
         * - populate D accordingly
         */

        Iterator<Entry<Integer, RepairSymbol>> repairSymbolsIter = symbolsState.repairSymbols().iterator();

        // identify missing source symbols and replace their lines with "repair lines"
        for (Integer missingSrcESI : missingSourceSymbols()) {

            Entry<Integer, RepairSymbol> next = repairSymbolsIter.next();
            final int repairESI = next.getKey();
            final int repairISI = SystematicIndices.getISI(repairESI, K(), Kprime);
            final RepairSymbol repairSymbol = next.getValue();

            final int row = S + H + missingSrcESI;

            // replace line S + H + missingSrcESI with the line for encIndexes
            Set<Integer> indexes = LinearSystem.encIndexes(Kprime, new Tuple(Kprime, repairISI));

            A.clearRow(row); // must clear previous data first!
            for (Integer col : indexes) {
                A.set(row, col, (byte)1);
            }

            // fill in missing source symbols in D with the repair symbols
            D[row] = repairSymbol.copyOfData(BufferType.ARRAY_BACKED).array();
        }

        // insert the values for overhead (repair) symbols
        for (int row = L; row < M; row++) {

            Entry<Integer, RepairSymbol> next = repairSymbolsIter.next();
            final int repairESI = next.getKey();
            final int repairISI = SystematicIndices.getISI(repairESI, K(), Kprime);
            final RepairSymbol repairSymbol = next.getValue();

            // generate the overhead lines
            Set<Integer> indexes = LinearSystem.encIndexes(Kprime, new Tuple(Kprime, repairISI));

            A.clearRow(row); // must clear previous data first!
            for (Integer col : indexes) {
                A.set(row, col, (byte)1);
            }

            // update D with the data for that symbol
            D[row] = repairSymbol.copyOfData(BufferType.ARRAY_BACKED).array();
        }

        /*
         * with the decoding matrix created and vector D populated,
         * we have the system of linear equations ready to be solved
         */

        try {
            return LinearSystem.PInactivationDecoding(A, D, Kprime);
            // return MatrixUtilities.gaussElimination(constraint_matrix, D);
        }
        catch (SingularMatrixException e) {

            return null; // decoding failure
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    // requires valid ESI
    private boolean putSourceData(int esi, ByteBuffer symbolData, SourceSymbolDataType dataType) {

        if (symbolsState.containsSourceSymbol(esi)) { // if already received, just advance the buffer position
            final int T = fecParameters().symbolSize();
            symbolData.position(symbolData.position() + T);
            return false;
        }
        else {
            symbolsState.addSourceSymbol(esi, symbolData, dataType);
            return true;
        }
    }

    /*
     * ===== Requires locked symbolsState! =====
     */
    // requires valid ESI
    private boolean putRepairData(int esi, ByteBuffer symbolData) {

        if (symbolsState.containsRepairSymbol(esi)) { // if already received, just advance the buffer position
            final int T = fecParameters().symbolSize();
            symbolData.position(symbolData.position() + T);
            return false;
        }
        else {
            // add this repair symbol to the set of received repair symbols
            symbolsState.addRepairSymbol(esi, symbolData);
            return true;
        }
    }


    private static enum SourceSymbolDataType {

        CODE,
        TRANSPORT
    }

    private static final class SymbolsState {

        private SourceBlockState sbState;

        private final ImmutableList<SourceSymbol> sourceSymbols;
        private final Map<Integer, RepairSymbol> repairSymbols;

        private final BitSet sourceSymbolsBitSet;
        private final Iterable<Integer> missingSourceSymbols;
        private final Iterable<Integer> receivedSourceSymbols;

        private int symbolOverhead;

        private final Lock symbolsStateLock;


        SymbolsState(ImmutableList<SourceSymbol> sourceSymbols, int symbOver) {

            this.sbState = SourceBlockState.INCOMPLETE;

            this.sourceSymbols = Objects.requireNonNull(sourceSymbols);
            this.repairSymbols = new LinkedHashMap<>(); // preserved receiving ordering

            final int K = sourceSymbols.size();

            this.sourceSymbolsBitSet = new BitSet(K);
            this.missingSourceSymbols = new MissingSourceSymbolsIterable(sourceSymbolsBitSet, K);
            this.receivedSourceSymbols = new ReceivedSourceSymbolsIterable(sourceSymbolsBitSet);

            setSymbolOverhead(symbOver);

            this.symbolsStateLock = new ReentrantLock(false); // non-fair lock
        }

        int K() {

            return sourceSymbols.size();
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

            return K() - sourceSymbolsBitSet.cardinality();
        }

        // requires valid parameter
        boolean containsSourceSymbol(int esi) {

            return sourceSymbolsBitSet.get(esi);
        }

        // requires valid parameter
        void addSourceSymbol(int esi, ByteBuffer symbolData, SourceSymbolDataType dataType) {

            putSourceSymbolData(esi, symbolData, dataType);
            sourceSymbolsBitSet.set(esi); // mark the symbol as received
            sbState = SourceBlockState.INCOMPLETE;

            if (numMissingSourceSymbols() == 0) {
                sbState = SourceBlockState.DECODED;
                repairSymbols.clear(); // free memory
            }
        }

        private void putSourceSymbolData(int esi, ByteBuffer symbolData, SourceSymbolDataType dataType) {

            switch (dataType) {
                case CODE:
                    sourceSymbols.get(esi).putCodeData(symbolData);
                break;

                case TRANSPORT:
                    sourceSymbols.get(esi).putTransportData(symbolData);
                break;

                default:
                    throw new AssertionError("unknown enum type");
            }
        }

        // requires valid parameter
        SourceSymbol getSourceSymbol(int esi) {

            return sourceSymbols.get(esi);
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

        /*
         * requires valid parameter
         * requires !isSourceBlockDecoded()
         */
        void addRepairSymbol(int esi, ByteBuffer symbolData) {

            repairSymbols.put(esi, RepairSymbol.copyData(symbolData));
            sbState = SourceBlockState.INCOMPLETE;
        }

        Iterable<Entry<Integer, RepairSymbol>> repairSymbols() {

            return repairSymbols.entrySet();
        }

        Set<Integer> repairSymbolsESIs() {

            return repairSymbols.keySet();
        }

        boolean haveEnoughSymbolsToDecode() {

            return (sourceSymbolsBitSet.cardinality() + repairSymbols.size()) >= (K() + symbolOverhead);
        }

        int symbolOverhead() {

            return symbolOverhead;
        }

        // requires non-negative parameter
        void setSymbolOverhead(int symbOver) {

            // the symbol overhead cannot exceed the number of repair symbols
            this.symbolOverhead = Math.min(symbOver, ParameterChecker.numRepairSymbolsPerBlock(K()));
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

