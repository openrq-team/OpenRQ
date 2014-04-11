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


import static net.fec.openrq.core.parameters.InternalConstants.T_max;
import static net.fec.openrq.core.parameters.InternalFunctions.KL;
import static net.fec.openrq.core.util.arithmetic.ExtraMath.ceilDiv;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.core.util.arithmetic.ExtraMath;
import net.fec.openrq.core.util.numericaltype.SizeOf;
import net.fec.openrq.core.util.parsing.Parsed;


/**
 * This class represents FEC parameters and provides methods to read from and write to byte arrays and
 * {@link ByteBuffer} objects.
 * <p>
 * The format of the sequence of bytes containing the FEC parameters is defined in RFC 6330 as the <i>Encoded FEC Object
 * Transmission Information (OTI)</i>, which contains the <i>Encoded Common FEC OTI</i> and the <i>Encoded
 * Scheme-specific FEC OTI</i>.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
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
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}</li>
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}</li>
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
        final int N = ParameterIO.extractNumSubBlocks(schemeSpecFecOTI);
        final int Al = ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);

        if (ParameterChecker.areValidFECParameters(F, T, Z, N, Al)) {
            return Parsed.of(new FECParameters(commonFecOTI, schemeSpecFecOTI));
        }
        else {
            return Parsed.invalid(ParameterChecker.testFECParameters(F, T, Z, N, Al));
        }
    }

    /**
     * Parses FEC parameters from the provided array starting in a specific index.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}</li>
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}</li>
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
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}</li>
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}</li>
     * </ul>
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
        final int N = ParameterIO.extractNumSubBlocks(schemeSpecFecOTI);
        final int Al = ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);

        if (ParameterChecker.areValidFECParameters(F, T, Z, N, Al)) {
            return Parsed.of(new FECParameters(commonFecOTI, schemeSpecFecOTI));
        }
        else {
            return Parsed.invalid(ParameterChecker.testFECParameters(F, T, Z, N, Al));
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
     * <li>If the parsing succeeded, the FEC parameters can be retrieved by calling the method {@link Parsed#value()}</li>
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()}</li>
     * </ul>
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
     * Returns a new instance, given specific FEC parameters. The number of sub-blocks will be equal to 1, which
     * effectively means that data interleaving is disabled.
     * <p>
     * The provided FEC parameters must be valid according to
     * {@link ParameterChecker#areValidFECParameters(long, int, int, int, int)}, otherwise an
     * {@code IllegalArgumentException} is thrown. Note, however, that the symbol alignment parameter is internally
     * obtained.
     * 
     * @param dataLen
     *            The length of the encodable data in number of bytes
     * @param symbolSize
     *            The size of a symbol in number of bytes
     * @param numSourceBlocks
     *            The number of blocks into which the encodable data is partitioned
     * @return a new {@code FECParameters} instance
     * @exception IllegalArgumentException
     *                If the provided FEC parameters are invalid
     */
    public static FECParameters newParameters(long dataLen, int symbolSize, int numSourceBlocks) {

        return newParameters(dataLen, symbolSize, numSourceBlocks, 1);
    }

    /**
     * Returns a new instance, given specific FEC parameters.
     * <p>
     * The provided FEC parameters must be valid according to
     * {@link ParameterChecker#areValidFECParameters(long, int, int, int, int)}, otherwise an
     * {@code IllegalArgumentException} is thrown. Note, however, that the symbol alignment parameter is internally
     * obtained.
     * 
     * @param dataLen
     *            The length of the encodable data in number of bytes
     * @param symbolSize
     *            The size of a symbol in number of bytes
     * @param numSourceBlocks
     *            The number of blocks into which the encodable data is partitioned
     * @param numSubBlocks
     *            The number of sub-blocks per source block into which the encodable data is partitioned
     * @return a new {@code FECParameters} instance
     * @exception IllegalArgumentException
     *                If the provided FEC parameters are invalid
     */
    public static FECParameters newParameters(long dataLen, int symbolSize, int numSourceBlocks, int numSubBlocks) {

        final long F = dataLen;
        final int T = symbolSize;
        final int Z = numSourceBlocks;
        final int N = numSubBlocks;
        final int Al = ParameterChecker.symbolAlignmentValue();

        if (ParameterChecker.areValidFECParameters(F, T, Z, N, Al)) {
            return newInstance(F, T, Z, N, Al);
        }
        else {
            throw new IllegalArgumentException(ParameterChecker.testFECParameters(F, T, Z, N, Al));
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
     * The provided FEC parameters must be valid according to
     * {@link ParameterChecker#areValidDerivingParameters(long, int, int, int)}, otherwise an
     * {@code IllegalArgumentException} is thrown. Note, however, that the symbol alignment parameter is internally
     * obtained.
     * 
     * @param dataLen
     *            The length of the encodable data in number of bytes
     * @param maxPayLen
     *            The maximum payload length in number of bytes
     * @param maxDecBlock
     *            The maximum block size in number of bytes that is decodable in working memory
     * @return a derived {@code FECParameters} instance
     */
    public static FECParameters deriveParameters(long dataLen, int maxPayLen, int maxDecBlock) {

        final long F = dataLen;
        final int P = maxPayLen;
        final int WS = maxDecBlock;
        final int Al = ParameterChecker.symbolAlignmentValue();

        if (ParameterChecker.areValidDerivingParameters(dataLen, maxPayLen, maxDecBlock, Al)) {
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
            throw new IllegalArgumentException("invalid deriving parameters");
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
     * The provided FEC parameters must be valid according to
     * {@link ParameterChecker#areValidDerivingParameters(long, int, int, int)}, otherwise an
     * {@code IllegalArgumentException} is thrown. Note, however, that the symbol alignment parameter is internally
     * obtained.
     * 
     * @param dataLen
     *            The length of the encodable data in number of bytes
     * @param maxPayLen
     *            The maximum payload length in number of bytes
     * @param maxDecBlock
     *            The maximum block size in number of bytes that is decodable in working memory
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
     * Returns the length of the encodable data in number of bytes.
     * 
     * @return the length of the encodable data in number of bytes
     */
    public long dataLength() {

        return ParameterIO.extractDataLength(commonFecOTI);
    }

    /**
     * Returns the size of a symbol in number of bytes.
     * 
     * @return the size of a symbol in number of bytes
     */
    public int symbolSize() {

        return ParameterIO.extractSymbolSize(commonFecOTI);
    }

    /**
     * Returns the number of blocks into which the encodable data is partitioned.
     * 
     * @return the number of blocks into which the encodable data is partitioned
     */
    public int numberOfSourceBlocks() {

        return ParameterIO.extractNumSourceBlocks(schemeSpecFecOTI);
    }

    /**
     * Returns the number of sub-blocks per source block into which the encodable data is partitioned.
     * 
     * @return the number of sub-blocks per source block into which the encodable data is partitioned
     */
    public int numberOfSubBlocks() {

        return ParameterIO.extractNumSubBlocks(schemeSpecFecOTI);
    }

    /**
     * Returns the symbol alignment value.
     * 
     * @return the symbol alignment value
     */
    public int symbolAlignment() {

        return ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);
    }

    /**
     * Returns the total number of source symbols in which the encodable data is divided.
     * 
     * @return the total number of source symbols in which the encodable data is divided
     */
    public int totalSymbols() {

        // safe cast because F and T are valid, which prevents integer overflow
        return (int)ExtraMath.ceilDiv(dataLength(), symbolSize());
    }
}
