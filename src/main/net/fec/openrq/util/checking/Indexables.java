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
package net.fec.openrq.util.checking;

/**
 * 
 */
public final class Indexables {

    /**
     * @param from
     *            The starting (inclusive) index of a contiguous region of the indexable object (must be non-negative)
     * @param to
     *            The ending (exclusive) index of a contiguous region of the indexable object (must be at least
     *            {@code from} and at most {@code length})
     * @param length
     *            The length of the indexable object (<b>required to be non-negative</b> - undefined result if negative)
     * @exception IndexOutOfBoundsException
     *                If {@code from < 0 || to < from || arrayLength < to}
     */
    public static void checkFromToBounds(int from, int to, int length) {

        if (to < from) {
            throw new IllegalArgumentException(getIllegalFromToMsg(from, to));
        }
        if (from < 0 || length < to) {
            throw new IndexOutOfBoundsException(getFromToBoundsMsg(from, to, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static String getIllegalFromToMsg(int from, int to) {

        return "to < from (" + to + " < " + from + ")";
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static String getFromToBoundsMsg(int from, int to, int length) {

        return "region from = " + from + "; region to = " + to + "; length = " + length;
    }

    /**
     * @param regionOff
     *            The starting (inclusive) index of a contiguous region of the indexable object (must be non-negative)
     * @param regionLen
     *            The length of the contiguous region of the indexable object (must be non-negative and no larger than
     *            {@code length - regionOff})
     * @param length
     *            The length of the indexable object (<b>required to be non-negative</b> - undefined result if negative)
     * @exception IndexOutOfBoundsException
     *                If {@code off < 0 || len < 0 || len > (length - off)}
     */
    public static void checkOffsetLengthBounds(int regionOff, int regionLen, int length) {

        // retrieved from java.nio.Buffer class
        if ((regionOff | regionLen | (regionOff + regionLen) | (length - (regionOff + regionLen))) < 0) {
            throw new IndexOutOfBoundsException(getOffLenBoundsMsg(regionOff, regionLen, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static String getOffLenBoundsMsg(int off, int len, int length) {

        return "region off = " + off + "; region length = " + len + "; length = " + length;
    }

    /**
     * @param index
     *            The index to be tested (must be non-negative)
     * @param length
     *            The length of the indexable object (<b>required to be non-negative</b> - undefined result if negative)
     * @exception IndexOutOfBoundsException
     *                If {@code index < 0 || index >= length}
     */
    public static void checkIndexBounds(int index, int length) {

        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(getIndexBoundsMsg(index, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static String getIndexBoundsMsg(int index, int length) {

        return "index = " + index + "; length = " + length;
    }

    /**
     * @param length
     *            The length of the indexable object
     */
    public static void checkLengthBounds(int length) {

        if (length < 0) {
            throw new IllegalArgumentException(String.format("negative length: %d", length));
        }
    }

    /**
     * @param length
     *            The length of the indexable object
     * @param lengthName
     *            The name of the length
     */
    public static void checkLengthBounds(int length, String lengthName) {

        if (length < 0) {
            throw new IllegalArgumentException(String.format("negative %s: %d", lengthName, length));
        }
    }

    private Indexables() {

        // not instantiable
    }
}
