///*
// * Copyright 2014 Jose Lopes
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// * http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
///*
// * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
// * 
// * This file is part of la4j project (http://la4j.org)
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * You may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// * http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * Contributor(s): Chandler May
// * Maxim Samoylov
// * Anveshi Charuvaka
// * Clement Skau
// * Catherine da Graca
// */
//package net.fec.openrq.util.linearalgebra.matrix.sparse;
//
//
//import static net.fec.openrq.util.arithmetic.OctetOps.aIsGreaterThanB;
//import static net.fec.openrq.util.arithmetic.OctetOps.aIsLessThanB;
//import static net.fec.openrq.util.arithmetic.OctetOps.maxByte;
//import static net.fec.openrq.util.arithmetic.OctetOps.minByte;
//
//import java.util.Arrays;
//
//import net.fec.openrq.util.checking.Indexables;
//import net.fec.openrq.util.linearalgebra.LinearAlgebra;
//import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
//import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
//import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
//import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
//import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
//import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
//import net.fec.openrq.util.linearalgebra.vector.ByteVector;
//import net.fec.openrq.util.linearalgebra.vector.sparse.CompressedByteVector;
//
//
///**
// * This is a CCS (Compressed Column Storage) matrix class.
// */
//public class CCSByteMatrix extends AbstractCompressedByteMatrix implements SparseByteMatrix {
//
//    private static final int MINIMUM_SIZE = 32;
//
//    private byte values[];
//    private int rowIndices[];
//    private int columnPointers[];
//
//
//    public CCSByteMatrix() {
//
//        this(0, 0);
//    }
//
//    public CCSByteMatrix(int rows, int columns) {
//
//        this(rows, columns, 0);
//    }
//
//    public CCSByteMatrix(int rows, int columns, byte array[]) {
//
//        this(ByteMatrices.asArray1DSource(rows, columns, array));
//    }
//
//    public CCSByteMatrix(ByteMatrix matrix) {
//
//        this(ByteMatrices.asMatrixSource(matrix));
//    }
//
//    public CCSByteMatrix(byte array[][]) {
//
//        this(ByteMatrices.asArray2DSource(array));
//    }
//
//    public CCSByteMatrix(MatrixSource source) {
//
//        this(source.rows(), source.columns(), 0);
//
//        for (int j = 0; j < columns(); j++) {
//            columnPointers[j] = cardinality;
//            for (int i = 0; i < rows; i++) {
//                byte value = source.get(i, j);
//                if (value != 0) {
//
//                    if (values.length < cardinality + 1) {
//                        growup();
//                    }
//
//                    values[cardinality] = value;
//                    rowIndices[cardinality] = i;
//                    cardinality++;
//                }
//            }
//        }
//
//        columnPointers[columns] = cardinality;
//    }
//
//    public CCSByteMatrix(int rows, int columns, int cardinality) {
//
//        super(LinearAlgebra.CCS_FACTORY, rows, columns);
//        ensureCardinalityIsCorrect(rows, columns, cardinality);
//
//        int alignedSize = align(cardinality);
//
//        this.cardinality = 0;
//        this.values = new byte[alignedSize];
//        this.rowIndices = new int[alignedSize];
//
//        this.columnPointers = new int[columns + 1];
//    }
//
//    public CCSByteMatrix(int rows,
//        int columns,
//        int cardinality,
//        byte values[],
//        int rowIndices[],
//        int columnPointers[]) {
//
//        super(LinearAlgebra.CCS_FACTORY, rows, columns);
//        ensureCardinalityIsCorrect(rows, columns, cardinality);
//
//        this.cardinality = cardinality;
//        this.values = values;
//        this.rowIndices = rowIndices;
//        this.columnPointers = columnPointers;
//    }
//
//    @Override
//    public byte safeGet(int i, int j) {
//
//        int k = searchForRowIndex(i, columnPointers[j], columnPointers[j + 1]);
//
//        if (k < columnPointers[j + 1] && rowIndices[k] == i) {
//            return values[k];
//        }
//
//        return 0;
//    }
//
//    @Override
//    public void safeSet(int i, int j, byte value) {
//
//        int k = searchForRowIndex(i, columnPointers[j], columnPointers[j + 1]);
//
//        if (k < columnPointers[j + 1] && rowIndices[k] == i) {
//            if (value == 0) {
//                remove(k, j);
//            }
//            else {
//                values[k] = value;
//            }
//        }
//        else {
//            insert(k, i, j, value);
//        }
//    }
//
//    @Override
//    public ByteVector getColumn(int j) {
//
//        Indexables.checkIndexBounds(j, columns());
//
//        int columnCardinality = columnPointers[j + 1] - columnPointers[j];
//
//        byte columnValues[] = new byte[columnCardinality];
//        int columnIndices[] = new int[columnCardinality];
//
//        System.arraycopy(values, columnPointers[j], columnValues, 0, columnCardinality);
//        System.arraycopy(rowIndices, columnPointers[j], columnIndices, 0, columnCardinality);
//
//        return new CompressedByteVector(rows, columnCardinality, columnValues, columnIndices);
//    }
//
//    @Override
//    public ByteMatrix copy() {
//
//        byte $values[] = new byte[align(cardinality)];
//        int $rowIndices[] = new int[align(cardinality)];
//        int $columnPointers[] = new int[columns + 1];
//
//        System.arraycopy(values, 0, $values, 0, cardinality);
//        System.arraycopy(rowIndices, 0, $rowIndices, 0, cardinality);
//        System.arraycopy(columnPointers, 0, $columnPointers, 0, columns() + 1);
//
//        return new CCSByteMatrix(rows(), columns(), cardinality, $values, $rowIndices, $columnPointers);
//    }
//
//    @Override
//    public ByteMatrix resize(int rows, int columns) {
//
//        ensureDimensionsAreCorrect(rows, columns);
//
//        if (this.rows() == rows && this.columns() == columns) {
//            return copy();
//        }
//
//        if (this.rows() >= rows && this.columns() >= columns) {
//
//            // TODO: think about cardinality in align call
//            byte $values[] = new byte[align(cardinality)];
//            int $rowIndices[] = new int[align(cardinality)];
//            int $columnPointers[] = new int[columns + 1];
//
//            int $cardinality = 0;
//
//            int k = 0, j = 0;
//            while (k < cardinality && j < columns) {
//
//                $columnPointers[j] = $cardinality;
//
//                for (int i = columnPointers[j]; i < columnPointers[j + 1]
//                                                && rowIndices[i] < rows; i++, k++) {
//
//                    $values[$cardinality] = values[i];
//                    $rowIndices[$cardinality] = rowIndices[i];
//                    $cardinality++;
//                }
//                j++;
//            }
//
//            $columnPointers[columns] = $cardinality;
//
//            return new CCSByteMatrix(rows, columns, $cardinality, $values, $rowIndices, $columnPointers);
//        }
//
//        if (this.columns() < columns) {
//
//            byte $values[] = new byte[align(cardinality)];
//            int $rowIndices[] = new int[align(cardinality)];
//            int $columnPointers[] = new int[columns + 1];
//
//            System.arraycopy(values, 0, $values, 0, cardinality);
//            System.arraycopy(rowIndices, 0, $rowIndices, 0, cardinality);
//            System.arraycopy(columnPointers, 0, $columnPointers, 0,
//                this.columns() + 1);
//
//            for (int i = this.columns(); i < columns + 1; i++) {
//                $columnPointers[i] = cardinality;
//            }
//
//            return new CCSByteMatrix(rows, columns, cardinality, $values, $rowIndices, $columnPointers);
//        }
//
//        // TODO: think about cardinality in align call
//        byte $values[] = new byte[align(cardinality)];
//        int $rowIndices[] = new int[align(cardinality)];
//        int $columnPointers[] = new int[columns + 1];
//
//        System.arraycopy(values, 0, $values, 0, cardinality);
//        System.arraycopy(rowIndices, 0, $rowIndices, 0, cardinality);
//        System.arraycopy(columnPointers, 0, $columnPointers, 0, this.columns() + 1);
//
//        return new CCSByteMatrix(rows, columns, cardinality, $values, $rowIndices, $columnPointers);
//    }
//
//    @Override
//    public boolean nonZeroAt(int i, int j) {
//
//        checkBounds(i, j);
//        final int k = searchForRowIndex(i, columnPointers[j], columnPointers[j + 1]);
//        return k < columnPointers[j + 1] && rowIndices[k] == i;
//    }
//
//    @Override
//    public int nonZerosInColumn(int j) {
//
//        Indexables.checkIndexBounds(j, columns());
//        return columnPointers[j + 1] - columnPointers[j];
//    }
//
//    @Override
//    public int nonZerosInColumn(int j, int fromRow, int toRow) {
//
//        Indexables.checkIndexBounds(j, columns());
//        Indexables.checkFromToBounds(fromRow, toRow, rows());
//
//        int nonZeros = columnPointers[j + 1] - columnPointers[j]; // upper bound
//
//        // discount non zeros to the left of the range
//        int k = columnPointers[j];
//        while (k < columnPointers[j + 1] && rowIndices[k] < fromRow) {
//            k++;
//            nonZeros--;
//        }
//
//        // discount non zeros to the right of the ranges
//        k = columnPointers[j + 1] - 1;
//        while (k >= columnPointers[j] && rowIndices[k] >= toRow) {
//            k--;
//            nonZeros--;
//        }
//
//        return nonZeros;
//    }
//
//    @Override
//    public int[] nonZeroPositionsInColumn(int j) {
//
//        Indexables.checkIndexBounds(j, columns());
//        return Arrays.copyOfRange(rowIndices, columnPointers[j], columnPointers[j + 1]);
//    }
//
//    @Override
//    public int[] nonZeroPositionsInColumn(int j, int fromRow, int toRow) {
//
//        Indexables.checkIndexBounds(j, columns());
//        Indexables.checkFromToBounds(fromRow, toRow, rows());
//
//        int first = columnPointers[j];
//        while (first < columnPointers[j + 1] && rowIndices[first] < fromRow) {
//            first++;
//        }
//
//        int last = columnPointers[j + 1] - 1;
//        while (last >= columnPointers[j] && rowIndices[last] >= toRow) {
//            last--;
//        }
//
//        return Arrays.copyOfRange(rowIndices, first, last + 1);
//    }
//
//    @Override
//    public void each(MatrixProcedure procedure) {
//
//        int k = 0;
//        for (int j = 0; j < columns(); j++) {
//            int valuesSoFar = columnPointers[j + 1];
//            for (int i = 0; i < rows(); i++) {
//                if (k < valuesSoFar && i == rowIndices[k]) {
//                    procedure.apply(i, j, values[k++]);
//                }
//                else {
//                    procedure.apply(i, j, (byte)0);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void eachNonZero(MatrixProcedure procedure) {
//
//        int nonZeroCount = 0, j = 0;
//        while (nonZeroCount < cardinality) {
//            for (int k = columnPointers[j]; k < columnPointers[j + 1]; k++, nonZeroCount++) {
//                procedure.apply(rowIndices[k], j, values[k]);
//            }
//            j++;
//        }
//    }
//
//    @Override
//    public void safeUpdate(int i, int j, MatrixFunction function) {
//
//        int k = searchForRowIndex(i, columnPointers[j], columnPointers[j + 1]);
//
//        if (k < columnPointers[j + 1] && rowIndices[k] == i) {
//            final byte value = function.evaluate(i, j, values[k]);
//            if (value == 0) {
//                remove(k, j);
//            }
//            else {
//                values[k] = value;
//            }
//        }
//        else {
//            insert(k, i, j, function.evaluate(i, j, (byte)0));
//        }
//    }
//
//    @Override
//    public void updateNonZero(MatrixFunction function) {
//
//        int nonZeroCount = 0, j = 0;
//        while (nonZeroCount < cardinality) {
//            for (int k = columnPointers[j]; k < columnPointers[j + 1]; k++, nonZeroCount++) {
//                final byte value = function.evaluate(rowIndices[k], j, values[k]);
//                if (value == 0) {
//                    remove(k, j);
//                    // since we removed a nonzero, the indices must be decremented accordingly
//                    k--;
//                    nonZeroCount--;
//                }
//                else {
//                    values[k] = value;
//                }
//            }
//            j++;
//        }
//    }
//
//    private int searchForRowIndex(int i, int left, int right) {
//
//        if (left == right) {
//            return left;
//        }
//
//        if (right - left < 8) {
//
//            int ii = left;
//            while (ii < right && rowIndices[ii] < i) {
//                ii++;
//            }
//
//            return ii;
//        }
//
//        int p = (left + right) / 2;
//
//        if (rowIndices[p] > i) {
//            return searchForRowIndex(i, left, p);
//        }
//        else if (rowIndices[p] < i) {
//            return searchForRowIndex(i, p + 1, right);
//        }
//        else {
//            return p;
//        }
//    }
//
//    private void insert(int k, int i, int j, byte value) {
//
//        // if (Math.abs(value) < Matrices.EPS && value >= 0.0) {
//        if (value == 0.0) {
//            return;
//        }
//
//        if (values.length < cardinality + 1) {
//            growup();
//        }
//
//        System.arraycopy(values, k, values, k + 1, cardinality - k);
//        System.arraycopy(rowIndices, k, rowIndices, k + 1, cardinality - k);
//
//        // for (int k = cardinality; k > position; k--) {
//        // values[k] = values[k - 1];
//        // rowIndices[k] = rowIndices[k - 1];
//        // }
//
//        values[k] = value;
//        rowIndices[k] = i;
//
//        for (int jj = j + 1; jj < columns() + 1; jj++) {
//            columnPointers[jj]++;
//        }
//
//        cardinality++;
//    }
//
//    private void remove(int k, int j) {
//
//        cardinality--;
//
//        System.arraycopy(values, k + 1, values, k, cardinality - k);
//        System.arraycopy(rowIndices, k + 1, rowIndices, k, cardinality - k);
//
//        // for (int kk = k; kk < cardinality; kk++) {
//        // values[kk] = values[kk + 1];
//        // rowIndices[kk] = rowIndices[kk + 1];
//        // }
//
//        for (int jj = j + 1; jj < columns() + 1; jj++) {
//            columnPointers[jj]--;
//        }
//    }
//
//    private void growup() {
//
//        if (values.length == capacity()) {
//            // This should never happen
//            throw new IllegalStateException("This matrix can't grow up.");
//        }
//
//        int min = (
//                  (rows() != 0 && columns() > Integer.MAX_VALUE / rows()) ?
//                                                                         Integer.MAX_VALUE :
//                                                                         (rows() * columns())
//                  );
//        int capacity = Math.min(min, (cardinality * 3) / 2 + 1);
//
//        byte $values[] = new byte[capacity];
//        int $rowIndices[] = new int[capacity];
//
//        System.arraycopy(values, 0, $values, 0, cardinality);
//        System.arraycopy(rowIndices, 0, $rowIndices, 0, cardinality);
//
//        values = $values;
//        rowIndices = $rowIndices;
//    }
//
//    private int align(int cardinality) {
//
//        return ((cardinality / MINIMUM_SIZE) + 1) * MINIMUM_SIZE;
//    }
//
//    @Override
//    public byte max() {
//
//        byte max = minByte();
//
//        for (int k = 0; k < cardinality; k++) {
//            if (aIsGreaterThanB(values[k], max)) {
//                max = values[k];
//            }
//        }
//
//        if (cardinality == capacity() || aIsGreaterThanB(max, (byte)0)) {
//            return max;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    @Override
//    public byte min() {
//
//        byte min = maxByte();
//
//        for (int k = 0; k < cardinality; k++) {
//            if (aIsLessThanB(values[k], min)) {
//                min = values[k];
//            }
//        }
//
//        if (cardinality == capacity() || aIsLessThanB(min, (byte)0)) {
//            return min;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    @Override
//    public byte maxInColumn(int j) {
//
//        Indexables.checkIndexBounds(j, columns());
//
//        byte max = minByte();
//
//        for (int k = columnPointers[j]; k < columnPointers[j + 1]; k++) {
//            if (aIsGreaterThanB(values[k], max)) {
//                max = values[k];
//            }
//        }
//
//        if (cardinality == capacity() || aIsGreaterThanB(max, (byte)0)) {
//            return max;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    @Override
//    public byte minInColumn(int j) {
//
//        Indexables.checkIndexBounds(j, columns());
//
//        byte min = minByte();
//
//        for (int k = columnPointers[j]; k < columnPointers[j + 1]; k++) {
//            if (aIsLessThanB(values[k], min)) {
//                min = values[k];
//            }
//        }
//
//        if (cardinality == capacity() || aIsLessThanB(min, (byte)0)) {
//            return min;
//        }
//        else {
//            return 0;
//        }
//    }
//
//    /**
//     * Returns a CCSMatrix with the selected rows and columns.
//     */
//    @Override
//    public ByteMatrix select(int[] rowIndices, int[] columnIndices) {
//
//        int newRows = rowIndices.length;
//        int newCols = columnIndices.length;
//
//        if (newRows == 0 || newCols == 0) {
//            fail("No rows or columns selected.");
//        }
//
//        // determine number of non-zero values (cardinality)
//        // before allocating space, this is perhaps more efficient
//        // than single pass and calling grow() when required.
//        int newCardinality = 0;
//        for (int i = 0; i < newRows; i++) {
//            for (int j = 0; j < newCols; j++) {
//                if (get(rowIndices[i], columnIndices[j]) != 0) {
//                    newCardinality++;
//                }
//            }
//        }
//
//        // Construct the raw structure for the sparse matrix
//        byte[] newValues = new byte[newCardinality];
//        int[] newRowIndices = new int[newCardinality];
//        int[] newColumnPointers = new int[newCols + 1];
//
//        newColumnPointers[0] = 0;
//        int endPtr = 0;
//        for (int j = 0; j < newCols; j++) {
//            newColumnPointers[j + 1] = newColumnPointers[j];
//            for (int i = 0; i < newRows; i++) {
//                byte val = get(rowIndices[i], columnIndices[j]);
//                if (val != 0) {
//                    newValues[endPtr] = val;
//                    newRowIndices[endPtr] = i;
//                    endPtr++;
//                    newColumnPointers[j + 1]++;
//                }
//            }
//        }
//
//        return new CCSByteMatrix(newRows, newCols, newCardinality, newValues,
//            newRowIndices, newColumnPointers);
//    }
//
//    @Override
//    public ByteVectorIterator columnIterator(int j) {
//
//        Indexables.checkIndexBounds(j, columns());
//        return new ColumnIterator(j, 0, rows());
//    }
//
//    @Override
//    public ByteVectorIterator columnIterator(int j, int fromRow, int toRow) {
//
//        Indexables.checkIndexBounds(j, columns());
//        Indexables.checkFromToBounds(fromRow, toRow, rows());
//        return new ColumnIterator(j, fromRow, toRow);
//    }
//
//
//    private final class ColumnIterator extends ByteVectorIterator {
//
//        private final int j;
//        private int i;
//        private final int end;
//        private int k;
//
//
//        /*
//         * Requires valid indices.
//         */
//        ColumnIterator(int j, int fromRow, int toRow) {
//
//            super(toRow - fromRow);
//
//            this.j = j;
//            this.i = fromRow - 1;
//            this.end = toRow;
//
//            setKToWithinRange(fromRow);
//        }
//
//        private void setKToWithinRange(int fromRow) {
//
//            /*
//             * only need to check the starting index
//             * if rowIndices[k] >= toRow, then k will never be used
//             */
//
//            k = columnPointers[j];
//            while (k < columnPointers[j + 1] && rowIndices[k] < fromRow) {
//                k++;
//            }
//        }
//
//        @Override
//        public int index() {
//
//            return i;
//        }
//
//        @Override
//        public byte get() {
//
//            if (k < columnPointers[j + 1] && rowIndices[k] == i) {
//                return values[k];
//            }
//            return 0;
//        }
//
//        @Override
//        public void set(byte value) {
//
//            if (k < columnPointers[j + 1] && rowIndices[k] == i) {
//                if (value == 0) {
//                    CCSByteMatrix.this.remove(k, j);
//                }
//                else {
//                    values[k] = value;
//                }
//            }
//            else {
//                CCSByteMatrix.this.insert(k, i, j, value);
//            }
//        }
//
//        @Override
//        public boolean hasNext() {
//
//            return i + 1 < end;
//        }
//
//        @Override
//        public Byte next() {
//
//            i++;
//            if (k < columnPointers[j + 1] && rowIndices[k] == i - 1) {
//                k++;
//            }
//            return get();
//        }
//
//        @Override
//        protected int innerCursor() {
//
//            return k;
//        }
//    }
//
//
//    @Override
//    public ByteVectorIterator nonZeroColumnIterator(int j) {
//
//        Indexables.checkIndexBounds(j, columns());
//        return new NonZeroColumnIterator(j, 0, rows());
//    }
//
//    @Override
//    public ByteVectorIterator nonZeroColumnIterator(int j, int fromRow, int toRow) {
//
//        Indexables.checkIndexBounds(j, columns());
//        Indexables.checkFromToBounds(fromRow, toRow, rows());
//        return new NonZeroColumnIterator(j, fromRow, toRow);
//    }
//
//
//    private final class NonZeroColumnIterator extends ByteVectorIterator {
//
//        private final int j;
//        private boolean currentIsRemoved = false;
//        private int removedIndex;
//        private final int end;
//        private int k;
//
//
//        /*
//         * Requires valid indices.
//         */
//        NonZeroColumnIterator(int j, int fromRow, int toRow) {
//
//            super(toRow - fromRow);
//
//            this.j = j;
//            this.currentIsRemoved = false;
//            this.removedIndex = -1;
//            this.end = toRow;
//
//            setKToWithinRange(fromRow);
//        }
//
//        private void setKToWithinRange(int fromRow) {
//
//            /*
//             * only need to check the starting index
//             * if rowIndices[k] >= toRow, then k will never be used
//             */
//
//            int kk = columnPointers[j];
//            while (kk < columnPointers[j + 1] && rowIndices[kk] < fromRow) {
//                kk++;
//            }
//
//            k = kk - 1; // start at kk - 1 so the first next() call can increment k to kk
//        }
//
//        @Override
//        public int index() {
//
//            return currentIsRemoved ? removedIndex : rowIndices[k];
//        }
//
//        @Override
//        public byte get() {
//
//            return currentIsRemoved ? (byte)0 : values[k];
//        }
//
//        @Override
//        public void set(byte value) {
//
//            if (value == 0 && !currentIsRemoved) {
//                currentIsRemoved = true;
//                removedIndex = rowIndices[k];
//                CCSByteMatrix.this.remove(k--, j);
//            }
//            else if (value != 0 && !currentIsRemoved) {
//                values[k] = value;
//            }
//            else {
//                currentIsRemoved = false;
//                CCSByteMatrix.this.insert(++k, removedIndex, j, value);
//            }
//        }
//
//        @Override
//        public boolean hasNext() {
//
//            return k + 1 < columnPointers[j + 1] && rowIndices[k + 1] < end;
//        }
//
//        @Override
//        public Byte next() {
//
//            currentIsRemoved = false;
//            return values[++k];
//        }
//
//        @Override
//        protected int innerCursor() {
//
//            return k;
//        }
//    }
//}
