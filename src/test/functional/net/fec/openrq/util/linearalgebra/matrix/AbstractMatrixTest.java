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
 * Contributor(s): Evgenia Krivova
 * Jakob Moellers
 * Maxim Samoylov
 * Anveshi Charuvaka
 * Todd Brunhoff
 * Catherine da Graca
 */
package net.fec.openrq.util.linearalgebra.matrix;


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;

import org.junit.Test;


public abstract class AbstractMatrixTest {

    public abstract Factory factory();

    @Test
    public void testAccess_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 3},
                                                            {0, 5, 0},
                                                            {7, 0, 9}
        });

        a.set(0, 1, aTimesB(a.get(1, 1), (byte)2));
        assertTrue(aIsEqualToB(a.get(0, 1), (byte)10));
    }

    @Test
    public void testGetColumn_4x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteVector b = factory().createVector(new byte[] {8, 4, 9, 5});
        ByteVector c = factory().createVector(new byte[] {1, 6, 1, 3});

        assertEquals(b, a.getColumn(0));
        assertEquals(c, a.getColumn(2));
    }

    @Test
    public void testGetRow_4x4() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {8, 3, 1, 9},
                                                            {4, 9, 6, 6},
                                                            {9, 1, 1, 4},
                                                            {5, 7, 3, 0}
        });

        ByteVector b = factory().createVector(new byte[] {8, 3, 1, 9});
        ByteVector c = factory().createVector(new byte[] {9, 1, 1, 4});

        assertEquals(b, a.getRow(0));
        assertEquals(c, a.getRow(2));
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
        assertTrue(aIsEqualToB(a.trace(), (byte)13));
    }

    @Test
    public void testDiagonalProduct_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        assertTrue(aIsEqualToB(a.diagonalProduct(), (byte)45));
    }

    @Test
    public void testProduct_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 1, 1},
                                                            {1, 5, 1},
                                                            {1, 1, 9}
        });

        assertTrue(aIsEqualToB(a.product(), (byte)45));
    }

    @Test
    public void testSum_3x3() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {1, 0, 0},
                                                            {0, 5, 0},
                                                            {0, 0, 9}
        });

        assertTrue(aIsEqualToB(a.sum(), (byte)13));
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
            if (!aIsEqualToB(array1[ii], array2[ii])) {
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
    public void testSerialization() throws IOException, ClassNotFoundException {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 3},
                                                            {0, 0, 6},
                                                            {0, 0, 9}
        });

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(a);
        out.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInput in = new ObjectInputStream(bis);
        ByteMatrix b = (ByteMatrix)in.readObject();
        in.close();

        assertEquals(a, b);
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

        assertTrue(aIsEqualToB(a.max(), (byte)-1));
    }

    @Test
    public void testMinCompressed() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1},
                                                            {0, 3, 0},
                                                            {0, 7, 2}
        });

        assertTrue(aIsEqualToB(a.min(), (byte)0));
    }

    @Test
    public void testMaxInRow() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {-2, 0, 0, -1}
        });

        assertTrue(aIsEqualToB(a.maxInRow(2), (byte)-1));
    }

    @Test
    public void testMinInRow() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {2, 0, 0, 1}
        });

        assertTrue(aIsEqualToB(a.minInRow(2), (byte)0));
    }

    @Test
    public void testMaxInColumn() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {-2, 0, 0, -1}
        });

        assertTrue(aIsEqualToB(a.maxInColumn(0), (byte)-2));
    }

    @Test
    public void testMinInColumn() {

        ByteMatrix a = factory().createMatrix(new byte[][] {
                                                            {0, 0, 1, 0},
                                                            {-3, 2, 0, 1},
                                                            {-2, 0, 0, -1}
        });

        assertTrue(aIsEqualToB(a.minInColumn(3), (byte)0));
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
            assertTrue(aIsEqualToB(sum, columnSums.get(col)));
        }

        ByteVector s = d.foldColumns(ByteMatrices.asSumAccumulator((byte)0));
        assertEquals(s, columnSums);

        ByteVector rowSums = factory().createVector(new byte[] {4, 1, 9, 7, 6, 9});

        for (int row = 0; row < d.columns(); row++) {
            byte sum = d.foldRow(row, ByteMatrices.asSumAccumulator((byte)0));
            assertTrue(aIsEqualToB(sum, rowSums.get(row)));
        }

        s = d.foldRows(ByteMatrices.asSumAccumulator((byte)0));
        assertEquals(s, rowSums);
    }

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
}
