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
 * Contributor(s): Ewald Grusk
 * Yuriy Drozd
 * Maxim Samoylov
 */
package net.fec.openrq.util.linearalgebra.vector.sparse;


import static net.fec.openrq.util.arithmetic.OctetOps.aPlusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;

import java.util.Arrays;

import net.fec.openrq.util.arithmetic.ExtraMath;
import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;


public class CompressedByteVector extends SparseByteVector {

    private static final int DEFAULT_CAPACITY = 8;

    private byte values[];
    private int indices[]; // must always be sorted


    public CompressedByteVector() {

        this(0);
    }

    public CompressedByteVector(int length) {

        this(length, 0);
    }

    public CompressedByteVector(ByteVector vector) {

        this(ByteVectors.asVectorSource(vector));
    }

    public CompressedByteVector(byte array[]) {

        this(ByteVectors.asArraySource(array));
    }

    public CompressedByteVector(VectorSource source) {

        this(source.length(), 0);

        for (int i = 0; i < length(); i++) {
            byte value = source.get(i);
            if (value != 0) {
                ensureCapacity(+1);
                values[cardinality] = value;
                indices[cardinality] = i;
                cardinality++;
            }
        }
    }

    public CompressedByteVector(int length, int cardinality) {

        super(length, cardinality);

        this.values = new byte[cardinality];
        this.indices = new int[cardinality];
    }

    public CompressedByteVector(int length, int cardinality, byte values[], int indices[]) {

        super(length, cardinality);

        this.values = values;
        this.indices = indices;
    }

    /**
     * Returns {@code true} iff there is a nonzero entry associated to the given k-index.
     * 
     * @param k
     *            a k-index (a value returned by the method {@link #binarySearch(int)})
     * @return {@code true} iff there is a nonzero entry associated to the given k-index
     */
    private static boolean hasEntry(int k) {

        // where k is the value returned by binarySearch(i)
        return k >= 0;
    }

    /**
     * If there is a nonzero entry associated to the given k-index, then this method returns that entry position.
     * Otherwise, this method returns the position of the nonzero entry that comes immediately after a new nonzero entry
     * created from the element index that resulted in the given k-index via the method {@link #binarySearch(int)}.
     * <p>
     * If a position of a new nonzero entry is returned (instead of a position of an entry associated to the given
     * k-index), then the returned position is also the insertion point in the nonzero entry arrays.
     * 
     * @param k
     *            a k-index (a value returned by the method {@link #binarySearch(int)})
     * @return the position of an existing nonzero entry associated to the given k-index, or the position of a new
     *         nonzero entry created from the element index that returned in the given k-index via the method
     *         {@link #binarySearch(int)}
     */
    private static int getEntry(int k) {

        // when k < 0, Arrays.binarySearch returns (-(insertion point) - 1)
        return hasEntry(k) ? k : -(k + 1);
    }

    /**
     * Searches for a nonzero entry given an element index and returns a k-index.
     * 
     * @param i
     *            an element index
     * @return a k-index
     */
    private int binarySearch(int i) {

        return Arrays.binarySearch(indices, 0, cardinality, i);
    }

    private SearchEntry searchByIndex(int i) {

        return new SearchEntry(i);
    }

    private SearchRange searchByRange(int from, int to) {

        return new SearchRange(from, to);
    }

    // =========================================================================
    // Optimized multiplications that take advantage of sparsity in this vector.

    @Override
    public ByteVector multiply(byte value) {

        return multiply(value, factory());
    }

    @Override
    public ByteVector multiply(byte value, Factory factory) {

        ByteVector result = blank(factory);

        if (value != 0) {
            ByteVectorIterator it = nonZeroIterator();
            while (it.hasNext()) {
                it.next();
                result.set(it.index(), aTimesB(value, it.get()));
            }
        }

        return result;
    }

    @Override
    public ByteVector multiply(ByteMatrix matrix) {

        return multiply(matrix, factory());
    }

    @Override
    public ByteVector multiply(ByteMatrix matrix, Factory factory) {

        ensureArgumentIsNotNull(matrix, "matrix");

        if (length() != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + length() + "x_.");
        }

