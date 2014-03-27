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


import net.fec.openrq.core.encoder.DataEncoderBuilder;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class OpenRQ {

    /**
     * @param data
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static DataEncoderBuilder<ArrayDataEncoder> newEncoderBuilder(byte[] data) {

        return newEncoderBuilder(data, 0, data.length);
    }

    /**
     * @param data
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static DataEncoderBuilder<ArrayDataEncoder> newEncoderBuilder(byte[] data, int beginIndex, int endIndex) {

        checkIndexBounds(beginIndex, endIndex, data.length);
        return Builders.newEncoderBuilder(data, beginIndex, endIndex - beginIndex);
    }

    /**
     * @param symbolSize
     * @param numSourceBlocks
     * @param numSubBlocks
     * @param data
     * @return
     */
    public static ArrayDataEncoder newEncoder(int symbolSize, int numSourceBlocks, int numSubBlocks, byte[] data) {

        return newEncoder(symbolSize, numSourceBlocks, numSubBlocks, data, 0, data.length);
    }

    /**
     * @param symbolSize
     * @param numSourceBlocks
     * @param numSubBlocks
     * @param data
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static ArrayDataEncoder newEncoder(
        int symbolSize,
        int numSourceBlocks,
        int numSubBlocks,
        byte[] data,
        int beginIndex,
        int endIndex)
    {

        checkIndexBounds(beginIndex, endIndex, data.length);
        final int len = endIndex - beginIndex;
        final FECParameters fecParams = newParams(len, symbolSize, numSourceBlocks, numSubBlocks);
        return ArrayDataEncoder.newEncoder(data, beginIndex, fecParams);
    }

    /**
     * @param fecParams
     * @param extraSymbols
     * @return
     */
    public static ArrayDataDecoder newDecoder(FECParameters fecParams, int extraSymbols) {

        return ArrayDataDecoder.newDecoder(fecParams, extraSymbols);
    }

    /**
     * @param dataLen
     * @param symbolSize
     * @param numSourceBlocks
     * @param numSubBlocks
     * @param extraSymbols
     * @return
     */
    public static ArrayDataDecoder newDecoder(
        int dataLen,
        int symbolSize,
        int numSourceBlocks,
        int numSubBlocks,
        int extraSymbols)
    {

        final FECParameters fecParams = newParams(dataLen, symbolSize, numSourceBlocks, numSubBlocks);
        return ArrayDataDecoder.newDecoder(fecParams, extraSymbols);
    }

    private static void checkIndexBounds(int beginIndex, int endIndex, int arrayLen) {

        if (beginIndex < 0 || endIndex > arrayLen || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
    }

    private static FECParameters newParams(int F, int T, int Z, int N) {

        return FECParameters.makeFECParameters(F, T, Z, N);
    }

    private OpenRQ() {

        // not instantiable
    }
}
