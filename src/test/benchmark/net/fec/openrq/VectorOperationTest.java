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


import static net.fec.openrq.util.io.ByteBuffers.BufferType.ARRAY_BACKED;
import static net.fec.openrq.util.io.ByteBuffers.BufferType.DIRECT;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.util.io.ByteBuffers;
import net.fec.openrq.util.math.OctetOps;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;


@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@Fork(0)
@State(Scope.Benchmark)
public class VectorOperationTest {

    private static final int SYMBOL_SIZE = 1500;

    private final byte[] srcArray = new byte[SYMBOL_SIZE];
    private final byte[] dstArray = new byte[SYMBOL_SIZE];

    private final ByteBuffer srcArrBuf = ByteBuffers.allocate(SYMBOL_SIZE, ARRAY_BACKED);
    private final ByteBuffer dstArrBuf = ByteBuffers.allocate(SYMBOL_SIZE, ARRAY_BACKED);

    private final ByteBuffer srcDirBuf = ByteBuffers.allocate(SYMBOL_SIZE, DIRECT);
    private final ByteBuffer dstDirBuf = ByteBuffers.allocate(SYMBOL_SIZE, DIRECT);

    private final byte divisor = (byte)TestingCommon.newSeededRandom().nextInt();


    @Setup(Level.Trial)
    public void setup() {

        TestingCommon.newSeededRandom().nextBytes(srcArray);
        TestingCommon.newSeededRandom().nextBytes(dstArray);

        randomBytes(srcArrBuf, TestingCommon.newSeededRandom());
        randomBytes(dstArrBuf, TestingCommon.newSeededRandom());

        randomBytes(srcDirBuf, TestingCommon.newSeededRandom());
        randomBytes(dstDirBuf, TestingCommon.newSeededRandom());
    }

    private static void randomBytes(ByteBuffer b, Random rand) {

        final int pos = b.position();
        final byte[] array = new byte[b.remaining()];
        rand.nextBytes(array);
        b.put(array);
        b.position(pos);
    }

    @Benchmark
    public void testArrayAddition() {

        OctetOps.vectorVectorAddition(srcArray, dstArray, dstArray);
    }

    @Benchmark
    public void testArrayDivision() {

        OctetOps.valueVectorDivision(divisor, srcArray, dstArray);
    }

    @Benchmark
    public void testArrayBufferAddition() {

        OctetOps.vectorVectorAddition(srcArrBuf, dstArrBuf, dstArrBuf);
    }

    @Benchmark
    public void testArrayBufferDivision() {

        OctetOps.valueVectorDivision(divisor, srcArrBuf, dstArrBuf);
    }

    @Benchmark
    public void testDirectBufferAddition() {

        OctetOps.vectorVectorAddition(srcDirBuf, dstDirBuf, dstDirBuf);
    }

    @Benchmark
    public void testDirectBufferDivision() {

        OctetOps.valueVectorDivision(divisor, srcDirBuf, dstDirBuf);
    }
}
