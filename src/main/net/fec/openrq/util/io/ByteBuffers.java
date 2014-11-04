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


import static net.fec.openrq.util.io.ByteBuffers.BufferType.ARRAY_BACKED;
import static net.fec.openrq.util.io.ByteBuffers.BufferType.DIRECT;

import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

import net.fec.openrq.util.checking.Indexables;


/**
 * Class containing utility methods for {@link ByteBuffer} objects.
 */
public final class ByteBuffers {

    public static enum BufferType {

        ARRAY_BACKED,
        DIRECT;
    }


    private static BufferType isDirectType(boolean isDirect) {

        return isDirect ? DIRECT : ARRAY_BACKED;
    }

    /**
     * Allocates a new buffer.
     * 
     * @param capacity
     *            The capacity of the new buffer
     * @param type
     *            The type of the returned buffer
     * @return a new allocated buffer
     */
    public static ByteBuffer allocate(int capacity, BufferType type) {

        switch (type) {
            case ARRAY_BACKED:
                return ByteBuffer.allocate(capacity);

            case DIRECT:
                return ByteBuffer.allocateDirect(capacity);

            default:
                throw new AssertionError("unknown enum type");
        }
    }


    /**
     * The maximum capacity of cached buffers.
     */
    public static final int MAX_CACHED_BUFFER_CAPACITY = 65536;


    /**
     * Returns a cached (per thread) buffer with at least the specified capacity.
     * <p>
     * The specified capacity must not exceed {@link #MAX_CACHED_BUFFER_CAPACITY}.
     * 
     * @param minCapacity
     *            The minimum capacity of the cached buffer
     * @param type
     *            The type of the returned buffer
     * @return a cached (per thread) buffer
     * @exception IllegalArgumentException
     *                If {@code minCapacity > }{@value #MAX_CACHED_BUFFER_CAPACITY}
     */
    public static ByteBuffer getCached(int minCapacity, BufferType type) {

        return cachedBuffer(minCapacity, type);
    }

    /**
     * Returns a buffer {@linkplain ByteBuffer#slice() slice}, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #getSlice(ByteBuffer, int, BufferOperation) slice(buf,
     * buf.remaining(), BufferOperation.RESTORE_POSITION)}.
     * 
     * @param buf
     *            The source buffer
     * @return a buffer slice
     */
    public static ByteBuffer getSlice(ByteBuffer buf) {

        return getSlice(buf, buf.remaining(), BufferOperation.RESTORE_POSITION);
    }

    /**
     * Returns a buffer {@linkplain ByteBuffer#slice() slice}, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #getSlice(ByteBuffer, int, BufferOperation) slice(buf,
     * sliceSize, BufferOperation.RESTORE_POSITION)}.
     * 
     * @param buf
     *            The source buffer
     * @param sliceSize
     *            The size of the slice buffer
     * @return a buffer slice
     * @exception IllegalArgumentException
     *                If the size of the slice is negative
     * @exception BufferUnderflowException
     *                If the size of the slice exceeds the number of {@linkplain Buffer#remaining() available} bytes in
     *                the source buffer
     */
    public static ByteBuffer getSlice(ByteBuffer buf, int sliceSize) {

        return getSlice(buf, sliceSize, BufferOperation.RESTORE_POSITION);
    }

    /**
     * Returns a buffer {@linkplain ByteBuffer#slice() slice}, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #getSlice(ByteBuffer, int, BufferOperation) slice(buf,
     * buf.remaining(), op)}.
     * 
     * @param buf
     *            The source buffer
     * @param op
     *            The operation to apply to the source buffer after being sliced
     * @return a buffer slice
     */
    public static ByteBuffer getSlice(ByteBuffer buf, BufferOperation op) {

        return getSlice(buf, buf.remaining(), op);
    }

    /**
     * Returns a buffer {@linkplain ByteBuffer#slice() slice}, starting at the current buffer position.
     * <p>
     * The size of the slice buffer is specified as a parameter. <em>Note that the position after the slice will be
     * considered as the original position plus the size of the slice</em> (before the provided buffer operation is
     * applied to the source buffer).
     * 
     * @param buf
     *            The source buffer
     * @param sliceSize
     *            The size of the slice buffer
     * @param op
     *            The operation to apply to the source buffer after being sliced
     * @return a buffer slice
     * @exception IllegalArgumentException
     *                If the size of the slice is negative
     * @exception BufferUnderflowException
     *                If the size of the slice exceeds the number of {@linkplain Buffer#remaining() available} bytes in
     *                the source buffer
     */
    public static ByteBuffer getSlice(ByteBuffer buf, int sliceSize, BufferOperation op) {

        final int pos = buf.position();
        final int lim = buf.limit();
        final int remaining = lim - pos;

        if (sliceSize < 0) throw new IllegalArgumentException("slice size is negative");
        if (remaining < sliceSize) throw new BufferUnderflowException();

        ByteBuffer slice;
        try {
            buf.limit(pos + sliceSize);
            slice = buf.slice();
        }
        finally {
            buf.limit(lim); // always restore the original limit
        }

        // only apply the operation if no exception was previously thrown
        op.apply(buf, pos, pos + sliceSize);
        return slice;
    }

