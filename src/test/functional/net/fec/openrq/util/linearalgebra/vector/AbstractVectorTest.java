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
 * Contributor(s): Daniel Renshaw
 * Jakob Moellers
 * Yuriy Drozd
 */
package net.fec.openrq.util.linearalgebra.vector;


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import static net.fec.openrq.util.arithmetic.OctetOps.aPlusB;
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
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;

import org.junit.Test;


public abstract class AbstractVectorTest {

    public abstract Factory factory();

    @Test
    public void testAccess_4() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3, 0, 0});

        assertEquals(5, a.length());

        a.set(0, aPlusB(a.get(2), (byte)10));
        assertTrue(aIsEqualToB((byte)9, a.get(0)));

        assertTrue(aIsEqualToB((byte)0, a.get(1)));
    }

    @Test
    public void testAssign_4() {

        ByteVector a = factory().createVector(4);
        ByteVector b = factory().createVector(new byte[] {10, 10, 10, 10});

        a.assign((byte)10);

        assertEquals(b, a);
    }

    @Test
    public void testResize_3_to_5_to_2() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 1});

        ByteVector b = factory().createVector(new byte[] {0, 0, 1, 0, 0});

        ByteVector c = factory().createVector(new byte[] {0, 0});

        a = a.resize(5);
        assertEquals(b, a);

        a = a.resize(2);
        assertEquals(c, a);
    }

    @Test
    public void testResize_5_to_0_to_4() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 2, 3, 0});

        ByteVector b = factory().createVector(new byte[0]);

        ByteVector c = factory().createVector(new byte[] {0, 0, 0, 0});

        a = a.resize(0);
        assertEquals(b, a);

        a = a.resize(4);
        assertEquals(c, a);
    }

    @Test
    public void testSlice_5_to_2_and_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3, 4, 5});

        ByteVector b = factory().createVector(new byte[] {2, 3});

        ByteVector c = factory().createVector(new byte[] {3, 4, 5});

        assertEquals(b, a.slice(1, 3));
        assertEquals(c, a.slice(2, 5));
    }

    @Test
    public void testSliceLeftRight_5_to_1_and_4() {

        ByteVector a = factory().createVector(new byte[] {0, 2, 0, 4, 0});

        ByteVector b = factory().createVector(new byte[] {0});

        ByteVector c = factory().createVector(new byte[] {2, 0, 4, 0});

        assertEquals(b, a.sliceLeft(1));
        assertEquals(c, a.sliceRight(1));
    }

    @Test
    public void testSelect_4() {

        ByteVector a = factory().createVector(new byte[] {0, 3, 7, 0});

        ByteVector b = factory().createVector(new byte[] {3, 0, 0, 7});

        ByteVector c = factory().createVector(new byte[] {7, 7, 0, 0});

        assertEquals(b, a.select(new int[] {1, 0, 3, 2}));
        assertEquals(c, a.select(new int[] {2, 2, 0, 3}));
    }

    @Test
    public void testSelect_5() {

        ByteVector a = factory().createVector(new byte[] {1, 6, 0, 0, 8});

        ByteVector b = factory().createVector(new byte[] {1, 1, 1});

        ByteVector c = factory().createVector(new byte[] {0, 0, 8, 8, 1, 0});

        assertEquals(b, a.select(new int[] {0, 0, 0}));
        assertEquals(c, a.select(new int[] {2, 3, 4, 4, 0, 3}));
    }

    @Test
    public void testSelect_3() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 0});

        ByteVector b = factory().createVector(new byte[] {0, 0, 0, 0});

        ByteVector c = factory().createVector(new byte[] {1});

        assertEquals(b, a.select(new int[] {1, 2, 2, 1}));
        assertEquals(c, a.select(new int[] {0}));
    }

    @Test
    public void testSwap_5() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {3, 0, 0, 0, 1});

        a.swap(0, 4);
        assertEquals(b, a);
    }

    @Test
    public void testSwap_4() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 0});

        ByteVector b = factory().createVector(new byte[] {0, 0, 1, 0});

        a.swap(1, 2);
        assertEquals(b, a);
    }

    @Test
    public void testSwap_4_2() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2});

        ByteVector b = factory().createVector(new byte[] {0, 0, 1, 2});

        a.swap(1, 2);
        assertEquals(a, b);
    }

    @Test
    public void testSwap_6() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0, 0, 0, -5});

        ByteVector b = factory().createVector(new byte[] {0, 0, 0, -5, 0, 0});

        a.swap(3, 5);
        assertEquals(a, b);
    }

    @Test
    public void testSwap_2() {

        ByteVector a = factory().createVector(new byte[] {1, 2});

        ByteVector b = factory().createVector(new byte[] {2, 1});

        a.swap(0, 1);
        assertEquals(b, a);
    }

    @Test
    public void testAdd_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {0, 5, 0});

        ByteVector c = factory().createVector(new byte[] {7, 7, 4});

        ByteVector d = factory().createVector(new byte[] {0, 5, 3});

        assertEquals(c, a.add((byte)7));
        assertEquals(d, a.add(b));
    }

    @Test
    public void testSubtract_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {4, 0, 0});

        ByteVector c = factory().createVector(new byte[] {7, 7, 4});

        ByteVector d = factory().createVector(new byte[] {4, 0, 3});

        assertEquals(c, a.subtract((byte)7));
        assertEquals(d, a.subtract(b));
    }

    @Test
    public void testMultiply_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 1});

        ByteVector b = factory().createVector(new byte[] {0, 5, 0});

        ByteVector c = factory().createVector(new byte[] {0, 0, 10});

        ByteVector d = factory().createVector(new byte[] {0, 0, 0});

        assertEquals(c, a.multiply((byte)10));
        assertEquals(d, a.hadamardProduct(b));
    }

    @Test
    public void testHadamardProduct_3() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2});

        ByteVector b = factory().createVector(new byte[] {3, 0, 0});

        ByteVector c = factory().createVector(new byte[] {3, 0, 0});

        assertEquals(c, a.hadamardProduct(b));
    }

    @Test
    public void testMultiply_2_2x4() {

        ByteVector a = factory().createVector(new byte[] {1, 2});

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0, 5, 0, 6},
                                                            {1, 0, 8, 0}
        });

        ByteVector c = factory().createVector(new byte[] {2, 5, 16, 6});

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testMultiply_3_3x1() {

        ByteVector a = factory().createVector(new byte[] {0, 2, 0});

        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0},
                                                            {3},
                                                            {0},
        });

        ByteVector c = factory().createVector(new byte[] {6});

        assertEquals(c, a.multiply(b));
    }

    @Test
    public void testProduct_3() {

        ByteVector a = factory().createVector(new byte[] {2, 4, 6});

        assertTrue(aIsEqualToB(a.product(), (byte)48));
    }

    @Test
    public void testSum_3() {

        ByteVector a = factory().createVector(new byte[] {2, 4, 6});

        assertTrue(aIsEqualToB(a.sum(), (byte)0));
    }

    @Test
    public void testDivide_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {0, 0, 122});

        assertEquals(b, a.divide((byte)10));
    }

    @Test
    public void testCopy_5() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0, 0, 1});

        assertEquals(a, a.copy());
    }

    @Test
    public void testBlank_5() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0, 0, 1});

        ByteVector b = factory().createVector(new byte[] {0, 0, 0, 0, 0});

        assertEquals(b, a.blank());
    }

    @Test
    public void testSerialization() throws IOException,
        ClassNotFoundException {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0, 0, 5});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(a);
        out.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInput in = new ObjectInputStream(bis);
        ByteVector b = (ByteVector)in.readObject();
        in.close();

        assertEquals(a, b);
    }

    /**
     * Tests whether two vectors contain the same elements
     * 
     * @param vector1
     *            Vector1
     * @param vector2
     *            Vector2
     * @return True if both vectors contain the same elements
     */
    private boolean testWhetherVectorsContainSameElements(ByteVector vector1, ByteVector vector2) {

        final int length = vector1.length();
        if (length == vector2.length()) {
            final byte[] array1 = new byte[length];
            final byte[] array2 = new byte[length];

            for (int ii = 0; ii < length; ii++) {
                array1[ii] = vector1.get(ii);
                array2[ii] = vector2.get(ii);
            }

            Arrays.sort(array1);
            Arrays.sort(array2);

            for (int ii = 0; ii < length; ii++) {
                if (!aIsEqualToB(array1[ii], array2[ii])) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Test
    public void testTestWhetherVectorsContainSameElements() {

        ByteVector a = factory().createVector(new byte[] {1, 1, 3, 4});
        ByteVector b = factory().createVector(new byte[] {4, 1, 1, 3});
        assertTrue(testWhetherVectorsContainSameElements(a, b));

        ByteVector c = factory().createVector(new byte[] {4, 2, 1, 3});
        assertFalse(testWhetherVectorsContainSameElements(a, c));
    }

    @Test
    public void testShuffle() {

        ByteVector a = factory().createVector(new byte[] {1, 1, 3, 4});
        ByteVector b = a.shuffle();

        assertTrue(testWhetherVectorsContainSameElements(a, b));
    }

    @Test
    public void testMax() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 0, -1, 0, 0, 0, 0, -5, 0, 0, 5});
        assertTrue(aIsEqualToB((byte)-1, a.max()));
    }

    @Test
    public void testMin() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 0, -1, 0, 0, 0, 0, -5, 0, 0, 5});
        assertTrue(aIsEqualToB((byte)0, a.min()));
    }
}
