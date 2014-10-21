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
package net.fec.openrq.util.linearalgebra.vector.dense;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.util.array.BytesAsLongs;
import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;
import net.fec.openrq.util.math.ExtraMath;


public class LongByteVector extends DenseByteVector {

    public static LongByteVector copyOf(byte[] array) {

        return new LongByteVector(BytesAsLongs.copyOf(array));
    }


    private BytesAsLongs self;


    public LongByteVector() {

        this(0);
    }

    public LongByteVector(ByteVector vector) {

        this(ByteVectors.asVectorSource(vector));
    }

    public LongByteVector(VectorSource source) {

        this(source.length());

        for (int i = 0; i < length(); i++) {
            self.setByte(i, source.get(i));
        }
    }

    public LongByteVector(int length) {

        this(BytesAsLongs.withSizeInBytes(length));
    }

    public LongByteVector(long array[], int length) {

        super(ExtraMath.multiplyExact(array.length, SizeOf.LONG));
        this.self = BytesAsLongs.ofSized(array, length);
    }

    public LongByteVector(BytesAsLongs bytes) {

        super(bytes.sizeInBytes());
        this.self = bytes;
    }

    @Override
    public byte safeGet(int i) {

        return self.getByte(i);
    }

    @Override
    public void safeSet(int i, byte value) {

        self.setByte(i, value);
    }

    @Override
    public void swap(int i, int j) {

        Indexables.checkIndexBounds(i, length());
        Indexables.checkIndexBounds(j, length());

        if (i != j) {
            byte tmp = self.getByte(i);
            self.setByte(i, self.getByte(j));
            self.setByte(j, tmp);
        }
    }

    @Override
    public LongByteVector copy() {

        return resize(length());
    }

    @Override
    public LongByteVector resize(int $length) {

        ensureLengthIsCorrect($length);

        BytesAsLongs $self = BytesAsLongs.withSizeInBytes($length);
        BytesAsLongs.copy(self, 0, $self, 0, Math.min($self.sizeInBytes(), self.sizeInBytes()));

        return new LongByteVector($self);
    }

    @Override
    public byte[] toArray() {

        return self.toBytes();
    }

    public BytesAsLongs getInternalBytes() {

        return self;
    }

    @Override
    public ByteBuffer serializeToBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(getSerializedDataSize());
        Serialization.writeType(buffer, Serialization.Type.DENSE_VECTOR);
        Serialization.writeVectorLength(buffer, length());

        for (int i = 0; i < length(); i++) {
            Serialization.writeVectorValue(buffer, safeGet(i));
        }

        buffer.rewind();
        return buffer;
    }

    @Override
    public void serializeToChannel(WritableByteChannel ch) throws IOException {

        Serialization.writeType(ch, Serialization.Type.DENSE_VECTOR);
        Serialization.writeVectorLength(ch, length());

        for (int i = 0; i < length(); i++) {
            Serialization.writeVectorValue(ch, safeGet(i));
        }
    }

    private int getSerializedDataSize() {

        final long dataSize = Serialization.SERIALIZATION_TYPE_NUMBYTES +
                              Serialization.VECTOR_LENGTH_NUMBYTES +
                              length();

        if (dataSize > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("vector is too large to be serialized");
        }

        return (int)dataSize;
    }
}
