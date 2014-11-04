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
package net.fec.openrq;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.datatype.UnsignedTypes;
import net.fec.openrq.util.io.BufferOperation;
import net.fec.openrq.util.io.ExtraChannels;


/**
 * This class represents information about a source block decoder that can be conveyed to an encoder, in order to help
 * decide which symbols are prioritized for encoding.
 * <p>
 * When a back communication channel is available from a receiver to a sender of data, information about a specific
 * source block may be useful in determining how the source block is encoded by the sender. For example, such
 * information may contain a list with the identifiers of the missing source symbols at the receiver, so that the sender
 * can know which symbols it needs to transmit.
 * <p>
 * This class offers the following information:
 * <ul>
 * <li><b>Source Block Number</b>: the identifier of the source block
 * <li><b>Decoder State</b>: a flag indicating if the source block is incomplete, decoded or if it suffered a decoding
 * failure
 * <li><b>Missing Source Symbols</b>: a list with the identifiers of all missing source symbols at the decoder
 * <li><b>Available Repair Symbols</b>: a list with the identifiers of all repair symbols that were received by the
 * decoder
 * </ul>
 * <p>
 * Methods are provided to write instances of this class to arrays of bytes, {@link ByteBuffer} objects, serializable
 * objects, {@link DataOutput} objects and {@link WritableByteChannel} objects. Additionally, static methods are
 * provided to parse/read instances of this class from arrays of bytes, {@code ByteBuffer} objects, serializable
 * objects, {@link DataInput} objects and {@link ReadableByteChannel} objects.
 */
public final class SBDInfo {

    /**
     * Requires valid arguments and unmodifiable sets!
     */
    @SuppressWarnings("javadoc")
    static SBDInfo newInformation(
        int sbn,
        SourceBlockState state,
        Set<Integer> missingSourceSymbols,
        Set<Integer> availableRepairSymbols)
    {

        return new SBDInfo(
            sbn,
            state,
            Collections.unmodifiableSet(missingSourceSymbols),
            Collections.unmodifiableSet(availableRepairSymbols));
    }

    /**
     * Parses source block decoder information from the given serializable object.
     * <p>
     * The provided FEC parameters are used to validate the information during the parsing.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the information can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param serInfo
     *            A serializable object containing source block decoder information
     * @param fecParams
     *            FEC parameters associated to the source data containing the source block being decoded
     * @return a container object containing a {@code SBFInfo} instance or a parsing failure reason string
     * @exception NullPointerException
     *                If any argument is {@code null}
     */
    public static Parsed<SBDInfo> parse(SerializableSBDInfo serInfo, FECParameters fecParams) {

        return parse(serInfo.sourceBlockDecoderInfo(), fecParams);
    }

    /**
     * Parses source block decoder information from the given array. The information bytes in the array must follow the
     * format specified by {@link #asArray()}.
     * <p>
     * The information will be read, in the array, from position {@code 0} inclusive to position {@code array.length}
     * exclusive.
     * <p>
     * The provided FEC parameters are used to validate the information during the parsing.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the information can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param array
     *            An array of bytes containing source block decoder information
     * @param fecParams
     *            FEC parameters associated to the source data containing the source block being decoded
     * @return a container object containing source block decoder information or a parsing failure reason string
     * @exception NullPointerException
     *                If any argument is {@code null}
     */
    public static Parsed<SBDInfo> parse(byte[] array, FECParameters fecParams) {

        return parse(array, 0, array.length, fecParams);
    }

    /**
     * Parses source block decoder information from the given array. The information bytes in the array must follow the
     * format specified by {@link #asArray()}.
     * <p>
     * The information will be read, in the array, from position {@code off} inclusive to position {@code (off + len)}
     * exclusive.
     * <p>
     * The provided FEC parameters are used to validate the information during the parsing.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the information can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param array
     *            An array of bytes containing source block decoder information
     * @param off
     *            The starting index in the array (must be non-negative)
     * @param len
     *            The length of the information (must be non-negative and no larger than {@code array.length - off})
     * @param fecParams
     *            FEC parameters associated to the source data containing the source block being decoded
     * @return a container object containing source block decoder information or a parsing failure reason string
     * @exception IndexOutOfBoundsException
     *                If the pre-conditions on the array offset and length do not hold
     * @exception NullPointerException
     *                If any argument is {@code null}
     */
    public static Parsed<SBDInfo> parse(byte[] array, int off, int len, FECParameters fecParams) {

        Indexables.checkOffsetLengthBounds(off, len, array.length);
        return parse(ByteBuffer.wrap(array, off, len), fecParams);
    }

