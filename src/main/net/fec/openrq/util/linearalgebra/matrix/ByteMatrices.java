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
 * Contributor(s): Yuriy Drozd
 * Ewald Grusk
 * Maxim Samoylov
 * Miron Aseev
 * Todd Brunhoff
 */
package net.fec.openrq.util.linearalgebra.matrix;


import static net.fec.openrq.util.linearalgebra.ByteOps.aDividedByB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aIsEqualToB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aMinusB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aPlusB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aTimesB;
import static net.fec.openrq.util.linearalgebra.ByteOps.maxByte;
import static net.fec.openrq.util.linearalgebra.ByteOps.maxOfAandB;
import static net.fec.openrq.util.linearalgebra.ByteOps.minByte;
import static net.fec.openrq.util.linearalgebra.ByteOps.minOfAandB;

import java.io.IOException;

import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.functor.AdvancedMatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.source.Array1DMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.Array2DMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.IdentityMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.LoopbackMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.RandomMatrixSource;
import net.fec.openrq.util.printing.appendable.PrintableAppendable;



public final class ByteMatrices {

    private static class DiagonalMatrixPredicate implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) || aIsEqualToB(value, (byte)0);
        }
    }

    private static class IdentityMatrixPredicate implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) ? aIsEqualToB(value, (byte)1) : aIsEqualToB(value, (byte)0);
        }
    }

    private static class ZeroMatrixPredicate implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return true;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return aIsEqualToB(value, (byte)0);
        }
    }

    private static class TridiagonalMatrixPredicate implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return Math.abs(i - j) <= 1 || aIsEqualToB(value, (byte)0);
        }
    }

    private static class LowerBidiagonalMatrixPredicate
        implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) || (i == j + 1) || aIsEqualToB(value, (byte)0);
        }
    }

    private static class UpperBidiagonalMatrixPredicate
        implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) || (i == j - 1) || aIsEqualToB(value, (byte)0);
        }
    }

    private static class LowerTriangularMatrixPredicate
        implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i >= j) || aIsEqualToB(value, (byte)0);
        }
    }

    private static class UpperTriangularMatrixPredicate
        implements MatrixPredicate {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i <= j) || aIsEqualToB(value, (byte)0);
        }
    }

    private static class SymmetricMatrixPredicate
        implements AdvancedMatrixPredicate {

        @Override
        public boolean test(ByteMatrix matrix) {

            if (matrix.rows() != matrix.columns()) {
                return false;
            }

            for (int i = 0; i < matrix.rows(); i++) {
                for (int j = i + 1; j < matrix.columns(); j++) {
                    byte a = matrix.get(i, j);
                    byte b = matrix.get(j, i);
                    if (!aIsEqualToB(a, b)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    private static class SquareMatrixPredicate implements AdvancedMatrixPredicate {

        @Override
        public boolean test(ByteMatrix matrix) {

            return matrix.rows() == matrix.columns();
        }
    }

    private static class ConstMatrixFunction
        implements MatrixFunction {

        private byte arg;


        public ConstMatrixFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, int j, byte value) {

            return arg;
        }
    }

    private static class PlusMatrixFunction
        implements MatrixFunction {

        private byte arg;


        public PlusMatrixFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, int j, byte value) {

            return aPlusB(value, arg);
        }
    }

    private static class MinusMatrixFunction
        implements MatrixFunction {

        private byte arg;


        public MinusMatrixFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, int j, byte value) {

            return aMinusB(value, arg);
        }
    }

    private static class MulMatrixFunction
        implements MatrixFunction {

        private byte arg;


        public MulMatrixFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, int j, byte value) {

            return aTimesB(value, arg);
        }
    }

    private static class DivMatrixFunction
        implements MatrixFunction {

        private byte arg;


        public DivMatrixFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, int j, byte value) {

            return aDividedByB(value, arg);
        }
    }

    private static class SumMatrixAccumulator
        implements MatrixAccumulator {

        private final byte neutral;
        private byte result;


        public SumMatrixAccumulator(byte neutral) {

            this.neutral = neutral;
            this.result = neutral;
        }

        @Override
        public void update(int i, int j, byte value) {

            result = aPlusB(result, value);
        }

        @Override
        public byte accumulate() {

            byte value = result;
            result = neutral;
            return value;
        }
    }

    private static class ProductMatrixAccumulator
        implements MatrixAccumulator {

        private final byte neutral;
        private byte result;


        public ProductMatrixAccumulator(byte neutral) {

            this.neutral = neutral;
            this.result = neutral;
        }

        @Override
        public void update(int i, int j, byte value) {

            result = aTimesB(result, value);
        }

        @Override
        public byte accumulate() {

            byte value = result;
            result = neutral;
            return value;
        }
    }

    private static class FunctionMatrixAccumulator
        implements MatrixAccumulator {

        private final MatrixAccumulator accumulator;
        private final MatrixFunction function;


        public FunctionMatrixAccumulator(MatrixAccumulator accumulator,
            MatrixFunction function) {

            this.accumulator = accumulator;
            this.function = function;
        }

        @Override
        public void update(int i, int j, byte value) {

            accumulator.update(i, j, function.evaluate(i, j, value));
        }

        @Override
        public byte accumulate() {

            return accumulator.accumulate();
        }
    }

    private static class MinMatrixAccumulator
        implements MatrixAccumulator {

        private byte result = maxByte();


        @Override
        public void update(int i, int j, byte value) {

            result = minOfAandB(result, value);
        }

        @Override
        public byte accumulate() {

            byte value = result;
            result = maxByte();
            return value;
        }
    }

    private static class MaxMatrixAccumulator
        implements MatrixAccumulator {

        private byte result = minByte();


        @Override
        public void update(int i, int j, byte value) {

            result = maxOfAandB(result, value);
        }

        @Override
        public byte accumulate() {

            byte value = result;
            result = minByte();
            return value;
        }
    }


    /**
     * Creates a const function that evaluates it's argument to given {@code value}.
     * 
     * @param value
     *            a const value
     * @return a closure object that does {@code _}
     */
    public static MatrixFunction asConstFunction(byte value) {

        return new ConstMatrixFunction(value);
    }

    /**
     * Creates a plus function that adds given {@code value} to it's argument.
     * 
     * @param value
     *            a value to be added to function's argument
     * @return a closure object that does {@code _ + _}
     */
    public static MatrixFunction asPlusFunction(byte value) {

        return new PlusMatrixFunction(value);
    }

    /**
     * Creates a minus function that subtracts given {@code value} from it's argument.
     * 
     * @param value
     *            a value to be subtracted from function's argument
     * @return a closure that does {@code _ - _}
     */
    public static MatrixFunction asMinusFunction(byte value) {

        return new MinusMatrixFunction(value);
    }

    /**
     * Creates a mul function that multiplies given {@code value} by it's argument.
     * 
     * @param value
     *            a value to be multiplied by function's argument
     * @return a closure that does {@code _ * _}
     */
    public static MatrixFunction asMulFunction(byte value) {

        return new MulMatrixFunction(value);
    }

    /**
     * Creates a div function that divides it's argument by given {@code value}.
     * 
     * @param value
     *            a divisor value
     * @return a closure that does {@code _ / _}
     */
    public static MatrixFunction asDivFunction(byte value) {

        return new DivMatrixFunction(value);
    }


    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/DiagonalMatrix.html">diagonal
     * matrix</a>.
     */
    public static final MatrixPredicate DIAGONAL_MATRIX =
                                                          new DiagonalMatrixPredicate();

    /**
     * Checks whether the matrix is an
     * <a href="http://mathworld.wolfram.com/IdentityMatrix.html">identity
     * matrix</a>.
     */
    public static final MatrixPredicate IDENTITY_MATRIX =
                                                          new IdentityMatrixPredicate();

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/ZeroMatrix.html">zero
     * matrix</a>.
     */
    public static final MatrixPredicate ZERO_MATRIX =
                                                      new ZeroMatrixPredicate();

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/TridiagonalMatrix.html">tridiagonal
     * matrix</a>.
     */
    public static final MatrixPredicate TRIDIAGONAL_MATRIX =
                                                             new TridiagonalMatrixPredicate();

    /**
     * Checks whether the matrix is a lower bidiagonal matrix</a>.
     */
    public static final MatrixPredicate LOWER_BIDIAGONAL_MATRIX =
                                                                  new LowerBidiagonalMatrixPredicate();

    /**
     * Checks whether the matrix is an upper bidiagonal matrix.
     */
    public static final MatrixPredicate UPPER_BIDIAGONAL_MATRIX =
                                                                  new UpperBidiagonalMatrixPredicate();

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/LowerTriangularMatrix.html">lower
     * triangular matrix</a>.
     */
    public static final MatrixPredicate LOWER_TRIANGULAR_MARTIX =
                                                                  new LowerTriangularMatrixPredicate();

    /**
     * Checks whether the matrix is an
     * <a href="http://mathworld.wolfram.com/UpperTriangularMatrix.html">upper
     * triangular matrix</a>.
     */
    public static final MatrixPredicate UPPER_TRIANGULAR_MATRIX =
                                                                  new UpperTriangularMatrixPredicate();

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/SymmetricMatrix.html">symmetric
     * matrix</a>.
     */
    public static final AdvancedMatrixPredicate SYMMETRIC_MATRIX =
                                                                   new SymmetricMatrixPredicate();

    /**
     * Checks whether the matrix is
     * <a href="http://en.wikipedia.org/wiki/Square_matrix">square</a>.
     */
    public static final AdvancedMatrixPredicate SQUARE_MATRIX =
                                                                new SquareMatrixPredicate();


    /**
     * Creates a singleton 1x1 matrix of given {@code value}.
     * 
     * @param value
     *            the singleton value
     * @return a singleton matrix
     */
    public static ByteMatrix asSingletonMatrix(byte value) {

        return LinearAlgebra.DEFAULT_FACTORY.createMatrix(new byte[][] {{value}});
    }

    /**
     * Creates a matrix source of given {@code matrix}.
     * 
     * @param matrix
     *            the source matrix
     * @return a matrix source
     */
    public static MatrixSource asMatrixSource(ByteMatrix matrix) {

        return new LoopbackMatrixSource(matrix);
    }

    /**
     * Creates a 1D-array matrix source of given {@code array} reference.
     * 
     * @param rows
     *            the number of rows in the source
     * @param columns
     *            the number of columns in the source
     * @param array
     *            the array reference
     * @return a 1D-array matrix source
     */
    public static MatrixSource asArray1DSource(int rows, int columns, byte[] array) {

        return new Array1DMatrixSource(rows, columns, array);
    }

    /**
     * Creates a 2D-array matrix source of given {@code array} reference.
     * 
     * @param array
     *            the array reference
     * @return a 2D-array matrix source
     */
    public static MatrixSource asArray2DSource(byte[][] array) {

        return new Array2DMatrixSource(array);
    }

    /**
     * Creates an identity matrix source of given {@code size}.
     * 
     * @param size
     *            the source size
     * @return an identity matrix source
     */
    public static MatrixSource asIdentitySource(int size) {

        return new IdentityMatrixSource(size);
    }

    /**
     * Creates a random matrix source of specified dimensions.
     * 
     * @param rows
     *            the number of rows in the source
     * @param columns
     *            the number of columns in the source
     * @return a random matrix source
     */
    public static MatrixSource asRandomSource(int rows, int columns) {

        return new RandomMatrixSource(rows, columns);
    }

    /**
     * Makes a minimum matrix accumulator that accumulates the minimum of matrix elements.
     * 
     * @return a minimum vector accumulator
     */
    public static MatrixAccumulator mkMinAccumulator() {

        return new MinMatrixAccumulator();
    }

    /**
     * Makes a maximum matrix accumulator that accumulates the maximum of matrix elements.
     * 
     * @return a maximum vector accumulator
     */
    public static MatrixAccumulator mkMaxAccumulator() {

        return new MaxMatrixAccumulator();
    }

    /**
     * Creates a sum matrix accumulator that calculates the sum of all elements in the matrix.
     * 
     * @param neutral
     *            the neutral value
     * @return a sum accumulator
     */
    public static MatrixAccumulator asSumAccumulator(byte neutral) {

        return new SumMatrixAccumulator(neutral);
    }

    /**
     * Creates a product matrix accumulator that calculates the product of all elements in the matrix.
     * 
     * @param neutral
     *            the neutral value
     * @return a product accumulator
     */
    public static MatrixAccumulator asProductAccumulator(byte neutral) {

        return new ProductMatrixAccumulator(neutral);
    }

    /**
     * Creates a sum function accumulator, that calculates the sum of all
     * elements in the matrix after applying given {@code function} to each of them.
     * 
     * @param neutral
     *            the neutral value
     * @param function
     *            the matrix function
     * @return a sum function accumulator
     */
    public static MatrixAccumulator asSumFunctionAccumulator(byte neutral, MatrixFunction function) {

        return new FunctionMatrixAccumulator(new SumMatrixAccumulator(neutral), function);
    }

    /**
     * Creates a product function accumulator, that calculates the product of
     * all elements in the matrix after applying given {@code function} to
     * each of them.
     * 
     * @param neutral
     *            the neutral value
     * @param function
     *            the matrix function
     * @return a product function accumulator
     */
    public static MatrixAccumulator asProductFunctionAccumulator(byte neutral, MatrixFunction function) {

        return new FunctionMatrixAccumulator(new ProductMatrixAccumulator(neutral), function);
    }

    /**
     * Prints a matrix to a given appendable.
     * 
     * @param matrix
     *            the matrix to be printed
     * @param appendable
     *            the appendable on which the matrix is printed
     */
    public static void printMatrix(ByteMatrix matrix, Appendable appendable) {

        final PrintableAppendable output = new PrintableAppendable(appendable);
        final int R = matrix.rows();
        final int C = matrix.columns();

        // this prints a line with column indexes and a line for each row preceded by a row index
        // (this only works fine for indices less than 100)
        try {
            output.printf("    ");
            for (int j = 0; j < C; j++)
                output.printf("* %02d ", j);

            output.println('|');

            for (int i = 0; i < R; i++) {
                output.printf(" %02d)", i);
                for (int j = 0; j < C; j++)
                    output.printf("| %02x ", matrix.get(i, j));
                output.println('|');
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
