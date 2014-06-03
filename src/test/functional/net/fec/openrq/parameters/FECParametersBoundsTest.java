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
package net.fec.openrq.parameters;


import static net.fec.openrq.TestingCommon.addInts;
import static net.fec.openrq.TestingCommon.addIntsL;
import static net.fec.openrq.TestingCommon.addLongs;
import static net.fec.openrq.TestingCommon.exponentialDistribution;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
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

    @Parameters(name = "F={0}; T={1}; Z={2}; N={3}")
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

        final List<Object[]> params = new ArrayList<>(Fs.size() * Ts.size() * Zs.size() * Ns.size());
        for (Long F : Fs) {
            for (Integer T : Ts) {
                for (Integer Z : Zs) {
                    for (Integer N : Ns) {
                        params.add(new Object[] {F, T, Z, N});
                    }
                }
            }
        }

        System.out.println("Testing " + 4 * params.size() + " FEC parameters tests...");
        return params;
    }


    @Parameter(0)
    public long F;

    @Parameter(1)
    public int T;

    @Parameter(2)
    public int Z;

    @Parameter(3)
    public int N;


    @Test
    public void testDataLength() {

        assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

        assertTrue("F is less than its minimum", F >= ParameterChecker.minDataLength());
        assertTrue("F is greater than its maximum", F <= ParameterChecker.maxDataLength());
        assertFalse("F is out of bounds", ParameterChecker.isDataLengthOutOfBounds(F));
        assertTrue("F is greater than its allowed maximum", F <= ParameterChecker.maxAllowedDataLength(T));
    }

    @Test
    public void testSymbolSize() {

        assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

        assertTrue("T is less than its minimum", T >= ParameterChecker.minSymbolSize());
        assertTrue("T is greater than its maximum", T <= ParameterChecker.maxSymbolSize());
        assertFalse("T is out of bounds", ParameterChecker.isDataLengthOutOfBounds(T));
        assertTrue("T is less than its allowed minimum", T >= ParameterChecker.minAllowedSymbolSize(F));
    }

    @Test
    public void testNumSourceBlocks() {

        assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

        assertTrue("Z is less than its minimum", Z >= ParameterChecker.minNumSourceBlocks());
        assertTrue("Z is greater than its maximum", Z <= ParameterChecker.maxNumSourceBlocks());
        assertFalse("Z is out of bounds", ParameterChecker.isNumSourceBlocksOutOfBounds(Z));
        assertTrue("Z is less than its allowed minimum", Z >= ParameterChecker.minAllowedNumSourceBlocks(F, T));
        assertTrue("Z is greater than its allowed maximum", Z <= ParameterChecker.maxAllowedNumSourceBlocks(F, T));
    }

    @Test
    public void testInterleaverLength() {

        assumeTrue(ParameterChecker.areValidFECParameters(F, T, Z, N));

        assertTrue("N is less than its minimum", N >= ParameterChecker.minInterleaverLength());
        assertTrue("N is greater than its maximum", N <= ParameterChecker.maxInterleaverLength());
        assertFalse("N is out of bounds", ParameterChecker.isInterleaverLengthOutOfBounds(N));
        assertTrue("N is greater than its allowed maximum", N <= ParameterChecker.maxAllowedInterleaverLength(T));
    }
}
