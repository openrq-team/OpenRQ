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
import java.util.Set;

import net.fec.openrq.core.decoder.SourceBlockDecoder;
import net.fec.openrq.core.decoder.SourceBlockState;
import net.fec.openrq.core.util.bytevector.Facades;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class ArraySourceBlockDecoder implements SourceBlockDecoder {

    // requires valid arguments
    static ArraySourceBlockDecoder newDecoder(
        byte[] array,
        int off,
        FECParameters fecParams,
        int sbn,
        int K,
        int extraSymbols)
    {

        final int Kprime = SystematicIndices.ceil(K);
        final int sourceLen = Math.min(K * fecParams.symbolSize(), array.length - off);
        final int extendedLen = Kprime * fecParams.symbolSize();

        return new ArraySourceBlockDecoder(array, off, sourceLen, extendedLen, fecParams, sbn, K, extraSymbols);
    }


    private final PaddedByteVector data;

    private final FECParameters fecParams;
    private final int sbn;
    private final int K;
    private final int symbolOverhead;


    private ArraySourceBlockDecoder(
        byte[] array,
        int off,
        int sourceLen,
        int extendedLen,
        FECParameters fecParams,
        int sbn,
        int K,
        int extraSymbols)
    {

        this.data = PaddedByteVector.newVector(Facades.wrapByteArray(array), off, sourceLen, extendedLen);

        this.fecParams = fecParams;
        this.sbn = sbn;
        this.K = K;
        this.symbolOverhead = extraSymbols;
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
    public boolean containsSourceSymbol(int encSymbolID) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsRepairSymbol(int encSymbolID) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSourceBlockDecoded() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<Integer> missingSourceSymbols() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SourceBlockState putSourceSymbol(int encSymbolID, ByteBuffer sourceSymbol) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SourceBlockState putSourceSymbol(int encSymbolID, byte[] sourceSymbol, int offset) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SourceBlockState putRepairSymbol(int encSymbolID, ByteBuffer repairSymbol) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SourceBlockState putRepairSymbol(int encSymbolID, byte[] repairSymbol, int offset) {

        // TODO Auto-generated method stub
        return null;
    }
}
