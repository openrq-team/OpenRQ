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


import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.arithmetic.ExtraMath;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


/**
 * Encodes a data object and decodes it, then checks if the decoded matches the original.
 */
@RunWith(Parameterized.class)
public final class DataIntegrityCheckTest {

    private static int MAX_EXTRA_SYMBOLS;
    private static Random RAND;


    @BeforeClass
    public static void initStaticParameters() {

        MAX_EXTRA_SYMBOLS = 2;
        RAND = TestingCommon.newSeededRandom();
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getFECParams() {

        final int[] Fs = TestingCommon.primeExponentialDistribution(2, 15);
        final int[] Ks = TestingCommon.primeExponentialDistribution(10, 2);
        final int[] Zs = TestingCommon.primeExponentialDistribution(2, 3);
        final int N = 1;

        // an estimation of how many sets of FEC parameters will be needed (not every combination is valid)
        final List<Object[]> params = new ArrayList<>((Fs.length * Ks.length * Zs.length) / 2);

        // fill the list with FEC parameters of many combinations
        // start with the highest parameters so we can perceive immediately the maximum time per test
        for (int f = Fs.length - 1; f >= 0; f--) {
            final int F = Fs[f];

            for (int k = Ks.length - 1; k >= 0; k--) {
                final int K = Ks[k];

                for (int z = Zs.length - 1; z >= 0; z--) {
                    final int Z = Zs[z];

                    final int T = ExtraMath.ceilDiv(F, K);
                    if (ParameterChecker.areValidFECParameters(F, T, Z, N)) {
                        params.add(new Object[] {FECParameters.newParameters(F, T, Z)});
                    }
                }
            }
        }

        System.out.println("Testing " + 2 * params.size() + " data integrity tests...");
        return params;
    }


    @Parameter(0)
    public FECParameters fecParams;


    @Test
    public void checkDataWithSourceSymbols() {

        final byte[] data = TestingCommon.randomBytes(fecParams.dataLengthAsInt(), RAND);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
        final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, 0);

        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            final SourceBlockDecoder sbDec = dec.sourceBlock(sbEnc.sourceBlockNumber());
            for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                sbDec.putEncodingPacket(srcPacket);
            }
        }

        // compare the original and decoded data
        assertArrayEquals(data, dec.dataArray());
    }

    @Test
    public void checkDataWithRandomSymbols() {

        final byte[] data = TestingCommon.randomBytes(fecParams.dataLengthAsInt(), RAND);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
        final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, 0);

        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            final int K = sbEnc.numberOfSourceSymbols();
            final Set<Integer> esis = TestingCommon.randomAnyESIs(RAND, K + MAX_EXTRA_SYMBOLS);
            final Iterator<Integer> esiIter = esis.iterator();
            SourceBlockState sbState = SourceBlockState.INCOMPLETE;

            // this should loop K times
            while (sbState == SourceBlockState.INCOMPLETE) {
                final EncodingPacket packet = sbEnc.encodingPacket(esiIter.next());
                final SourceBlockDecoder sbDec = dec.sourceBlock(packet.sourceBlockNumber());
                sbState = sbDec.putEncodingPacket(packet);
            }

            // if the decoding failed, we need more symbols
            while (sbState != SourceBlockState.DECODED && esiIter.hasNext()) {
                final EncodingPacket packet = sbEnc.encodingPacket(esiIter.next());
                final SourceBlockDecoder sbDec = dec.sourceBlock(packet.sourceBlockNumber());
                sbState = sbDec.putEncodingPacket(packet);
            }

            // if the source block is still not decoded, then we ignore this test
            Assume.assumeTrue(sbState == SourceBlockState.DECODED);
        }

        // compare the original and decoded data
        assertArrayEquals(data, dec.dataArray());
    }
}
