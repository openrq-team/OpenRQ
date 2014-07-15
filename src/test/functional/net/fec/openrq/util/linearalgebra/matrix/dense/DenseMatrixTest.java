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
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra.matrix.dense;


import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.fec.openrq.util.linearalgebra.matrix.AbstractMatrixTest;

import org.junit.Test;


public abstract class DenseMatrixTest extends AbstractMatrixTest {

    @Test
    public void testToArray() {

        byte array[][] = new byte[][] {
                                       {1, 0, 0},
                                       {0, 5, 0},
                                       {0, 0, 9}
        };

        DenseByteMatrix a = (DenseByteMatrix)factory().createMatrix(array);
        assertEquals(array.length, a.rows());

        byte[][] toArray = a.toArray();
        assertEquals(array.length, toArray.length);

        for (int i = 0; i < a.rows(); i++) {
            final byte[] row1 = array[i];
            final byte[] row2 = toArray[i];
            assertEquals(row1.length, row2.length);

            for (int j = 0; j < row1.length; j++) {
                assertTrue(aIsEqualToB(row1[j], row2[j]));
            }
        }
    }
}
