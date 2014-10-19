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


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.datatype.UnsignedTypes;
import net.fec.openrq.util.math.ExtraMath;


/**
 * 
 */
public final class BytesAsLongs {

    public static final int MAX_SIZE_IN_LONGS = Integer.MAX_VALUE / SizeOf.LONG;
    private static final int LONG_INDEX_SHIFT = 3;
    private static final int LONG_INDEX_MASK = SizeOf.LONG - 1;


    private static void assertValidSizeInLongs(int sizeInLongs) {

        Indexables.checkLengthBounds(sizeInLongs);
        if (sizeInLongs > MAX_SIZE_IN_LONGS) {
            throw new IllegalArgumentException("maximum size in longs exceeded");
        }
    }


    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;


    public static BytesAsLongs ofSizeInLongs(int sizeInLongs) {

        assertValidSizeInLongs(sizeInLongs);

        final int sizeInBytes = SizeOf.LONG * sizeInLongs;
        return new BytesAsLongs(new long[sizeInLongs], 0, sizeInLongs, sizeInBytes, DEFAULT_BYTE_ORDER);
    }

    public static BytesAsLongs ofSizeInLongs(int sizeInLongs, ByteOrder order) {

        assertValidSizeInLongs(sizeInLongs);
        Objects.requireNonNull(order);

        final int sizeInBytes = SizeOf.LONG * sizeInLongs;
        return new BytesAsLongs(new long[sizeInLongs], 0, sizeInLongs, sizeInBytes, order);
    }

    public static BytesAsLongs ofSizeInBytes(int sizeInBytes) {

        Indexables.checkLengthBounds(sizeInBytes);

        final int sizeInLongs = ExtraMath.ceilDiv(sizeInBytes, SizeOf.LONG);
        return new BytesAsLongs(new long[sizeInLongs], 0, sizeInLongs, sizeInBytes, DEFAULT_BYTE_ORDER);
    }

    public static BytesAsLongs ofSizeInBytes(int sizeInBytes, ByteOrder order) {

        Indexables.checkLengthBounds(sizeInBytes);
        Objects.requireNonNull(order);

        final int sizeInLongs = ExtraMath.ceilDiv(sizeInBytes, SizeOf.LONG);
        return new BytesAsLongs(new long[sizeInLongs], 0, sizeInLongs, sizeInBytes, order);
    }

