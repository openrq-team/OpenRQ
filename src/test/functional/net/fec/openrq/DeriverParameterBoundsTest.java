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

import net.fec.openrq.parameters.ParameterChecker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


/**
 * 
 */
@RunWith(Parameterized.class)
public class DeriverParameterBoundsTest {

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

        final List<Object[]> params = new ArrayList<>(Fs.size() * Ps.size() * WSs.size());
        for (Long F : Fs) {
            for (Integer P : Ps) {
                for (Long WS : WSs) {
                    params.add(new Object[] {F, P, WS});
                }
            }
        }

        System.out.println("Testing " + params.size() + " deriver parameters tests...");
        return params;
    }


    @Parameter(0)
    public long F;

    @Parameter(1)
    public int P;

    @Parameter(2)
    public long WS;


    @Test
    public void testDataLength() {

        assumeTrue(ParameterChecker.areValidDeriverParameters(F, P, WS));

        assertTrue("F is less than its minimum", F >= ParameterChecker.minDataLength());
        assertTrue("F is greater than its maximum", F <= ParameterChecker.maxDataLength());
        assertFalse("F is out of bounds", ParameterChecker.isDataLengthOutOfBounds(F));
        assertTrue("F is greater than its allowed maximum given P", F <= ParameterChecker.maxAllowedDataLength(P));
        assertTrue("F is greater than its allowed maximum given P and WS",
            F <= ParameterChecker.maxAllowedDataLength(P, WS));
    }

    @Test
    public void testPayloadLength() {

        assumeTrue(ParameterChecker.areValidDeriverParameters(F, P, WS));

        assertTrue("P is less than its minimum", P >= ParameterChecker.minPayloadLength());
        assertTrue("P is greater than its maximum", P <= ParameterChecker.maxPayloadLength());
        assertFalse("P is out of bounds", ParameterChecker.isPayloadLengthOutOfBounds(P));
        assertTrue("P is less than its allowed minimum", P >= ParameterChecker.minAllowedPayloadLength(F));
    }

    @Test
    public void testMaxDecodingBlockSize() {

        assumeTrue(ParameterChecker.areValidDeriverParameters(F, P, WS));

        assertTrue("WS is less than P", WS >= P);
        assertTrue("WS is less than its minimum", WS >= ParameterChecker.minDecodingBlockSize());
        assertTrue("WS is less than its allowed minimum", WS >= ParameterChecker.minAllowedDecodingBlockSize(F, P));
    }
}
