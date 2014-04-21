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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
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
    private final int Kprime;
    private final int symbolOverhead;

    /*
     * TODO
     * For now we don't keep an explicit decoded state to simplify things
     * but later we may have to do it in order to avoid re-decoding if we
     * receive a repeated source/repair symbol.
     */

    private Set<EncodingSymbol> repairSymbols; // TODO free memory when decoded
    private BitSet receivedSourceSymbols;


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
        this.Kprime = SystematicIndices.ceil(K);
        this.symbolOverhead = extraSymbols;

        this.repairSymbols = new LinkedHashSet<EncodingSymbol>(); // stores the receiving ordering
        this.receivedSourceSymbols = new BitSet(K);
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
        return receivedSourceSymbols.get(esi);
    }

    @Override
    public boolean containsRepairSymbol(int esi) {

        checkRepairSymbolESI(esi);
        for (EncodingSymbol repairSymbol : repairSymbols)
            if (repairSymbol.esi() == esi) return true;

        return false;
    }

    @Override
    public boolean isSourceBlockDecoded() {

        return (receivedSourceSymbols.cardinality() == K);
    }

    @Override
    public Set<Integer> missingSourceSymbols() {

        // linked hash set preserves insertion ordering (while not being sorted)
        Set<Integer> missingSourceSymbols = new LinkedHashSet<>();

        for (int i = receivedSourceSymbols.nextClearBit(0); i < K; i = receivedSourceSymbols.nextClearBit(i + 1))
            missingSourceSymbols.add(i);

        return missingSourceSymbols;
    }

    @Override
    public Set<Integer> availableRepairSymbols() {

        if (isSourceBlockDecoded()) {
            return Collections.emptySet();
        }
        else {
            final Set<Integer> repairESIs = new LinkedHashSet<>();
            for (EncodingSymbol repair : repairSymbols) {
                repairESIs.add(repair.esi());
            }
            return repairESIs;
        }
    }

    @Override
    public SourceBlockState putEncodingPacket(EncodingPacket packet) {

        // other than a different SBN, this method assumes a correct encoding packet
        if (packet.sourceBlockNumber() != sourceBlockNumber()) {
            throw new IllegalArgumentException("source block number does not match the expected");
        }

        /*
         * TODO deal with repeated symbols and avoid doing replacing
         * already existing data (first symbol data stays)
         */

        // put source symbol data
        final ByteBuffer symbols = packet.symbols();
        int esi = packet.encodingSymbolID();
        if (packet.symbolType() == SymbolType.SOURCE) {
            for (int i = 0; i < packet.numberOfSymbols(); i++) {
                putSourceData(esi + i, symbols);
            }
        }
        else { // packet.symbolType() == SymbolType.REPAIR
            for (int i = 0; i < packet.numberOfSymbols(); i++) {
                putRepairData(esi + i, symbols);
            }
        }

        // do we have all source symbols?
        if (isSourceBlockDecoded()) {
            return SourceBlockState.DECODED;
        }
        // check if we have enough symbols to decode
        else if (receivedSourceSymbols.cardinality() + repairSymbols.size() >= K + symbolOverhead) {
            return decode();
        }
        else {
            return SourceBlockState.INCOMPLETE;
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

    private SourceBlockState decode() {

        // generate intermediate symbols -- watch out for decoding failure
        byte[][] intermediate_symbols = generateIntermediateSymbols();
        if (intermediate_symbols == null) {
            return SourceBlockState.DECODING_FAILURE;
        }

        /*
         * with the intermediate symbols calculated, one can recover
         * every missing source symbol
         */

        // recover missing source symbols
        for (int esi : missingSourceSymbols()) {
            byte[] sourceSymbol = LinearSystem.enc(
                Kprime, intermediate_symbols, new Tuple(Kprime, esi), fecParameters().symbolSize());

            // write to data buffer
            putSourceData(esi, sourceSymbol, 0);
        }

        return SourceBlockState.DECODED;
    }

    private final byte[][] generateIntermediateSymbols() {

        // constraint matrix parameters
        int Ki = SystematicIndices.getKIndex(Kprime);
        int S = SystematicIndices.S(Ki);
        int H = SystematicIndices.H(Ki);
        int L = Kprime + S + H;
        int T = fecParameters().symbolSize();

        // number of extra repair symbols to be used for the decoding process
        int overhead = repairSymbols.size() - (K - receivedSourceSymbols.cardinality());

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
        for (int i = receivedSourceSymbols.nextSetBit(0); i >= 0; i = receivedSourceSymbols.nextSetBit(i + 1))
            data.getBytes(i * T, D[S + H + i]);

        /*
         * for every repair symbol received
         * - replace a missing source symbol's decoding matrix line for its corresponding line
         * - populate D accordingly
         */

        Iterator<EncodingSymbol> repair_symbol = repairSymbols.iterator();

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

    // requires valid ESI
    private void putSourceData(int esi, ByteBuffer symbolData) {

        final int T = fecParameters().symbolSize(); // TODO handle last symbol size (no padding)
        final int bufPos = symbolData.position();

        if (receivedSourceSymbols.get(esi)) { // if already received, just advance the buffer position
            symbolData.position(bufPos + T);
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
            receivedSourceSymbols.set(esi); // mark the symbol as received
        }
    }

    // requires valid ESI
    private void putSourceData(int esi, byte[] symbolData, int off) {

        if (!receivedSourceSymbols.get(esi)) { // if already received, do nothing
            final int T = fecParameters().symbolSize(); // TODO handle last symbol size (no padding)
            data.putBytes(esi * T, symbolData, off, T);
            receivedSourceSymbols.set(esi); // mark the symbol as received
        }
    }

    // requires valid ESI
    private void putRepairData(int esi, ByteBuffer symbolData) {

        // generate repair symbol (cannot avoid copy)
        final byte[] repairData = new byte[fecParameters().symbolSize()];
        symbolData.get(repairData); // this also advances buffer position
        final EncodingSymbol repair = EncodingSymbol.newRepairSymbol(esi, repairData);

        // add this repair symbol to the set of received repair symbols
        repairSymbols.add(repair);
    }
}
