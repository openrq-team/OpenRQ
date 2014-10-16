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

package net.fec.openrq.parameters;


import static net.fec.openrq.parameters.InternalConstants.Al;
import static net.fec.openrq.parameters.InternalConstants.ESI_max;
import static net.fec.openrq.parameters.InternalConstants.ESI_min;
import static net.fec.openrq.parameters.InternalConstants.F_max;
import static net.fec.openrq.parameters.InternalConstants.F_min;
import static net.fec.openrq.parameters.InternalConstants.K_max;
import static net.fec.openrq.parameters.InternalConstants.K_min;
import static net.fec.openrq.parameters.InternalConstants.K_prime_min;
import static net.fec.openrq.parameters.InternalConstants.Kt_max;
import static net.fec.openrq.parameters.InternalConstants.N_max;
import static net.fec.openrq.parameters.InternalConstants.N_min;
import static net.fec.openrq.parameters.InternalConstants.SBN_max;
import static net.fec.openrq.parameters.InternalConstants.SBN_min;
import static net.fec.openrq.parameters.InternalConstants.T_max;
import static net.fec.openrq.parameters.InternalConstants.T_min;
import static net.fec.openrq.parameters.InternalConstants.Z_max;
import static net.fec.openrq.parameters.InternalConstants.Z_min;
import static net.fec.openrq.parameters.InternalFunctions.getPossibleTotalSymbols;
import static net.fec.openrq.parameters.InternalFunctions.getTotalSymbols;
import static net.fec.openrq.parameters.InternalFunctions.minWS;
import static net.fec.openrq.parameters.InternalFunctions.topInterleaverLength;
import static net.fec.openrq.util.math.ExtraMath.ceilDiv;


/**
 * This class provides methods for checking bounds and validating FEC and encoding packet parameters.
 * <p>
 * <a name="fec-parameters-bounds">
 * <h5>FEC parameters bounds</h5></a>
 * <p>
 * FEC parameters have well defined bounds, and any parameter that is outside its bounds is considered invalid. It is
 * important to know these bounds in order to be able to create instances of the class {@link FECParameters}, which
 * contain only valid values.
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
 * <li>{@link #minAllowedSymbolSize(long)} <li>{@link #maxAllowedDataLength(int)} </ul>
 * <p>
 * Given a <b>source data length</b> and a <b>symbol size</b>, which are valid in respect to each other, it is possible
 * to obtain a <b>minimum and a maximum allowed number of source blocks</b>. Refer to methods for each:
 * <ul>
 * <li>{@link #minAllowedNumSourceBlocks(long, int)} <li>{@link #maxAllowedNumSourceBlocks(long, int)} </ul>
 * <p>
 * Given a <b>symbol size</b>, it is possible to obtain a <b>maximum allowed interleaver length</b>. Refer to the
 * method:
 * <ul>
 * <li>{@link #maxAllowedInterleaverLength(int)} </ul>
 * <p>
 * <a name="deriver-parameters-bounds">
 * <h5>Deriver parameters bounds</h5></a>
 * <p>
 * Parameters that derive FEC parameters, or "deriver parameters", include a <em>source data length</em>, a
 * <em>payload length</em>, and a <em>maximum size for a block decodable in working memory</em>. The payload length
 * parameter is equivalent to the "symbol size" FEC parameter.
 * <p>
 * Deriver parameters have well defined bounds, much like the FEC parameters. The source data length has the same bounds
 * as before, as well as the payload length since it is equivalent to the symbol size.
 * <p>
 * By default, the maximum decoding block size has a lower bound defined in method {@link #minDecodingBlockSize()}, but
 * has no upper bound.
 * <p>
 * There are also conditional bounds, much like in the FEC parameters. Methods are provided for obtaining these bounds.
 * They are summarized below for ease of access.
 * <p>
 * Given a <b>source data length</b> it is possible to obtain the <b>minimum allowed payload length</b>; conversely,
 * given a <b>payload length</b> it is possible to obtain the <b>maximum allowed source data length</b>. Refer to
 * methods for each:
 * <p>
 * <ul>
 * <li>{@link #minAllowedPayloadLength(long)} <li>{@link #maxAllowedDataLength(int)} </ul>
 * <p>
 * Given a <b>source data length</b> and a <b>payload length</b>, which are valid in respect to each other, it is
 * possible to obtain a <b>lower bound for the maximum decoding block size</b>. Refer to the method:
 * <ul>
 * <li>{@link #minAllowedDecodingBlockSize(long, int)} </ul>
 * <p>
 * Given a <b>payload length</b>, within bounds, and a <b>maximum decoding block size</b>, within bounds and no less
 * than the payload length, it is possible to obtain a <b>maximum allowed source data length</b>. Refer to the method:
 * <ul>
 * <li>{@link #maxAllowedDataLength(int, long)} </ul>
 */
