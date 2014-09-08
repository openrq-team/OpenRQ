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
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


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
    public boolean isZeroAt(int i, int j) {

        return !nonZeroAt(i, j);
    }

    @Override
    public int nonZeros() {

        return cardinality();
    }

    @Override
    public int nonZerosInRow(int i) {

        checkRowBounds(i);

        int nonZeros = 0;
        for (int j = 0; j < columns; j++) {
            if (nonZeroAt(i, j)) {
                nonZeros++;
            }
        }

        return nonZeros;
    }

    @Override
    public int nonZerosInRow(int i, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        int nonZeros = 0;
        for (int j = fromColumn; j < toColumn; j++) {
            if (nonZeroAt(i, j)) {
                nonZeros++;
            }
        }

        return nonZeros;
    }

    @Override
    public int nonZerosInColumn(int j) {

        checkColumnBounds(j);

        int nonZeros = 0;
        for (int i = 0; i < rows; i++) {
            if (nonZeroAt(i, j)) {
                nonZeros++;
            }
        }

        return nonZeros;
    }

    @Override
    public int nonZerosInColumn(int j, int fromRow, int toRow) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);

        int nonZeros = 0;
        for (int i = fromRow; i < toRow; i++) {
            if (nonZeroAt(i, j)) {
                nonZeros++;
            }
        }

        return nonZeros;
    }

    @Override
    public void eachNonZero(MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final byte val = safeGet(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    procedure.apply(i, j, val);
                }
            }
        }
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure) {

        checkRowBounds(i);

        for (int j = 0; j < columns; j++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                procedure.apply(i, j, val);
            }
        }
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        for (int j = fromColumn; j < toColumn; j++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                procedure.apply(i, j, val);
            }
        }
    }

    @Override
    public void eachNonZeroInColumn(int j, MatrixProcedure procedure) {

        checkColumnBounds(j);

        for (int i = 0; i < rows; i++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                procedure.apply(i, j, val);
            }
        }
    }

    @Override
    public void eachNonZeroInColumn(int j, MatrixProcedure procedure, int fromRow, int toRow) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);

        for (int i = fromRow; i < toRow; i++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                procedure.apply(i, j, val);
            }
        }
    }

    @Override
    public void updateNonZero(MatrixFunction function) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final byte val = safeGet(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    safeSet(i, j, function.evaluate(i, j, val));
                }
            }
        }
    }

    @Override
    public void updateNonZeroInRow(int i, MatrixFunction function) {

        checkRowBounds(i);

        for (int j = 0; j < columns; j++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                safeSet(i, j, function.evaluate(i, j, val));
            }
        }
    }

    @Override
    public void updateNonZeroInRow(int i, MatrixFunction function, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        for (int j = fromColumn; j < toColumn; j++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                safeSet(i, j, function.evaluate(i, j, val));
            }
        }
    }

    @Override
    public void updateNonZeroInColumn(int j, MatrixFunction function) {

        checkColumnBounds(j);

        for (int i = 0; i < rows; i++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                safeSet(i, j, function.evaluate(i, j, val));
            }
        }
    }

    @Override
    public void updateNonZeroInColumn(int j, MatrixFunction function, int fromRow, int toRow) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);

        for (int i = fromRow; i < toRow; i++) {
            final byte val = safeGet(i, j);
            if (!aIsEqualToB(val, (byte)0)) {
                safeSet(i, j, function.evaluate(i, j, val));
            }
        }
    }

    @Override
    public ByteMatrix transformNonZero(MatrixFunction function) {

        return transformNonZero(function, factory);
    }

    @Override
    public ByteMatrix transformNonZero(MatrixFunction function, Factory factory) {

        ByteMatrix result = copy(factory); // since it is a copy, we can use update methods

        if (result instanceof SparseByteMatrix) {
            ((SparseByteMatrix)result).updateNonZero(function);
        }
        else {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    final byte val = safeGet(i, j);
                    if (!aIsEqualToB(val, (byte)0)) {
                        result.set(i, j, function.evaluate(i, j, val));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public ByteMatrix transformNonZeroInRow(int i, MatrixFunction function) {

        return transformNonZeroInRow(i, function, factory);
    }

    @Override
    public ByteMatrix transformNonZeroInRow(int i, MatrixFunction function, Factory factory) {

        checkRowBounds(i);

        ByteMatrix result = copy(factory); // since it is a copy, we can use update methods

        if (result instanceof SparseByteMatrix) {
            ((SparseByteMatrix)result).updateNonZeroInRow(i, function);
        }
        else {
            for (int j = 0; j < columns; j++) {
                final byte val = safeGet(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    result.set(i, j, function.evaluate(i, j, val));
                }
            }
        }

        return result;
    }

    @Override
    public ByteMatrix transformNonZeroInRow(int i, MatrixFunction function, int fromColumn, int toColumn) {

        return transformNonZeroInRow(i, function, fromColumn, toColumn, factory);
    }

    @Override
    public ByteMatrix transformNonZeroInRow(
        int i,
        MatrixFunction function,
        int fromColumn,
        int toColumn,
        Factory factory)
    {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        ByteMatrix result = copy(factory); // since it is a copy, we can use update methods

        if (result instanceof SparseByteMatrix) {
            ((SparseByteMatrix)result).updateNonZeroInRow(i, function, fromColumn, toColumn);
        }
        else {
            for (int j = fromColumn; j < toColumn; j++) {
                final byte val = safeGet(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    result.set(i, j, function.evaluate(i, j, val));
                }
            }
        }

        return result;
    }

    @Override
    public ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function) {

        return transformNonZeroInColumn(j, function, factory);
    }

    @Override
    public ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function, Factory factory) {

        checkColumnBounds(j);

        ByteMatrix result = copy(factory); // since it is a copy, we can use update methods

        if (result instanceof SparseByteMatrix) {
            ((SparseByteMatrix)result).updateNonZeroInColumn(j, function);
        }
        else {
            for (int i = 0; i < rows; i++) {
                final byte val = safeGet(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    result.set(i, j, function.evaluate(i, j, val));
                }
            }
        }

        return result;
    }

    @Override
    public ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function, int fromRow, int toRow) {

        return transformNonZeroInColumn(j, function, fromRow, toRow, factory);
    }

    @Override
    public ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function, int fromRow, int toRow, Factory factory) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);

        ByteMatrix result = copy(factory); // since it is a copy, we can use update methods

        if (result instanceof SparseByteMatrix) {
            ((SparseByteMatrix)result).updateNonZeroInColumn(j, function, fromRow, toRow);
        }
        else {
            for (int i = fromRow; i < toRow; i++) {
                final byte val = safeGet(i, j);
                if (!aIsEqualToB(val, (byte)0)) {
                    result.set(i, j, function.evaluate(i, j, val));
                }
            }
        }

        return result;
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
}
