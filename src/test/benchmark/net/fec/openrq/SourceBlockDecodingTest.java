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

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;


@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
public class SourceBlockDecodingTest {

    // adjust these values for variable tests
    private static final int F = 9_999;
    private static final int K = 250;
    private static final int EXTRA_SYMBOLS = 0;
    private static final boolean PREFER_SOURCE_SYMBOLS = false;


    private static ArraySourceBlockDecoder newRandomSBDecoder() {

        // force single source block
        final FECParameters fecParams = FECParameters.newParameters(F, ceilDiv(F, K), 1);
        final Random rand = TestingCommon.newSeededRandom();

        final byte[] data = TestingCommon.randomBytes(F, rand);
        final Set<Integer> esis = randomESIs(rand);
        final SourceBlockEncoder enc = OpenRQ.newEncoder(data, fecParams).sourceBlock(0);

        final ArraySourceBlockDecoder dec = (ArraySourceBlockDecoder) /* safe cast */
                                            OpenRQ.newDecoder(fecParams, EXTRA_SYMBOLS).sourceBlock(0);

        for (int esi : esis) {
            if (dec.putEncodingPacket(enc.encodingPacket(esi)) == SourceBlockState.DECODING_FAILURE) {
                throw new IllegalStateException("decoding failed, try using a different set of symbols");
            }
        }
        return dec;
    }

    private static Set<Integer> randomESIs(Random rand) {

        final int numSymbols = K + EXTRA_SYMBOLS;
        if (PREFER_SOURCE_SYMBOLS) {
            return TestingCommon.randomSrcRepESIs(rand, numSymbols, K);
        }
        else {
            return TestingCommon.randomAnyESIs(rand, numSymbols);
        }
    }


    private ArraySourceBlockDecoder dec;


    @Setup
    public void setup() {

        dec = newRandomSBDecoder();
    }

    @GenerateMicroBenchmark
    public void test() {

        ArraySourceBlockDecoder.forceDecode(dec);
    }

    // for CPU/memory profiling
    public static void main(String[] args) {

        final ArraySourceBlockDecoder dec = newRandomSBDecoder();
        final int iters = 500;
        for (int i = 0; i < iters; i++) {
            ArraySourceBlockDecoder.forceDecode(dec);
            System.out.println(i);
        }
    }
}
