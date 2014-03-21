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
package net.fec.openrq.test;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.core.ArrayDataEncoder;
import net.fec.openrq.core.encoder.DataEncoder;
import net.fec.openrq.core.encoder.DataEncoderBuilder;
import net.fec.openrq.test.encodecode.DecoderTask;
import net.fec.openrq.test.encodecode.EncoderTask;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class TestRunner {

    private static final List<Integer> DATA_SIZES = Collections.unmodifiableList(Arrays.asList(
        1, 3, 7, 9,
        10, 13, 17, 99,
        100, 103, 107, 999,
        1000, 1003, 1007, 9999,
        10_000, 10_003, 10_007, 99_999,
        100_000, 100_003, 100_007, 999_999,
        1_000_000, 1_000_003, 1_000_007, 9_999_999,
        10_000_000, 10_000_003, 10_000_007, 99_999_999));

    private static final int WARMUP_SIZE = 1234;
    private static final long WARMUP_NANOS = TimeUnit.SECONDS.toNanos(15L);

    private static final Random rand = new Random();


    private static byte[] makeData(int size) {

        final byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }


    private static final class EncProvider implements EncoderTask.DataEncoderProvider {

        private final DataEncoderBuilder<ArrayDataEncoder> builder;


        EncProvider(DataEncoderBuilder<ArrayDataEncoder> builder) {

            this.builder = builder;
        }

        @Override
        public DataEncoder newEncoder() {

            return builder.build();
        }
    }

    private static final class DecChecker implements DecoderTask.DecodedDataChecker {

        private final byte[] originalData;


        DecChecker(byte[] originalData) {

            this.originalData = originalData;
        }

        @Override
        public boolean checkData(byte[] data) {

            return Arrays.equals(data, originalData);
        }
    }


    public static void main(String[] args) throws InterruptedException {

        final ExecutorService executor = Executors.newFixedThreadPool(2);

        runWarmupTasks(executor, makeData(WARMUP_SIZE));
    }

    // ===== WARM-UP ===== //

    private static void runWarmupTasks(ExecutorService executor, byte[] data) {

        final long startNanos = System.nanoTime();
        do {

        }
        while (System.nanoTime() - startNanos < WARMUP_NANOS);
    }
}
