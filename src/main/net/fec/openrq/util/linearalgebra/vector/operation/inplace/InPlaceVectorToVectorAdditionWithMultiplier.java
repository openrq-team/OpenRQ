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
package net.fec.openrq.util.linearalgebra.vector.operation.inplace;


import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static net.fec.openrq.util.math.OctetOps.aTimesB;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.vector.dense.DenseByteVector;
import net.fec.openrq.util.linearalgebra.vector.operation.VectorVectorOperation;
import net.fec.openrq.util.linearalgebra.vector.operation.inplace.Indexers.Indexer;
import net.fec.openrq.util.linearalgebra.vector.sparse.SparseByteVector;


public class InPlaceVectorToVectorAdditionWithMultiplier extends VectorVectorOperation<Void> {

    private final Indexer indexer;
    private final byte bMultiplier;


    public InPlaceVectorToVectorAdditionWithMultiplier(byte bMultiplier) {

        this.indexer = Indexers.defaultIndexer();
        this.bMultiplier = bMultiplier;
    }

    public InPlaceVectorToVectorAdditionWithMultiplier(byte bMultiplier, int from, int to) {

        this.indexer = Indexers.boundedIndexer(from, to);
        this.bMultiplier = bMultiplier;
    }

    @Override
    public Void apply(SparseByteVector a, SparseByteVector b) {

        ByteVectorIterator it = indexer.nonZeroIterator(b);
        // TODO: these.andAlsoAdd(those)
        // these.andAlsoSubtract(those);
        while (it.hasNext()) {
            it.next();
            a.addInPlace(it.index(), multB(it.get()));
        }
        return null;
    }

    @Override
    public Void apply(SparseByteVector a, DenseByteVector b) {

        ByteVectorIterator it = indexer.iterator(a);
        while (it.hasNext()) {
            it.set(aPlusB(it.get(), multB(b.get(it.index()))));
        }
        return null;
    }

    @Override
    public Void apply(DenseByteVector a, DenseByteVector b) {

        for (int i = indexer.fromIndex(); i < indexer.toIndex(a); i++) {
            a.set(i, aPlusB(a.get(i), multB(b.get(i))));
        }
        return null;
    }

    @Override
    public Void apply(DenseByteVector a, SparseByteVector b) {

        ByteVectorIterator it = indexer.nonZeroIterator(b);
        while (it.hasNext()) {
            it.next();
            a.set(it.index(), aPlusB(a.get(it.index()), multB(it.get())));
        }
        return null;
    }

    private byte multB(byte bValue) {

        return aTimesB(bMultiplier, bValue);
    }
}