        ByteVector result = factory.createVector(matrix.columns());

        for (int j = 0; j < matrix.columns(); j++) {
            byte acc = 0;

            ByteVectorIterator it = nonZeroIterator();
            while (it.hasNext()) {
                it.next();
                final byte prod = aTimesB(it.get(), matrix.get(it.index(), j));
                acc = aPlusB(acc, prod);
            }

            result.set(j, acc);
        }

        return result;
    }

    // Optimized multiplications that take advantage of sparsity in this vector.
    // =========================================================================

    @Override
    public int nonZeros() {

        return cardinality;
    }

    @Override
    public int nonZeros(int from, int to) {

        Indexables.checkFromToBounds(from, to, length());
        return searchByRange(from, to).rangeLength();
    }

    @Override
    public boolean nonZeroAt(int i) {

        Indexables.checkIndexBounds(i, length());
        return searchByIndex(i).isNonZero();
    }

    @Override
    protected byte safeGet(int i) {

        return searchByIndex(i).value();
    }

    @Override
    protected void safeSet(int i, byte value) {

        searchByIndex(i).update(value);
    }

    @Override
    public void addInPlace(int i, byte value) {

        Indexables.checkIndexBounds(i, length());
        SearchEntry entry = searchByIndex(i);
        entry.update(aPlusB(entry.value(), value));
    }

    @Override
    public void update(int i, VectorFunction function) {

        Indexables.checkIndexBounds(i, length());
        SearchEntry entry = searchByIndex(i);
        entry.update(function.evaluate(i, entry.value()));
    }

    @Override
    public int[] nonZeroPositions() {

        return Arrays.copyOf(indices, cardinality);
    }

    @Override
    public int[] nonZeroPositions(int from, int to) {

        Indexables.checkFromToBounds(from, to, length());
        SearchRange range = searchByRange(from, to);
        return Arrays.copyOfRange(indices, range.fromK(), range.toK());
    }

    @Override
    public void swap(int i, int j) {

        Indexables.checkIndexBounds(i, length());
        Indexables.checkIndexBounds(j, length());

        if (i != j) {
            final int left = Math.min(i, j);
            final int right = Math.max(i, j);

            // positions of the left and right column indices in the values/columnIndices arrays
            final int leftPos = binarySearch(left);
            final int rightPos = binarySearch(right);

            // if both positions were found
            if (hasEntry(leftPos) && hasEntry(rightPos)) {
                // only need to swap the non zero values, since the column indices remain fixed
                ArrayUtils.swapBytes(values, leftPos, rightPos);
            }
            else if (hasEntry(leftPos)) { // if only the left position was found
                // store temporarily the current value at leftPos
                byte tempLeftValue = values[leftPos];

                // the new position of the element at leftPos is the position
                // immediately before the insertion position of rightPos
                final int newLeftPos = getEntry(rightPos) - 1;

                // shift the elements between leftPos and newLeftPos by one to the left
                System.arraycopy(indices, leftPos + 1, indices, leftPos, newLeftPos - leftPos);
                System.arraycopy(values, leftPos + 1, values, leftPos, newLeftPos - leftPos);

                // update the element at newLeftPos with the new value/position
                indices[newLeftPos] = right;
                values[newLeftPos] = tempLeftValue;
            }
            else if (hasEntry(rightPos)) { // if only the right position was found
                // store temporarily the current value at rightPos
                byte tempRightValue = values[rightPos];

                // the new position of the element at rightPos is the insertion position of leftPos
                final int newRightPos = getEntry(leftPos);

                // shift the elements between newRightPos and rightPos by one to the right
                System.arraycopy(indices, newRightPos, indices, newRightPos + 1, rightPos - newRightPos);
                System.arraycopy(values, newRightPos, values, newRightPos + 1, rightPos - newRightPos);

                // update the element at newRightPos with the new value/position
                indices[newRightPos] = left;
                values[newRightPos] = tempRightValue;
            }
            // else nothing needs to be swapped
        }
    }

    @Override
    public ByteVector resize(int $length) {

        ensureLengthIsCorrect($length);

        final int $cardinality = ($length >= this.length()) ? this.cardinality : getEntry(binarySearch($length));
        final byte $values[] = Arrays.copyOf(this.values, $cardinality);
        final int $indices[] = Arrays.copyOf(this.indices, $cardinality);

        return new CompressedByteVector($length, $cardinality, $values, $indices);
    }

    @Override
    public void addInPlace(ByteVector vector) {

        if (vector instanceof CompressedByteVector) {
            ensureVectorIsSimilar(vector);
            _addInPlace((byte)1, (CompressedByteVector)vector);
        }
        else {
            super.addInPlace(vector);
        }
    }

    @Override
    public void addInPlace(ByteVector vector, int fromIndex, int toIndex) {

        if (vector instanceof CompressedByteVector) {
            ensureVectorIsSimilar(vector, fromIndex, toIndex);
            _addInPlace((byte)1, (CompressedByteVector)vector, fromIndex, toIndex);
        }
        else {
            super.addInPlace(vector, fromIndex, toIndex);
        }
    }

    @Override
    public void addInPlace(byte multiplier, ByteVector vector) {

        if (vector instanceof CompressedByteVector) {
            ensureVectorIsSimilar(vector);
            _addInPlace(multiplier, (CompressedByteVector)vector);
        }
        else {
            super.addInPlace(multiplier, vector);
        }
    }

    @Override
    public void addInPlace(byte multiplier, ByteVector vector, int fromIndex, int toIndex) {

        if (vector instanceof CompressedByteVector) {
            ensureVectorIsSimilar(vector, fromIndex, toIndex);
            _addInPlace(multiplier, (CompressedByteVector)vector, fromIndex, toIndex);
        }
        else {
            super.addInPlace(multiplier, vector, fromIndex, toIndex);
        }
    }

    private void _addInPlace(byte multiplier, CompressedByteVector vector) {

        _addInPlace(multiplier, vector, 0, this.cardinality(), 0, vector.cardinality());
    }

    /*
     * Requires valid indices.
     */
    private void _addInPlace(byte multiplier, CompressedByteVector vector, int fromIndex, int toIndex) {

        final SearchRange thisRange = this.searchByRange(fromIndex, toIndex);
        final SearchRange otherRange = vector.searchByRange(fromIndex, toIndex);
        _addInPlace(multiplier, vector, thisRange.fromK(), thisRange.toK(), otherRange.fromK(), otherRange.toK());
    }

    /*
     * Requires valid indices in respect to both vectors' cardinalities.
     */
    private void _addInPlace(
        final byte multiplier,
        final CompressedByteVector vector,
        final int thisFrom,
        final int thisTo,
        final int otherFrom,
        final int otherTo)
    {

        // if the multiplier is zero, or if the other vector only has zeros, then nothing needs to be added
        if (multiplier != 0 && otherTo > otherFrom) {
            final byte[] thisValues = this.values;
            final int[] thisIndices = this.indices;

            final byte[] otherValues = vector.values;
            final int[] otherIndices = vector.indices;

            // allocate with the maximum capacity from both vectors
            final ValueIndexList list = new ValueIndexList(Math.max(thisValues.length, otherValues.length));

            /*
             * Let's build a list with the new values resulting from the addition.
             * Only values whose indices in both vectors are the same need to be added,
             * the remaining are simply copied to the list, going by index order.
             */

            // first copy all elements before thisFrom
            list.bulkAdd(thisValues, thisIndices, 0, otherFrom);

            int th = thisFrom;
            int ot = otherFrom;
            while (th < thisTo || ot < otherTo) {
                if (!(ot < otherTo)) { // only values/indices from this vector remain
                    list.add(thisValues[th], thisIndices[th]);
                    th++;
                }
                else if (!(th < thisTo)) { // only values/indices from the other vector remain
                    list.add(aTimesB(multiplier, otherValues[ot]), otherIndices[ot]);
                    ot++;
                }
                else {
                    final int thisIndex = thisIndices[th];
                    final int otherIndex = otherIndices[ot];

                    if (thisIndex < otherIndex) {
                        list.add(thisValues[th], thisIndex);
                        th++;
                    }
                    else if (otherIndex < thisIndex) {
                        list.add(aTimesB(multiplier, otherValues[ot]), otherIndex);
                        ot++;
                    }
                    else {
                        final byte sum = aPlusB(aTimesB(multiplier, otherValues[ot]), thisValues[th]);
                        if (sum != 0) {
                            list.add(sum, thisIndex);
                        }
                        th++;
                        ot++;
                    }
                }
            }

            // lastly copy all elements from thisTo to this.cardinality
            list.bulkAdd(thisValues, thisIndices, thisTo, this.cardinality());

            // update this vector with the new values/indices
            this.values = list.values();
            this.indices = list.indices();
            this.cardinality = list.cardinality();
        }
    }

    @Override
    public ByteVectorIterator iterator() {

        return new VectorIterator();
    }

    @Override
    public ByteVectorIterator iterator(int fromIndex, int toIndex) {

        Indexables.checkFromToBounds(fromIndex, toIndex, length());
        return new VectorIterator(fromIndex, toIndex);
    }

    @Override
    public ByteVectorIterator nonZeroIterator() {

        return new NonZeroVectorIterator();
    }

    @Override
    public ByteVectorIterator nonZeroIterator(int fromIndex, int toIndex) {

        Indexables.checkFromToBounds(fromIndex, toIndex, length());
        return new NonZeroVectorIterator(fromIndex, toIndex);
    }

    private void insertNonZero(int k, int i, byte value) {

        if (value != 0) { // only nonzero values need to be inserted
            ensureCapacity(+1);

            System.arraycopy(values, k, values, k + 1, cardinality - k);
            System.arraycopy(indices, k, indices, k + 1, cardinality - k);

            values[k] = value;
            indices[k] = i;
            cardinality++;
        }
    }

    private void removeNonZero(int k) {

        cardinality--;
        System.arraycopy(values, k + 1, values, k, cardinality - k);
        System.arraycopy(indices, k + 1, indices, k, cardinality - k);
    }

    // requires non-negative extraElements
    private void ensureCapacity(int extraElements) {

        final int minCapacity = ExtraMath.addExact(cardinality, extraElements);
        final int oldCapacity = values.length;

        if (minCapacity > oldCapacity) {
            final int newCapacity = getNewCapacity(minCapacity, oldCapacity);

            final byte[] newValues = new byte[newCapacity];
            final int[] newIndices = new int[newCapacity];

            System.arraycopy(values, 0, newValues, 0, cardinality);
            System.arraycopy(indices, 0, newIndices, 0, cardinality);

            values = newValues;
            indices = newIndices;
        }
    }

    // requires non-negative extraElements
    private static int getNewCapacity(int minCapacity, int oldCapacity) {

        if (oldCapacity == 0) {
            return Math.max(minCapacity, DEFAULT_CAPACITY);
        }
        else {
            return Math.max(minCapacity, ExtraMath.addExact(oldCapacity, oldCapacity / 2));
        }
    }


    // class used for vector to vector addition
    private static final class ValueIndexList {

        private byte[] values;
        private int[] indices;
        private int cardinality;


        ValueIndexList(int length) {

            this.values = new byte[length];
            this.indices = new int[length];
            this.cardinality = 0;
        }

        byte[] values() {

            return values;
        }

        int[] indices() {

            return indices;
        }

        int cardinality() {

            return cardinality;
        }

        void add(byte newValue, int newIndex) {

            ensureCapacity(+1);
            values[cardinality] = newValue;
            indices[cardinality] = newIndex;
            cardinality++;
        }

        /*
         * Requires valid from and to indices.
         */
        void bulkAdd(byte[] newValues, int[] newIndices, int from, int to) {

            if (to > from) {
                final int len = to - from;
                ensureCapacity(+len);
                System.arraycopy(newValues, from, values, cardinality, len);
                System.arraycopy(newIndices, from, indices, cardinality, len);
                cardinality += len;
            }
        }

        // requires non-negative extraElements
        private void ensureCapacity(int extraElements) {

            final int minCapacity = ExtraMath.addExact(cardinality, extraElements);
            final int oldCapacity = values.length;

            if (minCapacity > oldCapacity) {
                final int newCapacity = getNewCapacity(minCapacity, oldCapacity);

                final byte[] newValues = new byte[newCapacity];
                final int[] newIndices = new int[newCapacity];

                System.arraycopy(values, 0, newValues, 0, cardinality);
                System.arraycopy(indices, 0, newIndices, 0, cardinality);

                values = newValues;
                indices = newIndices;
            }
        }
    }

    // class used for nonzero entry searches, and consequent value updates
    private final class SearchEntry {

        private final int i;
        private final int k;


        /*
         * Requires valid index.
         */
        SearchEntry(int i) {

            this.i = i;
            this.k = binarySearch(i);
        }

        boolean isNonZero() {

            return hasEntry(k);
        }

        byte value() {

            return isNonZero() ? values[k] : 0;
        }

        void update(byte value) {

            if (isNonZero()) {
                if (value != 0) {
                    values[k] = value;
                }
                else {
                    removeNonZero(k);
                }
            }
            else {
                insertNonZero(getEntry(k), i, value);
            }
        }
    }

    // class used for nonzero entry searches between a specified index range
    private final class SearchRange {

        private final int fromK;
        private final int toK;


        /*
         * Requires valid indices.
         */
        SearchRange(int from, int to) {

            this.fromK = getEntry(binarySearch(from));
            this.toK = getEntry(binarySearch(to));
        }

        int fromK() {

            return fromK;
        }

        int toK() {

            return toK;
        }

        int rangeLength() {

            return toK - fromK;
        }
    }

    private final class VectorIterator extends ByteVectorIterator {

        private int i;
        private final int end;
        private int k;


        VectorIterator() {

            super(length());

            this.i = -1;
            this.end = length();
            this.k = 0;
        }

        /*
         * Requires valid indices.
         */
        VectorIterator(int from, int to) {

            super(to - from);

            this.i = from - 1;
            this.end = to;
            this.k = getEntry(binarySearch(from));
        }

        @Override
        public int index() {

            return i;
        }

        @Override
        public byte get() {

            return isCurrentNonZero() ? values[k] : 0;
        }

        @Override
        public void set(byte value) {

            if (isCurrentNonZero()) {
                if (value != 0) {
                    values[k] = value;
                }
                else {
                    removeNonZero(k);
                }
            }
            else {
                insertNonZero(k, i, value);
            }
        }

        @Override
        public boolean hasNext() {

            return i + 1 < end;
        }

        @Override
        public Byte next() {

            if (isCurrentNonZero()) {
                k++;
            }
            i++;
            return get();
        }

        @Override
        protected int innerCursor() {

            return k;
        }

        private boolean isCurrentNonZero() {

            return k < cardinality && indices[k] == i;
        }
    }

    private final class NonZeroVectorIterator extends ByteVectorIterator {

        private boolean currentIsRemoved = false;
        private int removedIndex;
        private final int end;
        private int k;


        NonZeroVectorIterator() {

            super(length());

            this.currentIsRemoved = false;
            this.removedIndex = -1;
            this.end = length();
            this.k = -1; // hasNext checks for k + 1
        }

        /*
         * Requires valid indices.
         */
        NonZeroVectorIterator(int from, int to) {

            super(to - from);

            this.currentIsRemoved = false;
            this.removedIndex = -1;
            this.end = to;
            this.k = getEntry(binarySearch(from)) - 1; // hasNext checks for k + 1
        }

        @Override
        public int index() {

            return currentIsRemoved ? removedIndex : indices[k];
        }

        @Override
        public byte get() {

            return currentIsRemoved ? 0 : values[k];
        }

        @Override
        public void set(byte value) {

            if (value == 0 && !currentIsRemoved) {
                currentIsRemoved = true;
                removedIndex = indices[k];
                removeNonZero(k--);
            }
            else if (value != 0 && !currentIsRemoved) {
                values[k] = value;
            }
            else {
                currentIsRemoved = false;
                insertNonZero(++k, removedIndex, value);
            }
        }

        @Override
        public boolean hasNext() {

            return k + 1 < cardinality && indices[k + 1] < end;
        }

        @Override
        public Byte next() {

            currentIsRemoved = false;
            return values[++k];
        }

        @Override
        protected int innerCursor() {

            return k;
        }
    }
}
