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

/*
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
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
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.vector.AbstractByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;



public class BasicByteVector extends AbstractByteVector implements DenseByteVector {

    private static final long serialVersionUID = 4071505L;

    private byte self[];


    public BasicByteVector() {

        this(0);
    }

    public BasicByteVector(ByteVector vector) {

        this(ByteVectors.asVectorSource(vector));
    }

    public BasicByteVector(VectorSource source) {

        this(source.length());

        for (int i = 0; i < length; i++) {
            self[i] = source.get(i);
        }
    }

    public BasicByteVector(int length) {

        this(new byte[length]);
    }

    public BasicByteVector(byte array[]) {

        super(LinearAlgebra.DENSE_FACTORY, array.length);
        this.self = array;
    }

    @Override
    public byte get(int i) {

        return self[i];
    }

    @Override
    public void set(int i, byte value) {

        self[i] = value;
    }

    @Override
    public void swap(int i, int j) {

        if (i != j) {
            byte d = self[i];
            self[i] = self[j];
            self[j] = d;
        }
    }

    @Override
    public ByteVector copy() {

        return resize(length);
    }

    @Override
    public ByteVector resize(int length) {

        ensureLengthIsCorrect(length);

        byte $self[] = new byte[length];
        System.arraycopy(self, 0, $self, 0, Math.min($self.length, self.length));

        return new BasicByteVector($self);
    }

    @Override
    public byte[] toArray() {

        byte result[] = new byte[length];
        System.arraycopy(self, 0, result, 0, length);
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(length);

        for (int i = 0; i < length; i++) {
            out.writeByte(self[i]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {

        length = in.readInt();

        self = new byte[length];

        for (int i = 0; i < length; i++) {
            self[i] = in.readByte();
        }
    }
}
