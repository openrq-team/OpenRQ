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


import static net.fec.openrq.parameters.InternalConstants.Al;
import static net.fec.openrq.parameters.InternalConstants.ESI_max;
import static net.fec.openrq.parameters.InternalConstants.ESI_min;
import static net.fec.openrq.parameters.InternalConstants.F_max;
import static net.fec.openrq.parameters.InternalConstants.F_min;
import static net.fec.openrq.parameters.InternalConstants.K_max;
import static net.fec.openrq.parameters.InternalConstants.Kt_max;
import static net.fec.openrq.parameters.InternalConstants.N_max;
import static net.fec.openrq.parameters.InternalConstants.N_min;
import static net.fec.openrq.parameters.InternalConstants.SBN_max;
import static net.fec.openrq.parameters.InternalConstants.SBN_min;
import static net.fec.openrq.parameters.InternalConstants.T_max;
import static net.fec.openrq.parameters.InternalConstants.T_min;
import static net.fec.openrq.parameters.InternalConstants.Z_max;
import static net.fec.openrq.parameters.InternalConstants.Z_min;
import static net.fec.openrq.parameters.InternalFunctions.minWS;
import static net.fec.openrq.util.arithmetic.ExtraMath.ceilDiv;


/**
 * This class provides methods for checking bounds and validating FEC and encoding packet parameters.
 * <a name="fec-parameter-bounds"> <h5>FEC parameters bounds</h5></a>
 * <p>
 * FEC parameters have well defined bounds, and any parameter that is outside its bounds is considered invalid. It is
 * important to know these bounds in order to be able to create instances of the class {@link FECParameters}, which
 * contain only valid parameters.
 * <p>
 * By default, every FEC parameter has a minimum and a maximum value. For example, the method
 * {@link #maxNumSourceBlocks()} returns the theoretical upper bound on the number of source blocks. However, these
 * minimum and maximum values are not sufficient to limit the possible (valid) parameter values. When multiple
 * parameters are defined, they are also constrained in a way that is conditioned on the combination of the used values.
 * For example, if a very large source data length is defined, then a relatively large symbol size must be defined as
 * well, because there is a limit on the number of symbols into which a source data can be divided.
 * <p>
 * Methods are provided for obtaining these conditional bounds. They are summarized below for ease of access.
 * <p>
 * Given a <b>source data length</b> it is possible to obtain the <b>minimum allowed symbol size</b>; conversely, given
 * a <b>symbol size</b> it is possible to obtain the <b>maximum allowed source data length</b>. Refer to methods for
 * each:
 * <p>
 * <ul>
 * <li>{@link #minAllowedSymbolSize(long)}
 * <li>{@link #maxAllowedDataLength(int)}
 * </ul>
 * <p>
 * Given a <b>source data length</b> and a <b>symbol size</b>, which are valid in respect to each other, it is possible
 * to obtain a <b>minimum and a maximum allowed number of source blocks</b>. Refer to methods for each:
 * <ul>
 * <li>{@link #minAllowedNumSourceBlocks(long, int)}
 * <li>{@link #maxAllowedNumSourceBlocks(long, int)}
 * </ul>
 * <p>
 * Given a <b>symbol size</b>, it is possible to obtain a <b>maximum allowed interleaver length</b>. Refer to the
 * method:
 * <ul>
 * <li>{@link #maxAllowedInterleaverLength(int)}
 * </ul>
 * <p>
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
     * Returns {@code false} iff {@linkplain #minDataLength() minDataLen} <= <b>dataLen</b> <=
     * {@linkplain #maxDataLength() maxDataLen}.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @return {@code false} iff {@linkplain #minDataLength() minDataLen} <= <b>dataLen</b> <=
     *         {@linkplain #maxDataLength() maxDataLen}
     */
    public static boolean isDataLengthOutOfBounds(long dataLen) {

        return minDataLength() <= dataLen && dataLen <= maxDataLength();
    }

    private static void _checkDataLengthOutOfBounds(long F) {

        if (isDataLengthOutOfBounds(F)) {
            throw new IllegalArgumentException("source data length is out of bounds");
        }
    }

    /**
     * Returns the maximum allowed data length given a symbol size.
     * 
     * @param symbSize
     *            A symbol size, in number of bytes
     * @return the maximum allowed data length given a symbol size
     * @exception IllegalArgumentException
     *                If the symbol size is {@linkplain #isSymbolSizeOutOfBounds(int) out of bounds}
     */
    public static long maxAllowedDataLength(int symbSize) {

        _checkSymbolSizeOutOfBounds(symbSize);
        return _maxAllowedDataLength(symbSize);
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
     * Returns {@code false} iff {@linkplain #minSymbolSize() minSymbSize} <= <b>symbSize</b> <=
     * {@linkplain #maxSymbolSize() maxSymbSize}.
     * 
     * @param symbSize
     *            A symbol size, in number of bytes
     * @return {@code false} iff {@linkplain #minSymbolSize() minSymbSize} <= <b>symbSize</b> <=
     *         {@linkplain #maxSymbolSize() maxSymbSize}
     */
    public static boolean isSymbolSizeOutOfBounds(int symbSize) {

        return minSymbolSize() <= symbSize && symbSize <= maxSymbolSize();
    }

    private static void _checkSymbolSizeOutOfBounds(int T) {

        if (isSymbolSizeOutOfBounds(T)) {
            throw new IllegalArgumentException("symbol size is out of bounds");
        }
    }

    /**
     * Returns the minimum allowed symbol size given a source data length.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @return the minimum allowed symbol size given a source data length.
     * @exception IllegalArgumentException
     *                If the source data length is {@linkplain #isDataLengthOutOfBounds(long) out of bounds}
     */
    public int minAllowedSymbolSize(long dataLen) {

        _checkDataLengthOutOfBounds(dataLen);
        return _minAllowedSymbolSize(dataLen);
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
     * Returns {@code false} iff {@linkplain #minNumSourceBlocks() minSrcBs} <= <b>numSrcBs</b> <=
     * {@linkplain #maxNumSourceBlocks() maxSrcBs}.
     * 
     * @param numSrcBs
     *            A number of source blocks into which a source data is divided
     * @return {@code false} iff {@linkplain #minNumSourceBlocks() minSrcBs} <= <b>numSrcBs</b> <=
     *         {@linkplain #maxNumSourceBlocks() maxSrcBs}
     */
    public static boolean isNumSourceBlocksOutOfBounds(int numSrcBs) {

        return minNumSourceBlocks() <= numSrcBs && numSrcBs <= maxNumSourceBlocks();
    }

    private static void _checkNumSourceBlocksOutOfBounds(int Z) {

        if (isNumSourceBlocksOutOfBounds(Z)) {
            throw new IllegalArgumentException("invalid number of source blocks");
        }
    }

    /**
     * Returns the minimum allowed number of source blocks given a source data length and symbol size.
     * <p>
     * <em>Note that besides being within their own bounds, both arguments must also be valid together, that is, either
     * the source data length must be {@linkplain #maxAllowedDataLength(int) upper bounded given the symbol size}, or
     * the symbol size must be {@linkplain #minAllowedSymbolSize(long) lower bounded given the source data length}.</em>
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param symbSize
     *            A symbol size, in number of bytes
     * @return the minimum allowed number of source blocks given a source data length and symbol size
     * @exception IllegalArgumentException
     *                If either argument is individually out of bounds, or if they are out of bounds in unison
     */
    public static int minAllowedNumSourceBlocks(long dataLen, int symbSize) {

        _checkDataLengthOutOfBounds(dataLen);
        _checkSymbolSizeOutOfBounds(symbSize);
        _checkDataLengthAndSymbolSizeOutOfBounds(dataLen, symbSize);

        final int Kt = _totalSymbols(dataLen, symbSize);
        return _minAllowedNumSourceBlocks(Kt);
    }

    /**
     * Returns the maximum allowed number of source blocks given a source data length and symbol size.
     * <p>
     * <em>Note that besides being within their own bounds, both arguments must also be valid together, that is, either
     * the source data length must be {@linkplain #maxAllowedDataLength(int) upper bounded given the symbol size}, or
     * the symbol size must be {@linkplain #minAllowedSymbolSize(long) lower bounded given the source data length}.</em>
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param symbSize
     *            A symbol size, in number of bytes
     * @return the maximum allowed number of source blocks given a source data length and symbol size
     * @exception IllegalArgumentException
     *                If either argument is individually out of bounds, or if they are out of bounds in unison
     */
    public static int maxAllowedNumSourceBlocks(long dataLen, int symbSize) {

        _checkDataLengthOutOfBounds(dataLen);
        _checkSymbolSizeOutOfBounds(symbSize);
        _checkDataLengthAndSymbolSizeOutOfBounds(dataLen, symbSize);

        final int Kt = _totalSymbols(dataLen, symbSize);
        return _maxAllowedNumSourceBlocks(Kt);
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
     * Returns {@code false} iff {@linkplain #minInterleaverLength() minInterLen} <= <b>interLen</b> <=
     * {@linkplain #maxInterleaverLength() maxInterLen}.
     * 
     * @param interLen
     *            An interleaver length, in number of sub-blocks per source block
     * @return {@code false} iff {@linkplain #minInterleaverLength() minInterLen} <= <b>interLen</b> <=
     *         {@linkplain #maxInterleaverLength() maxInterLen}
     */
    public static boolean isInterleaverLengthOutOfBounds(int interLen) {

        return minInterleaverLength() <= interLen && interLen <= maxInterleaverLength();
    }

    /**
     * Returns the maximum allowed interleaver length given a symbol size.
     * 
     * @param symbSize
     *            A symbol size, in number of bytes
     * @return the maximum allowed interleaver length given a symbol size
     * @exception IllegalArgumentException
     *                If the symbol size is {@linkplain #isSymbolSizeOutOfBounds(int) out of bounds}
     */
    public static int maxAllowedInterleaverLength(int symbSize) {

        _checkSymbolSizeOutOfBounds(symbSize);
        return _maxAllowedInterleaverLength(symbSize);
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

        return Al;
    }

    // =========== F, T, Z, N =========== //

    // requires bounded argument
    private static long _maxAllowedDataLength(int T) {

        return Math.min(F_max, (long)T * Kt_max);
    }

    // requires bounded argument
    private static int _minAllowedSymbolSize(long F) {

        return Math.max(T_min, (int)(ceilDiv(F, Kt_max))); // downcast never overflows since dataLen is upper bounded
    }

    // requires bounded argument
    private static int _minAllowedNumSourceBlocks(int Kt) {

        return Math.max(Z_min, ceilDiv(Kt, K_max));
    }

    // requires bounded argument
    private static int _maxAllowedNumSourceBlocks(int Kt) {

        return Math.min(Z_max, Kt);
    }

    // requires individually bounded arguments
    private static int _maxAllowedInterleaverLength(int T) {

        return Math.min(N_max, T / Al);
    }

    // requires individually bounded arguments
    private static boolean _areDataLengthAndSymbolSizeOutOfBounds(long F, int T) {

        return _possibleTotalSymbols(F, T) > Kt_max;
    }

    private static void _checkDataLengthAndSymbolSizeOutOfBounds(long F, int T) {

        if (_areDataLengthAndSymbolSizeOutOfBounds(F, T)) {
            throw new IllegalArgumentException("source data length and symbol size are invalid in unison");
        }
    }

    /**
     * Returns {@code true} if, and only if, the provided FEC parameters are valid, that is, if they fall within certain
     * bounds.
     * <p>
     * This method shouldn't be called directly. It is mainly used to test the validity of parameters when creating
     * {@link FECParameters} instances. If this method returns {@code false} then an exception is thrown by the creator
     * method.
     * <p>
     * It is possible, a priori, to obtain lower and upper bounds for valid parameter values. If the parameters fall
     * within these bounds, then this method always returns {@code true}. For information on how to obtain these bounds,
     * refer to the <a href="#fec-parameter-bounds"> section on FEC parameter bounds</a> in the class header.
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
     * Tests if the FEC parameters are valid according to {@link #areValidFECParameters(long, int, int, int)}, and if so
     * the method returns an empty string, otherwise it returns an error string indicating which parameters are invalid.
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

        // domain restrictions
        if (isDataLengthOutOfBounds(F)) {
            return String.format(
                "by default, the data length (%d) must be within [%d, %d] bytes",
                F, F_min, F_max);
        }
        if (isSymbolSizeOutOfBounds(T)) {
            return String.format(
                "by default, the symbol size (%d) must be within [%d, %d] bytes",
                T, T_min, T_max);
        }
        if (isNumSourceBlocksOutOfBounds(Z)) {
            return String.format(
                "by default, the number of source blocks (%d) must be within [%d, %d]",
                Z, Z_min, Z_max);
        }
        if (isInterleaverLengthOutOfBounds(N)) {
            return String.format(
                "by default, the interleaver length (%d) must be within [%d, %d]",
                N, N_min, N_max);
        }

        // T must be a multiple of Al
        if (T % Al != 0) {
            return String.format(
                "the symbol size (%d) must be a multiple of the symbol alignment value %d",
                T, Al);
        }
        // the number of symbols cannot exceed Kt_max
        if (_areDataLengthAndSymbolSizeOutOfBounds(F, T)) {
            return String.format(
                "a data length of %d bytes requires a symbol size of at least %d bytes, or" +
                    "a symbol size of %d bytes requires a data length of at most %d bytes",
                F, _minAllowedSymbolSize(F), T, _maxAllowedDataLength(T));
        }

        // number of symbols
        final int Kt = _totalSymbols(F, T);

        final int minAllowedZ = _minAllowedNumSourceBlocks(Kt);
        final int maxAllowedZ = _maxAllowedNumSourceBlocks(Kt);

        // at least one symbol, and at most K_max symbols in each source block
        if (Z < minAllowedZ || Z > maxAllowedZ) {
            return String.format(
                "a data length of %d bytes and a symbol size of %d bytes require a number of source blocks (%d) within [%d, %d]",
                F, T, Z, minAllowedZ, maxAllowedZ);
        }

        final int maxAllowedN = _maxAllowedInterleaverLength(T);

        // interleaver length must be bounded as well
        if (N > maxAllowedN) {
            return String.format(
                "a symbol size of %d bytes requires an interleaver length (%d) at most %d",
                T, N, maxAllowedN);
        }

        // empty string means parameters are all valid
        return "";
    }

    // =========== F, P', WS =========== //

    /**
     * TODO document
     * 
     * @param dataLen
     * @param maxPaLen
     * @param maxDBMem
     * @return
     */
    public static boolean areValidDerivingParameters(long dataLen, int maxPaLen, int maxDBMem) {

        return getDerivingParamsErrorString(dataLen, maxPaLen, maxDBMem).isEmpty();
    }

    /**
     * Tests if the deriving parameters are valid according to {@link #areValidDerivingParameters(long, int, int)}, and
     * if so the method returns an empty string, otherwise it returns an error string indicating which parameters are
     * invalid.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param maxPaLen
     *            A maximum length, in number of bytes, for a payload containing one encoding symbol
     * @param maxDBMem
     *            A maximum size, in number of bytes, of a block decodable in working memory
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     */
    public static String getDerivingParamsErrorString(long dataLen, int maxPaLen, int maxDBMem) {

        final long F = dataLen;
        final int P = maxPaLen;
        final int WS = maxDBMem;

        // domain restrictions
        if (isDataLengthOutOfBounds(F)) {
            return String.format(
                "by default, the data length (%d) must be within [%d, %d] bytes",
                F, F_min, F_max);
        }
        if (P < 1) return "by default, the max payload length must be positive";
        if (WS < 1) return "by default, the max decoding block size must be positive";

        // minimum P is minimum allowed T
        final int minP = _minAllowedSymbolSize(F);
        if (P < minP) {
            return String.format(
                "a data length of %d bytes requires a max payload length (%d) of at least %d bytes",
                F, P, minP);
        }

        // T is at most P but cannot exceed T_max
        final int T = Math.min(T_max, (P / Al) * Al); // round down to the nearest multiple of Al

        // number of symbols
        final int Kt = _totalSymbols(F, T);

        // the theoretical maximum number of source symbols in a source block
        final int maxK = ceilDiv(Kt, Z_max);

        // minimum WS is the inverse of the function KL
        final int minWS = minWS(maxK, N_max, Al, T);
        if (WS < minWS) {
            return String.format(
                "a data length of %d bytes and a symbol size of %d bytes require a max decoding block size (%d) of at least %d bytes",
                F, T, WS, minWS);
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
     * Tests if the FEC Payload ID parameters are valid according to {@link #isValidFECPayloadID(int, int, int)}, and if
     * so the method returns an empty string, otherwise it returns an error string indicating which parameters are
     * invalid.
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

        _checkNumSourceBlocksOutOfBounds(numSrcBs);

        if (sbn < SBN_min || sbn > numSrcBs) {
            return String.format("source block number (%d) must be whithin [%d, %d]", sbn, SBN_min, numSrcBs - 1);
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

    // =========== total number of source symbols - Kt =========== //

    // requires individually bounded arguments
    private static long _possibleTotalSymbols(long F, int T) {

        return ceilDiv(F, T);
    }

    // requires individually and in unison bounded arguments
    static int _totalSymbols(long F, int T) {

        return (int)ceilDiv(F, T); // downcast never overflows since F and T are bounded
    }

    private ParameterChecker() {

        // not instantiable
    }
}
