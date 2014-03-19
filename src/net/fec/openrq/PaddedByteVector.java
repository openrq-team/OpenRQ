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


import net.fec.openrq.util.bytevector.ByteArrayFacade;
import net.fec.openrq.util.bytevector.ByteVector;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class PaddedByteVector extends ByteVector implements ByteArrayFacade {

    private static final byte[] EMPTY_ARRAY = new byte[0];


    static PaddedByteVector newVector(int size, ByteArrayFacade array) {

        if (size < 0) throw new IllegalArgumentException("negative size");

        return new PaddedByteVector(array, 0, array.length(), size);
    }

    static PaddedByteVector newVector(int size, ByteArrayFacade array, int offset) {

        if (size < 0) throw new IllegalArgumentException("negative size");
        if (offset < 0) throw new IllegalArgumentException("negative offset");

        return new PaddedByteVector(array, offset, array.length() - offset, size);
    }


    private final ByteArrayFacade array;
    private final int offset;
    private final int length;

    // paddedLength >= length ALWAYS
    private final int paddedLength;
    private final byte[] padding;


    private PaddedByteVector(ByteArrayFacade array, int offset, int length, int paddedLength) {

        this.array = array;
        this.offset = offset;
        this.length = Math.min(length, paddedLength);

        this.paddedLength = paddedLength;
        if (this.paddedLength == this.length) {
            this.padding = EMPTY_ARRAY;
        }
        else {
            this.padding = new byte[this.paddedLength - this.length];
        }
    }

    int paddinglessLength() {

        return length;
    }

    int arrayOffset() {

        return offset;
    }

    @Override
    public boolean hasArray() {

        return array.hasArray();
    }

    @Override
    public byte[] array() {

        return array.array();
    }

    @Override
    public int length() {

        return paddedLength;
    }

    @Override
    protected byte safeGet(int index) {

        if (index >= length) {
            return padding[index - length];
        }
        else {
            return array.get(offset + index);
        }
    }

    @Override
    protected void safeSet(int index, byte value) {

        if (index >= length) {
            padding[index - length] = value;
        }
        else {
            array.set(offset + index, value);
        }
    }
}
