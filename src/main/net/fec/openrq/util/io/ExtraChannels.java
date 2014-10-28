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
package net.fec.openrq.util.io;


import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

import net.fec.openrq.util.datatype.SizeOf;


/**
 * Class containing utility methods not present in class {@link java.nio.channels.Channels}.
 */
public final class ExtraChannels {

    /**
     * Writes to a channel all the available bytes from a buffer. The buffer will be consumed from its position to its
     * limit.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #writeBytes(WritableByteChannel, ByteBuffer, int, BufferOperation) writeBytes(ch, buf, buf.remaining(),
     * BufferOperation.ADVANCE_POSITION)}
     * 
     * @param ch
     *            The channel used to write bytes
     * @param buf
     *            The buffer containing the bytes to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeBytes(WritableByteChannel ch, ByteBuffer buf) throws IOException {

        writeBytes(ch, buf, buf.remaining(), BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Writes to a channel a specific number of bytes from a buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #writeBytes(WritableByteChannel, ByteBuffer, int, BufferOperation) writeBytes(ch, buf, numBytes,
     * BufferOperation.ADVANCE_POSITION)}
     * 
     * @param ch
     *            The channel used to write bytes
     * @param buf
     *            The buffer containing the bytes to be written
     * @param numBytes
     *            The number of bytes to write
     * @throws IOException
     *             If an I/O error occurs while writing
     * @exception IllegalArgumentException
     *                If {@code numBytes} is negative
     * @exception BufferUnderflowException
     *                If the provided buffer does not have at least {@code numBytes} bytes available to write
     */
    public static void writeBytes(WritableByteChannel ch, ByteBuffer buf, int numBytes) throws IOException {

        writeBytes(ch, buf, numBytes, BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Writes to a channel all the available bytes from a buffer. The buffer will be consumed from its position to its
     * limit.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #writeBytes(WritableByteChannel, ByteBuffer, int, BufferOperation) writeBytes(ch, buf, buf.remaining(),
     * op)}
     * 
     * @param ch
     *            The channel used to write bytes
     * @param buf
     *            The buffer containing the bytes to be written
     * @param op
     *            The operation to apply to the provided buffer after writing
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeBytes(WritableByteChannel ch, ByteBuffer buf, BufferOperation op) throws IOException {

        writeBytes(ch, buf, buf.remaining(), op);
    }

    /**
     * Writes to a channel a specific number of bytes from a buffer.
     * 
     * @param ch
     *            The channel used to write bytes
     * @param buf
     *            The buffer containing the bytes to be written
     * @param numBytes
     *            The number of bytes to write
     * @param op
     *            The operation to apply to the provided buffer after writing
     * @throws IOException
     *             If an I/O error occurs while writing
     * @exception IllegalArgumentException
     *                If {@code numBytes} is negative
     * @exception BufferUnderflowException
     *                If the provided buffer does not have at least {@code numBytes} bytes available to write
     */
    public static void writeBytes(WritableByteChannel ch, ByteBuffer buf, int numBytes, BufferOperation op)
        throws IOException
    {

        final int bufPos = buf.position();
        final int bufLim = buf.limit();
        final int remaining = bufLim - bufPos;

        if (numBytes < 0) throw new IllegalArgumentException("number of bytes is negative");
        if (remaining < numBytes) throw new BufferUnderflowException();
        Objects.requireNonNull(op);

        try {
            buf.limit(bufPos + numBytes);
            writeFully(ch, buf);
        }
        finally {
            buf.limit(bufLim); // always restore the original limit
        }

        // only apply the operation if no exception was previously thrown
        op.apply(buf, bufPos, buf.position());
    }

