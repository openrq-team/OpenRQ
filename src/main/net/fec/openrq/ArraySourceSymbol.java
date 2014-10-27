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
package net.fec.openrq;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

import net.fec.openrq.util.io.BufferOperation;
import net.fec.openrq.util.io.ByteBuffers;


/**
 * Container of source symbol array data.
 */
final class ArraySourceSymbol implements SourceSymbol {

    /*
     * Requires valid parameters.
     */
    static ArraySourceSymbol newSymbol(byte[] srcDataArray, int symbolOff, int symbolSize) {

        final int codeSize = symbolSize;
        final int transportSize = Math.min(symbolSize, srcDataArray.length - symbolOff);

        ByteBuffer transportBuf = prepareTransportBuffer(srcDataArray, symbolOff, transportSize);
        return new ArraySourceSymbol(srcDataArray, symbolOff, codeSize, transportBuf);
    }

    private static ByteBuffer prepareTransportBuffer(byte[] array, int off, int len) {

        // need to return a slice of the wrapped buffer,
        // otherwise the buffer position will be equal to off
        return ByteBuffer.wrap(array, off, len).slice();
    }


    private final byte[] srcDataArray;
    private final int symbolOff;

    private final int codeSize;

    private final ByteBuffer transportBuf;


    private ArraySourceSymbol(
        byte[] srcDataArray,
        int symbolOff,
        int codeSize,
        ByteBuffer transportBuf)
    {

        this.srcDataArray = Objects.requireNonNull(srcDataArray);
        this.symbolOff = symbolOff;

        this.codeSize = codeSize;

        this.transportBuf = Objects.requireNonNull(transportBuf);
    }

    @Override
    public int codeSize() {

        return codeSize;
    }

    @Override
    public void getCodeData(ByteBuffer dst) {

        getCodeData(dst, BufferOperation.DO_NOTHING);
    }

    @Override
    public void getCodeData(ByteBuffer dst, BufferOperation op) {

        final int pos = dst.position();
        final int lim = dst.limit();
        final int remaining = lim - pos;
        if (remaining < codeSize) throw new BufferOverflowException();

        dst.put(srcDataArray, symbolOff, transportSize());
        ByteBuffers.putZeros(dst, codeSize - transportSize());

        op.apply(dst, pos);
    }

    @Override
    public void putCodeData(ByteBuffer src) {

        putCodeData(src, BufferOperation.DO_NOTHING);
    }

    @Override
    public void putCodeData(ByteBuffer src, BufferOperation op) {

        final int pos = src.position();
        final int lim = src.limit();
        final int remaining = lim - pos;
        if (remaining < codeSize) throw new BufferUnderflowException();

        src.get(srcDataArray, symbolOff, transportSize());
        src.position(pos + codeSize); // always advance the position codeSize bytes

        op.apply(src, pos);
    }

    @Override
    public int transportSize() {

        return transportBuf.remaining();
    }

    @Override
    public ByteBuffer transportData() {

        return transportBuf.asReadOnlyBuffer();
    }
}
