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


import static net.fec.openrq.parameters.InternalConstants.Al_min;
import static net.fec.openrq.parameters.InternalConstants.ESI_max;
import static net.fec.openrq.parameters.InternalConstants.ESI_min;
import static net.fec.openrq.parameters.InternalConstants.F_max;
import static net.fec.openrq.parameters.InternalConstants.F_min;
import static net.fec.openrq.parameters.InternalConstants.K_max;
import static net.fec.openrq.parameters.InternalConstants.K_min;
import static net.fec.openrq.parameters.InternalConstants.Kt_max;
import static net.fec.openrq.parameters.InternalConstants.N_max;
import static net.fec.openrq.parameters.InternalConstants.N_min;
import static net.fec.openrq.parameters.InternalConstants.SBN_max;
import static net.fec.openrq.parameters.InternalConstants.SBN_min;
import static net.fec.openrq.parameters.InternalConstants.T_max;
import static net.fec.openrq.parameters.InternalConstants.T_min;
import static net.fec.openrq.parameters.InternalConstants.Z_max;
import static net.fec.openrq.parameters.InternalConstants.Z_min;
import static net.fec.openrq.parameters.InternalFunctions.KL;
import static net.fec.openrq.parameters.InternalFunctions.minWS;
import static net.fec.openrq.util.arithmetic.ExtraMath.ceilDiv;


/**
 * This class provides methods for checking bounds and validating FEC and encoding packet parameters.
 */
public final class ParameterChecker {

    // =========== F -> "transfer length of the object, in octets" ========== //

    /**
     * Returns the minimum source data length, in number of bytes (1).
     * 
     * @return the minimum source data length, in number of bytes
     */
    public static long minDataLength() {

        return F_min;
    }

    /**
     * Returns the maximum source data length, in number of bytes (946_270_874_880L).
     * 
     * @return the maximum source data length, in number of bytes
     */
    public static long maxDataLength() {

        return F_max;
    }

    /**
     * Returns {@code true} iff {@linkplain #minDataLength() minDataLen} <= <b>dataLen</b> <=
     * {@linkplain #maxDataLength() maxDataLen}.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @return {@code true} iff {@linkplain #minDataLength() minDataLen} <= <b>dataLen</b> <=
     *         {@linkplain #maxDataLength() maxDataLen}
     */
    public static boolean isDataLengthWithinBounds(long dataLen) {

        return minDataLength() <= dataLen && dataLen <= maxDataLength();
    }

    // =========== T -> "symbol size, in octets" ========== //

    /**
     * Returns the minimum symbol size, in number of bytes (1).
     * 
     * @return the minimum symbol size, in number of bytes
     */
    public static int minSymbolSize() {

        return T_min;
    }

    /**
     * Returns the maximum symbol size, in number of bytes (65535).
     * 
     * @return the maximum symbol size, in number of bytes
     */
    public static int maxSymbolSize() {

        return T_max;
    }

    /**
     * Returns {@code true} iff {@linkplain #minSymbolSize() minSymbSize} <= <b>symbSize</b> <=
     * {@linkplain #maxSymbolSize() maxSymbSize}.
     * 
     * @param symbSize
     *            A symbol size, in number of bytes
     * @return {@code true} iff {@linkplain #minSymbolSize() minSymbSize} <= <b>symbSize</b> <=
     *         {@linkplain #maxSymbolSize() maxSymbSize}
     */
    public static boolean isSymbolSizeWithinBounds(long symbSize) {

        return minSymbolSize() <= symbSize && symbSize <= maxSymbolSize();
    }

    // =========== Z -> "number of source blocks" ========== //

    /**
     * Returns the minimum number of source blocks into which a source data is divided (1).
     * 
     * @return the minimum number of source blocks into which a source data is divided
     */
    public static int minNumSourceBlocks() {

        return Z_min;
    }

    /**
     * Returns the maximum number of source blocks into which a source data is divided (256).
     * 
     * @return the maximum number of source blocks into which a source data is divided
     */
    public static int maxNumSourceBlocks() {

        return Z_max;
    }

    /**
     * Returns {@code true} iff {@linkplain #minNumSourceBlocks() minSrcBs} <= <b>numSrcBs</b> <=
     * {@linkplain #maxNumSourceBlocks() maxSrcBs}.
     * 
     * @param numSrcBs
     *            A number of source blocks into which a source data is divided
     * @return {@code true} iff {@linkplain #minNumSourceBlocks() minSrcBs} <= <b>numSrcBs</b> <=
     *         {@linkplain #maxNumSourceBlocks() maxSrcBs}
     */
    public static boolean isNumberOfSourceBlocksWithinBounds(long numSrcBs) {

        return minNumSourceBlocks() <= numSrcBs && numSrcBs <= maxNumSourceBlocks();
    }

