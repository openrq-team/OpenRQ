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

    /**
     * @param dataLen
     * @return
     */
    public static boolean isValidDataLength(long dataLen) {

        return dataLen >= minDataLength() && dataLen <= maxDataLength();
    }

    // =========== symbol size - T ========== //

    /**
     * @return
     */
    public static long minSymbolSize() {

        return InternalConstants.MIN_T;
    }

    /**
     * @return
     */
    public static long maxSymbolSize() {

        return InternalConstants.MAX_T;
    }

    /**
     * @param symbolSize
     * @return
     */
    public static boolean isValidSymbolSize(int symbolSize) {

        return symbolSize >= minSymbolSize() && symbolSize <= maxSymbolSize();
    }

    // =========== number of source blocks - Z ========== //

    /**
     * @return
     */
    public static long minNumSourceBlocks() {

        return InternalConstants.MIN_Z;
    }

    /**
     * @return
     */
    public static long maxNumSourceBlocks() {

        return InternalConstants.MAX_Z;
    }

    /**
     * @param numSourceBlocks
     * @return
     */
    public static boolean isValidNumSourceBlocks(int numSourceBlocks) {

        return numSourceBlocks >= minNumSourceBlocks() && numSourceBlocks <= maxNumSourceBlocks();
    }

    // =========== number of sub-blocks - N ========== //

    /**
     * @return
     */
    public static long minNumSubBlocks() {

        return InternalConstants.MIN_N;
    }

    /**
     * @return
     */
    public static long maxNumSubBlocks() {

        return InternalConstants.MAX_N;
    }

    /**
     * @param numSubBlocks
     * @return
     */
    public static boolean isValidNumSubBlocks(int numSubBlocks) {

        return numSubBlocks >= minNumSubBlocks() && numSubBlocks <= maxNumSubBlocks();
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
