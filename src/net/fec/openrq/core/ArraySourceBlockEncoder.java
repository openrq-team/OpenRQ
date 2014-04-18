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

import net.fec.openrq.core.encoder.SourceBlockEncoder;
import net.fec.openrq.core.parameters.FECParameters;
import net.fec.openrq.core.parameters.ParameterChecker;
import net.fec.openrq.core.util.rq.SingularMatrixException;
import net.fec.openrq.core.util.rq.SystematicIndices;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ArraySourceBlockEncoder implements SourceBlockEncoder {

    // requires valid arguments
    static ArraySourceBlockEncoder newEncoder(
        byte[] array,
        int arrayOff,
        FECParameters fecParams,
        int sbn,
        int K)
    {

        // account for padding in the last source symbol
        final EncodingSymbol[] sourceSymbols = prepareSourceSymbols(array, arrayOff, fecParams, K);
        return new ArraySourceBlockEncoder(sourceSymbols, fecParams, sbn, K);
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
            final PaddedByteArray symbolData = PaddedByteArray.newArray(array, symbolOff, symbolLen, T);

            symbols[esi] = EncodingSymbol.newSourceSymbol(esi, symbolData);
        }

        return symbols;
    }


    private final EncodingSymbol[] sourceSymbols;
    private byte[][] intermediateSymbols = null;

    private final FECParameters fecParams;
    private final int sbn;
    private final int K;
    private final int Kprime;


    private ArraySourceBlockEncoder(
        EncodingSymbol[] sourceSymbols,
        FECParameters fecParams,
        int sbn,
        int K)
    {

        this.sourceSymbols = sourceSymbols;

        this.fecParams = fecParams;
        this.sbn = sbn;
        this.K = K;
        this.Kprime = SystematicIndices.ceil(K);
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
    public EncodingPacket getSourcePacket(int esi) {

        checkSourceSymbolESI(esi);

        final EncodingSymbol sourceSymbol = sourceSymbols[esi];
        return EncodingPacket.newSourcePacket(sbn, esi, sourceSymbol.transportData(), 1);
    }

    @Override
    public EncodingPacket getSourcePacket(int esi, int numSymbols) {

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
    public EncodingPacket getRepairPacket(int esi) {

        checkRepairSymbolESI(esi);

        // check if we've got the intermediate symbols already
        if (intermediateSymbols == null) {
            intermediateSymbols = generateIntermediateSymbols();
        }

        // generate repair symbol
        int isi = esi + (Kprime - K);

        byte[] enc_data = LinearSystem.enc(Kprime, intermediateSymbols, new Tuple(Kprime, isi), fecParams.symbolSize());

        // generate repair symbol // TODO should we store the repair symbols generated?
        EncodingSymbol repairSymbol = EncodingSymbol.newRepairSymbol(esi, enc_data);

        // return the repair packet
        return EncodingPacket.newRepairPacket(sbn, esi, repairSymbol.transportData(), 1);
    }

    @Override
    public EncodingPacket getRepairPacket(int esi, int numSymbols) {

        checkRepairSymbolESI(esi);
        checkNumRepairSymbols(esi, numSymbols);

        // check if we've got the intermediate symbols already
        if (intermediateSymbols == null) { // TODO maybe make this thread safe?
            intermediateSymbols = generateIntermediateSymbols();
        }

        // generate repair symbols
        final ByteBuffer symbols = ByteBuffer.allocate(numSymbols * fecParams.symbolSize());
        int isi = esi + (Kprime - K);

        for (int i = 0; i < numSymbols; i++) {
            byte[] enc_data = LinearSystem.enc(
                Kprime, intermediateSymbols, new Tuple(Kprime, isi + i), fecParams.symbolSize());

            // generate repair symbol // TODO should we store the repair symbols generated?
            EncodingSymbol repairSymbol = EncodingSymbol.newRepairSymbol(esi + i, enc_data);

            symbols.put(repairSymbol.transportData());
        }
        symbols.flip();

        // return the repair packet
        return EncodingPacket.newRepairPacket(sbn, esi, symbols.asReadOnlyBuffer(), numSymbols);
    }

    private void checkSourceSymbolESI(int esi) {

        if (esi < 0 || esi >= K) {
            throw new IllegalArgumentException("invalid encoding symbol ID");
        }
    }

    // requires valid ESI
    private final void checkNumSourceSymbols(int esi, int numSymbols) {

        if (numSymbols < 1 || numSymbols > K - esi) {
            throw new IllegalArgumentException("invalid number of symbols");
        }
    }

    private void checkRepairSymbolESI(int esi) {

        if (esi < K || esi > ParameterChecker.maxEncodingSymbolID()) {
            throw new IllegalArgumentException("invalid encoding symbol ID");
        }
    }

    // requires valid ESI
    private final void checkNumRepairSymbols(int esi, int numSymbols) {

        if (numSymbols < 1 || numSymbols > (1 + ParameterChecker.maxEncodingSymbolID() - esi)) {
            throw new IllegalArgumentException("invalid number of symbols");
        }
    }

    private byte[][] generateIntermediateSymbols() {

        // source block's parameters
        int Ki = SystematicIndices.getKIndex(Kprime);
        int S = SystematicIndices.S(Ki);
        int H = SystematicIndices.H(Ki);
        int L = Kprime + S + H;
        int T = fecParams.symbolSize();

        // generate LxL Constraint Matrix
        byte[][] constraint_matrix = LinearSystem.generateConstraintMatrix(Kprime); // A

        // allocate and initialize vector D
        byte[][] D = new byte[L][T];
        for (int row = S + H, index = 0; row < K + S + H; row++, index++)
            D[row] = sourceSymbols[index].data();

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
