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
import java.util.Arrays;

import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.datatype.UnsignedTypes;
import net.fec.openrq.util.math.ExtraMath;


/**
 * 
 */
public final class BytesAsLongs {

    public static final int MAX_SIZE_IN_LONGS = Integer.MAX_VALUE / SizeOf.LONG;


    private static void assertValidSizeInLongs(int sizeInLongs) {

        Indexables.checkLengthBounds(sizeInLongs, "size in longs");
        if (sizeInLongs > MAX_SIZE_IN_LONGS) {
            throw new IllegalArgumentException("maximum size in longs exceeded");
        }
    }

    /*
     * Requires valid sizeInLongs
     */
    private static void assertValidSizeInBytes(int sizeInBytes, int sizeInLongs) {

        Indexables.checkLengthBounds(sizeInBytes, "size in bytes");
        if (sizeInBytes > sizeInLongs * SizeOf.LONG) {
            throw new IllegalArgumentException("illegal size in bytes");
        }
    }

    private static ByteBuffer order(ByteBuffer b) {

        // the least significant byte in a long is the one with smallest index
        return b.order(ByteOrder.LITTLE_ENDIAN);
    }

    public static BytesAsLongs withSizeInLongs(int sizeInLongs) {

        assertValidSizeInLongs(sizeInLongs);

        final int sizeInBytes = SizeOf.LONG * sizeInLongs;
        return new BytesAsLongs(new long[sizeInLongs], 0, sizeInLongs, sizeInBytes);
    }

    public static BytesAsLongs withSizeInBytes(int sizeInBytes) {

        Indexables.checkLengthBounds(sizeInBytes, "size in bytes");

        final int sizeInLongs = ExtraMath.ceilDiv(sizeInBytes, SizeOf.LONG);
        return new BytesAsLongs(new long[sizeInLongs], 0, sizeInLongs, sizeInBytes);
    }

