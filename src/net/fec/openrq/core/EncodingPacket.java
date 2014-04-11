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

package net.fec.openrq.core;


import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

import net.fec.openrq.core.parameters.ParameterIO;
import net.fec.openrq.core.util.numericaltype.SizeOf;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public abstract class EncodingPacket {

    /**
     * @param sbn
     * @param esi
     * @param symbols
     * @param numSymbols
     * @return
     */
    static EncodingPacket newSourcePacket(int sbn, int esi, ByteBuffer symbols, int numSymbols) {

        return new SourcePacket(sbn, esi, symbols, numSymbols);
    }

    /**
     * @param sbn
     * @param esi
     * @param symbols
     * @param numSymbols
     * @return
     */
    static EncodingPacket newRepairPacket(int sbn, int esi, ByteBuffer symbols, int numSymbols) {

        return new RepairPacket(sbn, esi, symbols, numSymbols);
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
     * Returns a concatenation of the {@linkplain #sourceBlockNumber() SBN} and {@linkplain #encodingSymbolID() ESI} as
     * defined by the <i>FEC Payload ID</i> in RFC 6330.
     * 
     * @return a concatenation of the SBN and ESI
     */
    public abstract int fecPayloadID();

    /**
     * Returns the number of symbols in this encoding packet. This value is the same as {@code getSymbolData().size()}.
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
     * to the size of the data from the symbol(s).
     * <p>
     * Note that the size of the data may not be a multiple of the symbol size (the last symbol may be smaller than the
     * others).
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
     * Returns a serializable instance with the contents of this packet.
     * 
     * @return a serializable instance with the contents of this packet
     */
    public abstract SerializablePacket asSerializable();

    /**
     * Returns a buffer with the contents of this packet. The buffer will contain the {@linkplain #fecPayloadID() FEC
     * payload ID}, followed by the size of the symbols data, followed by the symbols data itself.
     * 
     * @return a buffer with the contents of this packet
     */
    public abstract ByteBuffer asBuffer();

    /**
     * Returns an array with the contents of this packet. The array will contain the {@linkplain #fecPayloadID() FEC
     * payload ID}, followed by the size of the symbols data, followed by the symbols data itself.
     * 
     * @return an array with the contents of this packet
     */
    public abstract byte[] asArray();

    /**
     * Writes in the provided buffer the contents of this packet. The write consists of the {@linkplain #fecPayloadID()
     * FEC payload ID}, followed by the size of the symbols data, followed by the symbols data itself.
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
     * {@linkplain #fecPayloadID() FEC payload ID}, followed by the size of the symbols data, followed by the symbols
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
     * {@linkplain #fecPayloadID() FEC payload ID}, followed by the size of the symbols data, followed by the symbols
     * data itself.
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
     * {@linkplain #fecPayloadID() FEC payload ID}, followed by the size of the symbols data, followed by the symbols
     * data itself.
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
