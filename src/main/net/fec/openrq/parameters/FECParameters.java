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


import static net.fec.openrq.parameters.InternalFunctions.KL;
import static net.fec.openrq.parameters.InternalFunctions.getTotalSymbols;
import static net.fec.openrq.parameters.InternalFunctions.topInterleaverLength;
import static net.fec.openrq.util.math.ExtraMath.ceilDiv;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.Parsed;
import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.io.BufferOperation;
import net.fec.openrq.util.io.ExtraChannels;


/**
 * This class represents FEC parameters as defined in RFC 6330 as the <em>Encoded FEC Object Transmission Information
 * (OTI)</em>, which contains the <em>Encoded Common FEC OTI</em> and the <em>Encoded Scheme-Specific FEC OTI</em>.
 * <p>
 * The FEC parameters consist of:
 * <dl>
 * <dt><b>Source data length</b></dt>
 * <dd>The length of the source data, in number of bytes. Since encoded data may contain extra padding bytes, this value
 * allows a decoder to infer the real size of the decoded data.</dd>
 * <dt><b>Symbol size</b></dt>
 * <dd>The size of a symbol, in number of bytes. This value represents the size of an encoding symbol (source or
 * repair), except the size of the last source symbol, which may be smaller.</dd>
 * <dt><b>Number of source blocks</b></dt>
 * <dd>The number of source blocks into which the source data is partitioned. Each source block is encoded/decoded
 * independently, and not every one is divided into the same number of source symbols.</dd>
 * <dt><b>Interleaver length</b></dt>
 * <dd>The number of sub-blocks per source block into which the source data is interleaved. This value influences the
 * level of <em>uniform interleaving</em> used before encoding a source block. A value of 1 means no interleaving is
 * used, and a higher value means more interleaving per source block. Interleaving refers to a burst-error correction
 * technique in FEC codes.</dd>
 * <dt><b>Symbol alignment</b></dt>
 * <dd>This value has no relevance to the user of OpenRQ, since it is fixed to a value of 1. This parameter exists here
 * only for compliance to the RFC 6330.</dd>
 * </dl>
 * <p>
 * Methods are provided to write instances of this class to arrays of bytes, {@link ByteBuffer} objects, serializable
 * objects, {@link DataOutput} objects and {@link WritableByteChannel} objects. Additionally, static methods are
 * provided to parse/read instances of this class from arrays of bytes, {@code ByteBuffer} objects, serializable
 * objects, {@link DataInput} objects and {@link ReadableByteChannel} objects.
 */
public final class FECParameters {

    /**
     * Returns a new instance, given specific FEC parameters.
     * <p>
     * <b>Note:</b> <em>The symbol alignment parameter "Al" is internally obtained.</em>
     * <p>
     * The provided FEC parameters are validated by invoking {@link ParameterChecker#areValidFECParameters
     * ParameterChecker.areValidFECParameters(dataLen, symbSize, numSrcBs, interLen)}, and an
     * {@code IllegalArgumentException} is thrown if the parameters are invalid.
     * <p>
     * It is possible, a priori, to obtain lower and upper bounds for valid parameter values. If the parameters fall
     * within these bounds, then this method never throws an {@code IllegalArgumentException}. For information on how to
     * obtain these bounds, refer to the section on <a href="ParameterChecker.html#fec-parameters-bounds"><em>FEC
     * parameters bounds</em></a> in the {@link ParameterChecker} class header.
     * 
     * @param dataLen
     *            The length of the source data, in number of bytes
     * @param symbSize
     *            The size of a symbol, in number of bytes
     * @param numSrcBs
     *            The number of source blocks into which the source data is partitioned
     * @return a new {@code FECParameters} instance
     * @exception IllegalArgumentException
     *                If the provided FEC parameters are invalid
     */
    public static FECParameters newParameters(long dataLen, int symbSize, int numSrcBs) {

        return newParameters(dataLen, symbSize, numSrcBs, 1);
    }

