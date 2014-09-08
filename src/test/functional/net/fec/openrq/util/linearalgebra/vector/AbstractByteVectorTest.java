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

        ByteVector a1 = factory().createVector(new byte[] {0, 0, 3});
        ByteVector a2 = factory().createVector(new byte[] {0, 0, 3});

        ByteVector b = factory().createVector(new byte[] {0, 5, 0});

        ByteVector c = factory().createVector(new byte[] {7, 7, 4});

        ByteVector d = factory().createVector(new byte[] {0, 5, 3});

        a1.addInPlace((byte)7);
        assertEquals(c, a1);

        a2.addInPlace(b);
        assertEquals(d, a2);
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
    public void testHadamardProductInPlace_3() {

        ByteVector a = factory().createVector(new byte[] {1, 0, 2});

        ByteVector b = factory().createVector(new byte[] {3, 0, 0});

        ByteVector c = factory().createVector(new byte[] {3, 0, 0});

        a.hadamardProductInPlace(b);
        assertEquals(c, a);
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

        ByteVector d = c.resize(1076);
        d.assign((byte)42);
        assertTrue(d.is(fortyTwo));

        ByteVector e = d.resize(31);
        e.assign((byte)42);
        assertTrue(e.is(fortyTwo));
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

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
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
}
