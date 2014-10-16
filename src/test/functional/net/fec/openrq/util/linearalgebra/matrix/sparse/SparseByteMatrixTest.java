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
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra.matrix.sparse;


import static net.fec.openrq.util.math.OctetOps.aTimesB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.fec.openrq.util.linearalgebra.matrix.AbstractByteMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;

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

        ByteMatrix a = factory().createMatrix(i, j);

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

        ByteMatrix a = factory().createMatrix(i, j);

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

        ByteMatrix a = factory().createMatrix(i, j);

        assertEquals(i, a.rows());
        assertEquals(j, a.columns());

        for (int row = 0; row < 32; row++) {
            a.set(row, 1, (byte)3);
        }
    }
}