    /**
     * Parses source block decoder information from the given buffer. The information bytes in the buffer must follow
     * the format specified by {@link #asBuffer()}.
     * <p>
     * The information will be read, in the buffer, from the current {@linkplain ByteBuffer#position() position}
     * inclusive to the current {@linkplain ByteBuffer#limit() limit} exclusive. If the parsing succeeds, the position
     * of the buffer will have been advanced by the number of bytes read.
     * <p>
     * The provided FEC parameters are used to validate the information during the parsing.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the information can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param buffer
     *            A buffer containing source block decoder information
     * @param fecParams
     *            FEC parameters associated to the source data containing the source block being decoded
     * @return a container object containing source block decoder information or a parsing failure reason string
     * @exception NullPointerException
     *                If any argument is {@code null}
     */
    public static Parsed<SBDInfo> parse(ByteBuffer buffer, FECParameters fecParams) {

        Objects.requireNonNull(buffer);

        try {
            final int sbn = readSBN(buffer, fecParams);
            final int K = DataUtils.getK(fecParams, sbn);

            final SourceBlockState state = readState(buffer);

            final int numMiss = readNumMissingSourceSymbols(buffer, K, state);
            final Set<Integer> missing = readMissingSourceSymbols(buffer, numMiss, K);

            final int numAvail = readNumAvailableRepairSymbols(buffer, K, state);
            final Set<Integer> available = readAvailableRepairSymbols(buffer, numAvail, K);

            return newRemoteInfo(sbn, state, missing, available);
        }
        catch (InternalParsingException e) {
            return Parsed.invalid(e.getMessage());
        }
    }

    /**
     * Reads and parses source block decoder information from a {@code DataInput} object. The read information bytes
     * must follow the format specified by {@link #writeTo(java.io.DataOutput)}.
     * <p>
     * Examples of {@code DataInput} objects are {@link java.io.DataInputStream DataInputStream} and
     * {@link java.io.ObjectInputStream ObjectInputStream}.
     * <p>
     * The provided FEC parameters are used to validate the information during the parsing.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the information can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until the whole information is read from the input, or a
     * parsing failure is detected, or an {@code IOException} is throw.
     * 
     * @param in
     *            A {@code DataInput} object from which source block decoder information is read
     * @param fecParams
     *            FEC parameters associated to the source data containing the source block being decoded
     * @return a container object containing source block decoder information or a parsing failure reason string
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code DataInput} object
     * @exception NullPointerException
     *                If any argument is {@code null}
     */
    public static Parsed<SBDInfo> readFrom(DataInput in, FECParameters fecParams) throws IOException {

        Objects.requireNonNull(in);

        try {
            final int sbn = readSBN(in, fecParams);
            final int K = DataUtils.getK(fecParams, sbn);

            final SourceBlockState state = readState(in);

            final int numMiss = readNumMissingSourceSymbols(in, K, state);
            final Set<Integer> missing = readMissingSourceSymbols(in, numMiss, K);

            final int numAvail = readNumAvailableRepairSymbols(in, K, state);
            final Set<Integer> available = readAvailableRepairSymbols(in, numAvail, K);

            return newRemoteInfo(sbn, state, missing, available);
        }
        catch (InternalParsingException e) {
            return Parsed.invalid(e.getMessage());
        }
    }

