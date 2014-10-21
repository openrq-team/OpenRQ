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
package net.fec.openrq.util.linearalgebra.factory;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import net.fec.openrq.util.array.BytesAsLongs;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.dense.LongByteVector;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;


/**
 * 
 */
public abstract class LongFactory extends Factory {

    @Override
    public ByteVector createVector() {

        return new LongByteVector();
    }

    @Override
    public ByteVector createVector(int length) {

        return new LongByteVector(length);
    }

    @Override
    public ByteVector createVector(byte[] array) {

        return LongByteVector.copyOf(array);
    }

    @Override
    public ByteVector createVector(ByteVector vector) {

        return new LongByteVector(vector);
    }

    @Override
    public ByteVector createVector(VectorSource source) {

        return new LongByteVector(source);
    }

    @Override
    public ByteVector createConstantVector(int length, byte value) {

        BytesAsLongs bytes = BytesAsLongs.withSizeInBytes(length);
        for (int i = 0; i < length; i++) {
            bytes.setByte(i, value);
        }

        return new LongByteVector(bytes);
    }

    @Override
    public ByteVector createRandomVector(int length, Random random) {

        BytesAsLongs bytes = BytesAsLongs.withSizeInBytes(length);
        for (int i = 0; i < length; i++) {
            bytes.setByte(i, (byte)random.nextInt());
        }

        return new LongByteVector(bytes);
    }

    @Override
    public ByteVector deserializeVector(ByteBuffer buffer) throws DeserializationException {

        final int length = Serialization.readVectorLength(buffer);
        BytesAsLongs bytes = BytesAsLongs.withSizeInBytes(length);
        for (int i = 0; i < length; i++) {
            bytes.setByte(i, Serialization.readVectorValue(buffer));
        }

        return new LongByteVector(bytes);
    }

    @Override
    public ByteVector deserializeVector(ReadableByteChannel ch) throws IOException, DeserializationException {

        final int length = Serialization.readVectorLength(ch);
        BytesAsLongs bytes = BytesAsLongs.withSizeInBytes(length);
        for (int i = 0; i < length; i++) {
            bytes.setByte(i, Serialization.readVectorValue(ch));
        }

        return new LongByteVector(bytes);
    }
}
