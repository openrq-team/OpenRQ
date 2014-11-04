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
 * Contributor(s): Evgenia Krivova
 * Julia Kostyukova
 * Jakob Moellers
 * Maxim Samoylov
 * Anveshi Charuvaka
 * Todd Brunhoff
 */
package net.fec.openrq.util.linearalgebra.matrix;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.functor.AdvancedMatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


/**
 * This interface represents byte matrix.
 * <p>
 * <a href="http://mathworld.wolfram.com/Matrix.html"> http://mathworld.wolfram.com/Matrix.html </a>
 * </p>
 */
public interface ByteMatrix {

    /**
     * Gets the specified element of this matrix.
     * 
     * @param i
     *            element's row index
     * @param j
     *            element's column index
     * @return the element of this matrix
     */
    byte get(int i, int j);

    /**
     * Sets the specified element of this matrix to given {@code value}.
     * 
     * @param i
     *            element's row index
     * @param j
     *            element's column index
     * @param value
     *            element's new value
     */
    void set(int i, int j, byte value);

    /**
     * Assigns all the elements of this matrix to zero.
     */
    void clear();

    /**
     * Assigns all the elements of a row of this matrix to zero.
     * 
     * @param i
     *            row index
     */
    void clearRow(int i);

    /**
     * Assigns all the elements of a column of this matrix to zero.
     * 
     * @param j
     *            column index
     */
    void clearColumn(int j);

    /**
     * Assigns all elements of this matrix to given {@code value}.
     * 
     * @param value
     *            the elements' new value
     */
    void assign(byte value);

    /**
     * Assigns all elements of the specified row of this matrix to given {@code value}.
     * 
     * @param i
     *            the row index
     * @param value
     *            the elements' new value
     */
    void assignRow(int i, byte value);

    /**
     * Assigns all elements of the specified column of this matrix to given {@code value}.
     * 
     * @param j
     *            the column index
     * @param value
     *            the elements' new value
     */
    void assignColumn(int j, byte value);

    /**
     * Swaps the specified rows of this matrix.
     * 
     * @param i
     *            the row index
     * @param j
     *            the row index
     */
    void swapRows(int i, int j);

    /**
     * Swaps the specified columns of this matrix.
     * 
     * @param i
     *            the column index
     * @param j
     *            the column index
     */
    void swapColumns(int i, int j);

    /**
     * Returns the number of rows of this matrix.
     * 
     * @return the number of rows
     */
    int rows();

    /**
     * Returns the number of columns of this matrix.
     * 
     * @return the number of columns
     */
    int columns();

    /**
     * Transposes this matrix.
     * 
     * @return the transposed matrix
     */
    ByteMatrix transpose();

    /**
     * Transposes this matrix.
     * 
     * @param factory
     *            the factory of result matrix
     * @return the transposed matrix
     */
    ByteMatrix transpose(Factory factory);

    /**
     * Rotates this matrix by 90 degrees to the right.
     * 
     * @return the rotated matrix
     */
    ByteMatrix rotate();

    /**
     * Rotates this matrix by 90 degrees to the right.
     * 
     * @param factory
     *            the factory of result matrix
     * @return the rotated matrix
     */
    ByteMatrix rotate(Factory factory);

    /**
     * Powers this matrix of given exponent {code n}.
     * 
     * @param n
     *            the exponent
     * @return the powered matrix
     */
    ByteMatrix power(int n);

    /**
     * Powers this matrix of given exponent {code n}.
     * 
     * @param n
     *            the exponent
     * @param factory
     *            the factory of result matrix
     * @return the powered matrix
     */
    ByteMatrix power(int n, Factory factory);

    /**
     * Scales this matrix by given {@code value} (v).
     * 
     * @param value
     *            the scale factor
     * @return A * v
     */
    ByteMatrix multiply(byte value);

    /**
     * Scales this matrix by given {@code value} (v).
     * 
     * @param value
     *            the scale factor
     * @param factory
     *            the factory of result matrix
     * @return A * v
     */
    ByteMatrix multiply(byte value, Factory factory);

    /**
     * Multiplies this matrix (A) by given {@code vector} (x).
     * 
     * @param vector
     *            the vector
     * @return A * x
     */
    ByteVector multiply(ByteVector vector);

    /**
     * Multiplies this matrix (A) by given {@code vector} (x).
     * 
     * @param vector
     *            the right hand vector for multiplication
     * @param factory
     *            the factory of result matrix
     * @return A * x
     */
    ByteVector multiply(ByteVector vector, Factory factory);

    /**
     * Multiplies this matrix (A) by given {@code matrix} (B).
     * 
     * @param matrix
     *            the right hand matrix for multiplication
     * @return A * B
     */
    ByteMatrix multiply(ByteMatrix matrix);

    /**
     * Multiplies this matrix (A) by given {@code matrix} (B).
     * 
     * @param matrix
     *            the right hand matrix for multiplication
     * @param factory
     *            the factory of result matrix
     * @return A * B
     */
    ByteMatrix multiply(ByteMatrix matrix, Factory factory);

