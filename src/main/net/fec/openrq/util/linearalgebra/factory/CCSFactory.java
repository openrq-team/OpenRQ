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
 * Contributor(s): Maxim Samoylov
 */
package net.fec.openrq.util.linearalgebra.factory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CCSByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CRSByteMatrix;


public class CCSFactory extends CompressedFactory {

    private static final long serialVersionUID = 4071505L;


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

        int size = rows * columns;

        byte values[] = new byte[size];
        int rowIndices[] = new int[size];
        int columnPointers[] = new int[columns + 1];

        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < rows; i++) {
                values[j * rows + i] = value;
                rowIndices[j * rows + i] = i;
            }
            columnPointers[j] = rows * j;
        }

        columnPointers[columns] = size;

        return new CCSByteMatrix(rows, columns, size, values, rowIndices, columnPointers);
    }

    @Override
    public ByteMatrix createRandomMatrix(int rows, int columns, Random random) {

        int cardinality = (rows * columns) / DENSITY;

        byte values[] = new byte[cardinality];
        int rowIndices[] = new int[cardinality];
        int columnPointers[] = new int[columns + 1];

        int kk = cardinality / columns;
        int indices[] = new int[kk];

        int k = 0;
        for (int j = 0; j < columns; j++) {

            columnPointers[j] = k;

            for (int jj = 0; jj < kk; jj++) {
                indices[jj] = random.nextInt(rows);
            }

            Arrays.sort(indices);

            int previous = -1;
            for (int jj = 0; jj < kk; jj++) {

                if (indices[jj] == previous) {
                    continue;
                }

                values[k] = (byte)random.nextInt();
                rowIndices[k++] = indices[jj];
                previous = indices[jj];
            }
        }

        columnPointers[columns] = cardinality;

        return new CCSByteMatrix(rows, columns, cardinality, values, rowIndices, columnPointers);
    }

    @Override
    public ByteMatrix createRandomSymmetricMatrix(int size, Random random) {

        // TODO: Issue 15

        int cardinality = (size * size) / DENSITY;

        ByteMatrix matrix = new CCSByteMatrix(size, size, cardinality);

        for (int k = 0; k < cardinality / 2; k++) {
            int i = random.nextInt(size);
            int j = random.nextInt(size);
            byte value = (byte)random.nextInt();

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

        int rows = a.rows() + c.rows(), cols = a.columns() + b.columns();
        ArrayList<Byte> values = new ArrayList<>();
        ArrayList<Integer> rowIndices = new ArrayList<>();
        int columnPointers[] = new int[rows + 1];

        int k = 0;
        columnPointers[0] = 0;
        byte current = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((i < a.rows()) && (j < a.columns())) {
                    current = a.get(i, j);
                }
                if ((i < a.rows()) && (j > a.columns())) {
                    current = b.get(i, j);
                }
                if ((i > a.rows()) && (j < a.columns())) {
                    current = c.get(i, j);
                }
                if ((i > a.rows()) && (j > a.columns())) {
                    current = d.get(i, j);
                }
                if (current != 0) {
                    values.add(current);
                    rowIndices.add(j);
                    k++;
                }
            }
            columnPointers[i + 1] = k;
        }
        byte valuesArray[] = new byte[values.size()];
        int rowIndArray[] = new int[rowIndices.size()];
        for (int i = 0; i < values.size(); i++) {
            valuesArray[i] = values.get(i);
            rowIndArray[i] = rowIndices.get(i);
        }

        return new CRSByteMatrix(rows, cols, k, valuesArray, rowIndArray, columnPointers);
    }

    @Override
    public ByteMatrix createDiagonalMatrix(byte[] diagonal) {

        int size = diagonal.length;
        int rowIndices[] = new int[size];
        int columnPointers[] = new int[size + 1];

        for (int i = 0; i < size; i++) {
            rowIndices[i] = i;
            columnPointers[i] = i;
        }
        columnPointers[size] = size;

        return new CCSByteMatrix(size, size, size, diagonal, rowIndices, columnPointers);
    }
}
