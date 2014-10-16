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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.decoder.DataDecoder;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


/**
 * Tests the read/write methods of class SBDInfo.
 */
@RunWith(Parameterized.class)
public class SBDInfoReadWriteTest {

    private static final FECParameters FEC_PARAMS = FECParameters.newParameters(30, 1, 3);

    // for I/O
    private static ExecutorService executor;


    @BeforeClass
    public static void initExecutor() {

        executor = Executors.newFixedThreadPool(1);
    }

    @AfterClass
    public static void shutdownExecutor() throws InterruptedException {

        executor.shutdown();
        executor.awaitTermination(2L, TimeUnit.SECONDS);
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getSBDInfos() {

        final List<Object[]> list = new ArrayList<>();

        final DataEncoder dataEnc = OpenRQ.newEncoder(new byte[FEC_PARAMS.dataLengthAsInt()], FEC_PARAMS);
        final DataDecoder dataDec = OpenRQ.newDecoder(FEC_PARAMS, 0);

        for (SourceBlockDecoder sbDec : dataDec.sourceBlockIterable()) {
            // insert empty decoder
            list.add(new Object[] {sbDec.information(), true});
        }

        for (SourceBlockEncoder sbEnc : dataEnc.sourceBlockIterable()) {
            final int K = sbEnc.numberOfSourceSymbols();
            final SourceBlockDecoder sbDec = dataDec.sourceBlock(sbEnc.sourceBlockNumber());

            for (int n = 0; n < K / 3; n++) {
                final EncodingPacket pac = sbEnc.sourcePacket(n);
                sbDec.putEncodingPacket(pac);
            }
            // insert decoder with a few source symbols
            list.add(new Object[] {sbDec.information(), true});

            for (int n = K; n < 3 * K / 2; n++) {
                final EncodingPacket pac = sbEnc.repairPacket(n);
                sbDec.putEncodingPacket(pac);
            }
            // insert decoder with a few source and repair symbols
            list.add(new Object[] {sbDec.information(), true});

            for (int n = 3 * K / 2;; n++) {
                final EncodingPacket pac = sbEnc.repairPacket(n);
                sbDec.putEncodingPacket(pac);
                if (sbDec.latestState() != SourceBlockState.INCOMPLETE) {
                    break;
                }
            }
            // insert decoder already decoded or with a decoding failure
            list.add(new Object[] {sbDec.information(), true});
        }

        final int KforZeroSBN = DataUtils.getK(FEC_PARAMS, 0);

        final Set<Integer> empty = Collections.emptySet();
        final Set<Integer> zeroSrcESI = Collections.singleton(0);
        final Set<Integer> KESI = Collections.singleton(KforZeroSBN);

        final Set<Integer> invalidSrc = new HashSet<>(Arrays.asList(0, KforZeroSBN));
        final Set<Integer> invalidRep = new HashSet<>(Arrays.asList(KforZeroSBN - 1, KforZeroSBN));

        // insert a few invalid decoders
        list.add(new Object[] {SBDInfo.newInformation(-1, SourceBlockState.INCOMPLETE, empty, empty), false});
        list.add(new Object[] {SBDInfo.newInformation(0, SourceBlockState.DECODING_FAILURE, empty, empty), false});
        list.add(new Object[] {SBDInfo.newInformation(0, SourceBlockState.DECODED, zeroSrcESI, empty), false});
        list.add(new Object[] {SBDInfo.newInformation(0, SourceBlockState.INCOMPLETE, KESI, empty), false});
        list.add(new Object[] {SBDInfo.newInformation(0, SourceBlockState.INCOMPLETE, empty, zeroSrcESI), false});
        list.add(new Object[] {SBDInfo.newInformation(0, SourceBlockState.INCOMPLETE, invalidSrc, empty), false});
        list.add(new Object[] {SBDInfo.newInformation(0, SourceBlockState.INCOMPLETE, empty, invalidRep), false});

        return list;
    }


    @Parameter(0)
    public SBDInfo info;

    @Parameter(1)
    public boolean valid;


    @Test
    public void testAsSerializable() {

        final SerializableSBDInfo serInfo = info.asSerializable();
        final Parsed<SBDInfo> parsed = SBDInfo.parse(serInfo, FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }

    @Test
    public void testAsArray() {

        final byte[] array = info.asArray();
        final Parsed<SBDInfo> parsed = SBDInfo.parse(array, FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }

    @Test
    public void testWriteToArray() {

        final byte[] array = new byte[7 +
                                      2 * info.missingSourceSymbols().size() +
                                      3 * info.availableRepairSymbols().size()];

        info.writeTo(array);
        final Parsed<SBDInfo> parsed = SBDInfo.parse(array, FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteToArrayException() {

        final byte[] array = new byte[7
                                      + 2 * info.missingSourceSymbols().size()
                                      + 3 * info.availableRepairSymbols().size()
                                      - 1];
        info.writeTo(array);
    }

    @Test
    public void testWriteToArrayOffset() {

        final int off = 17;
        final byte[] array = new byte[7
                                      + 2 * info.missingSourceSymbols().size()
                                      + 3 * info.availableRepairSymbols().size()
                                      + off];

        info.writeTo(array, off);
        final Parsed<SBDInfo> parsed = SBDInfo.parse(array, off, array.length - off, FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteToArrayOffsetExceptionNegOffset() {

        final int off = -1;
        final byte[] array = new byte[7
                                      + 2 * info.missingSourceSymbols().size()
                                      + 3 * info.availableRepairSymbols().size()];

        info.writeTo(array, off);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteToArrayOffsetExceptionSmallArray() {

        final int off = 17;
        final byte[] array = new byte[7
                                      + 2 * info.missingSourceSymbols().size()
                                      + 3 * info.availableRepairSymbols().size()
                                      + off - 1];

        info.writeTo(array, off);
    }

    @Test
    public void testAsBuffer() {

        final ByteBuffer buffer = info.asBuffer();
        final Parsed<SBDInfo> parsed = SBDInfo.parse(buffer, FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }

    @Test
    public void testWriteToBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(7
                                                      + 2 * info.missingSourceSymbols().size()
                                                      + 3 * info.availableRepairSymbols().size());

        info.writeTo(buffer);
        buffer.rewind();
        final Parsed<SBDInfo> parsed = SBDInfo.parse(buffer, FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testWriteToBufferExceptionReadOnlyBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(7
                                                      + 2 * info.missingSourceSymbols().size()
                                                      + 3 * info.availableRepairSymbols().size());
        info.writeTo(buffer.asReadOnlyBuffer());
    }

    @Test(expected = BufferOverflowException.class)
    public void testWriteToBufferExceptionBufferOverflow() {

        final ByteBuffer buffer = ByteBuffer.allocate(7
                                                      + 2 * info.missingSourceSymbols().size()
                                                      + 3 * info.availableRepairSymbols().size()
                                                      - 1);
        info.writeTo(buffer);
    }

    @Test(timeout = 5000)
    public void testWriteToDataOutput() throws IOException {

        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);

        executor.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    info.writeTo(new DataOutputStream(out));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final Parsed<SBDInfo> parsed = SBDInfo.readFrom(new DataInputStream(in), FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }

    @Test(timeout = 5000)
    public void testWriteToByteChannel() throws IOException {

        final Pipe pipe = Pipe.open();

        executor.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    info.writeTo(pipe.sink());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final Parsed<SBDInfo> parsed = SBDInfo.readFrom(pipe.source(), FEC_PARAMS);

        if (valid) {
            assertEquals(info, parsed.value());
        }
        else {
            assertFalse(parsed.isValid());
        }
    }
}