    public static BytesAsLongs of(long[] longs) {

        final int sizeInLongs = longs.length;
        assertValidSizeInLongs(sizeInLongs);

        final int sizeInBytes = ExtraMath.ceilDiv(sizeInLongs, SizeOf.LONG);
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes, DEFAULT_BYTE_ORDER);
    }

    public static BytesAsLongs of(long[] longs, int off, int len) {

        Indexables.checkOffsetLengthBounds(off, len, longs.length);
        final int sizeInLongs = len;
        assertValidSizeInLongs(sizeInLongs);

        final int sizeInBytes = ExtraMath.ceilDiv(sizeInLongs, SizeOf.LONG);
        return new BytesAsLongs(longs, off, sizeInLongs, sizeInBytes, DEFAULT_BYTE_ORDER);
    }

    public static BytesAsLongs of(long[] longs, ByteOrder order) {

        final int sizeInLongs = longs.length;
        assertValidSizeInLongs(sizeInLongs);
        Objects.requireNonNull(order);

        final int sizeInBytes = ExtraMath.ceilDiv(sizeInLongs, SizeOf.LONG);
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes, order);
    }

    public static BytesAsLongs of(long[] longs, int off, int len, ByteOrder order) {

        Indexables.checkOffsetLengthBounds(off, len, longs.length);
        final int sizeInLongs = len;
        assertValidSizeInLongs(sizeInLongs);
        Objects.requireNonNull(order);

        final int sizeInBytes = ExtraMath.ceilDiv(sizeInLongs, SizeOf.LONG);
        return new BytesAsLongs(longs, off, sizeInLongs, sizeInBytes, order);
    }

    public static BytesAsLongs copyOf(byte[] bytes) {

        final int sizeInBytes = bytes.length;
        final long[] longs = bytesToLongs(bytes, 0, sizeInBytes, DEFAULT_BYTE_ORDER);
        final int sizeInLongs = longs.length;
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes, DEFAULT_BYTE_ORDER);
    }

    public static BytesAsLongs copyOf(byte[] bytes, int off, int len) {

        Indexables.checkOffsetLengthBounds(off, len, bytes.length);

        final int sizeInBytes = len;
        final long[] longs = bytesToLongs(bytes, off, sizeInBytes, DEFAULT_BYTE_ORDER);
        final int sizeInLongs = longs.length;
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes, DEFAULT_BYTE_ORDER);
    }

    public static BytesAsLongs copyOf(byte[] bytes, ByteOrder order) {

        Objects.requireNonNull(order);

        final int sizeInBytes = bytes.length;
        final long[] longs = bytesToLongs(bytes, 0, sizeInBytes, order);
        final int sizeInLongs = longs.length;
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes, order);
    }

    public static BytesAsLongs copyOf(byte[] bytes, int off, int len, ByteOrder order) {

        Indexables.checkOffsetLengthBounds(off, len, bytes.length);
        Objects.requireNonNull(order);

        final int sizeInBytes = len;
        final long[] longs = bytesToLongs(bytes, off, sizeInBytes, order);
        final int sizeInLongs = longs.length;
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes, order);
    }

    /*
     * Requires valid arguments.
     */
    private static long[] bytesToLongs(byte[] bytes, int off, int sizeInBytes, ByteOrder order) {

        final int sizeInLongs = ExtraMath.ceilDiv(sizeInBytes, SizeOf.LONG);
        final long[] longs = new long[sizeInLongs];

        final int sizeInBytesModLong = sizeInBytes % SizeOf.LONG;
        if (sizeInBytesModLong == 0) { // sizeInBytes is a multiple of sizeInLongs
            // simply retrieve the longs directly from all bytes
            ByteBuffer.wrap(bytes, off, sizeInBytes).order(order).asLongBuffer().get(longs);
        }
        else {
            // first get (sizeInLongs - 1) longs directly from all bytes except the last sizeInBytesModLong bytes
            ByteBuffer.wrap(bytes, off, sizeInBytes).order(order).asLongBuffer().get(longs, 0, sizeInLongs - 1);

            // then get the remaining bytes inside a buffer
            final int bufOff = off + SizeOf.LONG * (sizeInLongs - 1);
            final int bufLen = sizeInBytesModLong;
            final ByteBuffer lastBytesBuf = ByteBuffer.wrap(bytes, bufOff, bufLen).order(order);

            // finally get the final long by constructing it with the bytes inside the buffer
            final long lastLong = UnsignedTypes.readLongUnsignedBytes(lastBytesBuf, lastBytesBuf.remaining());
            longs[sizeInLongs - 1] = lastLong;
        }

        return longs;
    }


    private final long[] longs;
    private final int offset;

    private final int sizeInLongs;
    private final int sizeInBytes;

    private final ByteOrder order;


    /*
     * Requires valid arguments.
     */
    private BytesAsLongs(long[] longs, int offset, int sizeInLongs, int sizeInBytes, ByteOrder order) {

        this.longs = longs;
        this.offset = offset;

        this.sizeInLongs = sizeInLongs;
        this.sizeInBytes = sizeInBytes;

        this.order = order;
    }

    public int sizeInLongs() {

        return sizeInLongs;
    }

    public int sizeInBytes() {

        return sizeInBytes;
    }

    public long getLong(int index) {

        Indexables.checkIndexBounds(index, sizeInLongs);
        return longs[index + offset];
    }

    public void setLong(int index, long value) {

        Indexables.checkIndexBounds(index, sizeInLongs);
        longs[index + offset] = value;
    }

    public byte getByte(int byteIndex) {

        Indexables.checkIndexBounds(byteIndex, sizeInBytes);

        final int longIndex = byteIndex >> LONG_INDEX_SHIFT;
        final long longValue = getLong(longIndex);
        final int byteShift = getByteShift(byteIndex & LONG_INDEX_MASK);

        return (byte)(longValue >>> (byteShift * Byte.SIZE));
    }

    public void setByte(int byteIndex, byte value) {

        final int longIndex = byteIndex >> LONG_INDEX_SHIFT;
        final long longValue = getLong(longIndex);
        final int byteShift = getByteShift(byteIndex & LONG_INDEX_MASK);

        final long shiftedByteValue = ((long)value) << (byteShift * Byte.SIZE);
        final long longValueMask = ~(0xFF << (byteShift * Byte.SIZE));
        final long newLongValue = (longValue & longValueMask) | shiftedByteValue;

        setLong(longIndex, newLongValue);
    }

    /*
     * Requires byteIndex >= 0 && byteIndex < SizeOf.LONG
     */
    private int getByteShift(int byteIndex) {

        return (order == ByteOrder.LITTLE_ENDIAN) ? (byteIndex) : (SizeOf.LONG - 1 - byteIndex);
    }

    @Override
    public boolean equals(Object other) {

        return other instanceof BytesAsLongs && this.equals((BytesAsLongs)other);
    }

    public boolean equals(BytesAsLongs other) {

        if (this.sizeInBytes != other.sizeInBytes) {
            return false;
        }

        if (this.order != other.order) {
            return false;
        }

        final int size = this.sizeInLongs;
        if (other.sizeInLongs != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (this.longs[i + this.offset] != other.longs[i + other.offset]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 1;
        hash = 31 * hash + sizeInBytes;
        hash = 31 * hash + order.hashCode();
        for (int i = offset, n = 0; n < sizeInLongs; i++, n++) {
            hash = 31 * hash + Long.valueOf(longs[i]).hashCode();
        }

        return hash;
    }

    @Override
    public String toString() {

        final int iMax = sizeInBytes - 1;
        if (iMax == -1) {
            return "[]";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0;; i++) {
            sb.append(getByte(i));
            if (i == iMax) {
                return sb.append(']').toString();
            }
            sb.append(", ");
        }
    }
}
