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
 * Contributor(s): Ewald Grusk
 */
package net.fec.openrq.util.linearalgebra.matrix.sparse;


import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.AbstractByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;


public abstract class AbstractCompressedByteMatrix extends AbstractByteMatrix implements SparseByteMatrix {

    protected int cardinality;


    public AbstractCompressedByteMatrix(Factory factory, int rows, int columns) {

        super(factory, rows, columns);
    }

    @Override
    public int cardinality() {

        return cardinality;
    }

    @Override
    public double density() {

        return cardinality / (double)(rows * columns);
    }

    protected long capacity() {

        return ((long)rows) * columns;
    }

    protected void ensureCardinalityIsCorrect(long rows, long columns, long cardinality) {

        if (cardinality < 0) {
            fail("Cardinality should be positive: " + cardinality + ".");
        }

        long capacity = rows * columns;

        if (cardinality > capacity) {
            fail("Cardinality should be less then or equal to capacity: " + cardinality + ".");
        }
    }

    @Override
    public final boolean isZeroAt(int i, int j) {

        return !nonZeroAt(i, j);
    }

    @Override
    public abstract boolean nonZeroAt(int i, int j);

    @Override
    public int nonZeros() {

        return cardinality();
    }

    @Override
    public int nonZerosInRow(int i) {

        ByteVectorIterator it = nonZeroRowIterator(i);
        int nonZeros = 0;
        while (it.hasNext()) {
            it.next();
            nonZeros++;
        }

        return nonZeros;
    }

    @Override
    public int nonZerosInRow(int i, int fromColumn, int toColumn) {

        ByteVectorIterator it = nonZeroRowIterator(i, fromColumn, toColumn);
        int nonZeros = 0;
        while (it.hasNext()) {
            it.next();
            nonZeros++;
        }

        return nonZeros;
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure) {

        ByteVectorIterator it = nonZeroRowIterator(i);
        while (it.hasNext()) {
            it.next();
            procedure.apply(i, it.index(), it.get());
        }
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure, int fromColumn, int toColumn) {

        ByteVectorIterator it = nonZeroRowIterator(i, fromColumn, toColumn);
        while (it.hasNext()) {
            it.next();
            procedure.apply(i, it.index(), it.get());
        }
    }

    @Override
    public void updateNonZeroInRow(int i, MatrixFunction function) {

        ByteVectorIterator it = nonZeroRowIterator(i);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(i, it.index(), it.get()));
        }
    }

    @Override
    public void updateNonZeroInRow(int i, MatrixFunction function, int fromColumn, int toColumn) {

        ByteVectorIterator it = nonZeroRowIterator(i, fromColumn, toColumn);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(i, it.index(), it.get()));
        }
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(final int i) {

        checkRowBounds(i);

        return new ByteVectorIterator(columns()) {

            private int cachedColIndex = -1;
            private int j = -1;
            private int k = 0;


            @Override
            public int index() {

                return cachedColIndex;
            }

            @Override
            public byte get() {

                return safeGet(i, cachedColIndex);
            }

            @Override
            public void set(byte value) {

                safeSet(i, cachedColIndex, value);
            }

            @Override
            public Byte next() {

                j++;
                k++;
                cachedColIndex = j; // so that even after calling hasNext(), the current index remains the same
                return get();
            }

            @Override
            public boolean hasNext() {

                return k < cardinality && hasNextNonZero();
            }

            private boolean hasNextNonZero() {

                while (hasNotReachedTheEnd() && isZeroAt(i, j + 1)) {
                    j++;
                }

                return hasNotReachedTheEnd();
            }

            private boolean hasNotReachedTheEnd() {

                return j + 1 < columns();
            }
        };
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(final int i, final int fromColumn, final int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        return new ByteVectorIterator(toColumn - fromColumn) {

            private int cachedColIndex = fromColumn - 1;
            private int j = fromColumn - 1;
            private int k = 0;


            @Override
            public int index() {

                return cachedColIndex;
            }

            @Override
            public byte get() {

                return safeGet(i, cachedColIndex);
            }

            @Override
            public void set(byte value) {

                safeSet(i, cachedColIndex, value);
            }

            @Override
            public Byte next() {

                j++;
                k++;
                cachedColIndex = j; // so that even after calling hasNext(), the current index remains the same
                return get();
            }

            @Override
            public boolean hasNext() {

                return k < cardinality && hasNextNonZero();
            }

            private boolean hasNextNonZero() {

                while (hasNotReachedTheEnd() && isZeroAt(i, j + 1)) {
                    j++;
                }

                return hasNotReachedTheEnd();
            }

            private boolean hasNotReachedTheEnd() {

                return j + 1 < toColumn;
            }
        };
    }
}