    // =========== N -> "interleaver length, in number of sub-blocks" ========== //

    /**
     * Returns the minimum interleaver length, in number of sub-blocks per source block (1).
     * 
     * @return the interleaver length, in number of sub-blocks per source block
     */
    public static int minInterleaverLength() {

        return N_min;
    }

    /**
     * Returns the maximum interleaver length, in number of sub-blocks per source block (1).
     * <p>
     * <b>Note:</b> <em>For now, interleaving is disabled.</em>
     * 
     * @return the maximum interleaver length, in number of sub-blocks per source block
     */
    public static int maxInterleaverLength() {

        return N_max;
    }

    /**
     * Returns {@code true} iff {@linkplain #minInterleaverLength() minInterLen} <= <b>interLen</b> <=
     * {@linkplain #maxInterleaverLength() maxInterLen}.
     * 
     * @param interLen
     *            An interleaver length, in number of sub-blocks per source block
     * @return {@code true} iff {@linkplain #minInterleaverLength() minInterLen} <= <b>interLen</b> <=
     *         {@linkplain #maxInterleaverLength() maxInterLen}
     */
    public static boolean isInterleaverLengthWithinBounds(long interLen) {

        return minInterleaverLength() <= interLen && interLen <= maxInterleaverLength();
    }

    // =========== Al -> "symbol alignment parameter" ========== //

    /**
     * Returns the symbol alignment parameter (1).
     * <p>
     * <b>Note:</b> <em>This value is fixed in this implementation of RaptorQ.</em>
     * 
     * @return the symbol alignment parameter
     */
    public static int symbolAlignmentValue() {

        return 1; // fixed value
    }

    // =========== F, T, Z, N, Al =========== //

    /**
     * Returns {@code true} if, and only if, the provided FEC parameters are within certain bounds defined below (the
     * existence of minimum and maximum values for the FEC parameters, along with a
     * {@linkplain #maxNumSourceSymbolsPerBlock() maximum number of source symbols per source block}, enforces tighter
     * bounds around combinations of the parameter values).
     * <p>
     * <b><u>Restrictions over domain</u></b>
     * <p>
     * All parameters must be within their specific bounds (refer to methods for
     * {@linkplain #isDataLengthWithinBounds(long) data
     * length}, {@linkplain #isSymbolSizeWithinBounds(long) symbol
     * size}, {@linkplain #isNumberOfSourceBlocksWithinBounds(long)
     * number of source blocks} and {@linkplain #isInterleaverLengthWithinBounds(long) interleaver length}).
     * <p>
     * <b><u>Restrictions over value combinations</u></b>
     * <p>
     * Gist: <em>"If the data length is small/large, the symbol size must be equally small/large as well."</em>
     * <p>
     * Let <b>maxSymbPerBlock</b> be the {@linkplain #maxNumSourceSymbolsPerBlock() maximum number of source symbols per
     * source block}. The following item must be true:
     * <ul>
     * <li>{@code ceiling}(<b>dataLen</b> / <b>symbSize</b>) &le; <b>maxSymbPerBlock</b>
     * </ul>
     * <p>
     * Gist:
     * <em>"There cannot be more source blocks than source symbols; there cannot be too many source symbols per source block".</em>
     * <p>
     * Let <b>totalSymb</b> be the total number of symbols calculated as follows: <b>totalSymb</b> := {@code ceiling}
     * (<b>dataLen</b> / <b>symbSize</b>). The following items must all be true:
     * <ul>
     * <li><b>numSrcBs</b> &le; <b>totalSymb</b>
     * <li><b>numSrcBs</b> &ge; {@code ceiling}(<b>totalSymb</b> / <b>maxSymbPerBlock</b>)
     * </ul>
     * <p>
     * The following item must be true:
     * <ul>
     * <li><b>interLen</b> &le; <b>symbSize</b>
     * </ul>
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param symbSize
     *            A symbol size, in number of bytes
     * @param numSrcBs
     *            A number of source blocks into which a source data is divided
     * @param interLen
     *            An interleaver length, in number of sub-blocks per source block
     * @return {@code true} if, and only if, the provided FEC parameters are within certain bounds
     */
    public static boolean areValidFECParameters(long dataLen, int symbSize, int numSrcBs, int interLen) {

        // empty string means parameters are all valid
        return getFECParamsErrorString(dataLen, symbSize, numSrcBs, interLen).isEmpty();
    }