public final class ParameterChecker {

    // =========== F -> "source data length, in octets" ========== //

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
     * Returns {@code false} iff {@linkplain #minDataLength() minDataLen} &le; {@code dataLen} &le;
     * {@linkplain #maxDataLength() maxDataLen}.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @return {@code false} iff {@linkplain #minDataLength() minDataLen} &le; {@code dataLen} &le;
     *         {@linkplain #maxDataLength() maxDataLen}
     */
    public static boolean isDataLengthOutOfBounds(long dataLen) {

        return !(minDataLength() <= dataLen && dataLen <= maxDataLength());
    }

    /**
     * Returns the maximum allowed data length given a symbol size.
     * <p>
     * The provided parameter may also be a payload length, since it is equivalent to the symbol size, therefore the
     * same bounds apply.
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
     * Returns {@code false} iff {@linkplain #minSymbolSize() minSymbSize} &le; {@code symbSize} &le;
     * {@linkplain #maxSymbolSize() maxSymbSize}.
     * 
     * @param symbSize
     *            A symbol size, in number of bytes
     * @return {@code false} iff {@linkplain #minSymbolSize() minSymbSize} &le; {@code symbSize} &le;
     *         {@linkplain #maxSymbolSize() maxSymbSize}
     */
    public static boolean isSymbolSizeOutOfBounds(int symbSize) {

        return !(minSymbolSize() <= symbSize && symbSize <= maxSymbolSize());
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
    public static int minAllowedSymbolSize(long dataLen) {

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
     * Returns {@code false} iff {@linkplain #minNumSourceBlocks() minSrcBs} &le; {@code numSrcBs} &le;
     * {@linkplain #maxNumSourceBlocks() maxSrcBs}.
     * 
     * @param numSrcBs
     *            A number of source blocks into which a source data is divided
     * @return {@code false} iff {@linkplain #minNumSourceBlocks() minSrcBs} &le; {@code numSrcBs} &le;
     *         {@linkplain #maxNumSourceBlocks() maxSrcBs}
     */
    public static boolean isNumSourceBlocksOutOfBounds(int numSrcBs) {

        return !(minNumSourceBlocks() <= numSrcBs && numSrcBs <= maxNumSourceBlocks());
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

        final int Kt = getTotalSymbols(dataLen, symbSize);
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

        final int Kt = getTotalSymbols(dataLen, symbSize);
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
     * Returns {@code false} iff {@linkplain #minInterleaverLength() minInterLen} &le; {@code interLen} &le;
     * {@linkplain #maxInterleaverLength() maxInterLen}.
     * 
     * @param interLen
     *            An interleaver length, in number of sub-blocks per source block
     * @return {@code false} iff {@linkplain #minInterleaverLength() minInterLen} &le; {@code interLen} &le;
     *         {@linkplain #maxInterleaverLength() maxInterLen}
     */
    public static boolean isInterleaverLengthOutOfBounds(int interLen) {

        return !(minInterleaverLength() <= interLen && interLen <= maxInterleaverLength());
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
     * refer to the section on <a href="#fec-parameters-bounds"><em>FEC parameters bounds</em></a> in the class header.
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
        // if (T % Al != 0) {
        // return String.format(
        // "the symbol size (%d) must be a multiple of the symbol alignment value %d",
        // T, Al);
        // }

        // the number of symbols cannot exceed Kt_max
        if (_areDataLengthAndSymbolSizeOutOfBounds(F, T)) {
            return String.format(
                "%d byte(s) of data length only support symbol size values of at least %d byte(s); " +
                    "alternatively, %d bytes(s) of symbol size only support data length values of at most %d byte(s)",
                F, _minAllowedSymbolSize(F),
                T, _maxAllowedDataLength(T));
        }

        // number of symbols
        final int Kt = getTotalSymbols(F, T);

        final int minAllowedZ = _minAllowedNumSourceBlocks(Kt);
        final int maxAllowedZ = _maxAllowedNumSourceBlocks(Kt);

        // at least one symbol, and at most K_max symbols in each source block
        if (Z < minAllowedZ || Z > maxAllowedZ) {
            return String.format(
                "%d byte(s) of data length and %d byte(s) of symbol size only support " +
                    "a number of source blocks (%d) within [%d, %d]",
                F, T,
                Z, minAllowedZ, maxAllowedZ);
        }

        final int maxAllowedN = _maxAllowedInterleaverLength(T);

        // interleaver length must be bounded as well
        if (N > maxAllowedN) {
            return String.format(
                "%d byte(s) of symbol size only supports an interleaver length (%d) of at most %d",
                T, N, maxAllowedN);
        }

        // empty string means parameters are all valid
        return "";
    }

    // =========== F, P', WS =========== //

    /**
     * Returns the minimum payload length, in number of bytes. This value is equivalent to the
     * {@linkplain #minSymbolSize() minimum symbol size}.
     * 
     * @return the minimum payload length, in number of bytes
     */
    public static int minPayloadLength() {

        return minSymbolSize();
    }

    /**
     * Returns the maximum payload length, in number of bytes. This value is equivalent to the
     * {@linkplain #maxSymbolSize() maximum symbol size}.
     * 
     * @return the maximum payload length, in number of bytes
     */
    public static int maxPayloadLength() {

        return maxSymbolSize();
    }

    /**
     * Returns {@code false} iff {@linkplain #minPayloadLength() minPayLen} &le; {@code payLen} &le;
     * {@linkplain #maxPayloadLength() maxPayLen}.
     * 
     * @param payLen
     *            A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @return {@code false} iff {@linkplain #minPayloadLength() minPayLen} &le; {@code payLen} &le;
     *         {@linkplain #maxPayloadLength() maxPayLen}
     */
    public static boolean isPayloadLengthOutOfBounds(int payLen) {

        return !(minPayloadLength() <= payLen && payLen <= maxPayloadLength());
    }

    /**
     * Returns the minimum allowed payload length given a source data length. This method is equivalent to method
     * {@link #minAllowedSymbolSize(long)}.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @return the minimum allowed payload length given a source data length.
     * @exception IllegalArgumentException
     *                If the source data length is {@linkplain #isDataLengthOutOfBounds(long) out of bounds}
     */
    public static int minAllowedPayloadLength(long dataLen) {

        return minAllowedSymbolSize(dataLen);
    }

    /**
     * Returns the lowest possible bound on the maximum size for a block decodable in working memory.
     * 
     * @return the lowest possible bound on the maximum size for a block decodable in working memory
     */
    public static long minDecodingBlockSize() {

        return _minAllowedDecodingBlockSize(minDataLength(), minSymbolSize());
    }

    /**
     * Returns a lower bound on the maximum size for a block decodable in working memory, given a source data length and
     * payload length.
     * <p>
     * <em>Note that besides being within their own bounds, both arguments must also be valid together, that is, either
     * the source data length must be {@linkplain #maxAllowedDataLength(int) upper bounded given the payload length}, or
     * the payload length must be {@linkplain #minAllowedPayloadLength(long) lower bounded given the source data length}
     * .</em>
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param payLen
     *            A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @return a lower bound on the maximum size for a block decodable in working memory
     * @exception IllegalArgumentException
     *                If either argument is individually out of bounds, or if they are out of bounds in unison
     */
    public static long minAllowedDecodingBlockSize(long dataLen, int payLen) {

        _checkDataLengthOutOfBounds(dataLen);
        _checkPayloadLengthOutOfBounds(payLen);
        _checkDataLengthAndPayloadLengthOutOfBounds(dataLen, payLen);

        return _minAllowedDecodingBlockSize(dataLen, payLen);
    }

    /**
     * Returns the maximum allowed data length given a payload length and a maximum size for a block decodable in
     * working memory.
     * <p>
     * <b><em>Bounds checking</em></b> - The following must be true, otherwise an {@code IllegalArgumentException} is
     * thrown:
     * <ul>
     * <li>{@link #isPayloadLengthOutOfBounds(int) isPayloadLengthOutOfBounds(payLen)} {@code == false} <li>
     * {@code maxDBMem >=} {@link #minDecodingBlockSize()} <li>{@code maxDBMem >= payLen} </ul>
     * 
     * @param payLen
     *            A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @param maxDBMem
     *            A maximum size, in number of bytes, for a block decodable in working memory
     * @return the maximum allowed data length given a payload length and a maximum size for a block decodable in
     *         working memory
     * @exception IllegalArgumentException
     *                If any argument is out of bounds (see method description)
     */
    public static long maxAllowedDataLength(int payLen, long maxDBMem) {

        _checkPayloadLengthOutOfBounds(payLen);
        _checkDecodingBlockSizeOutOfBounds(maxDBMem);
        if (maxDBMem < payLen) {
            throw new IllegalArgumentException(
                "maximum decoding block size must be at least equal to the payload length");
        }

        return _maxAllowedDataLength(payLen, maxDBMem);
    }

    /**
     * Returns {@code true} if, and only if, the provided deriver parameters are valid, that is, if they fall within
     * certain bounds.
     * <p>
     * This method shouldn't be called directly. It is mainly used to test the validity of parameters when deriving
     * {@link FECParameters} instances. If this method returns {@code false} then an exception is thrown by the deriver
     * method.
     * <p>
     * It is possible, a priori, to obtain lower and upper bounds for valid parameter values. If the parameters fall
     * within these bounds, then this method always returns {@code true}. For information on how to obtain these bounds,
     * refer to the section on <a href="#deriver-parameters-bounds"><em>Deriver parameters bounds</em></a> in the class
     * header.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param payLen
     *            A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @param maxDBMem
     *            A maximum size, in number of bytes, for a block decodable in working memory
     * @return {@code true} if, and only if, the provided deriver parameters are within certain bounds
     */
    public static boolean areValidDeriverParameters(long dataLen, int payLen, long maxDBMem) {

        return getDeriverParamsErrorString(dataLen, payLen, maxDBMem).isEmpty();
    }

    /**
     * Tests if the deriver parameters are valid according to {@link #areValidDeriverParameters(long, int, long)}, and
     * if so the method returns an empty string, otherwise it returns an error string indicating which parameters are
     * invalid.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param payLen
     *            A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @param maxDBMem
     *            A maximum size, in number of bytes, for a block decodable in working memory
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     */
    public static String getDeriverParamsErrorString(long dataLen, int payLen, long maxDBMem) {

        final long F = dataLen;
        final int T = payLen;
        final long WS = maxDBMem;

        // domain restrictions
        if (isDataLengthOutOfBounds(F)) {
            return String.format(
                "by default, the data length (%d) must be within [%d, %d] bytes",
                F, F_min, F_max);
        }
        if (isSymbolSizeOutOfBounds(T)) {
            return String.format(
                "by default, the payload length (%d) must be within [%d, %d] bytes",
                T, T_min, T_max);
        }

        // T must be a multiple of Al
        // if (T % Al != 0) {
        // return String.format(
        // "the symbol size (%d) must be a multiple of the symbol alignment value %d",
        // T, Al);
        // }

        final long absolMinWS = minDecodingBlockSize();
        if (WS < absolMinWS) {
            return String.format("by default, the max decoding block size (%d) must be at least %d byte(s)",
                WS, absolMinWS);
        }

        final int minT = _minAllowedSymbolSize(F);
        if (T < minT) {
            return String.format(
                "%d byte(s) of data length only supports a payload length (%d) of at least %d byte(s)",
                F, T, minT);
        }

        final long minWS = _minAllowedDecodingBlockSize(F, T);
        if (WS < minWS) {
            return String.format(
                "%d byte(s) of data length and %d byte(s) of symbol size only support " +
                    "a max decoding block size (%d) of at least %d byte(s)",
                F, T,
                WS, minWS);
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
     * Returns {@code false} iff {@linkplain #minSourceBlockNumber() minSBN} &le; {@code sbn} &le;
     * {@linkplain #maxSourceBlockNumber() maxSBN}.
     * 
     * @param sbn
     *            A source block number
     * @return {@code false} iff {@linkplain #minSourceBlockNumber() minSBN} &le; {@code sbn} &le;
     *         {@linkplain #maxSourceBlockNumber() maxSBN}
     */
    public static boolean isSourceBlockNumberOutOfBounds(int sbn) {

        return !(minSourceBlockNumber() <= sbn && sbn <= maxSourceBlockNumber());
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
     * Returns {@code false} iff {@linkplain #minEncodingSymbolID() minESI} &le; {@code esi} &le;
     * {@linkplain #maxEncodingSymbolID() maxESI}.
     * 
     * @param esi
     *            An encoding symbol ID
     * @return {@code false} iff {@linkplain #minEncodingSymbolID() minESI} &le; {@code esi} &le;
     *         {@linkplain #maxEncodingSymbolID() maxESI}
     */
    public static boolean isEncodingSymbolIDOutOfBounds(int esi) {

        return !(minEncodingSymbolID() <= esi && esi <= maxEncodingSymbolID());
    }

    // =========== SBN, ESI =========== //

    /**
     * Returns {@code true} if, and only if, the FEC payload ID parameters are valid, that is, if they fall within
     * certain bounds.
     * <p>
     * The source block number (SBN) must be greater than or equal to its minimum value, given by the method
     * {@link #minSourceBlockNumber()}. The SBN must also be less than the number of source blocks.
     * <p>
     * The encoding symbol identifier (ESI) must not be out of bounds, meaning that the following method should return
     * {@code false} when passing the ESI as parameter: {@link #isEncodingSymbolIDOutOfBounds(int)}.
     * 
     * @param sbn
     *            A source block number
     * @param esi
     *            The encoding symbol identifier of the first symbol in an encoding packet
     * @param numSrcBs
     *            A number of source blocks into which a source data is divided
     * @return {@code true} if, and only if, the provided FEC payload ID parameters are within certain bounds
     * @exception IllegalArgumentException
     *                If the number of source blocks is {@linkplain #isNumSourceBlocksOutOfBounds(int) out of bounds}
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
     * @param numSBs
     *            A number of source blocks into which a source data is divided
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     * @exception IllegalArgumentException
     *                If the number of source blocks is {@linkplain #isNumSourceBlocksOutOfBounds(int) out of bounds}
     */
    public static String getFECPayloadIDErrorString(int sbn, int esi, int numSBs) {

        _checkNumSourceBlocksOutOfBounds(numSBs);

        if (sbn < SBN_min || sbn >= numSBs) {
            return String.format("source block number (%d) must be within [%d, %d]", sbn, SBN_min, numSBs - 1);
        }
        if (isEncodingSymbolIDOutOfBounds(esi)) {
            return String.format("encoding symbol identifier (%d) must be within [%d, %d]", esi, ESI_min, ESI_max);
        }
        return "";
    }

    // =========== number of source symbols - K =========== //

    /**
     * Returns the minimum number of source symbols in a source block (1).
     * 
     * @return the minimum number of source symbols in a source block
     */
    public static int minNumSourceSymbolsPerBlock() {

        return K_min;
    }

    /**
     * Returns the maximum number of source symbols in a source block (56_403).
     * 
     * @return the maximum number of source symbols in a source block
     */
    public static int maxNumSourceSymbolsPerBlock() {

        return K_max;
    }

    /**
     * Returns {@code false} iff {@linkplain #minNumSourceSymbolsPerBlock() minNumSrcSymbs} &le; {@code numSrcSymbs}
     * &le; {@linkplain #maxNumSourceSymbolsPerBlock() maxNumSrcSymbs}.
     * 
     * @param numSrcSymbs
     *            The number of source symbols in a source block
     * @return {@code false} iff {@linkplain #minNumSourceSymbolsPerBlock() minNumSrcSymbs} &le; {@code numSrcSymbs}
     *         &le; {@linkplain #maxNumSourceSymbolsPerBlock() maxNumSrcSymbs}
     */
    public static boolean isNumSourceSymbolsPerBlockOutOfBounds(int numSrcSymbs) {

        return !(minNumSourceSymbolsPerBlock() <= numSrcSymbs && numSrcSymbs <= maxNumSourceSymbolsPerBlock());
    }

    /**
     * Returns the total number of possible repair symbols in a source block, given the number of source symbols in the
     * block, starting at the initial repair symbol identifier.
     * 
     * @param numSrcSymbs
     *            The number of source symbols in a source block
     * @return the total number of possible repair symbols in a source block, given the number of source symbols in the
     *         block, starting at the initial repair symbol identifier
     * @exception IllegalArgumentException
     *                If the number of source symbols is {@linkplain #isNumSourceSymbolsPerBlockOutOfBounds(int) out of
     *                bounds}
     */
    public static int numRepairSymbolsPerBlock(int numSrcSymbs) {

        final int firstESI = numSrcSymbs; // initial repair symbol ESI
        return numRepairSymbolsPerBlock(numSrcSymbs, firstESI);
    }

    /**
     * Returns the total number of possible repair symbols in a source block, given the number of source symbols in the
     * block and the encoding symbol identifier of the first repair symbol.
     * 
     * @param numSrcSymbs
     *            The number of source symbols in a source block
     * @param firstESI
     *            The first repair symbol identifier
     * @return the total number of possible repair symbols in a source block, given the number of source symbols in the
     *         block, starting at a specified repair symbol identifier
     * @exception IllegalArgumentException
     *                If the number of source symbols is {@linkplain #isNumSourceSymbolsPerBlockOutOfBounds(int) out of
     *                bounds}, or if the first repair symbol identifier is
     *                {@linkplain #isEncodingSymbolIDOutOfBounds(int) out of bounds} or is less than the number of
     *                source symbols
     */
    public static int numRepairSymbolsPerBlock(int numSrcSymbs, int firstESI) {

        _checkNumSourceSymbolsPerBlockOutOfBounds(numSrcSymbs);

        final int maxESI = maxEncodingSymbolID();

        if (firstESI < numSrcSymbs || firstESI > maxESI) {
            throw new IllegalArgumentException(
                String.format("first repair symbol identifier must be within [%d, %d]",
                    numSrcSymbs, maxESI));
        }

        final int totalSymbs = 1 + maxESI - firstESI;

        return totalSymbs - numSrcSymbs;
    }

    // =========== private helper methods =========== //

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

    // requires individually and in unison bounded arguments
    private static long _minAllowedDecodingBlockSize(long F, int T) {

        // total number of symbols
        final int Kt = getTotalSymbols(F, T);

        // the theoretical minimum number of source symbols in an extended source block
        final int Kprime = Math.max(K_prime_min, ceilDiv(Kt, Z_max));

        // minimum WS is the inverse of the function KL
        return minWS(Kprime, T, Al, topInterleaverLength(T));
    }

    // requires individually and in unison bounded arguments
    private static long _maxAllowedDataLength(int T, long WS) {

        final long boundFromT = _maxAllowedDataLength(T);

        // Kt = ceil(F / T)
        // Z = ceil(Kt / KL)
        // ceil(Kt / KL) <= Z_max
        // Kt / KL <= Z_max
        // Kt <= Z_max * KL
        // ceil(F / T) <= Z_max * KL
        // F / T <= Z_max * KL
        // F <= Z_max * KL * T

        final int KL = InternalFunctions.KL(WS, T, Al, topInterleaverLength(T));
        final long boundFromWS = (long)Z_max * KL * T;

        return Math.min(boundFromT, boundFromWS);
    }

    // requires individually bounded arguments
    private static boolean _areDataLengthAndSymbolSizeOutOfBounds(long F, int T) {

        return getPossibleTotalSymbols(F, T) > Kt_max;
    }

    // requires individually bounded arguments
    private static boolean _areDataLengthAndPayloadLengthOutOfBounds(long F, int P) {

        return _areDataLengthAndSymbolSizeOutOfBounds(F, P);
    }

    private static void _checkDataLengthOutOfBounds(long F) {

        if (isDataLengthOutOfBounds(F)) {
            throw new IllegalArgumentException("source data length is out of bounds");
        }
    }

    private static void _checkSymbolSizeOutOfBounds(int T) {

        if (isSymbolSizeOutOfBounds(T)) {
            throw new IllegalArgumentException("symbol size is out of bounds");
        }
    }

    private static void _checkDataLengthAndSymbolSizeOutOfBounds(long F, int T) {

        if (_areDataLengthAndSymbolSizeOutOfBounds(F, T)) {
            throw new IllegalArgumentException("source data length and symbol size are out of bounds in unison");
        }
    }

    private static void _checkNumSourceBlocksOutOfBounds(int Z) {

        if (isNumSourceBlocksOutOfBounds(Z)) {
            throw new IllegalArgumentException("number of source blocks is out of bounds");
        }
    }

    private static void _checkPayloadLengthOutOfBounds(int P) {

        if (isPayloadLengthOutOfBounds(P)) {
            throw new IllegalArgumentException("payload length is out of bounds");
        }
    }

    private static void _checkDataLengthAndPayloadLengthOutOfBounds(long F, int P) {

        if (_areDataLengthAndPayloadLengthOutOfBounds(F, P)) {
            throw new IllegalArgumentException("source data length and payload length are out of bounds in unison");
        }
    }

    private static void _checkDecodingBlockSizeOutOfBounds(long WS) {

        if (WS < minDecodingBlockSize()) {
            throw new IllegalArgumentException("maximum decoding block size is out of bounds");
        }
    }

    private static void _checkNumSourceSymbolsPerBlockOutOfBounds(int K) {

        if (isNumSourceSymbolsPerBlockOutOfBounds(K)) {
            throw new IllegalArgumentException("number of source symbols per block is out of bounds");
        }
    }

    private ParameterChecker() {

        // not instantiable
    }
}
