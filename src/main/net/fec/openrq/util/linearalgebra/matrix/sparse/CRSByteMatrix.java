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


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import static net.fec.openrq.util.arithmetic.OctetOps.aIsGreaterThanB;
import static net.fec.openrq.util.arithmetic.OctetOps.aIsLessThanB;
import static net.fec.openrq.util.arithmetic.OctetOps.aPlusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;
import static net.fec.openrq.util.arithmetic.OctetOps.maxByte;
import static net.fec.openrq.util.arithmetic.OctetOps.minByte;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.factory.Factory;
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

    private static final long serialVersionUID = 4071505L;

    private static final int MINIMUM_SIZE = 32;

    private byte values[];
    private int columnIndices[];
    private int rowPointers[];


    public CRSByteMatrix() {

        this(0, 0);
    }

    public CRSByteMatrix(int rows, int columns) {

        this(rows, columns, 0);
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

        this(source.rows(), source.columns(), 0);

        for (int i = 0; i < rows; i++) {
            rowPointers[i] = cardinality;
            for (int j = 0; j < columns; j++) {
                byte value = source.get(i, j);
                if (!aIsEqualToB(value, (byte)0)) {

                    if (values.length < cardinality + 1) {
                        growup();
                    }

                    values[cardinality] = value;
                    columnIndices[cardinality] = j;
                    cardinality++;
                }
            }
        }

        rowPointers[rows] = cardinality;
    }

    public CRSByteMatrix(int rows, int columns, int cardinality) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        ensureCardinalityIsCorrect(rows, columns, cardinality);

        int alignedSize = align(cardinality);

        this.cardinality = 0;
        this.values = new byte[alignedSize];
        this.columnIndices = new int[alignedSize];
        this.rowPointers = new int[rows + 1];
    }

    public CRSByteMatrix(int rows,
        int columns,
        int cardinality,
        byte values[],
        int columnIndices[],
        int rowPointers[]) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        ensureCardinalityIsCorrect(rows, columns, cardinality);

        this.cardinality = cardinality;

        this.values = values;
        this.columnIndices = columnIndices;
        this.rowPointers = rowPointers;
    }

    @Override
    public byte safeGet(int i, int j) {

        int k = searchForColumnIndex(j, rowPointers[i], rowPointers[i + 1]);

        if (k < rowPointers[i + 1] && columnIndices[k] == j) {
            return values[k];
        }

        return 0;
    }

    @Override
    public void safeSet(int i, int j, byte value) {

        int k = searchForColumnIndex(j, rowPointers[i], rowPointers[i + 1]);

        if (k < rowPointers[i + 1] && columnIndices[k] == j) {
            if (aIsEqualToB(value, (byte)0)) {
                remove(k, i);
            }
            else {
                values[k] = value;
            }
        }
        else {
            insert(k, i, j, value);
        }
    }

    // =========================================================================
    // Optimized multiplications that take advantage of sparsity in this matrix.

    @Override
    public ByteMatrix multiply(byte value) {

        return multiply(value, factory);
    }

    @Override
    public ByteMatrix multiply(byte value, Factory factory) {

        ByteMatrix result = blank(factory);

        for (int i = 0; i < rows; i++) {
            for (int j = rowPointers[i]; j < rowPointers[i + 1]; j++) {
                final byte prod = aTimesB(value, values[j]);
                result.set(i, columnIndices[j], prod);
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

        final ByteMatrix result = factory.createMatrix(rows, matrix.columns());
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < result.columns(); j++) {
                byte acc = 0;
                for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
                    final byte prod = aTimesB(values[k], matrix.get(columnIndices[k], j));
                    acc = aPlusB(acc, prod);
                }

                if (!aIsEqualToB(acc, (byte)0)) {
                    result.set(i, j, acc);
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

        final ByteVector result = factory.createVector(rows);
        for (int i = 0; i < rows; i++) {
            byte acc = 0;
            for (int j = rowPointers[i]; j < rowPointers[i + 1]; j++) {
                final byte prod = aTimesB(values[j], vector.get(columnIndices[j]));
                acc = aPlusB(acc, prod);
            }

            if (!aIsEqualToB(acc, (byte)0)) {
                result.set(i, acc);
            }
        }

        return result;
    }

    // Optimized multiplications that take advantage of sparsity in this matrix.
    // =========================================================================

    @Override
    public ByteMatrix transpose(Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteMatrix result = factory.createMatrix(columns, rows);

        int nonZeroCount = 0, i = 0;
        while (nonZeroCount < cardinality) {
            for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++, nonZeroCount++) {
                result.set(columnIndices[k], i, values[k]);
            }
            i++;
        }

        return result;
    }

    @Override
    public ByteVector getRow(int i) {

        checkRowBounds(i);

        int rowCardinality = rowPointers[i + 1] - rowPointers[i];

        byte rowValues[] = new byte[rowCardinality];
        int rowIndices[] = new int[rowCardinality];

        System.arraycopy(values, rowPointers[i], rowValues, 0, rowCardinality);
        System.arraycopy(columnIndices, rowPointers[i], rowIndices, 0, rowCardinality);

        return new CompressedByteVector(columns, rowCardinality, rowValues, rowIndices);
    }

    @Override
    public ByteVector getRow(int i, Factory factory) {

        checkRowBounds(i);
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(columns);

        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            result.set(columnIndices[k], values[k]);
        }

        return result;
    }

    @Override
    public ByteVector getColumn(int j, Factory factory) {

        checkColumnBounds(j);
        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(rows);

        int i = 0;
        while (rowPointers[i] < cardinality) {

            int k = searchForColumnIndex(j, rowPointers[i], rowPointers[i + 1]);

            if (k < rowPointers[i + 1] && columnIndices[k] == j) {
                result.set(i, values[k]);
            }

            i++;
        }

        return result;
    }

    @Override
    public ByteMatrix copy() {

        byte $values[] = new byte[align(cardinality)];
        int $columnIndices[] = new int[align(cardinality)];
        int $rowPointers[] = new int[rows + 1];

        System.arraycopy(values, 0, $values, 0, cardinality);
        System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);
        System.arraycopy(rowPointers, 0, $rowPointers, 0, rows + 1);

        return new CRSByteMatrix(rows, columns, cardinality, $values, $columnIndices, $rowPointers);
    }

    @Override
    public ByteMatrix resize(int rows, int columns) {

        ensureDimensionsAreCorrect(rows, columns);

        if (this.rows == rows && this.columns == columns) {
            return copy();
        }

        if (this.rows >= rows && this.columns >= columns) {

            byte $values[] = new byte[align(cardinality)];
            int $columnIndices[] = new int[align(cardinality)];
            int $rowPointers[] = new int[rows + 1];

            int $cardinality = 0;

            int k = 0, i = 0;
            while (k < cardinality && i < rows) {

                $rowPointers[i] = $cardinality;

                for (int j = rowPointers[i]; j < rowPointers[i + 1]
                                             && columnIndices[j] < columns; j++, k++) {

                    $values[$cardinality] = values[j];
                    $columnIndices[$cardinality] = columnIndices[j];
                    $cardinality++;
                }
                i++;
            }

            $rowPointers[rows] = $cardinality;

            return new CRSByteMatrix(rows, columns, $cardinality, $values, $columnIndices, $rowPointers);
        }

        if (this.rows < rows) {
            byte $values[] = new byte[align(cardinality)];
            int $columnIndices[] = new int[align(cardinality)];
            int $rowPointers[] = new int[rows + 1];

            System.arraycopy(values, 0, $values, 0, cardinality);
            System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);
            System.arraycopy(rowPointers, 0, $rowPointers, 0, this.rows + 1);

            for (int i = this.rows; i < rows + 1; i++) {
                $rowPointers[i] = cardinality;
            }

            return new CRSByteMatrix(rows, columns, cardinality, $values, $columnIndices, $rowPointers);
        }

        // TODO: think about cardinality in align call
        byte $values[] = new byte[align(cardinality)];
        int $columnIndices[] = new int[align(cardinality)];
        int $rowPointers[] = new int[rows + 1];

        System.arraycopy(values, 0, $values, 0, cardinality);
        System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);
        System.arraycopy(rowPointers, 0, $rowPointers, 0, this.rows + 1);

        return new CRSByteMatrix(rows, columns, cardinality, $values, $columnIndices, $rowPointers);
    }

    @Override
    public boolean nonZeroAt(int i, int j) {

        checkBounds(i, j);
        final int k = searchForColumnIndex(j, rowPointers[i], rowPointers[i + 1]);
        return k < rowPointers[i + 1] && columnIndices[k] == j;
    }

    @Override
    public int nonZerosInRow(int i) {

        checkRowBounds(i);
        return rowPointers[i + 1] - rowPointers[i];
    }

    @Override
    public int nonZerosInRow(int i, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        int nonZeros = 0;
        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            final int col = columnIndices[k];
            if (fromColumn <= col) {
                if (col < toColumn) {
                    nonZeros++;
                }
                else {
                    break; // no need to check columns beyond the last one
                }
            }
        }

        return nonZeros;
    }

    @Override
    public void each(MatrixProcedure procedure) {

        int k = 0;
        for (int i = 0; i < rows; i++) {
            int valuesSoFar = rowPointers[i + 1];
            for (int j = 0; j < columns; j++) {
                if (k < valuesSoFar && j == columnIndices[k]) {
                    procedure.apply(i, j, values[k++]);
                }
                else {
                    procedure.apply(i, j, (byte)0);
                }
            }
        }
    }

    @Override
    public void eachInRow(int i, MatrixProcedure procedure) {

        checkRowBounds(i);

        int k = rowPointers[i];
        int valuesSoFar = rowPointers[i + 1];
        for (int j = 0; j < columns; j++) {
            if (k < valuesSoFar && j == columnIndices[k]) {
                procedure.apply(i, j, values[k++]);
            }
            else {
                procedure.apply(i, j, (byte)0);
            }
        }
    }

    @Override
    public void eachNonZero(MatrixProcedure procedure) {

        int nonZeroCount = 0, i = 0;
        while (nonZeroCount < cardinality) {
            for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++, nonZeroCount++) {
                procedure.apply(i, columnIndices[k], values[k]);
            }
            i++;
        }
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure) {

        checkRowBounds(i);
        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            procedure.apply(i, columnIndices[k], values[k]);
        }
    }

    @Override
    public void eachNonZeroInRow(int i, MatrixProcedure procedure, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            final int col = columnIndices[k];
            if (fromColumn <= col) {
                if (col < toColumn) {
                    procedure.apply(i, col, values[k]);
                }
                else {
                    break; // no need to check columns beyond the last one
                }
            }
        }
    }

    @Override
    public void safeUpdate(int i, int j, MatrixFunction function) {

        int k = searchForColumnIndex(j, rowPointers[i], rowPointers[i + 1]);

        if (k < rowPointers[i + 1] && columnIndices[k] == j) {
            final byte value = function.evaluate(i, j, values[k]);
            if (aIsEqualToB(value, (byte)0)) {
                remove(k, i);
            }
            else {
                values[k] = value;
            }
        }
        else {
            insert(k, i, j, function.evaluate(i, j, (byte)0));
        }
    }

    @Override
    public void updateNonZero(MatrixFunction function) {

        int nonZeroCount = 0, i = 0;
        while (nonZeroCount < cardinality) {
            for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++, nonZeroCount++) {
                final byte value = function.evaluate(i, columnIndices[k], values[k]);
                if (aIsEqualToB(value, (byte)0)) {
                    remove(k, i);
                    // since we removed a nonzero, the indices must be decremented accordingly
                    k--;
                    nonZeroCount--;
                }
                else {
                    values[k] = value;
                }
            }
            i++;
        }
    }

    @Override
    public void updateNonZeroInRow(int i, MatrixFunction function) {

        checkRowBounds(i);

        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            final byte value = function.evaluate(i, columnIndices[k], values[k]);
            if (aIsEqualToB(value, (byte)0)) {
                remove(k, i);
                // since we removed a nonzero, the index must be decremented accordingly
                k--;
            }
            else {
                values[k] = value;
            }
        }
    }

    @Override
    public void updateNonZeroInRow(int i, MatrixFunction function, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            final int col = columnIndices[k];
            if (fromColumn <= col) {
                if (col < toColumn) {
                    final byte value = function.evaluate(i, col, values[k]);
                    if (aIsEqualToB(value, (byte)0)) {
                        remove(k, i);
                        // since we removed a nonzero, the index must be decremented accordingly
                        k--;
                    }
                    else {
                        values[k] = value;
                    }
                }
                else {
                    break; // no need to check columns beyond the last one
                }
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(rows);
        out.writeInt(columns);
        out.writeInt(cardinality);

        int nonZeroCount = 0, i = 0;
        while (nonZeroCount < cardinality) {
            for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++, nonZeroCount++) {
                out.writeInt(i);
                out.writeInt(columnIndices[k]);
                out.writeByte(values[k]);
            }
            i++;
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {

        rows = in.readInt();
        columns = in.readInt();
        cardinality = in.readInt();

        int alignedSize = align(cardinality);

        values = new byte[alignedSize];
        columnIndices = new int[alignedSize];
        rowPointers = new int[rows + 1];

        for (int k = 0; k < cardinality; k++) {
            int i = in.readInt();
            columnIndices[k] = in.readInt();
            values[k] = in.readByte();
            rowPointers[i + 1] = k + 1;
        }
    }

    private int searchForColumnIndex(int j, int left, int right) {

        if (left == right) {
            return left;
        }

        if (right - left < 8) {

            int jj = left;
            while (jj < right && columnIndices[jj] < j) {
                jj++;
            }

            return jj;
        }

        int p = (left + right) / 2;

        if (columnIndices[p] > j) {
            return searchForColumnIndex(j, left, p);
        }
        else if (columnIndices[p] < j) {
            return searchForColumnIndex(j, p + 1, right);
        }
        else {
            return p;
        }
    }

    private void insert(int k, int i, int j, byte value) {

        if (aIsEqualToB(value, (byte)0)) {
            return;
        }

        if (values.length < cardinality + 1) {
            growup();
        }

        System.arraycopy(values, k, values, k + 1, cardinality - k);
        System.arraycopy(columnIndices, k, columnIndices, k + 1, cardinality - k);

        // for (int k = cardinality; k > position; k--) {
        // values[k] = values[k - 1];
        // columnIndices[k] = columnIndices[k - 1];
        // }

        values[k] = value;
        columnIndices[k] = j;

        for (int ii = i + 1; ii < rows + 1; ii++) {
            rowPointers[ii]++;
        }

        cardinality++;
    }

    private void remove(int k, int i) {

        cardinality--;

        System.arraycopy(values, k + 1, values, k, cardinality - k);
        System.arraycopy(columnIndices, k + 1, columnIndices, k, cardinality - k);

        // for (int kk = k; kk < cardinality; kk++) {
        // values[kk] = values[kk + 1];
        // columnIndices[kk] = columnIndices[kk + 1];
        // }

        for (int ii = i + 1; ii < rows + 1; ii++) {
            rowPointers[ii]--;
        }
    }

    private void growup() {

        if (values.length == capacity()) {
            // This should never happen
            throw new IllegalStateException("This matrix can't grow up.");
        }

        int min = (
                  (rows != 0 && columns > Integer.MAX_VALUE / rows) ?
                                                                   Integer.MAX_VALUE :
                                                                   (rows * columns)
                  );
        int capacity = Math.min(min, (cardinality * 3) / 2 + 1);

        byte $values[] = new byte[capacity];
        int $columnIndices[] = new int[capacity];

        System.arraycopy(values, 0, $values, 0, cardinality);
        System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);

        values = $values;
        columnIndices = $columnIndices;
    }

    private int align(int cardinality) {

        return ((cardinality / MINIMUM_SIZE) + 1) * MINIMUM_SIZE;
    }

    @Override
    public byte max() {

        byte max = minByte();

        for (int k = 0; k < cardinality; k++) {
            if (aIsGreaterThanB(values[k], max)) {
                max = values[k];
            }
        }

        if (cardinality == capacity() || aIsGreaterThanB(max, (byte)0)) {
            return max;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte min() {

        byte min = maxByte();

        for (int k = 0; k < cardinality; k++) {
            if (aIsLessThanB(values[k], min)) {
                min = values[k];
            }
        }

        if (cardinality == capacity() || aIsLessThanB(min, (byte)0)) {
            return min;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte maxInRow(int i) {

        checkRowBounds(i);

        byte max = minByte();

        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            if (aIsGreaterThanB(values[k], max)) {
                max = values[k];
            }
        }

        if (cardinality == capacity() || aIsGreaterThanB(max, (byte)0)) {
            return max;
        }
        else {
            return 0;
        }
    }

    @Override
    public byte minInRow(int i) {

        checkRowBounds(i);

        byte min = minByte();

        for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++) {
            if (aIsLessThanB(values[k], min)) {
                min = values[k];
            }
        }

        if (cardinality == capacity() || aIsLessThanB(min, (byte)0)) {
            return min;
        }
        else {
            return 0;
        }
    }

    /**
     * Returns a CRSMatrix with the selected rows and columns.
     */
    @Override
    public ByteMatrix select(int[] rowIndices, int[] columnIndices) {

        int newRows = rowIndices.length;
        int newCols = columnIndices.length;

        if (newRows == 0 || newCols == 0) {
            fail("No rows or columns selected.");
        }

        // determine number of non-zero values (cardinality)
        // before allocating space, this is perhaps more efficient
        // than single pass and calling grow() when required.
        int newCardinality = 0;
        for (int i = 0; i < newRows; i++) {
            for (int j = 0; j < newCols; j++) {
                if (!aIsEqualToB(get(rowIndices[i], columnIndices[j]), (byte)0)) {
                    newCardinality++;
                }
            }
        }

        // Construct the raw structure for the sparse matrix
        byte[] newValues = new byte[newCardinality];
        int[] newColumnIndices = new int[newCardinality];
        int[] newRowPointers = new int[newRows + 1];

        newRowPointers[0] = 0;
        int endPtr = 0;
        for (int i = 0; i < newRows; i++) {
            newRowPointers[i + 1] = newRowPointers[i];
            for (int j = 0; j < newCols; j++) {
                byte val = get(rowIndices[i], columnIndices[j]);
                if (!aIsEqualToB(val, (byte)0)) {
                    newValues[endPtr] = val;
                    newColumnIndices[endPtr] = j;
                    endPtr++;
                    newRowPointers[i + 1] += 1;

                }
            }
        }

        return new CRSByteMatrix(newRows, newCols, newCardinality, newValues, newColumnIndices, newRowPointers);
    }
}
