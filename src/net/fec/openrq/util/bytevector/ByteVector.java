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
package net.fec.openrq.util.bytevector;


import java.nio.BufferOverflowException;


/**
 * This interface defines a fixed-length vector of bytes.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public abstract class ByteVector {

    /**
     * Returns the (fixed) length in number of bytes of this vector.
     * 
     * @return the number of bytes in this vector
     */
    public abstract int length();

    protected abstract byte safeGet(int index);

    protected abstract void safeSet(int index, byte value);

    /**
     * Returns the byte at a given position.
     * 
     * @param index
     *            A position in this vector (must be non-negative and less than {@code length()})
     * @return the byte at position {@code index}
     * @exception IndexOutOfBoundsException
     *                If {@code index < 0} or if {@code index >= length()}
     */
    public final byte get(int index) {

        checkIndexRange(index, length());
        return safeGet(index);
    }

    /**
     * Applies a binary function to the byte at a given position and the provided secondary value, and returns the
     * result.
     * 
     * @param index
     *            A position in this vector (must be non-negative and less than {@code length()})
     * @param value
     *            A secondary byte value
     * @param binaryFunction
     *            A binary function to be applied to the byte at position {@code index} and {@code value}
     * @return the result of {@code binaryFunction} applied to the byte at position {@code index} and {@code value}
     */
    public final byte get(int index, byte value, BinaryByteFunction binaryFunction) {

        checkIndexRange(index, length());
        return binaryFunction.op(safeGet(index), value);
    }

    public final byte[] get(int index, byte[] dst) {

        return get(index, dst, 0, dst.length);
    }

    public final byte[] get(int index, BinaryByteFunction binaryFunction, byte[] dst) {

        return get(index, binaryFunction, dst, 0, dst.length);
    }

    public final byte[] get(int index, byte[] dst, int off, int len) {

        checkIndexAndArray(index, length(), dst, off, len);

        final int end = off + len;
        for (int d = off, v = index; d < end; d++, v++) {
            dst[d] = safeGet(v);
        }

        return dst;
    }

    public final byte[] get(int index, BinaryByteFunction binaryFunction, byte[] dst, int off, int len) {

        checkIndexAndArray(index, length(), dst, off, len);

        final int end = off + len;
        for (int d = off, v = index; d < end; d++, v++) {
            dst[d] = binaryFunction.op(safeGet(v), dst[d]);
        }

        return dst;
    }

    /**
     * Replaces the byte at a given position with the provided value.
     * 
     * @param index
     *            A position in this vector (must be non-negative and less than {@code length()})
     * @param value
     *            The value to be set at position {@code index}
     * @exception IndexOutOfBoundsException
     *                If {@code index < 0} or if {@code index >= length()}
     */
    public final void set(int index, byte value) {

        checkIndexRange(index, length());
        safeSet(index, value);
    }

    /**
     * Applies a binary function to the byte at a given position and the provided secondary value, and replaces the
     * former byte with the result.
     * 
     * @param index
     *            A position in this vector (must be non-negative and less than {@code length()})
     * @param value
     *            A secondary byte value
     * @param binaryFunction
     *            A binary function to be applied on the byte at position {@code index} and {@code value}
     * @exception IndexOutOfBoundsException
     *                If {@code index < 0} or if {@code index >= length()}
     */
    public final void set(int index, byte value, BinaryByteFunction binaryFunction) {

        checkIndexRange(index, length());
        safeSet(index, binaryFunction.op(safeGet(index), value));
    }

    public final void set(int index, byte[] src) {

        set(index, src, 0, src.length);
    }

    public final void set(int index, BinaryByteFunction binaryFunction, byte[] src) {

        set(index, binaryFunction, src, 0, src.length);
    }

    public final void set(int index, byte[] src, int off, int len) {

        checkIndexAndArray(index, length(), src, off, len);

        final int end = off + len;
        for (int s = off, v = index; s < end; s++, v++) {
            safeSet(v, src[s]);
        }
    }

    public void set(int index, BinaryByteFunction binaryFunction, byte[] src, int off, int len) {

        checkIndexAndArray(index, length(), src, off, len);

        final int end = off + len;
        for (int s = off, v = index; s < end; s++, v++) {
            safeSet(index, binaryFunction.op(safeGet(v), src[s]));
        }
    }

    private static final void checkIndexAndArray(int index, int length, byte[] dst, int off, int len) {

        checkIndexRange(index, length);
        checkArrayBounds(off, len, dst.length);

        final int remaining = length - index;
        if (len > remaining) throw new BufferOverflowException();
    }

    private static final void checkIndexRange(int index, int length) {

        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(getIndexRangeMsg(index, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static final String getIndexRangeMsg(int index, int length) {

        return "index = " + index + "; length = " + length;
    }

    private static final void checkArrayBounds(int arrOff, int arrLen, int length) {

        // retrieved from java.nio.Buffer class
        if ((arrOff | arrLen | (arrOff + arrLen) | (length - (arrOff + arrLen))) < 0) {
            throw new IndexOutOfBoundsException(getArrayBoundsMsg(arrOff, arrLen, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static final String getArrayBoundsMsg(int off, int len, int arrLength) {

        return "region off = " + off + "; region length = " + len + "; array length = " + arrLength;
    }
}
