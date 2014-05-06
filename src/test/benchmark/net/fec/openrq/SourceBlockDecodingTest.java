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

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;

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
    private static final long RANDOM_SEED = 5687047556870475L;
    private static final boolean PREFER_SOURCE_SYMBOLS = false;


    private static ArraySourceBlockDecoder newRandomSBDecoder() {

        // force single source block
        final FECParameters fecParams = FECParameters.newParameters(F, ceilDiv(F, K), 1);
        final Random rand = new Random(RANDOM_SEED);

        final byte[] data = generateRandomData(rand, F);
        final Set<Integer> esis = generateRandomSymbolESIs(rand, K + EXTRA_SYMBOLS, PREFER_SOURCE_SYMBOLS);
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

    private static byte[] generateRandomData(Random rand, int size) {

        final byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }

    private static Set<Integer> generateRandomSymbolESIs(Random rand, int numSymbols, boolean preferSourceSymbols) {

        final int maxESI = ParameterChecker.maxEncodingSymbolID();

        final Set<Integer> esis = new LinkedHashSet<>(); // preserve randomized ordering

        if (preferSourceSymbols) {
            while (esis.size() < numSymbols) {
                // exponential distribution with mean K/2
                final int esi = (int)((-K / 2D) * Math.log(1 - rand.nextDouble()));
                // repeat sampling if a repeated ESI is obtained
                esis.add(Math.min(esi, maxESI));
            }
        }
        else {
            // Floyd's Algorithm for random sampling (uniform over all possible ESIs)
            for (int i = maxESI - numSymbols; i < maxESI; i++) {
                // try to add a random index between 0 and i (inclusive)
                if (!esis.add(rand.nextInt(i + 1))) {
                    // if already present, choose index i, which is surely not present yet
                    esis.add(i);
                }
            }
        }

        return esis;
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
    
    public static void main(String[] args) {

        final ArraySourceBlockDecoder dec = newRandomSBDecoder();
        final int iters = 500;
        for (int i = 0; i < iters; i++) {
            ArraySourceBlockDecoder.forceDecode(dec);
            System.out.println(i);
        }
    }
}
