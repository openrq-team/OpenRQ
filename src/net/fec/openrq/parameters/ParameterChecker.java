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

package net.fec.openrq.parameters;


import net.fec.openrq.util.arithmetic.ExtraMath;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ParameterChecker {

    // =========== data length - F ========== //

    /**
     * @return
     */
    public static long minDataLength() {

        return InternalConstants.MIN_F;
    }

    /**
     * @return
     */
    public static long maxDataLength() {

        return InternalConstants.MAX_F;
    }

    // =========== symbol size - T ========== //

    /**
     * @return
     */
    public static int minSymbolSize() {

        return InternalConstants.MIN_T;
    }

    /**
     * @return
     */
    public static int maxSymbolSize() {

        return InternalConstants.MAX_T;
    }

    // =========== number of source blocks - Z ========== //

    /**
     * @return
     */
    public static int minNumSourceBlocks() {

        return InternalConstants.MIN_Z;
    }

    /**
     * @return
     */
    public static int maxNumSourceBlocks() {

        return InternalConstants.MAX_Z;
    }

    // =========== number of sub-blocks - N ========== //

    /**
     * @return
     */
    public static int minNumSubBlocks() {

        return InternalConstants.MIN_N;
    }

    /**
     * @return
     */
    public static int maxNumSubBlocks() {

        return InternalConstants.MAX_N;
    }

    // =========== F, T, Z, N =========== //

    /**
     * @param dataLength
     * @param symbolSize
     * @param numSourceBlocks
     * @param numSubBlocks
     * @return
     */
    public static boolean areValidFECParameters(long dataLength, int symbolSize, int numSourceBlocks, int numSubBlocks) {

        final long F = dataLength;
        final int T = symbolSize;
        final int Z = numSourceBlocks;
        final int N = numSubBlocks;
        final int Al = symbolAlignmentValue();

        // check max-min bounds
        if ((F < minDataLength()) | (maxDataLength() < F) |
            (T < minSymbolSize()) | (maxSymbolSize() < T) |
            (Z < minNumSourceBlocks()) | (maxSourceBlockNumber() < Z) |
            (N < minNumSubBlocks()) | (maxNumSubBlocks() < N)) {
            return false;
        }

        // check multiple of symbol alignment
        if ((T % Al) != 0) {
            return false;
        }

        final long Kt = ExtraMath.ceilDiv(F, T);

        // check partitioning bounds
        if (T > F || Kt > Z || N > (T / Al)) {
            return false;
        }

        // check number of symbols
        if (ExtraMath.ceilDiv(Kt, Z) > InternalConstants.K_MAX) {
            return false;
        }

        return true;
    }

    // =========== symbol alignment - Al ========== //

    /**
     * @return
     */
    public static int symbolAlignmentValue() {

        return InternalConstants.ALIGN_VALUE;
    }

    public static boolean isValidSymbolAlignment(int symbolAlign) {

        return symbolAlign == symbolAlignmentValue();
    }

    // =========== source block number - SBN ========== //

    /**
     * @return
     */
    public static int minSourceBlockNumber() {

        return InternalConstants.MIN_SBN;
    }

    /**
     * @return
     */
    public static int maxSourceBlockNumber() {

        return InternalConstants.MAX_SBN;
    }

    /**
     * @param sourceBlockNum
     * @return
     */
    public static boolean isValidSourceBlockNumber(int sourceBlockNum) {

        return sourceBlockNum >= minSourceBlockNumber() && sourceBlockNum <= maxSourceBlockNumber();
    }

    // =========== encoding symbol identifier - ESI ========== //

    /**
     * @return
     */
    public static int minEncodingSymbolID() {

        return InternalConstants.MIN_ESI;
    }

    /**
     * @return
     */
    public static int maxEncodingSymbolID() {

        return InternalConstants.MAX_ESI;
    }

    /**
     * @param encSymbolID
     * @return
     */
    public static boolean isValidEncodingSymbolID(int encSymbolID) {

        return encSymbolID >= minEncodingSymbolID() && encSymbolID <= maxEncodingSymbolID();
    }

    private ParameterChecker() {

        // not instantiable
    }
}
