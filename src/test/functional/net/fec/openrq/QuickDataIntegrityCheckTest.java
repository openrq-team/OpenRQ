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
import net.fec.openrq.util.math.ExtraMath;
import net.fec.openrq.util.rq.SystematicIndices;

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
public final class QuickDataIntegrityCheckTest {

    private static int MAX_EXTRA_SYMBOLS;
    private static Random RAND;


    @BeforeClass
    public static void initStaticParameters() {

        MAX_EXTRA_SYMBOLS = 2;
        RAND = TestingCommon.newSeededRandom();
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getFECParams() {

        final int maxK = 1000;
        final int T = 1500;
        final int Z = 1;

        final int maxKprime = SystematicIndices.ceil(maxK);
        final int maxK_index = SystematicIndices.getKIndex(maxKprime);
        final int minK_index = 0;

        final List<Object[]> params = new ArrayList<>(maxK_index + 1);

        // traverse all Kprime values from maxK_index to minK_index
        for (int K_index = maxK_index; K_index >= minK_index; K_index--) {
            final int Kprime = SystematicIndices.K(K_index);
            final int F = ExtraMath.multiplyExact(T, Kprime);
            params.add(new Object[] {FECParameters.newParameters(F, T, Z)});
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
