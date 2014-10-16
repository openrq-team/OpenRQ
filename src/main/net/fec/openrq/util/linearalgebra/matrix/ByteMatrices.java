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
 * Contributor(s): Yuriy Drozd
 * Ewald Grusk
 * Maxim Samoylov
 * Miron Aseev
 * Todd Brunhoff
 */
package net.fec.openrq.util.linearalgebra.matrix;


import static net.fec.openrq.util.math.OctetOps.aDividedByB;
import static net.fec.openrq.util.math.OctetOps.aMinusB;
import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static net.fec.openrq.util.math.OctetOps.aTimesB;
import static net.fec.openrq.util.math.OctetOps.maxByte;
import static net.fec.openrq.util.math.OctetOps.maxOfAandB;
import static net.fec.openrq.util.math.OctetOps.minByte;
import static net.fec.openrq.util.math.OctetOps.minOfAandB;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import net.fec.openrq.util.io.printing.appendable.PrintableAppendable;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.functor.AdvancedMatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixPredicate;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.matrix.source.Array1DMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.Array2DMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.IdentityMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.LoopbackMatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.matrix.source.RandomMatrixSource;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;


public final class ByteMatrices {

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/DiagonalMatrix.html">diagonal
     * matrix</a>.
     */
    public static final MatrixPredicate DIAGONAL_MATRIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) || (value == 0);
        }
    };

    /**
     * Checks whether the matrix is an
     * <a href="http://mathworld.wolfram.com/IdentityMatrix.html">identity
     * matrix</a>.
     */
    public static final MatrixPredicate IDENTITY_MATRIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) ? (value == 1) : (value == 0);
        }
    };

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/ZeroMatrix.html">zero
     * matrix</a>.
     */
    public static final MatrixPredicate ZERO_MATRIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return true;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return value == 0;
        }
    };

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/TridiagonalMatrix.html">tridiagonal
     * matrix</a>.
     */
    public static final MatrixPredicate TRIDIAGONAL_MATRIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return Math.abs(i - j) <= 1 || value == 0;
        }
    };

    /**
     * Checks whether the matrix is a lower bidiagonal matrix</a>.
     */
    public static final MatrixPredicate LOWER_BIDIAGONAL_MATRIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) || (i == j + 1) || value == 0;
        }
    };

    /**
     * Checks whether the matrix is an upper bidiagonal matrix.
     */
    public static final MatrixPredicate UPPER_BIDIAGONAL_MATRIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i == j) || (i == j - 1) || value == 0;
        }
    };

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/LowerTriangularMatrix.html">lower
     * triangular matrix</a>.
     */
    public static final MatrixPredicate LOWER_TRIANGULAR_MARTIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i >= j) || value == 0;
        }
    };

    /**
     * Checks whether the matrix is an
     * <a href="http://mathworld.wolfram.com/UpperTriangularMatrix.html">upper
     * triangular matrix</a>.
     */
    public static final MatrixPredicate UPPER_TRIANGULAR_MATRIX = new MatrixPredicate() {

        @Override
        public boolean test(int rows, int columns) {

            return rows == columns;
        }

        @Override
        public boolean test(int i, int j, byte value) {

            return (i <= j) || value == 0;
        }
    };

    /**
     * Checks whether the matrix is a
     * <a href="http://mathworld.wolfram.com/SymmetricMatrix.html">symmetric
     * matrix</a>.
     */
    public static final AdvancedMatrixPredicate SYMMETRIC_MATRIX = new AdvancedMatrixPredicate() {

        @Override
        public boolean test(ByteMatrix matrix) {

            if (matrix.rows() != matrix.columns()) {
                return false;
            }

            for (int i = 0; i < matrix.rows(); i++) {
                for (int j = i + 1; j < matrix.columns(); j++) {
                    byte a = matrix.get(i, j);
                    byte b = matrix.get(j, i);
                    if (a != b) {
                        return false;
                    }
                }
            }

            return true;
        }
    };

    /**
     * Checks whether the matrix is
     * <a href="http://en.wikipedia.org/wiki/Square_matrix">square</a>.
     */
    public static final AdvancedMatrixPredicate SQUARE_MATRIX = new AdvancedMatrixPredicate() {

        @Override
        public boolean test(ByteMatrix matrix) {

            return matrix.rows() == matrix.columns();
        }
    };


    /**
     * Creates a const function that evaluates it's argument to given {@code value}.
     * 
     * @param arg
     *            a constant value
     * @return a closure object that does {@code _}
     */
    public static MatrixFunction asConstFunction(final byte arg) {

        return new MatrixFunction() {

            @Override
            public byte evaluate(int i, int j, byte value) {

                return arg;
            }
        };
    }

    /**
     * Creates a plus function that adds given {@code value} to it's argument.
     * 
     * @param arg
     *            a value to be added to function's argument
     * @return a closure object that does {@code _ + _}
     */
    public static MatrixFunction asPlusFunction(final byte arg) {

        return new MatrixFunction() {

            @Override
            public byte evaluate(int i, int j, byte value) {

                return aPlusB(value, arg);
            }
        };
    }

    /**
     * Creates a minus function that subtracts given {@code value} from it's argument.
     * 
     * @param arg
     *            a value to be subtracted from function's argument
     * @return a closure that does {@code _ - _}
     */
    public static MatrixFunction asMinusFunction(final byte arg) {

        return new MatrixFunction() {

            @Override
            public byte evaluate(int i, int j, byte value) {

                return aMinusB(value, arg);
            }
        };
    }

    /**
     * Creates a mul function that multiplies given {@code value} by it's argument.
     * 
     * @param arg
     *            a value to be multiplied by function's argument
     * @return a closure that does {@code _ * _}
     */
    public static MatrixFunction asMulFunction(final byte arg) {

        return new MatrixFunction() {

            @Override
            public byte evaluate(int i, int j, byte value) {

                return aTimesB(value, arg);
            }
        };
    }

    /**
     * Creates a div function that divides it's argument by given {@code value}.
     * 
     * @param arg
     *            a divisor value
     * @return a closure that does {@code _ / _}
     */
    public static MatrixFunction asDivFunction(final byte arg) {

        return new MatrixFunction() {

            @Override
            public byte evaluate(int i, int j, byte value) {

                return aDividedByB(value, arg);
            }
        };
    }

    /**
     * Makes a minimum matrix accumulator that accumulates the minimum of matrix elements.
     * 
     * @return a minimum matrix accumulator
     */
    public static MatrixAccumulator mkMinAccumulator() {

        return new MatrixAccumulator() {

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
        };
    }

    /**
     * Makes a maximum matrix accumulator that accumulates the maximum of matrix elements.
     * 
     * @return a maximum matrix accumulator
     */
    public static MatrixAccumulator mkMaxAccumulator() {

        return new MatrixAccumulator() {

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
        };
    }

    /**
     * Creates a sum matrix accumulator that calculates the sum of all elements in the matrix.
     * 
     * @param neutral
     *            the neutral value
     * @return a sum accumulator
     */
    public static MatrixAccumulator asSumAccumulator(final byte neutral) {

        return new MatrixAccumulator() {

            private byte result = neutral;


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
        };
    }

    /**
     * Creates a product matrix accumulator that calculates the product of all elements in the matrix.
     * 
     * @param neutral
     *            the neutral value
     * @return a product accumulator
     */
    public static MatrixAccumulator asProductAccumulator(final byte neutral) {

        return new MatrixAccumulator() {

            private byte result = neutral;


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
        };
    }

    /**
     * Creates a function accumulator, that accumulates all elements in the matrix after applying given {@code function}
     * to each of them.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @param function
     *            the matrix function
     * @return a function accumulator
     */
    public static MatrixAccumulator asFunctionAccumulator(
        final MatrixAccumulator accumulator,
        final MatrixFunction function)
    {

        return new MatrixAccumulator() {

            @Override
            public void update(int i, int j, byte value) {

                accumulator.update(i, j, function.evaluate(i, j, value));
            }

            @Override
            public byte accumulate() {

                return accumulator.accumulate();
            }
        };
    }

    /**
     * Creates an accumulator procedure that adapts a matrix accumulator for procedure
     * interface. This is useful for reusing a single accumulator for multiple fold operations
     * in multiple matrices.
     * 
     * @param accumulator
     *            the matrix accumulator
     * @return an accumulator procedure
     */
    public static MatrixProcedure asAccumulatorProcedure(final MatrixAccumulator accumulator) {

        return new MatrixProcedure() {

            @Override
            public void apply(int i, int j, byte value) {

                accumulator.update(i, j, value);
            }
        };
    }

    /**
     * Creates a row vector source from a matrix source and a row index.
     * 
     * @param source
     * @param row
     * @return a row vector source from a matrix source and a row index
     */
    public static VectorSource asRowVectorSource(final MatrixSource source, final int row) {

        return new VectorSource() {

            @Override
            public int length() {

                return source.columns();
            }

            @Override
            public byte get(int j) {

                return source.get(row, j);
            }
        };
    }

    /**
     * Creates a column vector source from a matrix source and a column index.
     * 
     * @param source
     * @param column
     * @return a column vector source from a matrix source and a column index
     */
    public static VectorSource asColumnVectorSource(final MatrixSource source, final int column) {

        return new VectorSource() {

            @Override
            public int length() {

                return source.rows();
            }

            @Override
            public byte get(int i) {

                return source.get(i, column);
            }
        };
    }

    /**
     * Creates a row vector function from a matrix function and a row index.
     * 
     * @param function
     * @param row
     * @return a row vector function from a matrix function and a row index
     */
    public static VectorFunction asRowVectorFunction(final MatrixFunction function, final int row) {

        return new VectorFunction() {

            @Override
            public byte evaluate(int j, byte value) {

                return function.evaluate(row, j, value);
            }
        };
    }

    /**
     * Creates a column vector function from a matrix function and a column index.
     * 
     * @param function
     * @param column
     * @return a column vector function from a matrix function and a column index
     */
    public static VectorFunction asColumnVectorFunction(final MatrixFunction function, final int column) {

        return new VectorFunction() {

            @Override
            public byte evaluate(int i, byte value) {

                return function.evaluate(i, column, value);
            }
        };
    }

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
     *            * @param random the random generator instance
     * @return a random matrix source
     */
    public static MatrixSource asRandomSource(int rows, int columns, Random random) {

        return new RandomMatrixSource(rows, columns, random);
    }

    /**
     * Creates a default 1x1 matrix from given {@code value}.
     * 
     * @param value
     *            of the matrix
     * @return a default 1x1 matrix
     */
    public static ByteMatrix asMatrix1x1(byte value) {

        return LinearAlgebra.DEFAULT_FACTORY.createMatrix(new byte[][] {{value}});
    }

    /**
     * Creates a default 2x2 matrix from given {@code value}.
     * 
     * @param values
     *            of the matrix
     * @return a default 2x2 matrix
     */
    public static ByteMatrix asMatrix2x2(byte... values) {

        return LinearAlgebra.DEFAULT_FACTORY.createMatrix(unflatten(values, 2));
    }

    /**
     * Creates a default 3x3 matrix from given {@code value}.
     * 
     * @param values
     *            of the matrix
     * @return a default 3x3 matrix
     */
    public static ByteMatrix asMatrix3x3(byte... values) {

        return LinearAlgebra.DEFAULT_FACTORY.createMatrix(unflatten(values, 3));
    }

    /**
     * Creates a default 4x4 matrix from given {@code value}.
     * 
     * @param values
     *            of the matrix
     * @return a default 4x4 matrix
     */
    public static ByteMatrix asMatrix4x4(byte... values) {

        return LinearAlgebra.DEFAULT_FACTORY.createMatrix(unflatten(values, 4));
    }

    /**
     * TODO: It might be a good idea to put internal routines into a special utility class.
     * An internal routine that un-flats given 1D {@code array} to square 2D array with size {@code n}.
     * 
     * @param array
     *            the 1D array
     * @param n
     *            the size of square 2D array
     * @return the square 2D array
     */
    private static byte[][] unflatten(byte array[], int n) {

        byte result[][] = new byte[n][n];

        int m = Math.min(array.length, n * n);

        for (int i = 0; i < m; i++) {
            result[i / n][i % n] = array[i];
        }

        return result;
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

        final PrintableAppendable output = PrintableAppendable.of(appendable, true);
        final int R = matrix.rows();
        final int C = matrix.columns();

        // this prints a line with column indexes and a line for each row preceded by a row index
        // (this only works fine for indices less than 100)
        output.printf("   ");
        for (int j = 0; j < C; j++)
            output.printf("* %02d ", j);

        output.println('|');

        for (int i = 0; i < R; i++) {
            output.printf("%02d)", i);
            for (int j = 0; j < C; j++)
                output.printf("| %02X ", matrix.get(i, j));
            output.println('|');
        }
    }

    public static ByteMatrix deserializeMatrix(ByteBuffer buffer) throws DeserializationException {

        final Serialization.Type type = Serialization.readType(buffer);
        switch (type) {
            case DENSE_1D_MATRIX:
                return LinearAlgebra.BASIC1D_FACTORY.deserializeMatrix(buffer);

            case DENSE_2D_MATRIX:
                return LinearAlgebra.BASIC2D_FACTORY.deserializeMatrix(buffer);

            case SPARSE_ROW_MATRIX:
                return LinearAlgebra.CRS_FACTORY.deserializeMatrix(buffer);

            case SPARSE_COLUMN_MATRIX:
                return LinearAlgebra.CCS_FACTORY.deserializeMatrix(buffer);

            default:
                throw new DeserializationException("serialized data does not contain a byte matrix");
        }
    }

    public static ByteMatrix deserializeMatrix(ReadableByteChannel ch) throws IOException, DeserializationException {

        final Serialization.Type type = Serialization.readType(ch);
        switch (type) {
            case DENSE_1D_MATRIX:
                return LinearAlgebra.BASIC1D_FACTORY.deserializeMatrix(ch);

            case DENSE_2D_MATRIX:
                return LinearAlgebra.BASIC2D_FACTORY.deserializeMatrix(ch);

            case SPARSE_ROW_MATRIX:
                return LinearAlgebra.CRS_FACTORY.deserializeMatrix(ch);

            case SPARSE_COLUMN_MATRIX:
                return LinearAlgebra.CCS_FACTORY.deserializeMatrix(ch);

            default:
                throw new DeserializationException("serialized data does not contain a byte matrix");
        }
    }
}
