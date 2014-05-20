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


import static net.fec.openrq.parameters.InternalConstants.T_max;
import static net.fec.openrq.parameters.InternalFunctions.KL;
import static net.fec.openrq.util.arithmetic.ExtraMath.ceilDiv;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.Parsed;
import net.fec.openrq.util.arithmetic.ExtraMath;
import net.fec.openrq.util.numericaltype.SizeOf;


/**
 * This class represents FEC parameters as defined in RFC 6330 as the <em>Encoded FEC Object Transmission Information
 * (OTI)</em>, which contains the <em>Encoded Common FEC OTI</em> and the <em>Encoded Scheme-specific FEC OTI</em>.
 * <p>
 * The FEC parameters consist of:
 * <dl>
 * <dt><b>Data length</b></dt>
 * <dd>The length of the source data, in number of bytes. Since encoded data may contain extra padding bytes, this value
 * allows a decoder to infer the original source data length.</dd>
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
 * Methods are provided to write instances of this class to arrays of bytes, {@link ByteBuffer} objects,
 * {@link DataOutput} objects and {@link WritableByteChannel} objects. Additionally, static methods are provided to
 * parse/read instances of this class from arrays of bytes, {@code ByteBuffer} objects, {@link DataInput} objects and
 * {@link ReadableByteChannel} objects.
 */
public final class FECParameters {

    /**
     * Parses FEC parameters from the provided buffer.
     * <p>
     * The provided buffer must have at least 12 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 12 bytes.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}
     * </ul>
     * 
     * @param buffer
     *            A buffer from which a {@code FECParameters} instance is read
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public static Parsed<FECParameters> parse(ByteBuffer buffer) {

        final long commonFecOTI = buffer.getLong();   // 8 bytes
        final int schemeSpecFecOTI = buffer.getInt(); // 4 bytes

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
            return Parsed.of(new FECParameters(commonFecOTI, schemeSpecFecOTI));
        }
    }

    /**
     * Parses FEC parameters from the provided array starting in a specific index.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}
     * </ul>
     * 
     * @param array
     *            An array from which a {@code FECParameters} instance is read
     * @param offset
     *            The starting array index at which a {@code FECParameters} instance is read
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @exception NullPointerException
     *                If the provided array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If a {@code FECParameters} instance cannot be read at the given index
     */
    public static Parsed<FECParameters> parse(byte[] array, int offset) {

        if (offset < 0 || array.length - offset < 12) throw new IndexOutOfBoundsException();
        return parse(ByteBuffer.wrap(array, offset, 12));
    }

