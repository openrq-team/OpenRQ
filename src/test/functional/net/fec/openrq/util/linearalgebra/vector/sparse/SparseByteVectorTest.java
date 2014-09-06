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
import net.fec.openrq.util.linearalgebra.vector.AbstractByteVectorTest;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorAccumulator;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorProcedure;

import org.junit.Test;


public abstract class SparseByteVectorTest extends AbstractByteVectorTest {

    @Test
    public void testCardinality() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {0, 0, 0, 0, 1});

        assertEquals(1, a.cardinality());
    }

    @Test
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

    @Test
    public void testIsZeroAt_4() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {1, 0, 0, 4}
            );

        assertTrue(a.isZeroAt(1));
        assertFalse(a.isZeroAt(3));
    }

    @Test
    public void testNonZeroAt_6() {

        SparseByteVector a = (SparseByteVector)factory().createVector(
            new byte[] {0, 5, 2, 0, 0, 0}
            );

        assertTrue(a.nonZeroAt(1));
        assertFalse(a.nonZeroAt(3));
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
    public void testEachNonZero() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {7, 1, 2, 7, 7});

        a.eachNonZero(new SetterProcedure(b));

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_0_to_0() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        a.eachNonZero(new SetterProcedure(b), 0, 0);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_0_to_2() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {7, 1, 2, 3, 4});

        a.eachNonZero(new SetterProcedure(b), 0, 2);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_0_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {7, 1, 2, 7, 7});

        a.eachNonZero(new SetterProcedure(b), 0, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_2_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 7, 7});

        a.eachNonZero(new SetterProcedure(b), 2, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }

    @Test
    public void testEachNonZeroInRangeOf_5_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 1, 2, 3, 4});
        final ByteVector c = factory().createVector(new byte[] {0, 1, 2, 3, 4});

        a.eachNonZero(new SetterProcedure(b), 5, 5);

        assertEquals(initial, a); // check if each wrongly modifies the caller vector
        assertEquals(c, b);
    }


    private static final class IndexFunction implements VectorFunction {

        @Override
        public byte evaluate(int i, @SuppressWarnings("unused") byte value) {

            return (byte)i;
        }
    }


    @Test
    public void testUpdateNonZero() {

        final SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {7, 0, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 0, 0, 3, 4});

        a.updateNonZero(new IndexFunction());

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_0_to_0() {

        final SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {7, 0, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        a.updateNonZero(new IndexFunction(), 0, 0);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_0_to_2() {

        final SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {7, 0, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 0, 0, 7, 7});

        a.updateNonZero(new IndexFunction(), 0, 2);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_0_to_5() {

        final SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {7, 0, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {0, 0, 0, 3, 4});

        a.updateNonZero(new IndexFunction(), 0, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_2_to_5() {

        final SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {7, 0, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {7, 0, 0, 3, 4});

        a.updateNonZero(new IndexFunction(), 2, 5);

        assertEquals(b, a);
    }

    @Test
    public void testUpdateNonZeroInRangeOf_5_to_5() {

        final SparseByteVector a = (SparseByteVector)factory().createVector(new byte[] {7, 0, 0, 7, 7});
        final ByteVector b = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        a.updateNonZero(new IndexFunction(), 5, 5);

        assertEquals(b, a);
    }

    @Test
    public void testTransformNonZero() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 0, 0, 3, 4});

        final ByteVector c = a.transformNonZero(new IndexFunction());

        assertEquals(initial, a); // check if transform wrongly modifies the caller vector
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRangeOf_0_to_0() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector c = a.transformNonZero(new IndexFunction(), 0, 0);

        assertEquals(initial, a); // check if transform wrongly modifies the caller vector
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRangeOf_0_to_2() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 0, 0, 7, 7});

        final ByteVector c = a.transformNonZero(new IndexFunction(), 0, 2);

        assertEquals(initial, a); // check if transform wrongly modifies the caller vector
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRangeOf_0_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {0, 0, 0, 3, 4});

        final ByteVector c = a.transformNonZero(new IndexFunction(), 0, 5);

        assertEquals(initial, a); // check if transform wrongly modifies the caller vector
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRangeOf_2_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {7, 0, 0, 3, 4});

        final ByteVector c = a.transformNonZero(new IndexFunction(), 2, 5);

        assertEquals(initial, a); // check if transform wrongly modifies the caller vector
        assertEquals(b, c);
    }

    @Test
    public void testTransformNonZeroInRangeOf_5_to_5() {

        final ByteVector initial = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final SparseByteVector a = (SparseByteVector)initial.copy();
        final ByteVector b = factory().createVector(new byte[] {7, 0, 0, 7, 7});

        final ByteVector c = a.transformNonZero(new IndexFunction(), 5, 5);

        assertEquals(initial, a); // check if transform wrongly modifies the caller vector
        assertEquals(b, c);
    }
}
