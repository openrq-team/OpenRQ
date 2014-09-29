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

package net.fec.openrq;


import net.fec.openrq.util.arithmetic.OctetOps;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.dense.DenseByteVector;


/**
 */
final class MatrixUtilities {

    static final void printMatrix(byte[][] matrix) {

        int M = matrix.length;
        int N = matrix[0].length;

        for (int i = 0; i < M; i++)
            if (matrix[i].length != N) throw new IllegalArgumentException("Invalid matrix dimensions.");

        System.out.printf("   ");
        for (int j = 0; j < N; j++)
            System.out.printf("* %02d ", j);

        System.out.println("|");

        for (int i = 0; i < M; i++) {
            System.out.printf("%02d)", i);
            for (int j = 0; j < N; j++)
                System.out.printf("| %02X ", (matrix[i][j]));
            System.out.println("|");
        }
    }

    /**
     * Multiplies Matrix A by Matrix B. Requires the number of columns in A
     * to be equal to the number of rows in B.
     * 
     * @param A
     *            Matrix A
     * @param B
     *            Matrix B
     * @return A*B
     */
    static final byte[][] multiplyMatrices(byte[][] A, byte[][] B) {

        // if (A[0].length != B.length) throw new RuntimeException("Illegal matrix dimensions.");

        byte[][] C = new byte[A.length][B[0].length];

        for (int i = 0; i < C.length; i++) {
            for (int j = 0; j < C[0].length; j++) {
                for (int k = 0; k < A[0].length; k++) {

                    byte temp = OctetOps.aTimesB(A[i][k], B[k][j]);

                    C[i][j] = (byte)(C[i][j] ^ temp);
                }
            }
        }

        return C;
    }

    /**
     * Multiplies a sub-matrix of Matrix A by a sub-matrix of Matrix B. Requires the number
     * of columns in A's sub-matrix to be equal to the number of rows in B's sub-matrix.
     * 
     * @param A
     *            Matrix A
     * @param first_rowA
     * @param first_colA
     * @param last_rowA
     * @param last_colA
     * @param B
     *            Matrix B
     * @param first_rowB
     * @param first_colB
     * @param last_rowB
     * @param last_colB
     * @return A*B
     */
    static byte[][] multiplyMatrices(byte[][] A, int first_rowA, int last_rowA, int first_colA, int last_colA,
        byte[][] B, int first_rowB, int last_rowB, int first_colB, int last_colB) {

        // if ((last_colA - first_colA) != (last_rowB - first_rowB)) throw new
        // RuntimeException("Illegal matrix dimensions.");

        int colsA = last_colA - first_colA;
        int colsB = last_colB - first_colB;
        int rowsA = last_rowA - first_rowA;

        byte[][] C = new byte[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {

                for (int k = 0; k < colsA; k++) {

                    byte temp = OctetOps.aTimesB(A[i + first_rowA][k + first_colA], B[k + first_rowB][j + first_colB]);

                    C[i][j] = (byte)(C[i][j] ^ temp);
                }
            }
        }

        return C;
    }

    /**
     * Multiplies a sub-matrix of Matrix A by a sub-matrix of Matrix B. Requires the number
     * of columns in A's sub-matrix to be equal to the number of rows in B's sub-matrix.
     * 
     * @param A
     *            Matrix A
     * @param first_rowA
     * @param first_colA
     * @param last_rowA
     * @param last_colA
     * @param B
     *            Matrix B
     * @param first_rowB
     * @param first_colB
     * @param last_rowB
     * @param last_colB
     * @param C
     *            Matrix where the results should be stored.
     * @param first_rowC
     * @param first_colC
     * @param last_rowC
     * @param last_colC
     */
    static void multiplyMatrices(byte[][] A, int first_rowA, int last_rowA, int first_colA, int last_colA,
        byte[][] B, int first_rowB, int last_rowB, int first_colB, int last_colB,
        byte[][] C, int first_rowC, int last_rowC, int first_colC, int last_colC) {

        // if ((last_colA - first_colA) != (last_rowB - first_rowB)) throw new
        // RuntimeException("Illegal matrix dimensions.");

        int colsA = last_colA - first_colA;
        int colsC = last_colC - first_colC;
        int rowsC = last_rowC - first_rowC;

        for (int i = 0; i < rowsC; i++) {
            for (int j = 0; j < colsC; j++) {
                for (int k = 0; k < colsA; k++) {

                    byte temp = OctetOps.aTimesB(A[i + first_rowA][k + first_colA], B[k + first_rowB][j + first_colB]);

                    C[i + first_rowC][j + first_colC] = (byte)(C[i + first_rowC][j + first_colC] ^ temp);
                }
            }
        }
    }