    /**
     * Reads and parses source block decoder information from a {@code ReadableByteChannel} object. The read information
     * bytes must follow the format specified by {@link #writeTo(java.nio.channels.WritableByteChannel)}.
     * <p>
     * Examples of {@code ReadableByteChannel} objects are {@link java.nio.channels.SocketChannel SocketChannel} and
     * {@link java.nio.channels.FileChannel FileChannel}.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the information can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until the whole information is read from the channel, or a
     * parsing failure is detected, or an {@code IOException} is throw.
     * 
     * @param ch
     *            A {@code ReadableByteChannel} object from which source block decoder information is read
     * @param fecParams
     *            FEC parameters associated to the source data containing the source block being decoded
     * @return a container object containing source block decoder information or a parsing failure reason string
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code ReadableByteChannel} object
     * @exception NullPointerException
     *                If any argument is {@code null}
     */
    public static Parsed<SBDInfo> readFrom(ReadableByteChannel ch, FECParameters fecParams) throws IOException {

        try {
            final ByteBuffer sbnAndStateBuf = ByteBuffer.allocate(SizeOf.BYTE + SizeOf.BYTE);
            ExtraChannels.readBytes(ch, sbnAndStateBuf, BufferOperation.FLIP_ABSOLUTELY);

            final int sbn = readSBN(sbnAndStateBuf, fecParams);
            final int K = DataUtils.getK(fecParams, sbn);
            final SourceBlockState state = readState(sbnAndStateBuf);

            final ByteBuffer numMissBuf = ByteBuffer.allocate(SizeOf.SHORT);
            ExtraChannels.readBytes(ch, numMissBuf, BufferOperation.FLIP_ABSOLUTELY);

            final int numMiss = readNumMissingSourceSymbols(numMissBuf, K, state);

            final ByteBuffer missingBuf = ByteBuffer.allocate(numMiss * SizeOf.SHORT);
            ExtraChannels.readBytes(ch, missingBuf, BufferOperation.FLIP_ABSOLUTELY);

            final Set<Integer> missing = readMissingSourceSymbols(missingBuf, numMiss, K);

            final ByteBuffer numAvailBuf = ByteBuffer.allocate(SizeOf.UNSIGNED_3_BYTES);
            ExtraChannels.readBytes(ch, numAvailBuf, BufferOperation.FLIP_ABSOLUTELY);

            final int numAvail = readNumAvailableRepairSymbols(numAvailBuf, K, state);

            final ByteBuffer availableBuf = ByteBuffer.allocate(numAvail * SizeOf.UNSIGNED_3_BYTES);
            ExtraChannels.readBytes(ch, availableBuf, BufferOperation.FLIP_ABSOLUTELY);

            final Set<Integer> available = readAvailableRepairSymbols(availableBuf, numAvail, K);

            return newRemoteInfo(sbn, state, missing, available);
        }
        catch (InternalParsingException e) {
            return Parsed.invalid(e.getMessage());
        }
    }

    private static Parsed<SBDInfo> newRemoteInfo(
        int sbn,
        SourceBlockState state,
        Set<Integer> missingSourceSymbols,
        Set<Integer> availableRepairSymbols)
    {

        return Parsed.of(
            new SBDInfo(
                sbn,
                state,
                Collections.unmodifiableSet(missingSourceSymbols),
                Collections.unmodifiableSet(availableRepairSymbols)));
    }


    private final int sbn;
    private final SourceBlockState state;
    private final Set<Integer> missingSourceSymbols;
    private final Set<Integer> availableRepairSymbols;


    /*
     * Requires unmodifiable sets!
     */
    private SBDInfo(
        int sbn,
        SourceBlockState state,
        Set<Integer> missingSourceSymbols,
        Set<Integer> availableRepairSymbols)
    {

        this.sbn = sbn;
        this.state = Objects.requireNonNull(state);
        this.missingSourceSymbols = Objects.requireNonNull(missingSourceSymbols);
        this.availableRepairSymbols = Objects.requireNonNull(availableRepairSymbols);
    }

    /**
     * Returns the identifier of the source block being decoded.
     * 
     * @return the identifier of the source block being decoded
     */
    public int sourceBlockNumber() {

        return sbn;
    }

