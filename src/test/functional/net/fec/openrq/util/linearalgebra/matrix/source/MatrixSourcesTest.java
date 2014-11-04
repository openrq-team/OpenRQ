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
package net.fec.openrq.util.linearalgebra.matrix.source;


import static org.junit.Assert.assertEquals;
import net.fec.openrq.util.linearalgebra.factory.Basic1DFactory;
import net.fec.openrq.util.linearalgebra.factory.Basic2DFactory;
import net.fec.openrq.util.linearalgebra.factory.CRSFactory;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;

import org.junit.Test;


public class MatrixSourcesTest {

    public static final Factory[] FACTORIES = {
                                               new Basic1DFactory(), new Basic2DFactory(),
                                               new CRSFactory()// , new CCSFactory()
    };


    @Test
    public void testIdentityMatrixSource() {

        for (Factory factory : FACTORIES) {
            ByteMatrix a = factory.createMatrix(new IdentityMatrixSource(5));

            assertEquals(5, a.rows());
            assertEquals(5, a.columns());

            for (int i = 0; i < a.rows(); i++) {
                for (int j = 0; j < a.columns(); j++) {
                    if (i == j) {
                        assertEquals(a.get(i, j), 1);
                    }
                    else {
                        assertEquals(a.get(i, j), 0);
                    }
                }
            }
        }
    }
}
