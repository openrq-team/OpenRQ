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
package net.fec.openrq.decoder;


import java.io.DataInput;
import java.io.DataOutput;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import net.fec.openrq.util.numericaltype.SizeOf;


/**
 * This class represents information about a source block decoder that can be conveyed to an encoder, in order to help
 * decide which symbols are prioritized for encoding.
 * <p>
 * When a back communication channel is available from a receiver to a sender of data, information about a specific
 * source block may be useful in determining how the source block is encoded by the sender. For example, such
 * information may contain a list with the identifiers of the missing source symbols at the receiver, so that a sender
 * may transmit those missing symbols.
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

    private final int sbn;
    private final SourceBlockState state;
    private final Set<Integer> missingSourceSymbols;
    private final Set<Integer> availableRepairSymbols;


    SBDInfo(
        int sbn,
        SourceBlockState state,
        Set<Integer> missingSourceSymbols,
        Set<Integer> availableRepairSymbols)
    {

        this.sbn = sbn;
        this.state = Objects.requireNonNull(state);
        this.missingSourceSymbols = new LinkedHashSet<>(missingSourceSymbols);
        this.availableRepairSymbols = new LinkedHashSet<>(availableRepairSymbols);
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
     * Returns a set of integers containing the encoding symbol identifiers of the missing source symbols from the
     * source block being decoded.
     * 
     * @return a set of encoding symbol identifiers of missing source symbols
     */
    public Set<Integer> missingSourceSymbols() {

        return missingSourceSymbols;
    }

    /**
     * Returns a set of integers containing the encoding symbol identifiers of the available repair symbols for
     * decoding.
     * 
     * @return a set of encoding symbol identifiers of available repair symbols
     */
    public Set<Integer> availableRepairSymbols() {

        return availableRepairSymbols;
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
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()}
     * <li>{@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()}
     * </ul>
     * The array will contain the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * 
     * @return an array with this information encoded in a compact format
     */
    public byte[] asArray() {

        return null;
    }

    /**
     * Writes in the provided array starting at index zero this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()}
     * <li>{@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()}
     * </ul>
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
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()}
     * <li>{@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()}
     * </ul>
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

        final int arraySize = getEncodedArraySize();
        if (offset < 0 || array.length - offset < arraySize) throw new IndexOutOfBoundsException();
        writeTo(ByteBuffer.wrap(array, offset, arraySize));
    }

    /**
     * Returns a buffer with this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()}
     * <li>{@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()}
     * </ul>
     * The buffer will contain the {@linkplain #sourceBlockNumber() source block number}, followed by a byte value
     * corresponding to the {@linkplain #state() latest state of the source block}, followed by
     * {@code NUM_MISSING_BYTES}, followed by the list of the encoding symbol identifiers (ESIs) of the
     * {@linkplain #missingSourceSymbols() missing source symbols}, followed by {@code NUM_AVAILABLE_BYTES}, followed by
     * the list of the ESIs of the {@linkplain #availableRepairSymbols() available repair symbols}.
     * 
     * @return a buffer with this information encoded in a compact format
     */
    public ByteBuffer asBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(getEncodedArraySize());
        writeTo(buffer);
        buffer.flip();

        return buffer;
    }

    /**
     * Writes in the provided buffer this information encoded in a compact format.
     * <p>
     * Let:
     * <ul>
     * <li>{@code NUM_MISSING_BYTES} = {@code 2} &times; {@link #missingSourceSymbols()}{@code .size()}
     * <li>{@code NUM_AVAILABLE_BYTES} = {@code 3} &times; {@link #availableRepairSymbols()}{@code .size()}
     * </ul>
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
     *            A buffer on which the packet contents are written
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than {@code (8 + symbolsLength())} bytes remaining
     * @exception NullPointerException
     *                If the {@code buffer} is {@code null}
     */
    public void writeTo(ByteBuffer buffer) {

        
    }

    private int getEncodedArraySize() {

        final int numMissBytes = SizeOf.SHORT * missingSourceSymbols.size();
        final int numAvaBytes = SizeOf.UNSIGNED_3_BYTES * availableRepairSymbols.size();
        return SizeOf.BYTE + SizeOf.BYTE + SizeOf.SHORT + numMissBytes + SizeOf.UNSIGNED_3_BYTES + numAvaBytes;
    }
}
