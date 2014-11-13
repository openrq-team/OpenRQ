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


import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.parameters.FECParameters;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
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
public class SourceBlockEncodingTest {

    // default parameter values
    private static final int DEF_SYMBOL_SIZE = 1500;
    private static final int DEF_NUM_SOURCE_SYMBOLS = 1000;


    private static ArraySourceBlockEncoder newSBEncoder(int T, int K) {

        TestingCommon.checkParamsForSingleSourceBlockData(T, K);

        // force single source block
        final FECParameters fecParams = FECParameters.newParameters((long)T * K, T, 1);
        final Random rand = TestingCommon.newSeededRandom();

        final byte[] data = TestingCommon.randomBytes(fecParams.dataLengthAsInt(), rand);
        return (ArraySourceBlockEncoder)OpenRQ.newEncoder(data, fecParams).sourceBlock(0);
    }


    @Param({"" + DEF_SYMBOL_SIZE})
    private int symbsize;

    @Param({"" + DEF_NUM_SOURCE_SYMBOLS})
    private int srcsymbs;

    private ArraySourceBlockEncoder enc;


    public SourceBlockEncodingTest() {

        this.symbsize = DEF_SYMBOL_SIZE;
        this.srcsymbs = DEF_NUM_SOURCE_SYMBOLS;

        this.enc = null;
    }

    @Setup(Level.Trial)
    public void setup() {

        enc = newSBEncoder(symbsize, srcsymbs);
    }

    @Benchmark
    public void test() {

        ArraySourceBlockEncoder.forceInterSymbolsGen(enc);
    }

    // for CPU/memory profiling
    public static void main(String[] args) {

        final SourceBlockEncodingTest test = new SourceBlockEncodingTest();
        test.setup();
        final int iters = 100_000;
        for (int i = 0; i < iters; i++) {
            test.test();
            System.out.println(i);
        }
    }
}
