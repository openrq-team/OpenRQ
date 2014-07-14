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
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
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
package net.fec.openrq.util.linearalgebra.matrix.dense;


import static net.fec.openrq.util.arithmetic.OctetOps.aPlusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.matrix.AbstractByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;


public abstract class AbstractBasicByteMatrix extends AbstractByteMatrix
    implements DenseByteMatrix {

    private static final int BLOCKSIZE = 64;


    public AbstractBasicByteMatrix(Factory factory, int rows, int columns) {

        super(factory, rows, columns);
    }

    @Override
    public ByteMatrix multiply(ByteMatrix matrix, Factory factory) {

        if (matrix instanceof DenseByteMatrix && (rows % BLOCKSIZE == 0)
            && (columns % BLOCKSIZE == 0)
            && (matrix.columns() % BLOCKSIZE == 0)) {

            return multiplyBlockedWith64(matrix, factory);
        }

        return super.multiply(matrix, factory);
    }

    private ByteMatrix multiplyBlockedWith64(ByteMatrix matrix, Factory factory) {

        ByteMatrix result = factory.createMatrix(rows, matrix.columns());

        for (int i = 0; i < rows; i += BLOCKSIZE) {
            for (int k = 0; k < columns; k += BLOCKSIZE) {
                for (int j = 0; j < matrix.columns(); j += BLOCKSIZE) {
                    for (int u = 0; u < BLOCKSIZE; u++) {
                        for (int w = 0; w < BLOCKSIZE; w++) {
                            for (int v = 0; v < BLOCKSIZE; v++) {
                                final byte prod = aTimesB(get(i + u, k + w), matrix.get(k + w, j + v));
                                result.set(i + u, j + v, aPlusB(result.get(i + u, j + v), prod));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