    /**
     * Returns a new instance, given specific FEC parameters.
     * <p>
     * <b>Note:</b> <em>The symbol alignment parameter "Al" is internally obtained.</em>
     * <p>
     * The provided FEC parameters are validated by invoking {@link ParameterChecker#areValidFECParameters
     * ParameterChecker.areValidFECParameters(dataLen, symbSize, numSrcBs, interLen)}, and an
     * {@code IllegalArgumentException} is thrown if the parameters are invalid.
     * <p>
     * It is possible, a priori, to obtain lower and upper bounds for valid parameter values. If the parameters fall
     * within these bounds, then this method never throws an {@code IllegalArgumentException}. For information on how to
     * obtain these bounds, refer to the section on <a href="ParameterChecker.html#fec-parameters-bounds">
     * <em>FEC parameters bounds</em></a> in the {@link ParameterChecker} class header.
     * 
     * @param dataLen
     *            The length of the source data, in number of bytes
     * @param symbSize
     *            The size of a symbol, in number of bytes
     * @param numSrcBs
     *            The number of source blocks into which the source data is partitioned
     * @param interLen
     *            The interleaver length, in number of sub-blocks per source block
     * @return a new {@code FECParameters} instance
     * @exception IllegalArgumentException
     *                If the provided FEC parameters are invalid
     */
    public static FECParameters newParameters(long dataLen, int symbSize, int numSrcBs, int interLen) {

        final long F = dataLen;
        final int T = symbSize;
        final int Z = numSrcBs;
        final int N = interLen;
        final int Al = ParameterChecker.symbolAlignmentValue();

        if (ParameterChecker.areValidFECParameters(F, T, Z, N)) {
            return newLocalInstance(F, T, Z, N, Al);
        }
        else {
            throw new IllegalArgumentException(ParameterChecker.getFECParamsErrorString(F, T, Z, N));
        }
    }

    /**
     * Derives FEC parameters from specific deriver parameters.
     * <p>
     * <ul>
     * <li>A payload length is required. It is equivalent to the "symbol size" FEC parameter.
     * <li>A maximum size for a block that is decodable in working memory is required. It allows the decoder to work
     * with a limited amount of memory in an efficient way.
     * </ul>
     * <p>
     * <b>Note:</b> <em>The symbol alignment parameter "Al" is internally obtained.</em>
     * <p>
     * The provided parameters are validated by invoking {@link ParameterChecker#areValidDeriverParameters
     * ParameterChecker.areValidDeriverParameters(dataLen, payLen, maxDBMem)}, and an {@code IllegalArgumentException}
     * is thrown if the parameters are invalid.
     * <p>
     * It is possible, a priori, to obtain lower and upper bounds for valid parameter values. If the parameters fall
     * within these bounds, then this method never throws an {@code IllegalArgumentException}. For information on how to
     * obtain these bounds, refer to the section on <a href="ParameterChecker.html#deriver-parameters-bounds">
     * <em>Deriver parameters bounds</em></a> in the {@link ParameterChecker} class header.
     * 
     * @param dataLen
     *            A source data length, in number of bytes
     * @param payLen
     *            A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @param maxDBMem
     *            A maximum size, in number of bytes, for a block decodable in working memory
     * @return a derived {@code FECParameters} instance
     * @exception IllegalArgumentException
     *                If the provided deriver parameters are invalid
     */
    public static FECParameters deriveParameters(long dataLen, int payLen, long maxDBMem) {

        final long F = dataLen;
        final int P = payLen;
        final long WS = maxDBMem;
        final int Al = ParameterChecker.symbolAlignmentValue();

        if (ParameterChecker.areValidDeriverParameters(F, P, WS)) {
            final int T = P;

            // safe cast because F and T are appropriately bounded
            final int Kt = getTotalSymbols(F, T);     // Kt = ceil(F/T)
            final int topN = topInterleaverLength(T); // topN = floor(T/(SS*Al))

            final int Z = deriveZ(Kt, WS, T, Al, topN);
            final int N = deriveN(Kt, Z, WS, T, Al, topN);

            return newLocalInstance(F, T, Z, N, Al);
        }
        else {
            throw new IllegalArgumentException(ParameterChecker.getDeriverParamsErrorString(F, P, WS));
        }
    }