    /**
     * Copies the contents of a buffer to a newly created one.
     * <p>
     * Calling this method has the same effect as calling {@link #getCopy(ByteBuffer, int, BufferOperation)
     * copyBuffer(buf, buf.remaining(), BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param buf
     *            The buffer to be copied
     * @return a copy of a buffer
     */
    public static ByteBuffer getCopy(ByteBuffer buf) {

        return getCopy(buf, buf.remaining(), BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Copies the contents of a buffer to a newly created one.
     * <p>
     * Calling this method has the same effect as calling {@link #getCopy(ByteBuffer, int, BufferOperation)
     * copyBuffer(buf, copySize, BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param buf
     *            The buffer to be copied
     * @param copySize
     *            The number of bytes to be copied from the source buffer
     * @return a copy of a buffer
     * @exception IllegalArgumentException
     *                If the number of bytes to be copied is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to be copied exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     */
    public static ByteBuffer getCopy(ByteBuffer buf, int copySize) {

        return getCopy(buf, copySize, BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Copies the contents of a buffer to a newly created one.
     * <p>
     * Calling this method has the same effect as calling {@link #getCopy(ByteBuffer, int, BufferOperation)
     * copyBuffer(buf, buf.remaining(), op)}.
     * 
     * @param buf
     *            The buffer to be copied
     * @param op
     *            The operation to apply to the source buffer after being copied
     * @return a copy of a buffer
     */
    public static ByteBuffer getCopy(ByteBuffer buf, BufferOperation op) {

        return getCopy(buf, buf.remaining(), op);
    }

    /**
     * Copies the contents of a buffer to a newly created one. The buffer copy will have the same
     * {@linkplain ByteBuffer#order() byte order} and will only be {@linkplain ByteBuffer#isDirect() direct} if the
     * source buffer also is.
     * <p>
     * The number of bytes to be copied is specified as a parameter. <em>At the end of the copy, the position of the
     * source buffer will have advanced the specified number of bytes</em> (before the provided buffer operation is
     * applied to the source buffer).
     * 
     * @param buf
     *            The buffer to be copied
     * @param copySize
     *            The number of bytes to be copied from the source buffer
     * @param op
     *            The operation to apply to the source buffer after being copied
     * @return a copy of a buffer
     * @exception IllegalArgumentException
     *                If the number of bytes to be copied is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to be copied exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     */
    public static ByteBuffer getCopy(ByteBuffer buf, int copySize, BufferOperation op) {

        final int srcPos = buf.position();
        final int srcLim = buf.limit();
        final int remaining = srcLim - srcPos;

        if (copySize < 0) throw new IllegalArgumentException("number of bytes to copy is negative");
        if (remaining < copySize) throw new BufferUnderflowException();
        Objects.requireNonNull(op);

        ByteBuffer copy = allocate(copySize, isDirectType(buf.isDirect())).order(buf.order());
        try {
            buf.limit(srcPos + copySize);
            copy.put(buf);
            copy.rewind();
        }
        finally {
            buf.limit(srcLim); // always restore the original limit
        }

        // only apply the operation if no exception was previously thrown
        op.apply(buf, srcPos, buf.position());
        return copy;
    }

    /**
     * Copies the contents of an array to a newly created buffer.
     * <p>
     * Calling this method has the same effect as calling {@link #getCopy(byte[], int, int, BufferType) getCopy(array,
     * 0, array.length, type)}.
     * 
     * @param array
     *            The array to be copied
     * @param type
     *            The type of the returned buffer
     * @return a copy of an array
     */
    public static ByteBuffer getCopy(byte[] array, BufferType type) {

        return getCopy(array, 0, array.length, type);
    }

    /**
     * Copies the contents of an array to a newly created buffer. The array position to start the copy and the number of
     * bytes to copy are specified as parameters.
     * 
     * @param array
     *            The array to be copied
     * @param off
     *            The starting position of the copy
     * @param len
     *            The number of bytes to copy
     * @param type
     *            The type of the returned buffer
     * @return a copy of an array
     */
    public static ByteBuffer getCopy(byte[] array, int off, int len, BufferType type) {

        Indexables.checkOffsetLengthBounds(off, len, array.length);

        ByteBuffer copy = allocate(len, type);
        copy.put(array, off, len);
        copy.rewind();

        return copy;
    }

    /**
     * Copies the contents of a source buffer into a destination buffer. The number of bytes to copy is the number of
     * {@linkplain Buffer#remaining() available} bytes in the source buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #copy(ByteBuffer, BufferOperation, ByteBuffer, BufferOperation, int)
     * copyBuffer(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, src.remaining())}.
     * 
     * @param src
     *            The source buffer
     * @param dst
     *            The destination buffer
     * @exception BufferOverflowException
     *                If the number of {@linkplain Buffer#remaining() available} bytes in the source buffer exceeds the
     *                number of available bytes in the destination buffer
     */
    public static void copy(ByteBuffer src, ByteBuffer dst) {

        copy(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, src.remaining());
    }

    /**
     * Copies the contents of a source buffer into a destination buffer. The number of bytes to copy is the number of
     * {@linkplain Buffer#remaining() available} bytes in the source buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #copy(ByteBuffer, BufferOperation, ByteBuffer, BufferOperation, int)
     * copyBuffer(src, srcOp, dst, dstOp, src.remaining())}.
     * 
     * @param src
     *            The source buffer
     * @param srcOp
     *            The operation to apply to the source buffer after the copy
     * @param dst
     *            The destination buffer
     * @param dstOp
     *            The operation to apply to the destination buffer after the copy
     * @exception BufferOverflowException
     *                If the number of {@linkplain Buffer#remaining() available} bytes in the source buffer exceeds the
     *                number of available bytes in the destination buffer
     */
    public static void copy(ByteBuffer src, BufferOperation srcOp, ByteBuffer dst, BufferOperation dstOp) {

        copy(src, srcOp, dst, dstOp, src.remaining());
    }

    /**
     * Copies the contents of a source buffer into a destination buffer.
     * <p>
     * Calling this method has the same effect as calling
     * {@link #copy(ByteBuffer, BufferOperation, ByteBuffer, BufferOperation, int)
     * copyBuffer(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, copySize)}.
     * 
     * @param src
     *            The source buffer
     * @param dst
     *            The destination buffer
     * @param copySize
     *            The number of bytes to copy
     * @exception IllegalArgumentException
     *                If the number of bytes to copy is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     * @exception BufferOverflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the destination buffer
     */
    public static void copy(ByteBuffer src, ByteBuffer dst, int copySize) {

        copy(src, BufferOperation.ADVANCE_POSITION, dst, BufferOperation.ADVANCE_POSITION, copySize);
    }

    /**
     * Copies the contents of a source buffer into a destination buffer.
     * <p>
     * The number of bytes to be copied is specified as a parameter. <em>At the end of the copy, the positions of the
     * source and destination buffers will have advanced the specified number of bytes</em> (before the provided buffer
     * operations are applied to each buffer).
     * 
     * @param src
     *            The source buffer
     * @param srcOp
     *            The operation to apply to the source buffer after the copy
     * @param dst
     *            The destination buffer
     * @param dstOp
     *            The operation to apply to the destination buffer after the copy
     * @param copySize
     *            The number of bytes to copy
     * @exception IllegalArgumentException
     *                If the number of bytes to copy is negative
     * @exception BufferUnderflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the source buffer
     * @exception BufferOverflowException
     *                If the number of bytes to copy exceeds the number of {@linkplain Buffer#remaining()
     *                available} bytes in the destination buffer
     */
    public static void copy(ByteBuffer src, BufferOperation srcOp, ByteBuffer dst, BufferOperation dstOp, int copySize) {

        final int srcPos = src.position();
        final int srcLim = src.limit();
        final int srcRemaining = srcLim - srcPos;

        final int dstPos = dst.position();
        final int dstLim = dst.limit();
        final int dstRemaining = dstLim - dstPos;

        if (copySize < 0) throw new IllegalArgumentException("number of bytes to copy is negative");
        if (srcRemaining < copySize) throw new BufferUnderflowException();
        if (dstRemaining < copySize) throw new BufferOverflowException();
        Objects.requireNonNull(srcOp);
        Objects.requireNonNull(dstOp);

        try {
            src.limit(srcPos + copySize);
            dst.limit(dstPos + copySize);
            dst.put(src);
        }
        finally {
            // always restore the original limits
            src.limit(srcLim);
            dst.limit(dstLim);
        }

        // only apply the operations if no exception was previously thrown
        srcOp.apply(src, srcPos, src.position());
        dstOp.apply(dst, dstPos, dst.position());
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #putZeros(ByteBuffer, int, BufferOperation)
     * putZeros(dst, dst.remaining(), BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param dst
     *            The buffer to put zeros into
     */
    public static void putZeros(ByteBuffer dst) {

        putZeros(dst, dst.remaining(), BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #putZeros(ByteBuffer, int, BufferOperation)
     * putZeros(dst, numZeros, BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param dst
     *            The buffer to put zeros into
     * @param numZeros
     *            The number of zeros to put
     * @exception IllegalArgumentException
     *                If the number of zeros is negative
     * @exception BufferOverflowException
     *                If the number of zeros exceeds the number of {@linkplain Buffer#remaining() available} bytes in
     *                the buffer
     */
    public static void putZeros(ByteBuffer dst, int numZeros) {

        putZeros(dst, numZeros, BufferOperation.ADVANCE_POSITION);
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * Calling this method has the same effect as calling {@link #putZeros(ByteBuffer, int, BufferOperation)
     * putZeros(dst, dst.remaining(), op)}.
     * 
     * @param dst
     *            The buffer to put zeros into
     * @param op
     *            The operation to apply to the buffer after the put
     */
    public static void putZeros(ByteBuffer dst, BufferOperation op) {

        putZeros(dst, dst.remaining(), op);
    }

    /**
     * Puts zeros in the provided buffer, starting at the current buffer position.
     * <p>
     * <em>At the end of the put, the position of the buffer will have advanced the specified number of zeros</em>
     * (before the provided buffer operation is applied to the buffer).
     * 
     * @param dst
     *            The buffer to put zeros into
     * @param numZeros
     *            The number of zeros to put
     * @param op
     *            The operation to apply to the buffer after the put
     * @exception IllegalArgumentException
     *                If the number of zeros is negative
     * @exception BufferOverflowException
     *                If the number of zeros exceeds the number of {@linkplain Buffer#remaining() available} bytes in
     *                the buffer
     */
    public static void putZeros(ByteBuffer dst, final int numZeros, BufferOperation op) {

        final int pos = dst.position();
        final int lim = dst.limit();
        final int remaining = lim - pos;

        if (numZeros < 0) throw new IllegalArgumentException("number of zeros is negative");
        if (remaining < numZeros) throw new BufferOverflowException();
        Objects.requireNonNull(op);

        int remZeros = numZeros;
        while (remZeros > 0) {
            final int amount = Math.min(remZeros, ZERO_BUFFER_CAPACITY);
            dst.put(zeroBuffer(amount, isDirectType(dst.isDirect())));
            remZeros -= amount;
        }

        op.apply(dst, pos, dst.position());
    }


    private static final int ZERO_BUFFER_CAPACITY = 4096;
    private static final ThreadLocal<ByteBuffer> ZERO_ARR_BUFFER = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return allocate(ZERO_BUFFER_CAPACITY, ARRAY_BACKED);
        }
    };
    private static final ThreadLocal<ByteBuffer> ZERO_DIR_BUFFER = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return allocate(ZERO_BUFFER_CAPACITY, DIRECT);
        }
    };


    private static ThreadLocal<ByteBuffer> zeroTL(BufferType type) {

        switch (type) {

            case ARRAY_BACKED:
                return ZERO_ARR_BUFFER;

            case DIRECT:
                return ZERO_DIR_BUFFER;

            default:
                throw new AssertionError("unknown enum type");
        }
    }

    /*
     * Requires size <= ZERO_BUFFER_CAPACITY
     */
    private static ByteBuffer zeroBuffer(int size, BufferType type) {

        ByteBuffer zeroBuf = zeroTL(type).get();
        zeroBuf.clear();
        zeroBuf.limit(size);
        return zeroBuf;
    }


    private static final int INITIAL_CACHED_BUFFER_SIZE = 256;

    private static final ThreadLocal<ByteBuffer> CACHED_ARR_BUFFER = new ThreadLocal<ByteBuffer>() {

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected ByteBuffer initialValue() {

            return allocate(INITIAL_CACHED_BUFFER_SIZE, ARRAY_BACKED);
        }
    };
    private static final ThreadLocal<ByteBuffer> CACHED_DIR_BUFFER = new ThreadLocal<ByteBuffer>() {

        @Override
        protected ByteBuffer initialValue() {

            return allocate(INITIAL_CACHED_BUFFER_SIZE, DIRECT);
        }
    };


    private static ThreadLocal<ByteBuffer> cachedTL(BufferType type) {

        switch (type) {

            case ARRAY_BACKED:
                return CACHED_ARR_BUFFER;

            case DIRECT:
                return CACHED_DIR_BUFFER;

            default:
                throw new AssertionError("unknown enum type");
        }
    }

    private static ByteBuffer cachedBuffer(int minCapacity, BufferType type) {

        if (minCapacity > MAX_CACHED_BUFFER_CAPACITY) {
            throw new IllegalArgumentException("cached buffer capacity is too large");
        }

        ThreadLocal<ByteBuffer> tl = cachedTL(type);

        ByteBuffer buf = tl.get();
        if (minCapacity > buf.capacity()) {
            buf = allocate(minCapacity, type);
            tl.set(buf);
        }

        buf.clear();
        return buf;
    }

    private ByteBuffers() {

        // not instantiable
    }
}
