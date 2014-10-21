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


import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorAccumulator;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorPredicate;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorProcedure;

import org.junit.Test;


public abstract class AbstractByteVectorTest {

    public abstract Factory factory();

    @Test
    public void testAccess_4() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3, 0, 0});

        assertEquals(5, a.length());

        a.set(0, aPlusB(a.get(2), (byte)10));
        assertEquals(9, a.get(0));

        assertEquals(0, a.get(1));
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
    public void testAddInPlace_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3});
        ByteVector b = factory().createVector(new byte[] {7, 7, 4});

        a.addInPlace((byte)7);
        assertEquals(b, a);
    }

    @Test
    public void testAddInPlace() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {0, 0, 3, 0, 1, 3, 1});

        a.addInPlace(b);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceInRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});

        a.addInPlace(b, 0, 0);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceInRangeOf_0_to_4() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {0, 0, 3, 0, 0, 3, 0});

        a.addInPlace(b, 0, 4);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceInRangeOf_0_to_7() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {0, 0, 3, 0, 1, 3, 1});

        a.addInPlace(b, 0, 7);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceInRangeOf_4_to_4() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});

        a.addInPlace(b, 4, 4);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceInRangeOf_4_to_7() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {1, 0, 2, 0, 1, 3, 1});

        a.addInPlace(b, 4, 7);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceInRangeOf_7_to_7() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {1, 0, 2, 0, 0, 3, 0});

        a.addInPlace(b, 7, 7);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceWithMultiplier() {

        ByteVector a = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {0, 0, 5, 0, 7, 3, 7});
        byte multiplier = 7;

        a.addInPlace(multiplier, b);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceWithMultiplierInRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        byte multiplier = 7;

        a.addInPlace(multiplier, b, 0, 0);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceWithMultiplierInRangeOf_0_to_4() {

        ByteVector a = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {0, 0, 5, 0, 0, 3, 0});
        byte multiplier = 7;

        a.addInPlace(multiplier, b, 0, 4);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceWithMultiplierInRangeOf_0_to_7() {

        ByteVector a = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {0, 0, 5, 0, 7, 3, 7});
        byte multiplier = 7;

        a.addInPlace(multiplier, b, 0, 7);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceWithMultiplierInRangeOf_4_to_4() {

        ByteVector a = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        byte multiplier = 7;

        a.addInPlace(multiplier, b, 4, 4);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceWithMultiplierInRangeOf_4_to_7() {

        ByteVector a = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {7, 0, 2, 0, 7, 3, 7});
        byte multiplier = 7;

        a.addInPlace(multiplier, b, 4, 7);
        assertEquals(c, a);
    }

    @Test
    public void testAddInPlaceWithMultiplierInRangeOf_7_to_7() {

        ByteVector a = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 1, 0, 1, 0, 1});
        ByteVector c = factory().createVector(new byte[] {7, 0, 2, 0, 0, 3, 0});
        byte multiplier = 7;

        a.addInPlace(multiplier, b, 7, 7);
        assertEquals(c, a);
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
    public void testSubtractInPlace_3() {

        ByteVector a1 = factory().createVector(new byte[] {0, 0, 3});
        ByteVector a2 = factory().createVector(new byte[] {0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {4, 0, 0});

        ByteVector c = factory().createVector(new byte[] {7, 7, 4});

        ByteVector d = factory().createVector(new byte[] {4, 0, 3});

        a1.subtractInPlace((byte)7);
        assertEquals(c, a1);

        a2.subtractInPlace(b);
        assertEquals(d, a2);
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
    public void testMultiplyInPlace_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 1});

        ByteVector b = factory().createVector(new byte[] {0, 0, 10});

        a.multiplyInPlace((byte)10);
        assertEquals(b, a);
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
    public void testMultiply_5_0x0_InRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 1});
        ByteMatrix b = factory().createMatrix(new byte[][] {});
        ByteVector c = factory().createVector(new byte[] {});

        assertEquals(c, a.multiply(b, 0, 0));
    }

    @Test
    public void testMultiply_5_3x1_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 1});
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0},
                                                            {3},
                                                            {0}
        });
        ByteVector c = factory().createVector(new byte[] {3});

        assertEquals(c, a.multiply(b, 0, 3));
    }

    @Test
    public void testMultiply_5_5x1_InRangeOf_0_to_5() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 1});
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0},
                                                            {3},
                                                            {0},
                                                            {3},
                                                            {0}
        });
        ByteVector c = factory().createVector(new byte[] {5});

        assertEquals(c, a.multiply(b, 0, 5));
    }

    @Test
    public void testMultiply_5_0x0_InRangeOf_3_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 1});
        ByteMatrix b = factory().createMatrix(new byte[][] {});
        ByteVector c = factory().createVector(new byte[] {});

        assertEquals(c, a.multiply(b, 3, 3));
    }

    @Test
    public void testMultiply_5_2x1_InRangeOf_3_to_5() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 1});
        ByteMatrix b = factory().createMatrix(new byte[][] {
                                                            {0},
                                                            {3}
        });
        ByteVector c = factory().createVector(new byte[] {3});

        assertEquals(c, a.multiply(b, 3, 5));
    }

    @Test
    public void testMultiply_5_0x0_InRangeOf_5_to_5() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 1});
        ByteMatrix b = factory().createMatrix(new byte[][] {});
        ByteVector c = factory().createVector(new byte[] {});

        assertEquals(c, a.multiply(b, 5, 5));
    }

    @Test
    public void testProduct_3() {

        ByteVector a = factory().createVector(new byte[] {2, 4, 6});

        assertEquals(a.product(), 48);
    }

    @Test
    public void testSum_3() {

        ByteVector a = factory().createVector(new byte[] {2, 4, 6});

        assertEquals(a.sum(), 0);
    }

    @Test
    public void testDivide_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {0, 0, 122});

        assertEquals(b, a.divide((byte)10));
    }

    @Test
    public void testDivideInPlace_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {0, 0, 122});

        a.divideInPlace((byte)10);
        assertEquals(b, a);
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
                if (array1[ii] != array2[ii]) {
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
        assertEquals(-1, a.max());
    }

    @Test
    public void testMin() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 0, -1, 0, 0, 0, 0, -5, 0, 0, 5});
        assertEquals(0, a.min());
    }

    @Test
    public void testFold_6() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 5, 0, 2, 1});

        VectorAccumulator sum = ByteVectors.asSumAccumulator((byte)0);
        VectorAccumulator product = ByteVectors.asProductAccumulator((byte)1);

        assertEquals(a.fold(sum), 6);
        // check whether the accumulator were flushed
        assertEquals(a.fold(sum), 6);

        assertEquals(a.fold(product), 0);
        // check whether the accumulator were flushed
        assertEquals(a.fold(product), 0);
    }

    @Test
    public void testIssue162_0() {

        VectorPredicate three = new VectorPredicate() {

            @Override
            public boolean test(@SuppressWarnings("unused") int i, byte value) {

                return value == 3;
            }
        };

        ByteVector a = factory().createVector();
        ByteVector b = a.resize(31);

        assertEquals(0, a.length());
        assertEquals(31, b.length());

        b.assign((byte)3);
        assertTrue(b.is(three));

        ByteVector c = b.resize(42);
        c.assign((byte)3);
        assertTrue(c.is(three));

        ByteVector d = c.resize(54);
        d.assign((byte)3);
        assertTrue(d.is(three));
    }

    @Test
    public void testResize_32_to_110_to_1076_to_31() {

        VectorPredicate fortyTwo = new VectorPredicate() {

            @Override
            public boolean test(@SuppressWarnings("unused") int i, byte value) {

                return value == 42;
            }
        };

        ByteVector a = factory().createVector();
        ByteVector b = a.resize(32);

        assertEquals(32, b.length());

        b.assign((byte)42);
        assertTrue(b.is(fortyTwo));

        ByteVector c = b.resize(110);
        c.assign((byte)42);
        assertTrue(c.is(fortyTwo));

        // ByteVector d = c.resize(1076);
        // d.assign((byte)42);
        // assertTrue(d.is(fortyTwo));
        //
        // ByteVector e = d.resize(31);
        // e.assign((byte)42);
        // assertTrue(e.is(fortyTwo));
    }

    @Test
    public void testNonZeros() {

        ByteVector a = factory().createVector(new byte[] {0, 1, -1, 0, 0});
        assertEquals(2, a.nonZeros());
    }

    @Test
    public void testNonZerosInRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {0, 1, -1, 0, 0});
        assertEquals(0, a.nonZeros(0, 0));
    }

    @Test
    public void testNonZerosInRangeOf_0_to_2() {

        ByteVector a = factory().createVector(new byte[] {0, 1, -1, 0, 0});
        assertEquals(1, a.nonZeros(0, 2));
    }

    @Test
    public void testNonZerosInRangeOf_0_to_5() {

        ByteVector a = factory().createVector(new byte[] {0, 1, -1, 0, 0});
        assertEquals(2, a.nonZeros(0, 5));
    }

    @Test
    public void testNonZerosInRangeOf_2_to_2() {

        ByteVector a = factory().createVector(new byte[] {0, 1, -1, 0, 0});
        assertEquals(0, a.nonZeros(2, 2));
    }

    @Test
    public void testNonZerosInRangeOf_2_to_5() {

        ByteVector a = factory().createVector(new byte[] {0, 1, -1, 0, 0});
        assertEquals(1, a.nonZeros(2, 5));
    }

    @Test
    public void testNonZerosInRangeOf_5_to_5() {

        ByteVector a = factory().createVector(new byte[] {0, 1, -1, 0, 0});
        assertEquals(0, a.nonZeros(5, 5));
    }

    @Test
    public void testNonZerosWithAllZeros() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0, 0, 0});
        assertEquals(0, a.nonZeros());
    }

    @Test
    public void testNonZerosWithNoZeros() {

        ByteVector a = factory().createVector(new byte[] {1, -1, 2, -2, 3});
        assertEquals(5, a.nonZeros());
    }


    private static final class SetterProcedure implements VectorProcedure {

        private final ByteVector vector;


        SetterProcedure(ByteVector vector) {

            this.vector = vector;
        }

        @Override
        public void apply(int i, byte value) {

            vector.set(i, value);
        }
    }


    @Test
    public void testEach() {

        final ByteVector initial = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        final ByteVector a = initial.copy();
        final ByteVector b = a.blank();

        a.each(new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(a, b);
    }

    @Test
    public void testEachInRangeOf_0_to_0() {

        final ByteVector initial = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        final ByteVector a = initial.copy();
        final ByteVector b = a.blank();
        final ByteVector c = factory().createVector(new byte[] {0, 0, 0, 0, 0});

        a.each(new SetterProcedure(b), 0, 0);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachInRangeOf_0_to_2() {

        final ByteVector initial = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        final ByteVector a = initial.copy();
        final ByteVector b = a.blank();
        final ByteVector c = factory().createVector(new byte[] {0, 1, 0, 0, 0});

        a.each(new SetterProcedure(b), 0, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachInRangeOf_0_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        final ByteVector a = initial.copy();
        final ByteVector b = a.blank();
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        a.each(new SetterProcedure(b), 0, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachInRangeOf_2_to_2() {

        final ByteVector initial = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        final ByteVector a = initial.copy();
        final ByteVector b = a.blank();
        final ByteVector c = factory().createVector(new byte[] {0, 0, 0, 0, 0});

        a.each(new SetterProcedure(b), 2, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachInRangeOf_2_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        final ByteVector a = initial.copy();
        final ByteVector b = a.blank();
        final ByteVector c = factory().createVector(new byte[] {0, 0, 2, 3, 4});

        a.each(new SetterProcedure(b), 2, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachInRangeOf_5_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        final ByteVector a = initial.copy();
        final ByteVector b = a.blank();
        final ByteVector c = factory().createVector(new byte[] {0, 0, 0, 0, 0});

        a.each(new SetterProcedure(b), 5, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }


    private static final class IndexModulus2Function implements VectorFunction {

        @Override
        public byte evaluate(int i, @SuppressWarnings("unused") byte value) {

            return (byte)(i % 2);
        }
    }


    @Test
    public void testUpdate() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 1, 0, 1, 0});

        a.update(new IndexModulus2Function());

        assertEquals(b, a);
    }

    @Test
    public void testUpdateAt_2() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});

        a.update(2, new IndexModulus2Function());

        assertEquals(0, a.get(2));
    }

    @Test
    public void testUpdateInRangeOf_0_to_0() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {7, 7, 7, 7, 7});

        a.update(new IndexModulus2Function(), 0, 0);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateInRangeOf_0_to_2() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 1, 7, 7, 7});

        a.update(new IndexModulus2Function(), 0, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateInRangeOf_0_to_5() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 1, 0, 1, 0});

        a.update(new IndexModulus2Function(), 0, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateInRangeOf_2_to_2() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {7, 7, 7, 7, 7});

        a.update(new IndexModulus2Function(), 2, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateInRangeOf_2_to_5() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {7, 7, 0, 1, 0});

        a.update(new IndexModulus2Function(), 2, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateInRangeOf_5_to_5() {

        final ByteVector a = factory().createVector(new byte[] {7, 7, 7, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {7, 7, 7, 7, 7});

        a.update(new IndexModulus2Function(), 5, 5);

        assertEquals(b, a);
    }

    @Test
    public void testFoldNonZero_5() {

        ByteVector a = factory().createVector(
            new byte[] {2, 0, 5, 0, 2}
            );

        VectorAccumulator sum = ByteVectors.asSumAccumulator((byte)0);
        VectorAccumulator product = ByteVectors.asProductAccumulator((byte)1);

        assertEquals(a.foldNonZero(sum), 5);
        // check whether the accumulator were flushed
        assertEquals(a.foldNonZero(sum), 5);

        assertEquals(a.foldNonZero(product), 20);
        // check whether the accumulator were flushed
        assertEquals(a.foldNonZero(product), 20);
    }

    @Test
    public void testIsZeroAt_4() {

        ByteVector a = factory().createVector(
            new byte[] {1, 0, 0, 4}
            );

        assertTrue(a.isZeroAt(1));
        assertFalse(a.isZeroAt(3));
    }

    @Test
    public void testNonZeroAt_6() {

        ByteVector a = factory().createVector(
            new byte[] {0, 5, 2, 0, 0, 0}
            );

        assertTrue(a.nonZeroAt(1));
        assertFalse(a.nonZeroAt(3));
    }

    @Test
    public void testEachNonZero() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector a = initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {7, 1, 2, 7, 7});

        a.eachNonZero(new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_0_to_0() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector a = initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        a.eachNonZero(new SetterProcedure(b), 0, 0);

        assertEquals("initial == a?", initial, a); // check if each wrongly modifies the caller vector
        assertEquals("c == b?", c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_0_to_2() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector a = initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {7, 1, 2, 3, 4});

        a.eachNonZero(new SetterProcedure(b), 0, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_0_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector a = initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {7, 1, 2, 7, 7});

        a.eachNonZero(new SetterProcedure(b), 0, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_2_to_2() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector a = initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        a.eachNonZero(new SetterProcedure(b), 2, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_2_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector a = initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 7, 7});

        a.eachNonZero(new SetterProcedure(b), 2, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_5_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector a = initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        a.eachNonZero(new SetterProcedure(b), 5, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testUpdateNonZero() {

        final ByteVector a = factory().createVector(new byte[] {0, 7, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 1, 0, 1, 0});

        a.updateNonZero(new IndexModulus2Function());

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_0_to_0() {

        final ByteVector a = factory().createVector(new byte[] {0, 7, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 7, 0, 7, 7});

        a.updateNonZero(new IndexModulus2Function(), 0, 0);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_0_to_2() {

        final ByteVector a = factory().createVector(new byte[] {0, 7, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 1, 0, 7, 7});

        a.updateNonZero(new IndexModulus2Function(), 0, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_0_to_5() {

        final ByteVector a = factory().createVector(new byte[] {0, 7, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 1, 0, 1, 0});

        a.updateNonZero(new IndexModulus2Function(), 0, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_2_to_2() {

        final ByteVector a = factory().createVector(new byte[] {0, 7, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 7, 0, 7, 7});

        a.updateNonZero(new IndexModulus2Function(), 2, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_2_to_5() {

        final ByteVector a = factory().createVector(new byte[] {0, 7, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 7, 0, 1, 0});

        a.updateNonZero(new IndexModulus2Function(), 2, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_5_to_5() {

        final ByteVector a = factory().createVector(new byte[] {0, 7, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 7, 0, 7, 7});

        a.updateNonZero(new IndexModulus2Function(), 5, 5);

        assertEquals(b, a);
    }

    @Test
    public void testIterator_1() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator();

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
    public void testIterator_1_InRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator(0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_1_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator(0, 3);

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
    public void testIterator_1_InRangeOf_0_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator(0, 6);

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
    public void testIterator_1_InRangeOf_3_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator(3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_1_InRangeOf_3_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator(3, 6);

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
    public void testIterator_1_InRangeOf_6_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator(6, 6);

        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_2() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.iterator();

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
    public void testIterator_2_InRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.iterator(0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_2_InRangeOf_0_to_1() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.iterator(0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_2_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.iterator(0, 3);

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
    public void testIterator_2_InRangeOf_1_to_1() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.iterator(1, 1);

        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_2_InRangeOf_1_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.iterator(1, 3);

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
    public void testIterator_2_InRangeOf_3_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.iterator(3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_3() {

        ByteVector a = factory().createVector(new byte[] {});
        ByteVectorIterator it = a.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator_3_InRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {});
        ByteVectorIterator it = a.iterator(0, 0);
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
    public void testIteratorModify_1() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 0, 1, 0, 1});
        ByteVectorIterator it = a.iterator();

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
    public void testIteratorModify_1_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 0, 2, 3, 0});
        ByteVectorIterator it = a.iterator(0, 3);

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
    public void testIteratorModify_1_InRangeOf_0_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {1, 0, 0, 1, 0, 1});
        ByteVectorIterator it = a.iterator(0, 6);

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
    public void testIteratorModify_1_InRangeOf_3_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {0, 1, 0, 1, 0, 1});
        ByteVectorIterator it = a.iterator(3, 6);

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
    public void testIteratorModify_2() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 5, 0});
        ByteVectorIterator it = a.iterator();

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
    public void testIteratorModify_2_InRangeOf_0_to_1() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 2, 3});
        ByteVectorIterator it = a.iterator(0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy set 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(a, b);
    }

    @Test
    public void testIteratorModify_2_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 5, 0});
        ByteVectorIterator it = a.iterator(0, 3);

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
    public void testIteratorModify_2_InRangeOf_1_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {1, 5, 0});
        ByteVectorIterator it = a.iterator(1, 3);

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
    public void testNonZeroIterator_1() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator();

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
    public void testNonZeroIterator_1_InRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_1_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_1_InRangeOf_0_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 6);

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
    public void testNonZeroIterator_1_InRangeOf_3_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_1_InRangeOf_3_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(3, 6);

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
    public void testNonZeroIterator_1_InRangeOf_6_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(6, 6);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_2() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator();

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
    public void testNonZeroIterator_2_InRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_2_InRangeOf_0_to_1() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_2_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);

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
    public void testNonZeroIterator_2_InRangeOf_1_to_1() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(1, 1);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_2_InRangeOf_1_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(1, 3);

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
    public void testNonZeroIterator_2_InRangeOf_3_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_0_to_0() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 0);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_0_to_1() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 1);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_1_to_1() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(1, 1);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_1_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(1, 3);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_3_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(3, 3);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIteratorModify_1() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0, 4, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator();

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

        assertEquals(1, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_1_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_1_InRangeOf_0_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0, 4, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 6);

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

        assertEquals(1, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_1_InRangeOf_3_to_6() {

        ByteVector a = factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {0, 1, 0, 4, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(3, 6);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, NONZERO_DUMMY);
        iteratorSet("set 1", it, (byte)4);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator();

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

        assertEquals(0, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2_InRangeOf_0_to_1() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2_InRangeOf_0_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);

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

        assertEquals(0, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2_InRangeOf_1_to_3() {

        ByteVector a = factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {1, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.nonZeros());
        assertEquals(a, b);
    }

    @Test
    public void testSerialization() throws DeserializationException {

        ByteVector initial = factory().createVector(new byte[] {0, 2, 3});

        ByteVector a = initial.copy();

        ByteVector c = ByteVectors.deserializeVector(a.serializeToBuffer());

        assertEquals(initial, a); // make sure serialization does not modify the vector
        assertEquals(a, c);
    }
}