    private static int deriveZ(int Kt, long WS, int T, int Al, int topN) {

        // Z = ceil(Kt/KL(N_max))
        return ceilDiv(Kt, KL(WS, T, Al, topN));
    }

    private static int deriveN(int Kt, int Z, long WS, int T, int Al, int topN) {

        // N is the minimum n=1, ..., N_max such that ceil(Kt/Z) <= KL(n)
        final int topK = ceilDiv(Kt, Z);
        for (int n = topN; n >= 1; n--) {
            if (topK <= KL(WS, T, Al, n)) {
                return n;
            }
        }

        throw new RuntimeException("must never be thrown");
    }

    private static FECParameters newLocalInstance(long F, int T, int Z, int N, int Al) {

        final long commonFecOTI = ParameterIO.buildCommonFecOTI(F, T);
        final int schemeSpecFecOTI = ParameterIO.buildSchemeSpecFecOTI(Z, N, Al);
        return new FECParameters(commonFecOTI, schemeSpecFecOTI);
    }

    /**
     * Parses FEC parameters from the given serializable object.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param serParams
     *            A serializable object containing FEC parameters
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code serParams} is {@code null}
     */
    public static Parsed<FECParameters> parse(SerializableParameters serParams) {

        final long commonFecOTI = serParams.commonOTI();
        final int schemeSpecFecOTI = serParams.schemeSpecificOTI();

        return parseRemoteInstance(commonFecOTI, schemeSpecFecOTI);
    }

    /**
     * Parses FEC parameters from the given array. The parameters bytes in the array must follow the format specified by
     * {@link #asArray()}.
     * <p>
     * The FEC parameters will be read, in the array, from position {@code 0} inclusive to position {@code array.length}
     * exclusive.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param array
     *            An array of bytes containing FEC parameters
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @exception NullPointerException
     *                If the provided array is {@code null}
     */
    public static Parsed<FECParameters> parse(byte[] array) {

        return parse(array, 0);
    }

    /**
     * Parses FEC parameters from the given array. The parameters bytes in the array must follow the format specified by
     * {@link #asArray()}.
     * <p>
     * The FEC parameters will be read, in the array, from position {@code offset} inclusive to position
     * {@code array.length} exclusive.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param array
     *            An array of bytes containing FEC parameters
     * @param offset
     *            The starting index in the array (must be non-negative and less than {@code array.length})
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @exception IndexOutOfBoundsException
     *                If the pre-conditions on the array offset do not hold
     * @exception NullPointerException
     *                If the provided array is {@code null}
     */
    public static Parsed<FECParameters> parse(byte[] array, int offset) {

        if (offset < 0 || offset >= array.length) throw new IndexOutOfBoundsException();
        return parse(ByteBuffer.wrap(array, offset, array.length - offset));
    }

    /**
     * Parses FEC parameters from the given buffer. The parameters bytes in the buffer must follow the format specified
     * by {@link #asBuffer()}.
     * <p>
     * The FEC parameters will be read, in the buffer, from the current {@linkplain ByteBuffer#position() position}
     * inclusive to the current {@linkplain ByteBuffer#limit() limit} exclusive. If the parsing succeeds, the position
     * of the buffer will have been advanced by 12 bytes.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param buffer
     *            A buffer containing FEC parameters
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     */
    public static Parsed<FECParameters> parse(ByteBuffer buffer) {

        final int required = SizeOf.LONG + SizeOf.INT;
        if (buffer.remaining() < required) {
            return Parsed.invalid(String.format("missing FEC parameters, requires at least %d bytes", required));
        }
        else {
            final long commonFecOTI = buffer.getLong();   // 8 bytes
            final int schemeSpecFecOTI = buffer.getInt(); // 4 bytes

            return parseRemoteInstance(commonFecOTI, schemeSpecFecOTI);
        }
    }