    /**
     * Multiplies a sub-matrix of Matrix A by a sub-matrix of Matrix B. Requires the number
     * of columns in A's sub-matrix to be equal to the number of rows in B's sub-matrix.
     * 
     * @param A
     *            Matrix A
     * @param first_rowA
     * @param first_colA
     * @param last_rowA
     * @param last_colA
     * @param B
     *            Matrix B
     * @param first_rowB
     * @param first_colB
     * @param last_rowB
     * @param last_colB
     * @return A*B
     */
    static ByteMatrix multiplyMatrices(ByteMatrix A, int first_rowA, int last_rowA, int first_colA, int last_colA,
        ByteMatrix B, int first_rowB, int last_rowB, int first_colB, int last_colB) {

        // if ((last_colA - first_colA) != (last_rowB - first_rowB)) throw new
        // RuntimeException("Illegal matrix dimensions.");

        final int[] aRowIndices = getIndicesFromRange(first_rowA, last_rowA);
        final int[] aColIndices = getIndicesFromRange(first_colA, last_colA);
        final ByteMatrix subA = A.select(aRowIndices, aColIndices);

        final int[] bRowIndices = getIndicesFromRange(first_rowB, last_rowB);
        final int[] bColIndices = getIndicesFromRange(first_colB, last_colB);
        final ByteMatrix subB = B.select(bRowIndices, bColIndices);

        return subA.multiply(subB);
    }

    private static int[] getIndicesFromRange(int from, int to) {

        if (from < 0 || to < from) {
            throw new IndexOutOfBoundsException("illegal from and to indices");
        }

        final int[] indices = new int[to - from];
        for (int n = from, i = 0; n < to; n++, i++) {
            indices[i] = n;
        }

        return indices;
    }

    /**
     * Multiplies a row (<code>line</code>) by a vector.
     * The number of columns in <code>line</code> must be equal to the number of rows
     * in <code>vector</coder>
     * 
     * @param line
     * @param line_length
     * @param vector
     * @return a vector
     */
    static byte[] multiplyByteLineBySymbolVector(byte[] line, int line_length, byte[][] vector) {

        final int symbol_size = vector[0].length;

        byte[] result = new byte[symbol_size];

        for (int octet = 0; octet < symbol_size; octet++) {

            for (int colRow = 0; colRow < line_length; colRow++) {

                byte temp = OctetOps.aTimesB(line[colRow], vector[colRow][octet]);

                result[octet] = (byte)(result[octet] ^ temp);
            }
        }

        return result;
    }

    /**
     * Multiplies a row (<code>line</code>) by a vector.
     * The number of columns in <code>line</code> must be equal to the number of rows
     * in <code>vector</coder>
     * 
     * @param line
     * @param line_length
     * @param vector
     * @return a vector
     */
    static byte[] multiplyByteLineBySymbolVector(ByteVector line, int line_length, byte[][] vector) {

        final ByteVector subLine = line.resize(line_length);
        final byte[][] subVector = new byte[line_length][];
        for (int i = 0; i < line_length; i++) {
            subVector[i] = vector[i];
        }

        final ByteMatrix vecMatrix = LinearAlgebra.BASIC2D_FACTORY.createMatrix(subVector);
        final DenseByteVector result = (DenseByteVector)subLine.multiply(vecMatrix, LinearAlgebra.DENSE_FACTORY);
        return result.toArray();
    }

