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


import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.util.array.ArrayIO;
import net.fec.openrq.util.io.ExtraChannels;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;


/**
 * 
 */
final class ISDUtils {

    static void writeMatrix(WritableByteChannel ch, ByteMatrix mat) throws IOException {

        mat.serializeToChannel(ch);
    }

    static void writeIntArray(WritableByteChannel ch, int[] array) throws IOException {

        ExtraChannels.writeInt(ch, array.length);
        ArrayIO.writeInts(ch, array);
    }

    static ByteMatrix readMatrix(ReadableByteChannel ch) throws IOException {

        try {
            return ByteMatrices.deserializeMatrix(ch);
        }
        catch (DeserializationException e) {
            throw new IOException("deserialization error: " + e.getMessage());
        }
    }

    static int[] readIntArray(ReadableByteChannel ch) throws IOException {

        int[] array = new int[readIntArraySize(ch)];
        ArrayIO.readInts(ch, array);
        return array;
    }

    private static int readIntArraySize(ReadableByteChannel ch) throws IOException {

        final int dataLen = ExtraChannels.readInt(ch);
        if (dataLen < 0) {
            throw new IOException("unexpected negative data length: " + dataLen);
        }

        return dataLen;
    }

    private ISDUtils() {

        // not instantiable
    }
}
