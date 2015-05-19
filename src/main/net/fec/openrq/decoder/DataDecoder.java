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
package net.fec.openrq.decoder;


import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import net.fec.openrq.EncodingPacket;
import net.fec.openrq.Parsed;
import net.fec.openrq.SerializablePacket;
import net.fec.openrq.parameters.FECParameters;


/**
 * A RaptorQ decoder for a data object.
 * <p>
 * A decoder receives encoding packets produced by a RaptorQ encoder (as specified by RFC 6330), and decodes the
 * original data object (source data). The source data is divided into a fixed number of source blocks, and each source
 * block can be decoded independently.
 * <p>
 * A decoder provides methods for parsing/reading encoding packets from various formats. The parsed/read encoding
 * packets must then be forwarded to the right {@link SourceBlockDecoder} object, which takes care of decoding a
 * specific source block. These decoder objects are accessed via the method {@link #sourceBlock(int)}.
 * <p>
 * The number of source blocks, the length of the source data and other parameters are specified as the
 * <em>FEC parameters</em>. The method {@link #fecParameters()} provides the associated parameters to the decoder.
 * <p>
 */
public interface DataDecoder {

    /**
     * Returns the FEC parameters associated to this decoder.
     * 
     * @return the FEC parameters associated to this decoder
     */
    public FECParameters fecParameters();

    /**
     * Returns the length of the source data, in number of bytes. This value is the one returned by
     * {@code this.fecParameters().dataLength()}.
     * 
     * @return the length of the data, in number of bytes
     */
    public long dataLength();

    /**
     * Returns the size of a symbol, in number of bytes. This value is the one returned by
     * {@code this.fecParameters().symbolSize()}.
     * 
     * @return the size of a symbol, in number of bytes
     */
    public int symbolSize();

    /**
     * Returns the number of source blocks into which the source data is partitioned. This value is the one returned by
     * {@code this.fecParameters().numberOfSourceBlocks()}.
     * 
     * @return the number of source blocks
     */
    public int numberOfSourceBlocks();

    /**
     * Returns {@code true} if, and only if, the original data is fully decoded. The original data is considered fully
     * decoded when every source block is fully decoded.
     * 
     * @return {@code true} if, and only if, the original data is fully decoded
     */
    public boolean isDataDecoded();

    /**
     * Returns a decoder object for the source block with the provided source block number.
     * <p>
     * <b><em>Bounds checking</em></b> - If we have {@code Z} as the number of source blocks into which is divided the
     * the source data being decoded, then the following must be true, otherwise an {@code IllegalArgumentException} is
     * thrown:
     * <ul>
     * <li>{@code sbn} &ge; 0
     * <li>{@code sbn} &lt; {@code Z} </ul>
     * 
     * @param sbn
     *            A source block number
     * @return a decoder object for a specific source block
     * @exception IllegalArgumentException
     *                If the provided source block number is invalid
     * @see #numberOfSourceBlocks()
     */
    public SourceBlockDecoder sourceBlock(int sbn);

    /**
     * Returns a new iterable over all source block decoders. The resulting iterable can be iterated using a "foreach"
     * loop.
     * 
     * @return a new iterable over all source block decoders
     */
    public Iterable<SourceBlockDecoder> sourceBlockIterable();

