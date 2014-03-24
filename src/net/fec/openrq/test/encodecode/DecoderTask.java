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
import java.nio.channels.ReadableByteChannel;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.core.ArrayDataDecoder;
import net.fec.openrq.core.FECParameters;
import net.fec.openrq.core.FECPayloadID;
import net.fec.openrq.core.OpenRQ;
import net.fec.openrq.core.decoder.SourceBlockDecoder;
import net.fec.openrq.core.decoder.SourceBlockState;
import net.fec.openrq.core.parameters.ParameterChecker;
import net.fec.openrq.core.util.numericaltype.SizeOf;
import net.fec.openrq.test.util.summary.LongSummaryStatistics;
import net.fec.openrq.test.util.summary.Summarizable;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class DecoderTask implements Summarizable<StatsType> {

    public static interface DecodedDataChecker {

        public boolean checkData(byte[] data);
    }

    public static final class Builder {

        private final DecodedDataChecker dataChecker;
        private final ReadableByteChannel readable;


        public Builder(ReadableByteChannel readable) {

            checkReadable(readable);
            this.dataChecker = null;
            this.readable = readable;
            defStatsUnit();
            defNumIterations();
        }

        public Builder(DecodedDataChecker dataChecker, ReadableByteChannel readable) {

            checkDataChecker(dataChecker);
            checkReadable(readable);
            this.dataChecker = dataChecker;
            this.readable = readable;
            defStatsUnit();
            defNumIterations();
        }


        private TimeUnit statsUnit;
        private int numIters;


        public Builder statsUnit(TimeUnit unit) {

            this.statsUnit = unit;
            return this;
        }

        public Builder defStatsUnit() {

            this.statsUnit = Defaults.STATS_UNIT;
            return this;
        }

        public Builder numIterations(int iters) {

            this.numIters = iters;
            return this;
        }

        public Builder defNumIterations() {

            this.numIters = Defaults.NUM_ITERATIONS;
            return this;
        }

        public DecoderTask build() {

            return new DecoderTask(dataChecker, readable, statsUnit, numIters);
        }
    }


    /**
     * @param readable
     * @param statsUnit
     * @param numIterations
     * @return
     */
    public static DecoderTask newDecoderTask(ReadableByteChannel readable, TimeUnit statsUnit, int numIterations) {

        checkReadable(readable);
        checkStatsUnit(statsUnit);
        checkNumIterations(numIterations);
        return new DecoderTask(null, readable, statsUnit, numIterations);
    }

    /**
     * @param dataChecker
     * @param readable
     * @param statsUnit
     * @param numIterations
     * @return
     */
    public static DecoderTask newDecoderTask(
        DecodedDataChecker dataChecker,
        ReadableByteChannel readable,
        TimeUnit statsUnit,
        int numIterations)
    {

        checkDataChecker(dataChecker);
        checkReadable(readable);
        checkStatsUnit(statsUnit);
        checkNumIterations(numIterations);
        return new DecoderTask(dataChecker, readable, statsUnit, numIterations);
    }

    private static void checkDataChecker(DecodedDataChecker dataChecker) {

        Objects.requireNonNull(dataChecker, "null decoded data checker");
    }

    private static void checkReadable(ReadableByteChannel readable) {

        Objects.requireNonNull(readable, "null readable");
    }

    private static void checkStatsUnit(TimeUnit statsUnit) {

        Objects.requireNonNull(statsUnit, "null stats unit");
    }

    private static void checkNumIterations(int numIterations) {

        if (numIterations < 1) throw new IllegalArgumentException("number of iterations must be positive");
    }


    private final DecodedDataChecker dataChecker;
    private final ReadableByteChannel readable;
    private final TimeUnit statsUnit;
    private final int numIterations;


    private DecoderTask(
        DecodedDataChecker dataChecker,
        ReadableByteChannel readable,
        TimeUnit statsUnit,
        int numIterations)
    {

        this.dataChecker = dataChecker;
        this.readable = readable;
        this.statsUnit = statsUnit;
        this.numIterations = numIterations;
    }

    @Override
    public Map<StatsType, LongSummaryStatistics> call() throws IOException {

        final LongSummaryStatistics initTimeStats = new LongSummaryStatistics();
        final LongSummaryStatistics symbolTimeStats = new LongSummaryStatistics();
        final LongSummaryStatistics decTimeStats = new LongSummaryStatistics();
        final LongSummaryStatistics decFailTimeStats = new LongSummaryStatistics();
        final LongSummaryStatistics totalDecsStats = new LongSummaryStatistics();
        final LongSummaryStatistics numDecFailsStats = new LongSummaryStatistics(totalDecsStats);

        final ByteBuffer dataHeaderBuf = DataHeader.allocateNewBuffer();
        final ByteBuffer symbolHeaderBuf = SymbolHeader.allocateNewBuffer();

        for (int n = 0; n < numIterations; n++) {
            dataHeaderBuf.clear();
            readBytes(dataHeaderBuf);
            final DataHeader dataHeader = DataHeader.parseDataHeader(dataHeaderBuf);
            final FECParameters fecParams = dataHeader.getFECParams();
            final int extraSymbols = dataHeader.getExtraSymbols();

            final ByteBuffer symbolBuf = ByteBuffer.allocate(fecParams.symbolSize());

            final ArrayDataDecoder dataDec = initDataDecoder(fecParams, extraSymbols, initTimeStats);
            final int Z = dataDec.numberOfSourceBlocks();

            for (int sbn = 0; sbn < Z; sbn++) {
                final SourceBlockDecoder srcBlockDec = dataDec.decoderForSourceBlock(sbn);
                final int totalSymbols = srcBlockDec.numberOfSourceSymbols() + extraSymbols;

                for (int i = 0; i < totalSymbols; i++) {
                    symbolHeaderBuf.clear();
                    readBytes(symbolHeaderBuf);
                    final SymbolHeader symbolHeader = SymbolHeader.parseSymbolHeader(symbolHeaderBuf, fecParams, sbn);
                    final int firstESI = symbolHeader.getFECPayloadID().encodingSymbolID();
                    final int numSymbolsInPacket = symbolHeader.getNumSymbols();

                    for (int s = 0; s < numSymbolsInPacket; s++) {
                        symbolBuf.clear();
                        readBytes(symbolBuf);
                        putSymbol(
                            srcBlockDec,
                            firstESI + s,
                            symbolBuf,
                            symbolTimeStats,
                            decTimeStats,
                            decFailTimeStats,
                            totalDecsStats,
                            numDecFailsStats);
                    }
                }
            }

            checkData(dataDec);
        }

        final EnumMap<StatsType, LongSummaryStatistics> map = new EnumMap<>(StatsType.class);
        map.put(StatsType.DECODER_INIT_TIME, initTimeStats);
        map.put(StatsType.SYMBOL_INPUT_TIME, symbolTimeStats);
        map.put(StatsType.DECODING_TIME, decTimeStats);
        map.put(StatsType.DECODING_FAILURE_TIME, decFailTimeStats);
        map.put(StatsType.NUM_DECODING_FAILURES, numDecFailsStats);
        return map;
    }

    private void readBytes(ByteBuffer buf) throws IOException {

        final int pos = buf.position();
        readable.read(buf);
        buf.flip().position(pos);
    }

    private ArrayDataDecoder initDataDecoder(
        FECParameters fecParams,
        int extraSymbols,
        LongSummaryStatistics initTimeStats) {

        final long startNanos = System.nanoTime();
        final ArrayDataDecoder dataDec = OpenRQ.newDecoder(fecParams, extraSymbols);
        final long endNanos = System.nanoTime();
        initTimeStats.accept(statsUnit.convert(endNanos - startNanos, TimeUnit.NANOSECONDS));

        return dataDec;
    }

    private void putSymbol(
        SourceBlockDecoder srcBlockDec,
        int esi,
        ByteBuffer buf,
        LongSummaryStatistics symbolTimeStats,
        LongSummaryStatistics decTimeStats,
        LongSummaryStatistics decFailTimeStats,
        LongSummaryStatistics totalDecsStats,
        LongSummaryStatistics numDecFailsStats)
    {

        final int K = srcBlockDec.numberOfSourceSymbols();

        final SourceBlockState state;
        final long nanos;
        if (esi < K) {
            final long startNanos = System.nanoTime();
            state = srcBlockDec.putSourceSymbol(esi, buf);
            final long endNanos = System.nanoTime();
            nanos = endNanos - startNanos;
        }
        else {
            final long startNanos = System.nanoTime();
            state = srcBlockDec.putRepairSymbol(esi, buf);
            final long endNanos = System.nanoTime();
            nanos = endNanos - startNanos;
        }

        switch (state) {
            case DECODED:
                decTimeStats.accept(statsUnit.convert(nanos, TimeUnit.NANOSECONDS));
                totalDecsStats.accept(1);
            break;

            case DECODING_FAILURE:
                decTimeStats.accept(statsUnit.convert(nanos, TimeUnit.NANOSECONDS));
                totalDecsStats.accept(1);
                decFailTimeStats.accept(statsUnit.convert(nanos, TimeUnit.NANOSECONDS));
                numDecFailsStats.accept(1);
            break;

            case INCOMPLETE:
                symbolTimeStats.accept(statsUnit.convert(nanos, TimeUnit.NANOSECONDS));
            break;

            default:
                throw new AssertionError("unknown enum type");

        }
    }

    private void checkData(ArrayDataDecoder dataDec) {

        if (dataChecker != null && dataDec.isDataDecoded() && !dataChecker.checkData(dataDec.dataArray())) {
            throw new IllegalStateException("decoded data does not match");
        }
    }


    private static final class DataHeader {

        static ByteBuffer allocateNewBuffer() {

            return ByteBuffer.allocate(12 + SizeOf.INT);
        }

        static DataHeader parseDataHeader(ByteBuffer buf) {

            final FECParameters fecParams = FECParameters.readFromBuffer(buf);
            if (!fecParams.isValid()) {
                throw new IllegalArgumentException("invalid FEC parameters");
            }

            final int extraSymbols = buf.getInt();
            if (extraSymbols < 0 || extraSymbols > Integer.MAX_VALUE - ParameterChecker.maxNumSourceSymbolsPerBlock()) {
                throw new IllegalArgumentException("invalid number of extra symbols");
            }

            return new DataHeader(fecParams, extraSymbols);
        }


        private final FECParameters fecParams;
        private final int extraSymbols;


        private DataHeader(FECParameters fecParams, int extraSymbols) {

            this.fecParams = fecParams;
            this.extraSymbols = extraSymbols;
        }

        FECParameters getFECParams() {

            return fecParams;
        }

        int getExtraSymbols() {

            return extraSymbols;
        }
    }

    private static final class SymbolHeader {

        static ByteBuffer allocateNewBuffer() {

            return ByteBuffer.allocate(4 + SizeOf.INT);
        }

        static SymbolHeader parseSymbolHeader(ByteBuffer buf, FECParameters fecParams, int sbn) {

            final FECPayloadID fecPayloadID = FECPayloadID.readFromBuffer(buf, fecParams);
            if (!fecPayloadID.isValid()) {
                throw new IllegalArgumentException("invalid FEC Payload ID");
            }
            if (fecPayloadID.sourceBlockNumber() != sbn) {
                throw new IllegalArgumentException("source block number does not match the expected");
            }

            final int numSymbols = buf.getInt();
            if (numSymbols < 1) {
                throw new IllegalArgumentException("invalid number of symbols");
            }

            return new SymbolHeader(fecPayloadID, numSymbols);
        }


        private final FECPayloadID fecPayloadID;
        private final int numSymbols;


        SymbolHeader(FECPayloadID fecPayloadID, int numSymbols) {

            this.fecPayloadID = fecPayloadID;
            this.numSymbols = numSymbols;
        }

        FECPayloadID getFECPayloadID() {

            return fecPayloadID;
        }

        int getNumSymbols() {

            return numSymbols;
        }
    }
}
