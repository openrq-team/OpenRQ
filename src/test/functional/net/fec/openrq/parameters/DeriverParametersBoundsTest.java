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
 * Tests some bounds on deriver parameters.
 */
@RunWith(Parameterized.class)
public class DeriverParametersBoundsTest {

    private static final class Params {

        final long F;
        final int P;
        final long WS;


        Params(long F, int P, long WS) {

            this.F = F;
            this.P = P;
            this.WS = WS;
        }

        @Override
        public String toString() {

            return "[F=" + F + "; P=" + P + "; WS=" + WS + "]";
        }
    }


    @Parameters(name = "F={0}; P={1}; WS={2}")
    public static Iterable<Object[]> getFECParams() {

        final long maxF = ParameterChecker.maxDataLength();
        final int maxP = ParameterChecker.maxSymbolSize();

        final Set<Long> Fs = new LinkedHashSet<>();
        addIntsL(Fs, Integer.MIN_VALUE, -1, 0);
        addLongs(Fs, exponentialDistribution(2L, 40));
        addLongs(Fs, maxF, maxF + 1, Long.MAX_VALUE);

        final Set<Integer> Ps = new LinkedHashSet<>();
        addInts(Ps, Integer.MIN_VALUE, -1, 0);
        addInts(Ps, exponentialDistribution(2, 16));
        addInts(Ps, maxP, maxP + 1, Integer.MAX_VALUE);

        final Set<Long> WSs = new LinkedHashSet<>();
        addLongs(WSs, Long.MIN_VALUE, -1, 0);
        addLongs(WSs, exponentialDistribution(2L, 62));
        addLongs(WSs, Long.MAX_VALUE);

        final BufferedObjects<Params> bufferedParams = new BufferedObjects<>();
        int numCombinations = 0;
        for (Long F : Fs) {
            for (Integer P : Ps) {
                for (Long WS : WSs) {
                    bufferedParams.add(new Params(F, P, WS));
                    numCombinations += 3;
                }
            }
        }

        System.out.println("Testing " + numCombinations + " deriver parameters combinations...");
        return bufferedParams;
    }


    @Parameter(0)
    public List<Params> paramsList;


    @Test
    public void testDataLength() {

        for (Params params : paramsList) {
            final long F = params.F;
            final int P = params.P;
            final long WS = params.WS;

            assumeTrue(ParameterChecker.areValidDeriverParameters(F, P, WS));

            assertTrue(String.format("F is less than its minimum - %s", params),
                F >= ParameterChecker.minDataLength());

            assertTrue(String.format("F is greater than its maximum - %s", params),
                F <= ParameterChecker.maxDataLength());

            assertFalse(String.format("F is out of bounds - %s", params),
                ParameterChecker.isDataLengthOutOfBounds(F));

            assertTrue(String.format("F is greater than its allowed maximum given P - %s", params),
                F <= ParameterChecker.maxAllowedDataLength(P));

            assertTrue(String.format("F is greater than its allowed maximum given P and WS - %s", params),
                F <= ParameterChecker.maxAllowedDataLength(P, WS));
        }
    }

    @Test
    public void testPayloadLength() {

        for (Params params : paramsList) {
            final long F = params.F;
            final int P = params.P;
            final long WS = params.WS;

            assumeTrue(ParameterChecker.areValidDeriverParameters(F, P, WS));

            assertTrue(String.format("P is less than its minimum - %s", params),
                P >= ParameterChecker.minPayloadLength());

            assertTrue(String.format("P is greater than its maximum - %s", params),
                P <= ParameterChecker.maxPayloadLength());

            assertFalse(String.format("P is out of bounds - %s", params),
                ParameterChecker.isPayloadLengthOutOfBounds(P));

            assertTrue(String.format("P is less than its allowed minimum - %s", params),
                P >= ParameterChecker.minAllowedPayloadLength(F));
        }
    }

    @Test
    public void testMaxDecodingBlockSize() {

        for (Params params : paramsList) {
            final long F = params.F;
            final int P = params.P;
            final long WS = params.WS;

            assumeTrue(ParameterChecker.areValidDeriverParameters(F, P, WS));

            assertTrue(String.format("WS is less than P - %s", params),
                WS >= P);

            assertTrue(String.format("WS is less than its minimum - %s", params),
                WS >= ParameterChecker.minDecodingBlockSize());

            assertTrue(String.format("WS is less than its allowed minimum - %s", params),
                WS >= ParameterChecker.minAllowedDecodingBlockSize(F, P));
        }
    }
}
