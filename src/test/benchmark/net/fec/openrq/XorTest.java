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


import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.util.datatype.SizeOf;

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


@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@Fork(0)
@State(Scope.Benchmark)
public class XorTest {

    @Param({"8", "80", "800", "8000", "80000", "800000", "8000000", "80000000"})
    public int size;

    private ByteBuffer buf;
    private ByteBuffer dirBuf;


    @Setup
    public void setup() {

        buf = ByteBuffer.allocate(size);
        randomBytes(buf, TestingCommon.newSeededRandom());

        dirBuf = ByteBuffer.allocateDirect(size);
        randomBytes(dirBuf, TestingCommon.newSeededRandom());
    }

    private static void randomBytes(ByteBuffer b, Random rand) {

        final int pos = b.position();
        final byte[] array = new byte[b.remaining()];
        rand.nextBytes(array);
        b.put(array);
        b.position(pos);
    }

    @Benchmark
    public byte testArrayBytes() {

        buf.rewind();
        final int len = buf.remaining();

        byte result = 0;
        for (int i = 0; i < len; ++i) {
            result ^= buf.get();
        }
        return result;
    }

    @Benchmark
    public long testArrayLongs() {

        buf.rewind();
        final int len = buf.remaining() / SizeOf.LONG;

        long result = 0;
        for (int i = 0; i < len; ++i) {
            result ^= buf.getLong();
        }
        return result;
    }

    @Benchmark
    public byte testDirectBytes() {

        dirBuf.rewind();
        final int len = dirBuf.remaining();

        byte result = 0;
        for (int i = 0; i < len; ++i) {
            result ^= dirBuf.get();
        }
        return result;
    }

    @Benchmark
    public long testDirectLongs() {

        dirBuf.rewind();
        final int len = dirBuf.remaining() / SizeOf.LONG;

        long result = 0;
        for (int i = 0; i < len; ++i) {
            result ^= dirBuf.getLong();
        }
        return result;
    }
}