    public static BytesAsLongs of(long[] longs) {

        final int sizeInLongs = longs.length;
        assertValidSizeInLongs(sizeInLongs);

        final int sizeInBytes = ExtraMath.ceilDiv(sizeInLongs, SizeOf.LONG);
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes);
    }

    public static BytesAsLongs of(long[] longs, int off, int len) {

        Indexables.checkOffsetLengthBounds(off, len, longs.length);
        final int sizeInLongs = len;
        assertValidSizeInLongs(sizeInLongs);

        final int sizeInBytes = ExtraMath.ceilDiv(sizeInLongs, SizeOf.LONG);
        return new BytesAsLongs(longs, off, sizeInLongs, sizeInBytes);
    }

    public static BytesAsLongs ofSized(long[] longs, int sizeInBytes) {

        final int sizeInLongs = longs.length;
        assertValidSizeInLongs(sizeInLongs);
        assertValidSizeInBytes(sizeInBytes, sizeInLongs);

        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes);
    }

    public static BytesAsLongs ofSized(long[] longs, int off, int len, int sizeInBytes) {

        Indexables.checkOffsetLengthBounds(off, len, longs.length);
        final int sizeInLongs = len;
        assertValidSizeInLongs(sizeInLongs);
        assertValidSizeInBytes(sizeInBytes, sizeInLongs);

        return new BytesAsLongs(longs, off, sizeInLongs, sizeInBytes);
    }

    public static BytesAsLongs copyOf(byte[] bytes) {

        final int sizeInBytes = bytes.length;
        final long[] longs = bytesToLongs(bytes, 0, sizeInBytes);
        final int sizeInLongs = longs.length;
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes);
    }

    public static BytesAsLongs copyOf(byte[] bytes, int off, int len) {

        Indexables.checkOffsetLengthBounds(off, len, bytes.length);

        final int sizeInBytes = len;
        final long[] longs = bytesToLongs(bytes, off, sizeInBytes);
        final int sizeInLongs = longs.length;
        return new BytesAsLongs(longs, 0, sizeInLongs, sizeInBytes);
    }

    /*
     * Requires valid arguments.
     */
    private static long[] bytesToLongs(byte[] bytes, int off, int sizeInBytes) {

        final int sizeInLongs = ExtraMath.ceilDiv(sizeInBytes, SizeOf.LONG);
        final long[] longs = new long[sizeInLongs];

        final int sizeInBytesModLong = sizeInBytes % SizeOf.LONG;
        if (sizeInBytesModLong == 0) { // sizeInBytes is a multiple of sizeInLongs
            // simply retrieve the longs directly from all bytes
            order(ByteBuffer.wrap(bytes, off, sizeInBytes)).asLongBuffer().get(longs);
        }
        else {
            // first get (sizeInLongs - 1) longs directly from all bytes except the last sizeInBytesModLong bytes
            order(ByteBuffer.wrap(bytes, off, sizeInBytes)).asLongBuffer().get(longs, 0, sizeInLongs - 1);

            // then get the remaining bytes inside a buffer
            final int bufOff = off + SizeOf.LONG * (sizeInLongs - 1);
            final int bufLen = sizeInBytesModLong;
            final ByteBuffer lastBytesBuf = order(ByteBuffer.wrap(bytes, bufOff, bufLen));

            // finally get the final long by constructing it with the bytes inside the buffer
            final long lastLong = UnsignedTypes.readLongUnsignedBytes(lastBytesBuf, lastBytesBuf.remaining());
            longs[sizeInLongs - 1] = lastLong;
        }

        return longs;
    }

    public static void copy(BytesAsLongs src, int srcPos, BytesAsLongs dest, int destPos, int sizeInBytes) {

        Indexables.checkOffsetLengthBounds(srcPos, sizeInBytes, src.sizeInBytes());
        Indexables.checkOffsetLengthBounds(destPos, sizeInBytes, dest.sizeInBytes());

        final int srcLongPos = srcPos / SizeOf.LONG;
        final int srcPosMod = srcPos % SizeOf.LONG;

        final int destLongPos = destPos / SizeOf.LONG;
        final int destPosMod = destPos % SizeOf.LONG;

        final int sizeInLongs = sizeInBytes / SizeOf.LONG;
        final int sizeInBytesMod = sizeInBytes % SizeOf.LONG;

        if (srcPosMod == 0 && destPosMod == 0 && sizeInBytesMod == 0) {
            // optimization
            System.arraycopy(src.longs, srcLongPos, dest.longs, destLongPos, sizeInLongs);
        }
        else {
            final int srcEnd = srcPos + sizeInBytes;
            for (int s = srcPos, d = destPos; s < srcEnd; s++, d++) {
                dest.setByte(d, src.getByte(s));
            }
        }
    }


    private final long[] longs;
    private final int offset;

    private final int sizeInLongs;
    private final int sizeInBytes;


    /*
     * Requires valid arguments.
     */
    private BytesAsLongs(long[] longs, int offset, int sizeInLongs, int sizeInBytes) {

        this.longs = longs;
        this.offset = offset;

        this.sizeInLongs = sizeInLongs;
        this.sizeInBytes = sizeInBytes;
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

        final int longIndex = byteIndex / SizeOf.LONG;
        final int bitShift = (byteIndex % SizeOf.LONG) * Byte.SIZE;

        return (byte)(getLong(longIndex) >>> bitShift);
    }

    public void setByte(int byteIndex, byte value) {

        Indexables.checkIndexBounds(byteIndex, sizeInBytes);

        final int longIndex = byteIndex / SizeOf.LONG;
        final int bitShift = (byteIndex % SizeOf.LONG) * Byte.SIZE;

        final long maskedLongValue = getLong(longIndex) & ~(0xFFL << bitShift);
        final long shiftedByteValue = ((long)UnsignedTypes.getUnsignedByte(value)) << bitShift;

        setLong(longIndex, maskedLongValue | shiftedByteValue);
    }

    public byte[] toBytes() {

        final ByteBuffer buf = order(ByteBuffer.allocate(sizeInBytes));

        int remaining = sizeInBytes;
        int longIndex = 0;
        while (remaining > 0) {
            final int numBytes = Math.min(remaining, SizeOf.LONG);
            UnsignedTypes.writeLongUnsignedBytes(longs[longIndex++], buf, numBytes);
            remaining -= numBytes;
        }

        return buf.array();
    }

    public BytesAsLongs copy() {

        return BytesAsLongs.ofSized(Arrays.copyOf(longs, longs.length), sizeInBytes);
    }

    @Override
    public boolean equals(Object other) {

        return other instanceof BytesAsLongs && this.equals((BytesAsLongs)other);
    }

    public boolean equals(BytesAsLongs other) {

        if (this.sizeInBytes != other.sizeInBytes) {
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
