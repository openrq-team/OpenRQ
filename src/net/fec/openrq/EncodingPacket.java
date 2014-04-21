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

package net.fec.openrq;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

import net.fec.openrq.decoder.DataDecoder;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.ParameterIO;
import net.fec.openrq.util.numericaltype.SizeOf;
import net.fec.openrq.util.parsing.Parsed;


/**
 * An encoding packet containing one or more encoding symbols.
 * <p>
 * Source data is encoded by a RaptorQ encoder, which generates source and repair symbols. Those symbols are carried
 * inside encoding packets to be delivered to a RaptorQ decoder, as defined in RFC 6330.
 * <p>
 * Encoding packets contain either source symbols or repair symbols. Encoding packets also carry information that
 * identifies the symbols: a <i>FEC Payload ID</i> as defined in RFC 6330.
 * <p>
 * Instances of this class are provided by a {@link SourceBlockEncoder} object upon encoding data, and are received by a
 * {@link SourceBlockDecoder} object upon decoding data.
 * <p>
 * Methods are provided to write instances of this class to arrays of bytes, {@link ByteBuffer} objects, serializable
 * objects, {@link DataOutput} objects and {@link WritableByteChannel} objects. Additionally, static methods are
 * provided to parse/read instances of this class from arrays of bytes, {@code ByteBuffer} objects, serializable
 * objects, {@link DataInput} objects and {@link ReadableByteChannel} objects.
 * <p>
 * Note that parsing/reading requires the {@link DataDecoder} object responsible for decoding the source data associated
 * to the encoding symbols, in order to validate packets before passing them to a {@code SourceBlockDecoder} object.
 */
public abstract class EncodingPacket {

    /**
     * @param sbn
     * @param esi
     * @param symbols
     * @param numSymbols
     * @return a new source packet
     */
    static EncodingPacket newSourcePacket(int sbn, int esi, ByteBuffer symbols, int numSymbols) {

        return new SourcePacket(sbn, esi, symbols, numSymbols);
    }

    /**
     * @param sbn
     * @param esi
     * @param symbols
     * @param numSymbols
     * @return a new repair packet
     */
    static EncodingPacket newRepairPacket(int sbn, int esi, ByteBuffer symbols, int numSymbols) {

        return new RepairPacket(sbn, esi, symbols, numSymbols);
    }

    /**
     * Convenient method for parsing from a {@link DataDecoder} object an encoding packet, as defined in method
     * {@link DataDecoder#parsePacket(int, int, byte[], boolean)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
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
     *                If {@code dec} or {@code symbols} are {@code null}
     */
    public static Parsed<EncodingPacket> parsePacket(
        DataDecoder dec,
        int sbn,
        int esi,
        byte[] symbols,
        boolean copySymbols) {

        return dec.parsePacket(sbn, esi, symbols, copySymbols);
    }

    /**
     * Convenient method for parsing from a {@link DataDecoder} object an encoding packet, as defined in method
     * {@link DataDecoder#parsePacket(int, int, byte[], int, int, boolean)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
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
     *                If {@code dec} or {@code symbols} are {@code null}
     */
    public static Parsed<EncodingPacket> parsePacket(
        DataDecoder dec,
        int sbn,
        int esi,
        byte[] symbols,
        int off,
        int len,
        boolean copySymbols) {

        return dec.parsePacket(sbn, esi, symbols, off, len, copySymbols);
    }

    /**
     * Convenient method for parsing from a {@link DataDecoder} object an encoding packet, as defined in method
     * {@link DataDecoder#parsePacket(int, int, ByteBuffer, boolean)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
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
     *                If {@code dec} or {@code symbols} are {@code null}
     */
    public static Parsed<EncodingPacket> parsePacket(
        DataDecoder dec,
        int sbn,
        int esi,
        ByteBuffer symbols,
        boolean copySymbols) {

        return dec.parsePacket(sbn, esi, symbols, copySymbols);
    }

    /**
     * Convenient method for parsing from a {@link DataDecoder} object an encoding packet, as defined in method
     * {@link DataDecoder#parsePacket(SerializablePacket, boolean)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
     * @param ser
     *            A serializable packet
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            reference to the array inside the serializable packet
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code dec} or {@code ser} are {@code null}
     */
    public static Parsed<EncodingPacket> parsePacket(DataDecoder dec, SerializablePacket ser, boolean copySymbols) {

        return dec.parsePacket(ser, copySymbols);
    }

