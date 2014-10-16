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
package net.fec.openrq.parameters;


import static net.fec.openrq.TestingCommon.addInts;
import static net.fec.openrq.TestingCommon.addIntsL;
import static net.fec.openrq.TestingCommon.addLongs;
import static net.fec.openrq.TestingCommon.exponentialDistribution;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


/**
 * Tests some bounds on FEC parameters.
 */
@RunWith(Parameterized.class)
public class FECParametersBoundsTest {

    private static final class Params {

        final long F;
        final int T;
        final int Z;
        final int N;


        Params(long F, int T, int Z, int N) {

            this.F = F;
            this.T = T;
            this.Z = Z;
            this.N = N;
        }

        @Override
        public String toString() {

            return "[F=" + F + "; T=" + T + "; Z=" + Z + "; N=" + N + "]";
        }
    }


    @Parameters(name = "{0}")
    public static Iterable<Object[]> getFECParams() {

        final long maxF = ParameterChecker.maxDataLength();
        final int maxT = ParameterChecker.maxSymbolSize();
        final int maxZ = ParameterChecker.maxNumSourceBlocks();
        final int maxN = ParameterChecker.maxInterleaverLength();

        final Set<Long> Fs = new LinkedHashSet<>();
        addIntsL(Fs, Integer.MIN_VALUE, -1, 0);
        addLongs(Fs, exponentialDistribution(2L, 40));
        addLongs(Fs, maxF, maxF + 1, Long.MAX_VALUE);

        final Set<Integer> Ts = new LinkedHashSet<>();
        addInts(Ts, Integer.MIN_VALUE, -1, 0);
        addInts(Ts, exponentialDistribution(2, 16));
        addInts(Ts, maxT, maxT + 1, Integer.MAX_VALUE);

        final Set<Integer> Zs = new LinkedHashSet<>();
        addInts(Zs, Integer.MIN_VALUE, -1, 0);
        addInts(Zs, exponentialDistribution(2, 8));
        addInts(Zs, maxZ, maxZ + 1, Integer.MAX_VALUE);

        final Set<Integer> Ns = new LinkedHashSet<>();
        addInts(Ns, Integer.MIN_VALUE, -1, 0);
        addInts(Ns, 1);
        addInts(Ns, maxN, maxN + 1, Integer.MAX_VALUE);

        final BufferedObjects<Params> bufferedParams = new BufferedObjects<>();
        int numCombinations = 0;
        for (Long F : Fs) {
            for (Integer T : Ts) {
                for (Integer Z : Zs) {
                    for (Integer N : Ns) {
                        bufferedParams.add(new Params(F, T, Z, N));
                        numCombinations += 4;
                    }
                }
            }
        }

        System.out.println("Testing " + numCombinations + " FEC parameters combinations...");
        return bufferedParams;
    }


    @Parameter(0)
    public List<Params> paramsList;


    @Test
    public void testDataLength() {

        for (Params params : paramsList) {
            final long F = params.F;
            final int T = params.T;
            final int Z = params.Z;
            final int N = params.N;

            assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

            assertTrue(String.format("F is less than its minimum - %s", params),
                F >= ParameterChecker.minDataLength());

            assertTrue(String.format("F is greater than its maximum - %s", params),
                F <= ParameterChecker.maxDataLength());

            assertFalse(String.format("F is out of bounds - %s", params),
                ParameterChecker.isDataLengthOutOfBounds(F));

            assertTrue(String.format("F is greater than its allowed maximum - %s", params),
                F <= ParameterChecker.maxAllowedDataLength(T));
        }
    }

    @Test
    public void testSymbolSize() {

        for (Params params : paramsList) {
            final long F = params.F;
            final int T = params.T;
            final int Z = params.Z;
            final int N = params.N;

            assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

            assertTrue(String.format("T is less than its minimum - %s", params),
                T >= ParameterChecker.minSymbolSize());

            assertTrue(String.format("T is greater than its maximum - %s", params),
                T <= ParameterChecker.maxSymbolSize());

            assertFalse(String.format("T is out of bounds - %s", params),
                ParameterChecker.isDataLengthOutOfBounds(T));

            assertTrue(String.format("T is less than its allowed minimum - %s", params),
                T >= ParameterChecker.minAllowedSymbolSize(F));
        }
    }

    @Test
    public void testNumSourceBlocks() {

        for (Params params : paramsList) {
            final long F = params.F;
            final int T = params.T;
            final int Z = params.Z;
            final int N = params.N;

            assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

            assertTrue(String.format("Z is less than its minimum - %s", params),
                Z >= ParameterChecker.minNumSourceBlocks());
            assertTrue(String.format("Z is greater than its maximum - %s", params),
                Z <= ParameterChecker.maxNumSourceBlocks());

            assertFalse(String.format("Z is out of bounds - %s", params),
                ParameterChecker.isNumSourceBlocksOutOfBounds(Z));

            assertTrue(String.format("Z is less than its allowed minimum - %s", params),
                Z >= ParameterChecker.minAllowedNumSourceBlocks(F, T));

            assertTrue(String.format("Z is greater than its allowed maximum - %s", params),
                Z <= ParameterChecker.maxAllowedNumSourceBlocks(F, T));
        }
    }

    @Test
    public void testInterleaverLength() {

        for (Params params : paramsList) {
            final long F = params.F;
            final int T = params.T;
            final int Z = params.Z;
            final int N = params.N;

            assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

            assertTrue(String.format("N is less than its minimum - %s", params),
                N >= ParameterChecker.minInterleaverLength());

            assertTrue(String.format("N is greater than its maximum - %s", params),
                N <= ParameterChecker.maxInterleaverLength());

            assertFalse(String.format("N is out of bounds - %s", params),
                ParameterChecker.isInterleaverLengthOutOfBounds(N));

            assertTrue(String.format("N is greater than its allowed maximum - %s", params),
                N <= ParameterChecker.maxAllowedInterleaverLength(T));
        }
    }
}