    ByteMatrix multiply(
        ByteMatrix matrix,
        int fromThisRow,
        int toThisRow,
        int fromThisColumn,
        int toThisColumn,
        int fromOtherRow,
        int toOtherRow,
        int fromOtherColumn,
        int toOtherColumn);

    ByteMatrix multiply(
        ByteMatrix matrix,
        int fromThisRow,
        int toThisRow,
        int fromThisColumn,
        int toThisColumn,
        int fromOtherRow,
        int toOtherRow,
        int fromOtherColumn,
        int toOtherColumn,
        Factory factory);

    ByteVector multiplyRow(int i, ByteMatrix matrix);

    ByteVector multiplyRow(int i, ByteMatrix matrix, Factory factory);

    ByteVector multiplyRow(int i, ByteMatrix matrix, int fromColumn, int toColumn);

    ByteVector multiplyRow(int i, ByteMatrix matrix, int fromColumn, int toColumn, Factory factory);

    /**
     * Subtracts given {@code value} (v) from every element of this matrix (A).
     * 
     * @param value
     *            the right hand value for subtraction
     * @return A - v
     */
    ByteMatrix subtract(byte value);

    /**
     * Subtracts given {@code value} (v) from every element of this matrix (A).
     * 
     * @param value
     *            the right hand value for subtraction
     * @param factory
     *            the factory of result matrix
     * @return A - v
     */
    ByteMatrix subtract(byte value, Factory factory);

    /**
     * Subtracts given {@code matrix} (B) from this matrix (A).
     * 
     * @param matrix
     *            the right hand matrix for subtraction
     * @return A - B
     */
    ByteMatrix subtract(ByteMatrix matrix);

    /**
     * Subtracts given {@code matrix} (B) from this matrix (A).
     * 
     * @param matrix
     *            the right hand matrix for subtraction
     * @param factory
     *            the factory of result matrix
     * @return A - B
     */
    ByteMatrix subtract(ByteMatrix matrix, Factory factory);

    /**
     * Adds given {@code value} (v) to every element of this matrix (A).
     * 
     * @param value
     *            the right hand value for addition
     * @return A + v
     */
    ByteMatrix add(byte value);

    /**
     * Adds given {@code value} (v) to every element of this matrix (A).
     * 
     * @param value
     *            the right hand value for addition
     * @param factory
     *            the factory of result matrix
     * @return A + v
     */
    ByteMatrix add(byte value, Factory factory);

    /**
     * Adds given {@code matrix} (B) to this matrix (A).
     * 
     * @param matrix
     *            the right hand matrix for addition
     * @return A + B
     */
    ByteMatrix add(ByteMatrix matrix);

    /**
     * Adds given {@code matrix} (B) to this matrix (A).
     * 
     * @param matrix
     *            the right hand matrix for addition
     * @param factory
     *            the factory of result matrix
     * @return A + B
     */
    ByteMatrix add(ByteMatrix matrix, Factory factory);

    /**
     * Divides every element of this matrix (A) by given {@code value} (v).
     * 
     * @param value
     *            the right hand value for division
     * @return A / v
     */
    ByteMatrix divide(byte value);

    /**
     * Divides every element of this matrix (A) by given {@code value} (v).
     * 
     * @param value
     *            the right hand value for division
     * @param factory
     *            the factory of result matrix
     * @return A / v
     */
    ByteMatrix divide(byte value, Factory factory);

    /**
     * Divides every element of a row of this matrix (A) by given {@code value} (v).
     * 
     * @param i
     *            A row index
     * @param value
     *            the right hand value for division
     */
    void divideRowInPlace(int i, byte value);

    /**
     * Divides every element of a range of a row of this matrix (A) by given {@code value} (v).
     * 
     * @param i
     *            A row index
     * @param value
     *            the right hand value for division
     * @param fromColumn
     *            The starting column index (inclusive)
     * @param toColumn
     *            The ending column index (exclusive)
     */
    void divideRowInPlace(int i, byte value, int fromColumn, int toColumn);

    /**
     * Divides every element of a column of this matrix (A) by given {@code value} (v).
     * 
     * @param j
     *            A column index
     * @param value
     *            the right hand value for division
     */
    void divideColumnInPlace(int j, byte value);

    /**
     * Divides every element of a range of a column of this matrix (A) by given {@code value} (v).
     * 
     * @param j
     *            A column index
     * @param value
     *            the right hand value for division
     * @param fromRow
     *            The starting row index
     * @param toRow
     *            The ending row index
     */
    void divideColumnInPlace(int j, byte value, int fromRow, int toRow);

    /**
     * Divides every element of this matrix (A) by given {@code value} (v).
     * 
     * @param value
     *            the right hand value for division
     */
    void divideInPlace(byte value);

    /**
     * Calculates the trace of this matrix.
     * <p>
     * See <a href="http://mathworld.wolfram.com/MatrixTrace.html"> http://mathworld.wolfram.com/MatrixTrace.html</a>
     * for more details.
     * </p>
     * 
     * @return the trace of this matrix
     */
    byte trace();