    /**
     * Convenient method for parsing from a {@link DataDecoder} object an encoding packet, as defined in method
     * {@link DataDecoder#parsePacket(byte[], boolean)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
     * @param array
     *            An array containing an encoding packet
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            reference to the array
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code dec} or {@code array} are {@code null}
     */
    public static Parsed<EncodingPacket> parsePacket(DataDecoder dec, byte[] array, boolean copySymbols) {

        return dec.parsePacket(array, copySymbols);
    }

    /**
     * Convenient method for parsing from a {@link DataDecoder} object an encoding packet, as defined in method
     * {@link DataDecoder#parsePacket(byte[], int, int, boolean)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
     * @param array
     *            An array containing an encoding packet
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
     *                If {@code dec} or {@code array} are {@code null}
     */
    public Parsed<EncodingPacket> parsePacket(DataDecoder dec, byte[] array, int off, int len, boolean copySymbols) {

        return dec.parsePacket(array, off, len, copySymbols);
    }

    /**
     * Convenient method for parsing from a {@link DataDecoder} object an encoding packet, as defined in method
     * {@link DataDecoder#parsePacket(ByteBuffer, boolean)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
     * @param buffer
     *            A buffer containing an encoding packet
     * @param copySymbols
     *            If {@code true}, a copy of the symbols data will be performed, otherwise the packet will keep a
     *            {@linkplain ByteBuffer#duplicate() duplicate} of the buffer
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @exception NullPointerException
     *                If {@code dec} or {@code buffer} are {@code null}
     */
    public static Parsed<EncodingPacket> parsePacket(DataDecoder dec, ByteBuffer buffer, boolean copySymbols) {

        return dec.parsePacket(buffer, copySymbols);
    }

    /**
     * Convenient method for reading from a {@code DataInput} object and parsing from a {@link DataDecoder} object an
     * encoding packet, as defined in method {@link DataDecoder#readPacketFrom(DataInput)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
     * @param in
     *            A {@code DataInput} object from which an encoding packet is read
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @throws IOException
     *             If an IO error occurs while reading from the {@code DataInput} object
     * @exception NullPointerException
     *                If {@code dec} or {@code in} are {@code null}
     */
    public static Parsed<EncodingPacket> readPacketFrom(DataDecoder dec, DataInput in) throws IOException {

        return dec.readPacketFrom(in);
    }

    /**
     * Convenient method for reading from a {@code ReadableByteChannel} object and parsing from a {@link DataDecoder}
     * object an encoding packet, as defined in method {@link DataDecoder#readPacketFrom(ReadableByteChannel)}.
     * 
     * @param dec
     *            A {@code DataDecoder} object from which an encoding packet is parsed
     * @param ch
     *            A {@code ReadableByteChannel} object from which an encoding packet is read
     * @return a container object containing an encoding packet or a parsing failure reason string
     * @throws IOException
     *             If an IO error occurs while reading from the {@code ReadableByteChannel} object
     * @exception NullPointerException
     *                If {@code dec} or {@code ch} are {@code null}
     */
    public static Parsed<EncodingPacket> readPacketFrom(DataDecoder dec, ReadableByteChannel ch) throws IOException {

        return dec.readPacketFrom(ch);
    }

    /**
     * Returns the source block number of all symbols in this packet.
     * 
     * @return the source block number of all symbols in this packet
     */
    public abstract int sourceBlockNumber();

    /**
     * Returns the encoding symbol identifier of the first symbol in this packet.
     * 
     * @return the encoding symbol identifier of the first symbol in this packet
     */
    public abstract int encodingSymbolID();

    /**
     * Returns a <i>FEC Payload ID</i> as defined in RFC 6330 (concatenation of the {@linkplain #sourceBlockNumber()
     * source block number} and {@linkplain #encodingSymbolID() encoding symbol identifier}).
     * 
     * @return a <i>FEC Payload ID</i> as defined in RFC 6330
     */
    public abstract int fecPayloadID();

    /**
     * Returns the number of symbols in this encoding packet. This value is the same as {@code symbols().remaining()}.
     * 
     * @return the number of symbols in this encoding packet
     */
    public abstract int numberOfSymbols();

    /**
     * Returns the type of all the symbols in this encoding packet.
     * 
     * @return the type of all the symbols in this encoding packet
     */
    public abstract SymbolType symbolType();

