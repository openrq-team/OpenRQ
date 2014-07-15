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


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.fec.openrq.util.linearalgebra.vector.AbstractVectorTest;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorAccumulator;


public abstract class SparseVectorTest extends AbstractVectorTest {

    public void testCardinality() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {0, 0, 0, 0, 1});

        assertEquals(1, a.cardinality());
    }

    public void testFoldNonZero_5() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {2, 0, 5, 0, 2}
            );

        VectorAccumulator sum = ByteVectors.asSumAccumulator((byte)0);
        VectorAccumulator product = ByteVectors.asProductAccumulator((byte)1);

        assertTrue(aIsEqualToB(a.foldNonZero(sum), (byte)5));
        // check whether the accumulator were flushed
        assertTrue(aIsEqualToB(a.foldNonZero(sum), (byte)5));

        assertTrue(aIsEqualToB(a.foldNonZero(product), (byte)20));
        // check whether the accumulator were flushed
        assertTrue(aIsEqualToB(a.foldNonZero(product), (byte)20));
    }

    public void testIsZeroAt_4() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {1, 0, 0, 4}
            );

        assertTrue(a.isZeroAt(1));
        assertFalse(a.isZeroAt(3));
    }

    public void testNonZeroAt_6() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {0, 5, 2, 0, 0, 0}
            );

        assertTrue(a.nonZeroAt(1));
        assertFalse(a.nonZeroAt(3));
    }
}
