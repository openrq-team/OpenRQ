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
package net.fec.openrq.util.io;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import net.fec.openrq.util.text.Words;


/**
 * Class containing utility methods not present in class {@link java.nio.channels.Channels}.
 */
public final class ExtraChannels {

    public enum BufferOperation {

        /**
         * Does nothing to the buffer after reading into/from it.
         * <p>
         * <ul>
         * <li>The buffer position will be the index immediately after the last read byte, or 0 if nothing was read.
         * <li>The buffer limit will be the same as before reading.
         * </ul>
         */
        DO_NOTHING,

        /**
         * Restore the original buffer position after reading into/from it.
         * <p>
         * <ul>
         * <li>The buffer position will be the same as before reading.
         * <li>The buffer limit will be the same as before reading.
         * </ul>
         */
        RESTORE_POSITION,

        /**
         * Flips the buffer relatively after reading into/from it.
         * <p>
         * <ul>
         * <li>The buffer position will be the same as before reading.
         * <li>The buffer limit will be the index immediately after the last read byte, or 0 if nothing was read.
         * </ul>
         */
        FLIP_RELATIVELY,

        /**
         * Flips the buffer absolutely after reading into/from it (the same as calling the method
         * {@link ByteBuffer#flip()} afterwards).
         * <p>
         * <ul>
         * <li>The buffer position will be 0.
         * <li>The buffer limit will be the index immediately after the last read byte, or 0 if nothing was read.
         * </ul>
         */
        FLIP_ABSOLUTELY
    }


    /**
     * Reads a number of bytes from the provided channel into the provided buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #readBytes(ReadableByteChannel, ByteBuffer, int, BufferOperation) readBytes(ch, buf, numBytes,
     * BufferOperation.DO_NOTHING)}.
     * 
     * @param ch
     *            The channel to read bytes from
     * @param buf
     *            The buffer to store the read bytes
     * @param numBytes
     *            The numeclipse-open:%E2%98%82=OpenRQ/src%5C/main%3Cnet.fec.openrq.util.io%7BExtraChannels.java%E2%98%
     *            83ExtraChannels~readBytes~QReadableByteChannel;~QByteBuffer;~Iber of bytes to read
     * @throws IOException
     *             If an IO error occurs while reading
     * @exception IllegalArgumentException
     *                If the provided buffer does not have {@code numBytes} bytes available for storage
     */
    public static void readBytes(ReadableByteChannel ch, ByteBuffer buf, int numBytes)
        throws IOException
    {

        readBytes(ch, buf, numBytes, BufferOperation.DO_NOTHING);
    }

    /**
     * Reads a number of bytes from the provided channel into the provided buffer.
     * 
     * @param ch
     *            The channel to read bytes from
     * @param buf
     *            The buffer to store the read bytes
     * @param numBytes
     *            The number of bytes to read
     * @param op
     *            The operation to apply to the provided buffer after reading
     * @throws IOException
     *             If an IO error occurs while reading
     * @exception IllegalArgumentException
     *                If the provided buffer does not have {@code numBytes} bytes available for storage
     */
    public static void readBytes(ReadableByteChannel ch, ByteBuffer buf, int numBytes, BufferOperation op)
        throws IOException
    {

        final int bufPos = buf.position();
        final int bufLim = buf.limit();
        final int remaining = bufLim - bufPos;
        if (remaining < numBytes) {
            throw new IllegalArgumentException(
                "buffer cannot hold " + Words.bytes(numBytes) +
                    " (only " + Words.bytes(remaining) + " are available)");
        }

        try {
            buf.limit(bufPos + numBytes);
            while (buf.hasRemaining()) {
                ch.read(buf);
            }
        }
        finally {
            // NOTE: the buffer limit is equal to the current buffer position
            switch (op) {
                case DO_NOTHING: // except restoring the original limit
                    buf.limit(bufLim);
                break;

                case RESTORE_POSITION:
                    buf.limit(bufLim);
                    buf.position(bufPos);
                break;

                case FLIP_RELATIVELY:
                    buf.position(bufPos);
                break;

                case FLIP_ABSOLUTELY:
                    buf.position(0);
                break;

                default:
                    throw new AssertionError("unknown enum value");
            }
        }
    }

    private ExtraChannels() {

        // not instantiable
    }
}
