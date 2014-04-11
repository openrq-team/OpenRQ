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


import static net.fec.openrq.test.encodecode.EncoderTask.Type.ANY_SYMBOL_RANDOM;
import static net.fec.openrq.test.encodecode.EncoderTask.Type.SOURCE_PLUS_REPAIR_SYMBOLS_RANDOM;
import static net.fec.openrq.test.encodecode.EncoderTask.Type.SOURCE_SYMBOLS_ONLY_RANDOM;
import static net.fec.openrq.test.encodecode.EncoderTask.Type.SOURCE_SYMBOLS_ONLY_SEQUENTIAL;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.fec.openrq.core.OpenRQ;
import net.fec.openrq.core.encoder.DataEncoder;
import net.fec.openrq.core.parameters.FECParameters;
import net.fec.openrq.core.util.arithmetic.ExtraMath;
import net.fec.openrq.test.encodecode.DecoderTask;
import net.fec.openrq.test.encodecode.DecoderTask.DecodedDataChecker;
import net.fec.openrq.test.encodecode.Defaults;
import net.fec.openrq.test.encodecode.EncoderTask;
import net.fec.openrq.test.encodecode.StatsType;
import net.fec.openrq.test.util.summary.LongSummaryStatistics;
import net.fec.openrq.test.util.summary.Summaries;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class TestRunner {

    private static final int DEF_MAX_PAYLOAD_LENGTH = 7;//1392;  // P'
    private static final int DEF_MAX_DEC_BLOCK_SIZE = 70;//76800; // WS // B

    private static final List<Integer> DATA_SIZES;

    static {
        final int[] small = {1, 2, 3, 5, 7,
                             10, 22, 33, 55, 77};
        //                   100, 222, 333, 555, 777,};

        // final int[] medium = {1000, 2222, 3333, 5555, 7777,
        // 10_000, 22_222, 33_333, 55_555, 77_777,
        // 100_000, 222_222, 333_333, 555_555, 777_777};

        // final int[] large = {1_000_000, 2_222_222, 3_333_333, 5_555_555, 7_777_777};

        // final int[] xLarge = {10_000_000, 22_222_222, 33_333_333, 55_555_555, 77_777_777};

        // final int[] xxLarge = {100_000_000, 222_222_222, 333_333_333, 555_555_555, 777_777_777};

        final List<Integer> list = new ArrayList<>(45);
        putInts(small, list);
        // putInts(medium, list);
        // putInts(large, list);
        // putInts(xLarge, list);
        // putInts(xxLarge, list);

        DATA_SIZES = Collections.unmodifiableList(list);
    }


    private static void putInts(int[] ints, List<Integer> list) {

        for (int i : ints) {
            list.add(i);
        }
    }


    private static final boolean WARMUP_ENABLED = false;
    private static final int WARMUP_SIZE = 1237;
    private static final long WARMUP_NANOS = TimeUnit.SECONDS.toNanos(15L);
    private static final boolean PRINT_WARMUP_STATS = true;

    private static final Random rand = new Random();


    private static byte[] makeData(int size) {

        final byte[] data = new byte[size];
        rand.nextBytes(data);
        return data;
    }

    private static int randomMaxSymbolsPerPacket() {

        return 1 + rand.nextInt(50);
    }

    private static FECParameters deriveFECParams(byte[] data) {

        final int dataLen = data.length;
        final int maxPayLen = DEF_MAX_PAYLOAD_LENGTH;
        final int maxDecBlock = Math.max(DEF_MAX_DEC_BLOCK_SIZE, data.length / 200);

        return FECParameters.deriveParameters(dataLen, maxPayLen, maxDecBlock);
    }


    private static final class EncProvider implements EncoderTask.DataEncoderProvider {

        private final byte[] data;
        private final FECParameters fecParams;


        EncProvider(byte[] data, FECParameters fecParams) {

            this.data = data;
            this.fecParams = fecParams;
        }

        @Override
        public DataEncoder newEncoder() {

            return OpenRQ.newEncoder(data, fecParams);
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


    private static void runTasks(
        EncoderTask encTask,
        DecoderTask decTask,
        ExecutorService executor,
        boolean printStats)
        throws InterruptedException
    {

        final Future<Map<StatsType, LongSummaryStatistics>> decFuture = executor.submit(decTask);
        final Future<Map<StatsType, LongSummaryStatistics>> encFuture = executor.submit(encTask);

        boolean decDone = false;
        boolean encDone = false;
        do {
            if (!decDone) decDone = handleFuture(decFuture, printStats);
            if (!encDone) encDone = handleFuture(encFuture, printStats);
        }
        while (!(decDone && encDone));
    }

    private static boolean handleFuture(Future<Map<StatsType, LongSummaryStatistics>> future, boolean printStats)
        throws InterruptedException
    {

        try {
            final Map<StatsType, LongSummaryStatistics> stats = future.get(1L, TimeUnit.SECONDS);
            if (printStats) Summaries.printlnToStream(stats, System.out);
            return true;
        }
        catch (TimeoutException e) {
            return false;
        }
        catch (ExecutionException e) {
            e.getCause().printStackTrace();
            return true;
        }
    }

    private static void printFECParameters(FECParameters fecParams) {

        System.out.printf("FEC Parameters:{ F = %d; T = %d; Z = %d; N = %d; KL = %d }%n",
            fecParams.dataLength(),
            fecParams.symbolSize(),
            fecParams.numberOfSourceBlocks(),
            fecParams.numberOfSubBlocks(),
            ExtraMath.ceilDiv(fecParams.totalSymbols(), fecParams.numberOfSourceBlocks()));
    }

    private static void printMiscParameters(int extraSymbols, int maxSymbolsPerPacket) {

        System.out.printf("Extra symbols = %d; Max symbols/packet = %d%n", extraSymbols, maxSymbolsPerPacket);
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        final ExecutorService executor = Executors.newFixedThreadPool(2);

        if (WARMUP_ENABLED) {
            System.out.println("Warming up...");
            runWarmupTasks(executor, makeData(WARMUP_SIZE));
        }

        System.out.println();
        System.out.println();
        System.out.println("Running sequential source symbols test...");
        for (int size : DATA_SIZES) {
            final int maxSymbolsPerPacket = randomMaxSymbolsPerPacket();
            System.out.println();
            runSequentialSourceSymbolsTasks(executor, makeData(size), maxSymbolsPerPacket);
        }

        System.out.println();
        System.out.println();
        System.out.println("Running random source symbols test...");
        for (int size : DATA_SIZES) {
            final int maxSymbolsPerPacket = randomMaxSymbolsPerPacket();
            System.out.println();
            runRandomSourceSymbolsTasks(executor, makeData(size), maxSymbolsPerPacket);
        }

        System.out.println();
        System.out.println();
        System.out.println("Running source + repair symbols test...");
        for (int extraSymbols = 0; extraSymbols <= 2; extraSymbols++) {
            for (int size : DATA_SIZES) {
                System.out.println();
                runSourcePlusRepairSymbolsTasks(executor, makeData(size), extraSymbols);
            }
        }

        System.out.println();
        System.out.println();
        System.out.println("Running any symbols test...");
        for (int extraSymbols = 0; extraSymbols <= 2; extraSymbols++) {
            for (int size : DATA_SIZES) {
                System.out.println();
                runAnySymbolsTasks(executor, makeData(size), extraSymbols);
            }
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    // ===== WARM-UP ===== //

    private static void runWarmupTasks(ExecutorService executor, byte[] data) throws IOException, InterruptedException {

        final Pipe pipe = Pipe.open();
        final int itersPerTask = 50;

        final EncProvider encProv = new EncProvider(data, deriveFECParams(data));
        final DecodedDataChecker checker = new DecChecker(data);
        if (PRINT_WARMUP_STATS) {
            printFECParameters(encProv.newEncoder().fecParameters());
            printMiscParameters(Defaults.EXTRA_SYMBOLS, Defaults.MAX_SYMBOLS_PER_PACKET);
        }

        final EncoderTask encTask = new EncoderTask.Builder(encProv, pipe.sink(), SOURCE_PLUS_REPAIR_SYMBOLS_RANDOM)
            .numIterations(itersPerTask)
            .build();
        final DecoderTask decTask = new DecoderTask.Builder(checker, pipe.source())
            .numIterations(itersPerTask)
            .build();

        final long startNanos = System.nanoTime();
        do {
            if (PRINT_WARMUP_STATS) {
                System.out.println();
                System.out.println("Data size = " + WARMUP_SIZE + " bytes (1 symbol per packet)");
            }
            runTasks(encTask, decTask, executor, PRINT_WARMUP_STATS);
        }
        while (System.nanoTime() - startNanos < WARMUP_NANOS);
    }

    // ===== SEQUENTIAL SOURCE SYMBOLS ===== //

    private static void runSequentialSourceSymbolsTasks(ExecutorService executor, byte[] data, int maxSymbolsPerPacket)
        throws IOException, InterruptedException {

        final Pipe pipe = Pipe.open();

        final EncProvider encProv = new EncProvider(data, deriveFECParams(data));
        final DecodedDataChecker checker = new DecChecker(data);

        printFECParameters(encProv.newEncoder().fecParameters());
        printMiscParameters(Defaults.EXTRA_SYMBOLS, maxSymbolsPerPacket);

        final EncoderTask encTask = new EncoderTask.Builder(encProv, pipe.sink(), SOURCE_SYMBOLS_ONLY_SEQUENTIAL)
            .maxSymbolsPerPacket(maxSymbolsPerPacket)
            .build();
        final DecoderTask decTask = new DecoderTask.Builder(checker, pipe.source())
            .build();

        runTasks(encTask, decTask, executor, true);
    }

    // ===== RANDOM SOURCE SYMBOLS ===== //

    private static void runRandomSourceSymbolsTasks(ExecutorService executor, byte[] data, int maxSymbolsPerPacket)
        throws IOException, InterruptedException {

        final Pipe pipe = Pipe.open();

        final EncProvider encProv = new EncProvider(data, deriveFECParams(data));
        final DecodedDataChecker checker = new DecChecker(data);

        printFECParameters(encProv.newEncoder().fecParameters());
        printMiscParameters(Defaults.EXTRA_SYMBOLS, maxSymbolsPerPacket);

        final EncoderTask encTask = new EncoderTask.Builder(encProv, pipe.sink(), SOURCE_SYMBOLS_ONLY_RANDOM)
            .maxSymbolsPerPacket(maxSymbolsPerPacket)
            .build();
        final DecoderTask decTask = new DecoderTask.Builder(checker, pipe.source())
            .build();

        runTasks(encTask, decTask, executor, true);
    }

    // ===== SOURCE + REPAIR SYMBOLS ===== //

    private static void runSourcePlusRepairSymbolsTasks(ExecutorService executor, byte[] data, int extraSymbols)
        throws IOException, InterruptedException {

        final Pipe pipe = Pipe.open();

        final EncProvider encProv = new EncProvider(data, deriveFECParams(data));
        final DecodedDataChecker checker = new DecChecker(data);

        printFECParameters(encProv.newEncoder().fecParameters());
        printMiscParameters(extraSymbols, Defaults.MAX_SYMBOLS_PER_PACKET);

        final EncoderTask encTask = new EncoderTask.Builder(encProv, pipe.sink(), SOURCE_PLUS_REPAIR_SYMBOLS_RANDOM)
            .extraSymbols(extraSymbols)
            .build();
        final DecoderTask decTask = new DecoderTask.Builder(checker, pipe.source())
            .build();

        runTasks(encTask, decTask, executor, true);
    }

    // ===== ANY SYMBOLS ===== //

    private static void runAnySymbolsTasks(ExecutorService executor, byte[] data, int extraSymbols)
        throws IOException, InterruptedException {

        final Pipe pipe = Pipe.open();

        final EncProvider encProv = new EncProvider(data, deriveFECParams(data));
        final DecodedDataChecker checker = new DecChecker(data);

        printFECParameters(encProv.newEncoder().fecParameters());
        printMiscParameters(extraSymbols, Defaults.MAX_SYMBOLS_PER_PACKET);

        final EncoderTask encTask = new EncoderTask.Builder(encProv, pipe.sink(), ANY_SYMBOL_RANDOM)
            .extraSymbols(extraSymbols)
            .build();
        final DecoderTask decTask = new DecoderTask.Builder(checker, pipe.source())
            .build();

        runTasks(encTask, decTask, executor, true);
    }
}
