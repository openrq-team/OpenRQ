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
package net.fec.openrq.util.math;


import java.math.BigInteger;


/**
 * Defines arithmetical functions not present in class {@code java.lang.Math}.
 */
public final class ExtraMath {

    /**
     * <b>NOTE: Copied from {@code java.lang.Math} in Java 8.</b>
     * <p>
     * Returns the sum of its arguments, throwing an exception if the result overflows an {@code int}.
     * 
     * @param x
     *            the first value
     * @param y
     *            the second value
     * @return the result
     * @throws ArithmeticException
     *             if the result overflows an int
     */
    public static int addExact(int x, int y) {

        int r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            throw new ArithmeticException("integer overflow");
        }
        return r;
    }

    /**
     * <b>NOTE: Copied from {@code java.lang.Math} in Java 8.</b>
     * <p>
     * Returns the sum of its arguments, throwing an exception if the result overflows a {@code long}.
     * 
     * @param x
     *            the first value
     * @param y
     *            the second value
     * @return the result
     * @throws ArithmeticException
     *             if the result overflows a long
     */
    public static long addExact(long x, long y) {

        long r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            throw new ArithmeticException("long overflow");
        }
        return r;
    }

    /**
     * <b>NOTE: Copied from {@code java.lang.Math} in Java 8.</b>
     * <p>
     * Returns the product of the arguments,
     * throwing an exception if the result overflows an {@code int}.
     * 
     * @param x
     *            the first value
     * @param y
     *            the second value
     * @return the result
     * @throws ArithmeticException
     *             if the result overflows an int
     */
    public static int multiplyExact(int x, int y) {

        long r = (long)x * (long)y;
        if ((int)r != r) {
            throw new ArithmeticException("integer overflow");
        }
        return (int)r;
    }

    /**
     * <b>NOTE: Copied from {@code java.lang.Math} in Java 8.</b>
     * <p>
     * Returns the product of the arguments,
     * throwing an exception if the result overflows a {@code long}.
     * 
     * @param x
     *            the first value
     * @param y
     *            the second value
     * @return the result
     * @throws ArithmeticException
     *             if the result overflows a long
     */
    public static long multiplyExact(long x, long y) {

        long r = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if (((ax | ay) >>> 31 != 0)) {
            // Some bits greater than 2^31 that might cause overflow
            // Check the result using the divide operator
            // and check for the special case of Long.MIN_VALUE * -1
            if (((y != 0) && (r / y != x)) ||
                (x == Long.MIN_VALUE && y == -1)) {
                throw new ArithmeticException("long overflow");
            }
        }
        return r;
    }

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

        return (int)((num + (den - 1L)) / den); // there is an implicit cast to long to prevent integer overflow
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

        if (Long.MAX_VALUE - num < den - 1L) { // if num + (den - 1) overflows
            final BigInteger bigNum = BigInteger.valueOf(num);
            final BigInteger bigDen = BigInteger.valueOf(den);
            return bigNum.add(bigDen.subtract(BigInteger.ONE)).divide(bigDen).longValue();
        }
        else {
            return (num + (den - 1L)) / den;
        }
    }

    /**
     * Returns the (modular in case of overflow) integer power.
     * 
     * @param base
     *            The power base
     * @param exp
     *            The power exponent
     * @return base^^exp
     * @exception IllegalArgumentException
     *                If the exponent is negative or if both base and exponent are equal to zero
     */
    public static int integerPow(int base, int exp) {

        if (exp < 0) throw new IllegalArgumentException("exponent must be non-negative");
        if (base == 0) {
            if (exp == 0) throw new IllegalArgumentException("0^^0 is undefined");
            else return 0;
        }

        // exponentiation by squaring

        int result = 1;
        while (exp != 0)
        {
            if ((exp & 1) == 1) {
                result *= base;
            }
            exp >>= 1;
            base *= base;
        }

        return result;
    }

    /**
     * Returns the (modular in case of overflow) long integer power.
     * 
     * @param base
     *            The power base
     * @param exp
     *            The power exponent
     * @return base^^exp
     * @exception IllegalArgumentException
     *                If the exponent is negative or if both base and exponent are equal to zero
     */
    public static long integerPow(long base, long exp) {

        if (exp < 0) throw new IllegalArgumentException("exponent must be non-negative");
        if (base == 0) {
            if (exp == 0) throw new IllegalArgumentException("0^^0 is undefined");
            else return 0;
        }

        // exponentiation by squaring

        long result = 1;
        while (exp != 0)
        {
            if ((exp & 1) == 1) {
                result *= base;
            }
            exp >>= 1;
            base *= base;
        }

        return result;
    }

    private ExtraMath() {

        // not instantiable
    }
}