    /**
     * Reads and parses FEC parameters from a {@code DataInput} object. The read parameters bytes must follow the format
     * specified by {@link #writeTo(java.io.DataOutput)}.
     * <p>
     * Examples of {@code DataInput} objects are {@link java.io.DataInputStream DataInputStream} and
     * {@link java.io.ObjectInputStream ObjectInputStream}.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are read from the input, or a
     * parsing failure is detected, or an {@code IOException} is throw.
     * 
     * @param in
     *            A {@code DataInput} object from which FEC parameters are read
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code DataInput} object
     * @exception NullPointerException
     *                If {@code in} is {@code null}
     */
    public static Parsed<FECParameters> readFrom(DataInput in) throws IOException {

        final long commonFecOTI = in.readLong();   // 8 bytes
        final int schemeSpecFecOTI = in.readInt(); // 4 bytes

        return parseRemoteInstance(commonFecOTI, schemeSpecFecOTI);
    }

    /**
     * Reads and parses FEC parameters from a {@code ReadableByteChannel} object. The read parameters bytes must follow
     * the format specified by {@link #writeTo(java.nio.channels.WritableByteChannel)}.
     * <p>
     * Examples of {@code ReadableByteChannel} objects are {@link java.nio.channels.SocketChannel SocketChannel} and
     * {@link java.nio.channels.FileChannel FileChannel}.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are read from the channel, or a
     * parsing failure is detected, or an {@code IOException} is throw.
     * 
     * @param ch
     *            A {@code ReadableByteChannel} object from which FEC parameters are read
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code ReadableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    public static Parsed<FECParameters> readFrom(ReadableByteChannel ch) throws IOException {

        final ByteBuffer buffer = ByteBuffer.allocate(SizeOf.LONG + SizeOf.INT);
        ExtraChannels.readBytes(ch, buffer, BufferOperation.FLIP_ABSOLUTELY);
        return parse(buffer);
    }

    private static Parsed<FECParameters> parseRemoteInstance(long commonFecOTI, int schemeSpecFecOTI) {

        final long F = ParameterIO.extractDataLength(commonFecOTI);
        final int T = ParameterIO.extractSymbolSize(commonFecOTI);
        final int Z = ParameterIO.extractNumSourceBlocks(schemeSpecFecOTI);
        final int N = ParameterIO.extractInterleaverLength(schemeSpecFecOTI);
        final int Al = ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);

        if (Al != ParameterChecker.symbolAlignmentValue()) {
            return Parsed.invalid(String.format("symbol alignment value must be equal to %d",
                ParameterChecker.symbolAlignmentValue()));
        }
        else if (!ParameterChecker.areValidFECParameters(F, T, Z, N)) {
            return Parsed.invalid(ParameterChecker.getFECParamsErrorString(F, T, Z, N));
        }
        else {
            // equality comparison and hashCode use the full value of the commonFecOTI,
            // so make sure the reserved region of the Common FEC OTI is all zeroes
            final long canonCommonFecOTI = ParameterIO.canonicalizeCommonFecOTI(commonFecOTI);
            return Parsed.of(new FECParameters(canonCommonFecOTI, schemeSpecFecOTI));
        }
    }


    private final long commonFecOTI;
    private final int schemeSpecFecOTI;


    private FECParameters(long commonFecOTI, int schemeSpecFecOTI) {

        this.commonFecOTI = commonFecOTI;
        this.schemeSpecFecOTI = schemeSpecFecOTI;
    }

    /**
     * Returns a serializable object with these FEC parameters. The serializable object will contain the
     * <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * 
     * @return a serializable object with these FEC parameters
     */
    public SerializableParameters asSerializable() {

        return new SerializableParameters(commonFecOTI, schemeSpecFecOTI);
    }

    /**
     * Returns an array with these FEC parameters. The array will contain the
     * <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * 
     * @return an array with these FEC parameters
     */
    public byte[] asArray() {

        final byte[] array = new byte[SizeOf.LONG + SizeOf.INT];
        writeTo(ByteBuffer.wrap(array));

        return array;
    }

    /**
     * Writes in the provided array starting at index zero these FEC parameters. The write consists of the
     * <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * <p>
     * The provided array must have a length of at least 12 bytes.
     * 
     * @param array
     *            An array on which the FEC parameters are written
     * @exception IndexOutOfBoundsException
     *                If the length of the array is insufficient to hold the FEC parameters
     * @exception NullPointerException
     *                If the provided array is {@code null}
     */
    public void writeTo(byte[] array) {

        writeTo(array, 0);
    }

