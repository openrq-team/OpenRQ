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
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra.vector.sparse;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.vector.AbstractByteVectorTest;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;

import org.junit.Test;


public abstract class SparseByteVectorTest extends AbstractByteVectorTest {

    @Test
    public void testCardinality() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {0, 0, 0, 0, 1});

        assertEquals(1, a.cardinality());
    }

    @Test
    public void testNonZeroIterator_1() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
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

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_1_InRangeOf_0_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 1, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_1_InRangeOf_0_to_6() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
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
    public void testNonZeroIterator_1_InRangeOf_3_to_6() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
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

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(6, 6);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_2() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
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

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(0, 0);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_2_InRangeOf_0_to_1() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        assertEquals("get 0", 1, it.get());
        assertEquals("index 0", 0, it.index());

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_2_InRangeOf_0_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
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
    public void testNonZeroIterator_2_InRangeOf_1_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
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

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
        ByteVectorIterator it = a.nonZeroIterator(3, 3);

        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_0_to_0() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 0);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_0_to_1() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 1);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_0_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_1_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(1, 3);
        assertFalse(it.hasNext());
    }

    @Test
    public void testNonZeroIterator_3_InRangeOf_3_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.nonZeroIterator(3, 3);
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
    public void testNonZeroIteratorModify_1() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
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

        assertEquals(1, a.cardinality());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_1_InRangeOf_0_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0, 2, 3, 0});
        ByteVectorIterator it = a.nonZeroIterator(0, 3);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.cardinality());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_1_InRangeOf_0_to_6() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
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

        assertEquals(1, a.cardinality());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_1_InRangeOf_3_to_6() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {0, 1, 0, 2, 3, 0});
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

        assertEquals(2, a.cardinality());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.iterator();

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

        assertEquals(0, a.cardinality());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2_InRangeOf_0_to_1() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 2, 3});
        ByteVectorIterator it = a.iterator(0, 1);

        assertTrue("hasNext 0", it.hasNext());
        it.next();
        iteratorSet("dummy 0", it, ZERO_DUMMY);
        iteratorSet("set 0", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(2, a.cardinality());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2_InRangeOf_0_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {0, 0, 0});
        ByteVectorIterator it = a.iterator(0, 3);

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

        assertEquals(0, a.cardinality());
        assertEquals(a, b);
    }

    @Test
    public void testNonZeroIteratorModify_2_InRangeOf_1_to_3() {

        SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {1, 2, 3});
        ByteVector b = factory().createVector(new byte[] {1, 0, 0});
        ByteVectorIterator it = a.iterator(1, 3);

        assertTrue("hasNext 1", it.hasNext());
        it.next();
        iteratorSet("dummy 1", it, ZERO_DUMMY);
        iteratorSet("set 1", it, (byte)0);

        assertTrue("hasNext 2", it.hasNext());
        it.next();
        iteratorSet("dummy 2", it, ZERO_DUMMY);
        iteratorSet("set 2", it, (byte)0);

        assertFalse(it.hasNext());

        assertEquals(1, a.cardinality());
        assertEquals(a, b);
    }
}
