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
package net.fec.openrq.test.encodecode;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.core.encoder.DataEncoder;
import net.fec.openrq.core.encoder.EncodingPacket;
import net.fec.openrq.core.encoder.SourceBlockEncoder;
import net.fec.openrq.core.parameters.ParameterChecker;
import net.fec.openrq.core.util.numericaltype.SizeOf;
import net.fec.openrq.test.util.LongSummaryStatistics;
import net.fec.openrq.test.util.Summarizable;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class EncoderTask implements Summarizable<EncoderStats> {

    public static enum Type {

        SOURCE_SYMBOLS_ONLY_SEQUENTIAL,
        SOURCE_SYMBOLS_ONLY_RANDOM,
        SOURCE_PLUS_REPAIR_SYMBOLS_RANDOM,
        ANY_SYMBOL_RANDOM;
    }

    public static final class Builder {

        private final DataEncoder encoder;
        private final WritableByteChannel writable;
        private final Type type;

        private TimeUnit statsUnit;
        private int extraSymbols;
        private int maxSymbolsPerPacket;
        private int numIterations;


        public Builder(DataEncoder encoder, WritableByteChannel writable, Type type) {

            checkEncoder(encoder);
            checkWritable(writable);
            checkType(type);

            this.encoder = encoder;
            this.writable = writable;
            this.type = type;

            defStatsUnit();
            defExtraSymbols();
            defMaxSymbolsPerPacket();
            defNumIterations();
        }

        public Builder statsUnit(TimeUnit unit) {

            checkStatsUnit(unit);
            this.statsUnit = unit;
            return this;
        }

        public Builder defStatsUnit() {

            this.statsUnit = Defaults.STATS_UNIT;
            return this;
        }

        public Builder extraSymbols(int extra) {

            checkExtraSymbols(extra, type, encoder);
            this.extraSymbols = extra;
            return this;
        }

        public Builder defExtraSymbols() {

            this.extraSymbols = Defaults.EXTRA_SYMBOLS;
            return this;
        }

        public Builder maxSymbolsPerPacket(int maxSymbols) {

            checkMaxSymbolsPerPacket(maxSymbols, type);
            this.maxSymbolsPerPacket = maxSymbols;
            return this;
        }

        public Builder defMaxSymbolsPerPacket() {

            this.maxSymbolsPerPacket = Defaults.MAX_SYMBOLS_PER_PACKET;
            return this;
        }

        public Builder numIterations(int numIters) {

            checkNumIterations(numIters);
            this.numIterations = numIters;
            return this;
        }

        public Builder defNumIterations() {

            this.numIterations = Defaults.NUM_ITERATIONS;
            return this;
        }

        public EncoderTask build() {

            return new EncoderTask(
                encoder,
                writable,
                type,
                statsUnit,
                extraSymbols,
                maxSymbolsPerPacket,
                numIterations);
        }
    }


    /**
     * @param encoder
     * @param writable
     * @param type
     * @param statsUnit
     * @param extraSymbols
     * @param maxSymbolsPerPacket
     * @param numIterations
     * @return
     */
    public static EncoderTask newEncoderTask(
        DataEncoder encoder,
        WritableByteChannel writable,
        Type type,
        TimeUnit statsUnit,
        int extraSymbols,
        int maxSymbolsPerPacket,
        int numIterations)
    {

        checkEncoder(encoder);
        checkWritable(writable);
        checkType(type);
        checkStatsUnit(statsUnit);
        checkExtraSymbols(extraSymbols, type, encoder);
        checkMaxSymbolsPerPacket(maxSymbolsPerPacket, type);
        checkNumIterations(numIterations);

        return new EncoderTask(
            encoder,
            writable,
            type,
            statsUnit,
            extraSymbols,
            maxSymbolsPerPacket,
            numIterations);
    }

    private static void checkEncoder(DataEncoder encoder) {

        Objects.requireNonNull(encoder, "null encoder");
    }

    private static void checkWritable(WritableByteChannel writable) {

        Objects.requireNonNull(writable, "null writable");
    }

    private static void checkType(Type type) {

        Objects.requireNonNull(type, "null type");
    }

    private static void checkStatsUnit(TimeUnit statsUnit) {

        Objects.requireNonNull(statsUnit, "null stats unit");
    }

    // requires non-null type
    private static void checkExtraSymbols(int extraSymbols, Type type, DataEncoder dataEnc) {

        if (extraSymbols < 0) throw new IllegalArgumentException("extra symbols must be non-negative");
        switch (type) {
            case SOURCE_SYMBOLS_ONLY_RANDOM: // intentional fall-through
            case SOURCE_SYMBOLS_ONLY_SEQUENTIAL:
                if (extraSymbols > 0) throw new IllegalArgumentException("extra symbols must be 0");
            break;

            default:
            // do nothing
            break;
        }

        // integer overflow check
        for (int sbn = 0; sbn < dataEnc.numberOfSourceBlocks(); sbn++) {
            final SourceBlockEncoder enc = dataEnc.encoderForSourceBlock(sbn);
            final int K = enc.numberOfSourceSymbols();
            if (extraSymbols > Integer.MAX_VALUE - K) {
                throw new IllegalArgumentException("extra symbols is too large");
            }
        }
    }

    // requires non-null type
    private static void checkMaxSymbolsPerPacket(int maxSymbols, Type type) {

        if (maxSymbols < 1) throw new IllegalArgumentException("max symbols per packet must be positive");
        switch (type) {
            case SOURCE_PLUS_REPAIR_SYMBOLS_RANDOM: // intentional fall-through
            case ANY_SYMBOL_RANDOM:
                if (maxSymbols > 1) throw new IllegalArgumentException("max symbols per packet must be 1");
            break;

            default:
            // do nothing
            break;
        }
    }

    private static void checkNumIterations(int numIters) {

        if (numIters < 1) throw new IllegalArgumentException("number of iterations must be positive");
    }


    private final DataEncoder encoder;
    private final WritableByteChannel writable;
    private final Type type;

    private final TimeUnit statsUnit;
    private final int extraSymbols;
    private final int maxSymbolsPerPacket;
    private final int numIterations;


    private EncoderTask(
        DataEncoder encoder,
        WritableByteChannel writable,
        Type type,
        TimeUnit statsUnit,
        int extraSymbols,
        int maxSymbolsPerPacket,
        int numIterations) {

        this.writable = writable;
        this.encoder = encoder;
        this.type = type;

        this.statsUnit = statsUnit;
        this.extraSymbols = extraSymbols;
        this.maxSymbolsPerPacket = maxSymbolsPerPacket;
        this.numIterations = numIterations;
    }

    @Override
    public EnumMap<EncoderStats, LongSummaryStatistics> call() throws IOException {

        final LongSummaryStatistics stats = new LongSummaryStatistics();
        final Random rand = new Random();
        final int numSourceBlocks = encoder.numberOfSourceBlocks();

        sendDataHeader(encoder);

        switch (type) {
            case SOURCE_SYMBOLS_ONLY_SEQUENTIAL:
                for (int n = 0; n < numIterations; n++) {
                    for (int sbn = 0; sbn < numSourceBlocks; sbn++) {

                        final SourceBlockEncoder srcBlockEnc = encoder.encoderForSourceBlock(sbn);
                        final int numSymbols = srcBlockEnc.numberOfSourceSymbols();

                        for (int esi = 0; esi < numSymbols;) {
                            final EncodingPacket packet = getSequentialSourcePacket(srcBlockEnc, esi, stats);
                            sendPacket(packet);
                            esi += packet.numberOfSymbols();
                        }
                    }
                }
            break;

            case SOURCE_SYMBOLS_ONLY_RANDOM:
                for (int n = 0; n < numIterations; n++) {
                    for (int sbn = 0; sbn < numSourceBlocks; sbn++) {

                        final SourceBlockEncoder srcBlockEnc = encoder.encoderForSourceBlock(sbn);
                        final List<Integer> srcSymbolESIs = generateSourceSymbolESIs(srcBlockEnc);

                        while (!srcSymbolESIs.isEmpty()) {
                            final EncodingPacket packet = getRandomSourcePacket(srcBlockEnc, srcSymbolESIs, rand, stats);
                            sendPacket(packet);
                        }
                    }
                }
            break;

            case SOURCE_PLUS_REPAIR_SYMBOLS_RANDOM:
                for (int n = 0; n < numIterations; n++) {
                    for (int sbn = 0; sbn < numSourceBlocks; sbn++) {

                        final SourceBlockEncoder srcBlockEnc = encoder.encoderForSourceBlock(sbn);
                        final Set<Integer> esis = generateRandomSymbolESIs(srcBlockEnc, rand, true);

                        for (int esi : esis) {
                            final EncodingPacket packet = getSingleSymbolPacket(srcBlockEnc, esi);
                            sendPacket(packet);
                        }
                    }
                }
            break;

            case ANY_SYMBOL_RANDOM:
                for (int n = 0; n < numIterations; n++) {
                    for (int sbn = 0; sbn < numSourceBlocks; sbn++) {

                        final SourceBlockEncoder srcBlockEnc = encoder.encoderForSourceBlock(sbn);
                        final Set<Integer> esis = generateRandomSymbolESIs(srcBlockEnc, rand, false);

                        for (int esi : esis) {
                            final EncodingPacket packet = getSingleSymbolPacket(srcBlockEnc, esi);
                            sendPacket(packet);
                        }
                    }
                }
            break;

            default:
                throw new AssertionError("unknown type");
        }

        final EnumMap<EncoderStats, LongSummaryStatistics> map = new EnumMap<>(EncoderStats.class);
        map.put(EncoderStats.ENCODING_TIME, stats);
        return map;
    }

    private void sendDataHeader(DataEncoder enc) throws IOException {

        final ByteBuffer header = ByteBuffer.allocate(12);
        enc.fecParameters().writeToBuffer(header);
        header.rewind();

        writable.write(header);
    }

    private void sendPacket(EncodingPacket packet) throws IOException {

        final List<ByteBuffer> symbols = packet.getSymbolData();

        final ByteBuffer header = ByteBuffer.allocate(4 + SizeOf.INT);
        packet.fecPayloadID().writeToBuffer(header);
        header.putInt(symbols.size());
        header.rewind();

        writable.write(header);
        for (ByteBuffer symb : symbols) {
            symb.rewind();
            writable.write(symb);
        }
    }

    private EncodingPacket getSequentialSourcePacket(SourceBlockEncoder enc, int esi, LongSummaryStatistics stats) {

        final int numSymbols = getMaxNumSymbols(enc, esi);

        final long startNanos = System.nanoTime();
        final EncodingPacket packet = enc.getSourcePacket(esi, numSymbols);
        final long endNanos = System.nanoTime();
        stats.accept(statsUnit.convert(endNanos - startNanos, TimeUnit.NANOSECONDS));

        return packet;
    }

    private List<Integer> generateSourceSymbolESIs(SourceBlockEncoder enc) {

        final int K = enc.numberOfSourceSymbols();

        final List<Integer> list = new LinkedList<>(); // faster removals
        for (int esi = 0; esi < K; esi++) {
            list.add(esi);
        }

        return list;
    }

    private Set<Integer> generateRandomSymbolESIs(SourceBlockEncoder enc, Random rand, boolean preferSourceSymbols) {

        final int K = enc.numberOfSourceSymbols();
        final int numSymbols = K + extraSymbols;
        final Set<Integer> esis = new LinkedHashSet<>(); // preserve randomized ordering

        if (preferSourceSymbols) {
            while (esis.size() < numSymbols) {
                // exponential distribution with mean K/2
                final int esi = (int)((-K / 2) * Math.log(1 - rand.nextDouble()));
                // repeat sampling if a repeated ESI is obtained
                esis.add(Math.min(esi, ParameterChecker.maxEncodingSymbolID()));
            }
        }
        else {
            // Floyd's Algorithm for random sampling (uniform over all possible ESIs)
            final int maxESI = ParameterChecker.maxEncodingSymbolID();
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

    private EncodingPacket getRandomSourcePacket(
        SourceBlockEncoder enc,
        List<Integer> srcSymbolESIs,
        Random rand,
        LongSummaryStatistics stats)
    {

        // uniform distribution over the current source symbol ESIs
        final int listIndex = rand.nextInt(srcSymbolESIs.size());
        final int esi = srcSymbolESIs.remove(listIndex);
        final int maxNumSymbols = getMaxNumSymbols(enc, esi);

        // find the actual maximum number of symbols here, since the list may contain non-contiguous ESIs
        int numSymbols = 1;
        final Iterator<Integer> iter = srcSymbolESIs.listIterator(listIndex); // points to the next ESI
        while (numSymbols < maxNumSymbols && iter.hasNext()) {
            if (iter.next() == esi + numSymbols) {
                numSymbols++;
                iter.remove();
            }
            else {
                break;
            }
        }

        final long startNanos = System.nanoTime();
        final EncodingPacket packet = enc.getSourcePacket(esi, numSymbols);
        final long endNanos = System.nanoTime();
        stats.accept(statsUnit.convert(endNanos - startNanos, TimeUnit.NANOSECONDS));

        return packet;
    }

    private int getMaxNumSymbols(SourceBlockEncoder enc, int esi) {

        final int K = enc.numberOfSourceSymbols();

        if (esi < K) {
            return Math.min(maxSymbolsPerPacket, K - esi);
        }
        else {
            final int maxESI = ParameterChecker.maxEncodingSymbolID();
            return Math.min(maxSymbolsPerPacket, 1 + maxESI - esi);
        }
    }

    private EncodingPacket getSingleSymbolPacket(SourceBlockEncoder enc, int esi) {

        final int K = enc.numberOfSourceSymbols();
        if (esi < K) return enc.getSourcePacket(esi);
        else return enc.getRepairPacket(esi);
    }
}
