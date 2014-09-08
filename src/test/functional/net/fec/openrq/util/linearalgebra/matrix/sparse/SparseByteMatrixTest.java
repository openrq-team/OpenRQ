/*
 * Copyright 2014 Jose Lopes
 * 
 * Licensed under the Apache License, Version 2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2
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
 * Licensed under the Apache License, Version 2 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2
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


import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.fec.openrq.util.linearalgebra.matrix.AbstractByteMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;

import org.junit.Test;


public abstract class SparseByteMatrixTest extends AbstractByteMatrixTest {

    @Test
    public void testCardinality() {

        byte array[][] = new byte[][] {
                                       {1, 0, 0},
                                       {0, 5, 0},
                                       {0, 0, 9}
        };

        SparseByteMatrix a = (SparseByteMatrix)factory().createMatrix(array);

        assertEquals(3, a.cardinality());
    }

    @Test
    public void testLargeMatrix() {

        int i = 1000000;
        int j = 2000000;

        SparseByteMatrix a = (SparseByteMatrix)factory().createMatrix(i, j);

        assertEquals(i, a.rows());
        assertEquals(j, a.columns());

        for (int x = 0; x < i; x += 100000) {
            for (int y = 0; y < j; y += 500000) {
                a.set(x, y, aTimesB((byte)x, (byte)y));
            }
        }

        for (int x = 0; x < i; x += 100000) {
            for (int y = 0; y < j; y += 500000) {
                assertEquals(a.get(x, y), aTimesB((byte)x, (byte)y));
            }
        }
    }

    @Test
    public void testCapacityOverflow() {

        int i = 65536;
        int j = 65536;

        // Integer 65536 * 65536 overflows to 0
        assertEquals(0, i * j);

        SparseByteMatrix a = (SparseByteMatrix)factory().createMatrix(i, j);

        assertEquals(i, a.rows());
        assertEquals(j, a.columns());

        a.set(0, 0, (byte)42);
        assertEquals(a.get(0, 0), 42);

        a.set(i - 1, j - 1, (byte)7);
        assertEquals(a.get(i - 1, j - 1), 7);

        // Since values and Indices array sizes are align'd with CCSMatrix and
        // CRSMatrix.MINIMUM_SIZE (=32), we need to set more than 32 values.
        for (int row = 0; row < 32; row++) {
            a.set(row, 1, (byte)3);
        }
    }

    @Test
    public void testIssue141() {

        int i = 5000000;
        int j = 7340;

        // Test overflow
        assertTrue(i * j < 0);

        SparseByteMatrix a = (SparseByteMatrix)factory().createMatrix(i, j);

        assertEquals(i, a.rows());
        assertEquals(j, a.columns());

        for (int row = 0; row < 32; row++) {
            a.set(row, 1, (byte)3);
        }
    }

    @Test
    public void testFoldNonZero_3x3() {

        SparseByteMatrix a = (SparseByteMatrix)factory().createMatrix(new byte[][] {
                                                                                    {1, 0, 2},
                                                                                    {4, 0, 5},
                                                                                    {0, 0, 0}
        });

        MatrixAccumulator sum = ByteMatrices.asSumAccumulator((byte)0);
        MatrixAccumulator product = ByteMatrices.asProductAccumulator((byte)1);

        assertEquals(a.foldNonZero(sum), 2);
        // check whether the accumulator were flushed or not
        assertEquals(a.foldNonZero(sum), 2);

        assertEquals(a.foldNonZero(product), 40);
        // check whether the accumulator were flushed or not
        assertEquals(a.foldNonZero(product), 40);

        assertEquals(a.foldNonZeroInRow(1, product), 20);
        assertEquals(a.foldNonZeroInColumn(2, product), 10);

        ByteVector nonZeroInColumns = a.foldNonZeroInColumns(product);
        assertEquals(factory().createVector(new byte[] {4, 1, 10}), nonZeroInColumns);

        ByteVector nonZeroInRows = a.foldNonZeroInRows(product);
        assertEquals(factory().createVector(new byte[] {2, 20, 1}), nonZeroInRows);
    }

    @Test
    public void testIsZeroAt_5x3() {

        SparseByteMatrix a = (SparseByteMatrix)factory().createMatrix(new byte[][] {
                                                                                    {1, 0, 0},
                                                                                    {0, 0, 2},
                                                                                    {0, 0, 0},
                                                                                    {0, 3, 0},
                                                                                    {0, 0, 0}
        });

        assertTrue(a.isZeroAt(2, 2));
        assertFalse(a.isZeroAt(3, 1));
    }

    @Test
    public void testNonZeroAt_3x4() {

        SparseByteMatrix a = (SparseByteMatrix)factory().createMatrix(new byte[][] {
                                                                                    {0, 0, 2, 0},
                                                                                    {0, 0, 0, 0},
                                                                                    {0, 1, 0, 0}
        });

        assertTrue(a.nonZeroAt(2, 1));
        assertFalse(a.nonZeroAt(0, 3));
    }


    private static final class SetterProcedure implements MatrixProcedure {

        private final ByteMatrix matrix;


        SetterProcedure(ByteMatrix matrix) {

            this.matrix = matrix;
        }

        @Override
        public void apply(int i, int j, byte value) {

            matrix.set(i, j, value);
        }
    }


    @Test
    public void testEachNonZero() {

        final ByteMatrix initial = factory().createMatrix(new byte[][] {
                                                                        {7, 0, 7, 7, 7},
                                                                        {0, 7, 0, 7, 7},
                                                                        {0, 0, 7, 0, 7},
                                                                        {7, 0, 0, 7, 0},
                                                                        {7, 7, 0, 0, 7}
        });

        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = a.blank();

        a.eachNonZero(new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(a, b);
    }

    private ByteMatrix initialMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {5, 5, 9, 5, 5},
                                                    {5, 5, 0, 5, 5},
                                                    {9, 0, 9, 0, 9},
                                                    {5, 5, 0, 5, 5},
                                                    {5, 5, 9, 5, 5}
        });
    }

    private ByteMatrix bMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {3, 3, 3, 3, 3},
                                                    {3, 3, 3, 3, 3},
                                                    {3, 3, 3, 3, 3},
                                                    {3, 3, 3, 3, 3},
                                                    {3, 3, 3, 3, 3}
        });
    }

    @Test
    public void testEachNonZeroInRow() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {9, 3, 9, 3, 9},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInRow(2, new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRowInRangeOf_0_to_0() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInRow(2, new SetterProcedure(b), 0, 0);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRowInRangeOf_0_to_2() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {9, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInRow(2, new SetterProcedure(b), 0, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRowInRangeOf_0_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {9, 3, 9, 3, 9},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInRow(2, new SetterProcedure(b), 0, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRowInRangeOf_2_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 9, 3, 9},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInRow(2, new SetterProcedure(b), 2, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRowInRangeOf_5_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInRow(2, new SetterProcedure(b), 5, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInColumn() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 9, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 9, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 9, 3, 3}
        });

        a.eachNonZeroInColumn(2, new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInColumnInRangeOf_0_to_0() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInColumn(2, new SetterProcedure(b), 0, 0);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInColumnInRangeOf_0_to_2() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 9, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInColumn(2, new SetterProcedure(b), 0, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInColumnInRangeOf_0_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 9, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 9, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 9, 3, 3}
        });

        a.eachNonZeroInColumn(2, new SetterProcedure(b), 0, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInColumnInRangeOf_2_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 9, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 9, 3, 3}
        });

        a.eachNonZeroInColumn(2, new SetterProcedure(b), 2, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInColumnInRangeOf_5_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInColumn(2, new SetterProcedure(b), 5, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }


    private static final class IndexModulus2Function implements MatrixFunction {

        private final ByteMatrix matrix;


        IndexModulus2Function(ByteMatrix matrix) {

            this.matrix = matrix;
        }

        @Override
        public byte evaluate(int i, int j, @SuppressWarnings("unused") byte value) {

            // converts row/column indices into a "global" index
            int index = (i * matrix.rows()) + (j % matrix.columns());
            return (byte)(index % 2);
        }
    }


    @Test
    public void testUpdateNonZero() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {0, 1, 0, 1, 0},
                                                                  {1, 0, 0, 0, 1},
                                                                  {0, 0, 0, 0, 0},
                                                                  {1, 0, 0, 0, 1},
                                                                  {0, 1, 0, 1, 0}
        });

        a.updateNonZero(new IndexModulus2Function(a));

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRow() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {0, 0, 0, 0, 0},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInRow(2, new IndexModulus2Function(a));

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRowInRangeOf_0_to_0() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInRow(2, new IndexModulus2Function(a), 0, 0);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRowInRangeOf_0_to_2() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {0, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInRow(2, new IndexModulus2Function(a), 0, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRowInRangeOf_0_to_5() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {0, 0, 0, 0, 0},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInRow(2, new IndexModulus2Function(a), 0, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRowInRangeOf_2_to_5() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 0},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInRow(2, new IndexModulus2Function(a), 2, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRowInRangeOf_5_to_5() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInRow(2, new IndexModulus2Function(a), 5, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInColumn() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5}
        });

        a.updateNonZeroInColumn(2, new IndexModulus2Function(a));

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInColumnInRangeOf_0_to_0() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInColumn(2, new IndexModulus2Function(a), 0, 0);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInColumnInRangeOf_0_to_2() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInColumn(2, new IndexModulus2Function(a), 0, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInColumnInRangeOf_0_to_5() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5}
        });

        a.updateNonZeroInColumn(2, new IndexModulus2Function(a), 0, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInColumnInRangeOf_2_to_5() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5}
        });

        a.updateNonZeroInColumn(2, new IndexModulus2Function(a), 2, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInColumnInRangeOf_5_to_5() {

        final SparseByteMatrix a = (SparseByteMatrix)initialMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInColumn(2, new IndexModulus2Function(a), 5, 5);

        assertEquals(b, a);
    }

    @Test
    public void testTransformNonZero() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {0, 1, 0, 1, 0},
                                                                  {1, 0, 0, 0, 1},
                                                                  {0, 0, 0, 0, 0},
                                                                  {1, 0, 0, 0, 1},
                                                                  {0, 1, 0, 1, 0}
        });

        final ByteMatrix c = a.transformNonZero(new IndexModulus2Function(a));

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRow() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {0, 0, 0, 0, 0},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInRow(2, new IndexModulus2Function(a));

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRowInRangeOf_0_to_0() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInRow(2, new IndexModulus2Function(a), 0, 0);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRowInRangeOf_0_to_2() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {0, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInRow(2, new IndexModulus2Function(a), 0, 2);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRowInRangeOf_0_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {0, 0, 0, 0, 0},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInRow(2, new IndexModulus2Function(a), 0, 5);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRowInRangeOf_2_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 0},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInRow(2, new IndexModulus2Function(a), 2, 5);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRowInRangeOf_5_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInRow(2, new IndexModulus2Function(a), 5, 5);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInColumn() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInColumn(2, new IndexModulus2Function(a));

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInColumnInRangeOf_0_to_0() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInColumn(2, new IndexModulus2Function(a), 0, 0);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInColumnInRangeOf_0_to_2() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInColumn(2, new IndexModulus2Function(a), 0, 2);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInColumnInRangeOf_0_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInColumn(2, new IndexModulus2Function(a), 0, 5);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInColumnInRangeOf_2_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 0, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 0, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInColumn(2, new IndexModulus2Function(a), 2, 5);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInColumnInRangeOf_5_to_5() {

        final ByteMatrix initial = initialMatrix();
        final SparseByteMatrix a = (SparseByteMatrix)initial.copy();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        final ByteMatrix c = a.transformNonZeroInColumn(2, new IndexModulus2Function(a), 5, 5);

        assertEquals(initial, a);  // check if transform wrongly modifies the caller matrix
        assertEquals(b, c);
    }
}
