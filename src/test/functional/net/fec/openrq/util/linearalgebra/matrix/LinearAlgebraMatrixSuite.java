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
package net.fec.openrq.util.linearalgebra.matrix;


import net.fec.openrq.util.linearalgebra.matrix.dense.Basic1DByteMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.dense.Basic2DByteMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSourcesTest;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CCSByteMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CRSByteMatrixTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
               Basic1DByteMatrixTest.class,
               Basic2DByteMatrixTest.class,
               MatrixSourcesTest.class,
               CCSByteMatrixTest.class,
               CRSByteMatrixTest.class
})
public class LinearAlgebraMatrixSuite {

    // placeholder class for inclusion of remaining test classes
}
