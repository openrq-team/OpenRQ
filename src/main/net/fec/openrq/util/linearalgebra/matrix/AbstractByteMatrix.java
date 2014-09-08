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
 * Contributor(s): Evgenia Krivova
 * Pavel Kalaidin
 * Jakob Moellers
 * Ewald Grusk
 * Yuriy Drozd
 * Maxim Samoylov
 * Anveshi Charuvaka
 * Todd Brunhoff
 */
package net.fec.openrq.util.linearalgebra.matrix;


import static net.fec.openrq.util.arithmetic.OctetOps.aDividedByB;
import static net.fec.openrq.util.arithmetic.OctetOps.aMinusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aPlusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;

import java.util.Random;

import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.matrix.functor.AdvancedMatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


public abstract class AbstractByteMatrix implements ByteMatrix {

    protected int rows;
    protected int columns;

    protected Factory factory;


    protected AbstractByteMatrix(Factory factory, int rows, int columns) {

        ensureDimensionsAreCorrect(rows, columns);

        this.factory = factory;

        this.rows = rows;
        this.columns = columns;
    }

    @Override
    public final byte get(int i, int j) {

        checkBounds(i, j);
        return safeGet(i, j);
    }

    protected abstract byte safeGet(int i, int j);

    @Override
    public final void set(int i, int j, byte value) {

        checkBounds(i, j);
        safeSet(i, j, value);
    }

    protected abstract void safeSet(int i, int j, byte value);

    @Override
    public final void update(int i, int j, MatrixFunction function) {

        checkBounds(i, j);
        safeUpdate(i, j, function);
    }

    protected void safeUpdate(int i, int j, MatrixFunction function) {

        safeSet(i, j, function.evaluate(i, j, safeGet(i, j)));
    }

    @Override
    public void assign(byte value) {

        update(ByteMatrices.asConstFunction(value));
    }

    @Override
    public void assignRow(int i, byte value) {

        updateRow(i, ByteMatrices.asConstFunction(value));
    }

    @Override
    public void assignColumn(int j, byte value) {

        updateColumn(j, ByteMatrices.asConstFunction(value));
    }

    @Override
    public int rows() {

        return rows;
    }

    @Override
    public int columns() {

        return columns;
    }

    @Override
    public ByteVector getRow(int i) {

        return getRow(i, factory);
    }

    @Override
    public ByteVector getRow(int i, Factory factory) {

        checkRowBounds(i);
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(columns);

        for (int j = 0; j < columns; j++) {
            result.set(j, safeGet(i, j));
        }

        return result;
    }

    @Override
    public ByteVector getRow(int i, int fromColumn, int toColumn) {

        return getRow(i, fromColumn, toColumn, factory);
    }

    @Override
    public ByteVector getRow(int i, int fromColumn, int toColumn, Factory factory) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(toColumn - fromColumn);

        for (int jg = fromColumn, js = 0; jg < toColumn; jg++, js++) {
            result.set(js, safeGet(i, jg));
        }

