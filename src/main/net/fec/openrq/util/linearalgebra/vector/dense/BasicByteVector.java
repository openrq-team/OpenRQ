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
package net.fec.openrq.util.linearalgebra.vector.dense;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;


public class BasicByteVector extends DenseByteVector {

    private byte self[];


    public BasicByteVector() {

        this(0);
    }

    public BasicByteVector(ByteVector vector) {

        this(ByteVectors.asVectorSource(vector));
    }

    public BasicByteVector(VectorSource source) {

        this(source.length());

        for (int i = 0; i < length(); i++) {
            self[i] = source.get(i);
        }
    }

    public BasicByteVector(int length) {

        this(new byte[length]);
    }

    public BasicByteVector(byte array[]) {

        super(array.length);
        this.self = array;
    }

    @Override
    public byte safeGet(int i) {

        return self[i];
    }

    @Override
    public void safeSet(int i, byte value) {

        self[i] = value;
    }

    @Override
    public void swap(int i, int j) {

        Indexables.checkIndexBounds(i, length());
        Indexables.checkIndexBounds(j, length());

        if (i != j) {
            ArrayUtils.swapBytes(self, i, j);
        }
    }

    @Override
    public ByteVector copy() {

        return resize(length());
    }

    @Override
    public ByteVector resize(int $length) {

        ensureLengthIsCorrect($length);

        byte $self[] = new byte[$length];
        System.arraycopy(self, 0, $self, 0, Math.min($self.length, self.length));

        return new BasicByteVector($self);
    }

    @Override
    public byte[] toArray() {

        byte result[] = new byte[length()];
        System.arraycopy(self, 0, result, 0, length());
        return result;
    }

    public byte[] getInternalArray() {

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
