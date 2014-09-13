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
import static net.fec.openrq.util.arithmetic.OctetOps.maxByte;
import static net.fec.openrq.util.arithmetic.OctetOps.minByte;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.fec.openrq.util.array.ArrayUtils;
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
                if (value != 0) {

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
            if (value == 0) {
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
            for (int i = 0; i < rows; i++) {
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

        if (columns != vector.length()) {
            fail("Wrong vector length: " + vector.length() + ". Should be: " + columns + ".");
        }

        ByteVector result = factory.createVector(rows);

        for (int i = 0; i < rows; i++) {
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

        if (columns != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + columns + "x_.");
        }

        final ByteMatrix result = factory.createMatrix(rows, matrix.columns());
        for (int i = 0; i < rows; i++) {
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
    public void swapRows(int i, int j) {

        checkRowBounds(i);
        checkRowBounds(j);

        if (i != j) {
            // swap the column indices from the two rows
            ArrayUtils.swapBlocks(columnIndices, rowPointers[i], rowPointers[i + 1], rowPointers[j], rowPointers[j + 1]);

            // do the same for the values
            ArrayUtils.swapBlocks(values, rowPointers[i], rowPointers[i + 1], rowPointers[j], rowPointers[j + 1]);

            // figure out the difference between the new and the old cardinality of the row with lowest index
            final int low = Math.min(i, j);
            final int lowCardinality = rowPointers[low + 1] - rowPointers[low];
            final int high = Math.max(i, j);
            final int highCardinality = rowPointers[high + 1] - rowPointers[high];
            final int diff = highCardinality - lowCardinality; // may be negative

            // add the difference to all rows between low and high
            // indices + 1 since the first position of rowPointers is always 0
            for (int n = low + 1; n < high + 1; n++) {
                rowPointers[n] += diff;
            }
        }
    }

    @Override
    public void swapColumns(int i, int j) {

        checkColumnBounds(i);
        checkColumnBounds(j);

        if (i != j) {
            final int left = Math.min(i, j);
            final int right = Math.max(i, j);

            for (int row = 0; row < rows(); row++) {
                // positions of the left and right column indices in the columnIndices/values arrays
                int leftPos = -1;
                int rightPos = -1;

                // search in the current row for the left and right column indices over the non zero values
                // and store their positions in the columnIndices array
                for (int k = rowPointers[row]; k < rowPointers[row + 1]; k++) {
                    if (columnIndices[k] == left) {
                        leftPos = k;
                    }
                    else if (columnIndices[k] == right) {
                        rightPos = k;
                        break; // indices beyond the rightmost one do not matter
                    }
                }

                // if both positions were found
                if (leftPos != -1 && rightPos != -1) {
                    // only need to swap the non zero values, since the column indices remain fixed
                    ArrayUtils.swapBytes(values, leftPos, rightPos);
                }
                else if (leftPos != -1) { // if only the left position was found
                    columnIndices[leftPos] = right; // swap a zero value with a non zero
                    // now we need to carry the updated column index to its correct position
                    for (int k = leftPos; k + 1 < rowPointers[row + 1]; k++) {
                        if (columnIndices[k] > columnIndices[k + 1]) {
                            ArrayUtils.swapInts(columnIndices, k, k + 1);
                            ArrayUtils.swapBytes(values, k, k + 1); // mirror the operation on the values array
                        }
                        else {
                            break; // it is already at the correct position
                        }
                    }
                }
                else if (rightPos != -1) { // if only the right position was found
                    columnIndices[rightPos] = left; // swap a zero value with a non zero
                    // now we need to carry the updated column index to its correct position
                    for (int k = rightPos; k - 1 >= rowPointers[row]; k--) {
                        if (columnIndices[k] < columnIndices[k - 1]) {
                            ArrayUtils.swapInts(columnIndices, k, k - 1);
                            ArrayUtils.swapBytes(values, k, k - 1); // mirror the operation on the values array
                        }
                        else {
                            break; // it is already at the correct position
                        }
                    }
                }
                // else nothing needs to be swapped
            }
        }
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

        int nonZeros = rowPointers[i + 1] - rowPointers[i]; // upper bound

        // discount non zeros to the left of the range
        int k = rowPointers[i];
        while (k < rowPointers[i + 1] && columnIndices[k] < fromColumn) {
            k++;
            nonZeros--;
        }

        // discount non zeros to the right of the ranges
        k = rowPointers[i + 1] - 1;
        while (k >= rowPointers[i] && columnIndices[k] >= toColumn) {
            k--;
            nonZeros--;
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
    public void safeUpdate(int i, int j, MatrixFunction function) {

        int k = searchForColumnIndex(j, rowPointers[i], rowPointers[i + 1]);

        if (k < rowPointers[i + 1] && columnIndices[k] == j) {
            final byte value = function.evaluate(i, j, values[k]);
            if (value == 0) {
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
                if (value == 0) {
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

        if (value == 0) {
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
                if (get(rowIndices[i], columnIndices[j]) != 0) {
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
                if (val != 0) {
                    newValues[endPtr] = val;
                    newColumnIndices[endPtr] = j;
                    endPtr++;
                    newRowPointers[i + 1] += 1;

                }
            }
        }

        return new CRSByteMatrix(newRows, newCols, newCardinality, newValues, newColumnIndices, newRowPointers);
    }

    @Override
    public ByteVectorIterator rowIterator(int i) {

        checkRowBounds(i);
        return new RowIterator(i, 0, columns());
    }

    @Override
    public ByteVectorIterator rowIterator(int i, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);
        return new RowIterator(i, fromColumn, toColumn);
    }


    private final class RowIterator extends ByteVectorIterator {

        private final int i;
        private int j;
        private final int end;
        private int k;


        /*
         * Requires valid indices.
         */
        RowIterator(int i, int fromColumn, int toColumn) {

            super(toColumn - fromColumn);

            this.i = i;
            this.j = fromColumn - 1;
            this.end = toColumn;

            setKToWithinRange(fromColumn);
        }

        private void setKToWithinRange(int fromColumn) {

            /*
             * only need to check the starting index
             * if columnIndices[k] >= toColumn, then k will never be used
             */

            k = rowPointers[i];
            while (k < rowPointers[i + 1] && columnIndices[k] < fromColumn) {
                k++;
            }
        }

        @Override
        public int index() {

            return j;
        }

        @Override
        public byte get() {

            if (k < rowPointers[i + 1] && columnIndices[k] == j) {
                return values[k];
            }
            return 0;
        }

        @Override
        public void set(byte value) {

            if (k < rowPointers[i + 1] && columnIndices[k] == j) {
                if (value == 0) {
                    CRSByteMatrix.this.remove(k, i);
                }
                else {
                    values[k] = value;
                }
            }
            else {
                CRSByteMatrix.this.insert(k, i, j, value);
            }
        }

        @Override
        public boolean hasNext() {

            return j + 1 < end;
        }

        @Override
        public Byte next() {

            j++;
            if (k < rowPointers[i + 1] && columnIndices[k] == j - 1) {
                k++;
            }
            return get();
        }

        @Override
        protected int innerCursor() {

            return k;
        }
    }


    @Override
    public ByteVectorIterator nonZeroRowIterator(int i) {

        checkRowBounds(i);
        return new NonZeroRowIterator(i, 0, columns());
    }

    @Override
    public ByteVectorIterator nonZeroRowIterator(int i, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);
        return new NonZeroRowIterator(i, fromColumn, toColumn);
    }


    private final class NonZeroRowIterator extends ByteVectorIterator {

        private final int i;
        private boolean currentIsRemoved = false;
        private int removedIndex;
        private final int end;
        private int k;


        /*
         * Requires valid indices.
         */
        NonZeroRowIterator(int i, int fromColumn, int toColumn) {

            super(toColumn - fromColumn);

            this.i = i;
            this.currentIsRemoved = false;
            this.removedIndex = -1;
            this.end = toColumn;

            setKToWithinRange(fromColumn);
        }

        private void setKToWithinRange(int fromColumn) {

            /*
             * only need to check the starting index
             * if columnIndices[k] >= toColumn, then k will never be used
             */

            int kk = rowPointers[i];
            while (kk < rowPointers[i + 1] && columnIndices[kk] < fromColumn) {
                kk++;
            }

            k = kk - 1; // start at kk - 1 so the first next() call can increment k to kk
        }

        @Override
        public int index() {

            return currentIsRemoved ? removedIndex : columnIndices[k];
        }

        @Override
        public byte get() {

            return currentIsRemoved ? (byte)0 : values[k];
        }

        @Override
        public void set(byte value) {

            if (value == 0 && !currentIsRemoved) {
                currentIsRemoved = true;
                removedIndex = columnIndices[k];
                CRSByteMatrix.this.remove(k--, i);
            }
            else if (value != 0 && !currentIsRemoved) {
                values[k] = value;
            }
            else {
                currentIsRemoved = false;
                CRSByteMatrix.this.insert(++k, i, removedIndex, value);
            }
        }

        @Override
        public boolean hasNext() {

            return k + 1 < rowPointers[i + 1] && columnIndices[k + 1] < end;
        }

        @Override
        public Byte next() {

            currentIsRemoved = false;
            return values[++k];
        }

        @Override
        protected int innerCursor() {

            return k;
        }
    }
}