    /**
     * Calculates the product of diagonal elements of this matrix.
     * 
     * @return the product of diagonal elements of this matrix
     */
    byte diagonalProduct();

    /**
     * Multiplies up all elements of this matrix.
     * 
     * @return the product of all elements of this matrix
     */
    byte product();

    /**
     * Summarizes up all elements of this matrix.
     * 
     * @return the sum of all elements of this matrix
     */
    byte sum();

    /**
     * Calculates the Hadamard (element-wise) product of this and given {@code matrix}.
     * 
     * @param matrix
     *            the right hand matrix for Hadamard product
     * @return the Hadamard product of two matrices
     */
    ByteMatrix hadamardProduct(ByteMatrix matrix);

    /**
     * Calculates the Hadamard (element-wise) product of this and given {@code matrix}.
     * 
     * @param matrix
     *            the right hand matrix for Hadamard product
     * @param factory
     *            the factory of result matrix
     * @return the Hadamard product of two matrices
     */
    ByteMatrix hadamardProduct(ByteMatrix matrix, Factory factory);

    /**
     * Copies the specified row of this matrix into the vector.
     * 
     * @param i
     *            the row index
     * @return the row represented as vector
     */
    ByteVector getRow(int i);

    /**
     * Copies the specified row of this matrix into the vector.
     * 
     * @param i
     *            the row index
     * @param factory
     *            the factory of result vector
     * @return the row represented as vector
     */
    ByteVector getRow(int i, Factory factory);

    /**
     * Copies a range of elements from the specified row of this matrix into a vector.
     * 
     * @param i
     *            The row index
     * @param fromColumn
     *            The first column index (inclusive)
     * @param toColumn
     *            The last column index (exclusive)
     * @return the range of a row represented as a vector
     */
    ByteVector getRow(int i, int fromColumn, int toColumn);

    /**
     * Copies a range of elements from the specified row of this matrix into a vector.
     * 
     * @param i
     *            The row index
     * @param fromColumn
     *            The first column index (inclusive)
     * @param toColumn
     *            The last column index (exclusive)
     * @param factory
     *            The factory of the result vector
     * @return the range of a row represented as a vector
     */
    ByteVector getRow(int i, int fromColumn, int toColumn, Factory factory);

    /**
     * Copies the specified column of this matrix into the vector.
     * 
     * @param j
     *            the column index
     * @return the column represented as vector
     */
    ByteVector getColumn(int j);

    /**
     * Copies the specified column of this matrix into the vector.
     * 
     * @param j
     *            the column index
     * @param factory
     *            the factory of result vector
     * @return the column represented as vector
     */
    ByteVector getColumn(int j, Factory factory);

    /**
     * Copies a range of elements from the specified column of this matrix into a vector.
     * 
     * @param j
     *            The column index
     * @param fromRow
     *            The first row index (inclusive)
     * @param toRow
     *            The last row index (exclusive)
     * @return the range of a column represented as a vector
     */
    ByteVector getColumn(int j, int fromRow, int toRow);

    /**
     * Copies a range of elements from the specified column of this matrix into a vector.
     * 
     * @param j
     *            The column index
     * @param fromRow
     *            The first row index (inclusive)
     * @param toRow
     *            The last row index (exclusive)
     * @param factory
     *            The factory of the result vector
     * @return the range of a column represented as a vector
     */
    ByteVector getColumn(int j, int fromRow, int toRow, Factory factory);

    /**
     * Copies given {@code vector} into the specified row of this matrix.
     * 
     * @param i
     *            the row index
     * @param vector
     *            the row represented as vector
     */
    void setRow(int i, ByteVector vector);

    /**
     * Copies given region of a vector into the specified region of a row of this matrix.
     * 
     * @param i
     *            the row index
     * @param fromColumn
     *            the starting column index
     * @param vector
     *            the row represented as vector
     * @param fromIndex
     *            the starting vector index
     * @param length
     *            the number of bytes to be copied
     */
    void setRow(int i, int fromColumn, ByteVector vector, int fromIndex, int length);

    /**
     * Copies given {@code vector} into the specified column of this matrix.
     * 
     * @param j
     *            the column index
     * @param vector
     *            the column represented as vector
     */
    void setColumn(int j, ByteVector vector);

    /**
     * Copies given region of a vector into the specified region of a column of this matrix.
     * 
     * @param j
     *            the column index
     * @param fromRow
     *            the starting row index
     * @param vector
     *            the column represented as vector
     * @param fromIndex
     *            the starting vector index
     * @param length
     *            the number of bytes to be copied
     */
    void setColumn(int j, int fromRow, ByteVector vector, int fromIndex, int length);

    /**
     * Creates the blank (an empty matrix with same size) matrix of this matrix.
     * 
     * @return blank matrix
     */
    ByteMatrix blank();

    /**
     * Creates the blank (an empty matrix with same size) matrix of this matrix.
     * 
     * @param factory
     *            the factory of result matrix
     * @return blank matrix
     */
    ByteMatrix blank(Factory factory);