        return result;
    }

    @Override
    public ByteVector getColumn(int j) {

        return getColumn(j, factory);
    }

    @Override
    public ByteVector getColumn(int j, Factory factory) {

        checkColumnBounds(j);
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(rows);

        for (int i = 0; i < rows; i++) {
            result.set(i, safeGet(i, j));
        }

        return result;
    }

    @Override
    public ByteVector getColumn(int j, int fromRow, int toRow) {

        return getColumn(j, fromRow, toRow, factory);
    }

    @Override
    public ByteVector getColumn(int j, int fromRow, int toRow, Factory factory) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(toRow - fromRow);

        for (int ig = fromRow, is = 0; ig < toRow; ig++, is++) {
            result.set(is, safeGet(ig, j));
        }

        return result;
    }

    @Override
    public void setRow(int i, ByteVector row) {

        checkRowBounds(i);
        ensureArgumentIsNotNull(row, "vector");

        if (columns != row.length()) {
            fail("Wrong vector length: " + row.length() + ". Should be: " + columns + ".");
        }

        for (int j = 0; j < row.length(); j++) {
            safeSet(i, j, row.get(j));
        }
    }

    @Override
    public void setColumn(int j, ByteVector column) {

        checkColumnBounds(j);
        ensureArgumentIsNotNull(column, "vector");

        if (rows != column.length()) {
            fail("Wrong vector length: " + column.length() + ". Should be: " + rows + ".");
        }

        for (int i = 0; i < column.length(); i++) {
            safeSet(i, j, column.get(i));
        }
    }

    @Override
    public void swapRows(int i, int j) {

        checkRowBounds(i);
        checkRowBounds(j);

        if (i != j) {
            ByteVector ii = getRow(i);
            ByteVector jj = getRow(j);

            setRow(i, jj);
            setRow(j, ii);
        }
    }

    @Override
    public void swapColumns(int i, int j) {

        checkColumnBounds(i);
        checkColumnBounds(j);

        if (i != j) {
            ByteVector ii = getColumn(i);
            ByteVector jj = getColumn(j);

            setColumn(i, jj);
            setColumn(j, ii);
        }
    }

    @Override
    public ByteMatrix transpose() {

        return transpose(factory);
    }

    @Override
    public ByteMatrix transpose(Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = factory.createMatrix(columns, rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(j, i, safeGet(i, j));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix rotate() {

        return rotate(factory);
    }

    @Override
    public ByteMatrix rotate(Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = factory.createMatrix(columns, rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(j, rows - 1 - i, safeGet(i, j));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix power(int n) {

        return power(n, factory);
    }

    @Override
    public ByteMatrix power(int n, Factory factory) {

        if (n < 0) {
            fail("The exponent should be non-negative: " + n + ".");
        }

        ByteMatrix result = factory.createIdentityMatrix(rows);
        ByteMatrix that = this;

        while (n > 0) {
            if (n % 2 == 1) {
                result = result.multiply(that);
            }

            n /= 2;
            that = that.multiply(that);
        }

        return result;
    }

    @Override
    public ByteMatrix multiply(byte value) {

        return multiply(value, factory);
    }

    @Override
    public ByteMatrix multiply(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, aTimesB(safeGet(i, j), value));
            }
        }

        return result;
    }

    @Override
    public ByteVector multiply(ByteVector vector) {

        return multiply(vector, factory);
    }

    @Override
    public ByteVector multiply(ByteVector vector, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(vector, "vector");

        if (columns != vector.length()) {
            fail("Wrong vector length: " + vector.length() + ". Should be: " + columns + ".");
        }

        ByteVector result = factory.createVector(rows);

        for (int i = 0; i < rows; i++) {

            byte acc = 0;

            for (int j = 0; j < columns; j++) {
                final byte prod = aTimesB(safeGet(i, j), vector.get(j));
                acc = aPlusB(acc, prod);
            }

            result.set(i, acc);
        }

        return result;
    }

    @Override
    public ByteMatrix multiply(ByteMatrix matrix) {

        return multiply(matrix, factory);
    }

    @Override
    public ByteMatrix multiply(ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if (columns != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + columns + "x_.");
        }

        ByteMatrix result = factory.createMatrix(rows, matrix.columns());

        for (int j = 0; j < matrix.columns(); j++) {

            ByteVector column = matrix.getColumn(j);

            for (int i = 0; i < rows; i++) {

                byte acc = 0;

                for (int k = 0; k < columns; k++) {
                    final byte prod = aTimesB(safeGet(i, k), column.get(k));
                    acc = aPlusB(acc, prod);
                }

                result.set(i, j, acc);
            }
        }

        return result;
    }

    @Override
    public ByteMatrix subtract(byte value) {

        return subtract(value, factory);
    }

    @Override
    public ByteMatrix subtract(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, aMinusB(safeGet(i, j), value));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix subtract(ByteMatrix matrix) {

        return subtract(matrix, factory);
    }

    @Override
    public ByteMatrix subtract(ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if (rows != matrix.rows() || columns != matrix.columns()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + rows + "x" + columns + ".");
        }

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, aMinusB(safeGet(i, j), matrix.get(i, j)));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix add(byte value) {

        return add(value, factory);
    }

    @Override
    public ByteMatrix add(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, aPlusB(safeGet(i, j), value));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix add(ByteMatrix matrix) {

        return add(matrix, factory);
    }

    @Override
    public ByteMatrix add(ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if (rows != matrix.rows() || columns != matrix.columns()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + rows + "x" + columns + ".");
        }

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, aPlusB(safeGet(i, j), matrix.get(i, j)));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix divide(byte value) {

        return divide(value, factory);
    }

    @Override
    public ByteMatrix divide(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, aDividedByB(safeGet(i, j), value));
            }
        }

        return result;
    }

    @Override
    public byte trace() {

        byte result = 0;

        final int fence = Math.min(rows, columns);
        for (int i = 0; i < fence; i++) {
            result = aPlusB(result, safeGet(i, i));
        }

        return result;
    }

    @Override
    public byte diagonalProduct() {

        byte result = 1;

        final int fence = Math.min(rows, columns);
        for (int i = 0; i < fence; i++) {
            result = aTimesB(result, safeGet(i, i));
        }

        return result;
    }

    @Override
    public byte product() {

        return fold(ByteMatrices.asProductAccumulator((byte)1));
    }

    @Override
    public ByteMatrix hadamardProduct(ByteMatrix matrix) {

        return hadamardProduct(matrix, factory);
    }

    @Override
    public ByteMatrix hadamardProduct(ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if ((columns != matrix.columns()) || (rows != matrix.rows())) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + rows + "x" + columns + ".");
        }

        ByteMatrix result = factory.createMatrix(rows, columns);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, aTimesB(matrix.get(i, j), safeGet(i, j)));
            }
        }

        return result;
    }

    @Override
    public byte sum() {

        return fold(ByteMatrices.asSumAccumulator((byte)0));
    }

    @Override
    public ByteMatrix blank() {

        return blank(factory);
    }

    @Override
    public ByteMatrix blank(Factory factory) {

        ensureFactoryIsNotNull(factory);

        return factory.createMatrix(rows, columns);
    }

    @Override
    public ByteMatrix copy() {

        return copy(factory);
    }

    @Override
    public ByteMatrix copy(Factory factory) {

        ensureFactoryIsNotNull(factory);

        return factory.createMatrix(this);
    }

    @Override
    public ByteMatrix resize(int rows, int columns) {

        return resize(rows, columns, factory);
    }

    @Override
    public ByteMatrix resizeRows(int rows) {

        return resize(rows, columns, factory);
    }

    @Override
    public ByteMatrix resizeRows(int rows, Factory factory) {

        return resize(rows, columns, factory);
    }

    @Override
    public ByteMatrix resizeColumns(int columns) {

        return resize(rows, columns, factory);
    }

    @Override
    public ByteMatrix resizeColumns(int columns, Factory factory) {

        return resize(rows, columns, factory);
    }

    @Override
    public ByteMatrix resize(int rows, int columns, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = factory.createMatrix(rows, columns);

        for (int i = 0; i < Math.min(rows, this.rows); i++) {
            for (int j = 0; j < Math.min(columns, this.columns); j++) {
                result.set(i, j, safeGet(i, j));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix shuffle() {

        return shuffle(factory);
    }

    @Override
    public ByteMatrix shuffle(Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = copy(factory);

        // Conduct Fisher-Yates shuffle
        Random rnd = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int ii = rnd.nextInt(rows - i) + i;
                int jj = rnd.nextInt(columns - j) + j;

                byte a = result.get(ii, jj);
                result.set(ii, jj, result.get(i, j));
                result.set(i, j, a);
            }
        }

        return result;
    }

    @Override
    public ByteMatrix slice(
        int fromRow,
        int fromColumn,
        int untilRow,
        int untilColumn)
    {

        return slice(fromRow, fromColumn, untilRow, untilColumn, factory);
    }

    @Override
    public ByteMatrix slice(
        int fromRow,
        int fromColumn,
        int untilRow,
        int untilColumn,
        Factory factory)
    {

        checkRowRangeBounds(fromRow, untilRow);
        checkColumnRangeBounds(fromColumn, untilColumn);
        ensureFactoryIsNotNull(factory);

        ByteMatrix result = factory.createMatrix(untilRow - fromRow, untilColumn - fromColumn);

        for (int i = fromRow; i < untilRow; i++) {
            for (int j = fromColumn; j < untilColumn; j++) {
                result.set(i - fromRow, j - fromColumn, safeGet(i, j));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix sliceTopLeft(int untilRow, int untilColumn) {

        return slice(0, 0, untilRow, untilColumn, factory);
    }

    @Override
    public ByteMatrix sliceTopLeft(int untilRow, int untilColumn, Factory factory) {

        return slice(0, 0, untilRow, untilColumn, factory);
    }

    @Override
    public ByteMatrix sliceBottomRight(int fromRow, int fromColumn) {

        return slice(fromRow, fromColumn, rows, columns, factory);
    }

    @Override
    public ByteMatrix sliceBottomRight(int fromRow, int fromColumn, Factory factory) {

        return slice(fromRow, fromColumn, rows, columns, factory);
    }

    @Override
    public ByteMatrix select(int[] rowIndices, int[] columnIndices) {

        return select(rowIndices, columnIndices, factory);
    }

    @Override
    public ByteMatrix select(int[] rowIndices, int[] columnIndices, Factory factory) {

        int newRows = rowIndices.length;
        int newCols = columnIndices.length;

        if (newRows == 0 || newCols == 0) {
            fail("No rows or columns selected.");
        }

        ByteMatrix result = factory.createMatrix(newRows, newCols);

        for (int i = 0; i < newRows; i++) {
            for (int j = 0; j < newCols; j++) {
                result.set(i, j, get(rowIndices[i], columnIndices[j])); // use normal get for row/column bounds check
            }
        }

        return result;
    }

    @Override
    public Factory factory() {

        return factory;
    }

    @Override
    public int nonZeros() {

        int nonZeros = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (safeGet(i, j) != 0) {
                    nonZeros++;
                }
            }
        }

        return nonZeros;
    }

    @Override
    public int nonZerosInRow(int i) {

        checkRowBounds(i);

        int nonZeros = 0;
        for (int j = 0; j < columns; j++) {
            if (safeGet(i, j) != 0) {
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
            if (safeGet(i, j) != 0) {
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
            if (safeGet(i, j) != 0) {
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
            if (safeGet(i, j) != 0) {
                nonZeros++;
            }
        }

        return nonZeros;
    }

    @Override
    public void each(MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                procedure.apply(i, j, safeGet(i, j));
            }
        }
    }

    @Override
    public void eachInRow(int i, MatrixProcedure procedure) {

        checkRowBounds(i);
        for (int j = 0; j < columns; j++) {
            procedure.apply(i, j, safeGet(i, j));
        }
    }

    @Override
    public void eachInRow(int i, MatrixProcedure procedure, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);
        for (int j = fromColumn; j < toColumn; j++) {
            procedure.apply(i, j, safeGet(i, j));
        }
    }

    @Override
    public void eachInColumn(int j, MatrixProcedure procedure) {

        checkColumnBounds(j);
        for (int i = 0; i < rows; i++) {
            procedure.apply(i, j, safeGet(i, j));
        }
    }

    @Override
    public void eachInColumn(int j, MatrixProcedure procedure, int fromRow, int toRow) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);
        for (int i = fromRow; i < toRow; i++) {
            procedure.apply(i, j, safeGet(i, j));
        }
    }

    @Override
    public void update(MatrixFunction function) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                safeUpdate(i, j, function);
            }
        }
    }

    @Override
    public void updateRow(int i, MatrixFunction function) {

        checkRowBounds(i);
        for (int j = 0; j < columns; j++) {
            safeUpdate(i, j, function);
        }
    }

    @Override
    public void updateRow(int i, MatrixFunction function, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);
        for (int j = fromColumn; j < toColumn; j++) {
            safeUpdate(i, j, function);
        }
    }

    @Override
    public void updateColumn(int j, MatrixFunction function) {

        checkColumnBounds(j);
        for (int i = 0; i < rows; i++) {
            safeUpdate(i, j, function);
        }
    }

    @Override
    public void updateColumn(int j, MatrixFunction function, int fromRow, int toRow) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);
        for (int i = fromRow; i < toRow; i++) {
            safeUpdate(i, j, function);
        }
    }

    @Override
    public ByteMatrix transform(MatrixFunction function) {

        return transform(function, factory);
    }

    @Override
    public ByteMatrix transform(MatrixFunction function, Factory factory) {

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, function.evaluate(i, j, safeGet(i, j)));
            }
        }

        return result;
    }

    @Override
    public ByteMatrix transform(int i, int j, MatrixFunction function) {

        return transform(i, j, function, factory);
    }

    @Override
    public ByteMatrix transform(int i, int j, MatrixFunction function, Factory factory) {

        checkBounds(i, j);
        ByteMatrix result = copy(factory); // since it is a copy, we can use update method
        result.update(i, j, function);

        return result;
    }

    @Override
    public ByteMatrix transformRow(int i, MatrixFunction function) {

        return transformRow(i, function, factory);
    }

    @Override
    public ByteMatrix transformRow(int i, MatrixFunction function, Factory factory) {

        checkRowBounds(i);
        ByteMatrix result = copy(factory); // since it is a copy, we can use update method
        result.updateRow(i, function);

        return result;
    }

    @Override
    public ByteMatrix transformRow(int i, MatrixFunction function, int fromColumn, int toColumn) {

        return transformRow(i, function, fromColumn, toColumn, factory);
    }

    @Override
    public ByteMatrix transformRow(int i, MatrixFunction function, int fromColumn, int toColumn, Factory factory) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);
        ByteMatrix result = copy(factory); // since it is a copy, we can use update method
        result.updateRow(i, function, fromColumn, toColumn);

        return result;
    }

    @Override
    public ByteMatrix transformColumn(int j, MatrixFunction function) {

        return transformColumn(j, function, factory);
    }

    @Override
    public ByteMatrix transformColumn(int j, MatrixFunction function, Factory factory) {

        checkColumnBounds(j);
        ByteMatrix result = copy(factory); // since it is a copy, we can use update method
        result.updateColumn(j, function);

        return result;
    }

    @Override
    public ByteMatrix transformColumn(int j, MatrixFunction function, int fromRow, int toRow) {

        return transformColumn(j, function, fromRow, toRow, factory);
    }

    @Override
    public ByteMatrix transformColumn(int j, MatrixFunction function, int fromRow, int toRow, Factory factory) {

        checkColumnBounds(j);
        checkRowRangeBounds(fromRow, toRow);
        ByteMatrix result = copy(factory); // since it is a copy, we can use update method
        result.updateColumn(j, function, fromRow, toRow);

        return result;
    }

    @Override
    public byte fold(MatrixAccumulator accumulator) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                accumulator.update(i, j, safeGet(i, j));
            }
        }

        return accumulator.accumulate();
    }

    @Override
    public byte foldRow(int i, MatrixAccumulator accumulator) {

        checkRowBounds(i);
        for (int j = 0; j < columns; j++) {
            accumulator.update(i, j, safeGet(i, j));
        }

        return accumulator.accumulate();
    }

    @Override
    public ByteVector foldRows(MatrixAccumulator accumulator) {

        ByteVector result = factory.createVector(rows);

        for (int i = 0; i < rows; i++) {
            result.set(i, foldRow(i, accumulator));
        }

        return result;
    }

    @Override
    public byte foldColumn(int j, MatrixAccumulator accumulator) {

        checkColumnBounds(j);
        for (int i = 0; i < rows; i++) {
            accumulator.update(i, j, safeGet(i, j));
        }

        return accumulator.accumulate();
    }

    @Override
    public ByteVector foldColumns(MatrixAccumulator accumulator) {

        ByteVector result = factory.createVector(columns);

        for (int i = 0; i < columns; i++) {
            result.set(i, foldColumn(i, accumulator));
        }

        return result;
    }

    @Override
    public boolean is(MatrixPredicate predicate) {

        boolean result = predicate.test(rows, columns);

        for (int i = 0; result && i < rows; i++) {
            for (int j = 0; result && j < columns; j++) {
                result = predicate.test(i, j, safeGet(i, j));
            }
        }

        return result;
    }

    @Override
    public boolean is(AdvancedMatrixPredicate predicate) {

        return predicate.test(this);
    }

    @Override
    public boolean non(MatrixPredicate predicate) {

        return !is(predicate);
    }

    @Override
    public boolean non(AdvancedMatrixPredicate predicate) {

        return !is(predicate);
    }

    @Override
    public ByteVector toRowVector() {

        return toRowVector(factory);
    }

    @Override
    public ByteVector toRowVector(Factory factory) {

        return getRow(0, factory);
    }

    @Override
    public ByteVector toColumnVector() {

        return toColumnVector(factory);
    }

    @Override
    public ByteVector toColumnVector(Factory factory) {

        return getColumn(0, factory);
    }

    @Override
    public byte max() {

        return fold(ByteMatrices.mkMaxAccumulator());
    }

    @Override
    public byte min() {

        return fold(ByteMatrices.mkMinAccumulator());
    }

    @Override
    public byte maxInRow(int i) {

        return foldRow(i, ByteMatrices.mkMaxAccumulator());
    }

    @Override
    public byte minInRow(int i) {

        return foldRow(i, ByteMatrices.mkMinAccumulator());
    }

    @Override
    public byte maxInColumn(int j) {

        return foldColumn(j, ByteMatrices.mkMaxAccumulator());
    }

    @Override
    public byte minInColumn(int j) {

        return foldColumn(j, ByteMatrices.mkMinAccumulator());
    }

    @Override
    public int hashCode() {

        int result = 17;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                long value = safeGet(i, j);
                result = 37 * result + (int)(value ^ (value >>> 32));
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        // instanceof also checks for null
        if (!(object instanceof ByteMatrix)) {
            return false;
        }

        ByteMatrix matrix = (ByteMatrix)object;

        if (rows != matrix.rows() || columns != matrix.columns()) {
            return false;
        }

        boolean result = true;

        for (int i = 0; result && i < rows; i++) {
            for (int j = 0; result && j < columns; j++) {
                result = safeGet(i, j) == matrix.get(i, j);
            }
        }

        return result;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        ByteMatrices.printMatrix(this, sb);
        return sb.toString();
    }

    protected void ensureFactoryIsNotNull(Factory factory) {

        ensureArgumentIsNotNull(factory, "factory");
    }

    protected void ensureArgumentIsNotNull(Object argument, String name) {

        if (argument == null) {
            fail("Bad argument: \"" + name + "\" is 'null'.");
        }
    }

    protected void ensureDimensionsAreCorrect(int rows, int columns) {

        if (rows < 0 || columns < 0) {
            fail("Wrong matrix dimensions: " + rows + "x" + columns);
        }
        if (rows == Integer.MAX_VALUE || columns == Integer.MAX_VALUE) {
            fail("Wrong matrix dimensions: use 'Integer.MAX_VALUE - 1' instead.");
        }
    }

    protected void fail(String message) {

        throw new IllegalArgumentException(message);
    }

    protected void checkBounds(int row, int column) {

        checkRowBounds(row);
        checkColumnBounds(column);
    }

    protected void checkRowBounds(int row) {

        if (row < 0 || row >= rows()) throw new IndexOutOfBoundsException("row index is out of bounds");
    }

    protected void checkRowRangeBounds(int fromRow, int toRow) {

        if (fromRow < 0) throw new IndexOutOfBoundsException("fromRow < 0");
        if (toRow < fromRow) throw new IndexOutOfBoundsException("toRow < fromRow");
        if (rows() < toRow) throw new IndexOutOfBoundsException("rows() < toRow");
    }

    protected void checkColumnBounds(int column) {

        if (column < 0 || column >= columns()) throw new IndexOutOfBoundsException("column index is out of bounds");
    }

    protected void checkColumnRangeBounds(int fromColumn, int toColumn) {

        if (fromColumn < 0) throw new IndexOutOfBoundsException("fromColumn < 0");
        if (toColumn < fromColumn) throw new IndexOutOfBoundsException("toColumn < fromColumn");
        if (columns() < toColumn) throw new IndexOutOfBoundsException("columns() < toColumn");
    }
}