    /**
     * Reads and parses FEC parameters from a {@code DataInput} object.
     * <p>
     * Examples of {@code DataInput} objects are {@link java.io.DataInputStream DataInputStream} and
     * {@link java.io.ObjectInputStream ObjectInputStream}.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}
     * </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are read from the input, or a
     * parsing failure is detected, or an {@code IOException} is throw.
     * 
     * @param in
     *            A {@code DataInput} object from which FEC parameters are read
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @throws IOException
     *             If an IO error occurs while reading from the {@code DataInput} object
     * @exception NullPointerException
     *                If {@code in} is {@code null}
     */
    public static Parsed<FECParameters> readFrom(DataInput in) throws IOException {

        final long commonFecOTI = in.readLong();   // 8 bytes
        final int schemeSpecFecOTI = in.readInt(); // 4 bytes

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
            return Parsed.of(new FECParameters(commonFecOTI, schemeSpecFecOTI));
        }
    }

    /**
     * Reads and parses FEC parameters from a {@code ReadableByteChannel} object.
     * <p>
     * Examples of {@code ReadableByteChannel} objects are {@link java.nio.channels.SocketChannel SocketChannel} and
     * {@link java.nio.channels.FileChannel FileChannel}.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}
     * </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are read from the channel, or a
     * parsing failure is detected, or an {@code IOException} is throw.
     * 
     * @param ch
     *            A {@code ReadableByteChannel} object from which FEC parameters are read
     * @return a container object containing FEC parameters or a parsing failure reason string
     * @throws IOException
     *             If an IO error occurs while reading from the {@code ReadableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    public static Parsed<FECParameters> readFrom(ReadableByteChannel ch) throws IOException {

        final ByteBuffer buffer = ByteBuffer.allocate(SizeOf.LONG + SizeOf.INT);
        while (buffer.hasRemaining()) {
            ch.read(buffer);
        }
        buffer.flip();
        return parse(buffer);
    }

    /**
     * Returns a new instance, given specific FEC parameters.
     * <p>
     * <b>Note:</b> <em>The interleaver length will be equal to 1, which effectively means that data interleaving is
     * disabled. Additionally, the symbol alignment parameter "Al" is internally obtained.</em>
     * <p>
     * The provided FEC parameters are validated by invoking {@link ParameterChecker#areValidFECParameters
     * ParameterChecker.areValidFECParameters(dataLen, symbSize, numSrcBs, 1, Al)}, and an
     * {@code IllegalArgumentException} is thrown if the parameters are invalid.
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
     * ParameterChecker.areValidFECParameters(dataLen, symbSize, numSrcBs, interLen, Al)}, and an
     * {@code IllegalArgumentException} is thrown if the parameters are invalid.
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
            return newInstance(F, T, Z, N, Al);
        }
        else {
            throw new IllegalArgumentException(ParameterChecker.getFECParamsErrorString(F, T, Z, N));
        }
    }

    /**
     * Derives FEC parameters from specific deriving parameters.
     * <p>
     * A maximum payload length is required, and affects the maximum size of an encoding symbol, which will be equal to
     * the provided payload length.
     * <p>
     * A maximum block size that is decodable in working memory is required, and allows the decoder to work with a
     * limited amount of memory in an efficient way.
     * <p>
     * <b>Note:</b> <em>The symbol alignment parameter "Al" is internally obtained.</em>
     * <p>
     * The provided FEC parameters are validated by invoking {@link ParameterChecker#areValidDerivingParameters
     * ParameterChecker.areValidDerivingParameters(dataLen, maxPayLen, maxDecBlock, Al)}, and an
     * {@code IllegalArgumentException} is thrown if the parameters are invalid.
     * 
     * @param dataLen
     *            The length of the source data, in number of bytes
     * @param maxPaLen
     *            The maximum size for a payload containing one encoding symbol
     * @param maxDBMem
     *            The maximum block size, in number of bytes that is decodable in working memory
     * @return a derived {@code FECParameters} instance
     */
    public static FECParameters deriveParameters(long dataLen, int maxPaLen, int maxDBMem) {

        final long F = dataLen;
        final int P = maxPaLen;
        final int WS = maxDBMem;
        final int Al = ParameterChecker.symbolAlignmentValue();

        if (ParameterChecker.areValidDerivingParameters(F, P, WS, Al)) {
            final int T = Math.min(P, T_max);
            // interleaving is disabled for now
            final int SStimesAl = T;                     // SS * Al = T

            // safe cast because F and T are appropriately bounded
            final int Kt = (int)ExtraMath.ceilDiv(F, T); // Kt = ceil(F/T)
            final int N_max = T / SStimesAl;             // N_max = floor(T/(SS*Al))

            final int Z = deriveZ(Kt, N_max, WS, Al, T);
            final int N = deriveN(Kt, Z, N_max, WS, Al, T);

            return newInstance(F, T, Z, N, Al);
        }
        else {
            throw new IllegalArgumentException(ParameterChecker.getDerivingParamsErrorString(F, P, WS, Al));
        }
    }

    private static int deriveZ(long Kt, int N_max, int WS, int Al, int T) {

        // Z = ceil(Kt/KL(N_max))
        return (int)ceilDiv(Kt, KL(N_max, WS, Al, T));
    }

    private static int deriveN(long Kt, int Z, int N_max, int WS, int Al, int T) {

        // N is the minimum n=1, ..., N_max such that ceil(Kt/Z) <= KL(n)
        final int KtOverZ = (int)ceilDiv(Kt, Z);
        int n;
        for (n = 1; n <= N_max && KtOverZ > KL(n, WS, Al, T); n++) {/* loop */}

        return n;
    }

    /**
     * Derives FEC parameters from specific deriving parameters.
     * <p>
     * A maximum payload length is required, and affects the maximum size of an encoding symbol, which will be equal to
     * the provided payload length.
     * <p>
     * A maximum block size that is decodable in working memory is required, and allows the decoder to work with a
     * limited amount of memory in an efficient way.
     * <p>
     * <b>Note:</b> <em>The symbol alignment parameter "Al" is internally obtained.</em>
     * <p>
     * The provided FEC parameters are validated by invoking {@link ParameterChecker#areValidDerivingParameters
     * ParameterChecker.areValidDerivingParameters(dataLen, maxPayLen, maxDecBlock, Al)}, and an
     * {@code IllegalArgumentException} is thrown if the parameters are invalid.
     * 
     * @param dataLen
     *            The length of the source data, in number of bytes
     * @param maxPayLen
     *            The maximum payload length, in number of bytes
     * @param maxDecBlock
     *            The maximum block size, in number of bytes that is decodable in working memory
     * @return a derived {@code FECParameters} instance
     */
    public static FECParameters deriveParameters(int dataLen, int maxPayLen, int maxDecBlock) {

        return deriveParameters((long)dataLen, maxPayLen, maxDecBlock);
    }

    private static FECParameters newInstance(long F, int T, int Z, int N, int Al) {

        final long commonFecOTI = ParameterIO.buildCommonFecOTI(F, T);
        final int schemeSpecFecOTI = ParameterIO.buildSchemeSpecFecOTI(Z, N, Al);
        return new FECParameters(commonFecOTI, schemeSpecFecOTI);
    }


    private final long commonFecOTI;
    private final int schemeSpecFecOTI;


    private FECParameters(long commonFecOTI, int schemeSpecFecOTI) {

        this.commonFecOTI = commonFecOTI;
        this.schemeSpecFecOTI = schemeSpecFecOTI;
    }

    /**
     * Writes in the provided buffer a sequence of bytes that represent the FEC parameters.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
     * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
     * buffer will have advanced by 12 bytes.
     * 
     * @param buffer
     *            A buffer on which the FEC parameters are written
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public void writeTo(ByteBuffer buffer) {

        buffer.putLong(commonFecOTI);    // 8 bytes
        buffer.putInt(schemeSpecFecOTI); // 4 bytes
    }

    /**
     * Writes in the provided array starting in a specific index a sequence of bytes that represent the FEC parameters.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * 
     * @param array
     *            An array on which the FEC parameters are written
     * @param offset
     *            The starting array index at which the FEC parameters are written
     * @exception NullPointerException
     *                If the provided array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If the FEC parameters cannot be written at the given index
     */
    public void writeTo(byte[] array, int offset) {

        if (offset < 0 || array.length - offset < 12) throw new IndexOutOfBoundsException();
        writeTo(ByteBuffer.wrap(array, offset, 12));
    }

    /**
     * Writes these FEC parameters directly into the provided {@code DataOutput} object.
     * <p>
     * Examples of {@code DataOutput} objects are {@link java.io.DataOutputStream DataOutputStream} and
     * {@link java.io.ObjectOutputStream ObjectOutputStream}.
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are written to the output, or an
     * {@code IOException} is throw.
     * 
     * @param out
     *            A {@code DataOutput} object into which these FEC parameters are written
     * @throws IOException
     *             If an IO error occurs while writing to the {@code DataOutput} object
     * @exception NullPointerException
     *                If {@code out} is {@code null}
     */
    public void writeTo(DataOutput out) throws IOException {

        out.writeLong(commonFecOTI);
        out.writeInt(schemeSpecFecOTI);
    }

    /**
     * Writes these FEC parameters directly into the provided {@code WritableByteChannel} object.
     * <p>
     * Examples of {@code WritableByteChannel} objects are {@link java.nio.channels.SocketChannel SocketChannel} and
     * {@link java.nio.channels.FileChannel FileChannel}.
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until all FEC parameters are written to the channel, or an
     * {@code IOException} is throw.
     * 
     * @param ch
     *            A {@code WritableByteChannel} object into which these FEC parameters are written
     * @throws IOException
     *             If an IO error occurs while writing to the {@code WritableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    public void writeTo(WritableByteChannel ch) throws IOException {

        final ByteBuffer buffer = ByteBuffer.allocate(SizeOf.LONG + SizeOf.INT);
        writeTo(buffer);
        buffer.flip();
        while (buffer.hasRemaining()) {
            ch.write(buffer);
        }
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

        // safe cast because F and T are valid, which prevents integer overflow
        return (int)ExtraMath.ceilDiv(dataLength(), symbolSize());
    }

    /**
     * Returns {@code true} if, and only if, this instance is equal to another object.
     * <p>
     * This instance (<b>this</b>) is equal to another object (<b>obj</b>), if and only if:
     * <ul>
     * <li><b>obj</b> is non-null
     * <li>and <b>obj</b> is an instance of {@code FECParameters}
     * <li>and <b>this</b>.{@link #dataLength()} == <b>obj</b>.{@code dataLength()}
     * <li>and <b>this</b>.{@link #symbolSize()} == <b>obj</b>.{@code symbolSize()}
     * <li>and <b>this</b>.{@link #numberOfSourceBlocks()} == <b>obj</b>.{@code numberOfSourceBlocks()}
     * <li>and <b>this</b>.{@link #interleaverLength()} == <b>obj</b>.{@code interleaverLength()}
     * <li>and <b>this</b>.{@link #symbolAlignment()} == <b>obj</b>.{@code symbolAlignment()}
     * </ul>
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
        return "FEC Parameters:{F=" + dataLength() + ", T=" + symbolSize() + ", Z=" + numberOfSourceBlocks() + "}";
    }
}