    /**
     * Returns the latest state of the source block being decoded.
     * <p>
     * The result of this method invocation is a {@link SourceBlockState} value:
     * <dl>
     * <dt>{@link SourceBlockState#INCOMPLETE INCOMPLETE}:</dt>
     * <dd>means that not enough encoding symbols are available for a decoding operation.</dd>
     * <dt>{@link SourceBlockState#DECODED DECODED}:</dt>
     * <dd>means that a decoding operation took place and succeeded in decoding the source block.</dd>
     * <dt>{@link SourceBlockState#DECODING_FAILURE DECODING_FAILURE}:</dt>
     * <dd>means that a decoding operation took place but failed in decoding the source block; additional encoding
     * symbols are required for a successful decoding.</dd>
     * </dl>
     * 
     * @return the latest state of the source block being decoded
     */
    public SourceBlockState state() {

        return state;
    }

    /**
     * Returns an unmodifiable set of integers containing the encoding symbol identifiers of the missing source symbols
     * from the source block being decoded.
     * 
     * @return a set of encoding symbol identifiers of missing source symbols
     */
    public Set<Integer> missingSourceSymbols() {

        return missingSourceSymbols;
    }

    /**
     * Returns an unmodifiable set of integers containing the encoding symbol identifiers of the available repair
     * symbols for decoding.
     * 
     * @return a set of encoding symbol identifiers of available repair symbols
     */
    public Set<Integer> availableRepairSymbols() {

        return availableRepairSymbols;
    }

    /**
     * Returns {@code true} if, and only if, this instance is equal to another object.
     * <p>
     * This instance ({@code this}) is equal to another object ({@code obj}), if and only if:
     * <ul>
     * <li>{@code obj} is non-null
     * <li>and {@code obj} is an instance of {@code SBDInfo} <li>and {@code this}.{@link #sourceBlockNumber()} ==
     * {@code obj.sourceBlockNumber()} <li>and {@code this}.{@link #state()}{@code .equals(obj.state())} <li>and
     * {@code this}.{@link #missingSourceSymbols()}{@code .equals(obj.missingSourceSymbols())} <li>and {@code this}.
     * {@link #availableRepairSymbols()}{@code .equals(obj.availableRepairSymbols())} </ul>
     */
    @Override
    public boolean equals(Object obj) {

        return obj instanceof SBDInfo && areEqual(this, (SBDInfo)obj);
    }

    private static boolean areEqual(SBDInfo info1, SBDInfo info2) {

        return info1.sbn == info2.sbn &&
               info1.state.equals(info2.state) &&
               info1.missingSourceSymbols.equals(info2.missingSourceSymbols) &&
               info1.availableRepairSymbols.equals(info2.availableRepairSymbols);
    }

    /**
     * Returns a hash code value based on the whole information of this instance.
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + sbn;
        result = prime * result + state.hashCode();
        result = prime * result + missingSourceSymbols.hashCode();
        result = prime * result + availableRepairSymbols.hashCode();
        return result;
    }

    @Override
    public String toString() {

        return String.format("SBN: %d%nState: %s%nMissing source symbols: %s%nAvailable repair symbols: %s",
            sbn, state, missingSourceSymbols, availableRepairSymbols);
    }

    /**
     * Returns a serializable object with this information encoded in a compact format (the same format as of method
     * {@link #asArray()}).
     * 
     * @return a serializable object with this information encoded in a compact format
     */
    public SerializableSBDInfo asSerializable() {

        return new SerializableSBDInfo(asArray());
    }

    /**
     * Returns an array with this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()} <li>
     * {@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()} </ul>
     * <p>
     * The array will contain the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * 
     * @return an array with this information encoded in a compact format
     */
    public byte[] asArray() {

        final byte[] array = new byte[getEncodedByteSize()];
        writeTo(ByteBuffer.wrap(array));

        return array;
    }

    /**
     * Writes in the provided array starting at index zero this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()} <li>
     * {@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()} </ul>
     * <p>
     * The write consists of the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * <p>
     * The provided array must have a length of at least {@code (7 + NUM_MISSING_BYTES + NUM_AVAILABLE_BYTES)} bytes.
     * 
     * @param array
     *            An array on which the information is written
     * @exception IndexOutOfBoundsException
     *                If the length of the array is insufficient to hold the information
     * @exception NullPointerException
     *                If the provided array is {@code null}
     */
    public void writeTo(byte[] array) {

        writeTo(array, 0);
    }

