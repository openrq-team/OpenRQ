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


import static net.fec.openrq.util.linearalgebra.ByteOps.aDividedByB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aIsEqualToB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aMinusB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aPlusB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aTimesB;

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

        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(columns);

        for (int j = 0; j < columns; j++) {
            result.set(j, get(i, j));
        }

        return result;
    }

    @Override
    public ByteVector getColumn(int j) {

        return getColumn(j, factory);
    }

    @Override
    public ByteVector getColumn(int j, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(rows);

        for (int i = 0; i < rows; i++) {
            result.set(i, get(i, j));
        }

        return result;
    }

    @Override
    public void setRow(int i, ByteVector row) {

        ensureArgumentIsNotNull(row, "vector");

        if (columns != row.length()) {
            fail("Wrong vector length: " + row.length() + ". Should be: " + columns + ".");
        }

        for (int j = 0; j < row.length(); j++) {
            set(i, j, row.get(j));
        }
    }

    @Override
    public void setColumn(int j, ByteVector column) {

        ensureArgumentIsNotNull(column, "vector");

        if (rows != column.length()) {
            fail("Wrong vector length: " + column.length() + ". Should be: " + rows + ".");
        }

        for (int i = 0; i < column.length(); i++) {
            set(i, j, column.get(i));
        }
    }

    @Override
    public void swapRows(int i, int j) {

        if (i != j) {
            ByteVector ii = getRow(i);
            ByteVector jj = getRow(j);

            setRow(i, jj);
            setRow(j, ii);
        }
    }

    @Override
    public void swapColumns(int i, int j) {

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
                result.set(j, i, get(i, j));
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
                result.set(j, rows - 1 - i, get(i, j));
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
                result.set(i, j, aTimesB(get(i, j), value));
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
                final byte prod = aTimesB(get(i, j), vector.get(j));
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
                    final byte prod = aTimesB(get(i, k), column.get(k));
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
                result.set(i, j, aMinusB(get(i, j), value));
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
                result.set(i, j, aMinusB(get(i, j), matrix.get(i, j)));
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
                result.set(i, j, aPlusB(get(i, j), value));
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
                result.set(i, j, aPlusB(get(i, j), matrix.get(i, j)));
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
                result.set(i, j, aDividedByB(get(i, j), value));
            }
        }

        return result;
    }

    @Override
    public byte trace() {

        byte result = 0;

        final int fence = Math.min(rows, columns);
        for (int i = 0; i < fence; i++) {
            result = aPlusB(result, get(i, i));
        }

        return result;
    }

    @Override
    public byte diagonalProduct() {

        byte result = 1;

        final int fence = Math.min(rows, columns);
        for (int i = 0; i < fence; i++) {
            result = aTimesB(result, get(i, i));
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
                result.set(i, j, aTimesB(matrix.get(i, j), get(i, j)));
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
                result.set(i, j, get(i, j));
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
    public ByteMatrix slice(int fromRow, int fromColumn, int untilRow,
        int untilColumn) {

        return slice(fromRow, fromColumn, untilRow, untilColumn, factory);
    }

    @Override
    public ByteMatrix slice(int fromRow, int fromColumn, int untilRow,
        int untilColumn, Factory factory) {

        ensureFactoryIsNotNull(factory);

        if (untilRow - fromRow < 0 || untilColumn - fromColumn < 0) {
            fail("Wrong slice range: [" + fromRow + ".." + untilRow + "][" + fromColumn + ".." + untilColumn + "].");
        }

        ByteMatrix result = factory.createMatrix(untilRow - fromRow, untilColumn - fromColumn);

        for (int i = fromRow; i < untilRow; i++) {
            for (int j = fromColumn; j < untilColumn; j++) {
                result.set(i - fromRow, j - fromColumn, get(i, j));
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
                result.set(i, j, get(rowIndices[i], columnIndices[j]));
            }
        }

        return result;
    }

    @Override
    public Factory factory() {

        return factory;
    }

    @Override
    public void each(MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                procedure.apply(i, j, get(i, j));
            }
        }
    }

    @Override
    public void eachInRow(int i, MatrixProcedure procedure) {

        for (int j = 0; j < columns; j++) {
            procedure.apply(i, j, get(i, j));
        }
    }

    @Override
    public void eachInColumn(int j, MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            procedure.apply(i, j, get(i, j));
        }
    }

    @Override
    public void eachNonZero(MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (!aIsEqualToB(get(i, j), (byte)0)) {
                    procedure.apply(i, j, get(i, j));
                }
            }
        }
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure) {

        for (int j = 0; j < columns; j++) {
            if (!aIsEqualToB(get(i, j), (byte)0)) {
                procedure.apply(i, j, get(i, j));
            }
        }
    }

    @Override
    public void eachNonZeroInColumn(int j, MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            if (!aIsEqualToB(get(i, j), (byte)0)) {
                procedure.apply(i, j, get(i, j));
            }
        }
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
    public ByteMatrix transform(MatrixFunction function) {

        return transform(function, factory);
    }

    @Override
    public ByteMatrix transform(MatrixFunction function, Factory factory) {

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, function.evaluate(i, j, get(i, j)));
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

        ByteMatrix result = copy(factory);
        result.set(i, j, function.evaluate(i, j, result.get(i, j)));

        return result;
    }

    @Override
    public ByteMatrix transformRow(int i, MatrixFunction function) {

        return transformRow(i, function, factory);
    }

    @Override
    public ByteMatrix transformRow(int i, MatrixFunction function, Factory factory) {

        ByteMatrix result = copy(factory);

        for (int j = 0; j < columns; j++) {
            result.set(i, j, function.evaluate(i, j, result.get(i, j)));
        }

        return result;
    }

    @Override
    public ByteMatrix transformColumn(int j, MatrixFunction function) {

        return transformColumn(j, function, factory);
    }

    @Override
    public ByteMatrix transformColumn(int j, MatrixFunction function, Factory factory) {

        ByteMatrix result = copy(factory);

        for (int i = 0; i < rows; i++) {
            result.set(i, j, function.evaluate(i, j, result.get(i, j)));
        }

        return result;
    }

    @Override
    public void update(MatrixFunction function) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                set(i, j, function.evaluate(i, j, get(i, j)));
            }
        }
    }

    @Override
    public void update(int i, int j, MatrixFunction function) {

        set(i, j, function.evaluate(i, j, get(i, j)));
    }

    @Override
    public void updateRow(int i, MatrixFunction function) {

        for (int j = 0; j < columns; j++) {
            update(i, j, function);
        }
    }

    @Override
    public void updateColumn(int j, MatrixFunction function) {

        for (int i = 0; i < rows; i++) {
            update(i, j, function);
        }
    }

    @Override
    public byte fold(MatrixAccumulator accumulator) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                accumulator.update(i, j, get(i, j));
            }
        }

        return accumulator.accumulate();
    }

    @Override
    public byte foldRow(int i, MatrixAccumulator accumulator) {

        for (int j = 0; j < columns; j++) {
            accumulator.update(i, j, get(i, j));
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

        for (int i = 0; i < rows; i++) {
            accumulator.update(i, j, get(i, j));
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
                result = predicate.test(i, j, get(i, j));
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
    public int hashCode() {

        int result = 17;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                long value = get(i, j);
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
                result = aIsEqualToB(get(i, j), matrix.get(i, j));
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
}