    /**
     * Performs Gaussian elimination on a region of a matrix A and reduces the matrix region to a reduced row echelon
     * form. The operations are also performed in matrix D, with indices in d.
     * 
     * @param A
     * @param first_row
     * @param last_row
     * @param first_col
     * @param last_col
     * @param d
     * @param D
     */
    static void reduceToRowEchelonForm(
        ByteMatrix A,
        final int first_row,
        final int last_row,
        final int first_col,
        final int last_col,
        int[] d,
        byte[][] D) {

        int lead = 0;
        int rowCount = last_row - first_row;
        int columnCount = last_col - first_col;

        for (int r = 0; r < rowCount; r++) {

            if (columnCount <= lead) return;

            int i = r;
            while (A.get(i + first_row, lead + first_col) == 0) {

                i++;

                if (rowCount == i) {

                    i = r;
                    lead++;
                    if (columnCount == lead) return;
                }
            }

            if (i != r) {

                A.swapRows(i + first_row, r + first_row);

                // decoding process - swap d[i] with d[r] in d
                int auxIndex = d[i + first_row];
                d[i + first_row] = d[r + first_row];
                d[r + first_row] = auxIndex;

            }

            byte beta = A.get(r + first_row, lead + first_col);
            if (beta != 0) {
                // byte[] / byte
                A.updateRow(r + first_row, ByteMatrices.asDivFunction(beta), first_col, last_col);

                // decoding process - divide D[d[r]] by U_lower[r][lead]
                // byte[] / beta
                final int dIndex = d[r + first_row];
                OctetOps.betaDivision(beta, D[dIndex], D[dIndex]); // in place division
                // DEBUG
                // PRINTER.println(
                // "betaDivision((byte)" + beta + ",D[" + dIndex + "],D[" + dIndex + "]);");
            }

            for (i = 0; i < rowCount; i++) {

                beta = A.get(i + first_row, lead + first_col);

                if (i != r) {
                    // U_lower[i] - (U_lower[i][lead] * U_lower[r])
                    final ByteVector product = A.getRow(r + first_row, first_col, last_col);
                    product.update(ByteVectors.asMulFunction(beta));

                    A.updateRow(i + first_row,
                        new MatrixFunction() {

                            @Override
                            public byte evaluate(int notUsed, int col, byte value) {

                                return OctetOps.aMinusB(value, product.get(col - first_col));
                            }
                        },
                        first_col,
                        last_col);

                    // decoding process - D[d[i+first_row]] - (U_lower[i][lead] * D[d[r+first_row]])
                    byte[] p = OctetOps.betaProduct(beta, D[d[r + first_row]]);
                    addSymbolsInPlace(D[d[i + first_row]], p);
                    // DEBUG
                    // PRINTER.println(
                    // printVarDeclar(byte[].class, "p",
                    // "betaProduct((byte)" + beta + ",D[" + d[r + first_row] + "])"));
                    // PRINTER.println(
                    // "xorSymbolInPlace(D[" + d[i + first_row] + "],p);");
                }
            }

            lead++;
        }
    }

    static void swapColumns(byte[][] matrix, int a, int b) {

        // check sizes and limits and whatnot bla bla bla

        byte auxPos;

        for (int row = 0; row < matrix.length; row++) {
            // swap
            auxPos = matrix[row][a];
            matrix[row][a] = matrix[row][b];
            matrix[row][b] = auxPos;
        }
    }

    static byte[] addSymbols(byte[] s1, byte[] s2) {

        /*
         * if(s1.length != s2.length){
         * throw new IllegalArgumentException("Symbols must be of the same size.");
         * }
         */

        byte[] xor = new byte[s1.length];

        for (int i = 0; i < s1.length; i++) {
            xor[i] = OctetOps.aPlusB(s1[i], s2[i]);
        }

        return xor;
    }

    static byte[] addSymbolsWithMultiplier(byte[] s1, byte[] s2, byte s2Multiplier) {

        /*
         * if(s1.length != s2.length){
         * throw new IllegalArgumentException("Symbols must be of the same size.");
         * }
         */

        byte[] xor = new byte[s1.length];

        for (int i = 0; i < s1.length; i++) {
            xor[i] = OctetOps.aPlusB(s1[i], OctetOps.aTimesB(s2Multiplier, s2[i]));
        }

        return xor;
    }

    static void addSymbolsInPlace(byte[] s1, byte[] s2) {

        /*
         * if(s1.length != s2.length){
         * throw new IllegalArgumentException("Symbols must be of the same size.");
         * }
         */

        for (int i = 0; i < s1.length; i++) {
            s1[i] = OctetOps.aPlusB(s1[i], s2[i]);
        }
    }

