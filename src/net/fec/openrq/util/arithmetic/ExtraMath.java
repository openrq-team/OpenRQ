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
package net.fec.openrq.util.arithmetic;


import java.math.BigInteger;


/**
 * Defines arithmetical functions not present in class {@code java.lang.Math}.
 */
public final class ExtraMath {

    /**
     * Returns the ceiling value of an integer division (requires non-negative arguments).
     * 
     * @param num
     *            The numerator
     * @param den
     *            The denominator
     * @return the ceiling value of an integer division
     * @exception ArithmeticException
     *                If the denominator is equal to zero
     */
    public static int ceilDiv(int num, int den) {

        return (int)((num + (den - 1L)) / den);
    }

    /**
     * Returns the ceiling value of a long integer division (requires non-negative arguments).
     * 
     * @param num
     *            The numerator
     * @param den
     *            The denominator
     * @return the ceiling value of a long integer division
     * @exception ArithmeticException
     *                If the denominator is equal to zero
     */
    public static long ceilDiv(long num, long den) {

        if (Long.MAX_VALUE - num < den - 1L) {
            final BigInteger bigNum = BigInteger.valueOf(num);
            final BigInteger bigDen = BigInteger.valueOf(den);
            return bigNum.add(bigDen.subtract(BigInteger.ONE)).divide(bigDen).longValue();
        }
        else {
            return (num + (den - 1L)) / den;
        }
    }

    private ExtraMath() {

        // not instantiable
    }
}