    /**
     * Copies this matrix.
     * 
     * @return the copy of this matrix
     */
    ByteMatrix copy();

    /**
     * Copies this matrix.
     * 
     * @param factory
     *            the factory of result matrix
     * @return the copy of this matrix
     */
    ByteMatrix copy(Factory factory);

    /**
     * Copies this matrix into the new matrix with specified dimensions: {@code rows} and {@code columns}.
     * 
     * @param rows
     *            the number of rows in new matrix
     * @param columns
     *            the number of columns in new matrix
     * @return the copy of this matrix with new size
     */
    ByteMatrix resize(int rows, int columns);

    /**
     * Copies this matrix into the new matrix with specified dimensions: {@code rows} and {@code columns}.
     * 
     * @param rows
     *            the number of rows in new matrix
     * @param columns
     *            the number of columns in new matrix
     * @param factory
     *            the factory of result matrix
     * @return the copy of this matrix with new size
     */
    ByteMatrix resize(int rows, int columns, Factory factory);

    /**
     * Copies this matrix into the new matrix with specified row dimension: {@code rows}.
     * 
     * @param rows
     *            the number of rows in new matrix
     * @return the copy of this matrix with new size
     */
    ByteMatrix resizeRows(int rows);

    /**
     * Copies this matrix into the new matrix with specified row dimension: {@code rows}.
     * 
     * @param rows
     *            the number of rows in new matrix
     * @param factory
     *            the factory of result matrix
     * @return the copy of this matrix with new size
     */
    ByteMatrix resizeRows(int rows, Factory factory);

    /**
     * Copies this matrix into the new matrix with specified column dimension: {@code columns}.
     * 
     * @param columns
     *            the number of columns in new matrix
     * @return the copy of this matrix with new size
     */
    ByteMatrix resizeColumns(int columns);

    /**
     * Copies this matrix into the new matrix with specified column dimension: {@code columns}.
     * 
     * @param columns
     *            the number of columns in new matrix
     * @param factory
     *            the factory of result matrix
     * @return the copy of this matrix with new size
     */
    ByteMatrix resizeColumns(int columns, Factory factory);

    /**
     * Shuffles this matrix.
     * <p>
     * Copies this matrix into the matrix that contains the same elements but with the elements shuffled around (which
     * might also result in the same matrix (with a small likelihood)).
     * </p>
     * 
     * @return the shuffled matrix
     */
    ByteMatrix shuffle();

    /**
     * Shuffles this matrix.
     * <p>
     * Copies this matrix into the matrix that contains the same elements but with the elements shuffled around (which
     * might also result in the same matrix (with a small likelihood)).
     * </p>
     * 
     * @param factory
     *            the factory of result matrix
     * @return the shuffled matrix
     */
    ByteMatrix shuffle(Factory factory);

    /**
     * Retrieves the specified sub-matrix of this matrix. The sub-matrix is specified by
     * intervals for row indices and column indices.
     * 
     * @param fromRow
     *            the beginning of the row indices interval
     * @param fromColumn
     *            the beginning of the column indices interval
     * @param untilRow
     *            the ending of the row indices interval
     * @param untilColumn
     *            the ending of the column indices interval
     * @return the sub-matrix of this matrix
     */
    ByteMatrix slice(int fromRow, int fromColumn, int untilRow, int untilColumn);

    /**
     * Retrieves the specified sub-matrix of this matrix. The sub-matrix is specified by
     * intervals for row indices and column indices.
     * 
     * @param fromRow
     *            the beginning of the row indices interval
     * @param fromColumn
     *            the beginning of the column indices interval
     * @param untilRow
     *            the ending of the row indices interval
     * @param untilColumn
     *            the ending of the column indices interval
     * @param factory
     *            the factory of result matrix
     * @return the sub-matrix of this matrix
     */
    ByteMatrix slice(int fromRow, int fromColumn, int untilRow, int untilColumn, Factory factory);

    /**
     * Retrieves the specified sub-matrix of this matrix. The sub-matrix is specified by
     * intervals for row indices and column indices. The top left points of both intervals
     * are fixed to zero.
     * 
     * @param untilRow
     *            the ending of the row indices interval
     * @param untilColumn
     *            the ending of the column indices interval
     * @return the sub-matrix of this matrix
     */
    ByteMatrix sliceTopLeft(int untilRow, int untilColumn);

    /**
     * Retrieves the specified sub-matrix of this matrix. The sub-matrix is specified by
     * intervals for row indices and column indices. The top left points of both intervals
     * are fixed to zero.
     * 
     * @param untilRow
     *            the ending of the row indices interval
     * @param untilColumn
     *            the ending of the column indices interval
     * @param factory
     *            the factory of result matrix
     * @return the sub-matrix of this matrix
     */
    ByteMatrix sliceTopLeft(int untilRow, int untilColumn, Factory factory);

    /**
     * Retrieves the specified sub-matrix of this matrix. The sub-matrix is specified by
     * intervals for row indices and column indices. The bottom right points of both intervals
     * are fixed to matrix dimensions - it's rows and columns correspondingly.
     * 
     * @param fromRow
     *            the beginning of the row indices interval
     * @param fromColumn
     *            the beginning of the column indices interval
     * @return the sub-matrix of this matrix
     */
    ByteMatrix sliceBottomRight(int fromRow, int fromColumn);