    /**
     * Writes in the provided array starting in a specific index these FEC parameters. The write consists of the
     * <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * 
     * @param array
     *            An array on which the FEC parameters are written
     * @param offset
     *            The starting array index at which the FEC parameters are written (must be non-negative)
     * @exception IndexOutOfBoundsException
     *                If the offset is negative or if the length of the array region starting at the offset is
     *                insufficient to hold the FEC parameters
     * @exception NullPointerException
     *                If the provided array is {@code null}
     */
    public void writeTo(byte[] array, int offset) {

        final int arraySize = SizeOf.LONG + SizeOf.INT;
        if (offset < 0 || array.length - offset < arraySize) throw new IndexOutOfBoundsException();
        writeTo(ByteBuffer.wrap(array, offset, arraySize));
    }

    /**
     * Returns a buffer with these FEC parameters. The buffer will contain the
     * <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * 
     * @return a buffer with these FEC parameters
     */
    public ByteBuffer asBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(SizeOf.LONG + SizeOf.INT);
        writeTo(buffer);
        buffer.flip();

        return buffer;
    }

    /**
     * Writes in the provided buffer these FEC parameters. The write consists of the
     * <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
     * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
     * buffer will have been advanced by the same amount.
     * 
     * @param buffer
     *            A buffer on which the FEC parameters are written
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 12 bytes remaining
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     */
    public void writeTo(ByteBuffer buffer) {

        buffer.putLong(commonFecOTI);    // 8 bytes
        buffer.putInt(schemeSpecFecOTI); // 4 bytes
    }

    /**
     * Writes these FEC parameters directly into the provided {@code DataOutput} object. The method will write the
     * <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * <p>
     * Examples of {@code DataOutput} objects are {@link java.io.DataOutputStream DataOutputStream} and
     * {@link java.io.ObjectOutputStream ObjectOutputStream}.
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are written to the output, or an
     * {@code IOException} is throw.
     * 
     * @param out
     *            A {@code DataOutput} object into which the FEC parameters are written
     * @throws IOException
     *             If an I/O error occurs while writing to the {@code DataOutput} object
     * @exception NullPointerException
     *                If {@code out} is {@code null}
     */
    public void writeTo(DataOutput out) throws IOException {

        out.writeLong(commonFecOTI);
        out.writeInt(schemeSpecFecOTI);
    }

    /**
     * Writes these FEC parameters directly into the provided {@code WritableByteChannel} object. The method will write
     * the <a href="ParameterIO.html#common-fec-oti">Common FEC OTI</a> followed by the
     * <a href="ParameterIO.html#schemespec-fec-oti">Scheme-Specific FEC OTI</a>.
     * <p>
     * Examples of {@code WritableByteChannel} objects are {@link java.nio.channels.SocketChannel SocketChannel} and
     * {@link java.nio.channels.FileChannel FileChannel}.
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are written to the channel, or an
     * {@code IOException} is throw.
     * 
     * @param ch
     *            A {@code WritableByteChannel} object into which the FEC parameters are written
     * @throws IOException
     *             If an I/O error occurs while writing to the {@code WritableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    public void writeTo(WritableByteChannel ch) throws IOException {

        ExtraChannels.writeBytes(ch, asBuffer());
    }

    /**
     * Returns the length of the source data, in number of bytes.
     * <p>
     * Since encoded data may contain extra padding bytes, this value allows a decoder to infer the original source data
     * length.
     * 
     * @return the length of the source data, in number of bytes
     */
    public long dataLength() {

        return ParameterIO.extractDataLength(commonFecOTI);
    }

    /**
     * Returns the size of a symbol, in number of bytes.
     * <p>
     * This value represents the size of an encoding symbol (source or repair), except the size of the last source
     * symbol, which may be smaller.
     * 
     * @return the size of a symbol, in number of bytes
     */
    public int symbolSize() {

        return ParameterIO.extractSymbolSize(commonFecOTI);
    }

    /**
     * Returns the number of source blocks into which the source data is partitioned.
     * <p>
     * Each source block is encoded/decoded independently, and not every one is divided into the same number of source
     * symbols.
     * 
     * @return the number of source blocks into which the source data is partitioned
     */
    public int numberOfSourceBlocks() {

        return ParameterIO.extractNumSourceBlocks(schemeSpecFecOTI);
    }

    /**
     * Returns the interleaver length, in number of sub-blocks per source block.
     * <p>
     * This value influences the level of <em>uniform interleaving</em> used before encoding a source block. A value of
     * 1 means no interleaving is used, and a higher value means more interleaving per source block.
     * 
     * @return the interleaver length, in number of sub-blocks per source block
     */
    public int interleaverLength() {

        return ParameterIO.extractInterleaverLength(schemeSpecFecOTI);
    }

    /**
     * Returns the symbol alignment value.
     * <p>
     * This value has no relevance to the user of OpenRQ, since it is fixed to a value of 1. This method exists only for
     * completeness purposes.
     * 
     * @return the symbol alignment value
     */
    public int symbolAlignment() {

        return ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);
    }

    /**
     * Returns the value returned by {@link #dataLength()} cast to an {@code int}, unless the cast causes the data
     * length value to overflow (when {@code dataLength() > Integer.MAX_VALUE}).
     * 
     * @return the value returned by {@code dataLength()} cast to an {@code int}
     * @exception ArithmeticException
     *                If the cast causes the data length value to overflow
     *                (when {@code dataLength() > Integer.MAX_VALUE})
     */
    public int dataLengthAsInt() {

        final long F = dataLength();
        if (F > Integer.MAX_VALUE) {
            throw new ArithmeticException("data length value does not fit inside an int");
        }

        return (int)F;
    }

    /**
     * Returns the total number of source symbols into which the source data is divided.
     * <p>
     * This is a convenience method that simply returns the result of {@code ceiling(dataLength()/symbolSize())}.
     * 
     * @return the total number of source symbols into which the source data is divided
     */
    public int totalSymbols() {

        return getTotalSymbols(dataLength(), symbolSize());
    }

    /**
     * Returns {@code true} if, and only if, this instance is equal to another object.
     * <p>
     * This instance ({@code this}) is equal to another object ({@code obj}), if and only if:
     * <ul>
     * <li>{@code obj} is non-null
     * <li>and {@code obj} is an instance of {@code FECParameters} <li>and {@code this}.{@link #dataLength()} ==
     * {@code obj.dataLength()} <li>and {@code this}.{@link #symbolSize()} == {@code obj.symbolSize()} <li>and
     * {@code this}.{@link #numberOfSourceBlocks()} == {@code obj.numberOfSourceBlocks()} <li>and {@code this}.
     * {@link #interleaverLength()} == {@code obj.interleaverLength()} <li>and {@code this}.{@link #symbolAlignment()}
     * == {@code obj.symbolAlignment()} </ul>
     */
    @Override
    public boolean equals(Object obj) {

        // the first condition filters null objects, so it is always safe to call method areEqual
        return obj instanceof FECParameters && areEqual(this, (FECParameters)obj);
    }

    // requires non-null arguments
    private static boolean areEqual(FECParameters a, FECParameters b) {

        // it is simpler to compare the OTIs, since they contain all FEC parameters within
        return (a.commonFecOTI == b.commonFecOTI) && (a.schemeSpecFecOTI == b.schemeSpecFecOTI);
    }

    /**
     * Returns a hash code value based on all FEC parameters in this instance.
     */
    @Override
    public int hashCode() {

        // it is simpler to use the OTIs, since they contain all FEC parameters within
        final ByteBuffer buf = ByteBuffer.allocate(SizeOf.LONG + SizeOf.INT);
        buf.putLong(commonFecOTI).putInt(schemeSpecFecOTI).rewind();

        return buf.hashCode();
    }

    @Override
    public String toString() {

        // TEST_CODE
        return "FEC Parameters:{F=" + dataLength() + ", T=" + symbolSize() + ", Z=" + numberOfSourceBlocks() + ", N="
               + interleaverLength() + "}";
    }
}
