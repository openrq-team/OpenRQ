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
package net.fec.openrq.core;


import net.fec.openrq.core.util.bytevector.ByteArrayFacade;
import net.fec.openrq.core.util.bytevector.ByteVector;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class PaddedByteVector extends ByteVector implements ByteArrayFacade {

    private static final byte[] EMPTY_ARRAY = new byte[0];


    static PaddedByteVector newVector(ByteArrayFacade array, int paddedLen) {

        return newVector(array, 0, array.length(), paddedLen);
    }

    static PaddedByteVector newVector(ByteArrayFacade array, int off, int len, int paddedLen) {

        ByteVector.checkArrayBounds(off, len, array.length());
        if (paddedLen < 0) throw new IllegalArgumentException("negative padded length");

        return new PaddedByteVector(array, off, len, paddedLen);
    }


    private final ByteArrayFacade array;
    private final int off;
    private final int len;

    // paddedLength >= length ALWAYS
    private final int paddedLen;
    private final byte[] padding;


    private PaddedByteVector(ByteArrayFacade array, int off, int len, int paddedLen) {

        this.array = array;
        this.off = off;
        this.len = Math.min(len, paddedLen);
        this.paddedLen = paddedLen;

        if (length() == paddinglessLength()) {
            this.padding = EMPTY_ARRAY;
        }
        else {
            this.padding = new byte[length() - paddinglessLength()];
        }
    }

    int paddinglessLength() {

        return len;
    }

    int arrayOffset() {

        return off;
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

        return paddedLen;
    }

    @Override
    protected byte safeGet(int index) {

        if (index >= len) {
            return padding[index - len];
        }
        else {
            return array.get(off + index);
        }
    }

    @Override
    protected void safeSet(int index, byte value) {

        if (index >= len) {
            padding[index - len] = value;
        }
        else {
            array.set(off + index, value);
        }
    }
}