    /**
     * Retrieves the specified sub-matrix of this matrix. The sub-matrix is specified by
     * intervals for row indices and column indices. The bottom right points of both intervals
     * are fixed to matrix dimensions - it's rows and columns correspondingly.
     * 
     * @param fromRow
     *            the beginning of the row indices interval
     * @param fromColumn
     *            the beginning of the column indices interval
     * @param factory
     *            the factory of result matrix
     * @return the sub-matrix of this matrix
     */
    ByteMatrix sliceBottomRight(int fromRow, int fromColumn, Factory factory);

    /**
     * Returns a new matrix with the selected rows and columns. This method can
     * be used either return a specific subset of rows and/or columns or to
     * permute the indices in an arbitrary order. The list of indices are
     * allowed to contain duplicates indices. This is more general than slice()
     * which selects only contiguous blocks. However, where applicable slice()
     * is probably more efficient.
     * 
     * @param rowIndices
     *            the array of row indices
     * @param columnIndices
     *            the array of column indices
     * @return the new matrix with the selected rows and columns
     * @throws IllegalArgumentException
     *             if invalid row or column indices are provided
     */
    public ByteMatrix select(int[] rowIndices, int[] columnIndices);

    /**
     * Returns a new matrix with the selected rows and columns. This method can
     * be used either return a specific subset of rows and/or columns or to
     * permute the indices in an arbitrary order. The list of indices are
     * allowed to contain duplicates indices. This is more general than slice()
     * which selects only contiguous blocks. However, where applicable slice()
     * is probably more efficient.
     * 
     * @param rowIndices
     *            the array of row indices
     * @param columnIndices
     *            the array of column indices
     * @param factory
     *            the factory of result matrix
     * @return the new matrix with the selected rows and columns
     * @throws IllegalArgumentException
     *             if invalid row or column indices are provided
     */
    public ByteMatrix select(int[] rowIndices, int[] columnIndices, Factory factory);

    /**
     * Returns the factory of this matrix.
     * 
     * @return the factory of this matrix
     */
    Factory factory();

    /**
     * Whether or not the specified element is zero.
     * 
     * @param i
     *            element's row index
     * @param j
     *            element's column index
     * @return {@code true} if specified element is zero, {@code false} otherwise
     */
    boolean isZeroAt(int i, int j);

    /**
     * Whether or not the specified element is not zero.
     * 
     * @param i
     *            element's row index
     * @param j
     *            element's column index
     * @return {@code true} if specified element is not zero, {@code false} otherwise
     */
    boolean nonZeroAt(int i, int j);

    /**
     * Returns the number of non zero elements in this matrix.
     * 
     * @return the number of non zero elements in this matrix
     */
    int nonZeros();

    /**
     * Returns the number of non zero elements in the row of this matrix.
     * 
     * @param i
     *            The row index
     * @return the number of non zero elements in the row of this matrix
     */
    int nonZerosInRow(int i);

    /**
     * Returns the number of non zero elements in the range of row of this matrix.
     * 
     * @param i
     *            The row index
     * @param fromColumn
     *            The first column index (inclusive)
     * @param toColumn
     *            The last column index (exclusive)
     * @return the number of non zero elements in the range of row of this matrix
     */
    int nonZerosInRow(int i, int fromColumn, int toColumn);

    /**
     * Returns an array with the positions (column indices) of the non zero elements in the specified row of this
     * matrix.
     * 
     * @param i
     *            The row index
     * @return an array with the positions (column indices) of the non zero elements in the specified row of this
     *         matrix
     */
    int[] nonZeroPositionsInRow(int i);

    /**
     * Returns an array with the positions (column indices) of the non zero elements in a range of the specified row of
     * this matrix.
     * 
     * @param i
     *            The row index
     * @param fromColumn
     *            The starting column index (inclusive)
     * @param toColumn
     *            The ending column index (exclusive)
     * @return an array with the positions (column indices) of the non zero elements in the specified row of this
     *         matrix
     */
    int[] nonZeroPositionsInRow(int i, int fromColumn, int toColumn);

    /**
     * Returns the number of non zero elements in the column of this matrix.
     * 
     * @param j
     *            The column index
     * @return the number of non zero elements in the column of this matrix
     */
    int nonZerosInColumn(int j);

    /**
     * Returns the number of non zero elements in the range of column of this matrix.
     * 
     * @param j
     *            The column index
     * @param fromRow
     *            The first row index (inclusive)
     * @param toRow
     *            The last row index (exclusive)
     * @return the number of non zero elements in the range of column of this matrix
     */
    int nonZerosInColumn(int j, int fromRow, int toRow);

    /**
     * Returns an array with the positions (row indices) of the non zero elements in the specified column of this
     * matrix.
     * 
     * @param j
     *            The column index
     * @return an array with the positions (row indices) of the non zero elements in the specified column of this
     *         matrix
     */
    int[] nonZeroPositionsInColumn(int j);

