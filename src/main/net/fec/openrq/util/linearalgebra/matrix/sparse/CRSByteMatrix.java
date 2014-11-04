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
 * Contributor(s): Chandler May
 * Maxim Samoylov
 * Anveshi Charuvaka
 * Clement Skau
 * Catherine da Graca
 */
package net.fec.openrq.util.linearalgebra.matrix.sparse;


import static net.fec.openrq.util.math.OctetOps.aIsGreaterThanB;
import static net.fec.openrq.util.math.OctetOps.aIsLessThanB;
import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static net.fec.openrq.util.math.OctetOps.aTimesB;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
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
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


/**
 * This is a CRS (Compressed Row Storage) matrix class.
 */
public class CRSByteMatrix extends AbstractCompressedByteMatrix implements SparseByteMatrix {

    private final SparseVectors sparseRows;


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
            sparseRows.initializeVector(i, ByteMatrices.asRowVectorSource(source, i));
        }
    }

    public CRSByteMatrix(int rows, int columns) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseRows = new SparseVectors(rows, columns);
    }

    public CRSByteMatrix(int rows, int columns, byte columnValues[][], int columnIndices[][], int[] rowCardinalities) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseRows = new SparseVectors(rows, columns, columnValues, columnIndices, rowCardinalities);
    }

    private CRSByteMatrix(int rows, int columns, SparseVectors sparseRows) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseRows = Objects.requireNonNull(sparseRows);
    }

    @Override
    public int cardinality() {

        int cardinality = 0;
        for (int i = 0; i < rows(); i++) {
            cardinality += sparseRows.vectorR(i).nonZeros();
        }

        return cardinality;
    }

    @Override
    public byte safeGet(int i, int j) {

        return sparseRows.vectorR(i).get(j);
    }

    @Override
    public void safeSet(int i, int j, byte value) {

        sparseRows.vectorRW(i).set(j, value);
    }

    @Override
    public void clear() {

        for (int i = 0; i < rows(); i++) {
            clearRow(i);
        }
    }

    @Override
    public void clearRow(int i) {

        sparseRows.vectorR(i).clear(); // this is non mutable on an empty vector
    }

    // =========================================================================
    // Optimized multiplications that take advantage of row sparsity in matrix.

    @Override
    public ByteMatrix multiply(byte value) {

        return multiply(value, factory());
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

        return multiply(vector, factory());
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

        return multiply(matrix, factory());
    }

    @Override
    public ByteMatrix multiply(ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if (columns() != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + columns() + "x_.");
        }

        ByteMatrix result = factory.createMatrix(rows(), matrix.columns());

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
            factory());
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
        Indexables.checkFromToBounds(fromThisRow, toThisRow, rows());
        Indexables.checkFromToBounds(fromThisColumn, toThisColumn, columns());
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
                ByteVectorIterator it = nonZeroRowIterator(i, fromThisColumn, toThisColumn);
                while (it.hasNext()) {
                    it.next();
                    final byte prod = aTimesB(it.get(), matrix.get(it.index(), j));
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

        return multiplyRow(i, matrix, factory());
    }

    @Override
    public ByteVector multiplyRow(int i, ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if (columns() != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + columns() + "x_.");
        }

        ByteVector result = factory.createVector(matrix.columns());

        for (int j = 0; j < matrix.columns(); j++) {
            byte acc = 0;

            ByteVectorIterator it = nonZeroRowIterator(i);
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

        return multiplyRow(i, matrix, fromColumn, toColumn, factory());
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

            ByteVectorIterator it = nonZeroRowIterator(i, fromColumn, toColumn);
            while (it.hasNext()) {
                it.next();
                final byte prod = aTimesB(it.get(), matrix.get(it.index() - fromColumn, j));
                acc = aPlusB(acc, prod);
            }

            result.set(j, acc);
        }

        return result;
    }

    // Optimized multiplications that take advantage of row sparsity in matrix.
    // =========================================================================

    @Override
    public ByteMatrix transpose() {

        return transpose(factory());
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
    public void divideRowInPlace(int i, byte value) {

        Indexables.checkIndexBounds(i, rows());

        sparseRows.vectorRW(i).divideInPlace(value);
    }

    @Override
    public void divideRowInPlace(int i, byte value, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        sparseRows.vectorRW(i).divideInPlace(value, fromColumn, toColumn);
    }

    @Override
    public ByteVector getRow(int i) {

        Indexables.checkIndexBounds(i, rows());

        return sparseRows.vectorR(i).copy();
    }

    @Override
    public void swapRows(int i, int j) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkIndexBounds(j, rows());

        sparseRows.swapVectors(i, j);
    }

    @Override
    public void swapColumns(int i, int j) {

        Indexables.checkIndexBounds(i, columns());
        Indexables.checkIndexBounds(j, columns());

        if (i != j) {
            for (int row = 0; row < rows(); row++) {
                sparseRows.vectorR(row).swap(i, j); // vectorR because swap is non-destructive in empty vectors
            }
        }
    }

    @Override
    public ByteMatrix copy() {

        return new CRSByteMatrix(rows(), columns(), sparseRows.copy());
    }

    @Override
    public boolean nonZeroAt(int i, int j) {

        checkBounds(i, j);

        return sparseRows.vectorR(i).nonZeroAt(j);
    }

    @Override
    public int nonZerosInRow(int i) {

        Indexables.checkIndexBounds(i, rows());

        return sparseRows.vectorR(i).nonZeros();
    }

    @Override
    public int nonZerosInRow(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        return sparseRows.vectorR(i).nonZeros(fromColumn, toColumn);
    }

    @Override
    public int[] nonZeroPositionsInRow(int i) {

        Indexables.checkIndexBounds(i, rows());

        return sparseRows.vectorR(i).nonZeroPositions();
    }

    @Override
    public int[] nonZeroPositionsInRow(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        return sparseRows.vectorR(i).nonZeroPositions(fromColumn, toColumn);
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

        sparseRows.vectorRW(i).update(j, ByteMatrices.asRowVectorFunction(function, i));
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
    public void addRowsInPlace(int srcRow, int destRow) {

        Indexables.checkIndexBounds(srcRow, rows());
        Indexables.checkIndexBounds(destRow, rows());

        sparseRows.vectorRW(destRow).addInPlace(sparseRows.vectorR(srcRow));
    }

    @Override
    public void addRowsInPlace(int srcRow, int destRow, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(srcRow, rows());
        Indexables.checkIndexBounds(destRow, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        sparseRows.vectorRW(destRow).addInPlace(sparseRows.vectorR(srcRow), fromColumn, toColumn);
    }

    @Override
    public void addRowsInPlace(byte srcMultiplier, int srcRow, int destRow) {

        Indexables.checkIndexBounds(srcRow, rows());
        Indexables.checkIndexBounds(destRow, rows());

        sparseRows.vectorRW(destRow).addInPlace(srcMultiplier, sparseRows.vectorR(srcRow));
    }

    @Override
    public void addRowsInPlace(byte srcMultiplier, int srcRow, int destRow, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(srcRow, rows());
        Indexables.checkIndexBounds(destRow, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());

        sparseRows.vectorRW(destRow).addInPlace(srcMultiplier, sparseRows.vectorR(srcRow), fromColumn, toColumn);
    }

    @Override
    public byte maxInRow(int i) {

        byte max = foldNonZeroInRow(i, ByteMatrices.mkMaxAccumulator());
        if (sparseRows.vectorR(i).nonZeros() == columns() || aIsGreaterThanB(max, (byte)0)) {
            return max;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte minInRow(int i) {

        byte min = foldNonZeroInRow(i, ByteMatrices.mkMinAccumulator());
        if (sparseRows.vectorR(i).nonZeros() == columns() || aIsLessThanB(min, (byte)0)) {
            return min;
        }
        else {
            return 0;
        }
    }

    @Override
    public ByteVectorIterator rowIterator(int i) {

        Indexables.checkIndexBounds(i, rows());
        return sparseRows.vectorRW(i).iterator();
    }

    @Override
    public ByteVectorIterator rowIterator(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());
        return sparseRows.vectorRW(i).iterator(fromColumn, toColumn);
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(int i) {

        Indexables.checkIndexBounds(i, rows());
        return sparseRows.vectorRW(i).nonZeroIterator();
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(int i, int fromColumn, int toColumn) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkFromToBounds(fromColumn, toColumn, columns());
        return sparseRows.vectorRW(i).nonZeroIterator(fromColumn, toColumn);
    }

    @Override
    public ByteBuffer serializeToBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(getSerializedDataSize());
        Serialization.writeType(buffer, Serialization.Type.SPARSE_ROW_MATRIX);
        Serialization.writeMatrixRows(buffer, rows());
        Serialization.writeMatrixColumns(buffer, columns());

        for (int i = 0; i < rows(); i++) {
            Serialization.writeMatrixRowCardinality(buffer, nonZerosInRow(i));
            ByteVectorIterator it = nonZeroRowIterator(i);
            while (it.hasNext()) {
                it.next();
                Serialization.writeMatrixColumnIndex(buffer, it.index());
                Serialization.writeMatrixValue(buffer, it.get());
            }
        }

        buffer.rewind();
        return buffer;
    }

    @Override
    public void serializeToChannel(WritableByteChannel ch) throws IOException {

        Serialization.writeType(ch, Serialization.Type.SPARSE_ROW_MATRIX);
        Serialization.writeMatrixRows(ch, rows());
        Serialization.writeMatrixColumns(ch, columns());

        for (int i = 0; i < rows(); i++) {
            Serialization.writeMatrixRowCardinality(ch, nonZerosInRow(i));
            ByteVectorIterator it = nonZeroRowIterator(i);
            while (it.hasNext()) {
                it.next();
                Serialization.writeMatrixColumnIndex(ch, it.index());
                Serialization.writeMatrixValue(ch, it.get());
            }
        }
    }

    private int getSerializedDataSize() {

        final long dataSize = Serialization.SERIALIZATION_TYPE_NUMBYTES +
                              Serialization.MATRIX_ROWS_NUMBYTES +
                              Serialization.MATRIX_COLUMNS_NUMBYTES +
                              Serialization.MATRIX_ROW_CARDINALITY_NUMBYTES * (long)rows() +
                              Serialization.MATRIX_COLUMN_INDEX_NUMBYTES * (long)cardinality() +
                              cardinality();

        if (dataSize > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("matrix is too large to be serialized");
        }

        return (int)dataSize;
    }
}
