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
package net.fec.openrq.util.linearalgebra.matrix.sparse;


import java.util.NoSuchElementException;
import java.util.Objects;

import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;
import net.fec.openrq.util.linearalgebra.vector.sparse.CompressedByteVector;
import net.fec.openrq.util.linearalgebra.vector.sparse.SparseByteVector;


/**
 * Base class for CRS/CCS matrices.
 */
final class SparseVectors {

    private final ByteVector[] vectors;
    private final ByteVector empty;


    SparseVectors(int numVectors, int vectorLength) {

        this.vectors = new ByteVector[numVectors];
        this.empty = new EmptyImmutableByteVector(vectorLength);
        for (int i = 0; i < numVectors; i++) {
            vectors[i] = empty;
        }
    }

    SparseVectors(int numVectors, int vectorLength, byte[][] values, int[][] indices, int[] cardinalities) {

        this.vectors = new ByteVector[numVectors];
        this.empty = new EmptyImmutableByteVector(vectorLength);
        for (int i = 0; i < numVectors; i++) {
            vectors[i] = initCompressedVector(values[i], indices[i], cardinalities[i]);
        }
    }

    private SparseVectors(ByteVector[] vectors, ByteVector empty) {

        this.vectors = Objects.requireNonNull(vectors);
        this.empty = Objects.requireNonNull(empty);
    }

    SparseVectors copy() {

        ByteVector[] $vectors = new ByteVector[this.vectors.length];
        int i = 0;
        for (ByteVector vec : this.vectors) {
            $vectors[i++] = vec.copy(); // empty vectors return themselves on copy()
        }

        return new SparseVectors($vectors, empty);
    }

    void initializeVector(int index, VectorSource source) {

        vectors[index] = new CompressedByteVector(source);
    }

    // Read/Write
    ByteVector vectorRW(int index) {

        ByteVector vec = vectors[index];
        if (vec == empty) {
            vec = initCompressedVector();
            vectors[index] = vec;
        }
        return vec;
    }

    // Read Only
    ByteVector vectorR(int index) {

        return vectors[index];
    }

    void swapVectors(int i, int j) {

        ArrayUtils.swapObjects(vectors, i, j);
    }

    private ByteVector initCompressedVector() {

        return new CompressedByteVector(empty.length());
    }

    private ByteVector initCompressedVector(byte[] values, int[] indices, int cardinality) {

        return new CompressedByteVector(empty.length(), cardinality, values, indices);
    }


    private static final class EmptyImmutableByteVector extends SparseByteVector {

        EmptyImmutableByteVector(int length) {

            super(length, 0); // cardinality == 0
        }

        @Override
        @SuppressWarnings("unused")
        protected void safeSet(int i, byte value) {

            throw new UnsupportedOperationException("immutable byte vector");
        }

        @Override
        @SuppressWarnings("unused")
        protected byte safeGet(int i) {

            return 0; // an empty byte vector only has zero entries
        }

        @Override
        public boolean nonZeroAt(int i) {

            Indexables.checkIndexBounds(i, length());
            return false; // an empty byte vector only has zero entries
        }

        @Override
        public int[] nonZeroPositions() {

            return ArrayUtils.EmptyArrayOf.ints(); // an empty byte vector only has zero entries
        }

        @Override
        public int[] nonZeroPositions(int fromIndex, int toIndex) {

            Indexables.checkFromToBounds(fromIndex, toIndex, length());
            return ArrayUtils.EmptyArrayOf.ints(); // an empty byte vector only has zero entries
        }

        @Override
        public void clear() {

            // an empty byte vector only has zero entries, so do nothing
        }

        @Override
        public void swap(int i, int j) {

            Indexables.checkIndexBounds(i, length());
            Indexables.checkIndexBounds(j, length());
            // an empty byte vector only has zero entries, so do nothing
        }

        @Override
        public ByteVector copy() {

            return this; // since this vector is immutable we can simply just return it
        }

        @Override
        public ByteVectorIterator nonZeroIterator() {

            return EmptyNonZeroIterator.INSTANCE;
        }

        @Override
        public ByteVectorIterator nonZeroIterator(int fromIndex, int toIndex) {

            Indexables.checkFromToBounds(fromIndex, toIndex, length());
            return EmptyNonZeroIterator.INSTANCE;
        }


        private static final class EmptyNonZeroIterator extends ByteVectorIterator {

            static final EmptyNonZeroIterator INSTANCE = new EmptyNonZeroIterator();


            EmptyNonZeroIterator() {

                super(0);
            }

            @Override
            public boolean hasNext() {

                return false; // an empty byte vector only has zero entries
            }

            @Override
            public Byte next() {

                throw new NoSuchElementException("empty non zero iterator has zero elements");
            }

            @Override
            public int index() {

                throw new NoSuchElementException("empty non zero iterator has zero elements");
            }

            @Override
            public byte get() {

                throw new NoSuchElementException("empty non zero iterator has zero elements");
            }

            @Override
            @SuppressWarnings("unused")
            public void set(byte value) {

                throw new NoSuchElementException("empty non zero iterator has zero elements");
            }
        }
    }
}
