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
     * @param clazz
     * @param length
     * @return a new array from a specific class with the specified length
     */
    public static <T> T[] newArray(Class<T> clazz, int length) {

        @SuppressWarnings("unchecked")
        final T[] array = (T[])Array.newInstance(clazz, length);
        return array;
    }

    /**
     * @param arrOff
     * @param arrLen
     * @param length
     * @exception IndexOutOfBoundsException
     */
    public static void checkArrayBounds(int arrOff, int arrLen, int length) {

        // retrieved from java.nio.Buffer class
        if ((arrOff | arrLen | (arrOff + arrLen) | (length - (arrOff + arrLen))) < 0) {
            throw new IndexOutOfBoundsException(getArrayBoundsMsg(arrOff, arrLen, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static String getArrayBoundsMsg(int off, int len, int arrLength) {

        return "region off = " + off + "; region length = " + len + "; array length = " + arrLength;
    }

    /**
     * @param index
     * @param length
     * @exception IndexOutOfBoundsException
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

    private ArrayUtils() {

        // not instantiable
    }
}
