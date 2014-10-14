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


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.ReadableByteChannel;

import net.fec.openrq.util.io.ExtraChannels;
import net.fec.openrq.util.io.ExtraChannels.BufferOperation;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.numericaltype.SizeOf;


/**
 * 
 */
final class ISDUtils {

    static ByteMatrix readMatrix(ReadableByteChannel ch) throws IOException {

        return readMatrix(ch, ByteBuffer.allocate(SizeOf.INT));
    }

    static ByteMatrix readMatrix(ReadableByteChannel ch, ByteBuffer intBuffer) throws IOException {

        final int dataLen = readDataLength(ch, intBuffer);
        final ByteBuffer data = readData(ch, dataLen);

        try {
            return ByteMatrices.deserializeMatrix(data);
        }
        catch (DeserializationException e) {
            throw new IOException("deserialization error: " + e.getMessage());
        }
    }

    static int[] readIntArray(ReadableByteChannel ch) throws IOException {

        return readIntArray(ch, ByteBuffer.allocate(SizeOf.INT));
    }

    static int[] readIntArray(ReadableByteChannel ch, ByteBuffer intBuffer) throws IOException {

        final int dataLen = readDataLength(ch, intBuffer);
        final IntBuffer data = readData(ch, dataLen).asIntBuffer();
        final int[] array = new int[data.capacity()];
        data.get(array);
        return array;
    }

    private static int readDataLength(ReadableByteChannel ch, ByteBuffer intBuffer) throws IOException {

        ExtraChannels.readBytes(ch, intBuffer, SizeOf.INT, BufferOperation.RESTORE_POSITION);
        final int integer = intBuffer.getInt();
        if (integer < 0) throw new IOException("unexpected negative data length: " + integer);
        return integer;
    }

    private static ByteBuffer readData(ReadableByteChannel ch, int dataLen) throws IOException {

        final ByteBuffer buf = ByteBuffer.allocate(dataLen);
        ExtraChannels.readBytes(ch, buf, dataLen, BufferOperation.FLIP_ABSOLUTELY);
        return buf;
    }

    private ISDUtils() {

        // not instantiable
    }
}
