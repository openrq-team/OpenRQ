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
import net.fec.openrq.util.linearalgebra.matrix.dense.Basic1DByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;


public class Basic1DFactory extends BasicFactory {

    @Override
    public ByteMatrix createMatrix() {

        return new Basic1DByteMatrix();
    }

    @Override
    public ByteMatrix createMatrix(int rows, int columns) {

        return new Basic1DByteMatrix(rows, columns);
    }

    @Override
    public ByteMatrix createMatrix(int rows, int columns, byte[] array) {

        return new Basic1DByteMatrix(rows, columns, array);
    }

    @Override
    public ByteMatrix createMatrix(byte[][] array) {

        return new Basic1DByteMatrix(array);
    }

    @Override
    public ByteMatrix createMatrix(ByteMatrix matrix) {

        return new Basic1DByteMatrix(matrix);
    }

    @Override
    public ByteMatrix createMatrix(MatrixSource source) {

        return new Basic1DByteMatrix(source);
    }

    @Override
    public ByteMatrix createConstantMatrix(int rows, int columns, byte value) {

        byte array[] = new byte[rows * columns];
        Arrays.fill(array, value);

        return new Basic1DByteMatrix(rows, columns, array);
    }

    @Override
    public ByteMatrix createRandomMatrix(int rows, int columns, Random random) {

        byte array[] = new byte[rows * columns];

        for (int i = 0; i < rows * columns; i++) {
            array[i] = (byte)random.nextInt();
        }

        return new Basic1DByteMatrix(rows, columns, array);
    }

    @Override
    public ByteMatrix createRandomSymmetricMatrix(int size, Random random) {

        byte array[] = new byte[size * size];

        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                byte value = (byte)random.nextInt();
                array[i * size + j] = value;
                array[j * size + i] = value;
            }
        }

        return new Basic1DByteMatrix(size, size, array);
    }

    @Override
    public ByteMatrix createSquareMatrix(int size) {

        return new Basic1DByteMatrix(size, size);
    }

    @Override
    public ByteMatrix createIdentityMatrix(int size) {

        byte array[] = new byte[size * size];

        for (int i = 0; i < size; i++) {
            array[i * size + i] = 1;
        }

        return new Basic1DByteMatrix(size, size, array);
    }

    @Override
    public ByteMatrix createBlockMatrix(ByteMatrix a, ByteMatrix b, ByteMatrix c, ByteMatrix d) {

        if ((a.rows() != b.rows()) || (a.columns() != c.columns()) ||
            (c.rows() != d.rows()) || (b.columns() != d.columns())) {
            throw new IllegalArgumentException("Sides of blocks are incompatible!");
        }

        int rows = a.rows() + c.rows(), cols = a.columns() + b.columns();
        byte blockMatrix[] = new byte[rows * cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((i < a.rows()) && (j < a.columns())) {
                    blockMatrix[i * rows + j] = a.get(i, j);
                }
                if ((i < a.rows()) && (j > a.columns())) {
                    blockMatrix[i * rows + j] = b.get(i, j);
                }
                if ((i > a.rows()) && (j < a.columns())) {
                    blockMatrix[i * rows + j] = c.get(i, j);
                }
                if ((i > a.rows()) && (j > a.columns())) {
                    blockMatrix[i * rows + j] = d.get(i, j);
                }
            }
        }

        return new Basic1DByteMatrix(rows, cols, blockMatrix);
    }

    @Override
    public ByteMatrix createDiagonalMatrix(byte[] diagonal) {

        int size = diagonal.length;
        byte array[] = new byte[size * size];

        for (int i = 0; i < size; i++) {
            array[i * size + i] = diagonal[i];
        }

        return new Basic1DByteMatrix(size, size, array);
    }

    @Override
    public ByteMatrix deserializeMatrix(ByteBuffer buffer) throws DeserializationException {

        final int rows = Serialization.readMatrixRows(buffer);
        final int columns = Serialization.readMatrixColumns(buffer);
        final byte array[] = new byte[rows * columns];

        for (int i = 0; i < rows * columns; i++) {
            array[i] = Serialization.readMatrixValue(buffer);
        }

        return new Basic1DByteMatrix(rows, columns, array);
    }

    @Override
    public ByteMatrix deserializeMatrix(ReadableByteChannel ch) throws IOException, DeserializationException {

        final int rows = Serialization.readMatrixRows(ch);
        final int columns = Serialization.readMatrixColumns(ch);
        final byte array[] = new byte[rows * columns];

        for (int i = 0; i < rows * columns; i++) {
            array[i] = Serialization.readMatrixValue(ch);
        }

        return new Basic1DByteMatrix(rows, columns, array);
    }
}
