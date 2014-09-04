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
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra.matrix.sparse;


import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


public interface SparseByteMatrix extends ByteMatrix {

    /**
     * Returns the number of non-zero elements of this matrix.
     * 
     * @return the number of non-zero elements of this matrix
     */
    int cardinality();

    /**
     * Returns the density (non-zero elements divided by total elements)
     * of this sparse matrix.
     * 
     * @return the density of this matrix
     */
    double density();

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
     * Builds a new matrix by applying given {@code function} to each non zero element of this matrix.
     * 
     * @param function
     *            the matrix function
     * @return the transformed matrix
     */
    ByteMatrix transformNonZero(MatrixFunction function);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of this matrix.
     * 
     * @param function
     *            the matrix function
     * @param factory
     *            the factory of result matrix
     * @return the transformed matrix
     */
    ByteMatrix transformNonZero(MatrixFunction function, Factory factory);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of specified
     * row in this matrix.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInRow(int i, MatrixFunction function);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of specified
     * row in this matrix.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     * @param factory
     *            the factory of result matrix
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInRow(int i, MatrixFunction function, Factory factory);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of a range of specified
     * row in this matrix.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     * @param fromColumn
     *            The first column index (inclusive)
     * @param toColumn
     *            The last column index (exclusive)
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInRow(int i, MatrixFunction function, int fromColumn, int toColumn);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of a range of specified
     * row in this matrix.
     * 
     * @param i
     *            the row index
     * @param function
     *            the matrix function
     * @param fromColumn
     *            The first column index (inclusive)
     * @param toColumn
     *            The last column index (exclusive)
     * @param factory
     *            the factory of result matrix
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInRow(int i, MatrixFunction function, int fromColumn, int toColumn, Factory factory);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of specified
     * column in this matrix.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of specified
     * column in this matrix.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     * @param factory
     *            the factory of result matrix
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function, Factory factory);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of a range of specified
     * column in this matrix.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     * @param fromRow
     *            The first row index (inclusive)
     * @param toRow
     *            The last row index (exclusive)
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function, int fromRow, int toRow);

    /**
     * Builds a new matrix by applying given {@code function} to each non zero element of a range of specified
     * column in this matrix.
     * 
     * @param j
     *            the column index
     * @param function
     *            the matrix function
     * @param fromRow
     *            The first row index (inclusive)
     * @param toRow
     *            The last row index (exclusive)
     * @param factory
     *            the factory of result matrix
     * @return the transformed matrix
     */
    ByteMatrix transformNonZeroInColumn(int j, MatrixFunction function, int fromRow, int toRow, Factory factory);

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
}
