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
 * Contributor(s): Ewald Grusk
 * Yuriy Drozd
 * Maxim Samoylov
 */
package net.fec.openrq.util.linearalgebra.vector.sparse;


import static net.fec.openrq.util.math.OctetOps.aDividedByB;
import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static net.fec.openrq.util.math.OctetOps.aTimesB;

import java.util.Arrays;

import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.datatype.UnsignedTypes;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;
import net.fec.openrq.util.math.ExtraMath;


public class CompressedByteVector extends SparseByteVector {

    private static final int DEFAULT_CAPACITY = 8;

    private static final int INDEX_SHIFT = Byte.SIZE;
    private static final int INDEX_MASK = ((int)UnsignedTypes.MAX_UNSIGNED_INT_VALUE) << INDEX_SHIFT;
    private static final int MAX_LENGTH = 1 << (31 - INDEX_SHIFT);


    private static int toNonZero(byte value, int index) {

        return (index << INDEX_SHIFT) | UnsignedTypes.getUnsignedByte(value);
    }

    private static byte getValue(int nonzero) {

        return (byte)UnsignedTypes.getUnsignedByte(nonzero);
    }

    private static int getIndex(int nonzero) {

        return nonzero >>> INDEX_SHIFT;
    }

    private static int updateValue(int nonzero, byte value) {

        return (nonzero & INDEX_MASK) | UnsignedTypes.getUnsignedByte(value);
    }

    private static int updateIndex(int nonzero, int index) {

        return (index << INDEX_SHIFT) | UnsignedTypes.getUnsignedByte(nonzero);
    }


