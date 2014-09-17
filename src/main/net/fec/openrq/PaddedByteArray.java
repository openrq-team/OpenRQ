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


import java.nio.BufferOverflowException;

import net.fec.openrq.util.array.ArrayUtils;


/**
 */
final class PaddedByteArray {

    static PaddedByteArray newArray(byte[] array, int paddedLen) {

        return newArray(array, 0, array.length, paddedLen);
    }

    static PaddedByteArray newArray(byte[] array, int off, int len, int paddedLen) {

        ArrayUtils.checkOffsetLengthBounds(off, len, array.length);
        if (paddedLen < 0) throw new IllegalArgumentException("negative padded length");

        return new PaddedByteArray(array, off, len, paddedLen);
    }


    private final byte[] array;
    private final int arrayOff;
    private final int arrayLen;

    // paddedLength >= arrayLen ALWAYS
    private final int paddedLen;
    private final byte[] padding;


    private PaddedByteArray(byte[] array, int off, int len, int paddedLen) {

        this.array = array;
        this.arrayOff = off;
        this.arrayLen = Math.min(len, paddedLen);
        this.paddedLen = paddedLen;

        if (length() == paddinglessLength()) {
            this.padding = ArrayUtils.EmptyArrayOf.bytes();
        }
        else {
            this.padding = new byte[length() - paddinglessLength()];
        }
    }

    byte[] array() {

        return array;
    }

    int arrayOffset() {

        return arrayOff;
    }

    int paddinglessLength() {

        return arrayLen;
    }

    int length() {

        return paddedLen;
    }

    private byte safeGet(int index) {

        if (index >= arrayLen) {
            return padding[index - arrayLen];
        }
        else {
            return array[arrayOff + index];
        }
    }

    private void safeSet(int index, byte value) {

        if (index >= arrayLen) {
            padding[index - arrayLen] = value;
        }
        else {
            array[arrayOff + index] = value;
        }
    }

    byte get(int index) {

        ArrayUtils.checkIndexBounds(index, length());
        return safeGet(index);
    }

    void set(int index, byte value) {

        ArrayUtils.checkIndexBounds(index, length());
        safeSet(index, value);
    }

    byte[] getBytes(byte[] dst) {

        return getBytes(0, dst, 0, dst.length);
    }

    byte[] getBytes(byte[] dst, int off, int len) {

        return getBytes(0, dst, off, len);
    }

    byte[] getBytes(int index, byte[] dst) {

        return getBytes(index, dst, 0, dst.length);
    }

    byte[] getBytes(int index, byte[] dst, int off, int len) {

        checkIndexAndArray(index, length(), dst, off, len);

        final int end = off + len;
        for (int d = off, ii = index; d < end; d++, ii++) {
            dst[d] = safeGet(ii);
        }

        return dst;
    }

    void putBytes(byte[] src) {

        putBytes(0, src, 0, src.length);
    }

    void putBytes(byte[] src, int off, int len) {

        putBytes(0, src, off, len);
    }

    void putBytes(int index, byte[] src) {

        putBytes(index, src, 0, src.length);
    }

    void putBytes(int index, byte[] src, int off, int len) {

        checkIndexAndArray(index, length(), src, off, len);

        final int end = off + len;
        for (int s = off, ii = index; s < end; s++, ii++) {
            safeSet(ii, src[s]);
        }
    }

    private static final void checkIndexAndArray(int index, int length, byte[] dst, int off, int len) {

        ArrayUtils.checkIndexBounds(index, length);
        ArrayUtils.checkOffsetLengthBounds(off, len, dst.length);

        final int remaining = length - index;
        if (len > remaining) throw new BufferOverflowException();
    }
}
