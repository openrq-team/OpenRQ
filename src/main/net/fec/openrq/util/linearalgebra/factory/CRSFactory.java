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
import net.fec.openrq.util.linearalgebra.matrix.sparse.CRSByteMatrix;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;


public class CRSFactory extends CompressedFactory {

    @Override
    public ByteMatrix createMatrix() {

        return new CRSByteMatrix();
    }

    @Override
    public ByteMatrix createMatrix(int rows, int columns) {

        return new CRSByteMatrix(rows, columns);
    }

    @Override
    public ByteMatrix createMatrix(int rows, int columns, byte[] array) {

        return new CRSByteMatrix(rows, columns, array);
    }

    @Override
    public ByteMatrix createMatrix(byte[][] array) {

        return new CRSByteMatrix(array);
    }

    @Override
    public ByteMatrix createMatrix(ByteMatrix matrix) {

        return new CRSByteMatrix(matrix);
    }

    @Override
    public ByteMatrix createMatrix(MatrixSource source) {

        return new CRSByteMatrix(source);
    }

    @Override
    public ByteMatrix createConstantMatrix(int rows, int columns, byte value) {

        if (value == 0) {
            return new CRSByteMatrix(rows, columns);
        }
        else {
            byte[][] columnValues = new byte[rows][columns];
            int[][] columnIndices = new int[rows][columns];
            int[] rowCardinalities = new int[rows];

            for (int i = 0; i < rows; i++) {
                rowCardinalities[i] = columns;
                for (int j = 0; j < columns; j++) {
                    columnValues[i][j] = value;
                    columnIndices[i][j] = j;
                }
            }

            return new CRSByteMatrix(rows, columns, columnValues, columnIndices, rowCardinalities);
        }
    }

    @Override
    public ByteMatrix createRandomMatrix(int rows, int columns, Random random) {

        int cardinality = (rows * columns) / DENSITY;

        ByteMatrix matrix = new CRSByteMatrix(rows, columns);
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

        ByteMatrix matrix = new CRSByteMatrix(size, size);
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

        return createMatrix(size, size);
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
        ByteMatrix matrix = new CRSByteMatrix(rows, cols);
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

        ByteMatrix matrix = new CRSByteMatrix(size, size);
        for (int i = 0; i < size; i++) {
            matrix.set(i, i, diagonal[i]);
        }

        return matrix;
    }

    @Override
    public ByteMatrix deserializeMatrix(ByteBuffer buffer) throws DeserializationException {

        final int rows = Serialization.readMatrixRows(buffer);
        final int cols = Serialization.readMatrixColumns(buffer);

        final int[] rowCards = new int[rows];
        final int[][] colInds = new int[rows][];
        final byte[][] colVals = new byte[rows][];

        for (int i = 0; i < rows; i++) {
            rowCards[i] = Serialization.readMatrixRowCardinality(buffer);
            colInds[i] = new int[rowCards[i]];
            colVals[i] = new byte[rowCards[i]];
            for (int j = 0; j < rowCards[i]; j++) {
                colInds[i][j] = Serialization.readMatrixColumnIndex(buffer);
                colVals[i][j] = Serialization.readMatrixValue(buffer);
            }
        }

        return new CRSByteMatrix(rows, cols, colVals, colInds, rowCards);
    }

    @Override
    public ByteMatrix deserializeMatrix(ReadableByteChannel ch) throws IOException, DeserializationException {

        final int rows = Serialization.readMatrixRows(ch);
        final int cols = Serialization.readMatrixColumns(ch);

        final int[] rowCards = new int[rows];
        final int[][] colInds = new int[rows][];
        final byte[][] colVals = new byte[rows][];

        for (int i = 0; i < rows; i++) {
            rowCards[i] = Serialization.readMatrixRowCardinality(ch);
            colInds[i] = new int[rowCards[i]];
            colVals[i] = new byte[rowCards[i]];
            for (int j = 0; j < rowCards[i]; j++) {
                colInds[i][j] = Serialization.readMatrixColumnIndex(ch);
                colVals[i][j] = Serialization.readMatrixValue(ch);
            }
        }

        return new CRSByteMatrix(rows, cols, colVals, colInds, rowCards);
    }
}
