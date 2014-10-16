/*
 * Copyright 2014 OpenRQ Team
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
 * Contributor(s): Evgenia Krivova
 * Jakob Moellers
 * Maxim Samoylov
 * Anveshi Charuvaka
 * Todd Brunhoff
 * Catherine da Graca
 */
package net.fec.openrq.util.linearalgebra.matrix;


import static net.fec.openrq.util.math.OctetOps.aTimesB;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixAccumulator;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixProcedure;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;

import org.junit.Test;


public abstract class AbstractByteMatrixTest {

    public abstract Factory factory();

    @Test
    public void testAccess_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 3},
                                                            {0, 5, 0},
                                                            {7, 0, 9}
        });

        a.set(0, 1, aTimesB(a.get(1, 1), (byte)2));
        assertEquals(a.get(0, 1), 10);
    }

    private ByteMatrix getRowColumnMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {8, 3, 1, 9},
                                                    {4, 9, 6, 6},
                                                    {9, 1, 1, 4},
                                                    {5, 7, 3, 0}
        });
    }

    @Test
    public void testGetRow() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {8, 3, 1, 9});
        ByteVector vecB = factory().createVector(new byte[] {4, 9, 6, 6});
        ByteVector vecC = factory().createVector(new byte[] {9, 1, 1, 4});
        ByteVector vecD = factory().createVector(new byte[] {5, 7, 3, 0});

        assertEquals(vecA, matrix.getRow(0));
        assertEquals(vecB, matrix.getRow(1));
        assertEquals(vecC, matrix.getRow(2));
        assertEquals(vecD, matrix.getRow(3));
    }

    @Test
    public void testGetRowInRangeOf_0_to_0() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {});
        ByteVector vecB = factory().createVector(new byte[] {});
        ByteVector vecC = factory().createVector(new byte[] {});
        ByteVector vecD = factory().createVector(new byte[] {});

        assertEquals(vecA, matrix.getRow(0, 0, 0));
        assertEquals(vecB, matrix.getRow(1, 0, 0));
        assertEquals(vecC, matrix.getRow(2, 0, 0));
        assertEquals(vecD, matrix.getRow(3, 0, 0));
    }

    @Test
    public void testGetRowInRangeOf_0_to_2() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {8, 3});
        ByteVector vecB = factory().createVector(new byte[] {4, 9});
        ByteVector vecC = factory().createVector(new byte[] {9, 1});
        ByteVector vecD = factory().createVector(new byte[] {5, 7});

        assertEquals(vecA, matrix.getRow(0, 0, 2));
        assertEquals(vecB, matrix.getRow(1, 0, 2));
        assertEquals(vecC, matrix.getRow(2, 0, 2));
        assertEquals(vecD, matrix.getRow(3, 0, 2));
    }

    @Test
    public void testGetRowInRangeOf_0_to_4() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {8, 3, 1, 9});
        ByteVector vecB = factory().createVector(new byte[] {4, 9, 6, 6});
        ByteVector vecC = factory().createVector(new byte[] {9, 1, 1, 4});
        ByteVector vecD = factory().createVector(new byte[] {5, 7, 3, 0});

        assertEquals(vecA, matrix.getRow(0, 0, 4));
        assertEquals(vecB, matrix.getRow(1, 0, 4));
        assertEquals(vecC, matrix.getRow(2, 0, 4));
        assertEquals(vecD, matrix.getRow(3, 0, 4));
    }

    @Test
    public void testGetRowInRangeOf_2_to_2() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {});
        ByteVector vecB = factory().createVector(new byte[] {});
        ByteVector vecC = factory().createVector(new byte[] {});
        ByteVector vecD = factory().createVector(new byte[] {});

        assertEquals(vecA, matrix.getRow(0, 2, 2));
        assertEquals(vecB, matrix.getRow(1, 2, 2));
        assertEquals(vecC, matrix.getRow(2, 2, 2));
        assertEquals(vecD, matrix.getRow(3, 2, 2));
    }

    @Test
    public void testGetRowInRangeOf_2_to_4() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {1, 9});
        ByteVector vecB = factory().createVector(new byte[] {6, 6});
        ByteVector vecC = factory().createVector(new byte[] {1, 4});
        ByteVector vecD = factory().createVector(new byte[] {3, 0});

        assertEquals(vecA, matrix.getRow(0, 2, 4));
        assertEquals(vecB, matrix.getRow(1, 2, 4));
        assertEquals(vecC, matrix.getRow(2, 2, 4));
        assertEquals(vecD, matrix.getRow(3, 2, 4));
    }

    @Test
    public void testGetRowInRangeOf_4_to_4() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {});
        ByteVector vecB = factory().createVector(new byte[] {});
        ByteVector vecC = factory().createVector(new byte[] {});
        ByteVector vecD = factory().createVector(new byte[] {});

        assertEquals(vecA, matrix.getRow(0, 4, 4));
        assertEquals(vecB, matrix.getRow(1, 4, 4));
        assertEquals(vecC, matrix.getRow(2, 4, 4));
        assertEquals(vecD, matrix.getRow(3, 4, 4));
    }

    @Test
    public void testSetRow() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteMatrix b = factory().createMatrix(new byte[][] {{1, 2, 3, 4}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setRow(0, vector);

        assertEquals(a, b);
    }

    @Test
    public void testSetRow_InRangeOf_0_to_0() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteMatrix b = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setRow(0, 0, vector, 0, 0);

        assertEquals(a, b);
    }

    @Test
    public void testSetRow_InRangeOf_0_to_2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteMatrix b = factory().createMatrix(new byte[][] {{1, 2, 0, 0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setRow(0, 0, vector, 0, 2);

        assertEquals(a, b);
    }

    @Test
    public void testSetRow_InRangeOf_0_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteMatrix b = factory().createMatrix(new byte[][] {{1, 2, 3, 4}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setRow(0, 0, vector, 0, 4);

        assertEquals(a, b);
    }

    @Test
    public void testSetRow_InRangeOf_2_to_2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteMatrix b = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setRow(0, 0, vector, 2, 0); // yes, 0 is the length

        assertEquals(a, b);
    }

    @Test
    public void testSetRow_InRangeOf_2_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteMatrix b = factory().createMatrix(new byte[][] {{0, 0, 3, 4}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setRow(0, 2, vector, 2, 2); // yes, the second 2 is the length

        assertEquals(a, b);
    }

    @Test
    public void testSetRow_InRangeOf_4_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteMatrix b = factory().createMatrix(new byte[][] {{0, 0, 0, 0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setRow(0, 4, vector, 4, 0); // yes, 0 is the length

        assertEquals(a, b);
    }

    @Test
    public void testGetColumn() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {8, 4, 9, 5});
        ByteVector vecB = factory().createVector(new byte[] {3, 9, 1, 7});
        ByteVector vecC = factory().createVector(new byte[] {1, 6, 1, 3});
        ByteVector vecD = factory().createVector(new byte[] {9, 6, 4, 0});

        assertEquals(vecA, matrix.getColumn(0));
        assertEquals(vecB, matrix.getColumn(1));
        assertEquals(vecC, matrix.getColumn(2));
        assertEquals(vecD, matrix.getColumn(3));
    }

    @Test
    public void testGetColumnInRangeOf_0_to_0() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {});
        ByteVector vecB = factory().createVector(new byte[] {});
        ByteVector vecC = factory().createVector(new byte[] {});
        ByteVector vecD = factory().createVector(new byte[] {});

        assertEquals(vecA, matrix.getColumn(0, 0, 0));
        assertEquals(vecB, matrix.getColumn(1, 0, 0));
        assertEquals(vecC, matrix.getColumn(2, 0, 0));
        assertEquals(vecD, matrix.getColumn(3, 0, 0));
    }

    @Test
    public void testGetColumnInRangeOf_0_to_2() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {8, 4});
        ByteVector vecB = factory().createVector(new byte[] {3, 9});
        ByteVector vecC = factory().createVector(new byte[] {1, 6});
        ByteVector vecD = factory().createVector(new byte[] {9, 6});

        assertEquals(vecA, matrix.getColumn(0, 0, 2));
        assertEquals(vecB, matrix.getColumn(1, 0, 2));
        assertEquals(vecC, matrix.getColumn(2, 0, 2));
        assertEquals(vecD, matrix.getColumn(3, 0, 2));
    }

    @Test
    public void testGetColumnInRangeOf_0_to_4() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {8, 4, 9, 5});
        ByteVector vecB = factory().createVector(new byte[] {3, 9, 1, 7});
        ByteVector vecC = factory().createVector(new byte[] {1, 6, 1, 3});
        ByteVector vecD = factory().createVector(new byte[] {9, 6, 4, 0});

        assertEquals(vecA, matrix.getColumn(0, 0, 4));
        assertEquals(vecB, matrix.getColumn(1, 0, 4));
        assertEquals(vecC, matrix.getColumn(2, 0, 4));
        assertEquals(vecD, matrix.getColumn(3, 0, 4));
    }

    @Test
    public void testGetColumnInRangeOf_2_to_2() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {});
        ByteVector vecB = factory().createVector(new byte[] {});
        ByteVector vecC = factory().createVector(new byte[] {});
        ByteVector vecD = factory().createVector(new byte[] {});

        assertEquals(vecA, matrix.getColumn(0, 2, 2));
        assertEquals(vecB, matrix.getColumn(1, 2, 2));
        assertEquals(vecC, matrix.getColumn(2, 2, 2));
        assertEquals(vecD, matrix.getColumn(3, 2, 2));
    }

    @Test
    public void testGetColumnInRangeOf_2_to_4() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {9, 5});
        ByteVector vecB = factory().createVector(new byte[] {1, 7});
        ByteVector vecC = factory().createVector(new byte[] {1, 3});
        ByteVector vecD = factory().createVector(new byte[] {4, 0});

        assertEquals(vecA, matrix.getColumn(0, 2, 4));
        assertEquals(vecB, matrix.getColumn(1, 2, 4));
        assertEquals(vecC, matrix.getColumn(2, 2, 4));
        assertEquals(vecD, matrix.getColumn(3, 2, 4));
    }

    @Test
    public void testGetColumnInRangeOf_4_to_4() {

        ByteMatrix matrix = getRowColumnMatrix();

        ByteVector vecA = factory().createVector(new byte[] {});
        ByteVector vecB = factory().createVector(new byte[] {});
        ByteVector vecC = factory().createVector(new byte[] {});
        ByteVector vecD = factory().createVector(new byte[] {});

        assertEquals(vecA, matrix.getColumn(0, 4, 4));
        assertEquals(vecB, matrix.getColumn(1, 4, 4));
        assertEquals(vecC, matrix.getColumn(2, 4, 4));
        assertEquals(vecD, matrix.getColumn(3, 4, 4));
    }

    @Test
    public void testSetColumn() {

        ByteMatrix a = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteMatrix b = factory().createMatrix(new byte[][] { {1}, {2}, {3}, {4}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setColumn(0, vector);

        assertEquals(a, b);
    }

    @Test
    public void testSetColumn_InRangeOf_0_to_0() {

        ByteMatrix a = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteMatrix b = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setColumn(0, 0, vector, 0, 0);

        assertEquals(a, b);
    }

    @Test
    public void testSetColumn_InRangeOf_0_to_2() {

        ByteMatrix a = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteMatrix b = factory().createMatrix(new byte[][] { {1}, {2}, {0}, {0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setColumn(0, 0, vector, 0, 2);

        assertEquals(a, b);
    }

    @Test
    public void testSetColumn_InRangeOf_0_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteMatrix b = factory().createMatrix(new byte[][] { {1}, {2}, {3}, {4}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setColumn(0, 0, vector, 0, 4);

        assertEquals(a, b);
    }

    @Test
    public void testSetColumn_InRangeOf_2_to_2() {

        ByteMatrix a = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteMatrix b = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setColumn(0, 0, vector, 2, 0); // yes, 0 is the length

        assertEquals(a, b);
    }

    @Test
    public void testSetColumn_InRangeOf_2_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteMatrix b = factory().createMatrix(new byte[][] { {0}, {0}, {3}, {4}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setColumn(0, 2, vector, 2, 2); // yes, the second 2 is the length

        assertEquals(a, b);
    }

    @Test
    public void testSetColumn_InRangeOf_4_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteMatrix b = factory().createMatrix(new byte[][] { {0}, {0}, {0}, {0}});
        ByteVector vector = factory().createVector(new byte[] {1, 2, 3, 4});

        a.setColumn(0, 4, vector, 4, 0); // yes, 0 is the length

        assertEquals(a, b);
    }

    @Test
    public void testAssign_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {5, 5, 5},
                                                            {5, 5, 5},
                                                            {5, 5, 5}
        });

        ByteMatrix b = factory().createMatrix(3, 3);

        b.assign((byte)5);

        assertEquals(a, b);
    }

    @Test
    public void testResize_3x3_to_4x4_to_2x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 0},
                                                            {0, 5, 0, 0},
                                                            {0, 0, 9, 0},
                                                            {0, 0, 0, 0}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {1, 0},
                                                            {0, 5}
        });

        a = a.resize(a.rows() + 1, a.columns() + 1);
        assertEquals(b, a);

        a = a.resize(a.rows() - 2, a.columns() - 2);
        assertEquals(c, a);
    }

    @Test
    public void testResize_2x3_to_3x4_to_1x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 0},
                                                            {0, 5, 0, 0},
                                                            {0, 0, 0, 0},
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
        {1, 0},
        });

        a = a.resize(a.rows() + 1, a.columns() + 1);
        assertEquals(b, a);

        a = a.resize(a.rows() - 2, a.columns() - 2);
        assertEquals(c, a);
    }

    @Test
    public void testResize_2x3_to_2x4_to_2x1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 0},
                                                            {0, 5, 0, 0},
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {1},
                                                            {0}
        });

        a = a.resize(a.rows(), a.columns() + 1);
        assertEquals(b, a);

        a = a.resize(a.rows(), a.columns() - 3);
        assertEquals(c, a);
    }

    @Test
    public void testResize_3x5_to_4x5_to_2x5() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 0, 0},
                                                            {0, 5, 0, 0, 0},
                                                            {0, 0, 7, 0, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 0, 0},
                                                            {0, 5, 0, 0, 0},
                                                            {0, 0, 7, 0, 0},
                                                            {0, 0, 0, 0, 0},
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 0, 0},
                                                            {0, 5, 0, 0, 0},
        });

        a = a.resize(a.rows() + 1, a.columns());
        assertEquals(b, a);

        a = a.resize(a.rows() - 2, a.columns());
        assertEquals(c, a);
    }

    @Test
    public void testSlice_4x4_to_2x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 0},
                                                            {0, 5, 0, 0},
                                                            {0, 0, 9, 0},
                                                            {0, 0, 0, 15}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {5, 0},
                                                            {0, 9}
        });

        ByteMatrix c = a.slice(1, 1, 3, 3);
        assertEquals(b, c);
    }

    @Test
    public void testSlice_3x4_to_1x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 3, 0},
                                                            {0, 5, 0, 7},
                                                            {4, 0, 9, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
        {4, 0, 9, 0}
        });

        ByteMatrix c = a.slice(2, 0, 3, 4);
        assertEquals(b, c);
    }

    @Test
    public void testSwap_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 9},
                                                            {0, 5, 0},
                                                            {1, 0, 0}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {9, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 1}
        });

        a.swapRows(0, 2);
        assertEquals(b, a);

        b.swapColumns(0, 2);
        assertEquals(c, b);
    }

    @Test
    public void testSwap_2x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 3},
                                                            {0, 5, 4, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 5, 4, 0},
                                                            {1, 0, 0, 3}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {0, 4, 5, 0},
                                                            {1, 0, 0, 3}
        });

        a.swapRows(0, 1);
        assertEquals(b, a);

        b.swapColumns(1, 2);
        assertEquals(c, b);
    }

    @Test
    public void testSwap_5x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 4},
                                                            {7, 0, 2},
                                                            {0, 8, 0},
                                                            {5, 0, 6}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 4},
                                                            {0, 8, 0},
                                                            {7, 0, 2},
                                                            {5, 0, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 4, 5},
                                                            {0, 0, 8},
                                                            {7, 2, 0},
                                                            {5, 6, 0}
        });

        a.swapRows(2, 3);
        assertEquals(b, a);

        b.swapColumns(1, 2);
        assertEquals(c, b);
    }

    @Test
    public void testSwapRows() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 2, 3, 4},
                                                            {5, 6, 7, 8}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {5, 6, 7, 8},
                                                            {1, 2, 3, 4}
        });

        a.swapRows(0, 1);

        assertEquals(a, b);
    }

    @Test
    public void testSwapColumns() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 5},
                                                            {2, 6},
                                                            {3, 7},
                                                            {4, 8}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {5, 1},
                                                            {6, 2},
                                                            {7, 3},
                                                            {8, 4}
        });

        a.swapColumns(0, 1);

        assertEquals(a, b);
    }

    @Test
    public void testTranspose_4x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 14, 0, 4},
                                                            {0, 5, 10, 0},
                                                            {0, 3, 0, 2},
                                                            {11, 7, 0, 1}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0, 11},
                                                            {14, 5, 3, 7},
                                                            {0, 10, 0, 0},
                                                            {4, 0, 2, 1}
        });

        ByteMatrix c = a.transpose();
        assertEquals(b, c);

        ByteMatrix d = c.transpose();
        assertEquals(a, d);
    }

    @Test
    public void testTranspose_5x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 14, 0},
                                                            {0, 5, 10},
                                                            {0, 3, 0},
                                                            {11, 7, 0},
                                                            {12, 7, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0, 11, 12},
                                                            {14, 5, 3, 7, 7},
                                                            {0, 10, 0, 0, 0}
        });

        ByteMatrix c = a.transpose();
        assertEquals(b, c);

        ByteMatrix d = c.transpose();
        assertEquals(a, d);
    }

    @Test
    public void testTranspose_6x5() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 7, 3, 75},
                                                            {5, 86, 5, 98, 7},
                                                            {67, 21, 8, 1, 8},
                                                            {9, 9, 0, 2, 2},
                                                            {3, 4, 7, 3, 94},
                                                            {8, 6, 6, 4, 6}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {8, 5, 67, 9, 3, 8},
                                                            {3, 86, 21, 9, 4, 6},
                                                            {7, 5, 8, 0, 7, 6},
                                                            {3, 98, 1, 2, 3, 4},
                                                            {75, 7, 8, 2, 94, 6}
        });

        ByteMatrix c = a.transpose();
        assertEquals(b, c);

        ByteMatrix d = c.transpose();
        assertEquals(a, d);
    }

    @Test
    public void testAdd_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {11, 10, 10},
                                                            {10, 15, 10},
                                                            {10, 10, 3}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });

        // adds are XORs
        assertEquals(b, a.add((byte)10));
        assertEquals(c, a.add(a));
    }

    @Test
    public void testAdd_4x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0},
                                                            {0, 5},
                                                            {7, 0},
                                                            {0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {11, 10},
                                                            {10, 15},
                                                            {13, 10},
                                                            {10, 3}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {0, 0},
                                                            {0, 0},
                                                            {0, 0},
                                                            {0, 0}
        });

        assertEquals(b, a.add((byte)10));
        assertEquals(c, a.add(a));
    }

    @Test
    public void testSubtract_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {11, 10, 10},
                                                            {10, 15, 10},
                                                            {10, 10, 3}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });

        // subtracts are XORs
        assertEquals(b, a.subtract((byte)10));
        assertEquals(c, a.subtract(a));
    }

    @Test
    public void testSubtract_2x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 7, 0},
                                                            {0, 5, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {11, 10, 13, 10},
                                                            {10, 15, 10, 3}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0, 0},
                                                            {0, 0, 0, 0}
        });

        assertEquals(b, a.subtract((byte)10));
        assertEquals(c, a.subtract(a));
    }

    @Test
    public void testMultiply_2x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 3},
                                                            {0, 5, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {2, 0, 6},
                                                            {0, 10, 0}
        });

        assertEquals(b, a.multiply((byte)2));
    }

    @Test
    public void testMultiply_2x3_3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 3},
                                                            {0, 5, 0}
        });

        ByteVector b = factory().createVector(new byte[] {10, 0, 30});

        ByteVector c = factory().createVector(new byte[] {40, 0});

        // products are done over GF(2^8)
        // adds are XORs
        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_5x2_2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0},
                                                            {0, 5},
                                                            {7, 0},
                                                            {3, 0},
                                                            {0, 1}
        });

        ByteVector b = factory().createVector(new byte[] {0, 10});

        ByteVector c = factory().createVector(new byte[] {0, 34, 0, 0, 10});

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_1x1_1x1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{3}});

        ByteMatrix b = factory().createMatrix(new byte[][] {{8}});

        ByteMatrix c = factory().createMatrix(new byte[][] {{24}});

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_2x2_2x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {2, 1},
                                                            {77, 2}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {3, 88},
                                                            {82, 8}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {84, -72},
                                                            {115, 76}
        });

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_4x4_4x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {4, 9, 0, 3},
                                                            {6, 7, 7, 6},
                                                            {9, 4, 3, 3},
                                                            {4, 4, 1, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {7, 97, 3, 39},
                                                            {8, 27, 51, 36},
                                                            {59, 82, 0, 6},
                                                            {29, 52, 16, 24}
        });

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_4x4_4x4_InRangeOf_0x0_to_0x0() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {4, 9, 0, 3},
                                                            {6, 7, 7, 6},
                                                            {9, 4, 3, 3},
                                                            {4, 4, 1, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {});

        assertEquals(c, a.multiply(b, 0, 0, 0, 0, 0, 0, 0, 0));
    }

    @Test
    public void testMultiply_4x4_4x4_InRangeOf_0x0_to_2x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {4, 9, 0, 3},
                                                            {6, 7, 7, 6},
                                                            {9, 4, 3, 3},
                                                            {4, 4, 1, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {42, 65},
                                                            {38, 27},
        });

        assertEquals(c, a.multiply(b, 0, 2, 0, 2, 0, 2, 0, 2));
    }

    @Test
    public void testMultiply_4x4_4x4_InRangeOf_0x0_to_4x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {4, 9, 0, 3},
                                                            {6, 7, 7, 6},
                                                            {9, 4, 3, 3},
                                                            {4, 4, 1, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {7, 97, 3, 39},
                                                            {8, 27, 51, 36},
                                                            {59, 82, 0, 6},
                                                            {29, 52, 16, 24}
        });

        assertEquals(c, a.multiply(b, 0, 4, 0, 4, 0, 4, 0, 4));
    }

    @Test
    public void testMultiply_4x4_4x4_InRangeOf_2x2_to_2x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {4, 9, 0, 3},
                                                            {6, 7, 7, 6},
                                                            {9, 4, 3, 3},
                                                            {4, 4, 1, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {});

        assertEquals(c, a.multiply(b, 2, 2, 2, 2, 2, 2, 2, 2));
    }

    @Test
    public void testMultiply_4x4_4x4_InRangeOf_2x2_to_4x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {4, 9, 0, 3},
                                                            {6, 7, 7, 6},
                                                            {9, 4, 3, 3},
                                                            {4, 4, 1, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {7, 27},
                                                            {5, 5}
        });

        assertEquals(c, a.multiply(b, 2, 4, 2, 4, 2, 4, 2, 4));
    }

    @Test
    public void testMultiply_4x4_4x4_InRangeOf_4x4_to_4x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {4, 9, 0, 3},
                                                            {6, 7, 7, 6},
                                                            {9, 4, 3, 3},
                                                            {4, 4, 1, 6}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {});

        assertEquals(c, a.multiply(b, 4, 4, 4, 4, 4, 4, 4, 4));
    }

    @Test
    public void testMultiply_4x1_1x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {6},
                                                            {66},
                                                            {4},
                                                            {9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
        {5, 66, 6, 5}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {30, -111, 20, 30},
                                                            {87, -55, -111, 87},
                                                            {20, 21, 24, 20},
                                                            {45, 104, 54, 45}
        });

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_1x10_10x1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
        {8, 1, 5, 1, 21, 5, 2, 28, 7, 3}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {9},
                                                            {0},
                                                            {8},
                                                            {84},
                                                            {0},
                                                            {25},
                                                            {1},
                                                            {22},
                                                            {7},
                                                            {1}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
        {-56}
        });

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_3x2_2x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 9},
                                                            {9, 1},
                                                            {8, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 3, 0},
                                                            {2, 0, 4}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {18, 3, 36},
                                                            {2, 27, 4},
                                                            {18, 24, 36}
        });

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_4x9_9x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {5, 3, 91, 9, 2, 6, 0, 62, 1},
                                                            {8, 9, 76, 26, 7, 4, 39, 8, 85},
                                                            {4, 76, 67, 73, 8, 18, 9, 81, 6},
                                                            {4, 76, 4, 0, 2, 4, 8, 5, 7}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {98, 7, 4, 4},
                                                            {4, 2, 9, 5},
                                                            {44, 6, 19, 5},
                                                            {8, 26, 48, 7},
                                                            {2, 3, 72, 5},
                                                            {81, 9, 4, 0},
                                                            {9, 8, 53, 6},
                                                            {9, 48, 3, 3},
                                                            {59, 4, 53, 4}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {-104, 112, 105, 66},
                                                            {-6, 26, 4, -78},
                                                            {-126, 86, 73, 52},
                                                            {-111, 18, -5, 76}
        });

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_5_5x1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 1, 0, 2, 1}});
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1},
                                                            {3},
                                                            {0},
                                                            {3},
                                                            {0}
        });
        ByteVector c = factory().createVector(new byte[] {5});

        assertEquals(c, a.multiplyRow(0, b));
    }

    @Test
    public void testMultiply_5_0x0_InRangeOf_0_to_0() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 1, 0, 2, 1}});
        ByteMatrix b = factory().createMatrix(new byte[][] {});
        ByteVector c = factory().createVector(new byte[] {});

        assertEquals(c, a.multiplyRow(0, b, 0, 0));
    }

    @Test
    public void testMultiply_5_3x1_InRangeOf_0_to_3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 1, 0, 2, 1}});
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1},
                                                            {3},
                                                            {0}
        });
        ByteVector c = factory().createVector(new byte[] {3});

        assertEquals(c, a.multiplyRow(0, b, 0, 3));
    }

    @Test
    public void testMultiply_5_5x1_InRangeOf_0_to_5() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 1, 0, 2, 1}});
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1},
                                                            {3},
                                                            {0},
                                                            {3},
                                                            {0}
        });
        ByteVector c = factory().createVector(new byte[] {5});

        assertEquals(c, a.multiplyRow(0, b, 0, 5));
    }

    @Test
    public void testMultiply_5_0x0_InRangeOf_3_to_3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 1, 0, 2, 1}});
        ByteMatrix b = factory().createMatrix(new byte[][] {});
        ByteVector c = factory().createVector(new byte[] {});

        assertEquals(c, a.multiplyRow(0, b, 3, 3));
    }

    @Test
    public void testMultiply_5_2x1_InRangeOf_3_to_5() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 1, 0, 2, 1}});
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1},
                                                            {3}
        });
        ByteVector c = factory().createVector(new byte[] {1});

        assertEquals(c, a.multiplyRow(0, b, 3, 5));
    }

    @Test
    public void testMultiply_5_0x0_InRangeOf_5_to_5() {

        ByteMatrix a = factory().createMatrix(new byte[][] {{0, 1, 0, 2, 1}});
        ByteMatrix b = factory().createMatrix(new byte[][] {});
        ByteVector c = factory().createVector(new byte[] {});

        assertEquals(c, a.multiplyRow(0, b, 5, 5));
    }

    @Test
    public void testDivide_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-35, 0, 0},
                                                            {0, -114, 0},
                                                            {0, 0, 123}
        });

        // divisions are done in GF(2^8)
        assertEquals(b, a.divide((byte)10));
    }

    @Test
    public void testTrace_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        // additions are XORs
        assertEquals(a.trace(), 13);
    }

    @Test
    public void testDiagonalProduct_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        assertEquals(a.diagonalProduct(), 45);
    }

    @Test
    public void testProduct_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 1, 1},
                                                            {1, 5, 1},
                                                            {1, 1, 9}
        });

        assertEquals(a.product(), 45);
    }

    @Test
    public void testSum_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        assertEquals(a.sum(), 13);
    }

    @Test
    public void testHadamardProduct_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 2, 3},
                                                            {4, 5, 6},
                                                            {7, 8, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {9, 8, 7},
                                                            {6, 5, 4},
                                                            {3, 2, 1}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {9, 16, 9},
                                                            {24, 17, 24},
                                                            {9, 16, 9}
        });

        // multiplications are done in GF(2^8)
        assertEquals(c, a.hadamardProduct(b));
    }

    @Test
    public void testHadamardProduct_5x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 2},
                                                            {2, 3},
                                                            {3, 4},
                                                            {4, 5},
                                                            {5, 6}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {6, 5},
                                                            {5, 4},
                                                            {4, 3},
                                                            {3, 2},
                                                            {2, 1}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {6, 10},
                                                            {10, 12},
                                                            {12, 12},
                                                            {12, 10},
                                                            {10, 6}
        });

        assertEquals(c, a.hadamardProduct(b));
    }

    @Test
    public void testHadamardProduct_3x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 2, 3, 4},
                                                            {2, 3, 4, 5},
                                                            {3, 4, 5, 6},
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {6, 5, 4, 3},
                                                            {5, 4, 3, 2},
                                                            {4, 3, 2, 1},
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {6, 10, 12, 12},
                                                            {10, 12, 12, 10},
                                                            {12, 12, 10, 6},
        });

        assertEquals(c, a.hadamardProduct(b));
    }

    @Test
    public void testHadamardProduct_1x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
        {1, 2, 3, 4}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
        {6, 5, 4, 3}
        });

        ByteMatrix c = factory().createMatrix(new byte[][] {
        {6, 10, 12, 12}
        });

        assertEquals(c, a.hadamardProduct(b));
    }

    @Test
    public void testRowAccess_2x1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {99},
                                                            {88}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {99},
                                                            {99}
        });

        a.setRow(1, a.getRow(0));

        assertEquals(b, a);
    }

    @Test
    public void testRowAccess_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {1, 0, 0}
        });

        a.setRow(2, a.getRow(0));

        assertEquals(b, a);
    }

    @Test
    public void testRowAccess_2x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 4, 0},
                                                            {0, 5, 0, 7},
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 4, 0},
                                                            {1, 0, 4, 0},
        });

        a.setRow(1, a.getRow(0));

        assertEquals(b, a);
    }

    @Test
    public void testRowAccess_5x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 4},
                                                            {0, 5, 3},
                                                            {9, 0, 0},
                                                            {0, 1, 8},
                                                            {2, 0, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 4},
                                                            {0, 5, 3},
                                                            {9, 0, 0},
                                                            {9, 0, 0},
                                                            {2, 0, 0}
        });

        a.setRow(3, a.getRow(2));

        assertEquals(b, a);
    }

    @Test
    public void testRowAccess_6x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 18, 15, 0},
                                                            {1, 0, -55, 9},
                                                            {0, 0, 71, 19},
                                                            {-1, -8, 54, 0},
                                                            {25, 18, 0, 0},
                                                            {78, 28, 0, -8}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 18, 15, 0},
                                                            {1, 0, -55, 9},
                                                            {0, 0, 71, 19},
                                                            {-1, -8, 54, 0},
                                                            {25, 18, 0, 0},
                                                            {25, 18, 0, 0}
        });

        a.setRow(5, a.getRow(4));

        assertEquals(b, a);
    }

    /**
     * Returns true if both matrices contain the same elements and have equal dimensions.
     * 
     * @param matrix1
     *            ByteMatrix 1
     * @param matrix2
     *            ByteMatrix 2
     * @return True if both matrices contain the same elements and have equal dimensions.
     */
    private boolean testWhetherMatricesContainSameElements(ByteMatrix matrix1, ByteMatrix matrix2) {

        // Test for equal columns and rows
        if (matrix1.rows() != matrix2.rows()) {
            return false;
        }
        if (matrix1.columns() != matrix2.columns()) {
            return false;
        }

        final byte[] array1 = new byte[matrix1.columns() * matrix1.rows()];
        final byte[] array2 = new byte[matrix2.columns() * matrix2.rows()];

        for (int ii = 0; ii < matrix1.rows(); ii++) {
            for (int jj = 0; jj < matrix1.columns(); jj++) {
                array1[ii * matrix1.columns() + jj] = matrix1.get(ii, jj);
                array2[ii * matrix2.columns() + jj] = matrix2.get(ii, jj);
            }
        }

        Arrays.sort(array1);
        Arrays.sort(array2);

        for (int ii = 0; ii < array1.length; ii++) {
            if (array1[ii] != array2[ii]) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testTestWhetherMatricesContainSameElements() {

        ByteMatrix m1 = factory().createMatrix(new byte[][] {
                                                             {1, 1, 3},
                                                             {4, 5, 6},
                                                             {7, 8, 9}
        });

        ByteMatrix m2 = factory().createMatrix(new byte[][] {
                                                             {1, 1, 4},
                                                             {5, 6, 9},
                                                             {7, 3, 8}
        });

        assertTrue(testWhetherMatricesContainSameElements(m1, m2));

        ByteMatrix m3 = factory().createMatrix(new byte[][] {
                                                             {1, 1, 3},
                                                             {4, 52, 6},
                                                             {7, 8, 9}
        });

        assertFalse(testWhetherMatricesContainSameElements(m1, m3));
    }

    @Test
    public void testShuffle_3x2() {

        ByteMatrix m1 = factory().createMatrix(new byte[][] {
                                                             {1, 2},
                                                             {4, 5},
                                                             {7, 8}
        });

        ByteMatrix m2 = m1.shuffle();

        assertTrue(testWhetherMatricesContainSameElements(m1, m2));
    }

    @Test
    public void testShuffle_5x3() {

        ByteMatrix m1 = factory().createMatrix(new byte[][] {
                                                             {1, 2, 3},
                                                             {4, 5, 6},
                                                             {7, 8, 9},
                                                             {10, 11, 12},
                                                             {13, 14, 15}
        });

        ByteMatrix m2 = m1.shuffle();

        assertTrue(testWhetherMatricesContainSameElements(m1, m2));
    }

    @Test
    public void testRotate_3x1() {

        ByteMatrix m1 = factory().createMatrix(new byte[][] {
                                                             {1},
                                                             {3},
                                                             {5}
        });

        ByteMatrix m3 = factory().createMatrix(new byte[][] {
        {5, 3, 1}
        });

        ByteMatrix m2 = m1.rotate();

        assertTrue(m2.equals(m3));
    }

    @Test
    public void testRotate_2x2() {

        ByteMatrix m1 = factory().createMatrix(new byte[][] {
                                                             {1, 2},
                                                             {3, 4}
        });

        ByteMatrix m3 = factory().createMatrix(new byte[][] {
                                                             {3, 1},
                                                             {4, 2}
        });

        ByteMatrix m2 = m1.rotate();

        assertTrue(m2.equals(m3));
    }

    @Test
    public void testRotate_2x4() {

        ByteMatrix m1 = factory().createMatrix(new byte[][] {
                                                             {1, 2, 3, 4},
                                                             {5, 6, 7, 8}
        });

        ByteMatrix m3 = factory().createMatrix(new byte[][] {
                                                             {5, 1},
                                                             {6, 2},
                                                             {7, 3},
                                                             {8, 4}
        });

        ByteMatrix m2 = m1.rotate();

        assertTrue(m2.equals(m3));
    }

    @Test
    public void testRotate_5x3() {

        ByteMatrix m1 = factory().createMatrix(new byte[][] {
                                                             {1, 2, 3},
                                                             {4, 5, 6},
                                                             {7, 8, 9},
                                                             {10, 11, 12},
                                                             {13, 14, 15}
        });

        ByteMatrix m3 = factory().createMatrix(new byte[][] {
                                                             {13, 10, 7, 4, 1},
                                                             {14, 11, 8, 5, 2},
                                                             {15, 12, 9, 6, 3}
        });

        ByteMatrix m2 = m1.rotate();

        assertTrue(m2.equals(m3));
    }

    @Test
    public void testColumnAccess_2x1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
        {11, 22},
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
        {22, 22},
        });

        a.setColumn(0, a.getColumn(1));

        assertEquals(b, a);
    }

    @Test
    public void testColumnAccess_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 1},
                                                            {0, 5, 0},
                                                            {0, 0, 0}
        });

        a.setColumn(2, a.getColumn(0));

        assertEquals(b, a);
    }

    @Test
    public void testColumnAccess_2x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 4},
                                                            {0, 5, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0, 1},
                                                            {0, 5, 0, 0},
        });

        a.setColumn(3, a.getColumn(0));

        assertEquals(b, a);
    }

    @Test
    public void testColumnAccess_5x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 6},
                                                            {3, 0, 4},
                                                            {0, 0, 0},
                                                            {2, 7, 0}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 1, 0},
                                                            {0, 0, 6},
                                                            {3, 3, 4},
                                                            {0, 0, 0},
                                                            {2, 2, 0}
        });

        a.setColumn(1, a.getColumn(0));

        assertEquals(b, a);
    }

    @Test
    public void testColumnAccess_6x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 18, 15, 0},
                                                            {1, 0, -55, 9},
                                                            {0, 0, 71, 19},
                                                            {-1, -8, 54, 0},
                                                            {25, 18, 0, 0},
                                                            {78, 28, 0, -8}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 18, 15, 0},
                                                            {1, 0, -55, 1},
                                                            {0, 0, 71, 0},
                                                            {-1, -8, 54, -1},
                                                            {25, 18, 0, 25},
                                                            {78, 28, 0, 78}
        });

        a.setColumn(3, a.getColumn(0));

        assertEquals(b, a);
    }

    @Test
    public void testCopy_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 2, 3},
                                                            {4, 5, 6},
                                                            {7, 8, 9}
        });

        assertEquals(a, a.copy());
    }

    @Test
    public void testBlank_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 3},
                                                            {0, 0, 6},
                                                            {0, 0, 9}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });

        assertEquals(b, a.blank());
    }

    @Test
    public void testPower_2x2() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 2},
                                                            {3, 4}
        });

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {7, 10},
                                                            {15, 22}
        });

        ByteMatrix c = a.power(2);
        assertEquals(b, c);

        ByteMatrix d = factory().createMatrix(new byte[][] {
                                                            {25, 38},
                                                            {53, 70}
        });

        ByteMatrix e = a.power(3);
        assertEquals(d, e);

        ByteMatrix f = factory().createMatrix(new byte[][] {
                                                            {12, -16},
                                                            {-120, -119}
        });

        ByteMatrix g = a.power(6);
        assertEquals(f, g);
    }

    @Test
    public void testPower_3x3() {

        ByteMatrix h = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {4, 3, 6},
                                                            {0, 0, 9}
        });

        ByteMatrix i = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {32, 17, 75},
                                                            {0, 0, -52}
        });

        ByteMatrix j = h.power(4);
        assertEquals(i, j);

        ByteMatrix k = h.power(1);
        assertEquals(h, k);
    }

    @Test
    public void testMax() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, -1},
                                                            {0, -3, 0},
                                                            {6, -7, -2}
        });

        assertEquals(a.max(), -1);
    }

    @Test
    public void testMinCompressed() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1},
                                                            {0, 3, 0},
                                                            {0, 7, 2}
        });

        assertEquals(a.min(), 0);
    }

    @Test
    public void testMaxInRow() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {-2, 0, 0, -1}
        });

        assertEquals(a.maxInRow(2), -1);
    }

    @Test
    public void testMinInRow() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {2, 0, 0, 1}
        });

        assertEquals(a.minInRow(2), 0);
    }

    @Test
    public void testMaxInColumn() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {-2, 0, 0, -1}
        });

        assertEquals(a.maxInColumn(0), -2);
    }

    @Test
    public void testMinInColumn() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {-2, 0, 0, -1}
        });

        assertEquals(a.minInColumn(3), 0);
    }

    private ByteMatrix matrixA() {

        return factory().createMatrix(new byte[][] {
                                                    // 0 1 2 3 4 5
                                                    {8, 5, 67, 9, 3, 8},// 0
                                                    {3, 86, 21, 9, 4, 6},// 1
                                                    {7, 5, 8, 0, 7, 6},// 2
                                                    {3, 98, 1, 2, 3, 4},// 3
                                                    {75, 7, 8, 2, 94, 6} // 4
        });
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void xtestSelect1() {

        // Throw exception when row indices are invalid
        ByteMatrix a = matrixA();
        int[] rowInd = new int[] {3, 4, 10};
        int[] colInd = new int[] {0, 1, 2}; // all columns
        a.select(rowInd, colInd);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void xtestSelect2() {

        // Throw exception when column indices are invalid
        ByteMatrix a = matrixA();
        int[] rowInd = new int[] {0, 1, 2};
        int[] colInd = new int[] {-1, 1, 2};
        a.select(rowInd, colInd);
    }

    @Test
    public void testSelect3() {

        // All columns and a subset of rows selected.
        ByteMatrix a = matrixA();
        int[] rowInd = new int[] {1, 3, 4};
        int[] colInd = new int[] {0, 1, 2, 3, 4, 5}; // all columns
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            // 0 1 2 3 4 5
                                                            {3, 86, 21, 9, 4, 6},// 1
                                                            {3, 98, 1, 2, 3, 4},// 3
                                                            {75, 7, 8, 2, 94, 6} // 4
        });
        assertEquals(b, a.select(rowInd, colInd));
    }

    @Test
    public void testSelect4() {

        // All rows and a subset of columns selected.
        ByteMatrix a = matrixA();
        int[] rowInd = new int[] {0, 1, 2, 3, 4};
        int[] colInd = new int[] {0, 2, 4, 5}; // all columns
        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            // 0 2 4 5
                                                            {8, 67, 3, 8},// 0
                                                            {3, 21, 4, 6},// 1
                                                            {7, 8, 7, 6},// 2
                                                            {3, 1, 3, 4},// 3
                                                            {75, 8, 94, 6} // 4
        });
        assertEquals(c, a.select(rowInd, colInd));
    }

    @Test
    public void testSelect5() {

        // A subset of rows and columns is selected.
        ByteMatrix a = matrixA();
        int[] rowInd = new int[] {1, 3, 4};
        int[] colInd = new int[] {2, 4, 5};
        ByteMatrix d = factory().createMatrix(new byte[][] {
                                                            // 2 4 5
                                                            {21, 4, 6},// 1
                                                            {1, 3, 4},// 3
                                                            {8, 94, 6} // 4
        });
        assertEquals(d, a.select(rowInd, colInd));
    }

    @Test
    public void testSelect6() {

        // Duplication of rows and columns.
        ByteMatrix a = matrixA();
        int[] rowInd = new int[] {1, 3, 3, 4};
        int[] colInd = new int[] {2, 2, 4, 5, 5};
        ByteMatrix d = factory().createMatrix(new byte[][] {
                                                            // 2 2 4 5 5
                                                            {21, 21, 4, 6, 6},// 1
                                                            {1, 1, 3, 4, 4},// 3
                                                            {1, 1, 3, 4, 4},// 3
                                                            {8, 8, 94, 6, 6} // 4
        });
        assertEquals(d, a.select(rowInd, colInd));
    }

    @Test
    public void testFoldSum() {

        ByteMatrix d = factory().createMatrix(new byte[][] {
                                                            {6, 4, 3, 5},
                                                            {0, 5, 4, 0},
                                                            {0, 0, 10, 3},
                                                            {5, 1, 6, 5},
                                                            {5, 6, 7, 2},
                                                            {0, 1, 9, 1},
        });

        ByteVector columnSums = factory().createVector(new byte[] {6, 7, 5, 0});

        for (int col = 0; col < d.columns(); col++) {
            byte sum = d.foldColumn(col, ByteMatrices.asSumAccumulator((byte)0));
            assertEquals(sum, columnSums.get(col));
        }

        ByteVector s = d.foldColumns(ByteMatrices.asSumAccumulator((byte)0));
        assertEquals(s, columnSums);

        ByteVector rowSums = factory().createVector(new byte[] {4, 1, 9, 7, 6, 9});

        for (int row = 0; row < d.columns(); row++) {
            byte sum = d.foldRow(row, ByteMatrices.asSumAccumulator((byte)0));
            assertEquals(sum, rowSums.get(row));
        }

        s = d.foldRows(ByteMatrices.asSumAccumulator((byte)0));
        assertEquals(s, rowSums);
    }

    @Test
    public void testDiagonalMatrixPredicate() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 1, 0},
                                                            {0, 0, 1}
        });

        assertTrue(a.is(ByteMatrices.DIAGONAL_MATRIX));

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {2, 1, 0},
                                                            {0, 0, 1}
        });

        assertFalse(b.is(ByteMatrices.DIAGONAL_MATRIX));
    }

    @Test
    public void testIdentityMatrixPredicate() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 1, 0},
                                                            {0, 0, 1}
        });

        assertTrue(a.is(ByteMatrices.IDENTITY_MATRIX));

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {1, 0, 0},
                                                            {0, 0, 1}
        });

        assertFalse(b.is(ByteMatrices.IDENTITY_MATRIX));
    }

    @Test
    public void testZeroMatrixPredicate() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });

        assertTrue(a.is(ByteMatrices.ZERO_MATRIX));

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 1}
        });

        assertFalse(b.is(ByteMatrices.ZERO_MATRIX));
    }

    @Test
    public void testTridiagonalMatrixPredicate() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 1, 0, 0},
                                                            {1, 2, 3, 0},
                                                            {0, 1, 0, 2},
                                                            {0, 0, 1, 2}
        });

        assertTrue(a.is(ByteMatrices.TRIDIAGONAL_MATRIX));

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 1, 0, 0},
                                                            {1, 2, 3, 0},
                                                            {5, 0, 0, 2},
                                                            {0, 0, 1, 2}
        });

        assertFalse(b.is(ByteMatrices.TRIDIAGONAL_MATRIX));
    }

    @Test
    public void testSymmetricMatrixPredicate() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 1, 0, 0},
                                                            {1, 2, 3, 5},
                                                            {0, 3, 0, 0},
                                                            {0, 5, 0, 2}
        });

        assertTrue(a.is(ByteMatrices.SYMMETRIC_MATRIX));

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0, 0},
                                                            {0, 2, 3, 0},
                                                            {3, 3, 0, 0},
                                                            {0, 0, 0, 2}
        });

        assertFalse(b.is(ByteMatrices.SYMMETRIC_MATRIX));
    }

    @Test
    public void testNonZeros() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });

        assertEquals(0, a.nonZeros());

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1},
                                                            {0, 1, 0},
                                                            {1, 0, 0}
        });

        assertEquals(3, b.nonZeros());

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {1, 1, 1},
                                                            {1, 1, 1},
                                                            {1, 1, 1}
        });

        assertEquals(9, c.nonZeros());
    }

    @Test
    public void testNonZerosInRow() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });

        assertEquals(0, a.nonZerosInRow(1));

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 1, 0},
                                                            {0, 0, 0}
        });

        assertEquals(1, b.nonZerosInRow(1));

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {1, 1, 1},
                                                            {0, 0, 0}
        });

        assertEquals(3, c.nonZerosInRow(1));
    }

    private ByteMatrix nonZeroMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {7, 7, 1, 7, 7},
                                                    {7, 7, 0, 7, 7},
                                                    {0, 1, 0, 0, 1},
                                                    {7, 7, 1, 7, 7},
                                                    {7, 7, 0, 7, 7}
        });
    }

    @Test
    public void testNonZerosInRowInRangeOf_0_to_0() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(0, nonZeroMatrix.nonZerosInRow(2, 0, 0));
    }

    @Test
    public void testNonZerosInRowInRangeOf_0_to_2() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(1, nonZeroMatrix.nonZerosInRow(2, 0, 2));
    }

    @Test
    public void testNonZerosInRowInRangeOf_0_to_5() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(2, nonZeroMatrix.nonZerosInRow(2, 0, 5));
    }

    @Test
    public void testNonZerosInRowInRangeOf_2_to_2() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(0, nonZeroMatrix.nonZerosInRow(2, 2, 2));
    }

    @Test
    public void testNonZerosInRowInRangeOf_2_to_5() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(1, nonZeroMatrix.nonZerosInRow(2, 2, 5));
    }

    @Test
    public void testNonZerosInRowInRangeOf_5_to_5() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(0, nonZeroMatrix.nonZerosInRow(2, 5, 5));
    }

    @Test
    public void testNonZerosInColumn() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });

        assertEquals(0, a.nonZerosInColumn(1));

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 1, 0},
                                                            {0, 0, 0}
        });

        assertEquals(1, b.nonZerosInColumn(1));

        ByteMatrix c = factory().createMatrix(new byte[][] {
                                                            {0, 1, 0},
                                                            {0, 1, 0},
                                                            {0, 1, 0}
        });

        assertEquals(3, c.nonZerosInColumn(1));
    }

    @Test
    public void testNonZerosInColumnInRangeOf_0_to_0() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(0, nonZeroMatrix.nonZerosInColumn(2, 0, 0));
    }

    @Test
    public void testNonZerosInColumnInRangeOf_0_to_2() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(1, nonZeroMatrix.nonZerosInColumn(2, 0, 2));
    }

    @Test
    public void testNonZerosInColumnInRangeOf_0_to_5() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(2, nonZeroMatrix.nonZerosInColumn(2, 0, 5));
    }

    @Test
    public void testNonZerosInColumnInRangeOf_2_to_2() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(0, nonZeroMatrix.nonZerosInColumn(2, 2, 2));
    }

    @Test
    public void testNonZerosInColumnInRangeOf_2_to_5() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(1, nonZeroMatrix.nonZerosInColumn(2, 2, 5));
    }

    @Test
    public void testNonZerosInColumnInRangeOf_5_to_5() {

        final ByteMatrix nonZeroMatrix = nonZeroMatrix();
        assertEquals(0, nonZeroMatrix.nonZerosInColumn(2, 5, 5));
    }

    private ByteMatrix nonZeroPositionsInRowMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {0, 0, 0, 0},
                                                    {0, 1, 1, 0},
                                                    {1, 0, 0, 1},
                                                    {1, 1, 1, 1}
        });
    }

    @Test
    public void testNonZeroPositionsInRow() {

        ByteMatrix a = nonZeroPositionsInRowMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(0));
        assertArrayEquals(new int[] {1, 2}, a.nonZeroPositionsInRow(1));
        assertArrayEquals(new int[] {0, 3}, a.nonZeroPositionsInRow(2));
        assertArrayEquals(new int[] {0, 1, 2, 3}, a.nonZeroPositionsInRow(3));
    }

    @Test
    public void testNonZeroPositionsInRowInRangeOf_0_to_0() {

        ByteMatrix a = nonZeroPositionsInRowMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(0, 0, 0));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(1, 0, 0));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(2, 0, 0));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(3, 0, 0));
    }

    @Test
    public void testNonZeroPositionsInRowInRangeOf_0_to_2() {

        ByteMatrix a = nonZeroPositionsInRowMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(0, 0, 2));
        assertArrayEquals(new int[] {1}, a.nonZeroPositionsInRow(1, 0, 2));
        assertArrayEquals(new int[] {0}, a.nonZeroPositionsInRow(2, 0, 2));
        assertArrayEquals(new int[] {0, 1}, a.nonZeroPositionsInRow(3, 0, 2));
    }

    @Test
    public void testNonZeroPositionsInRowInRangeOf_0_to_4() {

        ByteMatrix a = nonZeroPositionsInRowMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(0, 0, 4));
        assertArrayEquals(new int[] {1, 2}, a.nonZeroPositionsInRow(1, 0, 4));
        assertArrayEquals(new int[] {0, 3}, a.nonZeroPositionsInRow(2, 0, 4));
        assertArrayEquals(new int[] {0, 1, 2, 3}, a.nonZeroPositionsInRow(3, 0, 4));
    }

    @Test
    public void testNonZeroPositionsInRowInRangeOf_2_to_2() {

        ByteMatrix a = nonZeroPositionsInRowMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(0, 2, 2));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(1, 2, 2));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(2, 2, 2));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(3, 2, 2));
    }

    @Test
    public void testNonZeroPositionsInRowInRangeOf_2_to_4() {

        ByteMatrix a = nonZeroPositionsInRowMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(0, 2, 4));
        assertArrayEquals(new int[] {2}, a.nonZeroPositionsInRow(1, 2, 4));
        assertArrayEquals(new int[] {3}, a.nonZeroPositionsInRow(2, 2, 4));
        assertArrayEquals(new int[] {2, 3}, a.nonZeroPositionsInRow(3, 2, 4));
    }

    @Test
    public void testNonZeroPositionsInRowInRangeOf_4_to_4() {

        ByteMatrix a = nonZeroPositionsInRowMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(0, 4, 4));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(1, 4, 4));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(2, 4, 4));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInRow(3, 4, 4));
    }

    private ByteMatrix nonZeroPositionsInColumnMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {0, 0, 1, 1},
                                                    {0, 1, 0, 1},
                                                    {0, 1, 0, 1},
                                                    {0, 0, 1, 1}
        });
    }

    @Test
    public void testNonZeroPositionsInColumn() {

        ByteMatrix a = nonZeroPositionsInColumnMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(0));
        assertArrayEquals(new int[] {1, 2}, a.nonZeroPositionsInColumn(1));
        assertArrayEquals(new int[] {0, 3}, a.nonZeroPositionsInColumn(2));
        assertArrayEquals(new int[] {0, 1, 2, 3}, a.nonZeroPositionsInColumn(3));
    }

    @Test
    public void testNonZeroPositionsInColumnInRangeOf_0_to_0() {

        ByteMatrix a = nonZeroPositionsInColumnMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(0, 0, 0));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(1, 0, 0));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(2, 0, 0));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(3, 0, 0));
    }

    @Test
    public void testNonZeroPositionsInColumnInRangeOf_0_to_2() {

        ByteMatrix a = nonZeroPositionsInColumnMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(0, 0, 2));
        assertArrayEquals(new int[] {1}, a.nonZeroPositionsInColumn(1, 0, 2));
        assertArrayEquals(new int[] {0}, a.nonZeroPositionsInColumn(2, 0, 2));
        assertArrayEquals(new int[] {0, 1}, a.nonZeroPositionsInColumn(3, 0, 2));
    }

    @Test
    public void testNonZeroPositionsInColumnInRangeOf_0_to_4() {

        ByteMatrix a = nonZeroPositionsInColumnMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(0, 0, 4));
        assertArrayEquals(new int[] {1, 2}, a.nonZeroPositionsInColumn(1, 0, 4));
        assertArrayEquals(new int[] {0, 3}, a.nonZeroPositionsInColumn(2, 0, 4));
        assertArrayEquals(new int[] {0, 1, 2, 3}, a.nonZeroPositionsInColumn(3, 0, 4));
    }

    @Test
    public void testNonZeroPositionsInColumnInRangeOf_2_to_2() {

        ByteMatrix a = nonZeroPositionsInColumnMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(0, 2, 2));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(1, 2, 2));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(2, 2, 2));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(3, 2, 2));
    }

    @Test
    public void testNonZeroPositionsInColumnInRangeOf_2_to_4() {

        ByteMatrix a = nonZeroPositionsInColumnMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(0, 2, 4));
        assertArrayEquals(new int[] {2}, a.nonZeroPositionsInColumn(1, 2, 4));
        assertArrayEquals(new int[] {3}, a.nonZeroPositionsInColumn(2, 2, 4));
        assertArrayEquals(new int[] {2, 3}, a.nonZeroPositionsInColumn(3, 2, 4));
    }

    @Test
    public void testNonZeroPositionsInColumnInRangeOf_4_to_4() {

        ByteMatrix a = nonZeroPositionsInColumnMatrix();
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(0, 4, 4));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(1, 4, 4));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(2, 4, 4));
        assertArrayEquals(new int[] {}, a.nonZeroPositionsInColumn(3, 4, 4));
    }

    private ByteMatrix initialMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {0, 1, 2},
                                                    {3, 0, 5},
                                                    {6, 7, 0}
        });
    }

    private static ByteMatrix minusOneMatrix(ByteMatrix a) {

        ByteMatrix b = a.blank();
        b.assign((byte)-1);
        return b;
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
    public void testEach() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);

        a.each(new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(a, b);
    }

    @Test
    public void testEachInRow() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {3, 0, 5},
                                                                  {-1, -1, -1}
        });

        a.eachInRow(1, new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInRowInRangeOf_0_to_0() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInRow(1, new SetterProcedure(b), 0, 0);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInRowInRangeOf_0_to_1() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {3, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInRow(1, new SetterProcedure(b), 0, 1);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInRowInRangeOf_0_to_3() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {3, 0, 5},
                                                                  {-1, -1, -1}
        });

        a.eachInRow(1, new SetterProcedure(b), 0, 3);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInRowInRangeOf_1_to_1() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInRow(1, new SetterProcedure(b), 1, 1);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInRowInRangeOf_1_to_3() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, 0, 5},
                                                                  {-1, -1, -1}
        });

        a.eachInRow(1, new SetterProcedure(b), 1, 3);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInRowInRangeOf_3_to_3() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInRow(1, new SetterProcedure(b), 3, 3);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInColumn() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, 1, -1},
                                                                  {-1, 0, -1},
                                                                  {-1, 7, -1}
        });

        a.eachInColumn(1, new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInColumnInRangeOf_0_to_0() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInColumn(1, new SetterProcedure(b), 0, 0);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInColumnInRangeOf_0_to_1() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, 1, -1},
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInColumn(1, new SetterProcedure(b), 0, 1);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInColumnInRangeOf_0_to_3() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, 1, -1},
                                                                  {-1, 0, -1},
                                                                  {-1, 7, -1}
        });

        a.eachInColumn(1, new SetterProcedure(b), 0, 3);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInColumnInRangeOf_1_to_1() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInColumn(1, new SetterProcedure(b), 1, 1);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInColumnInRangeOf_1_to_3() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, 0, -1},
                                                                  {-1, 7, -1}
        });

        a.eachInColumn(1, new SetterProcedure(b), 1, 3);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachInColumnInRangeOf_3_to_3() {

        final ByteMatrix initial = initialMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = minusOneMatrix(a);
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1},
                                                                  {-1, -1, -1}
        });

        a.eachInColumn(1, new SetterProcedure(b), 3, 3);

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


    private ByteMatrix allNinesMatrix() {

        return factory().createMatrix(new byte[][] {
                                                    {9, 9, 9},
                                                    {9, 9, 9},
                                                    {9, 9, 9}
        });
    }

    @Test
    public void testUpdate() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {0, 1, 0},
                                                                  {1, 0, 1},
                                                                  {0, 1, 0}
        });

        a.update(new IndexModulus2Function(a));

        assertEquals(b, a);
    }

    @Test
    public void testUpdateAt_1x1() {

        final ByteMatrix a = allNinesMatrix();

        a.update(1, 1, new IndexModulus2Function(a));

        assertEquals(0, a.get(1, 1));
    }

    @Test
    public void testUpdateRow() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {1, 0, 1},
                                                                  {9, 9, 9}
        });

        a.updateRow(1, new IndexModulus2Function(a));

        assertEquals(b, a);
    }

    @Test
    public void testUpdateRowInRangeOf_0_to_0() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateRow(1, new IndexModulus2Function(a), 0, 0);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateRowInRangeOf_0_to_1() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {1, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateRow(1, new IndexModulus2Function(a), 0, 1);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateRowInRangeOf_0_to_3() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {1, 0, 1},
                                                                  {9, 9, 9}
        });

        a.updateRow(1, new IndexModulus2Function(a), 0, 3);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateRowInRangeOf_1_to_1() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateRow(1, new IndexModulus2Function(a), 1, 1);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateRowInRangeOf_1_to_3() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 0, 1},
                                                                  {9, 9, 9}
        });

        a.updateRow(1, new IndexModulus2Function(a), 1, 3);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateRowInRangeOf_3_to_3() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateRow(1, new IndexModulus2Function(a), 3, 3);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateColumn() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 1, 9},
                                                                  {9, 0, 9},
                                                                  {9, 1, 9}
        });

        a.updateColumn(1, new IndexModulus2Function(a));

        assertEquals(b, a);
    }

    @Test
    public void testUpdateColumnInRangeOf_0_to_0() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateColumn(1, new IndexModulus2Function(a), 0, 0);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateColumnInRangeOf_0_to_1() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 1, 9},
                                                                  {9, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateColumn(1, new IndexModulus2Function(a), 0, 1);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateColumnInRangeOf_0_to_3() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 1, 9},
                                                                  {9, 0, 9},
                                                                  {9, 1, 9}
        });

        a.updateColumn(1, new IndexModulus2Function(a), 0, 3);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateColumnInRangeOf_1_to_1() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateColumn(1, new IndexModulus2Function(a), 1, 1);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateColumnInRangeOf_1_to_3() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 0, 9},
                                                                  {9, 1, 9}
        });

        a.updateColumn(1, new IndexModulus2Function(a), 1, 3);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateColumnInRangeOf_3_to_3() {

        final ByteMatrix a = allNinesMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {9, 9, 9},
                                                                  {9, 9, 9},
                                                                  {9, 9, 9}
        });

        a.updateColumn(1, new IndexModulus2Function(a), 3, 3);

        assertEquals(b, a);
    }

    @Test
    public void testFoldNonZero_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
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

        ByteMatrix a = factory().createMatrix(new byte[][] {
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

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 2, 0},
                                                            {0, 0, 0, 0},
                                                            {0, 1, 0, 0}
        });

        assertTrue(a.nonZeroAt(2, 1));
        assertFalse(a.nonZeroAt(0, 3));
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

        final ByteMatrix a = initial.copy();
        final ByteMatrix b = a.blank();

        a.eachNonZero(new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(a, b);
    }

    private ByteMatrix initialNonZeroMatrix() {

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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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
    public void testEachNonZeroInRowInRangeOf_2_to_2() {

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInRow(2, new SetterProcedure(b), 2, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRowInRangeOf_2_to_5() {

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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
    public void testEachNonZeroInColumnInRangeOf_2_to_2() {

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
        final ByteMatrix b = bMatrix();
        final ByteMatrix c = factory().createMatrix(new byte[][] {
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3},
                                                                  {3, 3, 3, 3, 3}
        });

        a.eachNonZeroInColumn(2, new SetterProcedure(b), 2, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller matrix
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInColumnInRangeOf_2_to_5() {

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

        final ByteMatrix initial = initialNonZeroMatrix();
        final ByteMatrix a = initial.copy();
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

    @Test
    public void testUpdateNonZero() {

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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
    public void testUpdateNonZeroInRowInRangeOf_2_to_2() {

        final ByteMatrix a = initialNonZeroMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInRow(2, new IndexModulus2Function(a), 2, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRowInRangeOf_2_to_5() {

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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
    public void testUpdateNonZeroInColumnInRangeOf_2_to_2() {

        final ByteMatrix a = initialNonZeroMatrix();
        final ByteMatrix b = factory().createMatrix(new byte[][] {
                                                                  {5, 5, 9, 5, 5},
                                                                  {5, 5, 0, 5, 5},
                                                                  {9, 0, 9, 0, 9},
                                                                  {5, 5, 0, 5, 5},
                                                                  {5, 5, 9, 5, 5}
        });

        a.updateNonZeroInColumn(2, new IndexModulus2Function(a), 2, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInColumnInRangeOf_2_to_5() {

        final ByteMatrix a = initialNonZeroMatrix();
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

        final ByteMatrix a = initialNonZeroMatrix();
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

    private ByteMatrix iteratorMatrix_1() {

        return factory().createMatrix(new byte[][] {
                                                    {-1, 0, -1, -1, -1, -1},
                                                    {0, 1, 0, 2, 3, 0},
                                                    {-1, 0, -1, -1, -1, -1},
                                                    {-1, 2, -1, -1, -1, -1},
                                                    {-1, 3, -1, -1, -1, -1},
                                                    {-1, 0, -1, -1, -1, -1}
        });
    }

    @Test
    public void testRowIterator_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.rowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 0, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 1, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 0, it.get());
        assertEquals("index 2", 2, it.index());

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        assertEquals("get 3", 2, it.get());
        assertEquals("index 3", 3, it.index());

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        assertEquals("get 4", 3, it.get());
        assertEquals("index 4", 4, it.index());

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        assertEquals("get 5", 0, it.get());
        assertEquals("index 5", 5, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_1_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.rowIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.rowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 0, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 1, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 0, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.rowIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 0, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 1, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 0, it.get());
        assertEquals("index 2", 2, it.index());

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        assertEquals("get 3", 2, it.get());
        assertEquals("index 3", 3, it.index());

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        assertEquals("get 4", 3, it.get());
        assertEquals("index 4", 4, it.index());

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        assertEquals("get 5", 0, it.get());
        assertEquals("index 5", 5, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_1_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.rowIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.rowIterator(1, 3, 6);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        assertEquals("get 3", 2, it.get());
        assertEquals("index 3", 3, it.index());

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        assertEquals("get 4", 3, it.get());
        assertEquals("index 4", 4, it.index());

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        assertEquals("get 5", 0, it.get());
        assertEquals("index 5", 5, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_1_InRangeOf_6_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.rowIterator(1, 6, 6);

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.columnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 0, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 1, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 0, it.get());
        assertEquals("index 2", 2, it.index());

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        assertEquals("get 3", 2, it.get());
        assertEquals("index 3", 3, it.index());

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        assertEquals("get 4", 3, it.get());
        assertEquals("index 4", 4, it.index());

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        assertEquals("get 5", 0, it.get());
        assertEquals("index 5", 5, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_1_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.columnIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.columnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 0, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 1, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 0, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.columnIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 0, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 1, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 0, it.get());
        assertEquals("index 2", 2, it.index());

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        assertEquals("get 3", 2, it.get());
        assertEquals("index 3", 3, it.index());

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        assertEquals("get 4", 3, it.get());
        assertEquals("index 4", 4, it.index());

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        assertEquals("get 5", 0, it.get());
        assertEquals("index 5", 5, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_1_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.columnIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.columnIterator(1, 3, 6);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        assertEquals("get 3", 2, it.get());
        assertEquals("index 3", 3, it.index());

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        assertEquals("get 4", 3, it.get());
        assertEquals("index 4", 4, it.index());

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        assertEquals("get 5", 0, it.get());
        assertEquals("index 5", 5, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_1_InRangeOf_6_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.columnIterator(1, 6, 6);

        assertFalse(it.hasNext());
    }

    private ByteMatrix iteratorMatrix_2() {

        return factory().createMatrix(new byte[][] {
                                                    {-1, 1, -1},
                                                    {1, 2, 3},
                                                    {-1, 3, -1},
        });
    }

    @Test
    public void testRowIterator_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.rowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_2_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.rowIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.rowIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.rowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_2_InRangeOf_1_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.rowIterator(1, 1, 1);

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.rowIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testRowIterator_2_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.rowIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.columnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_2_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.columnIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.columnIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.columnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_2_InRangeOf_1_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.columnIterator(1, 1, 1);

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.columnIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testColumnIterator_2_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.columnIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }


    // dummy values to iterator set() method in order to test consecutive calls to the method
    private static final byte ZERO_DUMMY = -1;
    private static final byte NONZERO_DUMMY = 0;


    private static void iteratorSet(String msg, ByteVectorIterator it, byte value) {

        it.set(value);
        assertEquals(msg, value, it.get());
    }

    @Test
    public void testRowIteratorModify_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {1, 0, 0, 1, 0, 1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.rowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("set dummy 0", it, NONZERO_DUMMY);
        iteratorSet("set 0", it, (byte)1);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("set dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("set dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        iteratorSet("set dummy 3", it, NONZERO_DUMMY);
        iteratorSet("set 3", it, (byte)1);

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        iteratorSet("set dummy 4", it, ZERO_DUMMY);
        iteratorSet("set 4", it, (byte)0);

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        iteratorSet("set dummy 5", it, NONZERO_DUMMY);
        iteratorSet("set 5", it, (byte)1);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testRowIteratorModify_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {1, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.rowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("set dummy 0", it, NONZERO_DUMMY);
        iteratorSet("set 0", it, (byte)1);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("set dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("set dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testRowIteratorModify_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {1, 0, 0, 1, 0, 1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.rowIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("set dummy 0", it, NONZERO_DUMMY);
        iteratorSet("set 0", it, (byte)1);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("set dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("set dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        iteratorSet("set dummy 3", it, NONZERO_DUMMY);
        iteratorSet("set 3", it, (byte)1);

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        iteratorSet("set dummy 4", it, ZERO_DUMMY);
        iteratorSet("set 4", it, (byte)0);

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        iteratorSet("set dummy 5", it, NONZERO_DUMMY);
        iteratorSet("set 5", it, (byte)1);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testRowIteratorModify_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 1, 0, 1, 0, 1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.rowIterator(1, 3, 6);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        iteratorSet("set dummy 3", it, NONZERO_DUMMY);
        iteratorSet("set 3", it, (byte)1);

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        iteratorSet("set dummy 4", it, ZERO_DUMMY);
        iteratorSet("set 4", it, (byte)0);

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        iteratorSet("set dummy 5", it, NONZERO_DUMMY);
        iteratorSet("set 5", it, (byte)1);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1, -1, -1, -1},
                                                            {0, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 1, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 1, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.columnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("set dummy 0", it, NONZERO_DUMMY);
        iteratorSet("set 0", it, (byte)1);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("set dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("set dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        iteratorSet("set dummy 3", it, NONZERO_DUMMY);
        iteratorSet("set 3", it, (byte)1);

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        iteratorSet("set dummy 4", it, ZERO_DUMMY);
        iteratorSet("set 4", it, (byte)0);

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        iteratorSet("set dummy 5", it, NONZERO_DUMMY);
        iteratorSet("set 5", it, (byte)1);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1, -1, -1, -1},
                                                            {0, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.columnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("set dummy 0", it, NONZERO_DUMMY);
        iteratorSet("set 0", it, (byte)1);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("set dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("set dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1, -1, -1, -1},
                                                            {0, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 1, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 1, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.columnIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("set dummy 0", it, NONZERO_DUMMY);
        iteratorSet("set 0", it, (byte)1);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("set dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("set dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        iteratorSet("set dummy 3", it, NONZERO_DUMMY);
        iteratorSet("set 3", it, (byte)1);

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        iteratorSet("set dummy 4", it, ZERO_DUMMY);
        iteratorSet("set 4", it, (byte)0);

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        iteratorSet("set dummy 5", it, NONZERO_DUMMY);
        iteratorSet("set 5", it, (byte)1);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 1, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 1, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 1, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.columnIterator(1, 3, 6);

        assertTrue("hasNext 3", it.hasNext());
        it.next();
        iteratorSet("set dummy 3", it, NONZERO_DUMMY);
        iteratorSet("set 3", it, (byte)1);

        assertTrue("hasNext 4", it.hasNext());
        it.next();
        iteratorSet("set dummy 4", it, ZERO_DUMMY);
        iteratorSet("set 4", it, (byte)0);

        assertTrue("hasNext 5", it.hasNext());
        it.next();
        iteratorSet("set dummy 5", it, NONZERO_DUMMY);
        iteratorSet("set 5", it, (byte)1);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testRowIteratorModify_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {0, 5, 0},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.rowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy set 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy set 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)5);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy set 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testRowIteratorModify_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {0, 2, 3},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.rowIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy set 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testRowIteratorModify_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {0, 5, 0},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.rowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy set 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy set 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)5);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy set 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testRowIteratorModify_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {1, 5, 0},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.rowIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy set 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)5);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy set 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1},
                                                            {1, 5, 3},
                                                            {-1, 0, -1},
        });

        ByteVectorIterator it = a.columnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy set 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy set 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)5);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy set 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1},
                                                            {1, 2, 3},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.columnIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy set 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1},
                                                            {1, 5, 3},
                                                            {-1, 0, -1},
        });

        ByteVectorIterator it = a.columnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy set 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy set 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)5);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy set 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testColumnIteratorModify_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {1, 5, 3},
                                                            {-1, 0, -1},
        });

        ByteVectorIterator it = a.columnIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy set 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)5);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy set 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIterator_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroRowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 3, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 4, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_1_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 3, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 4, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_1_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 3, 6);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 3, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 4, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_1_InRangeOf_6_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 6, 6);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroColumnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 3, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 4, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_1_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 3, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 4, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_1_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 3, 6);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 3, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 4, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_1_InRangeOf_6_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 6, 6);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroRowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_2_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_2_InRangeOf_1_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 1, 1);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowIterator_2_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroRowIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroColumnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_2_InRangeOf_0_to_0() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_2_InRangeOf_1_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 1, 1);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        assertEquals("get 1", 2, it.get());
        assertEquals("index 1", 1, it.index());

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        assertEquals("get 2", 3, it.get());
        assertEquals("index 2", 2, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroColumnIterator_2_InRangeOf_3_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteVectorIterator it = a.nonZeroColumnIterator(1, 3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroRowAndColumnIterator_3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });
        ByteVectorIterator rowIt = a.nonZeroRowIterator(1);
        assertFalse("row", rowIt.hasNext());

        ByteVectorIterator colIt = a.nonZeroColumnIterator(1);
        assertFalse("column", colIt.hasNext());
    }

    @Test
    public void testNonZeroRowAndColumnIterator_3_InRangeOf_0_to_0() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });
        ByteVectorIterator rowIt = a.nonZeroRowIterator(1, 0, 0);
        assertFalse("row", rowIt.hasNext());

        ByteVectorIterator colIt = a.nonZeroColumnIterator(1, 0, 0);
        assertFalse("column", colIt.hasNext());
    }

    @Test
    public void testNonZeroRowAndColumnIterator_3_InRangeOf_0_to_1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });
        ByteVectorIterator rowIt = a.nonZeroRowIterator(1, 0, 1);
        assertFalse("row", rowIt.hasNext());

        ByteVectorIterator colIt = a.nonZeroColumnIterator(1, 0, 1);
        assertFalse("column", colIt.hasNext());
    }

    @Test
    public void testNonZeroRowAndColumnIterator_3_InRangeOf_0_to_3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });
        ByteVectorIterator rowIt = a.nonZeroRowIterator(1, 0, 3);
        assertFalse("row", rowIt.hasNext());

        ByteVectorIterator colIt = a.nonZeroColumnIterator(1, 0, 3);
        assertFalse("column", colIt.hasNext());
    }

    @Test
    public void testNonZeroRowAndColumnIterator_3_InRangeOf_1_to_1() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });
        ByteVectorIterator rowIt = a.nonZeroRowIterator(1, 1, 1);
        assertFalse("row", rowIt.hasNext());

        ByteVectorIterator colIt = a.nonZeroColumnIterator(1, 1, 1);
        assertFalse("column", colIt.hasNext());
    }

    @Test
    public void testNonZeroRowAndColumnIterator_3_InRangeOf_1_to_3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });
        ByteVectorIterator rowIt = a.nonZeroRowIterator(1, 1, 3);
        assertFalse("row", rowIt.hasNext());

        ByteVectorIterator colIt = a.nonZeroColumnIterator(1, 1, 3);
        assertFalse("column", colIt.hasNext());
    }

    @Test
    public void testNonZeroRowAndColumnIterator_3_InRangeOf_3_to_3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 0},
                                                            {0, 0, 0},
                                                            {0, 0, 0}
        });
        ByteVectorIterator rowIt = a.nonZeroRowIterator(1, 3, 3);
        assertFalse("row", rowIt.hasNext());

        ByteVectorIterator colIt = a.nonZeroColumnIterator(1, 3, 3);
        assertFalse("column", colIt.hasNext());
    }

    @Test
    public void testNonZeroRowIteratorModify_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 0, 0, 4, 0, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)4);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIteratorModify_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIteratorModify_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 0, 0, 4, 0, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)4);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIteratorModify_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 1, 0, 4, 0, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1, 3, 6);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)4);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_1() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 4, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)4);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_1_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 2, -1, -1, -1, -1},
                                                            {-1, 3, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_1_InRangeOf_0_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 0, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 4, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 6);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)4);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_1_InRangeOf_3_to_6() {

        ByteMatrix a = iteratorMatrix_1();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {0, 1, 0, 2, 3, 0},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 4, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1},
                                                            {-1, 0, -1, -1, -1, -1}
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1, 3, 6);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)4);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIteratorModify_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {0, 0, 0},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(0, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIteratorModify_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {0, 2, 3},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIteratorModify_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {0, 0, 0},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(0, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroRowIteratorModify_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {1, 0, 0},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.nonZeroRowIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.nonZerosInRow(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_2() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1},
                                                            {1, 0, 3},
                                                            {-1, 0, -1},
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(0, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_2_InRangeOf_0_to_1() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1},
                                                            {1, 2, 3},
                                                            {-1, 3, -1},
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_2_InRangeOf_0_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 0, -1},
                                                            {1, 0, 3},
                                                            {-1, 0, -1},
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1, 0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(0, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroColumnIteratorModify_2_InRangeOf_1_to_3() {

        ByteMatrix a = iteratorMatrix_2();
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {-1, 1, -1},
                                                            {1, 0, 3},
                                                            {-1, 0, -1},
        });

        ByteVectorIterator it = a.nonZeroColumnIterator(1, 1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.nonZerosInColumn(1));
        assertEquals(a, b);
    }

    @Test
    public void testAddRowsInPlace() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 3, 0, 1, 3, 1},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });

        a.addRowsInPlace(1, 0);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceInRangeOf_0_to_0() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });

        a.addRowsInPlace(1, 0, 0, 0);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceInRangeOf_0_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 3, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });

        a.addRowsInPlace(1, 0, 0, 4);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceInRangeOf_0_to_7() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 0, 3, 0, 1, 3, 1},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });

        a.addRowsInPlace(1, 0, 0, 7);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceInRangeOf_4_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });

        a.addRowsInPlace(1, 0, 4, 4);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceInRangeOf_4_to_7() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 1, 3, 1},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });

        a.addRowsInPlace(1, 0, 4, 7);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceInRangeOf_7_to_7() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });

        a.addRowsInPlace(1, 0, 7, 7);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceWithMultiplier() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {6, 0, 5, 0, 7, 3, 7},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        byte multiplier = 7;

        a.addRowsInPlace(multiplier, 1, 0);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceWithMultiplierInRangeOf_0_to_0() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        byte multiplier = 7;

        a.addRowsInPlace(multiplier, 1, 0, 0, 0);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceWithMultiplierInRangeOf_0_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {6, 0, 5, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        byte multiplier = 7;

        a.addRowsInPlace(multiplier, 1, 0, 0, 4);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceWithMultiplierInRangeOf_0_to_7() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {6, 0, 5, 0, 7, 3, 7},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        byte multiplier = 7;

        a.addRowsInPlace(multiplier, 1, 0, 0, 7);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceWithMultiplierInRangeOf_4_to_4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        byte multiplier = 7;

        a.addRowsInPlace(multiplier, 1, 0, 4, 4);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceWithMultiplierInRangeOf_4_to_7() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 7, 3, 7},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        byte multiplier = 7;

        a.addRowsInPlace(multiplier, 1, 0, 4, 7);
        assertEquals(b, a);
    }

    @Test
    public void testAddRowsInPlaceWithMultiplierInRangeOf_7_to_7() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {1, 0, 2, 0, 0, 3, 0},
                                                            {1, 0, 1, 0, 1, 0, 1}
        });
        byte multiplier = 7;

        a.addRowsInPlace(multiplier, 1, 0, 7, 7);
        assertEquals(b, a);
    }

    @Test
    public void testSerialization() throws DeserializationException {

        ByteMatrix initial = factory().createMatrix(new byte[][] {
                                                                  {1, 0, 3},
                                                                  {0, 2, 3},
                                                                  {1, 2, 0}
        });

        ByteMatrix a = initial.copy();

        ByteMatrix c = ByteMatrices.deserializeMatrix(a.serializeToBuffer());

        assertEquals(initial, a); // make sure serialization does not modify the matrix
        assertEquals(a, c);
    }
}
