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
package net.fec.openrq.util.linearalgebra.vector.operation.inplace;


import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;
import net.fec.openrq.util.arithmetic.OctetOps;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.vector.dense.DenseByteVector;
import net.fec.openrq.util.linearalgebra.vector.operation.VectorVectorOperation;
import net.fec.openrq.util.linearalgebra.vector.sparse.SparseByteVector;


public class InPlaceHadamardProduct extends VectorVectorOperation<Void> {

    @Override
    public Void apply(SparseByteVector a, SparseByteVector b) {

        ByteVectorIterator these = a.nonZeroBurningIterator();
        ByteVectorIterator those = b.nonZeroIterator();
        ByteVectorIterator both = these.andAlsoMultiply(those);

        // TODO: Wrap within a method in iterator
        while (both.hasNext()) {
            both.next();
            these.set(both.get());
        }
        these.flush();

        return null;
    }

    @Override
    public Void apply(SparseByteVector a, DenseByteVector b) {

        ByteVectorIterator it = a.nonZeroIterator();
        while (it.hasNext()) {
            it.next();
            it.set(aTimesB(it.get(), b.get(it.index())));
        }
        return null;
    }

    @Override
    public Void apply(DenseByteVector a, DenseByteVector b) {

        for (int i = 0; i < a.length(); i++) {
            a.set(i, aTimesB(a.get(i), b.get(i)));
        }
        return null;
    }

    @Override
    public Void apply(DenseByteVector a, SparseByteVector b) {

        ByteVectorIterator these = a.burningIterator();
        ByteVectorIterator those = b.nonZeroIterator();
        ByteVectorIterator both = these.andAlsoMultiply(those);

        // TODO: Wrap within a method in iterator
        while (both.hasNext()) {
            both.next();
            these.set(both.get());
        }
        these.flush();

        return null;
    }
}
