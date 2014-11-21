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
package net.fec.openrq.util.array;


import java.lang.reflect.Array;

import net.fec.openrq.util.checking.Indexables;


/**
 */
public final class ArrayUtils {

    /**
     * @param componentType
     *            The {@code Class} object representing the component type of the new array
     * @param length
     *            The length of the new array
     * @return a new array with a specific component type and length
     * @exception NullPointerException
     *                If the specified {@code componentType} parameter is null
     * @exception IllegalArgumentException
     *                If {@code componentType} is {@link Void#TYPE}
     * @exception NegativeArraySizeException
     *                If the specified {@code length} is negative
     */
    public static <T> T[] newArray(Class<T> componentType, int length) {

        @SuppressWarnings("unchecked")
        final T[] array = (T[])Array.newInstance(componentType, length);
        return array;
    }

    /**
     * Returns {@code true} iff an array of ints is sorted.
     * 
     * @param array
     *            An array of ints
     * @return {@code true} iff an array of ints is sorted
     */
    public static boolean isSorted(int[] array) {

        return isSorted(array, 0, array.length);
    }

    /**
     * Returns {@code true} iff an array of ints is sorted.
     * 
     * @param array
     *            An array of ints
     * @param from
     *            The starting index (inclusive)
     * @param to
     *            The ending index (exclusive)
     * @return {@code true} iff an array of ints is sorted
     */
    public static boolean isSorted(int[] array, int from, int to) {

        Indexables.checkFromToBounds(from, to, array.length);

        for (int i = from; i < to - 1; i++) {
            if (array[i + 1] < array[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Swaps two non intersecting blocks inside an array of bytes.
     * 
     * @param array
     *            The array
     * @param fromA
     *            The starting index of block A (inclusive)
     * @param toA
     *            The ending index of block B (exclusive)
     * @param fromB
     *            The starting index of block B (inclusive)
     * @param toB
     *            The ending index of block B (exclusive)
     */
    public static void swapBlocks(byte[] array, int fromA, int toA, int fromB, int toB) {

        checkArrayBlocksRanges(fromA, toA, fromB, toB, array.length);

        final int lFrom = Math.min(fromA, fromB);
        final int lTo = Math.min(toA, toB);
        final int rFrom = Math.max(fromA, fromB);
        final int rTo = Math.max(toA, toB);

        reverseBytes(array, lFrom, lTo); // reverse the leftmost block
        reverseBytes(array, lTo, rFrom); // reverse the region between the two blocks
        reverseBytes(array, rFrom, rTo); // reverse the rightmost block

        reverseBytes(array, lFrom, rTo); // reverse the total region spanning the two blocks
    }

    /**
     * Swaps two non intersecting blocks inside an array of ints.
     * 
     * @param array
     *            The array
     * @param fromA
     *            The starting index of block A (inclusive)
     * @param toA
     *            The ending index of block B (exclusive)
     * @param fromB
     *            The starting index of block B (inclusive)
     * @param toB
     *            The ending index of block B (exclusive)
     */
    public static void swapBlocks(int[] array, int fromA, int toA, int fromB, int toB) {

        checkArrayBlocksRanges(fromA, toA, fromB, toB, array.length);

        final int lFrom = Math.min(fromA, fromB);
        final int lTo = Math.min(toA, toB);
        final int rFrom = Math.max(fromA, fromB);
        final int rTo = Math.max(toA, toB);

        reverseInts(array, lFrom, lTo); // reverse the leftmost block
        reverseInts(array, lTo, rFrom); // reverse the region between the two blocks
        reverseInts(array, rFrom, rTo); // reverse the rightmost block

        reverseInts(array, lFrom, rTo); // reverse the total region spanning the two blocks
    }

    private static void checkArrayBlocksRanges(int fromA, int toA, int fromB, int toB, int arrayLen) {

        if (fromA < 0) throw new IndexOutOfBoundsException("fromA < 0");
        if (toA < fromA) throw new IndexOutOfBoundsException("toA < fromA");
        if (arrayLen < toA) throw new IndexOutOfBoundsException("toA > array length");

        if (fromB < 0) throw new IndexOutOfBoundsException("fromB < 0");
        if (toB < fromB) throw new IndexOutOfBoundsException("toB < fromB");
        if (arrayLen < toB) throw new IndexOutOfBoundsException("toB > array length");

        if (Math.max(fromA, fromB) < Math.min(toA, toB)) {
            throw new IllegalArgumentException("the two blocks intersect");
        }
    }

    // from is inclusive, to is exclusive
    private static void reverseBytes(byte[] array, int from, int to) {

        while (from < to) {
            swapBytes(array, from++, --to);
        }
    }

    // from is inclusive, to is exclusive
    private static void reverseInts(int[] array, int from, int to) {

        while (from < to) {
            swapInts(array, from++, --to);
        }
    }

    /**
     * Swaps two bytes in an array of bytes.
     * 
     * @param array
     *            The array of bytes
     * @param a
     *            One of the indices of the values to be swapped
     * @param b
     *            One of the indices of the values to be swapped
     */
    public static void swapBytes(byte[] array, int a, int b) {

        byte tmp = array[a]; // checks for null array and illegal a index
        array[a] = array[b]; // checks for illegal b index before any changes can be made to the array
        array[b] = tmp;
    }

    /**
     * Swaps two ints in an array of ints.
     * 
     * @param array
     *            The array of ints
     * @param a
     *            One of the indices of the values to be swapped
     * @param b
     *            One of the indices of the values to be swapped
     */
    public static void swapInts(int[] array, int a, int b) {

        int tmp = array[a]; // checks for null array and illegal a index
        array[a] = array[b]; // checks for illegal b index before any changes can be made to the array
        array[b] = tmp;
    }

    /**
     * Swaps two longs in an array of longs.
     * 
     * @param array
     *            The array of longs
     * @param a
     *            One of the indices of the values to be swapped
     * @param b
     *            One of the indices of the values to be swapped
     */
    public static void swapLongs(long[] array, int a, int b) {

        long tmp = array[a]; // checks for null array and illegal a index
        array[a] = array[b]; // checks for illegal b index before any changes can be made to the array
        array[b] = tmp;
    }

    /**
     * Swaps two Objects in an array of Objects.
     * 
     * @param array
     *            The array of Objects
     * @param a
     *            One of the indices of the values to be swapped
     * @param b
     *            One of the indices of the values to be swapped
     */
    public static void swapObjects(Object[] array, int a, int b) {

        Object tmp = array[a]; // checks for null array and illegal a index
        array[a] = array[b]; // checks for illegal b index before any changes can be made to the array
        array[b] = tmp;
    }


    /**
     * Provides empty array instances.
     */
    public static final class EmptyArrayOf {

        private static final byte[] BYTES = {};
        private static final char[] CHARS = {};
        private static final short[] SHORTS = {};
        private static final int[] INTS = {};
        private static final long[] LONGS = {};
        private static final float[] FLOATS = {};
        private static final double[] DOUBLES = {};
        private static final Object[] OBJECTS = {};


        /**
         * Returns an empty array of bytes.
         * 
         * @return an empty array of bytes
         */
        public static byte[] bytes() {

            return BYTES;
        }

        /**
         * Returns an empty array of chars.
         * 
         * @return an empty array of chars
         */
        public static char[] chars() {

            return CHARS;
        }

        /**
         * Returns an empty array of shorts.
         * 
         * @return an empty array of shorts
         */
        public static short[] shorts() {

            return SHORTS;
        }

        /**
         * Returns an empty array of ints.
         * 
         * @return an empty array of ints
         */
        public static int[] ints() {

            return INTS;
        }

        /**
         * Returns an empty array of longs.
         * 
         * @return an empty array of longs
         */
        public static long[] longs() {

            return LONGS;
        }

        /**
         * Returns an empty array of floats.
         * 
         * @return an empty array of floats
         */
        public static float[] floats() {

            return FLOATS;
        }

        /**
         * Returns an empty array of doubles.
         * 
         * @return an empty array of doubles
         */
        public static double[] doubles() {

            return DOUBLES;
        }

        /**
         * Returns an empty array of objects.
         * 
         * @param <T>
         *            The container type of the array
         * @return an empty array of objects
         */
        public static <T> T[] objects() {

            @SuppressWarnings("unchecked")
            final T[] array = (T[])OBJECTS;
            return array;
        }
    }


    private ArrayUtils() {

        // not instantiable
    }
}
