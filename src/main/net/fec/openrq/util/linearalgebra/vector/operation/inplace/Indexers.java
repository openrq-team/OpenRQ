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


import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;


final class Indexers {

    static Indexer defaultIndexer() {

        return DefaultIndexer.INSTANCE;
    }

    static Indexer boundedIndexer(int from, int to) {

        return new BoundedIndexer(from, to);
    }


    static abstract class Indexer {

        private Indexer() {

            // only instances from the outer class may be created
        }

        abstract int fromIndex();

        abstract int toIndex(ByteVector vector);

        abstract ByteVectorIterator iterator(ByteVector vector);

        abstract ByteVectorIterator nonZeroIterator(ByteVector vector);
    }

    private static final class DefaultIndexer extends Indexer {

        static final DefaultIndexer INSTANCE = new DefaultIndexer();


        @Override
        int fromIndex() {

            return 0;
        }

        @Override
        int toIndex(ByteVector vector) {

            return vector.length();
        }

        @Override
        ByteVectorIterator iterator(ByteVector vector) {

            return vector.iterator();
        }

        @Override
        ByteVectorIterator nonZeroIterator(ByteVector vector) {

            return vector.nonZeroIterator();
        }
    }

    private static final class BoundedIndexer extends Indexer {

        private final int from;
        private final int to;


        BoundedIndexer(int from, int to) {

            this.from = from;
            this.to = to;
        }

        @Override
        int fromIndex() {

            return from;
        }

        @Override
        @SuppressWarnings("unused")
        int toIndex(ByteVector vector) {

            return to;
        }

        @Override
        ByteVectorIterator iterator(ByteVector vector) {

            return vector.iterator(from, to);
        }

        @Override
        ByteVectorIterator nonZeroIterator(ByteVector vector) {

            return vector.nonZeroIterator(from, to);
        }
    }


    private Indexers() {

        // not instantiable
    }
}
