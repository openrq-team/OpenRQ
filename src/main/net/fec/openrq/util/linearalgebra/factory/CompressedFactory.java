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

/*
 * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra.factory;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Random;

import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;
import net.fec.openrq.util.linearalgebra.vector.sparse.CompressedByteVector;


public abstract class CompressedFactory extends Factory {

    public static final int DENSITY = 4;


    @Override
    public ByteVector createVector() {

        return new CompressedByteVector();
    }

    @Override
    public ByteVector createVector(int length) {

        return new CompressedByteVector(length);
    }

    @Override
    public ByteVector createVector(byte[] array) {

        return new CompressedByteVector(array);
    }

    @Override
    public ByteVector createVector(ByteVector vector) {

        return new CompressedByteVector(vector);
    }

    @Override
    public ByteVector createVector(VectorSource source) {

        return new CompressedByteVector(source);
    }

    @Override
    public ByteVector createConstantVector(int length, byte value) {

        byte values[] = new byte[length];
        int indices[] = new int[length];

        for (int i = 0; i < length; i++) {
            indices[i] = i;
            values[i] = value;
        }

        return new CompressedByteVector(length, length, values, indices);
    }

    @Override
    public ByteVector createRandomVector(int length, Random random) {

        int cardinality = length / DENSITY;

        byte values[] = new byte[cardinality];
        int indices[] = new int[cardinality];

        for (int i = 0; i < cardinality; i++) {
            values[i] = (byte)random.nextInt();
            indices[i] = random.nextInt(length);
        }

        Arrays.sort(indices);

        return new CompressedByteVector(length, cardinality, values, indices);
    }

    @Override
    public ByteVector deserializeVector(ByteBuffer buffer) throws DeserializationException {

        final int length = Serialization.readVectorLength(buffer);
        final int cardinality = Serialization.readVectorCardinality(buffer);
        final int[] indices = new int[cardinality];
        final byte[] values = new byte[cardinality];
        for (int i = 0; i < cardinality; i++) {
            indices[i] = Serialization.readVectorIndex(buffer);
            values[i] = Serialization.readVectorValue(buffer);
        }

        return new CompressedByteVector(length, cardinality, values, indices);
    }

    @Override
    public ByteVector deserializeVector(ReadableByteChannel ch) throws IOException, DeserializationException {

        final int length = Serialization.readVectorLength(ch);
        final int cardinality = Serialization.readVectorCardinality(ch);
        final int[] indices = new int[cardinality];
        final byte[] values = new byte[cardinality];
        for (int i = 0; i < cardinality; i++) {
            indices[i] = Serialization.readVectorIndex(ch);
            values[i] = Serialization.readVectorValue(ch);
        }

        return new CompressedByteVector(length, cardinality, values, indices);
    }
}
