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
package net.fec.openrq.util.linearalgebra.vector.sparse;


import static net.fec.openrq.util.arithmetic.OctetOps.aIsGreaterThanB;
import static net.fec.openrq.util.arithmetic.OctetOps.aIsLessThanB;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.vector.AbstractByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.operation.VectorOperation;
import net.fec.openrq.util.linearalgebra.vector.operation.VectorVectorOperation;


public abstract class SparseByteVector extends AbstractByteVector {

    protected int cardinality;


    public SparseByteVector(int length, int cardinality) {

        super(LinearAlgebra.SPARSE_FACTORY, length);
        this.cardinality = cardinality;
    }

    /**
     * Returns the cardinality (the number of non-zero elements)
     * of this sparse vector.
     * 
     * @return the cardinality of this vector
     */
    public int cardinality() {

        return cardinality;
    }

    /**
     * Returns the density (non-zero elements divided by total elements)
     * of this sparse vector.
     * 
     * @return the density of this vector
     */
    public double density() {

        return cardinality / (double)length();
    }

    @Override
    public void assign(byte value) {

        // fast clear
        if (value == 0) {
            cardinality = 0;
        }
        else {
            super.assign(value);
        }
    }

    @Override
    public final boolean isZeroAt(int i) {

        return !nonZeroAt(i);
    }

    @Override
    public abstract boolean nonZeroAt(int i);

    @Override
    public int nonZeros() {

        return cardinality();
    }

    @Override
    public byte max() {

        byte max = foldNonZero(ByteVectors.mkMaxAccumulator());
        if (cardinality == length() || aIsGreaterThanB(max, (byte)0)) {
            return max;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte min() {

        byte min = foldNonZero(ByteVectors.mkMinAccumulator());
        if (cardinality == length() || aIsLessThanB(min, (byte)0)) {
            return min;
        }
        else {
            return 0;
        }
    }

    @Override
    public ByteVector copy() {

        return resize(length());
    }

    @Override
    public <T> T pipeTo(VectorOperation<T> operation) {

        return operation.apply(this);
    }

    @Override
    public <T> T pipeTo(VectorVectorOperation<T> operation, ByteVector that) {

        return that.pipeTo(operation.curry(this));
    }
}
