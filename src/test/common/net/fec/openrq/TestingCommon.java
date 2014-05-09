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
package net.fec.openrq;


import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.arithmetic.ExtraMath;


/**
 * Contains useful constants and utility methods for the test classes.
 */
final class TestingCommon {

    private static final long RAND_SEED = 42L;


    static Random newRandom() {

        return new Random();
    }

    static Random newSeededRandom() {

        return new Random(RAND_SEED);
    }

    static byte[] randomBytes(int size, Random rand) {

        final byte[] bs = new byte[size];
        rand.nextBytes(bs);
        return bs;
    }

    static Set<Integer> randomAnyESIs(Random rand, int numSymbols) {

        final int maxESI = ParameterChecker.maxEncodingSymbolID();
        final Set<Integer> esis = new LinkedHashSet<>(); // preserve randomized ordering

        // Floyd's Algorithm for random sampling (uniform over all possible ESIs)
        for (int i = maxESI - numSymbols; i < maxESI; i++) {
            // try to add a random index between 0 and i (inclusive)
            if (!esis.add(rand.nextInt(i + 1))) {
                // if already present, choose index i, which is surely not present yet
                esis.add(i);
            }
        }

        return esis;
    }

    static Set<Integer> randomSrcRepESIs(Random rand, int numSymbols, int K) {

        final int maxESI = ParameterChecker.maxEncodingSymbolID();
        final Set<Integer> esis = new LinkedHashSet<>(); // preserve randomized ordering

        while (esis.size() < numSymbols) {
            // exponential distribution with mean K/2
            final int esi = (int)((-K / 2D) * Math.log(1 - rand.nextDouble()));
            // repeat sampling if a repeated ESI is obtained
            esis.add(Math.min(esi, maxESI));
        }

        return esis;
    }

    static int[] primeAndPowerDistribution(int base, int maxExponent) {

        ExtraMath.integerPow(base, maxExponent); // test a power calculation to validate arguments
        if (base <= 0) throw new IllegalArgumentException("base must be positive");

        final Sieve sieve = new Sieve();

        // distribution.length == approx. number of primes + number of powers
        // distribution.length == approx. 1 + 2 + ... + maxExponent + (maxExponent+1)
        final LinkedHashSet<Integer> distribution = new LinkedHashSet<>(((maxExponent + 1) * (maxExponent + 2)) / 2);

        for (int exp = 0; exp < maxExponent; exp++) {
            // this provides a sort of uniform distribution of primes of limited size
            final int increment = (exp == 0) ? 1 : ((1 << (exp + 1)) - (1 << exp)) / exp;

            // prepare the prime number search
            int n = ExtraMath.integerPow(base, exp);

            // add the current power to the distribution
            distribution.add(n);

            // collect some primes between current and next power
            final int fence = ExtraMath.integerPow(base, exp + 1);
            while (n < fence) {
                n = sieve.nextPrimeInclusive(n);
                if (n < fence) {
                    // add the collected prime to the distribution
                    distribution.add(n);
                }
                n += increment;
            }
        }

        // add the last power to the distribution
        distribution.add(ExtraMath.integerPow(base, maxExponent));

        final int[] intDist = new int[distribution.size()];
        int idx = 0;
        for (int value : distribution) {
            intDist[idx++] = value;
        }
        return intDist;
    }


    /**
     * Class that implements Sieve of Eratosthenes to check and find prime numbers.
     */
    private static final class Sieve {

        // Bit set where 0 means prime, 1 means composite
        // Keeps odd numbers only, so index == n / 2
        // First index is never accessed, so there is no error for n = {0, 1, 2}
        private final BitSet bitset;

        // The last number in the bit set (not necessarily the last index in the bit set)
        private int last;


        Sieve() {

            // we expect isPrime queries for N <= 2^^20
            this.bitset = new BitSet(1 >> 20);
            this.last = 3;
        }

        int nextPrimeInclusive(int n) {

            if (n <= 2) return 2;

            // begin at an odd number
            for (int nn = (n % 2 == 0) ? n + 1 : n; true; nn += 2) {
                if (isPrime(nn)) {
                    return nn;
                }
            }
        }

        // A note on micro-optimizations: division and modulo by 2 are usually
        // converted by the compiler to an unsigned right shift and an AND 1,
        // respectively.
        boolean isPrime(final int n) {

            // trivial cases
            if (n == 2) return true;
            if (n % 2 == 0 || n < 3) return false;

            // not yet cached
            if (n > last) {
                // mark in the bit set composite numbers that are multiples of ii
                for (int ii = 3; ii <= n / 2; ii += 2) {
                    // if ii is prime, then mark all multiples of ii as composite in the bit set
                    if (isCachedPrime(ii)) {
                        // find the greatest multiple of ii, that is <= last (to avoid repeating markings)
                        int firstMultiple = (last / ii) * ii;
                        if (firstMultiple <= ii) { // at first this will be true
                            firstMultiple = ii * 2;
                        }

                        markMultiples(ii, firstMultiple, n);
                    }
                }
                // update the last number that was processed
                last = n;
            }

            return isCachedPrime(n);
        }

        private void markMultiples(int prime, int startAt, int endAt) {

            for (int mult = startAt; mult <= endAt; mult += prime) {
                markAsComposite(mult);
            }
        }

        private void markAsComposite(final int n) {

            // only odd numbers in bit set
            if (n % 2 != 0) {
                bitset.set(n / 2, true);
            }
        }

        private boolean isCachedPrime(int n) {

            return !bitset.get(n / 2);
        }
    }


    private TestingCommon() {

        // not instantiable
    }
}