    /**
     * Returns an array with the positions (row indices) of the non zero elements in a range of the specified column of
     * this matrix.
     * 
     * @param j
     *            The column index
     * @param fromRow
     *            The starting row index (inclusive)
     * @param toRow
     *            The ending row index (exclusive)
     * @return an array with the positions (row indices) of the non zero elements in a range of the specified column of
     *         this matrix
     */
    int[] nonZeroPositionsInColumn(int j, int fromRow, int toRow);

    /**
     * Applies given {@code procedure} to each element of this matrix.
     * 
     * @param procedure
     *            the matrix procedure
     */
    void each(MatrixProcedure procedure);

    /**
     * Applies given {@code procedure} to each element of specified row of this matrix.
     * 
     * @param i
     *            the row index
     * @param procedure
     *            the matrix procedure
     */
    void eachInRow(int i, MatrixProcedure procedure);

    /**
     * Applies given {@code procedure} to each element of a range of specified row of this matrix.
     * 
     * @param i
     *            the row index
     * @param procedure
     *            the matrix procedure
     * @param fromColumn
     *            The first column index (inclusive)
     * @param toColumn
     *            The last column index (exclusive)
     */
    void eachInRow(int i, MatrixProcedure procedure, int fromColumn, int toColumn);

    /**
     * Applies given {@code procedure} to each element of specified column of this matrix.
     * 
     * @param j
     *            the column index
     * @param procedure
     *            the matrix procedure
     */
    void eachInColumn(int j, MatrixProcedure procedure);

    /**
     * Applies given {@code procedure} to each element of a range of specified column of this matrix.
     * 
     * @param j
     *            the column index
     * @param procedure
     *            the matrix procedure
     * @param fromRow
     *            The first row index (inclusive)
     * @param toRow
     *            The last row index (exclusive)
     */
    void eachInColumn(int j, MatrixProcedure procedure, int fromRow, int toRow);

    /**
     * Applies given {@code procedure} to each non-zero element of this matrix.
     * 
     * @param procedure
     *            the matrix procedure
     */
    void eachNonZero(MatrixProcedure procedure);

    /**
     * Applies given {@code procedure} to each non-zero element of specified row of this matrix.
     * 
     * @param i
     *            the row index
     * @param procedure
     *            the matrix procedure
     */
    void eachNonZeroInRow(int i, MatrixProcedure procedure);

    /**
     * Applies given {@code procedure} to each non-zero element of a range of specified row of this matrix.
     * 
     * @param i
     *            the row index
     * @param procedure
     *            the matrix procedure
     * @param fromColumn
     *            The starting column (inclusive)
     * @param toColumn
     *            The ending column (exclusive)
     */
    void eachNonZeroInRow(int i, MatrixProcedure procedure, int fromColumn, int toColumn);

    /**
     * Applies given {@code procedure} to each non-zero element of specified column of this matrix.
     * 
     * @param j
     *            the column index
     * @param procedure
     *            the matrix procedure
     */
    void eachNonZeroInColumn(int j, MatrixProcedure procedure);

    /**
     * Applies given {@code procedure} to each non-zero element of a range of specified column of this matrix.
     * 
     * @param j
     *            the column index
     * @param procedure
     *            the matrix procedure
     * @param fromRow
     *            The starting row (inclusive)
     * @param toRow
     *            The ending row (exclusive)
     */
    void eachNonZeroInColumn(int j, MatrixProcedure procedure, int fromRow, int toRow);

    /**
     * Updates all elements of this matrix by applying given {@code function}.
     * 
     * @param function
     *            the matrix function
     */
    void update(MatrixFunction function);

    /**
     * Updates the specified element of this matrix by applying given {@code function}.
     * 
     * @param i
     *            the row index
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     */
    void update(int i, int j, MatrixFunction function);

    /**
     * Updates all elements of the specified row in this matrix by applying given {@code function}.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     */
    void updateRow(int i, MatrixFunction function);

    /**
     * Updates a range of elements of the specified row in this matrix by applying given {@code function}.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     * @param fromColumn
     *            The first column index (inclusive)
     * @param toColumn
     *            The last column index (exclusive)
     */
    void updateRow(int i, MatrixFunction function, int fromColumn, int toColumn);

    /**
     * Updates all elements of the specified column in this matrix by applying given {@code function}.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     */
    void updateColumn(int j, MatrixFunction function);

    /**
     * Updates a range of elements of the specified column in this matrix by applying given {@code function}.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     * @param fromRow
     *            The first row index (inclusive)
     * @param toRow
     *            The last row index (exclusive)
     */
    void updateColumn(int j, MatrixFunction function, int fromRow, int toRow);

    /**
     * Updates all non zero elements of this matrix by applying given {@code function}.
     * 
     * @param function
     *            the matrix function
     */
    void updateNonZero(MatrixFunction function);

    /**
     * Updates all non zero elements of the specified row in this matrix by applying given {@code function}.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     */
    void updateNonZeroInRow(int i, MatrixFunction function);

