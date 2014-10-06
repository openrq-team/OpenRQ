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


import static net.fec.openrq.util.arithmetic.ExtraMath.ceilDiv;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;


@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@Fork(0)
@State(Scope.Benchmark)
public class SourceBlockDecodingTest {

    // change this to use a larger number of source symbols
    private static final boolean PREFER_SOURCE_SYMBOLS = false;

    // default parameter values
    private static final int DEF_DATA_LEN = 9_999;
    private static final int DEF_NUM_SOURCE_SYMBOLS = 250;
    private static final int DEF_EXTRA_SYMBOLS = 0;


    private static ArraySourceBlockDecoder newRandomSBDecoder(int F, int K, int overhead) {

        TestingCommon.checkParamsForSingleSourceBlockData(F, K);
        if (overhead < 0) throw new IllegalArgumentException("symbol overhead must be non-negative");

        // force single source block
        final FECParameters fecParams = FECParameters.newParameters(F, ceilDiv(F, K), 1);
        final Random rand = TestingCommon.newSeededRandom();

        final byte[] data = TestingCommon.randomBytes(F, rand);
        final int numESIs = K + overhead;
        final Set<Integer> esis = randomESIs(rand, K, numESIs);
        final SourceBlockEncoder enc = OpenRQ.newEncoder(data, fecParams).sourceBlock(0);

        final ArraySourceBlockDecoder dec = (ArraySourceBlockDecoder) /* safe cast */
                                            OpenRQ.newDecoder(fecParams, overhead).sourceBlock(0);

        for (int esi : esis) {
            if (dec.putEncodingPacket(enc.encodingPacket(esi)) == SourceBlockState.DECODING_FAILURE) {
                throw new IllegalStateException("decoding failed, try using a different set of symbols");
            }
        }
        return dec;
    }

    private static Set<Integer> randomESIs(Random rand, int K, int numESIs) {

        if (PREFER_SOURCE_SYMBOLS) {
            return TestingCommon.randomSrcRepESIs(rand, numESIs, K);
        }
        else {
            return TestingCommon.randomAnyESIs(rand, numESIs);
        }
    }


    @Param({"" + DEF_DATA_LEN})
    private int datalen;

    @Param({"" + DEF_NUM_SOURCE_SYMBOLS})
    private int srcsymbs;

    @Param({"" + DEF_EXTRA_SYMBOLS})
    private int symbover;

    private ArraySourceBlockDecoder dec;


    public SourceBlockDecodingTest() {

        this.datalen = DEF_DATA_LEN;
        this.srcsymbs = DEF_NUM_SOURCE_SYMBOLS;
        this.symbover = DEF_EXTRA_SYMBOLS;

        this.dec = null;
    }

    @Setup
    public void setup() {

        dec = newRandomSBDecoder(datalen, srcsymbs, symbover);
    }

    @Benchmark
    public void test() {

        ArraySourceBlockDecoder.forceDecode(dec);
    }

    // for CPU/memory profiling
    public static void main(String[] args) {

        final SourceBlockDecodingTest test = new SourceBlockDecodingTest();
        test.setup();
        final int iters = 500;
        for (int i = 0; i < iters; i++) {
            test.test();
            System.out.println(i);
        }
    }
}
