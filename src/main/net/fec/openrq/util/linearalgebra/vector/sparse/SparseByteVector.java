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
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;

import java.util.Iterator;

import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.io.VectorToBurningIterator;
import net.fec.openrq.util.linearalgebra.vector.AbstractByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorProcedure;
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

        return cardinality / (double)length;
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
    public int nonZeros(int fromIndex, int toIndex) {

        checkIndexRangeBounds(fromIndex, toIndex);

        int nonZeros = 0;

        ByteVectorIterator it = nonZeroIterator();
        while (it.hasNext()) {
            it.next(); // advance the iterator
            final int currIndex = it.index();

            // check if the current index is inside the range
            if (fromIndex <= currIndex) {
                if (currIndex < toIndex) {
                    nonZeros++;
                }
                else {
                    break; // we stop if the current index surpasses the range
                }
            }
        }

        return nonZeros;
    }

    @Override
    public void eachNonZero(VectorProcedure procedure) {

        ByteVectorIterator it = nonZeroIterator();
        while (it.hasNext()) {
            it.next();
            procedure.apply(it.index(), it.get());
        }
    }

    @Override
    public void eachNonZero(VectorProcedure procedure, int fromIndex, int toIndex) {

        checkIndexRangeBounds(fromIndex, toIndex);

        ByteVectorIterator it = nonZeroIterator();
        while (it.hasNext()) {
            it.next(); // advance the iterator
            final int currIndex = it.index();

            // check if the current index is inside the range
            if (fromIndex <= currIndex) {
                if (currIndex < toIndex) {
                    procedure.apply(currIndex, it.get());
                }
                else {
                    break; // we stop if the current index surpasses the range
                }
            }
        }
    }

    @Override
    public void updateNonZero(VectorFunction function) {

        ByteVectorIterator it = nonZeroIterator();
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(it.index(), it.get()));
        }
    }

    @Override
    public void updateNonZero(VectorFunction function, int fromIndex, int toIndex) {

        checkIndexRangeBounds(fromIndex, toIndex);

        ByteVectorIterator it = nonZeroIterator();
        while (it.hasNext()) {
            it.next(); // advance the iterator
            final int currIndex = it.index();

            // check if the current index is inside the range
            if (fromIndex <= currIndex) {
                if (currIndex < toIndex) {
                    it.set(function.evaluate(currIndex, it.get()));
                }
                else {
                    break; // we stop if the current index surpasses the range
                }
            }
        }
    }

    @Override
    public byte max() {

        byte max = foldNonZero(ByteVectors.mkMaxAccumulator());
        if (cardinality == length || aIsGreaterThanB(max, (byte)0)) {
            return max;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte min() {

        byte min = foldNonZero(ByteVectors.mkMinAccumulator());
        if (cardinality == length || aIsLessThanB(min, (byte)0)) {
            return min;
        }
        else {
            return 0;
        }
    }

    @Override
    public ByteVector multiply(byte value, Factory factory) {

        ByteVector result = blank(factory);
        ByteVectorIterator it = nonZeroIterator();

        while (it.hasNext()) {
            it.next();
            result.set(it.index(), aTimesB(it.get(), value));
        }

        return result;
    }

    @Override
    public void multiplyInPlace(byte value) {

        // TODO: multiply by 0 = clear()
        ByteVectorIterator it = nonZeroIterator();

        while (it.hasNext()) {
            it.next();
            it.set(aTimesB(it.get(), value));
        }
    }

    /**
     * Returns a non-zero iterator.
     * 
     * @return a non-zero iterator
     */
    public abstract ByteVectorIterator nonZeroIterator();

    /**
     * Returns a non-zero iterable instance. This method is useful in for-each loops.
     * 
     * @return a non-zero iterable instance
     */
    public Iterable<Byte> skipZeros() {

        return new Iterable<Byte>() {

            @Override
            public Iterator<Byte> iterator() {

                return nonZeroIterator();
            }
        };
    }

    public ByteVectorIterator nonZeroBurningIterator() {

        return iteratorToBurning(nonZeroIterator());
    }

    @Override
    public ByteVectorIterator burningIterator() {

        return iteratorToBurning(iterator());
    }

    private ByteVectorIterator iteratorToBurning(final ByteVectorIterator iterator) {

        return new VectorToBurningIterator(iterator) {

            @Override
            public void flush() {

                // fast flush
                SparseByteVector.this.cardinality = innerCursor() + 1;
            }
        };
    }

    @Override
    public ByteVector copy() {

        return resize(length);
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
