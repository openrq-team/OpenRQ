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
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
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
package net.fec.openrq.util.linearalgebra.factory;


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import junit.framework.TestCase;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;

import org.junit.Test;


public abstract class AbstractFactoryTest extends TestCase {

    public abstract Factory factory();

    @Test
    public void testCreateMatrix() {

        ByteMatrix a = factory().createMatrix();
        ByteMatrix b = factory().createMatrix(5, 5);
        ByteMatrix c = factory().createRandomMatrix(5, 5);
        ByteMatrix d = factory().createSquareMatrix(5);

        assertEquals(0, a.rows());
        assertEquals(0, a.columns());
        assertEquals(5, b.columns());
        assertEquals(5, b.rows());
        assertEquals(5, c.rows());
        assertEquals(5, c.columns());
        assertEquals(5, d.rows());
        assertEquals(5, d.columns());

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++)
            {
                assertTrue(aIsEqualToB(b.get(i, j), (byte)0));
                assertTrue(aIsEqualToB(d.get(i, j), (byte)0));
            }
        }
    }

    @Test
    public void testCreateMatrixFromArray() {

        byte array[][] = new byte[][] {
                                       {1, 0, 3},
                                       {0, 5, 0},
                                       {7, 0, 9}
        };

        ByteMatrix a = factory().createMatrix(array);

        assertEquals(3, a.rows());
        assertEquals(3, a.columns());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertTrue(aIsEqualToB(array[i][j], a.get(i, j)));
            }
        }
    }

    @Test
    public void testCreateConstantMatrix_3x3() {

        ByteMatrix a = factory().createConstantMatrix(3, 3, (byte)10);

        assertEquals(3, a.rows());
        assertEquals(3, a.columns());

        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.columns(); j++) {
                assertTrue(aIsEqualToB(a.get(i, j), (byte)10));
            }
        }
    }

    @Test
    public void testCreateConstantMatrix_2x5() {

        ByteMatrix a = factory().createConstantMatrix(2, 5, (byte)20);

        assertEquals(2, a.rows());
        assertEquals(5, a.columns());

        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.columns(); j++) {
                assertTrue(aIsEqualToB(a.get(i, j), (byte)20));
            }
        }
    }

    @Test
    public void testCreateConstantMatrix_4x1() {

        ByteMatrix a = factory().createConstantMatrix(4, 1, (byte)30);

        assertEquals(4, a.rows());
        assertEquals(1, a.columns());

        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.columns(); j++) {
                assertTrue(aIsEqualToB(a.get(i, j), (byte)30));
            }
        }
    }

    @Test
    public void testCreateRandomSymmetricMatrix() {

        ByteMatrix a = factory().createRandomSymmetricMatrix(5);

        for (int i = 0; i < a.rows(); i++) {
            for (int j = i; j < a.columns(); j++) {
                assertTrue(aIsEqualToB(a.get(i, j), a.get(j, i)));
            }
        }
    }

    @Test
    public void testCreateIdentityMatrix() {

        ByteMatrix a = factory().createIdentityMatrix(3);

        assertEquals(3, a.rows());
        assertEquals(3, a.columns());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertTrue(aIsEqualToB(a.get(i, j), (byte)1));
                }
                else {
                    assertTrue(aIsEqualToB(a.get(i, j), (byte)0));
                }
            }
        }
    }

    @Test
    public void testVector() {

        ByteVector a = factory().createVector();
        ByteVector b = factory().createVector(5);
        ByteVector c = factory().createRandomVector(5);

        assertEquals(0, a.length());
        assertEquals(5, b.length());
        assertEquals(5, c.length());

        for (int i = 0; i < b.length(); i++) {
            assertTrue(aIsEqualToB(b.get(i), (byte)0));
        }
    }

    @Test
    public void testCreateVectorFromArray() {

        byte array[] = new byte[] {1, 0, 2, 0, 3};
        ByteVector a = factory().createVector(array);

        assertEquals(5, a.length());

        for (int i = 0; i < 5; i++) {
            assertTrue(aIsEqualToB(array[i], a.get(i)));
        }
    }
}
