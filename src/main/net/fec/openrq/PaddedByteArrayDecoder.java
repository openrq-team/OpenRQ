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


import net.fec.openrq.util.array.ArrayUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;


/**
 */
final class PaddedByteArrayDecoder {

    static PaddedByteArrayDecoder newArray(RandomAccessFileHandle rafHandle, int off, int len, int paddedLen) {

        if (paddedLen < 0) throw new IllegalArgumentException("negative padded length");

        return new PaddedByteArrayDecoder(rafHandle, off, len, paddedLen);
    }

    private final RandomAccessFileHandle rafHandle;
    private final int arrayOff;
    private final int arrayLen;

    // paddedLength >= arrayLen ALWAYS
    private final int paddedLen;
    private final byte[] padding;


    private PaddedByteArrayDecoder(RandomAccessFileHandle rafHandle, int off, int len, int paddedLen) {

        this.rafHandle = rafHandle;
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

    byte[] array(byte[] buffer) {

        try {
            rafHandle.getHandle().read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return buffer;
        }
    }

    RandomAccessFile tempStorage () {
        return rafHandle.getHandle();
    }

    String tempStorageName () {
        return rafHandle.getName();
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

        byte readByte = 0;

        if (index >= arrayLen) {
            return padding[index - arrayLen];
        }
        else {
            try {
                rafHandle.getHandle().seek(index);
                readByte = rafHandle.getHandle().readByte();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return readByte;
        }
    }

    private void safeSet(int index, byte value) {

        if (index >= arrayLen) {
            padding[index - arrayLen] = value;
        }
        else {
            try {
                rafHandle.getHandle().seek(index);
                rafHandle.getHandle().write(value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void safeSetBytes(int index, byte [] value, int offset, int len) {

        if (offset >= arrayLen) {
            for (int i = offset; i < (offset + len); i++)
            padding[i - arrayLen] = value [i];
        }
        else {
            try {
                rafHandle.getHandle().seek(index);
                rafHandle.getHandle().write(value, offset, len);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    byte get(int index) {

        ArrayUtils.checkIndexRange(index, length());
        return safeGet(index);
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

        safeSetBytes(index, src, off, len);

    }

    private static final void checkIndexAndArray(int index, int length, byte[] dst, int off, int len) {

        ArrayUtils.checkIndexRange(index, length);
        ArrayUtils.checkArrayBounds(off, len, dst.length);

        final int remaining = length - index;
        if (len > remaining) throw new BufferOverflowException();
    }
}