    /**
     * Returns the data from the symbol(s) in this packet. The symbols have contiguous <i>encoding symbol
     * identifiers</i>.
     * <p>
     * The returned buffer is {@linkplain ByteBuffer#isReadOnly() read-only}, has a {@linkplain ByteBuffer#position()
     * position} of 0, and a {@linkplain ByteBuffer#capacity() capacity} and {@linkplain ByteBuffer#limit() limit} equal
     * to the symbols data length.
     * <p>
     * Note that the symbols data length may not be a multiple of the symbol size (the last symbol may be smaller than
     * the others).
     * 
     * @return a read-only buffer with the data from the symbol(s) in this packet
     */
    public abstract ByteBuffer symbols();

    /**
     * Returns the length of the symbols data in number of bytes.
     * 
     * @return the length of the symbols data in number of bytes
     */
    public abstract int symbolsLength();

    /**
     * Returns a serializable object with the contents of this packet.
     * 
     * @return a serializable object with the contents of this packet
     */
    public abstract SerializablePacket asSerializable();

    /**
     * Returns a buffer with the contents of this packet. The buffer will contain the {@linkplain #fecPayloadID() FEC
     * payload ID}, followed by the symbols data length, followed by the symbols data itself.
     * 
     * @return a buffer with the contents of this packet
     */
    public abstract ByteBuffer asBuffer();

    /**
     * Returns an array with the contents of this packet. The array will contain the {@linkplain #fecPayloadID() FEC
     * payload ID}, followed by the symbols data length, followed by the symbols data itself.
     * 
     * @return an array with the contents of this packet
     */
    public abstract byte[] asArray();

    /**
     * Writes in the provided buffer the contents of this packet. The write consists of the {@linkplain #fecPayloadID()
     * FEC payload ID}, followed by the symbols data length, followed by the symbols data itself.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least
     * {@code (8 + symbolsLength())} bytes {@linkplain ByteBuffer#remaining() remaining}. If this method returns
     * normally, the position of the provided buffer will have advanced by {@code (8 + symbolsLength())} bytes.
     * 
     * @param buffer
     *            A buffer on which the packet contents are written
     * @exception NullPointerException
     *                If the {@code buffer} is {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than {@code (8 + symbolsLength())} bytes remaining
     */
    public abstract void writeTo(ByteBuffer buffer);

    /**
     * Writes in the provided array starting in a specific index the contents of this packet. The write consists of the
     * {@linkplain #fecPayloadID() FEC payload ID}, followed by the symbols data length, followed by the symbols data
     * itself.
     * data itself.
     * <p>
     * The provided array must have at least {@code (8 + symbolsLength())} bytes bytes between the given index and its
     * length.
     * 
     * @param array
     *            An array on which the packet contents are written
     * @param offset
     *            The starting array index at which the packet contents are written
     * @exception NullPointerException
     *                If {@code array} is {@code null}
     * @exception IndexOutOfBoundsException
     *                If the packet contents cannot be written at the given index
     */
    public abstract void writeTo(byte[] array, int offset);

    /**
     * Writes this packet directly into the provided {@code DataOutput} object. The method will write the
     * {@linkplain #fecPayloadID() FEC payload ID}, followed by the symbols data length, followed by the symbols data
     * itself.
     * <p>
     * Examples of {@code DataOutput} objects are {@link java.io.DataOutputStream DataOutputStream} and
     * {@link java.io.ObjectOutputStream ObjectOutputStream}.
     * 
     * @param out
     *            A {@code DataOutput} object into which this packet is written
     * @throws IOException
     *             If an IO error occurs while writing to the {@code DataOutput} object
     * @exception NullPointerException
     *                If {@code out} is {@code null}
     */
    public abstract void writeTo(DataOutput out) throws IOException;

