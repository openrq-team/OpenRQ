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
package net.fec.openrq.util.array;


import java.lang.reflect.Array;


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
     * @param off
     *            An index of the array (must be non-negative)
     * @param len
     *            The length of the array region (must be non-negative and no larger than {@code arrayFence - off})
     * @param arrayFence
     *            The proper length of the array (<b>required to be non-negative</b> - undefined result if negative)
     * @exception IndexOutOfBoundsException
     *                If {@code off < 0 || len < 0 || len > (arrayFence - off)}
     */
    public static void checkArrayBounds(int off, int len, int arrayFence) {

        // retrieved from java.nio.Buffer class
        if ((off | len | (off + len) | (arrayFence - (off + len))) < 0) {
            throw new IndexOutOfBoundsException(getArrayBoundsMsg(off, len, arrayFence));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static String getArrayBoundsMsg(int off, int len, int arrayFence) {

        return "region off = " + off + "; region length = " + len + "; array length = " + arrayFence;
    }

    /**
     * @param index
     *            An index of anything with a length
     * @param length
     *            The length of the object being indexed
     * @exception IndexOutOfBoundsException
     *                If {@code index < 0 || index >= length}
     */
    public static void checkIndexRange(int index, int length) {

        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(getIndexRangeMsg(index, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static String getIndexRangeMsg(int index, int length) {

        return "index = " + index + "; length = " + length;
    }


    /**
     * Provides empty array instances.
     */
    public static final class EmptyArrayOf {

        private static final byte[] BYTES = new byte[0];
        private static final char[] CHARS = new char[0];
        private static final short[] SHORTS = new short[0];
        private static final int[] INTS = new int[0];
        private static final long[] LONGS = new long[0];
        private static final float[] FLOATS = new float[0];
        private static final double[] DOUBLES = new double[0];
        private static final Object[] OBJECTS = new Object[0];


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

    private ArrayUtils() {

        // not instantiable
    }
}
