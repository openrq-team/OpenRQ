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
package net.fec.openrq.util.array;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.io.BufferOperation;
import net.fec.openrq.util.io.ExtraChannels;
import net.fec.openrq.util.math.ExtraMath;


/**
 * Class with utility methods for handling I/O operations that make use of arrays.
 */
public final class ArrayIO {

    /**
     * Writes integers from an array to a channel.
     * 
     * @param ch
     *            The channel used to write the array
     * @param src
     *            An array of ints
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeInts(WritableByteChannel ch, int[] src) throws IOException {

        writeInts(ch, src, 0, src.length);
    }

    /**
     * Writes integers from an array region to a channel.
     * 
     * @param ch
     *            The channel used to write the array
     * @param src
     *            An array of ints
     * @param from
     *            The starting region index (inclusive)
     * @param to
     *            The ending region index (exclusive)
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    public static void writeInts(WritableByteChannel ch, int[] src, int from, int to) throws IOException {

        Indexables.checkFromToBounds(from, to, src.length);

        int off = from;
        while (off < to) {
            final int remaining = to - off;
            final int len = Math.min(remaining, BUFFER_SIZE / SizeOf.INT);
            writeFromArrayRegion(ch, src, off, len);
            off += len;
        }
    }

    /**
     * Reads integers from a channel to an array.
     * 
     * @param ch
     *            The channel used to read ints from
     * @param dst
     *            The array used to store the read ints
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static void readInts(ReadableByteChannel ch, int[] dst) throws IOException {

        readInts(ch, dst, 0, dst.length);
    }

    /**
     * Reads integers from a channel to an array region.
     * 
     * @param ch
     *            The channel used to read ints from
     * @param dst
     *            The array used to store the read ints
     * @param from
     *            The starting region index (inclusive)
     * @param to
     *            The ending region index (exclusive)
     * @throws IOException
     *             If an I/O error occurs while reading
     */
    public static void readInts(ReadableByteChannel ch, int[] dst, int from, int to) throws IOException {

        Indexables.checkFromToBounds(from, to, dst.length);

        int off = from;
        while (off < to) {
            final int remaining = to - off;
            final int len = Math.min(remaining, BUFFER_SIZE / SizeOf.INT);
            readToArrayRegion(ch, dst, off, len);
            off += len;
        }
    }

    /*
     * requires len <= BUFFER_SIZE / SizeOf.INT
     */
    private static void writeFromArrayRegion(WritableByteChannel ch, int[] array, int off, int len) throws IOException {

        ByteBuffer buf = getCachedBufferForChannel(ch);
        buf.limit(ExtraMath.multiplyExact(len, SizeOf.INT));

        buf.asIntBuffer().put(array, off, len); // the position of the byte buffer is not changed

        ExtraChannels.writeBytes(ch, buf);
    }

    /*
     * requires len <= BUFFER_SIZE / SizeOf.INT
     */
    private static void readToArrayRegion(ReadableByteChannel ch, int[] array, int off, int len) throws IOException {

        ByteBuffer buf = getCachedBufferForChannel(ch);
        buf.limit(ExtraMath.multiplyExact(len, SizeOf.INT));

        ExtraChannels.readBytes(ch, buf, BufferOperation.RESTORE_POSITION);

        buf.asIntBuffer().get(array, off, len);
    }

    private static ByteBuffer getCachedBufferForChannel(WritableByteChannel ch) {

        final ByteBuffer buf;

        if (ch instanceof NetworkChannel || ch instanceof FileChannel || ch instanceof SinkChannel) {
            buf = CACHED_DIRECT_BUFFER.get();
        }
        else {
            buf = CACHED_BUFFER.get();
        }

        buf.clear();
        return buf;
    }

    private static ByteBuffer getCachedBufferForChannel(ReadableByteChannel ch) {

        final ByteBuffer buf;

        if (ch instanceof NetworkChannel || ch instanceof FileChannel || ch instanceof SourceChannel) {
            buf = CACHED_DIRECT_BUFFER.get();
        }
        else {
            buf = CACHED_BUFFER.get();
        }

        buf.clear();
        return buf;
    }


    private static final int BUFFER_SIZE = 4096;
    private static final ThreadLocal<ByteBuffer> CACHED_BUFFER = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return ByteBuffer.allocate(BUFFER_SIZE);
        }
    };
    private static final ThreadLocal<ByteBuffer> CACHED_DIRECT_BUFFER = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return ByteBuffer.allocateDirect(BUFFER_SIZE);
        }
    };


    private ArrayIO() {

        // not instantiable
    }
}