    /**
     * Parses an encoding packet from the given source block number, encoding symbol identifier of the first symbol, and
     * symbols data.
     * <p>
     * The symbols data will be read, in the array, from position {@code 0} inclusive to position {@code symbols.length}
     * exclusive.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param sbn
     *            The common source block number of all symbols in the packet
     * @param esi
     *            The encoding symbol identifier of the first symbol in the packet
     * @param symbols
     *            An array of bytes containing the symbols data
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            reference to the array
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code symbols} is {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(int sbn, int esi, byte[] symbols, boolean copySymbols);

    /**
     * Parses an encoding packet from the given source block number, encoding symbol identifier of the first symbol, and
     * symbols data.
     * <p>
     * The symbols data will be read, in the array, from position {@code off} inclusive to position {@code (off + len)}
     * exclusive.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param sbn
     *            The common source block number of all symbols in the packet
     * @param esi
     *            The encoding symbol identifier of the first symbol in the packet
     * @param symbols
     *            An array of bytes containing the symbols data
     * @param off
     *            The starting index in the array (must be non-negative)
     * @param len
     *            The length of the symbols data (must be non-negative and no larger than {@code symbols.length - off})
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            reference to the array
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception IndexOutOfBoundsException
     *                If the pre-conditions on the array offset and length do not hold
     * @exception NullPointerException
     *                If {@code symbols} is {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(int sbn, int esi, byte[] symbols, int off, int len, boolean copySymbols);

    /**
     * Parses an encoding packet from the given source block number, encoding symbol identifier of the first symbol, and
     * symbols data.
     * <p>
     * The symbols data will be read, in the buffer, from the current {@linkplain ByteBuffer#position() position}
     * inclusive to the current {@linkplain ByteBuffer#limit() limit} exclusive. If the parsing succeeds, the position
     * of the buffer will have been advanced to the limit.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param sbn
     *            The common source block number of all symbols in the packet
     * @param esi
     *            The encoding symbol identifier of the first symbol in the packet
     * @param symbols
     *            A buffer containing the symbols data
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            {@linkplain ByteBuffer#duplicate() duplicate} of the buffer
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code symbols} is {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(int sbn, int esi, ByteBuffer symbols, boolean copySymbols);

    /**
     * Parses an encoding packet from the given serializable packet.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param serPac
     *            A serializable packet
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            reference to the array inside the serializable packet
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code ser} is {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(SerializablePacket serPac, boolean copySymbols);

    /**
     * Parses an encoding packet from the given array. The packet bytes in the array must follow the format specified by
     * {@link EncodingPacket#asArray()}.
     * <p>
     * The encoding packet will be read, in the array, from position {@code 0} inclusive to position
     * {@code array.length} exclusive.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param array
     *            An array of bytes containing an encoding packet
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            reference to the array
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code array} is {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(byte[] array, boolean copySymbols);

    /**
     * Parses an encoding packet from the given array. The packet bytes in the array must follow the format specified by
     * {@link EncodingPacket#asArray()}.
     * <p>
     * The encoding packet will be read, in the array, from position {@code off} inclusive to position
     * {@code (off + len)} exclusive.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param array
     *            An array of bytes containing an encoding packet
     * @param off
     *            The starting index in the array (must be non-negative)
     * @param len
     *            The length of the encoding packet (must be non-negative and no larger than {@code array.length - off})
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            reference to the array
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception IndexOutOfBoundsException
     *                If the pre-conditions on the array offset and length do not hold
     * @exception NullPointerException
     *                If {@code array} is {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(byte[] array, int off, int len, boolean copySymbols);

    /**
     * Parses an encoding packet from the given buffer. The packet bytes in the buffer must follow the format specified
     * by {@link EncodingPacket#asBuffer()}.
     * <p>
     * The encoding packet will be read, in the buffer, from the current {@linkplain ByteBuffer#position() position}
     * inclusive to the current {@linkplain ByteBuffer#limit() limit} exclusive. If the parsing succeeds, the position
     * of the buffer will have been advanced by the number of bytes read.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * 
     * @param buffer
     *            A buffer containing an encoding packet
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            {@linkplain ByteBuffer#duplicate() duplicate} of the buffer
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code buffer} is {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(ByteBuffer buffer, boolean copySymbols);

    /**
     * Reads and parses an encoding packet from a {@code DataInput} object. The read packet bytes must follow the format
     * specified by {@link EncodingPacket#writeTo(java.io.DataOutput)}.
     * <p>
     * Examples of {@code DataInput} objects are {@link java.io.DataInputStream DataInputStream} and
     * {@link java.io.ObjectInputStream ObjectInputStream}.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until a whole packet is read from the input, or a parsing
     * failure is detected, or an {@code IOException} is throw.
     * 
     * @param in
     *            A {@code DataInput} object from which an encoding packet is read
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code DataInput} object
     * @exception NullPointerException
     *                If {@code in} is {@code null}
     */
    public Parsed<EncodingPacket> readPacketFrom(DataInput in) throws IOException;

    /**
     * Reads and parses an encoding packet from a {@code ReadableByteChannel} object. The read packet bytes must follow
     * the format specified by {@link EncodingPacket#writeTo(java.nio.channels.WritableByteChannel)}.
     * <p>
     * Examples of {@code ReadableByteChannel} objects are {@link java.nio.channels.SocketChannel SocketChannel} and
     * {@link java.nio.channels.FileChannel FileChannel}.
     * <p>
     * The returned container object indicates if the parsing succeeded or failed:
     * <ul>
     * <li>If the parsing succeeded, the encoding packet can be retrieved by calling the method {@link Parsed#value()}
     * <li>If the parsing failed, the container object will be {@linkplain Parsed#isValid() invalid} and the reason for
     * the parsing failure can be retrieved by calling the method {@link Parsed#failureReason()} </ul>
     * <p>
     * <b><em>Blocking behavior</em></b>: this method blocks until a whole packet is read from the channel, or a parsing
     * failure is detected, or an {@code IOException} is throw.
     * 
     * @param ch
     *            A {@code ReadableByteChannel} object from which an encoding packet is read
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code ReadableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    public Parsed<EncodingPacket> readPacketFrom(ReadableByteChannel ch) throws IOException;
}