    /**
     * Writes in the provided array starting in a specific index this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()} <li>
     * {@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()} </ul>
     * <p>
     * The write consists of the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * <p>
     * The provided array must have at least {@code (7 + NUM_MISSING_BYTES + NUM_AVAILABLE_BYTES)} bytes between the
     * given index and its length.
     * 
     * @param array
     *            An array on which the information is written
     * @param offset
     *            The starting array index at which the information is written (must be non-negative)
     * @exception IndexOutOfBoundsException
     *                If the offset is negative or if the length of the array region starting at the offset is
     *                insufficient to hold the information
     * @exception NullPointerException
     *                If the provided array is {@code null}
     */
    public void writeTo(byte[] array, int offset) {

        final int arraySize = getEncodedByteSize();
        if (offset < 0 || array.length - offset < arraySize) throw new IndexOutOfBoundsException();
        writeTo(ByteBuffer.wrap(array, offset, arraySize));
    }

    /**
     * Returns a buffer with this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()} <li>
     * {@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()} </ul>
     * <p>
     * The buffer will contain the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * 
     * @return a buffer with this information encoded in a compact format
     */
    public ByteBuffer asBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(getEncodedByteSize());
        writeTo(buffer);
        buffer.flip();

        return buffer;
    }

    /**
     * Writes in the provided buffer this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()} <li>
     * {@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()} </ul>
     * <p>
     * The write consists of the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least
     * {@code (7 + NUM_MISSING_BYTES + NUM_AVAILABLE_BYTES)} bytes {@linkplain ByteBuffer#remaining() remaining}. If
     * this method returns normally, the position of the provided buffer will have been advanced by the same amount.
     * 
     * @param buffer
     *            A buffer on which the information is written
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than {@code (7 + NUM_MISSING_BYTES + NUM_AVAILABLE_BYTES)} bytes
     *                remaining
     * @exception NullPointerException
     *                If the {@code buffer} is {@code null}
     */
    public void writeTo(ByteBuffer buffer) {

        Objects.requireNonNull(buffer);

        writeSBN(sbn, buffer);
        writeState(state, buffer);
        writeMissingSourceSymbols(missingSourceSymbols, buffer);
        writeAvailableRepairSymbols(availableRepairSymbols, buffer);
    }

    /**
     * Writes this information encoded in a compact format directly into the provided {@code DataOutput} object.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()} <li>
     * {@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()} </ul>
     * <p>
     * The method will write the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * <p>
     * Examples of {@code DataOutput} objects are {@link java.io.DataOutputStream DataOutputStream} and
     * {@link java.io.ObjectOutputStream ObjectOutputStream}.
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until the whole information is written to the output, or an
     * {@code IOException} is throw.
     * 
     * @param out
     *            A {@code DataOutput} object into which the information is written
     * @throws IOException
     *             If an I/O error occurs while writing to the {@code DataOutput} object
     * @exception NullPointerException
     *                If {@code out} is {@code null}
     */
    public void writeTo(DataOutput out) throws IOException {

        Objects.requireNonNull(out);

        writeSBN(sbn, out);
        writeState(state, out);
        writeMissingSourceSymbols(missingSourceSymbols, out);
        writeAvailableRepairSymbols(availableRepairSymbols, out);
    }

    /**
     * Writes this information encoded in a compact format directly into the provided {@code WritableByteChannel}
     * object.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()} <li>
     * {@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()} </ul>
     * <p>
     * The method will write the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until the whole information is written to the channel, or
     * an {@code IOException} is throw.
     * 
     * @param ch
     *            A {@code WritableByteChannel} object into which the information is written
     * @throws IOException
     *             If an I/O error occurs while writing to the {@code WritableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    public void writeTo(WritableByteChannel ch) throws IOException {

        ExtraChannels.writeBytes(ch, asBuffer());
    }

    private int getEncodedByteSize() {

        final int numMissBytes = SizeOf.SHORT * missingSourceSymbols.size();
        final int numAvaBytes = SizeOf.UNSIGNED_3_BYTES * availableRepairSymbols.size();
        return SizeOf.BYTE + SizeOf.BYTE + SizeOf.SHORT + numMissBytes + SizeOf.UNSIGNED_3_BYTES + numAvaBytes;
    }


    /*
     * -------------------------- state/byte methods --------------------------
     */

    private static final Map<SourceBlockState, Byte> STATE_BYTE_VALUES;
    private static final Map<Byte, SourceBlockState> BYTE_STATE_VALUES;
    static {
        final byte incomplete = 1;
        final byte decoded = 2;
        final byte decodingFailure = 3;

        STATE_BYTE_VALUES = new EnumMap<>(SourceBlockState.class);
        BYTE_STATE_VALUES = new HashMap<>(4, 1.0f);

        STATE_BYTE_VALUES.put(SourceBlockState.INCOMPLETE, incomplete);
        BYTE_STATE_VALUES.put(incomplete, SourceBlockState.INCOMPLETE);

        STATE_BYTE_VALUES.put(SourceBlockState.DECODED, decoded);
        BYTE_STATE_VALUES.put(decoded, SourceBlockState.DECODED);

        STATE_BYTE_VALUES.put(SourceBlockState.DECODING_FAILURE, decodingFailure);
        BYTE_STATE_VALUES.put(decodingFailure, SourceBlockState.DECODING_FAILURE);
    }


    // throws NullPointerException if an invalid state is passed as argument
    private static byte stateToByte(SourceBlockState state) {

        return STATE_BYTE_VALUES.get(state);
    }

    // returns null if an invalid byte is passed as argument
    private static SourceBlockState byteToState(byte b) {

        return BYTE_STATE_VALUES.get(b);
    }

    /*
     * -------------------------- Write/Read methods --------------------------
     */

    /*
     * ========================== SBN ==========================
     * 1 byte
     * range: [0, 255]
     */

    private static void writeSBN(int sbn, ByteBuffer buf) {

        UnsignedTypes.writeUnsignedByte(sbn, buf);
    }

    private static void writeSBN(int sbn, DataOutput out) throws IOException {

        out.writeByte((byte)sbn);
    }

    private static int readSBN(ByteBuffer buf, FECParameters fecParams) throws InternalParsingException {

        if (buf.remaining() < SizeOf.BYTE) {
            throw new InternalParsingException("source block number is missing");
        }

        final int sbn = UnsignedTypes.readUnsignedByte(buf);
        return checkSBN(sbn, fecParams);
    }

    private static int readSBN(DataInput in, FECParameters fecParams) throws IOException, InternalParsingException {

        final int sbn = UnsignedTypes.getUnsignedByte(in.readByte());
        return checkSBN(sbn, fecParams);
    }

    private static int checkSBN(int sbn, FECParameters fecParams) throws InternalParsingException {

        if (ParameterChecker.isSourceBlockNumberOutOfBounds(sbn)) {
            throw new InternalParsingException("source block number is out of bounds");
        }
        if (sbn >= fecParams.numberOfSourceBlocks()) {
            throw new InternalParsingException("source block number is too high");
        }
        return sbn;
    }

    /*
     * ========================== state ==========================
     * 1 byte
     * values: {1, 2, 3}
     */

    private static void writeState(SourceBlockState state, ByteBuffer buf) {

        UnsignedTypes.writeUnsignedByte(stateToByte(state), buf);
    }

    private static void writeState(SourceBlockState state, DataOutput out) throws IOException {

        out.writeByte(stateToByte(state));
    }

    private static SourceBlockState readState(ByteBuffer buf) throws InternalParsingException {

        if (buf.remaining() < SizeOf.BYTE) {
            throw new InternalParsingException("source block state is missing");
        }

        final SourceBlockState state = byteToState(buf.get());
        return checkState(state);
    }

    private static SourceBlockState readState(DataInput in) throws IOException, InternalParsingException {

        final SourceBlockState state = byteToState(in.readByte());
        return checkState(state);
    }

    private static SourceBlockState checkState(SourceBlockState state) throws InternalParsingException {

        if (state == null) {
            throw new InternalParsingException("invalid source block state");
        }
        return state;
    }


    // ========================== useful ESI bounds ==========================

    private static final int minESI = ParameterChecker.minEncodingSymbolID();
    private static final int maxESI = ParameterChecker.maxEncodingSymbolID();


    /*
     * ========================== missing source symbols ==========================
     * 2 bytes for the number of symbols, and 2 bytes for each symbol ESI
     * range of number: [0, maxSrcSymbs]
     * range of each ESI: [minESI, maxSrcESI]
     */

    private static void writeMissingSourceSymbols(Set<Integer> missing, ByteBuffer buf) {

        UnsignedTypes.writeUnsignedShort(missing.size(), buf);
        for (int esi : missing) {
            UnsignedTypes.writeUnsignedShort(esi, buf);
        }
    }

    private static void writeMissingSourceSymbols(Set<Integer> missing, DataOutput out) throws IOException {

        out.writeShort((short)missing.size());
        for (int esi : missing) {
            out.writeShort((short)esi);
        }
    }

    private static int readNumMissingSourceSymbols(ByteBuffer buf, int K, SourceBlockState state)
        throws InternalParsingException
    {

        if (buf.remaining() < SizeOf.SHORT) {
            throw new InternalParsingException("number of missing source symbols is missing");
        }

        return checkNumMissing(UnsignedTypes.readUnsignedShort(buf), K, state);
    }

    private static int readNumMissingSourceSymbols(DataInput in, int K, SourceBlockState state)
        throws IOException, InternalParsingException
    {

        return checkNumMissing(UnsignedTypes.getUnsignedShort(in.readShort()), K, state);
    }

    private static int checkNumMissing(int numMiss, int K, SourceBlockState state) throws InternalParsingException {

        if (numMiss < 0 || numMiss > K) {
            throw new InternalParsingException("number of missing source symbols is out of bounds");
        }
        if (state == SourceBlockState.DECODED && numMiss != 0) {
            throw new InternalParsingException(
                "number of missing source symbols is not zero when decoder state is DECODED");
        }
        return numMiss;
    }

    // requires valid numMiss
    private static Set<Integer> readMissingSourceSymbols(ByteBuffer buf, int numMiss, int K)
        throws InternalParsingException
    {

        final int rem = buf.remaining();
        if (rem < (numMiss * SizeOf.SHORT)) { // product never overflows
            throw new InternalParsingException(String.format(
                "missing source symbols data is incomplete, required %d bytes but only %d bytes are available",
                (numMiss * SizeOf.SHORT), rem));
        }

        final Set<Integer> missing = new LinkedHashSet<>(numMiss);
        for (int n = 0; n < numMiss; n++) {
            final int esi = UnsignedTypes.readUnsignedShort(buf);
            addMissingSourceSymbolESI(esi, missing, K);
        }

        return missing;
    }

    // requires valid numMiss
    private static Set<Integer> readMissingSourceSymbols(DataInput in, int numMiss, int K)
        throws IOException, InternalParsingException
    {

        final Set<Integer> missing = new LinkedHashSet<>(numMiss);
        for (int n = 0; n < numMiss; n++) {
            final int esi = UnsignedTypes.getUnsignedShort(in.readShort());
            addMissingSourceSymbolESI(esi, missing, K);
        }

        return missing;
    }

    private static void addMissingSourceSymbolESI(int esi, Set<Integer> missing, int K)
        throws InternalParsingException
    {

        if (esi < minESI || esi >= K) {
            throw new InternalParsingException("missing source symbol identifier is out of bounds");
        }
        if (!missing.add(esi)) {
            throw new InternalParsingException("found repeated missing source symbol identifier");
        }
    }

    /*
     * ========================== available repair symbols ==========================
     * 3 bytes for the number of symbols, and 3 bytes for each symbol ESI
     * range of number: [0, maxRepSymbs]
     * range of each ESI: [minRepESI, maxESI]
     */

    private static void writeAvailableRepairSymbols(Set<Integer> available, ByteBuffer buf) {

        UnsignedTypes.writeUnsignedBytes(available.size(), buf, SizeOf.UNSIGNED_3_BYTES);
        for (int esi : available) {
            UnsignedTypes.writeUnsignedBytes(esi, buf, SizeOf.UNSIGNED_3_BYTES);
        }
    }

    private static void writeAvailableRepairSymbols(Set<Integer> available, DataOutput out) throws IOException {

        out.write(UnsignedTypes.getUnsignedBytesAsArray(available.size(), SizeOf.UNSIGNED_3_BYTES));
        for (int esi : available) {
            out.write(UnsignedTypes.getUnsignedBytesAsArray(esi, SizeOf.UNSIGNED_3_BYTES));
        }
    }

    private static int readNumAvailableRepairSymbols(ByteBuffer buf, int K, SourceBlockState state)
        throws InternalParsingException
    {

        if (buf.remaining() < SizeOf.UNSIGNED_3_BYTES) {
            throw new InternalParsingException("number of available repair symbols is missing");
        }

        return checkNumAvailable(UnsignedTypes.readUnsignedBytes(buf, SizeOf.UNSIGNED_3_BYTES), K, state);
    }

    private static int readNumAvailableRepairSymbols(DataInput in, int K, SourceBlockState state)
        throws IOException, InternalParsingException
    {

        final byte[] _3byteArray = new byte[SizeOf.UNSIGNED_3_BYTES];
        in.readFully(_3byteArray);

        return checkNumAvailable(UnsignedTypes.getUnsignedBytes(_3byteArray, SizeOf.UNSIGNED_3_BYTES), K, state);
    }

    private static int checkNumAvailable(int numAvail, int K, SourceBlockState state)
        throws InternalParsingException
    {

        if (numAvail < 0 || numAvail > ParameterChecker.numRepairSymbolsPerBlock(K)) {
            throw new InternalParsingException("number of available repair symbols is out of bounds");
        }
        if (state == SourceBlockState.DECODED && numAvail != 0) {
            throw new InternalParsingException(
                "number of available repair symbols is not zero when decoder state is DECODED");
        }
        if (state == SourceBlockState.DECODING_FAILURE && numAvail == 0) {
            throw new InternalParsingException(
                "number of available repair symbols is zero when decoder state is DECODING_FAILURE");
        }
        return numAvail;
    }

    // requires valid numAvail
    private static Set<Integer> readAvailableRepairSymbols(ByteBuffer buf, int numAvail, int K)
        throws InternalParsingException
    {

        final int rem = buf.remaining();
        if (rem < (numAvail * SizeOf.UNSIGNED_3_BYTES)) { // product never overflows
            throw new InternalParsingException(String.format(
                "available repair symbols data is incomplete, required %d bytes but only %d bytes are available",
                (numAvail * SizeOf.UNSIGNED_3_BYTES), rem));
        }

        final Set<Integer> available = new LinkedHashSet<>(numAvail);
        for (int n = 0; n < numAvail; n++) {
            final int esi = UnsignedTypes.readUnsignedBytes(buf, SizeOf.UNSIGNED_3_BYTES);
            addAvailableRepairSymbolESI(esi, available, K);
        }

        return available;
    }

    // requires valid numAvail
    private static Set<Integer> readAvailableRepairSymbols(DataInput in, int numAvail, int K)
        throws IOException, InternalParsingException
    {

        final byte[] _3byteArray = new byte[SizeOf.UNSIGNED_3_BYTES];

        final Set<Integer> available = new LinkedHashSet<>(numAvail);
        for (int n = 0; n < numAvail; n++) {
            in.readFully(_3byteArray);
            final int esi = UnsignedTypes.getUnsignedBytes(_3byteArray, SizeOf.UNSIGNED_3_BYTES);
            addAvailableRepairSymbolESI(esi, available, K);
        }

        return available;
    }

    private static void addAvailableRepairSymbolESI(int esi, Set<Integer> available, int K)
        throws InternalParsingException
    {

        if (esi < K || esi > maxESI) {
            throw new InternalParsingException("available repair symbol identifier is out of bounds");
        }
        if (!available.add(esi)) {
            throw new InternalParsingException("found repeated available repair symbol identifier");
        }
    }


    /**
     * Internal exception used to simplify parsing code (always meant to be caught).
     */
    private static final class InternalParsingException extends Exception {

        private static final long serialVersionUID = 1L;


        InternalParsingException(String message) {

            // non-writable stack for lower exception creation cost
            super(message, null, false, false);
        }
    }
}