    /**
     * Updates all non zero elements of a range of the specified row in this matrix by applying given {@code function}.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     * @param fromColumn
     *            The starting column (inclusive)
     * @param toColumn
     *            The ending column (exclusive)
     */
    void updateNonZeroInRow(int i, MatrixFunction function, int fromColumn, int toColumn);

    /**
     * Updates all non zero elements of the specified column in this matrix by applying given {@code function}.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     */
    void updateNonZeroInColumn(int j, MatrixFunction function);

    /**
     * Updates all non zero elements of a range of the specified column in this matrix by applying given
     * {@code function}.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     * @param fromRow
     *            The starting row (inclusive)
     * @param toRow
     *            The ending row (exclusive)
     */
    void updateNonZeroInColumn(int j, MatrixFunction function, int fromRow, int toRow);

    /**
     * Folds all elements of this matrix with given {@code accumulator}.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated value
     */
    byte fold(MatrixAccumulator accumulator);

    /**
     * Folds all elements of specified row in this matrix with given {@code accumulator}.
     * 
     * @param i
     *            the row index
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated value
     */
    byte foldRow(int i, MatrixAccumulator accumulator);

    /**
     * Folds all elements (in row-by-row manner) of this matrix with given {@code accumulator}.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated vector
     */
    ByteVector foldRows(MatrixAccumulator accumulator);

    /**
     * Folds all elements of specified column in this matrix with given {@code accumulator}.
     * 
     * @param j
     *            the column index
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated value
     */
    byte foldColumn(int j, MatrixAccumulator accumulator);

    /**
     * Folds all elements (in column-by-column manner) of this matrix with given {@code accumulator}.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated vector
     */
    ByteVector foldColumns(MatrixAccumulator accumulator);

    /**
     * Folds non-zero elements of this matrix with given {@code accumulator}.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated value
     */
    byte foldNonZero(MatrixAccumulator accumulator);

    /**
     * Folds non-zero elements of specified row in this matrix with given {@code accumulator}.
     * 
     * @param i
     *            the row index
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated value
     */
    byte foldNonZeroInRow(int i, MatrixAccumulator accumulator);

    /**
     * Folds non-zero elements of specified column in this matrix with given {@code accumulator}.
     * 
     * @param j
     *            the column index
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated value
     */
    byte foldNonZeroInColumn(int j, MatrixAccumulator accumulator);

    /**
     * Folds non-zero elements (in a column-by-column manner) of this matrix with given {@code accumulator}.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated vector
     */
    ByteVector foldNonZeroInColumns(MatrixAccumulator accumulator);

    /**
     * Folds non-zero elements (in a row-by-row manner) of this matrix with given {@code accumulator}.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @return the accumulated vector
     */
    ByteVector foldNonZeroInRows(MatrixAccumulator accumulator);

    /**
     * Checks whether this matrix compiles with given {@code predicate} or not.
     * 
     * @param predicate
     *            the matrix predicate
     * @return whether this matrix compiles with predicate
     */
    boolean is(MatrixPredicate predicate);

    /**
     * Checks whether this matrix compiles with given {@code predicate} or not.
     * 
     * @param predicate
     *            the advanced matrix predicate
     * @return whether this matrix compiles with predicate
     */
    boolean is(AdvancedMatrixPredicate predicate);

    /**
     * Checks whether this matrix compiles with given {@code predicate} or not.
     * 
     * @param predicate
     *            the matrix predicate
     * @return whether this matrix compiles with predicate
     */
    boolean non(MatrixPredicate predicate);

    /**
     * Checks whether this matrix compiles with given {@code predicate} or not.
     * 
     * @param predicate
     *            the advanced matrix predicate
     * @return whether this matrix compiles with predicate
     */
    boolean non(AdvancedMatrixPredicate predicate);

    /**
     * Converts this matrix into the row vector.
     * 
     * @return the row vector of this matrix
     */
    ByteVector toRowVector();

    /**
     * Converts this matrix into the row vector.
     * 
     * @param factory
     *            the factory of result vector
     * @return the row vector of this matrix
     */
    ByteVector toRowVector(Factory factory);

    /**
     * Converts this matrix into the column vector.
     * 
     * @return the column vector of this matrix
     */
    ByteVector toColumnVector();

    /**
     * Converts this matrix into the column vector.
     * 
     * @param factory
     *            the factory of result vector
     * @return the column vector of this matrix
     */
    ByteVector toColumnVector(Factory factory);

    /**
     * Searches for the maximum value of the elements of this matrix.
     * 
     * @return maximum value of this matrix
     */
    byte max();

    /**
     * Searches for the minimum value of the elements of this matrix.
     * 
     * @return minimum value of this matrix
     */
    byte min();

    /**
     * Searches for the maximum value of specified row in this matrix.
     * 
     * @param i
     *            the row index
     * @return maximum value of specified row in this matrix
     */
    byte maxInRow(int i);

    /**
     * Searches for the minimum value of specified row in this matrix.
     * 
     * @param i
     *            the row index
     * @return minimum value of specified row in this matrix
     */
    byte minInRow(int i);