    private int nonzeros[]; // must always be sorted


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
                nonzeros[cardinality] = toNonZero(value, i);
                cardinality++;
            }
        }
    }

    public CompressedByteVector(int length, int cardinality, byte values[], int indices[]) {

        this(length, cardinality);

        for (int i = 0; i < cardinality; i++) {
            nonzeros[i] = toNonZero(values[i], indices[i]);
        }
    }

    public CompressedByteVector(int length, int cardinality) {

        super(length, cardinality);

        if (length > MAX_LENGTH) fail("Maximum length exceeded.");
        this.nonzeros = new int[cardinality];
    }

    private CompressedByteVector(int length, int cardinality, int[] nonzeros) {

        super(length, cardinality);

        this.nonzeros = nonzeros;
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

        int low = 0;
        int high = cardinality - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midInd = getIndex(nonzeros[mid]);

            if (midInd < i) {
                low = mid + 1;
            }
            else if (midInd > i) {
                high = mid - 1;
            }
            else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
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

        ensureFactoryIsNotNull(factory);
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
    public void multiplyInPlace(byte value) {

        if (value == 0) {
            clear();
        }
        else {
            ByteVectorIterator it = nonZeroIterator();
            while (it.hasNext()) {
                it.next();
                it.set(aTimesB(it.get(), value));
            }
        }
    }

    @Override
    public ByteVector multiply(ByteMatrix matrix) {

        return multiply(matrix, factory());
    }

    @Override
    public ByteVector multiply(ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
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

    @Override
    public ByteVector multiply(ByteMatrix matrix, int fromIndex, int toIndex) {

        return multiply(matrix, fromIndex, toIndex, factory());
    }

    @Override
    public ByteVector multiply(ByteMatrix matrix, int fromIndex, int toIndex, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");
        Indexables.checkFromToBounds(fromIndex, toIndex, length());

        if ((toIndex - fromIndex) != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + (toIndex - fromIndex) + "x_.");
        }

        ByteVector result = factory.createVector(matrix.columns());

        for (int j = 0; j < matrix.columns(); j++) {
            byte acc = 0;

            ByteVectorIterator it = nonZeroIterator(fromIndex, toIndex);
            while (it.hasNext()) {
                it.next();
                final byte prod = aTimesB(it.get(), matrix.get(it.index() - fromIndex, j));
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
    public void divideInPlace(byte value) {

        if (value != 1) {
            ByteVectorIterator it = nonZeroIterator();
            while (it.hasNext()) {
                it.next();
                it.set(aDividedByB(it.get(), value));
            }
        }
    }

    @Override
    public void divideInPlace(byte value, int fromIndex, int toIndex) {

        Indexables.checkFromToBounds(fromIndex, toIndex, length());

        if (value != 1) {
            ByteVectorIterator it = nonZeroIterator(fromIndex, toIndex);
            while (it.hasNext()) {
                it.next();
                it.set(aDividedByB(it.get(), value));
            }
        }
    }

    @Override
    public void update(int i, VectorFunction function) {

        Indexables.checkIndexBounds(i, length());
        SearchEntry entry = searchByIndex(i);
        entry.update(function.evaluate(i, entry.value()));
    }

    @Override
    public int[] nonZeroPositions() {

        return extractIndices(0, cardinality);
    }

    @Override
    public int[] nonZeroPositions(int from, int to) {

        Indexables.checkFromToBounds(from, to, length());
        SearchRange range = searchByRange(from, to);
        return extractIndices(range.fromK(), range.toK());
    }

    private int[] extractIndices(int fromK, int toK) {

        final int[] indices = new int[toK - fromK];
        for (int i = fromK; i < toK; i++) {
            indices[i - fromK] = getIndex(nonzeros[i]);
        }

        return indices;
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
                final byte leftValue = getValue(nonzeros[leftPos]);
                final byte rightValue = getValue(nonzeros[rightPos]);
                nonzeros[leftPos] = updateValue(nonzeros[leftPos], rightValue);
                nonzeros[rightPos] = updateValue(nonzeros[rightPos], leftValue);
            }
            else if (hasEntry(leftPos)) { // if only the left position was found
                // store temporarily the current value at leftPos
                int prevLeftNonzero = nonzeros[leftPos];

                // the new position of the element at leftPos is the position
                // immediately before the insertion position of rightPos
                final int newLeftPos = getEntry(rightPos) - 1;

                // shift the elements between leftPos and newLeftPos by one to the left
                System.arraycopy(nonzeros, leftPos + 1, nonzeros, leftPos, newLeftPos - leftPos);

                // update the element at newLeftPos with the new value/position
                nonzeros[newLeftPos] = updateIndex(prevLeftNonzero, right);
            }
            else if (hasEntry(rightPos)) { // if only the right position was found
                // store temporarily the current value at rightPos
                int prevRightNonzero = nonzeros[rightPos];

                // the new position of the element at rightPos is the insertion position of leftPos
                final int newRightPos = getEntry(leftPos);

                // shift the elements between newRightPos and rightPos by one to the right
                System.arraycopy(nonzeros, newRightPos, nonzeros, newRightPos + 1, rightPos - newRightPos);

                // update the element at newRightPos with the new value/position
                nonzeros[newRightPos] = updateIndex(prevRightNonzero, left);
            }
            // else nothing needs to be swapped
        }
    }

    @Override
    public ByteVector resize(int $length) {

        ensureLengthIsCorrect($length);

        final int $cardinality = ($length >= this.length()) ? this.cardinality : getEntry(binarySearch($length));
        final int $nonzeros[] = Arrays.copyOf(this.nonzeros, $cardinality);

        return new CompressedByteVector($length, $cardinality, $nonzeros);
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

            System.arraycopy(nonzeros, k, nonzeros, k + 1, cardinality - k);

            nonzeros[k] = toNonZero(value, i);
            cardinality++;
        }
    }

    private void removeNonZero(int k) {

        cardinality--;
        System.arraycopy(nonzeros, k + 1, nonzeros, k, cardinality - k);
    }

    // requires non-negative extraElements
    private void ensureCapacity(int extraElements) {

        final int minCapacity = ExtraMath.addExact(cardinality, extraElements);
        final int oldCapacity = nonzeros.length;

        if (minCapacity > oldCapacity) {
            final int newCapacity = getNewCapacity(minCapacity, oldCapacity);
            final int[] newNonzeros = new int[newCapacity];
            System.arraycopy(nonzeros, 0, newNonzeros, 0, cardinality);
            nonzeros = newNonzeros;
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

            return isNonZero() ? getValue(nonzeros[k]) : 0;
        }

        void update(byte value) {

            if (isNonZero()) {
                if (value != 0) {
                    nonzeros[k] = updateValue(nonzeros[k], value);
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

            return isCurrentNonZero() ? getValue(nonzeros[k]) : 0;
        }

        @Override
        public void set(byte value) {

            if (isCurrentNonZero()) {
                if (value != 0) {
                    nonzeros[k] = updateValue(nonzeros[k], value);
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

            return k < cardinality && getIndex(nonzeros[k]) == i;
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

            return currentIsRemoved ? removedIndex : getIndex(nonzeros[k]);
        }

        @Override
        public byte get() {

            return currentIsRemoved ? 0 : getValue(nonzeros[k]);
        }

        @Override
        public void set(byte value) {

            if (value == 0 && !currentIsRemoved) {
                currentIsRemoved = true;
                removedIndex = getIndex(nonzeros[k]);
                removeNonZero(k--);
            }
            else if (value != 0 && !currentIsRemoved) {
                nonzeros[k] = updateValue(nonzeros[k], value);
            }
            else {
                currentIsRemoved = false;
                insertNonZero(++k, removedIndex, value);
            }
        }

        @Override
        public boolean hasNext() {

            return k + 1 < cardinality && getIndex(nonzeros[k + 1]) < end;
        }

        @Override
        public Byte next() {

            currentIsRemoved = false;
            return getValue(nonzeros[++k]);
        }

        @Override
        protected int innerCursor() {

            return k;
        }
    }
}
