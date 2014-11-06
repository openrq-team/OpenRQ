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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.math.ExtraMath;
import net.fec.openrq.util.math.OctetOps;

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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;


@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@State(Scope.Benchmark)
public class ParallelVectorAdditionTest {

    // default parameter values
    private static final int DEF_NUMVECS = 1000;
    private static final int DEF_VECSIZE = 1500;

    /*
     * Matched against the total number of bytes of all available source vectors.
     * If total is less than or equal to this value, the addition(s) are performed
     * sequentially.
     */
    private static final long VECTOR_BYTES_THRESHOLD = ParameterChecker.maxSymbolSize();

    @Param("" + DEF_NUMVECS)
    private int numvecs;

    @Param("" + DEF_VECSIZE)
    private int vecsize;

    private final ForkJoinPool pool = new ForkJoinPool();
    private final Random rand = TestingCommon.newSeededRandom();

    private byte[][] srcVecs;
    private byte[][] dstVecs;


    @Setup
    public void setup() {

        srcVecs = new byte[numvecs][vecsize];
        randomBytes(srcVecs, rand);

        dstVecs = new byte[numvecs][vecsize];
        randomBytes(dstVecs, rand);
    }

    private static void randomBytes(byte[][] bytes, Random rand) {

        for (byte[] bs : bytes) {
            rand.nextBytes(bs);
        }
    }

    @TearDown
    public void finish() {

        pool.shutdown();
    }

    @Benchmark
    public void testSequential() {

        for (int i = 0; i < numvecs; i++) {
            OctetOps.vectorVectorAddition(srcVecs[i], dstVecs[i], dstVecs[i]);
        }
    }

    @Benchmark
    public void testParallel() {

        pool.invoke(new ForkVectorAddition(srcVecs, 0, dstVecs, 0, 0, (long)numvecs * vecsize));
    }


    private static final class ForkVectorAddition extends RecursiveAction {

        private static final long serialVersionUID = 1L;

        private final byte[][] srcVecs;
        private final int srcIndex;

        private final byte[][] dstVecs;
        private final int dstIndex;

        private final int vecOff;
        private final long totalBytes;


        ForkVectorAddition(
            byte[][] srcVecs,
            int srcIndex,
            byte[][] dstVecs,
            int dstIndex,
            int vecOff,
            long totalBytes)
        {

            this.srcVecs = srcVecs;
            this.srcIndex = srcIndex;

            this.dstVecs = dstVecs;
            this.dstIndex = dstIndex;

            this.vecOff = vecOff;
            this.totalBytes = totalBytes;
        }

        @Override
        protected void compute() {

            if (totalBytes <= VECTOR_BYTES_THRESHOLD) {
                addVectors();
            }
            else {
                final long fstTotalHalf = totalBytes / 2;
                final long sndTotalHalf = totalBytes - fstTotalHalf;

                final long virtualOff = ExtraMath.addExact(vecOff, fstTotalHalf);

                final int midNumVecs = ExtraMath.toIntExact(virtualOff / vectorLength());
                final int midSI = ExtraMath.addExact(srcIndex, midNumVecs);
                final int midDI = ExtraMath.addExact(dstIndex, midNumVecs);
                final int midVecOff = ExtraMath.toIntExact(virtualOff % vectorLength());

                invokeAll(
                    new ForkVectorAddition(srcVecs, srcIndex, dstVecs, dstIndex, vecOff, fstTotalHalf),
                    new ForkVectorAddition(srcVecs, midSI, dstVecs, midDI, midVecOff, sndTotalHalf));
            }
        }

        private int vectorLength() {

            return (srcVecs.length == 0) ? 0 : srcVecs[0].length;
        }

        private void addVectors() {

            int si = srcIndex;
            int di = dstIndex;
            int vo = vecOff;
            long remaining = totalBytes;

            for /* ever */(;;) {
                final byte[] src = srcVecs[si];
                final byte[] dst = dstVecs[di];
                final int numBytes = (int)Math.min(remaining, vectorLength() - vo);
                addInPlace(src, vo, dst, vo, numBytes);

                if (remaining > numBytes) {
                    remaining -= numBytes;
                    si++;
                    di++;
                    vo = 0;
                }
                else {
                    break;
                }
            }
        }

        private static void addInPlace(byte[] vector1, int vecPos1, byte[] vector2, int vecPos2, int length) {

            OctetOps.vectorVectorAddition(vector1, vecPos1, vector2, vecPos2, vector2, vecPos2, length);
        }
    }
}
