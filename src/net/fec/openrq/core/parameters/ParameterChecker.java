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

package net.fec.openrq.core.parameters;


import static net.fec.openrq.core.parameters.InternalConstants.Al_min;
import static net.fec.openrq.core.parameters.InternalConstants.ESI_max;
import static net.fec.openrq.core.parameters.InternalConstants.ESI_min;
import static net.fec.openrq.core.parameters.InternalConstants.F_max;
import static net.fec.openrq.core.parameters.InternalConstants.F_min;
import static net.fec.openrq.core.parameters.InternalConstants.K_max;
import static net.fec.openrq.core.parameters.InternalConstants.K_min;
import static net.fec.openrq.core.parameters.InternalConstants.Kt_max;
import static net.fec.openrq.core.parameters.InternalConstants.N_max;
import static net.fec.openrq.core.parameters.InternalConstants.N_min;
import static net.fec.openrq.core.parameters.InternalConstants.SBN_max;
import static net.fec.openrq.core.parameters.InternalConstants.SBN_min;
import static net.fec.openrq.core.parameters.InternalConstants.T_max;
import static net.fec.openrq.core.parameters.InternalConstants.T_min;
import static net.fec.openrq.core.parameters.InternalConstants.Z_max;
import static net.fec.openrq.core.parameters.InternalConstants.Z_min;
import static net.fec.openrq.core.parameters.InternalFunctions.KL;
import static net.fec.openrq.core.util.arithmetic.ExtraMath.ceilDiv;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ParameterChecker {

    // =========== F -> "transfer length of the object, in octets" ========== //

    /**
     * @return
     */
    public static long minDataLength() {

        return F_min;
    }

    /**
     * @return
     */
    public static long maxDataLength() {

        return F_max;
    }

    // =========== T -> "symbol size, in octets" ========== //

    /**
     * @return
     */
    public static int minSymbolSize() {

        return T_min;
    }

    /**
     * @return
     */
    public static int maxSymbolSize() {

        return T_max;
    }

    // =========== Z -> "number of source blocks" ========== //

    /**
     * @return
     */
    public static int minNumSourceBlocks() {

        return Z_min;
    }

    /**
     * @return
     */
    public static int maxNumSourceBlocks() {

        return Z_max;
    }

    // =========== N -> "number of sub-blocks in each source block" ========== //

    /**
     * @return
     */
    public static int minNumSubBlocks() {

        return N_min;
    }

    /**
     * @return
     */
    public static int maxNumSubBlocks() {

        return N_max;
    }

    // =========== Al -> "symbol alignment parameter" ========== //

    /**
     * @return
     */
    public static int symbolAlignmentValue() {

        return 1; // fixed value
    }

    // =========== F, T, Z, N, Al =========== //

    /**
     * @param dataLen
     * @param symbolSize
     * @param numSourceBlocks
     * @param numSubBlocks
     * @param sAlign
     * @return
     */
    public static boolean areValidFECParameters(
        long dataLen,
        int symbolSize,
        int numSourceBlocks,
        int numSubBlocks,
        int sAlign)
    {

        final long F = dataLen;
        final int T = symbolSize;
        final int Z = numSourceBlocks;
        final int N = numSubBlocks;
        final int Al = sAlign;

        // domain restrictions
        if ((F < F_min) || (F_max < F) ||
            (T < Al) || (T_max < T) ||
            (Z < Z_min) || (Z_max < Z) ||
            (N < N_min) || (N_max < N) ||
            (Al < Al_min)) {
            return false;
        }

        // T must be a multiple of Al
        if (T % Al != 0) {
            return false;
        }
        // the number of symbols cannot exceed Kt_max
        if (ceilDiv(F, T) > Kt_max) {
            return false;
        }

        // number of symbols (the downcast never overflows)
        final int Kt = (int)ceilDiv(F, T);

        // at least one symbol, and at most K_max symbols in each source block
        if (Z > Kt || Z < ceilDiv(Kt, K_max)) {
            return false;
        }

        // sub-symbol size must be at least Al
        if (N > T / Al) {
            return false;
        }

        return true;
    }

    // =========== F, P', WS, Al =========== //

    /**
     * @param dataLen
     * @param maxPayLen
     * @param maxDecBlock
     * @param sAlign
     * @return
     */
    public static boolean areValidDerivingParameters(long dataLen, int maxPayLen, int maxDecBlock, int sAlign) {

        final long F = dataLen;
        final int P = maxPayLen;
        final int WS = maxDecBlock;
        final int Al = sAlign;

        // domain restrictions
        if (F < F_min || F_max < F ||
            P < Al || // (no upper bound due to it being a max value)
            WS < 1 || // ...
            Al < Al_min) {
            return false;
        }

        // P must be a multiple of Al
        if (P % Al != 0) {
            return false;
        }

        // the number of symbols cannot exceed Kt_max
        if (P < ceilDiv(F, Kt_max)) {
            return false;
        }

        // T is at most P but cannot exceed T_max
        final int T = Math.min(P, T_max);

        // number of symbols (the downcast never overflows)
        final int Kt = (int)ceilDiv(F, T);

        // WS cannot be too small
        if (WS < K_min * T) {
            return false;
        }

        // number of symbols in a source block (always between K_min and K_max)
        final int KL = KL(N_max, WS, Al, T);

        // the number of source blocks cannot exceed Z_max
        if (ceilDiv(Kt, KL) > Z_max) {
            return false;
        }

        return true;
    }

    // =========== source block number - SBN ========== //

    /**
     * @return
     */
    public static int minSourceBlockNumber() {

        return SBN_min;
    }

    /**
     * @return
     */
    public static int maxSourceBlockNumber() {

        return SBN_max;
    }

    // =========== encoding symbol identifier - ESI ========== //

    /**
     * @return
     */
    public static int minEncodingSymbolID() {

        return ESI_min;
    }

    /**
     * @return
     */
    public static int maxEncodingSymbolID() {

        return ESI_max;
    }

    // =========== SBN, ESI =========== //

    /**
     * @param sbn
     * @param esi
     * @param numSourceBlocks
     * @return
     * @exception IllegalArgumentException
     *                If {@code numSourceBlocks} is invalid
     */
    public static boolean isValidFECPayloadID(int sbn, int esi, int numSourceBlocks) {

        if (numSourceBlocks < minSourceBlockNumber() || numSourceBlocks > maxSourceBlockNumber()) {
            throw new IllegalArgumentException("invalid number of source blocks");
        }

        return sbn >= minSourceBlockNumber() &&
               sbn < numSourceBlocks &&
               esi >= minEncodingSymbolID() &&
               esi <= maxEncodingSymbolID();
    }

    // =========== number of source symbols - K =========== //

    /**
     * @return
     */
    public static int maxNumSourceSymbolsPerBlock() {

        return K_max;
    }

    private ParameterChecker() {

        // not instantiable
    }
}
