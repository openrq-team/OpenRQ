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
package net.fec.openrq.util.linearalgebra.matrix;


import net.fec.openrq.util.linearalgebra.matrix.dense.Basic1DMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.dense.Basic2DMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSourcesTest;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CCSMatrixTest;
import net.fec.openrq.util.linearalgebra.matrix.sparse.CRSMatrixTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
               Basic1DMatrixTest.class,
               Basic2DMatrixTest.class,
               MatrixSourcesTest.class,
               CCSMatrixTest.class,
               CRSMatrixTest.class
})
public class LinearAlgebraMatrixSuite {

    // placeholder class for inclusion of remaining test classes
}
