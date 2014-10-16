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
package net.fec.openrq.util.linearalgebra.matrix.dense;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import net.fec.openrq.util.linearalgebra.matrix.AbstractByteMatrixTest;

import org.junit.Assert;
import org.junit.Test;


public abstract class DenseByteMatrixTest extends AbstractByteMatrixTest {

    @Test
    public void testToArray() {

        byte array[][] = new byte[][] {
                                       {1, 0, 0},
                                       {0, 5, 0},
                                       {0, 0, 9}
        };

        DenseByteMatrix a = (DenseByteMatrix)factory().createMatrix(array);
        assertEquals(array.length, a.rows());
        assertEquals(array[0].length, a.columns());
        Assert.assertTrue(Arrays.deepEquals(array, a.toArray()));
    }
}
