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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.fec.openrq.util.arithmetic.ExtraMath;
import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.checking.Invariants;
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

    private final SparseRow[] sparseRows;


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

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns; j++) {
                byte value = source.get(i, j);
                if (value != 0) {
                    colValuesForInsertion(i, rowCard + 1)[rowCard] = value;
                    getColumnIndicesForInsertion(i, rowCard + 1)[rowCard] = j;
                    rowCard++;
                    cardinality++;
                }
            }
        }
    }

    public CRSByteMatrix(int rows, int columns, int cardinality) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        ensureCardinalityIsCorrect(rows, columns, cardinality);

        this.cardinality = 0;
        this.columnIndices = new int[rows][];
        this.columnValues = new byte[rows][];
        this.rowCardinalities = new int[rows];

        for (int i = 0; i < rows; i++) {
            setColValues(i, EMPTY_COLUMN_VALUES);
            setColIndices(i, EMPTY_COLUMN_INDICES);
        }
    }

    public CRSByteMatrix(int rows,
        int columns,
        int cardinality,
        byte columnValues[][],
        int columnIndices[][],
        int rowSizes[]) {

        super(LinearAlgebra.CRS_FACTORY, rows, columns);
        ensureCardinalityIsCorrect(rows, columns, cardinality);

        this.cardinality = cardinality;

        this.columnValues = Objects.requireNonNull(columnValues);
        this.columnIndices = Objects.requireNonNull(columnIndices);
        this.rowCardinalities = Objects.requireNonNull(rowSizes);
    }

    @Override
    public byte safeGet(int i, int j) {

        final int k = searchForColumnIndex(j, i);
        if (hasEntry(k, i, j)) {
            return colValues(i)[k];
        }

        return 0;
    }

    @Override
    public void safeSet(int i, int j, byte value) {

        final int k = searchForColumnIndex(j, i);
        if (hasEntry(k, i, j)) {
            if (value == 0) {
                remove(k, i);
            }
            else {
                colValues(i)[k] = value;
            }
        }
        else {
            insert(getInsertionPoint(k), i, j, value);
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
            for (int i = 0; i < sparseRows; i++) {
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

        ByteVector result = factory.createVector(sparseRows);

        for (int i = 0; i < sparseRows; i++) {
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

        final ByteMatrix result = factory.createMatrix(sparseRows, matrix.columns());
        for (int i = 0; i < sparseRows; i++) {
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

        ByteMatrix result = factory.createMatrix(columns, sparseRows);

        int nonZeroCount = 0, i = 0;
        while (nonZeroCount < cardinality) {
            for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++, nonZeroCount++) {
                result.set(columnIndices[k], i, columnValues[k]);
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

        System.arraycopy(columnValues, rowPointers[i], rowValues, 0, rowCardinality);
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
            ArrayUtils.swapBlocks(columnValues, rowPointers[i], rowPointers[i + 1], rowPointers[j], rowPointers[j + 1]);

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
                    ArrayUtils.swapBytes(columnValues, leftPos, rightPos);
                }
                else if (leftPos != -1) { // if only the left position was found
                    columnIndices[leftPos] = right; // swap a zero value with a non zero
                    // now we need to carry the updated column index to its correct position
                    for (int k = leftPos; k + 1 < rowPointers[row + 1]; k++) {
                        if (columnIndices[k] > columnIndices[k + 1]) {
                            ArrayUtils.swapInts(columnIndices, k, k + 1);
                            ArrayUtils.swapBytes(columnValues, k, k + 1); // mirror the operation on the values array
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
                            ArrayUtils.swapBytes(columnValues, k, k - 1); // mirror the operation on the values array
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
        int $rowPointers[] = new int[sparseRows + 1];

        System.arraycopy(columnValues, 0, $values, 0, cardinality);
        System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);
        System.arraycopy(rowPointers, 0, $rowPointers, 0, sparseRows + 1);

        return new CRSByteMatrix(sparseRows, columns, cardinality, $values, $columnIndices, $rowPointers);
    }

    @Override
    public ByteMatrix resize(int rows, int columns) {

        ensureDimensionsAreCorrect(rows, columns);

        if (this.sparseRows == rows && this.columns == columns) {
            return copy();
        }

        if (this.sparseRows >= rows && this.columns >= columns) {

            byte $values[] = new byte[align(cardinality)];
            int $columnIndices[] = new int[align(cardinality)];
            int $rowPointers[] = new int[rows + 1];

            int $cardinality = 0;

            int k = 0, i = 0;
            while (k < cardinality && i < rows) {

                $rowPointers[i] = $cardinality;

                for (int j = rowPointers[i]; j < rowPointers[i + 1]
                                             && columnIndices[j] < columns; j++, k++) {

                    $values[$cardinality] = columnValues[j];
                    $columnIndices[$cardinality] = columnIndices[j];
                    $cardinality++;
                }
                i++;
            }

            $rowPointers[rows] = $cardinality;

            return new CRSByteMatrix(rows, columns, $cardinality, $values, $columnIndices, $rowPointers);
        }

        if (this.sparseRows < rows) {
            byte $values[] = new byte[align(cardinality)];
            int $columnIndices[] = new int[align(cardinality)];
            int $rowPointers[] = new int[rows + 1];

            System.arraycopy(columnValues, 0, $values, 0, cardinality);
            System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);
            System.arraycopy(rowPointers, 0, $rowPointers, 0, this.sparseRows + 1);

            for (int i = this.sparseRows; i < rows + 1; i++) {
                $rowPointers[i] = cardinality;
            }

            return new CRSByteMatrix(rows, columns, cardinality, $values, $columnIndices, $rowPointers);
        }

        // TODO: think about cardinality in align call
        byte $values[] = new byte[align(cardinality)];
        int $columnIndices[] = new int[align(cardinality)];
        int $rowPointers[] = new int[rows + 1];

        System.arraycopy(columnValues, 0, $values, 0, cardinality);
        System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);
        System.arraycopy(rowPointers, 0, $rowPointers, 0, this.sparseRows + 1);

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
    public int[] nonZeroPositionsInRow(int i) {

        checkRowBounds(i);
        return Arrays.copyOfRange(columnIndices, rowPointers[i], rowPointers[i + 1]);
    }

    @Override
    public int[] nonZeroPositionsInRow(int i, int fromColumn, int toColumn) {

        checkRowBounds(i);
        checkColumnRangeBounds(fromColumn, toColumn);

        int first = rowPointers[i];
        while (first < rowPointers[i + 1] && columnIndices[first] < fromColumn) {
            first++;
        }

        int last = rowPointers[i + 1] - 1;
        while (last >= rowPointers[i] && columnIndices[last] >= toColumn) {
            last--;
        }

        return Arrays.copyOfRange(columnIndices, first, last + 1);
    }

    @Override
    public void each(MatrixProcedure procedure) {

        int k = 0;
        for (int i = 0; i < sparseRows; i++) {
            int valuesSoFar = rowPointers[i + 1];
            for (int j = 0; j < columns; j++) {
                if (k < valuesSoFar && j == columnIndices[k]) {
                    procedure.apply(i, j, columnValues[k++]);
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
                procedure.apply(i, columnIndices[k], columnValues[k]);
            }
            i++;
        }
    }

    @Override
    public void safeUpdate(int i, int j, MatrixFunction function) {

        int k = searchForColumnIndex(j, rowPointers[i], rowPointers[i + 1]);

        if (k < rowPointers[i + 1] && columnIndices[k] == j) {
            final byte value = function.evaluate(i, j, columnValues[k]);
            if (value == 0) {
                remove(k, i);
            }
            else {
                columnValues[k] = value;
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
                final byte value = function.evaluate(i, columnIndices[k], columnValues[k]);
                if (value == 0) {
                    remove(k, i);
                    // since we removed a nonzero, the indices must be decremented accordingly
                    k--;
                    nonZeroCount--;
                }
                else {
                    columnValues[k] = value;
                }
            }
            i++;
        }
    }

    @Override
    public void addRowsInPlace(int row1, int row2) {

        checkRowBounds(row1);
        checkRowBounds(row2);

        // row cardinalities
        final int card1 = rowPointers[row1 + 1] - rowPointers[row1];
        final int card2 = rowPointers[row2 + 1] - rowPointers[row2];

        if (card1 != 0) { // if row1 has only zeros then nothing needs to be added
            if (card2 == 0) { // if row2 has only zeros then row1 is copied to row2

            }
        }

        // initial capacity to half the cardinality of each row
        List<ValueAndIndex> toInsert = new ArrayList<>(Math.max(10, card1 / 2));
        List<Integer> toRemove = new ArrayList<>(Math.max(10, card2 / 2));

        ByteVectorIterator it1 = nonZeroRowIterator(row1);
        ByteVectorIterator it2 = nonZeroRowIterator(row2);

        // we iterate over row1 because only the nonzero entries of row1 will have an impact on row2
        int curr2 = -1;
        while (it1.hasNext()) {
            it1.next();
            final int curr1 = it1.index();

            toInsert.add(new ValueAndIndex(it1.get(), curr1));

            // we skip the entries in row2 that are before the current entry in row1
            while (curr2 < curr1 && it2.hasNext()) {
                it2.next();
                curr2 = it2.index();
            }
        }
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
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(sparseRows);
        out.writeInt(columns);
        out.writeInt(cardinality);

        int nonZeroCount = 0, i = 0;
        while (nonZeroCount < cardinality) {
            for (int k = rowPointers[i]; k < rowPointers[i + 1]; k++, nonZeroCount++) {
                out.writeInt(i);
                out.writeInt(columnIndices[k]);
                out.writeByte(columnValues[k]);
            }
            i++;
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {

        sparseRows = in.readInt();
        columns = in.readInt();
        cardinality = in.readInt();

        int alignedSize = align(cardinality);

        columnValues = new byte[alignedSize];
        columnIndices = new int[alignedSize];
        rowPointers = new int[sparseRows + 1];

        for (int k = 0; k < cardinality; k++) {
            int i = in.readInt();
            columnIndices[k] = in.readInt();
            columnValues[k] = in.readByte();
            rowPointers[i + 1] = k + 1;
        }
    }

    private void insert(int k, int i, int j, byte value) {

        if (value == 0) {
            return;
        }

        if (columnValues.length < cardinality + 1) {
            growup();
        }

        System.arraycopy(columnValues, k, columnValues, k + 1, cardinality - k);
        System.arraycopy(columnIndices, k, columnIndices, k + 1, cardinality - k);

        // for (int k = cardinality; k > position; k--) {
        // values[k] = values[k - 1];
        // columnIndices[k] = columnIndices[k - 1];
        // }

        columnValues[k] = value;
        columnIndices[k] = j;

        for (int ii = i + 1; ii < sparseRows + 1; ii++) {
            rowPointers[ii]++;
        }

        cardinality++;
    }

    private void remove(int k, int i) {

        cardinality--;

        System.arraycopy(columnValues, k + 1, columnValues, k, cardinality - k);
        System.arraycopy(columnIndices, k + 1, columnIndices, k, cardinality - k);

        // for (int kk = k; kk < cardinality; kk++) {
        // values[kk] = values[kk + 1];
        // columnIndices[kk] = columnIndices[kk + 1];
        // }

        for (int ii = i + 1; ii < sparseRows + 1; ii++) {
            rowPointers[ii]--;
        }
    }

    private void growup() {

        if (columnValues.length == capacity()) {
            // This should never happen
            throw new IllegalStateException("This matrix can't grow up.");
        }

        int min = (
                  (sparseRows != 0 && columns > Integer.MAX_VALUE / sparseRows) ?
                                                                   Integer.MAX_VALUE :
                                                                   (sparseRows * columns)
                  );
        int capacity = Math.min(min, (cardinality * 3) / 2 + 1);

        byte $values[] = new byte[capacity];
        int $columnIndices[] = new int[capacity];

        System.arraycopy(columnValues, 0, $values, 0, cardinality);
        System.arraycopy(columnIndices, 0, $columnIndices, 0, cardinality);

        columnValues = $values;
        columnIndices = $columnIndices;
    }

    @Override
    public byte max() {

        byte max = minByte();

        for (int k = 0; k < cardinality; k++) {
            if (aIsGreaterThanB(columnValues[k], max)) {
                max = columnValues[k];
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
            if (aIsLessThanB(columnValues[k], min)) {
                min = columnValues[k];
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
            if (aIsGreaterThanB(columnValues[k], max)) {
                max = columnValues[k];
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
            if (aIsLessThanB(columnValues[k], min)) {
                min = columnValues[k];
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
                return columnValues[k];
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
                    columnValues[k] = value;
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

            return currentIsRemoved ? (byte)0 : columnValues[k];
        }

        @Override
        public void set(byte value) {

            if (value == 0 && !currentIsRemoved) {
                currentIsRemoved = true;
                removedIndex = columnIndices[k];
                CRSByteMatrix.this.remove(k--, i);
            }
            else if (value != 0 && !currentIsRemoved) {
                columnValues[k] = value;
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
            return columnValues[++k];
        }

        @Override
        protected int innerCursor() {

            return k;
        }
    }

    private static final class SparseRow {

        private static final int DEFAULT_ROW_CAPACITY = 8;

        private byte[] values;
        private int[] columnIndices; // must always be sorted
        private int cardinality;

        // since CRSByteMatrix is not thread safe, only one SearchEntry
        // instance per row is required for all (sequential) searches
        private final SearchEntry searchEntry;


        SparseRow() {

            this(ArrayUtils.EmptyArrayOf.bytes(), ArrayUtils.EmptyArrayOf.ints(), 0);
        }

        SparseRow(byte[] values, int[] columnIndices, int cardinality) {

            Invariants.assertInvariants(values.length == columnIndices.length);
            Invariants.assertInvariants(cardinality >= 0 && cardinality <= values.length);
            Invariants.assertInvariants(ArrayUtils.isSorted(columnIndices, 0, cardinality));

            this.values = values;
            this.columnIndices = columnIndices;
            this.cardinality = cardinality;

            this.searchEntry = new SearchEntry();
        }

        private SearchEntry getSearchEntry(int columnIndex) {

            return searchEntry.search(columnIndex);
        }

        private void insert(int k, int j, byte value) {

            if (value != 0) { // only nonzero values need to be inserted
                ensureCapacity(+1);

                System.arraycopy(values, k, values, k + 1, cardinality - k);
                System.arraycopy(columnIndices, k, columnIndices, k + 1, cardinality - k);

                values[k] = value;
                columnIndices[k] = j;
                cardinality++;
            }
        }

        private void remove(int k) {

            cardinality--;
            System.arraycopy(values, k + 1, values, k, cardinality - k);
            System.arraycopy(columnIndices, k + 1, columnIndices, k, cardinality - k);
        }

        // requires non-negative extraElements
        private void ensureCapacity(int extraElements) {

            final int newCapacity = getNewCapacity(extraElements);

            final byte[] newValues = new byte[newCapacity];
            final int[] newColumnIndices = new int[newCapacity];

            System.arraycopy(values, 0, newValues, 0, cardinality);
            System.arraycopy(columnIndices, 0, newColumnIndices, 0, cardinality);

            values = newValues;
            columnIndices = newColumnIndices;
        }

        // requires non-negative extraElements
        private int getNewCapacity(int extraElements) {

            final int minCapacity = ExtraMath.addExact(cardinality, extraElements);
            final int oldCapacity = values.length;
            if (oldCapacity == 0) {
                return Math.max(minCapacity, DEFAULT_ROW_CAPACITY);
            }
            else {
                return Math.max(minCapacity, oldCapacity + (oldCapacity / 2));
            }
        }


        // class used for nonzero entry searches, and consequent value updates
        private final class SearchEntry {

            private int j;
            private int k;


            /*
             * Requires valid column index.
             */
            SearchEntry search(int columnIndex) {

                j = columnIndex;
                k = Arrays.binarySearch(columnIndices, j);
                return this;
            }

            boolean isNonZero() {

                return k >= 0 && columnIndices[k] == j;
            }

            byte value() {

                return isNonZero() ? values[k] : 0;
            }

            int index() {

                return j;
            }

            void update(byte value) {

                if (isNonZero()) {
                    if (value != 0) {
                        values[k] = value;
                        columnIndices[k] = j;
                    }
                    else {
                        remove(k);
                    }
                }
                else {
                    insert(getInsertionPoint(), j, value);
                }
            }

            private int getInsertionPoint() {

                // Arrays.binarySearch returns (-(insertion point) - 1)
                final int k = this.k;
                return k < 0 ? -(k + 1) : k;
            }
        }
    }
}