    /**
     * Searches for the maximum value of specified column in this matrix.
     * 
     * @param j
     *            the column index
     * @return maximum value of specified column in this matrix
     */
    byte maxInColumn(int j);

    /**
     * Searches for the minimum value of specified column in this matrix.
     * 
     * @param j
     *            the column index
     * @return minimum value of specified column in this matrix
     */
    byte minInColumn(int j);

    /**
     * Returns a vector iterator over a row of this matrix (no copies are performed).
     * 
     * @param i
     *            The row index
     * @return a vector iterator
     */
    ByteVectorIterator rowIterator(int i);

    /**
     * Returns a vector iterator over a range of a row of this matrix (no copies are performed).
     * 
     * @param i
     *            The row index
     * @param fromColumn
     *            The starting column index (inclusive)
     * @param toColumn
     *            The ending column index (exclusive)
     * @return a vector iterator
     */
    ByteVectorIterator rowIterator(int i, int fromColumn, int toColumn);

    /**
     * Returns a vector iterator over a column of this matrix (no copies are performed).
     * 
     * @param j
     *            The column index
     * @return a vector iterator
     */
    ByteVectorIterator columnIterator(int j);

    /**
     * Returns a vector iterator over a range of a column of this matrix (no copies are performed).
     * 
     * @param j
     *            The column index
     * @param fromRow
     *            The starting row index (inclusive)
     * @param toRow
     *            The ending row index (exclusive)
     * @return a vector iterator
     */
    ByteVectorIterator columnIterator(int j, int fromRow, int toRow);

    /**
     * Returns a vector iterator over the non zero elements of a row of this matrix (no copies are performed).
     * 
     * @param i
     *            The row index
     * @return a non zero vector iterator
     */
    ByteVectorIterator nonZeroRowIterator(int i);

    /**
     * Returns a vector iterator over the non zero elements of a range of a row of this matrix (no copies are
     * performed).
     * 
     * @param i
     *            The row index
     * @param fromColumn
     *            The starting column index (inclusive)
     * @param toColumn
     *            The ending column index (exclusive)
     * @return a non zero vector iterator
     */
    ByteVectorIterator nonZeroRowIterator(int i, int fromColumn, int toColumn);

    /**
     * Returns a vector iterator over the non zero elements of a column of this matrix (no copies are performed).
     * 
     * @param j
     *            The column index
     * @return a non zero vector iterator
     */
    ByteVectorIterator nonZeroColumnIterator(int j);

    /**
     * Returns a vector iterator over the non zero elements of a range of a column of this matrix (no copies are
     * performed).
     * 
     * @param j
     *            The column index
     * @param fromRow
     *            The starting row index (inclusive)
     * @param toRow
     *            The ending row index (exclusive)
     * @return a non zero vector iterator
     */
    ByteVectorIterator nonZeroColumnIterator(int j, int fromRow, int toRow);

    /**
     * Adds one row to another.
     * 
     * @param sourceRow
     *            The index of the adder row
     * @param destRow
     *            The index of the row being added in place
     */
    void addRowsInPlace(int sourceRow, int destRow);

    /**
     * Adds a range of one row to a range of another.
     * 
     * @param sourceRow
     *            The index of the adder row
     * @param destRow
     *            The index of the row being added in place
     * @param fromColumn
     *            The starting column index (inclusive)
     * @param toColumn
     *            The ending column index (exclusive)
     */
    void addRowsInPlace(int sourceRow, int destRow, int fromColumn, int toColumn);

    /**
     * Multiplies a row by a given value and adds the result to another row.
     * 
     * @param sourceMultiplier
     *            The value to multiply the source row before it is added to the destination row
     * @param sourceRow
     *            The index of the adder row
     * @param destRow
     *            The index of the row being added in place
     */
    void addRowsInPlace(byte sourceMultiplier, int sourceRow, int destRow);

    /**
     * Multiplies a range of one row and adds the result to a range of another row.
     * 
     * @param sourceMultiplier
     *            The value to multiply the source row before it is added to the destination row
     * @param sourceRow
     *            The index of the adder row
     * @param destRow
     *            The index of the row being added in place
     * @param fromColumn
     *            The starting column index (inclusive)
     * @param toColumn
     *            The ending column index (exclusive)
     */
    void addRowsInPlace(byte sourceMultiplier, int sourceRow, int destRow, int fromColumn, int toColumn);

    /**
     * Serializes this matrix into a sequence of bytes and returns that sequence in a byte buffer.
     * <p>
     * The serialized data begins at the position of the returned buffer, and ends at its limit.
     * 
     * @return a byte buffer containing the serialized form of this matrix
     */
    ByteBuffer serializeToBuffer();

    /**
     * Serializes this matrix into a sequence of bytes and writes that sequence to a channel.
     * 
     * @param ch
     *            The channel used to write the serialized form of this matrix
     * @throws IOException
     *             If an I/O error occurs while writing
     */
    void serializeToChannel(WritableByteChannel ch) throws IOException;
}
