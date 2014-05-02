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


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.decoder.DataDecoder;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.numericaltype.SizeOf;
import net.fec.openrq.util.summary.LongSummaryStatistics;
import net.fec.openrq.util.summary.Summarizable;


/**
 */
final class DecoderTask implements Summarizable<StatsType> {

    static interface DecodedDataChecker {

        public boolean checkData(byte[] data);
    }

    static final class Builder {

        private final DecodedDataChecker dataChecker;
        private final ReadableByteChannel readable;


        Builder(ReadableByteChannel readable) {

            checkReadable(readable);
            this.dataChecker = null;
            this.readable = readable;
            defNumIterations();
        }

        Builder(DecodedDataChecker dataChecker, ReadableByteChannel readable) {

            checkDataChecker(dataChecker);
            checkReadable(readable);
            this.dataChecker = dataChecker;
            this.readable = readable;
            defNumIterations();
        }


        private int numIters;


        Builder numIterations(int iters) {

            this.numIters = iters;
            return this;
        }

        Builder defNumIterations() {

            this.numIters = Defaults.NUM_ITERATIONS;
            return this;
        }

        DecoderTask build() {

            return new DecoderTask(dataChecker, readable, numIters);
        }
    }


    /**
     * @param readable
     * @param numIterations
     * @return
     */
    static DecoderTask newDecoderTask(ReadableByteChannel readable, int numIterations) {

        checkReadable(readable);
        checkNumIterations(numIterations);
        return new DecoderTask(null, readable, numIterations);
    }

    /**
     * @param dataChecker
     * @param readable
     * @param numIterations
     * @return
     */
    static DecoderTask newDecoderTask(
        DecodedDataChecker dataChecker,
        ReadableByteChannel readable,
        int numIterations)
    {

        checkDataChecker(dataChecker);
        checkReadable(readable);
        checkNumIterations(numIterations);
        return new DecoderTask(dataChecker, readable, numIterations);
    }

    private static void checkDataChecker(DecodedDataChecker dataChecker) {

        Objects.requireNonNull(dataChecker, "null decoded data checker");
    }

    private static void checkReadable(ReadableByteChannel readable) {

        Objects.requireNonNull(readable, "null readable");
    }

    private static void checkNumIterations(int numIterations) {

        if (numIterations < 1) throw new IllegalArgumentException("number of iterations must be positive");
    }


    private final DecodedDataChecker dataChecker;
    private final ReadableByteChannel readable;
    private final int numIterations;


    private DecoderTask(
        DecodedDataChecker dataChecker,
        ReadableByteChannel readable,
        int numIterations)
    {

        this.dataChecker = dataChecker;
        this.readable = readable;
        this.numIterations = numIterations;
    }

    @Override
    public Map<StatsType, LongSummaryStatistics> call() throws IOException {

        final LongSummaryStatistics initTimeStats = new LongSummaryStatistics();
        final LongSummaryStatistics symbolTimeStats = new LongSummaryStatistics();
        final LongSummaryStatistics decTimeStats = new LongSummaryStatistics();
        final LongSummaryStatistics totalDecsStats = new LongSummaryStatistics();
        final LongSummaryStatistics numDecFailsStats = new LongSummaryStatistics(totalDecsStats);
        final LongSummaryStatistics decFailTimeStats = new LongSummaryStatistics();

        final ByteBuffer dataHeaderBuf = DataHeader.allocateNewBuffer();

        for (int n = 0; n < numIterations; n++) {
            dataHeaderBuf.clear();
            readBytes(dataHeaderBuf);
            final DataHeader dataHeader = DataHeader.parseDataHeader(dataHeaderBuf);
            final FECParameters fecParams = dataHeader.getFECParams();
            final int extraSymbols = dataHeader.getExtraSymbols();

            final ArrayDataDecoder dataDec = initDataDecoder(fecParams, extraSymbols, initTimeStats);
            final int Z = dataDec.numberOfSourceBlocks();

            for (int sbn = 0; sbn < Z; sbn++) {
                final SourceBlockDecoder srcBlockDec = dataDec.sourceBlock(sbn);
                final int totalSymbols = srcBlockDec.numberOfSourceSymbols() + extraSymbols;

                for (int i = 0; i < totalSymbols;) {
                    final EncodingPacket packet = readPacket(dataDec);
                    putPacket(
                        srcBlockDec,
                        packet,
                        symbolTimeStats,
                        decTimeStats,
                        decFailTimeStats,
                        totalDecsStats,
                        numDecFailsStats);

                    i += packet.numberOfSymbols();
                }
            }

            checkData(dataDec);
        }

        final EnumMap<StatsType, LongSummaryStatistics> map = new EnumMap<>(StatsType.class);
        map.put(StatsType.DECODER_INIT_TIME, initTimeStats);
        map.put(StatsType.SYMBOL_INPUT_TIME, symbolTimeStats);
        map.put(StatsType.DECODING_TIME, decTimeStats);
        map.put(StatsType.NUM_DECODING_FAILURES, numDecFailsStats);
        if (numDecFailsStats.getCount() > 0) {
            map.put(StatsType.DECODING_FAILURE_TIME, decFailTimeStats);
        }
        return map;
    }

    private void readBytes(ByteBuffer buf) throws IOException {

        final int pos = buf.position();
        while (buf.hasRemaining()) {
            readable.read(buf);
        }
        buf.flip().position(pos);
    }

    private EncodingPacket readPacket(DataDecoder dec) throws IOException {

        return dec.readPacketFrom(readable).value();
    }

    private ArrayDataDecoder initDataDecoder(
        FECParameters fecParams,
        int extraSymbols,
        LongSummaryStatistics initTimeStats) {

        final long startNanos = System.nanoTime();
        final ArrayDataDecoder dataDec = OpenRQ.newDecoder(fecParams, extraSymbols);
        final long endNanos = System.nanoTime();
        initTimeStats.accept(endNanos - startNanos);

        return dataDec;
    }

    private void putPacket(
        SourceBlockDecoder srcBlockDec,
        EncodingPacket packet,
        LongSummaryStatistics symbolTimeStats,
        LongSummaryStatistics decTimeStats,
        LongSummaryStatistics decFailTimeStats,
        LongSummaryStatistics totalDecsStats,
        LongSummaryStatistics numDecFailsStats)
    {

        final long startNanos = System.nanoTime();
        final SourceBlockState state = srcBlockDec.putEncodingPacket(packet);
        final long endNanos = System.nanoTime();

        final long nanos = endNanos - startNanos;
        switch (state) {
            case DECODED:
                symbolTimeStats.accept(nanos);
                decTimeStats.accept(nanos);
                totalDecsStats.accept(1);
            break;

            case DECODING_FAILURE:
                symbolTimeStats.accept(nanos);
                decTimeStats.accept(nanos);
                totalDecsStats.accept(1);
                decFailTimeStats.accept(nanos);
                numDecFailsStats.accept(1);
            break;

            case INCOMPLETE:
                symbolTimeStats.accept(nanos);
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

            final Parsed<FECParameters> parsed = FECParameters.parse(buf);
            final FECParameters fecParams = parsed.value(); // may throw ParsingFailureException with failure reason msg

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
}
