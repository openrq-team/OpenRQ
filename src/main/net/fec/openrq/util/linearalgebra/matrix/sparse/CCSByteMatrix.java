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
 * This is a CCS (Compressed Column Storage) matrix class.
 */
public class CCSByteMatrix extends AbstractCompressedByteMatrix implements SparseByteMatrix {

    private final SparseVectors sparseCols;


    public CCSByteMatrix() {

        this(0, 0);
    }

    public CCSByteMatrix(int rows, int columns, byte array[]) {

        this(ByteMatrices.asArray1DSource(rows, columns, array));
    }

    public CCSByteMatrix(ByteMatrix matrix) {

        this(ByteMatrices.asMatrixSource(matrix));
    }

    public CCSByteMatrix(byte array[][]) {

        this(ByteMatrices.asArray2DSource(array));
    }

    public CCSByteMatrix(MatrixSource source) {

        this(source.rows(), source.columns());

        for (int j = 0; j < columns(); j++) {
            sparseCols.initializeVector(j, ByteMatrices.asColumnVectorSource(source, j));
        }
    }

    public CCSByteMatrix(int rows, int columns) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseCols = new SparseVectors(columns, rows);
    }

    public CCSByteMatrix(int rows, int columns, byte rowValues[][], int rowIndices[][], int[] colCardinalities) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseCols = new SparseVectors(columns, rows, rowValues, rowIndices, colCardinalities);
    }

    private CCSByteMatrix(int rows, int columns, SparseVectors sparseCols) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        this.sparseCols = Objects.requireNonNull(sparseCols);
    }

    @Override
    public int cardinality() {

        int cardinality = 0;
        for (int i = 0; i < columns(); i++) {
            cardinality += sparseCols.vectorR(i).nonZeros();
        }

        return cardinality;
    }

    @Override
    public byte safeGet(int i, int j) {

        return sparseCols.vectorR(j).get(i);
    }

    @Override
    public void safeSet(int i, int j, byte value) {

        sparseCols.vectorRW(j).set(i, value);
    }

    @Override
    public void clear() {

        for (int j = 0; j < columns(); j++) {
            clearColumn(j);
        }
    }

    @Override
    public void clearColumn(int j) {

        sparseCols.vectorR(j).clear(); // this is non mutable on an empty vector
    }

    @Override
    public ByteMatrix transpose() {

        return transpose(factory());
    }

    @Override
    public ByteMatrix transpose(Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = factory.createMatrix(columns(), rows());

        for (int j = 0; j < columns(); j++) {
            ByteVectorIterator it = nonZeroColumnIterator(j);
            while (it.hasNext()) {
                it.next();
                result.set(j, it.index(), it.get());
            }
        }

        return result;
    }

    @Override
    public void divideColumnInPlace(int j, byte value) {

        Indexables.checkIndexBounds(j, columns());

        sparseCols.vectorRW(j).divideInPlace(value);
    }

    @Override
    public void divideColumnInPlace(int i, byte value, int fromRow, int toRow) {

        Indexables.checkIndexBounds(i, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());

        sparseCols.vectorRW(i).divideInPlace(value, fromRow, toRow);
    }

    @Override
    public ByteVector getColumn(int j) {

        Indexables.checkIndexBounds(j, columns());

        return sparseCols.vectorR(j).copy();
    }

    @Override
    public void swapColumns(int i, int j) {

        Indexables.checkIndexBounds(i, columns());
        Indexables.checkIndexBounds(j, columns());

        sparseCols.swapVectors(i, j);
    }

    @Override
    public void swapRows(int i, int j) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkIndexBounds(j, rows());

        if (i != j) {
            for (int col = 0; col < columns(); col++) {
                sparseCols.vectorR(col).swap(i, j); // vectorR because swap is non-destructive in empty vectors
            }
        }
    }

    @Override
    public ByteMatrix copy() {

        return new CCSByteMatrix(rows(), columns(), sparseCols.copy());
    }

    @Override
    public boolean nonZeroAt(int i, int j) {

        checkBounds(i, j);

        return sparseCols.vectorR(j).nonZeroAt(i);
    }

    @Override
    public int nonZerosInColumn(int j) {

        Indexables.checkIndexBounds(j, columns());

        return sparseCols.vectorR(j).nonZeros();
    }

    @Override
    public int nonZerosInColumn(int j, int fromRow, int toRow) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());

        return sparseCols.vectorR(j).nonZeros(fromRow, toRow);
    }

    @Override
    public int[] nonZeroPositionsInColumn(int j) {

        Indexables.checkIndexBounds(j, columns());

        return sparseCols.vectorR(j).nonZeroPositions();
    }

    @Override
    public int[] nonZeroPositionsInColumn(int j, int fromRow, int toRow) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());

        return sparseCols.vectorR(j).nonZeroPositions(fromRow, toRow);
    }

    @Override
    public void each(MatrixProcedure procedure) {

        for (int j = 0; j < columns(); j++) {
            ByteVectorIterator it = columnIterator(j);
            while (it.hasNext()) {
                it.next();
                procedure.apply(it.index(), j, it.get());
            }
        }
    }

    @Override
    public void eachNonZero(MatrixProcedure procedure) {

        for (int j = 0; j < columns(); j++) {
            ByteVectorIterator it = nonZeroColumnIterator(j);
            while (it.hasNext()) {
                it.next();
                procedure.apply(it.index(), j, it.get());
            }
        }
    }

    @Override
    public void safeUpdate(int i, int j, MatrixFunction function) {

        sparseCols.vectorRW(j).update(i, ByteMatrices.asColumnVectorFunction(function, j));
    }

    @Override
    public void updateNonZero(MatrixFunction function) {

        for (int j = 0; j < columns(); j++) {
            ByteVectorIterator it = nonZeroColumnIterator(j);
            while (it.hasNext()) {
                it.next();
                it.set(function.evaluate(it.index(), j, it.get()));
            }
        }
    }

    @Override
    public byte maxInColumn(int j) {

        byte max = foldNonZeroInColumn(j, ByteMatrices.mkMaxAccumulator());
        if (sparseCols.vectorR(j).nonZeros() == rows() || aIsGreaterThanB(max, (byte)0)) {
            return max;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte minInColumn(int j) {

        byte min = foldNonZeroInColumn(j, ByteMatrices.mkMinAccumulator());
        if (sparseCols.vectorR(j).nonZeros() == rows() || aIsLessThanB(min, (byte)0)) {
            return min;
        }
        else {
            return 0;
        }
    }

    @Override
    public ByteVectorIterator columnIterator(int j) {

        Indexables.checkIndexBounds(j, columns());
        return sparseCols.vectorRW(j).iterator();
    }

    @Override
    public ByteVectorIterator columnIterator(int j, int fromRow, int toRow) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());
        return sparseCols.vectorRW(j).iterator(fromRow, toRow);
    }

    @Override
    public ByteVectorIterator nonZeroColumnIterator(int j) {

        Indexables.checkIndexBounds(j, columns());
        return sparseCols.vectorRW(j).nonZeroIterator();
    }

    @Override
    public ByteVectorIterator nonZeroColumnIterator(int j, int fromRow, int toRow) {

        Indexables.checkIndexBounds(j, columns());
        Indexables.checkFromToBounds(fromRow, toRow, rows());
        return sparseCols.vectorRW(j).nonZeroIterator(fromRow, toRow);
    }

    @Override
    public ByteBuffer serializeToBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(getSerializedDataSize());
        Serialization.writeType(buffer, Serialization.Type.SPARSE_COLUMN_MATRIX);
        Serialization.writeMatrixRows(buffer, rows());
        Serialization.writeMatrixColumns(buffer, columns());

        for (int i = 0; i < columns(); i++) {
            Serialization.writeMatrixRowCardinality(buffer, nonZerosInColumn(i));
            ByteVectorIterator it = nonZeroColumnIterator(i);
            while (it.hasNext()) {
                it.next();
                Serialization.writeMatrixRowIndex(buffer, it.index());
                Serialization.writeMatrixValue(buffer, it.get());
            }
        }

        buffer.rewind();
        return buffer;
    }

    @Override
    public void serializeToChannel(WritableByteChannel ch) throws IOException {

        Serialization.writeType(ch, Serialization.Type.SPARSE_COLUMN_MATRIX);
        Serialization.writeMatrixRows(ch, rows());
        Serialization.writeMatrixColumns(ch, columns());

        for (int i = 0; i < columns(); i++) {
            Serialization.writeMatrixRowCardinality(ch, nonZerosInColumn(i));
            ByteVectorIterator it = nonZeroColumnIterator(i);
            while (it.hasNext()) {
                it.next();
                Serialization.writeMatrixRowIndex(ch, it.index());
                Serialization.writeMatrixValue(ch, it.get());
            }
        }
    }

    private int getSerializedDataSize() {

        final long dataSize = Serialization.SERIALIZATION_TYPE_NUMBYTES +
                              Serialization.MATRIX_ROWS_NUMBYTES +
                              Serialization.MATRIX_COLUMNS_NUMBYTES +
                              Serialization.MATRIX_COLUMN_CARDINALITY_NUMBYTES * (long)columns() +
                              Serialization.MATRIX_ROW_INDEX_NUMBYTES * (long)cardinality() +
                              cardinality();

        if (dataSize > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("matrix is too large to be serialized");
        }

        return (int)dataSize;
    }
}