    /**
     * Writes this packet directly into the provided {@code WritableByteChannel} object. The method will write the
     * {@linkplain #fecPayloadID() FEC payload ID}, followed by the symbols data length, followed by the symbols data
     * itself.
     * <p>
     * Examples of {@code WritableByteChannel} objects are {@link java.nio.channels.SocketChannel SocketChannel} and
     * {@link java.nio.channels.FileChannel FileChannel}.
     * 
     * @param ch
     *            A {@code WritableByteChannel} object into which this packet is written
     * @throws IOException
     *             If an IO error occurs while writing to the {@code WritableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    public abstract void writeTo(WritableByteChannel ch) throws IOException;

    private EncodingPacket() {

        // private constructor to prevent external sub-classing
    }


    private static abstract class AbstractEncodingPacket extends EncodingPacket {

        private final int fecPayloadID;
        private final ByteBuffer symbols;
        private final int numSymbols;


        AbstractEncodingPacket(int sbn, int esi, ByteBuffer symbols, int numSymbols) {

            this.fecPayloadID = ParameterIO.buildFECpayloadID(sbn, esi);
            this.symbols = Objects.requireNonNull(symbols);
            this.numSymbols = numSymbols;
        }

        @Override
        public int sourceBlockNumber() {

            return ParameterIO.extractSourceBlockNumber(fecPayloadID);
        }

        @Override
        public int encodingSymbolID() {

            return ParameterIO.extractEncodingSymbolID(fecPayloadID);
        }

        @Override
        public int fecPayloadID() {

            return fecPayloadID;
        }

        @Override
        public int numberOfSymbols() {

            return numSymbols;
        }

        @Override
        public ByteBuffer symbols() {

            return symbols.asReadOnlyBuffer();
        }

        @Override
        public int symbolsLength() {

            return symbols.remaining();
        }

        @Override
        public SerializablePacket asSerializable() {

            // cannot use the field directly because the position of the buffer will be changed
            final ByteBuffer symbolsBuf = symbols();
            final byte[] symbolsArr = new byte[symbolsLength()];
            symbolsBuf.get(symbolsArr);

            return new SerializablePacket(fecPayloadID, symbolsArr);
        }

        @Override
        public ByteBuffer asBuffer() {

            // cannot use the field directly because the position of the buffer will be changed
            final ByteBuffer symbolsBuf = symbols();
            final ByteBuffer buffer = ByteBuffer.allocate(SizeOf.INT + SizeOf.INT + symbolsLength());
            buffer.putInt(fecPayloadID).putInt(symbolsLength()).put(symbolsBuf);
            buffer.flip();

            return buffer;
        }

        @Override
        public byte[] asArray() {

            // cannot use the field directly because the position of the buffer will be changed
            final ByteBuffer symbolsBuf = symbols();
            final byte[] array = new byte[SizeOf.INT + SizeOf.INT + symbolsLength()];
            ByteBuffer.wrap(array).putInt(fecPayloadID).putInt(symbolsLength()).put(symbolsBuf);

            return array;
        }

        @Override
        public void writeTo(ByteBuffer buffer) {

            buffer.putInt(fecPayloadID);
            buffer.putInt(symbolsLength());
            buffer.put(symbols());
        }

        @Override
        public void writeTo(byte[] array, int offset) {

            final int arraySize = SizeOf.INT + SizeOf.INT + symbolsLength();
            if (offset < 0 || array.length - offset < arraySize) throw new IndexOutOfBoundsException();
            writeTo(ByteBuffer.wrap(array, offset, arraySize));
        }

        @Override
        public void writeTo(DataOutput out) throws IOException {

            final byte[] symbolsArr;
            final int symbolsOff;

            if (symbols.hasArray()) {
                symbolsArr = symbols.array();
                symbolsOff = symbols.position() + symbols.arrayOffset();
            }
            else {
                // cannot use the field directly because the position of the buffer will be changed
                final ByteBuffer symbolsBuf = symbols();
                symbolsArr = new byte[symbolsLength()];
                symbolsBuf.get(symbolsArr);
                symbolsOff = 0;
            }

            out.writeInt(fecPayloadID);
            out.writeInt(symbolsLength());
            out.write(symbolsArr, symbolsOff, symbolsLength());
        }

        @Override
        public void writeTo(WritableByteChannel ch) throws IOException {

            // cannot use the field directly because the position of the buffer will be changed
            final ByteBuffer symbolsBuf = symbols();
            final ByteBuffer intsBuf = ByteBuffer.allocate(SizeOf.INT + SizeOf.INT);
            intsBuf.putInt(fecPayloadID).putInt(symbolsLength());
            intsBuf.flip();

            while (intsBuf.hasRemaining()) {
                ch.write(intsBuf);
            }
            while (symbolsBuf.hasRemaining()) {
                ch.write(symbolsBuf);
            }
        }
    }

    private static final class SourcePacket extends AbstractEncodingPacket {

        SourcePacket(int sbn, int esi, ByteBuffer symbols, int numSymbols) {

            super(sbn, esi, symbols, numSymbols);
        }

        @Override
        public SymbolType symbolType() {

            return SymbolType.SOURCE;
        }
    }

    private static final class RepairPacket extends AbstractEncodingPacket {

        RepairPacket(int sbn, int esi, ByteBuffer symbols, int numSymbols) {

            super(sbn, esi, symbols, numSymbols);
        }

        @Override
        public SymbolType symbolType() {

            return SymbolType.REPAIR;
        }
    }
}
