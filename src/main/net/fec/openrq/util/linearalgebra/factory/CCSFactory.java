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
 * Contributor(s): Maxim Samoylov
 */
package net.fec.openrq.util.linearalgebra.factory;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Random;

import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CCSByteMatrix;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;


public class CCSFactory extends CompressedFactory {

    @Override
    public ByteMatrix createMatrix() {

        return new CCSByteMatrix();
    }

    @Override
    public ByteMatrix createMatrix(int rows, int columns) {

        return new CCSByteMatrix(rows, columns);
    }

    @Override
    public ByteMatrix createMatrix(int rows, int columns, byte[] array) {

        return new CCSByteMatrix(rows, columns, array);
    }

    @Override
    public ByteMatrix createMatrix(byte[][] array) {

        return new CCSByteMatrix(array);
    }

    @Override
    public ByteMatrix createMatrix(ByteMatrix matrix) {

        return new CCSByteMatrix(matrix);
    }

    @Override
    public ByteMatrix createMatrix(MatrixSource source) {

        return new CCSByteMatrix(source);
    }

    @Override
    public ByteMatrix createConstantMatrix(int rows, int columns, byte value) {

        if (value == 0) {
            return new CCSByteMatrix(rows, columns);
        }
        else {
            byte[][] rowValues = new byte[columns][rows];
            int[][] rowIndices = new int[columns][rows];
            int[] colCardinalities = new int[columns];

            for (int j = 0; j < columns; j++) {
                colCardinalities[j] = rows;
                for (int i = 0; i < rows; i++) {
                    rowValues[j][i] = value;
                    rowIndices[j][i] = i;
                }
            }

            return new CCSByteMatrix(rows, columns, rowValues, rowIndices, colCardinalities);
        }
    }

    @Override
    public ByteMatrix createRandomMatrix(int rows, int columns, Random random) {

        int cardinality = (rows * columns) / DENSITY;

        ByteMatrix matrix = new CCSByteMatrix(rows, columns);
        for (; cardinality > 0; cardinality--) {
            final int i = random.nextInt(rows);
            final int j = random.nextInt(columns);
            matrix.set(i, j, (byte)random.nextInt());
        }

        return matrix;
    }

    @Override
    public ByteMatrix createRandomSymmetricMatrix(int size, Random random) {

        int cardinality = (size * size) / DENSITY;

        ByteMatrix matrix = new CCSByteMatrix(size, size);
        for (int k = 0; k < cardinality / 2; k++) {
            final int i = random.nextInt(size);
            final int j = random.nextInt(size);
            final byte value = (byte)random.nextInt();

            matrix.set(i, j, value);
            matrix.set(j, i, value);
        }

        return matrix;
    }

    @Override
    public ByteMatrix createSquareMatrix(int size) {

        return new CCSByteMatrix(size, size);
    }

    @Override
    public ByteMatrix createIdentityMatrix(int size) {

        byte diagonal[] = new byte[size];
        Arrays.fill(diagonal, (byte)1);

        return createDiagonalMatrix(diagonal);
    }

    @Override
    public ByteMatrix createBlockMatrix(ByteMatrix a, ByteMatrix b, ByteMatrix c, ByteMatrix d) {

        if ((a.rows() != b.rows()) || (a.columns() != c.columns()) ||
            (c.rows() != d.rows()) || (b.columns() != d.columns())) {
            throw new IllegalArgumentException("Sides of blocks are incompatible!");
        }

        final int rows = a.rows() + c.rows();
        final int cols = a.columns() + b.columns();
        ByteMatrix matrix = new CCSByteMatrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((i < a.rows()) && (j < a.columns())) {
                    matrix.set(i, j, a.get(i, j));
                }
                if ((i < a.rows()) && (j > a.columns())) {
                    matrix.set(i, j, b.get(i, j));
                }
                if ((i > a.rows()) && (j < a.columns())) {
                    matrix.set(i, j, c.get(i, j));
                }
                if ((i > a.rows()) && (j > a.columns())) {
                    matrix.set(i, j, d.get(i, j));
                }
            }
        }

        return matrix;
    }

    @Override
    public ByteMatrix createDiagonalMatrix(byte[] diagonal) {

        final int size = diagonal.length;

        ByteMatrix matrix = new CCSByteMatrix(size, size);
        for (int i = 0; i < size; i++) {
            matrix.set(i, i, diagonal[i]);
        }

        return matrix;
    }

    @Override
    public ByteMatrix deserializeMatrix(ByteBuffer buffer) throws DeserializationException {

        final int rows = Serialization.readMatrixRows(buffer);
        final int cols = Serialization.readMatrixColumns(buffer);

        final int[] colCards = new int[rows];
        final int[][] rowInds = new int[rows][];
        final byte[][] rowVals = new byte[rows][];

        for (int i = 0; i < cols; i++) {
            colCards[i] = Serialization.readMatrixColumnCardinality(buffer);
            rowInds[i] = new int[colCards[i]];
            rowVals[i] = new byte[colCards[i]];
            for (int j = 0; j < colCards[i]; j++) {
                rowInds[i][j] = Serialization.readMatrixRowIndex(buffer);
                rowVals[i][j] = Serialization.readMatrixValue(buffer);
            }
        }

        return new CCSByteMatrix(rows, cols, rowVals, rowInds, colCards);
    }

    @Override
    public ByteMatrix deserializeMatrix(ReadableByteChannel ch) throws IOException, DeserializationException {

        final int rows = Serialization.readMatrixRows(ch);
        final int cols = Serialization.readMatrixColumns(ch);

        final int[] colCards = new int[rows];
        final int[][] rowInds = new int[rows][];
        final byte[][] rowVals = new byte[rows][];

        for (int i = 0; i < cols; i++) {
            colCards[i] = Serialization.readMatrixColumnCardinality(ch);
            rowInds[i] = new int[colCards[i]];
            rowVals[i] = new byte[colCards[i]];
            for (int j = 0; j < colCards[i]; j++) {
                rowInds[i][j] = Serialization.readMatrixRowIndex(ch);
                rowVals[i][j] = Serialization.readMatrixValue(ch);
            }
        }

        return new CCSByteMatrix(rows, cols, rowVals, rowInds, colCards);
    }
}