    /**
     * Writes a single byte to a channel.
     * 
     * @param ch
     *            The channel used to write a byte
     * @param b
     *            The byte value to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeByte(WritableByteChannel ch, byte b) throws IOException {

        ByteBuffer buf = tinyBuffer();
        buf.put(0, b).limit(SizeOf.BYTE); // position remains at 0
        writeFully(ch, buf);
    }

    /**
     * Writes a single character to a channel.
     * 
     * @param ch
     *            The channel used to write a char
     * @param c
     *            The char value to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeChar(WritableByteChannel ch, char c) throws IOException {

        ByteBuffer buf = tinyBuffer();
        buf.putChar(0, c).limit(SizeOf.CHAR); // position remains at 0
        writeFully(ch, buf);
    }

    /**
     * Writes a single short integer to a channel.
     * 
     * @param ch
     *            The channel used to write a short
     * @param s
     *            The short value to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeShort(WritableByteChannel ch, short s) throws IOException {

        ByteBuffer buf = tinyBuffer();
        buf.putShort(0, s).limit(SizeOf.SHORT); // position remains at 0
        writeFully(ch, buf);
    }

    /**
     * Writes a single integer to a channel.
     * 
     * @param ch
     *            The channel used to write an int
     * @param i
     *            The int value to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeInt(WritableByteChannel ch, int i) throws IOException {

        ByteBuffer buf = tinyBuffer();
        buf.putInt(0, i).limit(SizeOf.INT); // position remains at 0
        writeFully(ch, buf);
    }

    /**
     * Writes a single long integer to a channel.
     * 
     * @param ch
     *            The channel used to write a long
     * @param eL
     *            The long value to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeLong(WritableByteChannel ch, long eL) throws IOException {

        ByteBuffer buf = tinyBuffer();
        buf.putLong(0, eL).limit(SizeOf.LONG); // position remains at 0
        writeFully(ch, buf);
    }

    /**
     * Writes a single float to a channel.
     * 
     * @param ch
     *            The channel used to write a float
     * @param f
     *            The float value to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeFloat(WritableByteChannel ch, float f) throws IOException {

        ByteBuffer buf = tinyBuffer();
        buf.putFloat(0, f).limit(SizeOf.FLOAT); // position remains at 0
        writeFully(ch, buf);
    }

    /**
     * Writes a single double to a channel.
     * 
     * @param ch
     *            The channel used to write a double
     * @param d
     *            The double value to be written
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeDouble(WritableByteChannel ch, double d) throws IOException {

        ByteBuffer buf = tinyBuffer();
        buf.putDouble(0, d).limit(SizeOf.DOUBLE); // position remains at 0
        writeFully(ch, buf);
    }

    /**
     * Reads into a buffer bytes from a channel. The buffer will be filled from its position to its limit.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #readBytes(ReadableByteChannel, ByteBuffer, int, BufferOperation) readBytes(ch, buf, buf.remaining(),
     * BufferOperation.ADVANCE_POSITION)}
     * 
     * @param ch
     *            The channel used to read bytes from
     * @param buf
     *            The buffer used to store the read bytes
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static void readBytes(ReadableByteChannel ch, ByteBuffer buf) throws EOFException, IOException {

        readBytes(ch, buf, buf.remaining(), BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Reads into a buffer a specific number of bytes from a channel.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #readBytes(ReadableByteChannel, ByteBuffer, int, BufferOperation) readBytes(ch, buf, numBytes,
     * BufferOperation.ADVANCE_POSITION)}
     * 
     * @param ch
     *            The channel used to read bytes from
     * @param buf
     *            The buffer used to store the read bytes
     * @param numBytes
     *            The number of bytes to read
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     * @exception IllegalArgumentException
     *                If {@code numBytes} is negative
     * @exception BufferOverflowException
     *                If the provided buffer does not have at least {@code numBytes} bytes available for storage
     */
    public static void readBytes(ReadableByteChannel ch, ByteBuffer buf, int numBytes)
        throws EOFException, IOException
    {

        readBytes(ch, buf, numBytes, BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Reads into a buffer bytes from a channel. The buffer will be filled from its position to its limit.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #readBytes(ReadableByteChannel, ByteBuffer, int, BufferOperation) readBytes(ch, buf, buf.remaining(), op)}
     * 
     * @param ch
     *            The channel used to read bytes from
     * @param buf
     *            The buffer used to store the read bytes
     * @param op
     *            The operation to apply to the provided buffer after reading
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static void readBytes(ReadableByteChannel ch, ByteBuffer buf, BufferOperation op)
        throws EOFException, IOException
    {

        readBytes(ch, buf, buf.remaining(), op);
    }

    /**
     * Reads into a buffer a specific number of bytes from a channel.
     * 
     * @param ch
     *            The channel used to read bytes from
     * @param buf
     *            The buffer used to store the read bytes
     * @param numBytes
     *            The number of bytes to read
     * @param op
     *            The operation to apply to the provided buffer after reading
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     * @exception IllegalArgumentException
     *                If {@code numBytes} is negative
     * @exception BufferOverflowException
     *                If the provided buffer does not have at least {@code numBytes} bytes available for storage
     */
    public static void readBytes(ReadableByteChannel ch, ByteBuffer buf, int numBytes, BufferOperation op)
        throws EOFException, IOException
    {

        final int bufPos = buf.position();
        final int bufLim = buf.limit();
        final int remaining = bufLim - bufPos;

        if (numBytes < 0) throw new IllegalArgumentException("number of bytes is negative");
        if (remaining < numBytes) throw new BufferOverflowException();
        Objects.requireNonNull(op);

        try {
            buf.limit(bufPos + numBytes);
            readFully(ch, buf);
        }
        finally {
            buf.limit(bufLim); // always restore the original limit
        }

        // only apply the operation if no exception was previously thrown
        op.apply(buf, bufPos, buf.position());
    }

    /**
     * Reads a single byte from a channel.
     * 
     * @param ch
     *            The channel used to read the byte value
     * @return a byte value
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static byte readByte(ReadableByteChannel ch) throws EOFException, IOException {

        ByteBuffer buf = tinyBuffer();
        buf.limit(SizeOf.BYTE);
        readFully(ch, buf);
        return buf.get(0); // ignores current position
    }

    /**
     * Reads a single character from a channel.
     * 
     * @param ch
     *            The channel used to read the char value
     * @return a char value
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static char readChar(ReadableByteChannel ch) throws EOFException, IOException {

        ByteBuffer buf = tinyBuffer();
        buf.limit(SizeOf.CHAR);
        readFully(ch, buf);
        return buf.getChar(0); // ignores current position
    }

    /**
     * Reads a single short integer from a channel.
     * 
     * @param ch
     *            The channel used to read the short value
     * @return a short value
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static short readShort(ReadableByteChannel ch) throws EOFException, IOException {

        ByteBuffer buf = tinyBuffer();
        buf.limit(SizeOf.SHORT);
        readFully(ch, buf);
        return buf.getShort(0); // ignores current position
    }

    /**
     * Reads a single integer from a channel.
     * 
     * @param ch
     *            The channel used to read the int value
     * @return an int value
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static int readInt(ReadableByteChannel ch) throws EOFException, IOException {

        ByteBuffer buf = tinyBuffer();
        buf.limit(SizeOf.INT);
        readFully(ch, buf);
        return buf.getInt(0); // ignores current position
    }

    /**
     * Reads a single long integer from a channel.
     * 
     * @param ch
     *            The channel used to read the long value
     * @return a long value
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static long readLong(ReadableByteChannel ch) throws EOFException, IOException {

        ByteBuffer buf = tinyBuffer();
        buf.limit(SizeOf.LONG);
        readFully(ch, buf);
        return buf.getLong(0); // ignores current position
    }

    /**
     * Reads a single float from a channel.
     * 
     * @param ch
     *            The channel used to read the float value
     * @return a float value
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static float readFloat(ReadableByteChannel ch) throws EOFException, IOException {

        ByteBuffer buf = tinyBuffer();
        buf.limit(SizeOf.FLOAT);
        readFully(ch, buf);
        return buf.getFloat(0); // ignores current position
    }

    /**
     * Reads a single double from a channel.
     * 
     * @param ch
     *            The channel used to read the double value
     * @return a double value
     * @throws EOFException
     *             If the channel has reached end-of-stream
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static double readDouble(ReadableByteChannel ch) throws EOFException, IOException {

        ByteBuffer buf = tinyBuffer();
        buf.limit(SizeOf.DOUBLE);
        readFully(ch, buf);
        return buf.getDouble(0); // ignores current position
    }

    private static void writeFully(WritableByteChannel ch, ByteBuffer buf) throws IOException {

        while (buf.hasRemaining()) {
            ch.write(buf);
        }
    }

    private static void readFully(ReadableByteChannel ch, ByteBuffer buf) throws EOFException, IOException {

        while (buf.hasRemaining()) {
            if (ch.read(buf) == -1) {
                throw new EOFException();
            }
        }
    }


    /**
     * Using a thread local of a tiny buffer is more efficient than allocating the buffer each time.
     */
    private static final ThreadLocal<ByteBuffer> TINY_BUFFER_CACHE = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return ByteBuffer.allocateDirect(SizeOf.LONG);
        }
    };


    private static ByteBuffer tinyBuffer() {

        ByteBuffer buf = TINY_BUFFER_CACHE.get();
        buf.clear();
        return buf;
    }

    private ExtraChannels() {

        // not instantiable
    }
}