    /**
     * Tests multiple cases and returns an error string if any FEC parameter is invalid, otherwise the method returns an
     * empty string.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param symbSize
     *            A symbol size, in number of bytes
     * @param numSrcBs
     *            A number of source blocks into which a source data is divided
     * @param interLen
     *            An interleaver length, in number of sub-blocks per source block
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     */
    public static String getFECParamsErrorString(long dataLen, int symbSize, int numSrcBs, int interLen) {

        final long F = dataLen;
        final int T = symbSize;
        final int Z = numSrcBs;
        final int N = interLen;
        final int Al = symbolAlignmentValue();

        // domain restrictions
        if (!isDataLengthWithinBounds(F)) {
            return String.format(
                "data length (%d) must be within [%d, %d] bytes",
                F, F_min, F_max);
        }
        if (Al < Al_min) {
            return String.format(
                "symbol alignment value (%d) must be at least %d",
                Al, Al_min);
        }
        if ((T < Al) || (T_max < T)) {
            return String.format(
                "symbol size (%d) must be within [%d, %d] bytes",
                T, Al, T_max);
        }
        if (!isNumberOfSourceBlocksWithinBounds(Z)) {
            return String.format(
                "number of source blocks (%d) must be within [%d, %d]",
                Z, Z_min, Z_max);
        }
        if (!isInterleaverLengthWithinBounds(N)) {
            return String.format(
                "interleaver length (%d) must be within [%d, %d]",
                N, N_min, N_max);
        }

        // T must be a multiple of Al
        if (T % Al != 0) {
            return String.format(
                "symbol size (%d) must be a multiple of the symbol alignment value %d",
                T, Al);
        }
        // the number of symbols cannot exceed Kt_max
        if (ceilDiv(F, T) > Kt_max) {
            return String.format(
                "a data length of %d bytes requires a symbol size (%d) of at least %d bytes",
                F, T, ceilDiv(F, Kt_max));
        }

        // number of symbols (the downcast never overflows)
        final int Kt = (int)ceilDiv(F, T);

        // at least one symbol, and at most K_max symbols in each source block
        if (Z > Kt || Z < ceilDiv(Kt, K_max)) {
            return String.format(
                "a data length of %d bytes and a symbol size of %d bytes require a number of source blocks (%d) within [%d, %d]",
                F, T, Z, ceilDiv(Kt, K_max), Math.min(Z_max, Kt));
        }

        // interleaver length must be at least Al
        if (N > T / Al) {
            return String.format(
                "a symbol size of %d bytes and a symbol alignment value of %d require an interleaver length (%d) at most %d",
                T, Al, N, T / Al);
        }

        // empty string means parameters are all valid
        return "";
    }

    // =========== F, P', WS, Al =========== //

    /**
     * TODO document
     * 
     * @param dataLen
     * @param maxPaLen
     * @param maxDBMem
     * @param sAlign
     * @return
     */
    public static boolean areValidDerivingParameters(long dataLen, int maxPaLen, int maxDBMem, int sAlign) {

        return getDerivingParamsErrorString(dataLen, maxPaLen, maxDBMem, sAlign).isEmpty();
    }

    /**
     * Tests multiple cases and returns an error string if any deriving parameter is invalid, otherwise the method
     * returns an empty string.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param maxPaLen
     *            A maximum size for a payload containing one encoding symbol
     * @param maxDBMem
     *            A maximum block size, in number of bytes that is decodable in working memory
     * @param sAlign
     *            The symbol alignment parameter
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     */
    public static String getDerivingParamsErrorString(long dataLen, int maxPaLen, int maxDBMem, int sAlign) {

        final long F = dataLen;
        final int P = maxPaLen;
        final int WS = maxDBMem;
        final int Al = sAlign;

        // domain restrictions
        if (!isDataLengthWithinBounds(F)) {
            return String.format(
                "data length (%d) must be within [%d, %d] bytes",
                F, F_min, F_max);
        }
        if (Al < Al_min) {
            return String.format(
                "symbol aligment value (%d) must be at least %d",
                Al, Al_min);
        }
        if (P < Al) { // (no upper bound due to it being a max value)
            return String.format(
                "max payload length (%d) must be at least %d %s",
                P, Al, Al == 1 ? "byte" : "bytes");
        }
        if (WS < 1) { // ...
            return String.format(
                "max decoding block length (%d) must be at least 1 byte",
                WS);
        }

        // P must be a multiple of Al
        if (P % Al != 0) {
            return String.format(
                "max payload length (%d) must be a multiple of %d",
                P, Al);
        }

        // the number of symbols cannot exceed Kt_max
        if (P < ceilDiv(F, Kt_max)) {
            return String.format(
                "a data length of %d bytes requires a max payload length (%d) of at least %d bytes",
                F, P, ceilDiv(F, Kt_max));
        }

        // T is at most P but cannot exceed T_max
        final int T = Math.min(P, T_max);

        // number of symbols (the downcast never overflows)
        final int Kt = (int)ceilDiv(F, T);

        // WS cannot be too small
        if (WS < K_min * T) {
            return String.format(
                "a symbol size of %d bytes requires a max decoding block length (%d) of at least %d bytes",
                T, WS, K_min * T);
        }

        // number of symbols in a source block (always between K_min and K_max)
        final int KL = KL(N_max, WS, Al, T);

        // the number of source blocks cannot exceed Z_max
        if (KL < ceilDiv(Kt, Z_max)) {
            return String.format(
                "a data length of %d bytes and a symbol size of %d bytes require a max decoding block length (%d) of at least %d bytes",
                F, T, WS, minWS(ceilDiv(Kt, Z_max), N_max, Al, T));
        }

        return "";
    }

