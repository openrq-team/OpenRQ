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


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.matrix.AbstractByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


public abstract class AbstractCompressedByteMatrix extends AbstractByteMatrix
    implements SparseByteMatrix {

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
    public boolean isZeroAt(int i, int j) {

        return !nonZeroAt(i, j);
    }

    @Override
    public void eachNonZero(MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final byte val = get(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    procedure.apply(i, j, val);
                }
            }
        }
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure) {

        for (int j = 0; j < columns; j++) {
            final byte val = get(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                procedure.apply(i, j, val);
            }
        }
    }

    @Override
    public void eachNonZeroInColumn(int j, MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            final byte val = get(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                procedure.apply(i, j, val);
            }
        }
    }

    @Override
    public byte foldNonZero(MatrixAccumulator accumulator) {

        eachNonZero(ByteMatrices.asAccumulatorProcedure(accumulator));
        return accumulator.accumulate();
    }

    @Override
    public byte foldNonZeroInRow(int i, MatrixAccumulator accumulator) {

        eachNonZeroInRow(i, ByteMatrices.asAccumulatorProcedure(accumulator));
        return accumulator.accumulate();
    }

    @Override
    public byte foldNonZeroInColumn(int j, MatrixAccumulator accumulator) {

        eachNonZeroInColumn(j, ByteMatrices.asAccumulatorProcedure(accumulator));
        return accumulator.accumulate();
    }

    @Override
    public ByteVector foldNonZeroInColumns(MatrixAccumulator accumulator) {

        ByteVector result = factory.createVector(columns);

        for (int i = 0; i < columns; i++) {
            result.set(i, foldNonZeroInColumn(i, accumulator));
        }

        return result;
    }

    @Override
    public ByteVector foldNonZeroInRows(MatrixAccumulator accumulator) {

        ByteVector result = factory.createVector(rows);

        for (int i = 0; i < rows; i++) {
            result.set(i, foldNonZeroInRow(i, accumulator));
        }

        return result;
    }

    @Override
    public void updateNonZero(MatrixFunction function) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final byte val = get(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    set(i, j, function.evaluate(i, j, val));
                }
            }
        }
    }

    @Override
    public void updateNonZeroInRow(int i, MatrixFunction function) {

        for (int j = 0; j < columns; j++) {
            final byte val = get(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                set(i, j, function.evaluate(i, j, val));
            }
        }
    }

    @Override
    public void updateNonZeroInColumn(int j, MatrixFunction function) {

        for (int i = 0; i < rows; i++) {
            final byte val = get(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                set(i, j, function.evaluate(i, j, val));
            }
        }
    }
}
