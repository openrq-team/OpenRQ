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


import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;


@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
public class XorTest {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000", "10000000", "100000000"})
    public int size;

    private byte[] array;
    private LongBuffer buffer;


    @Setup
    public void setup() {

        array = new byte[size];
        buffer = ByteBuffer.wrap(array).asLongBuffer();
    }

    @GenerateMicroBenchmark
    public byte testArray() {

        byte result = 0;
        for (int i = 0, len = array.length; i < len; ++i) {
            result ^= array[i];
        }
        return result;
    }

    @GenerateMicroBenchmark
    public long testBuffer() {

        long result = 0L;
        buffer.rewind();
        while (buffer.hasRemaining()) {
            result ^= buffer.get();
        }
        return result;
    }
}
