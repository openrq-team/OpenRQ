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
 * Contributor(s): Chandler May
 * Maxim Samoylov
 * Anveshi Charuvaka
 * Clement Skau
 * Catherine da Graca
 */
package net.fec.openrq.util.linearalgebra.matrix.sparse;


import static net.fec.openrq.util.arithmetic.OctetOps.aIsGreaterThanB;
import static net.fec.openrq.util.arithmetic.OctetOps.aIsLessThanB;
import static net.fec.openrq.util.arithmetic.OctetOps.aPlusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;

import java.util.Objects;

import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.sparse.CompressedByteVector;


/**
 * This is a CRS (Compressed Row Storage) matrix class.
 */
public class CRSByteMatrix extends AbstractCompressedByteMatrix implements SparseByteMatrix {

    private final SparseRows sparseRows;


    public CRSByteMatrix() {

        this(0, 0);
    }

    public CRSByteMatrix(int rows, int columns, byte array[]) {

        this(ByteMatrices.asArray1DSource(rows, columns, array));
    }

    public CRSByteMatrix(ByteMatrix matrix) {

        this(ByteMatrices.asMatrixSource(matrix));
    }

    public CRSByteMatrix(byte array[][]) {

        this(ByteMatrices.asArray2DSource(array));
    }

    public CRSByteMatrix(MatrixSource source) {

        this(source.rows(), source.columns());

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                byte value = source.get(i, j);
                if (value != 0) {
                    sparseRows.set(i, j, value);
                }
            }
        }
    }

    public CRSByteMatrix(int rows, int columns) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseRows = new SparseRows(rows);
    }

    public CRSByteMatrix(int rows, int columns, byte columnValues[][], int columnIndices[][], int[] rowCardinalities) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseRows = new SparseRows(columnValues, columnIndices, rowCardinalities);
    }

    private CRSByteMatrix(int rows, int columns, SparseRows sparseRows) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseRows = Objects.requireNonNull(sparseRows);
    }

    @Override
    public int cardinality() {

        int cardinality = 0;
        for (int i = 0; i < rows(); i++) {
            cardinality += sparseRows.nonZeros(i);
        }

        return cardinality;
    }

    @Override
    public byte safeGet(int i, int j) {

        return sparseRows.get(i, j);
    }

    @Override
    public void safeSet(int i, int j, byte value) {

        sparseRows.set(i, j, value);
    }

    // =========================================================================
    // Optimized multiplications that take advantage of row sparsity in matrix.

    @Override
    public ByteMatrix multiply(byte value) {

        return multiply(value, factory);
    }

    @Override
    public ByteMatrix multiply(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = blank(factory);

        if (value != 0) {
            for (int i = 0; i < rows(); i++) {
                ByteVectorIterator it = nonZeroRowIterator(i);
                while (it.hasNext()) {
                    it.next();
                    final byte prod = aTimesB(value, it.get());
                    result.set(i, it.index(), prod);
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

        if (columns() != vector.length()) {
            fail("Wrong vector length: " + vector.length() + ". Should be: " + columns() + ".");
        }

        ByteVector result = factory.createVector(rows());

        for (int i = 0; i < rows(); i++) {
            byte acc = 0;
            ByteVectorIterator it = nonZeroRowIterator(i);
            while (it.hasNext()) {
                it.next();
                final byte prod = aTimesB(it.get(), vector.get(it.index()));
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

        if (columns() != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + columns() + "x_.");
        }

        final ByteMatrix result = factory.createMatrix(rows(), matrix.columns());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < result.columns(); j++) {
                byte acc = 0;
                ByteVectorIterator it = nonZeroRowIterator(i);
                while (it.hasNext()) {
                    it.next();
                    final byte prod = aTimesB(it.get(), matrix.get(it.index(), j));
                    acc = aPlusB(acc, prod);
                }

                if (acc != 0) {
                    result.set(i, j, acc);
                }
            }
        }

        return result;
    }

    // Optimized multiplications that take advantage of row sparsity in matrix.
    // =========================================================================

    @Override
    public ByteMatrix transpose() {

        return transpose(factory);
    }

    @Override
    public ByteMatrix transpose(Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = factory.createMatrix(columns(), rows());

        for (int i = 0; i < rows(); i++) {
            ByteVectorIterator it = nonZeroRowIterator(i);
            while (it.hasNext()) {
                it.next();
                result.set(it.index(), i, it.get());
            }
        }

        return result;
    }

    @Override
    public ByteVector getRow(int i) {

        Indexables.checkIndexBounds(i, rows());

        return new CompressedByteVector(
            columns(),
            sparseRows.nonZeros(i),
            sparseRows.copyOfValues(i),
            sparseRows.copyOfColumnIndices(i));
    }

    @Override
    public void swapRows(int i, int j) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkIndexBounds(j, rows());

        sparseRows.swapRows(i, j);
    }

    @Override
    public void swapColumns(int i, int j) {

        Indexables.checkIndexBounds(i, columns());
        Indexables.checkIndexBounds(j, columns());

        if (i != j) {
            final int left = Math.min(i, j);
            final int right = Math.max(i, j);
            for (int row = 0; row < rows(); row++) {
                sparseRows.swapElements(row, left, right);
            }
        }
    }

    @Override
    public ByteMatrix copy() {

        return new CRSByteMatrix(rows(), columns(), sparseRows.clone());
    }

    @Override
    public boolean nonZeroAt(int i, int j) {

        checkBounds(i, j);

        return sparseRows.isNonZero(i, j);
    }

    @Override
    public int nonZerosInRow(int i) {

        Indexables.checkIndexBounds(i, rows());

        return sparseRows.nonZeros(i);
    }

    @Override
    public int nonZerosInRow(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        return sparseRows.nonZeros(i, fromColumn, toColumn);
    }

    @Override
    public int[] nonZeroPositionsInRow(int i) {

        Indexables.checkIndexBounds(i, rows());

        return sparseRows.copyOfColumnIndices(i);
    }

    @Override
    public int[] nonZeroPositionsInRow(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        return sparseRows.copyOfColumnIndices(i, fromColumn, toColumn);
    }

    @Override
    public void each(MatrixProcedure procedure) {

        for (int i = 0; i < rows(); i++) {
            ByteVectorIterator it = rowIterator(i);
            while (it.hasNext()) {
                it.next();
                procedure.apply(i, it.index(), it.get());
            }
        }
    }

    @Override
    public void eachNonZero(MatrixProcedure procedure) {

        for (int i = 0; i < rows(); i++) {
            ByteVectorIterator it = nonZeroRowIterator(i);
            while (it.hasNext()) {
                it.next();
                procedure.apply(i, it.index(), it.get());
            }
        }
    }

    @Override
    public void safeUpdate(int i, int j, MatrixFunction function) {

        sparseRows.update(i, j, function);
    }

    @Override
    public void updateNonZero(MatrixFunction function) {

        for (int i = 0; i < rows(); i++) {
            ByteVectorIterator it = nonZeroRowIterator(i);
            while (it.hasNext()) {
                it.next();
                it.set(function.evaluate(i, it.index(), it.get()));
            }
        }
    }

    @Override
    public void addRowsInPlace(int row1, int row2) {

        // TODO Auto-generated method stub
        super.addRowsInPlace(row1, row2);
    }

    @Override
    public void addRowsInPlace(int row1, int row2, int fromColumn, int toColumn) {

        // TODO Auto-generated method stub
        super.addRowsInPlace(row1, row2, fromColumn, toColumn);
    }

    @Override
    public void addRowsInPlace(byte row1Multiplier, int row1, int row2) {

        // TODO Auto-generated method stub
        super.addRowsInPlace(row1Multiplier, row1, row2);
    }

    @Override
    public void addRowsInPlace(byte row1Multiplier, int row1, int row2, int fromColumn, int toColumn) {

        // TODO Auto-generated method stub
        super.addRowsInPlace(row1Multiplier, row1, row2, fromColumn, toColumn);
    }

    @Override
    public byte maxInRow(int i) {

        byte max = foldNonZeroInRow(i, ByteMatrices.mkMaxAccumulator());
        if (sparseRows.nonZeros(i) == columns() || aIsGreaterThanB(max, (byte)0)) {
            return max;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte minInRow(int i) {

        byte min = foldNonZeroInRow(i, ByteMatrices.mkMinAccumulator());
        if (sparseRows.nonZeros(i) == columns() || aIsLessThanB(min, (byte)0)) {
            return min;
        }
        else {
            return 0;
        }
    }

    @Override
    public ByteVectorIterator rowIterator(int i) {

        Indexables.checkIndexBounds(i, rows());
        return sparseRows.iterator(i, 0, columns());
    }

    @Override
    public ByteVectorIterator rowIterator(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());
        return sparseRows.iterator(i, fromColumn, toColumn);
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(int i) {

        Indexables.checkIndexBounds(i, rows());
        return sparseRows.nonZeroIterator(i, 0, columns());
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());
        return sparseRows.nonZeroIterator(i, fromColumn, toColumn);
    }
}
