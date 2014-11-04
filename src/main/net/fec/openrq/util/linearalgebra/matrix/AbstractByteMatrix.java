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


import static net.fec.openrq.util.math.OctetOps.aDividedByB;
import static net.fec.openrq.util.math.OctetOps.aMinusB;
import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static net.fec.openrq.util.math.OctetOps.aTimesB;

import java.util.Random;

import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.functor.AdvancedMatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


public abstract class AbstractByteMatrix implements ByteMatrix {

    private final int rows;
    private final int columns;

    private final Factory factory;


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
    public void clear() {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                safeSet(i, j, (byte)0);
            }
        }
    }

    @Override
    public void clearRow(int i) {

        ByteVectorIterator it = rowIterator(i);
        while (it.hasNext()) {
            it.next();
            it.set((byte)0);
        }
    }

    @Override
    public void clearColumn(int j) {

        ByteVectorIterator it = columnIterator(j);
        while (it.hasNext()) {
            it.next();
            it.set((byte)0);
        }
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

        Indexables.checkIndexBounds(i, rows());
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(columns);

        ByteVectorIterator it = nonZeroRowIterator(i);
        while (it.hasNext()) {
            it.next();
            result.set(it.index(), it.get());
        }

        return result;
    }

    @Override
    public ByteVector getRow(int i, int fromColumn, int toColumn) {

        return getRow(i, fromColumn, toColumn, factory);
    }

    @Override
    public ByteVector getRow(int i, int fromColumn, int toColumn, Factory factory) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(toColumn - fromColumn);

        ByteVectorIterator it = nonZeroRowIterator(i, fromColumn, toColumn);
        while (it.hasNext()) {
            it.next();
            result.set(it.index() - fromColumn, it.get());
        }

        return result;
    }

    @Override
    public ByteVector getColumn(int j) {

        return getColumn(j, factory);
    }

    @Override
    public ByteVector getColumn(int j, Factory factory) {

        Indexables.checkIndexBounds(j, columns());
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(rows);

        ByteVectorIterator it = nonZeroColumnIterator(j);
        while (it.hasNext()) {
            it.next();
            result.set(it.index(), it.get());
        }

        return result;
    }

    @Override
    public ByteVector getColumn(int j, int fromRow, int toRow) {

        return getColumn(j, fromRow, toRow, factory);
    }

    @Override
    public ByteVector getColumn(int j, int fromRow, int toRow, Factory factory) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(toRow - fromRow);

        ByteVectorIterator it = nonZeroColumnIterator(j, fromRow, toRow);
        while (it.hasNext()) {
            it.next();
            result.set(it.index() - fromRow, it.get());
        }

        return result;
    }

    @Override
    public void setRow(int i, ByteVector row) {

        Indexables.checkIndexBounds(i, rows());
        ensureArgumentIsNotNull(row, "vector");

        if (columns != row.length()) {
            fail("Wrong vector length: " + row.length() + ". Should be: " + columns + ".");
        }

        ByteVectorIterator newIt = row.iterator();
        ByteVectorIterator oldIt = rowIterator(i);
        while (newIt.hasNext()) { // && oldIt.hasNext()
            newIt.next();
            oldIt.next();
            oldIt.set(newIt.get());
        }
    }

    @Override
    public void setRow(int i, int fromColumn, ByteVector row, int fromIndex, int length) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkOffsetLengthBounds(fromColumn, length, columns());
        Indexables.checkOffsetLengthBounds(fromIndex, length, row.length());

        ByteVectorIterator newIt = row.iterator(fromIndex, fromIndex + length);
        ByteVectorIterator oldIt = rowIterator(i, fromColumn, fromColumn + length);
        while (newIt.hasNext()) { // && oldIt.hasNext()
            newIt.next();
            oldIt.next();
            oldIt.set(newIt.get());
        }
    }

    @Override
    public void setColumn(int j, ByteVector column) {

        Indexables.checkIndexBounds(j, columns());
        ensureArgumentIsNotNull(column, "vector");

        if (rows != column.length()) {
            fail("Wrong vector length: " + column.length() + ". Should be: " + rows + ".");
        }

        ByteVectorIterator newIt = column.iterator();
        ByteVectorIterator oldIt = columnIterator(j);
        while (newIt.hasNext()) { // && oldIt.hasNext()
            newIt.next();
            oldIt.next();
            oldIt.set(newIt.get());
        }
    }

    @Override
    public void setColumn(int j, int fromRow, ByteVector column, int fromIndex, int length) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkOffsetLengthBounds(fromRow, length, rows());
        Indexables.checkOffsetLengthBounds(fromIndex, length, column.length());

        ByteVectorIterator newIt = column.iterator(fromIndex, fromIndex + length);
        ByteVectorIterator oldIt = columnIterator(j, fromRow, fromRow + length);
        while (newIt.hasNext()) { // && oldIt.hasNext()
            newIt.next();
            oldIt.next();
            oldIt.set(newIt.get());
        }
    }

    @Override
    public void swapRows(int i, int j) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkIndexBounds(j, rows());

        if (i != j) {
            ByteVector ii = getRow(i);
            ByteVector jj = getRow(j);

            setRow(i, jj);
            setRow(j, ii);
        }
    }

    @Override
    public void swapColumns(int i, int j) {

        Indexables.checkIndexBounds(i, columns());
        Indexables.checkIndexBounds(j, columns());

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

        if (value != 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    result.set(i, j, aTimesB(safeGet(i, j), value));
                }
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

            if (acc != 0) {
                result.set(i, acc);
            }
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

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < result.columns(); j++) {
                byte acc = 0;
                for (int k = 0; k < columns; k++) {
                    final byte prod = aTimesB(safeGet(i, k), matrix.get(k, j));
                    acc = aPlusB(acc, prod);
                }

                if (acc != 0) {
                    result.set(i, j, acc);
                }
            }
        }

        return result;
    }

    @Override
    public ByteMatrix multiply(
        ByteMatrix matrix,
        int fromThisRow,
        int toThisRow,
        int fromThisColumn,
        int toThisColumn,
        int fromOtherRow,
        int toOtherRow,
        int fromOtherColumn,
        int toOtherColumn)
    {

        return multiply(
            matrix,
            fromThisRow, toThisRow,
            fromThisColumn, toThisColumn,
            fromOtherRow, toOtherRow,
            fromOtherColumn, toOtherColumn,
            factory);
    }

    @Override
    public ByteMatrix multiply(
        ByteMatrix matrix,
        int fromThisRow,
        int toThisRow,
        int fromThisColumn,
        int toThisColumn,
        int fromOtherRow,
        int toOtherRow,
        int fromOtherColumn,
        int toOtherColumn,
        Factory factory)
    {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");
        Indexables.checkFromToBounds(fromThisRow, toThisRow, rows);
        Indexables.checkFromToBounds(fromThisColumn, toThisColumn, columns);
        Indexables.checkFromToBounds(fromOtherRow, fromOtherRow, matrix.rows());
        Indexables.checkFromToBounds(fromOtherColumn, toOtherColumn, matrix.columns());

        if ((toThisColumn - fromThisColumn) != (toOtherRow - fromOtherRow)) {
            fail("Wrong matrix dimensions: " +
                 (toOtherRow - fromOtherRow) + "x" + (toOtherColumn - fromOtherColumn) +
                 ". Should be: " + (toThisColumn - fromThisColumn) + "x_.");
        }

        ByteMatrix result = factory.createMatrix(toThisRow - fromThisRow, toOtherColumn - fromOtherColumn);

        for (int i = fromThisRow; i < toThisRow; i++) {
            for (int j = fromOtherColumn; j < toOtherColumn; j++) {
                byte acc = 0;
                for (int k = fromThisColumn; k < toThisColumn; k++) {
                    final byte prod = aTimesB(safeGet(i, k), matrix.get(k, j));
                    acc = aPlusB(acc, prod);
                }

                if (acc != 0) {
                    result.set(i - fromThisRow, j - fromOtherColumn, acc);
                }
            }
        }

        return result;
    }

    @Override
    public ByteVector multiplyRow(int i, ByteMatrix matrix) {

        return multiplyRow(i, matrix, factory);
    }

    @Override
    public ByteVector multiplyRow(int i, ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if (columns != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + columns + "x_.");
        }

        ByteVector result = factory.createVector(matrix.columns());

        for (int j = 0; j < matrix.columns(); j++) {
            byte acc = 0;

            ByteVectorIterator it = rowIterator(i);
            while (it.hasNext()) {
                it.next();
                final byte prod = aTimesB(it.get(), matrix.get(it.index(), j));
                acc = aPlusB(acc, prod);
            }

            result.set(j, acc);
        }

        return result;
    }

    @Override
    public ByteVector multiplyRow(int i, ByteMatrix matrix, int fromColumn, int toColumn) {

        return multiplyRow(i, matrix, fromColumn, toColumn, factory);
    }

    @Override
    public ByteVector multiplyRow(int i, ByteMatrix matrix, int fromColumn, int toColumn, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        if ((toColumn - fromColumn) != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + (toColumn - fromColumn) + "x_.");
        }

        ByteVector result = factory.createVector(matrix.columns());

        for (int j = 0; j < matrix.columns(); j++) {
            byte acc = 0;

            ByteVectorIterator it = rowIterator(i, fromColumn, toColumn);
            while (it.hasNext()) {
                it.next();
                final byte prod = aTimesB(it.get(), matrix.get(it.index() - fromColumn, j));
                acc = aPlusB(acc, prod);
            }

            result.set(j, acc);
        }

        return result;
    }

    @Override
    public ByteMatrix subtract(byte value) {

        return subtract(value, factory);
    }

    @Override
    public ByteMatrix subtract(ByteMatrix matrix) {

        return subtract(matrix, factory);
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
    public void divideInPlace(byte value) {

        if (value != 1) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    safeSet(i, j, aDividedByB(safeGet(i, j), value));
                }
            }
        }
    }

    @Override
    public void divideRowInPlace(int i, byte value) {

        Indexables.checkIndexBounds(i, rows());

        if (value != 1) {
            ByteVectorIterator it = rowIterator(i);
            while (it.hasNext()) {
                it.next();
                it.set(aDividedByB(it.get(), value));
            }
        }
    }

    @Override
    public void divideRowInPlace(int i, byte value, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        if (value != 1) {
            ByteVectorIterator it = rowIterator(i, fromColumn, toColumn);
            while (it.hasNext()) {
                it.next();
                it.set(aDividedByB(it.get(), value));
            }
        }
    }

    @Override
    public void divideColumnInPlace(int j, byte value) {

        Indexables.checkIndexBounds(j, columns());

        if (value != 1) {
            ByteVectorIterator it = columnIterator(j);
            while (it.hasNext()) {
                it.next();
                it.set(aDividedByB(it.get(), value));
            }
        }
    }

    @Override
    public void divideColumnInPlace(int j, byte value, int fromRow, int toRow) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());

        if (value != 1) {
            ByteVectorIterator it = columnIterator(j, fromRow, toRow);
            while (it.hasNext()) {
                it.next();
                it.set(aDividedByB(it.get(), value));
            }
        }
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

        Indexables.checkFromToBounds(fromRow, untilRow, rows());
        Indexables.checkFromToBounds(fromColumn, untilColumn, columns());
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
    public boolean isZeroAt(int i, int j) {

        checkBounds(i, j);
        return safeGet(i, j) == 0;
    }

    @Override
    public boolean nonZeroAt(int i, int j) {

        checkBounds(i, j);
        return safeGet(i, j) != 0;
    }

    @Override
    public int nonZeros() {

        int nonZeros = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (nonZeroAt(i, j)) {
                    nonZeros++;
                }
            }
        }

        return nonZeros;
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
    public int[] nonZeroPositionsInRow(int i) {

        int[] positions = new int[nonZerosInRow(i)];

        int n = 0;
        ByteVectorIterator it = nonZeroRowIterator(i);
        while (it.hasNext()) {
            it.next();
            positions[n++] = it.index();
        }

        return positions;
    }

    @Override
    public int[] nonZeroPositionsInRow(int i, int fromColumn, int toColumn) {

        int[] positions = new int[nonZerosInRow(i, fromColumn, toColumn)];

        int n = 0;
        ByteVectorIterator it = nonZeroRowIterator(i, fromColumn, toColumn);
        while (it.hasNext()) {
            it.next();
            positions[n++] = it.index();
        }

        return positions;
    }

    @Override
    public int nonZerosInColumn(int j) {

        ByteVectorIterator it = nonZeroColumnIterator(j);
        int nonZeros = 0;
        while (it.hasNext()) {
            it.next();
            nonZeros++;
        }

        return nonZeros;
    }

    @Override
    public int nonZerosInColumn(int j, int fromRow, int toRow) {

        ByteVectorIterator it = nonZeroColumnIterator(j, fromRow, toRow);
        int nonZeros = 0;
        while (it.hasNext()) {
            it.next();
            nonZeros++;
        }

        return nonZeros;
    }

    @Override
    public int[] nonZeroPositionsInColumn(int j) {

        int[] positions = new int[nonZerosInColumn(j)];

        int n = 0;
        ByteVectorIterator it = nonZeroColumnIterator(j);
        while (it.hasNext()) {
            it.next();
            positions[n++] = it.index();
        }

        return positions;
    }

    @Override
    public int[] nonZeroPositionsInColumn(int j, int fromRow, int toRow) {

        int[] positions = new int[nonZerosInColumn(j, fromRow, toRow)];

        int n = 0;
        ByteVectorIterator it = nonZeroColumnIterator(j, fromRow, toRow);
        while (it.hasNext()) {
            it.next();
            positions[n++] = it.index();
        }

        return positions;
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

        ByteVectorIterator it = rowIterator(i);
        while (it.hasNext()) {
            it.next();
            procedure.apply(i, it.index(), it.get());
        }
    }

    @Override
    public void eachInRow(int i, MatrixProcedure procedure, int fromColumn, int toColumn) {

        ByteVectorIterator it = rowIterator(i, fromColumn, toColumn);
        while (it.hasNext()) {
            it.next();
            procedure.apply(i, it.index(), it.get());
        }
    }

    @Override
    public void eachInColumn(int j, MatrixProcedure procedure) {

        ByteVectorIterator it = columnIterator(j);
        while (it.hasNext()) {
            it.next();
            procedure.apply(it.index(), j, it.get());
        }
    }

    @Override
    public void eachInColumn(int j, MatrixProcedure procedure, int fromRow, int toRow) {

        ByteVectorIterator it = columnIterator(j, fromRow, toRow);
        while (it.hasNext()) {
            it.next();
            procedure.apply(it.index(), j, it.get());
        }
    }

    @Override
    public void eachNonZero(MatrixProcedure procedure) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final byte val = safeGet(i, j);
                if (val != 0) {
                    procedure.apply(i, j, val);
                }
            }
        }
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
    public void eachNonZeroInColumn(int j, MatrixProcedure procedure) {

        ByteVectorIterator it = nonZeroColumnIterator(j);
        while (it.hasNext()) {
            it.next();
            procedure.apply(it.index(), j, it.get());
        }
    }

    @Override
    public void eachNonZeroInColumn(int j, MatrixProcedure procedure, int fromRow, int toRow) {

        ByteVectorIterator it = nonZeroColumnIterator(j, fromRow, toRow);
        while (it.hasNext()) {
            it.next();
            procedure.apply(it.index(), j, it.get());
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

        ByteVectorIterator it = rowIterator(i);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(i, it.index(), it.get()));
        }
    }

    @Override
    public void updateRow(int i, MatrixFunction function, int fromColumn, int toColumn) {

        ByteVectorIterator it = rowIterator(i, fromColumn, toColumn);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(i, it.index(), it.get()));
        }
    }

    @Override
    public void updateColumn(int j, MatrixFunction function) {

        ByteVectorIterator it = columnIterator(j);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(it.index(), j, it.get()));
        }
    }

    @Override
    public void updateColumn(int j, MatrixFunction function, int fromRow, int toRow) {

        ByteVectorIterator it = columnIterator(j, fromRow, toRow);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(it.index(), j, it.get()));
        }
    }

    @Override
    public void updateNonZero(MatrixFunction function) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final byte val = safeGet(i, j);
                if (val != 0) {
                    safeSet(i, j, function.evaluate(i, j, val));
                }
            }
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
    public void updateNonZeroInColumn(int j, MatrixFunction function) {

        ByteVectorIterator it = nonZeroColumnIterator(j);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(it.index(), j, it.get()));
        }
    }

    @Override
    public void updateNonZeroInColumn(int j, MatrixFunction function, int fromRow, int toRow) {

        ByteVectorIterator it = nonZeroColumnIterator(j, fromRow, toRow);
        while (it.hasNext()) {
            it.next();
            it.set(function.evaluate(it.index(), j, it.get()));
        }
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

        ByteVectorIterator it = rowIterator(i);
        while (it.hasNext()) {
            it.next();
            accumulator.update(i, it.index(), it.get());
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

        ByteVectorIterator it = columnIterator(j);
        while (it.hasNext()) {
            it.next();
            accumulator.update(it.index(), j, it.get());
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
    public ByteVectorIterator rowIterator(int i) {

        Indexables.checkIndexBounds(i, rows());
        return new RowIterator(i, 0, columns());
    }

    @Override
    public ByteVectorIterator rowIterator(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());
        return new RowIterator(i, fromColumn, toColumn);
    }


    private final class RowIterator extends AbstractIterator {

        /*
         * Requires valid indices.
         */
        RowIterator(int i, int fromColumn, int toColumn) {

            super(i, fromColumn, toColumn);
        }

        @Override
        protected byte fcGet(int i, int j) {

            return safeGet(i, j);
        }

        @Override
        protected void fcSet(int i, int j, byte value) {

            safeSet(i, j, value);
        }
    }


    @Override
    public ByteVectorIterator columnIterator(int j) {

        Indexables.checkIndexBounds(j, columns());
        return new ColumnIterator(j, 0, rows());
    }

    @Override
    public ByteVectorIterator columnIterator(int j, int fromRow, int toRow) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());
        return new ColumnIterator(j, fromRow, toRow);
    }


    private final class ColumnIterator extends AbstractIterator {

        /*
         * Requires valid indices.
         */
        ColumnIterator(int j, int fromRow, int toRow) {

            super(j, fromRow, toRow);
        }

        @Override
        protected byte fcGet(int j, int i) {

            return safeGet(i, j);
        }

        @Override
        protected void fcSet(int j, int i, byte value) {

            safeSet(i, j, value);
        }
    }

    /**
     * Abstract iterator superclass for both row and column iterator classes.
     */
    private abstract class AbstractIterator extends ByteVectorIterator {

        private final int fixed;
        private int cursor;
        private final int end;


        /*
         * Requires valid indices.
         */
        protected AbstractIterator(int fixed, int fromCursor, int toCursor) {

            super(toCursor - fromCursor);

            this.fixed = fixed;
            this.cursor = fromCursor - 1;
            this.end = toCursor;
        }

        protected abstract byte fcGet(int fixed, int cursor);

        protected abstract void fcSet(int fixed, int cursor, byte value);

        @Override
        public byte get() {

            return fcGet(fixed, cursor);
        }

        @Override
        public void set(byte value) {

            fcSet(fixed, cursor, value);
        }

        @Override
        public int index() {

            return cursor;
        }

        @Override
        public Byte next() {

            cursor++;
            return get();
        }

        @Override
        public boolean hasNext() {

            return cursor + 1 < end;
        }
    }


    @Override
    public ByteVectorIterator nonZeroRowIterator(final int i) {

        Indexables.checkIndexBounds(i, rows());
        return new NonZeroRowIterator(i, 0, columns());
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(final int i, final int fromColumn, final int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());
        return new NonZeroRowIterator(i, fromColumn, toColumn);
    }


    private final class NonZeroRowIterator extends AbstractNonZeroIterator {

        /*
         * Requires valid indices.
         */
        NonZeroRowIterator(int i, int fromColumn, int toColumn) {

            super(i, fromColumn, toColumn);
        }

        @Override
        protected byte fcGet(int i, int nextJ) {

            return safeGet(i, nextJ);
        }

        @Override
        protected void fcSet(int i, int nextJ, byte value) {

            safeSet(i, nextJ, value);
        }

        @Override
        protected boolean fcNonZeroAt(int i, int j) {

            return nonZeroAt(i, j);
        }
    }


    @Override
    public ByteVectorIterator nonZeroColumnIterator(int j) {

        Indexables.checkIndexBounds(j, columns());
        return new NonZeroColumnIterator(j, 0, rows());
    }

    @Override
    public ByteVectorIterator nonZeroColumnIterator(int j, int fromRow, int toRow) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());
        return new NonZeroColumnIterator(j, fromRow, toRow);
    }


    private final class NonZeroColumnIterator extends AbstractNonZeroIterator {

        /*
         * Requires valid indices.
         */
        NonZeroColumnIterator(int j, int fromRow, int toRow) {

            super(j, fromRow, toRow);
        }

        @Override
        protected byte fcGet(int j, int nextI) {

            return safeGet(nextI, j);
        }

        @Override
        protected void fcSet(int j, int nextI, byte value) {

            safeSet(nextI, j, value);
        }

        @Override
        protected boolean fcNonZeroAt(int j, int i) {

            return nonZeroAt(i, j);
        }
    }

    /**
     * Abstract non zero iterator superclass for both row and column non zero iterator classes.
     */
    private abstract class AbstractNonZeroIterator extends ByteVectorIterator {

        private final int fixed;
        private int cursor;
        private final int end;
        private int nextCursor;
        private boolean hasNext;


        /*
         * Requires valid indices.
         */
        protected AbstractNonZeroIterator(int fixed, int fromCursor, int toCursor) {

            super(toCursor - fromCursor);

            this.fixed = fixed;
            this.cursor = -1;
            this.end = toCursor;
            this.nextCursor = fromCursor;

            findNextNonZero(); // this method initializes nextCursor and hasNext properly
        }

        protected abstract byte fcGet(int fixed, int nextCursor);

        protected abstract void fcSet(int fixed, int nextCursor, byte value);

        protected abstract boolean fcNonZeroAt(int fixed, int cursor);

        @Override
        public int index() {

            return cursor;
        }

        @Override
        public byte get() {

            return fcGet(fixed, cursor);
        }

        @Override
        public void set(byte value) {

            fcSet(fixed, cursor, value);
        }

        @Override
        public Byte next() {

            cursor = nextCursor - 1;
            findNextNonZero();
            return get();
        }

        @Override
        public boolean hasNext() {

            return hasNext;
        }

        private void findNextNonZero() {

            hasNext = false;
            while (nextCursor < end) {
                hasNext = fcNonZeroAt(fixed, nextCursor++);
                if (hasNext) {
                    break;
                }
            }
        }
    }


    @Override
    public void addRowsInPlace(int srcRow, int destRow) {

        Indexables.checkIndexBounds(srcRow, rows());
        Indexables.checkIndexBounds(destRow, rows());

        ByteVectorIterator srcIt = rowIterator(srcRow);
        ByteVectorIterator destIt = rowIterator(destRow);
        while (srcIt.hasNext()) { // && destIt.hasNext()
            srcIt.next();
            destIt.next();
            destIt.set(aPlusB(srcIt.get(), destIt.get()));
        }
    }

    @Override
    public void addRowsInPlace(int srcRow, int destRow, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(srcRow, rows());
        Indexables.checkIndexBounds(destRow, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        ByteVectorIterator srcIt = rowIterator(srcRow, fromColumn, toColumn);
        ByteVectorIterator destIt = rowIterator(destRow, fromColumn, toColumn);
        while (srcIt.hasNext()) { // && destIt.hasNext()
            srcIt.next();
            destIt.next();
            destIt.set(aPlusB(srcIt.get(), destIt.get()));
        }
    }

    @Override
    public void addRowsInPlace(byte srcMultiplier, int srcRow, int destRow) {

        if (srcMultiplier != 0) { // if the multiplier is zero, then nothing needs to be added to row2
            if (srcMultiplier == 1) { // if the multiplier is one, then a product is not required
                addRowsInPlace(srcRow, destRow);
            }
            else {
                Indexables.checkIndexBounds(srcRow, rows());
                Indexables.checkIndexBounds(destRow, rows());

                ByteVectorIterator srcIt = rowIterator(srcRow);
                ByteVectorIterator destIt = rowIterator(destRow);
                while (srcIt.hasNext()) { // && destIt.hasNext()
                    srcIt.next();
                    destIt.next();
                    final byte prod = aTimesB(srcMultiplier, srcIt.get());
                    destIt.set(aPlusB(prod, destIt.get()));
                }
            }
        }
    }

    @Override
    public void addRowsInPlace(byte srcMultiplier, int srcRow, int destRow, int fromColumn, int toColumn) {

        if (srcMultiplier != 0) { // if the multiplier is zero, then nothing needs to be added to row2
            if (srcMultiplier == 1) { // if the multiplier is one, then a product is not required
                addRowsInPlace(srcRow, destRow, fromColumn, toColumn);
            }
            else {
                Indexables.checkIndexBounds(srcRow, rows());
                Indexables.checkIndexBounds(destRow, rows());
                Indexables.checkFromToBounds(fromColumn, toColumn, columns());

                ByteVectorIterator srcIt = rowIterator(srcRow, fromColumn, toColumn);
                ByteVectorIterator destIt = rowIterator(destRow, fromColumn, toColumn);
                while (srcIt.hasNext()) { // && destIt.hasNext()
                    srcIt.next();
                    destIt.next();
                    final byte prod = aTimesB(srcMultiplier, srcIt.get());
                    destIt.set(aPlusB(prod, destIt.get()));
                }
            }
        }
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

        Indexables.checkIndexBounds(row, rows());
        Indexables.checkIndexBounds(column, columns());
    }
}
