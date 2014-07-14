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
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
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


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CRSByteMatrix;


public class CRSFactory extends CompressedFactory implements Factory {

    private static final long serialVersionUID = 4071505L;


    @Override
    public ByteMatrix createMatrix() {

        return new CRSByteMatrix();
    }

    @Override
    public ByteMatrix createMatrix(int rows, int columns) {

        return new CRSByteMatrix(rows, columns);
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

        int size = rows * columns;

        byte values[] = new byte[size];
        int columnIndices[] = new int[size];
        int rowPointers[] = new int[rows + 1];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                values[i * columns + j] = value;
                columnIndices[i * columns + j] = j;
            }
            rowPointers[i] = columns * i;
        }

        rowPointers[rows] = size;

        return new CRSByteMatrix(rows, columns, size, values, columnIndices, rowPointers);
    }

    @Override
    public ByteMatrix createRandomMatrix(int rows, int columns) {

        Random random = new Random();

        int cardinality = (rows * columns) / DENSITY;

        byte values[] = new byte[cardinality];
        int columnIndices[] = new int[cardinality];
        int rowPointers[] = new int[rows + 1];

        int kk = cardinality / rows;
        int indices[] = new int[kk];

        int k = 0;
        for (int i = 0; i < rows; i++) {

            rowPointers[i] = k;

            for (int ii = 0; ii < kk; ii++) {
                indices[ii] = random.nextInt(columns);
            }

            Arrays.sort(indices);

            int previous = -1;
            for (int ii = 0; ii < kk; ii++) {

                if (indices[ii] == previous) {
                    continue;
                }

                values[k] = (byte)random.nextInt();
                columnIndices[k++] = indices[ii];
                previous = indices[ii];
            }
        }

        rowPointers[rows] = cardinality;

        return new CRSByteMatrix(rows, columns, cardinality, values, columnIndices, rowPointers);
    }

    @Override
    public ByteMatrix createRandomSymmetricMatrix(int size) {

        // TODO: Issue 15

        int cardinality = (size * size) / DENSITY;

        Random random = new Random();

        ByteMatrix matrix = new CRSByteMatrix(size, size, cardinality);

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

        return createMatrix(size, size);
    }

    @Override
    public ByteMatrix createIdentityMatrix(int size) {

        byte values[] = new byte[size];
        int columnIndices[] = new int[size];
        int rowPointers[] = new int[size + 1];

        for (int i = 0; i < size; i++) {
            values[i] = 1;
            columnIndices[i] = i;
            rowPointers[i] = i;
        }

        rowPointers[size] = size;

        return new CRSByteMatrix(size, size, size, values, columnIndices, rowPointers);
    }

    @Override
    public ByteMatrix createBlockMatrix(ByteMatrix a, ByteMatrix b, ByteMatrix c, ByteMatrix d) {

        if ((a.rows() != b.rows()) || (a.columns() != c.columns()) ||
            (c.rows() != d.rows()) || (b.columns() != d.columns())) {
            throw new IllegalArgumentException("Sides of blocks are incompatible!");
        }

        int rows = a.rows() + c.rows(), cols = a.columns() + b.columns();
        ArrayList<Byte> values = new ArrayList<>();
        ArrayList<Integer> columnIndices = new ArrayList<>();
        int rowPointers[] = new int[rows + 1];

        int k = 0;
        rowPointers[0] = 0;
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
                if (!aIsEqualToB(current, (byte)0)) {
                    values.add(current);
                    columnIndices.add(j);
                    k++;
                }
            }
            rowPointers[i + 1] = k;
        }
        byte valuesArray[] = new byte[values.size()];
        int colIndArray[] = new int[columnIndices.size()];
        for (int i = 0; i < values.size(); i++) {
            valuesArray[i] = values.get(i);
            colIndArray[i] = columnIndices.get(i);
        }

        return new CRSByteMatrix(rows, cols, k, valuesArray, colIndArray, rowPointers);
    }
}
