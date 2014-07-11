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


import java.io.Serializable;

import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;



public interface Factory extends Serializable {

    /**
     * Creates an empty matrix.
     * <p>
     * See <a href="http://mathworld.wolfram.com/Matrix.html"> http://mathworld.wolfram.com/Matrix.html</a> for more
     * details.
     * </p>
     * 
     * @return empty matrix
     */
    ByteMatrix createMatrix();

    /**
     * Creates a matrix with specified size.
     * <p>
     * See <a href="http://mathworld.wolfram.com/Matrix.html"> http://mathworld.wolfram.com/Matrix.html</a> for more
     * details.
     * </p>
     * 
     * @param rows
     * @param columns
     * @return
     */
    ByteMatrix createMatrix(int rows, int columns);

    /**
     * Creates a matrix from array.
     * 
     * @param array
     * @return
     */
    ByteMatrix createMatrix(byte array[][]);

    /**
     * Creates a matrix from another matrix.
     * 
     * @param matrix
     * @return
     */
    ByteMatrix createMatrix(ByteMatrix matrix);

    /**
     * Creates a matrix from matrix proxy.
     * 
     * @param source
     * @return
     */
    ByteMatrix createMatrix(MatrixSource source);

    /**
     * Creates the constant matrix
     * 
     * @param rows
     * @param columns
     * @return constant matrix
     */
    ByteMatrix createConstantMatrix(int rows, int columns, byte value);

    /**
     * Creates random matrix.
     * 
     * @param rows
     * @param columns
     * @return
     */
    ByteMatrix createRandomMatrix(int rows, int columns);

    /**
     * Creates random symmetric matrix.
     * 
     * @param size
     * @return
     */
    ByteMatrix createRandomSymmetricMatrix(int size);

    /**
     * Creates square matrix with specified size.
     * <p>
     * See <a href="http://mathworld.wolfram.com/SquareMatrix.html"> http://mathworld.wolfram.com/SquareMatrix.html</a>
     * for more details.
     * </p>
     * 
     * @param size
     * @return
     */
    ByteMatrix createSquareMatrix(int size);

    /**
     * Creates identity matrix.
     * <p>
     * See <a href="http://mathworld.wolfram.com/IdentityMatrix.html">
     * http://mathworld.wolfram.com/IdentityMatrix.html</a> for more details.
     * </p>
     * 
     * @param size
     * @return
     */
    ByteMatrix createIdentityMatrix(int size);

    /**
     * Creates matrix from given blocks.
     * Throws IllegalArgumentException if sizes of blocks are incompatible.
     * <p>
     * See <a href="http://mathworld.wolfram.com/BlockMatrix.html"> http://mathworld.wolfram.com/BlockMatrix.html</a>
     * for more details.
     * </p>
     * 
     * @param a
     * @param b
     * @param c
     * @param d
     * @return Matrix created from blocks a, b, c, d.
     */
    ByteMatrix createBlockMatrix(ByteMatrix a, ByteMatrix b, ByteMatrix c, ByteMatrix d);

    /**
     * Creates an empty vector.
     * See <a href="http://mathworld.wolfram.com/Vector.html">
     * http://mathworld.wolfram.com/Vector.html</a> for more details.
     * </p>
     * 
     * @return empty vector
     */
    ByteVector createVector();

    /**
     * Creates vector with specified length.
     * 
     * @param length
     * @return
     */
    ByteVector createVector(int length);

    /**
     * Creates vector from array.
     * 
     * @param array
     * @return
     */
    ByteVector createVector(byte array[]);

    /**
     * @param vector
     * @return
     */
    ByteVector createVector(ByteVector vector);

    /**
     * @param source
     * @return
     */
    ByteVector createVector(VectorSource source);

    /**
     * Creates the constant vector.
     * 
     * @param length
     * @param value
     * @return constant vector
     */
    ByteVector createConstantVector(int length, byte value);

    /**
     * Creates random vector.
     * 
     * @param length
     * @return
     */
    ByteVector createRandomVector(int length);
}