    // =========== source block number - SBN ========== //

    /**
     * Returns the minimum source block number (0).
     * 
     * @return the minimum source block number
     */
    public static int minSourceBlockNumber() {

        return SBN_min;
    }

    /**
     * Returns the maximum source block number (255).
     * 
     * @return the maximum source block number
     */
    public static int maxSourceBlockNumber() {

        return SBN_max;
    }

    /**
     * Returns {@code true} iff {@linkplain #minSourceBlockNumber() minSBN} <= <b>sbn</b> <=
     * {@linkplain #maxSourceBlockNumber() maxSBN}.
     * 
     * @param sbn
     *            A source block number
     * @return {@code true} iff {@linkplain #minSourceBlockNumber() minSBN} <= <b>sbn</b> <=
     *         {@linkplain #maxSourceBlockNumber() maxSBN}
     */
    public static boolean isSourceBlockNumberWithinBounds(int sbn) {

        return minSourceBlockNumber() <= sbn && sbn <= maxSourceBlockNumber();
    }

    // =========== encoding symbol identifier - ESI ========== //

    /**
     * Returns the minimum encoding symbol identifier (0).
     * 
     * @return the minimum encoding symbol identifier
     */
    public static int minEncodingSymbolID() {

        return ESI_min;
    }

    /**
     * Returns the maximum encoding symbol identifier (16_777_215).
     * 
     * @return the maximum encoding symbol identifier
     */
    public static int maxEncodingSymbolID() {

        return ESI_max;
    }

    /**
     * Returns {@code true} iff {@linkplain #minEncodingSymbolID() minESI} <= <b>esi</b> <=
     * {@linkplain #maxEncodingSymbolID() maxESI}.
     * 
     * @param esi
     *            An encoding symbol ID
     * @return {@code true} iff {@linkplain #minEncodingSymbolID() minESI} <= <b>esi</b> <=
     *         {@linkplain #maxEncodingSymbolID() maxESI}
     */
    public static boolean isEncodingSymbolIDWithinBounds(int esi) {

        return minEncodingSymbolID() <= esi && esi <= maxEncodingSymbolID();
    }

    // =========== SBN, ESI =========== //

    /**
     * TODO document
     * 
     * @param sbn
     * @param esi
     * @param numSrcBs
     * @return
     * @exception IllegalArgumentException
     *                If the number of source blocks is out of bounds
     */
    public static boolean isValidFECPayloadID(int sbn, int esi, int numSrcBs) {

        return getFECPayloadIDErrorString(sbn, esi, numSrcBs).isEmpty();
    }

    /**
     * Tests multiple cases and returns an error string if any FEC Payload ID parameter is invalid, otherwise the method
     * returns an empty string.
     * 
     * @param sbn
     *            A source block number
     * @param esi
     *            The encoding symbol identifier of the first symbol in an encoding packet
     * @param numSrcBs
     *            A number of source blocks into which a source data is divided
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     * @exception IllegalArgumentException
     *                If the number of source blocks is out of bounds
     */
    public static String getFECPayloadIDErrorString(int sbn, int esi, int numSrcBs) {

        final int Z = numSrcBs;
        if (!isNumberOfSourceBlocksWithinBounds(Z)) {
            throw new IllegalArgumentException("invalid number of source blocks");
        }

        if (sbn < SBN_min || sbn > Z) {
            return String.format("source block number (%d) must be whithin [%d, %d]", sbn, SBN_min, Z);
        }
        if (!isEncodingSymbolIDWithinBounds(esi)) {
            return String.format("encoding symbol identifier (%d) must be whithin [%d, %d]", esi, ESI_min, ESI_max);
        }
        return "";
    }

    // =========== number of source symbols - K =========== //

    /**
     * Returns the maximum number of source symbols in a source block (56_403).
     * 
     * @return the maximum number of source symbols in a source block
     */
    public static int maxNumSourceSymbolsPerBlock() {

        return K_max;
    }

    private ParameterChecker() {

        // not instantiable
    }
}