    static void addSymbolsWithMultiplierInPlace(byte[] s1, byte[] s2, byte s2Multiplier) {

        /*
         * if(s1.length != s2.length){
         * throw new IllegalArgumentException("Symbols must be of the same size.");
         * }
         */

        for (int i = 0; i < s1.length; i++) {
            s1[i] = OctetOps.aPlusB(s1[i], OctetOps.aTimesB(s2Multiplier, s2[i]));
        }
    }

    static long ceilPrime(long p) {

        if (p == 1) p++;

        while (!isPrime(p))
            p++;

        return p;
    }

    static boolean isPrime(long n) {

        // check if n is a multiple of 2
        if (n % 2 == 0) return false;

        // if not, then just check the odds
        for (long i = 3; i * i <= n; i += 2)
            if (n % i == 0) return false;

        return true;
    }

    static int nonZeroRows(
        byte[][] matrix,
        int first_row,
        int last_row,
        int first_col,
        int last_col)
    {

        int nonZeroRows = 0;

        for (int row = first_row; row < last_row; row++) {

            for (int col = first_col; col < last_col; col++) {

                if (matrix[row][col] != 0) {
                    nonZeroRows++;
                    break;
                }
            }
        }

        return nonZeroRows;
    }

    static int nonZeroRows(
        ByteMatrix matrix,
        int first_row,
        int last_row,
        int first_col,
        int last_col)
    {

        int nonZeroRows = 0;

        for (int row = first_row; row < last_row; row++) {
            if (matrix.nonZerosInRow(row, first_col, last_col) != 0) {
                nonZeroRows++;
            }
        }

        return nonZeroRows;
    }

    /**
     * DEBUG
     * Solves a 'A.x = b' system using regular Gauss elimination.
     * 
     * @param A
     * @param b
     * @return Array of symbols 'x'
     * @throws SingularMatrixException
     */
    static byte[][] gaussElimination(byte[][] A, byte[][] b) throws SingularMatrixException
    {

        if (A.length != A[0].length || b.length != A.length) throw new RuntimeException("Illegal matrix dimensions.");

        int num_cols = b[0].length;

        byte[][] x = new byte[b.length][num_cols];

        // Utilities.printMatrix(A);

        int ROWS = b.length;

        for (int row = 0; row < ROWS; row++) {

            int max = row;

            // find pivot row and swap
            for (int i = row + 1; i < ROWS; i++)
                if (OctetOps.UNSIGN(A[i][row]) > OctetOps.UNSIGN(A[max][row])) max = i;

            // this destroys the original matrixes... dont really need a fix, but should be kept in mind
            byte[] temp = A[row];
            A[row] = A[max];
            A[max] = temp;

            temp = b[row];
            b[row] = b[max];
            b[max] = temp;

            // singular or nearly singular
            if (A[row][row] == 0) {
                // System.err.println("LINHA QUE DEU SINGULAR: "+row);
                // Utilities.printMatrix(b);
                // Utilities.printMatrix(A);
                throw new SingularMatrixException("LINHA QUE DEU SINGULAR: " + row);
            }

            // pivot within A and b
            for (int i = row + 1; i < ROWS; i++) {

                byte alpha = OctetOps.aDividedByB(A[i][row], A[row][row]);

                temp = OctetOps.betaProduct(alpha, b[row]);

                MatrixUtilities.addSymbolsInPlace(b[i], temp);

                for (int j = row; j < ROWS; j++) {

                    byte aux = OctetOps.aTimesB(alpha, A[row][j]);

                    A[i][j] = OctetOps.aMinusB(A[i][j], aux);
                }
            }
        }

        // back substitution
        for (int i = ROWS - 1; i >= 0; i--) {

            byte[] sum = new byte[num_cols];

            for (int j = i + 1; j < ROWS; j++) {

                // i*num_cols+j
                byte[] temp = OctetOps.betaProduct(A[i][j], x[j], 0, num_cols);

                MatrixUtilities.addSymbolsInPlace(sum, temp);
            }

            byte[] temp = MatrixUtilities.addSymbols(b[i], sum);

            x[i] = OctetOps.betaDivision(A[i][i], temp);
            // for (int bite = 0; bite < num_cols; bite++) {
            //
            // x[(i * num_cols) + bite] = OctetOps.division(temp[bite], A[i][i]);
            // }
        }

        return x;
    }
}
