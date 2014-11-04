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
package net.fec.openrq.util.collection;


import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;


/**
 * This class provides iterator objects over bits in a bit set.
 */
public final class BitSetIterators {

    /**
     * Returns a new iterator over all true bits in the provided bit set.
     * 
     * @param bitSet
     *            A bit set
     * @return an iterator over true bits
     */
    public static Iterator<Integer> newTrueIterator(BitSet bitSet) {

        return newTrueIterator(bitSet, 0);
    }

    /**
     * Returns a new iterator over all true bits in the provided bit set, starting at the provided index (inclusive).
     * 
     * @param bitSet
     *            A bit set
     * @param fromIndex
     *            The initial index (inclusive)
     * @return an iterator over true bits
     */
    public static Iterator<Integer> newTrueIterator(BitSet bitSet, int fromIndex) {

        return new TrueIterator(bitSet, fromIndex);
    }

    /**
     * Returns a new iterator over all false bits in the provided bit set, until the provided index (exclusive).
     * 
     * @param bitSet
     *            A bit set
     * @param toIndex
     *            The final index (exclusive)
     * @return an iterator over false bits
     */
    public static Iterator<Integer> newFalseIterator(BitSet bitSet, int toIndex) {

        return newFalseIterator(bitSet, 0, toIndex);
    }

    /**
     * Returns a new iterator over all false bits in the provided bit set, starting at one provided index (inclusive),
     * until the other provided index (exclusive).
     * 
     * @param bitSet
     *            A bit set
     * @param fromIndex
     *            The initial index (inclusive)
     * @param toIndex
     *            The final index (exclusive)
     * @return an iterator over false bits
     */
    public static Iterator<Integer> newFalseIterator(BitSet bitSet, int fromIndex, int toIndex) {

        return new FalseIterator(bitSet, fromIndex, toIndex);
    }


    private static final class TrueIterator implements Iterator<Integer> {

        private final BitSet bitSet;
        private int next;


        TrueIterator(BitSet bitSet, int fromIndex) {

            this.bitSet = Objects.requireNonNull(bitSet);
            this.next = bitSet.nextSetBit(fromIndex); // throws IooB exception if negative index
        }

        @Override
        public boolean hasNext() {

            return next >= 0;
        }

        @Override
        public Integer next() {

            if (!hasNext()) throw new NoSuchElementException();

            final int current = next;
            next = bitSet.nextSetBit(current + 1);

            return current;
        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();
        }
    }

    private static final class FalseIterator implements Iterator<Integer> {

        private final BitSet bitSet;
        private final int fence;
        private int next;


        FalseIterator(BitSet bitSet, int fromIndex, int toIndex) {

            if (toIndex < 0) throw new IllegalArgumentException("toIndex < 0");
            if (fromIndex > toIndex) throw new IllegalArgumentException("fromIndex > toIndex");

            this.bitSet = Objects.requireNonNull(bitSet);
            this.fence = toIndex;
            this.next = bitSet.nextClearBit(fromIndex); // throws IooB exception if negative index
        }

        @Override
        public boolean hasNext() {

            return next < fence;
        }

        @Override
        public Integer next() {

            if (!hasNext()) throw new NoSuchElementException();

            final int current = next;
            next = bitSet.nextClearBit(current + 1);

            return current;
        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();
        }
    }


    private BitSetIterators() {

        // not instantiable
    }
}
