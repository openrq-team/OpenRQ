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
package net.fec.openrq.util.linearalgebra.vector.operation.ooplace;


import static net.fec.openrq.util.math.OctetOps.aTimesB;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.dense.DenseByteVector;
import net.fec.openrq.util.linearalgebra.vector.operation.VectorVectorOperation;
import net.fec.openrq.util.linearalgebra.vector.sparse.SparseByteVector;


public class OoPlaceHadamardProduct extends VectorVectorOperation<ByteVector> {

    private Factory factory;


    public OoPlaceHadamardProduct(Factory factory) {

        this.factory = factory;
    }

    @Override
    public ByteVector apply(SparseByteVector a, SparseByteVector b) {

        ByteVectorIterator these = a.nonZeroIterator();
        ByteVectorIterator those = b.nonZeroIterator();
        ByteVectorIterator both = these.andAlsoMultiply(those);

        return both.toVector(factory);
    }

    @Override
    public ByteVector apply(SparseByteVector a, DenseByteVector b) {

        return apply(b, a); // this is possible because our multiplication operation is commutative
    }

    @Override
    public ByteVector apply(DenseByteVector a, DenseByteVector b) {

        ByteVector result = a.blank(factory);

        for (int i = 0; i < a.length(); i++) {
            result.set(i, aTimesB(a.get(i), b.get(i)));
        }

        return result;
    }

    @Override
    public ByteVector apply(DenseByteVector a, SparseByteVector b) {

        ByteVector result = a.blank(factory);
        ByteVectorIterator it = b.nonZeroIterator();

        while (it.hasNext()) {
            it.next();
            result.set(it.index(), aTimesB(it.get(), a.get(it.index())));
        }

        return result;
    }
}
