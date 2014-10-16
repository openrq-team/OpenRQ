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
import java.util.Random;

import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;


public abstract class Factory {

    /**
     * Creates an empty matrix.
     * 
     * @return an empty matrix
     */
    public abstract ByteMatrix createMatrix();

    /**
     * Creates a matrix of specified shape.
     * 
     * @param rows
     *            the number of matrix rows
     * @param columns
     *            the number of matrix columns
     * @return a new matrix of given shape
     */
    public abstract ByteMatrix createMatrix(int rows, int columns);

    /**
     * Creates a matrix of specified shape.
     * 
     * @param rows
     *            the number of matrix rows
     * @param columns
     *            the number of matrix columns
     * @param array
     *            the source 1D array
     * @return a new matrix of given shape
     */
    public abstract ByteMatrix createMatrix(int rows, int columns, byte array[]);

    /**
     * Creates a matrix from given {@code array}.
     * 
     * @param array
     *            the source 2D array
     * @return a new matrix of given array
     */
    public abstract ByteMatrix createMatrix(byte array[][]);

    /**
     * Creates a matrix from another {@code matrix}.
     * 
     * @param matrix
     *            the source matrix
     * @return a new matrix
     */
    public abstract ByteMatrix createMatrix(ByteMatrix matrix);

    /**
     * Creates a matrix from given matrix {@code source}.
     * 
     * @param source
     *            the matrix source
     * @return a new matrix
     */
    public abstract ByteMatrix createMatrix(MatrixSource source);

    /**
     * Creates a constant matrix of given shape with {@code value} stored in
     * each matrix cell.
     * 
     * @param rows
     *            the number of matrix rows
     * @param columns
     *            the number of matrix columns
     * @param value
     * @return a constant matrix
     */
    public abstract ByteMatrix createConstantMatrix(int rows, int columns, byte value);

    /**
     * Creates a random matrix of given shape.
     * 
     * @param rows
     *            the number of matrix rows
     * @param columns
     *            the number of matrix columns
     * @return a random matrix
     */
    public ByteMatrix createRandomMatrix(int rows, int columns) {

        return createRandomMatrix(rows, columns, new Random());
    }

    /**
     * Creates a random matrix of given shape.
     * 
     * @param rows
     *            the number of matrix rows
     * @param columns
     *            the number of matrix columns
     * @param random
     *            the random object instance
     * @return a random matrix
     */
    public abstract ByteMatrix createRandomMatrix(int rows, int columns, Random random);

    /**
     * Creates a square random symmetric matrix of given {@code size}.
     * 
     * @param size
     *            the number of matrix rows/columns
     * @return a square random symmetric matrix
     */
    ByteMatrix createRandomSymmetricMatrix(int size) {

        return createRandomSymmetricMatrix(size, new Random());
    }

    /**
     * Creates a square random symmetric matrix of given {@code size}.
     * 
     * @param size
     *            the number of matrix rows/columns
     * @param random
     *            the random object instance
     * @return a square random symmetric matrix
     */
    public abstract ByteMatrix createRandomSymmetricMatrix(int size, Random random);

    /**
     * Creates a square matrix of given {@code size}.
     * 
     * @param size
     *            the number of matrix rows/columns
     * @return a square matrix
     */
    public abstract ByteMatrix createSquareMatrix(int size);

    /**
     * Creates an identity matrix of given {@code size}. An identity matrix
     * contains {@code 1.0} at its main diagonal.
     * 
     * @param size
     *            the number of matrix rows/columns
     * @return an identity matrix
     */
    public abstract ByteMatrix createIdentityMatrix(int size);

    /**
     * Creates a matrix from given blocks.
     * Throws IllegalArgumentException if sizes of blocks are incompatible.
     * <p>
     * See <a href="http://mathworld.wolfram.com/BlockMatrix.html"> http://mathworld.wolfram.com/BlockMatrix.html</a>
     * for more details.
     * </p>
     * 
     * @param a
     *            the first block
     * @param b
     *            the second block
     * @param c
     *            the third block
     * @param d
     *            the forth block
     * @return a block matrix
     */
    public abstract ByteMatrix createBlockMatrix(ByteMatrix a, ByteMatrix b, ByteMatrix c, ByteMatrix d);

    /**
     * Creates a diagonal matrix of given {@code diagonal}.
     * 
     * @param diagonal
     *            the matrix diagonal
     * @return a diagonal matrix
     */
    public abstract ByteMatrix createDiagonalMatrix(byte diagonal[]);

    /**
     * Creates an empty vector.
     * 
     * @return empty vector
     */
    public abstract ByteVector createVector();

    /**
     * Creates a vector of given {@code length}.
     * 
     * @param length
     *            the vector's length
     * @return a new vector
     */
    public abstract ByteVector createVector(int length);

    /**
     * Creates a vector from given {@code array}.
     * 
     * @param array
     *            the source 1D array
     * @return a new vector
     */
    public abstract ByteVector createVector(byte array[]);

    /**
     * Creates a vector from another {@code vector}.
     * 
     * @param vector
     *            the source vector
     * @return a new vector
     */
    public abstract ByteVector createVector(ByteVector vector);

    /**
     * Creates a vector of given {@code source}.
     * 
     * @param source
     *            the vector source
     * @return a new vector
     */
    public abstract ByteVector createVector(VectorSource source);

    /**
     * Creates a constant vector of given {@code length} and constant {@code value}.
     * 
     * @param length
     *            the vector's length
     * @param value
     *            the constant value
     * @return a constant vector
     */
    public abstract ByteVector createConstantVector(int length, byte value);

    /**
     * Creates a random vector of given {@code length}.
     * 
     * @param length
     *            the vector's length
     * @return a random vector
     */
    public ByteVector createRandomVector(int length) {

        return createRandomVector(length, new Random());
    }

    /**
     * Creates a random vector of given {@code length}.
     * 
     * @param length
     *            the vector's length
     * @param random
     *            the random object instance
     * @return a random vector
     */
    public abstract ByteVector createRandomVector(int length, Random random);

    public abstract ByteVector deserializeVector(ByteBuffer buffer) throws DeserializationException;

    public abstract ByteVector deserializeVector(ReadableByteChannel ch) throws IOException, DeserializationException;

    public abstract ByteMatrix deserializeMatrix(ByteBuffer buffer) throws DeserializationException;

    public abstract ByteMatrix deserializeMatrix(ReadableByteChannel ch) throws IOException, DeserializationException;
}
