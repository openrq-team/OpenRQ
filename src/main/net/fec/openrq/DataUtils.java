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


import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import net.fec.openrq.decoder.DataDecoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.parameters.ParameterIO;
import net.fec.openrq.util.arithmetic.ExtraMath;
import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.collection.ImmutableList;
import net.fec.openrq.util.numericaltype.SizeOf;


/**
 */
final class DataUtils {

    static interface SourceBlockSupplier<SB> {

        SB get(int off, int sbn, int K);
    }


    /**
     * @param <SB>
     * @param clazz
     * @param fecParams
     * @param supplier
     * @return an immutable list of source block encoders/decoders
     */
    static <SB> ImmutableList<SB> partitionData(
        Class<SB> clazz,
        FECParameters fecParams,
        SourceBlockSupplier<SB> supplier) {

        return partitionData(clazz, fecParams, 0, supplier);
    }

    /**
     * @param <SB>
     * @param clazz
     * @param fecParams
     * @param startOffset
     * @param supplier
     * @return an immutable list of source block encoders/decoders
     */
    static <SB> ImmutableList<SB> partitionData(
        Class<SB> clazz,
        FECParameters fecParams,
        int startOffset,
        SourceBlockSupplier<SB> supplier) {

        final int Kt = fecParams.totalSymbols();
        final int Z = fecParams.numberOfSourceBlocks();

        // (KL, KS, ZL, ZS) = Partition[Kt, Z]
        final Partition KZ = new Partition(Kt, Z);
        final int KL = KZ.get(1);
        final int KS = KZ.get(2);
        final int ZL = KZ.get(3);

        // partitioned source blocks
        final SB[] srcBlocks = ArrayUtils.newArray(clazz, Z);

        /*
         * The object MUST be partitioned into Z = ZL + ZS contiguous source blocks.
         * Each source block contains a region of the data array, except the last source block
         * which may also contain extra padding.
         */

        final int T = fecParams.symbolSize();
        // source block number (index)
        int sbn;
        int off;

        for (sbn = 0, off = startOffset; sbn < ZL; sbn++, off += KL * T) { // first ZL
            srcBlocks[sbn] = supplier.get(off, sbn, KL);
        }

        for (; sbn < Z; sbn++, off += KS * T) { // last ZS
            srcBlocks[sbn] = supplier.get(off, sbn, KS);
        }

        return ImmutableList.of(srcBlocks);
    }

    /**
     * Requires valid sbn in respect to Z.
     * 
     * @param fecParams
     * @param sbn
     * @return the number of source symbols of a source block identified by the provided source block number
     */
    static int getK(FECParameters fecParams, int sbn) {

        final int Kt = fecParams.totalSymbols();
        final int Z = fecParams.numberOfSourceBlocks();

        // (KL, KS, ZL, ZS) = Partition[Kt, Z]
        final Partition KZ = new Partition(Kt, Z);
        final int KL = KZ.get(1);
        final int KS = KZ.get(2);
        final int ZL = KZ.get(3);

        return (sbn < ZL) ? KL : KS;
    }

    /**
     * @param dec
     * @param sbn
     * @param esi
     * @param symbols
     * @param copySymbols
     * @return a parsed encoding packet
     */
    static Parsed<EncodingPacket> parsePacket(DataDecoder dec, int sbn, int esi, byte[] symbols, boolean copySymbols) {

        return parsePacket(dec, sbn, esi, symbols, 0, symbols.length, copySymbols);
    }

    /**
     * @param dec
     * @param sbn
     * @param esi
     * @param symbols
     * @param off
     * @param len
     * @param copySymbols
     * @return a parsed encoding packet
     */
    static Parsed<EncodingPacket> parsePacket(
        DataDecoder dec,
        int sbn,
        int esi,
        byte[] symbols,
        int off,
        int len,
        boolean copySymbols) {

        ArrayUtils.checkArrayBounds(off, len, symbols.length);
        return parsePacket(dec, sbn, esi, ByteBuffer.wrap(symbols, off, len), copySymbols);
    }

    /**
     * @param dec
     * @param sbn
     * @param esi
     * @param symbols
     * @param copySymbols
     * @return a parsed encoding packet
     */
    static Parsed<EncodingPacket> parsePacket(DataDecoder dec, int sbn, int esi, ByteBuffer symbols, boolean copySymbols) {

        return parsePacket(dec, sbn, esi, symbols, symbols.remaining(), copySymbols);
    }

    /**
     * @param dec
     * @param ser
     * @param copySymbols
     * @return a parsed encoding packet
     */
    static Parsed<EncodingPacket> parsePacket(DataDecoder dec, SerializablePacket ser, boolean copySymbols) {

        return parsePacket(dec,
            ser.sourceBlockNumber(), ser.encodingSymbolID(), ser.symbols(), copySymbols);
    }

    /**
     * @param dec
     * @param array
     * @param copySymbols
     * @return a parsed encoding packet
     */
    static Parsed<EncodingPacket> parsePacket(DataDecoder dec, byte[] array, boolean copySymbols) {

        return parsePacket(dec, array, 0, array.length, copySymbols);
    }

    /**
     * @param dec
     * @param array
     * @param off
     * @param len
     * @param copySymbols
     * @return a parsed encoding packet
     */
    static Parsed<EncodingPacket> parsePacket(DataDecoder dec, byte[] array, int off, int len, boolean copySymbols) {

        ArrayUtils.checkArrayBounds(off, len, array.length);
        return parsePacket(dec, ByteBuffer.wrap(array, off, len), copySymbols);
    }

    /**
     * @param dec
     * @param buffer
     * @param copySymbols
     * @return a parsed encoding packet
     */
    static Parsed<EncodingPacket> parsePacket(DataDecoder dec, ByteBuffer buffer, boolean copySymbols) {

        if (buffer.remaining() < SizeOf.INT) return Parsed.invalid("FEC Payload ID is missing");
        final int fecPayloadID = buffer.getInt();

        if (buffer.remaining() < SizeOf.INT) return Parsed.invalid("size of symbols data is missing");
        final int symbLen = buffer.getInt();
        if (symbLen <= 0) return Parsed.invalid("size of symbols data is non-positive");

        final int rem = buffer.remaining();
        if (rem < symbLen) {
            return Parsed.invalid(String.format(
                "symbols data is incomplete, required %d bytes but only %d bytes are available", symbLen, rem));
        }

        final int sbn = ParameterIO.extractSourceBlockNumber(fecPayloadID);
        final int esi = ParameterIO.extractEncodingSymbolID(fecPayloadID);
        return parsePacket(dec, sbn, esi, buffer, symbLen, copySymbols);
    }

    /**
     * @param dec
     * @param in
     * @return a parsed encoding packet
     * @throws IOException
     */
    static Parsed<EncodingPacket> readPacketFrom(DataDecoder dec, DataInput in) throws IOException {

        final int fecPayloadID = in.readInt();
        final int symbLen = in.readInt();
        if (symbLen <= 0) return Parsed.invalid("size of symbols data is non-positive");

        final byte[] symbols = new byte[symbLen];
        in.readFully(symbols);

        final int sbn = ParameterIO.extractSourceBlockNumber(fecPayloadID);
        final int esi = ParameterIO.extractEncodingSymbolID(fecPayloadID);
        return parsePacket(dec, sbn, esi, symbols, false);
    }

    /**
     * @param dec
     * @param ch
     * @return a parsed encoding packet
     * @throws IOException
     */
    static Parsed<EncodingPacket> readPacketFrom(DataDecoder dec, ReadableByteChannel ch) throws IOException {

        final ByteBuffer intsBuf = ByteBuffer.allocate(SizeOf.INT + SizeOf.INT);
        while (intsBuf.hasRemaining()) {
            ch.read(intsBuf);
        }
        intsBuf.flip();

        final int fecPayloadID = intsBuf.getInt();
        final int symbLen = intsBuf.getInt();
        if (symbLen <= 0) return Parsed.invalid("size of symbols data is non-positive");

        final ByteBuffer symbols = ByteBuffer.allocate(symbLen);
        while (symbols.hasRemaining()) {
            ch.read(symbols);
        }
        symbols.flip();

        final int sbn = ParameterIO.extractSourceBlockNumber(fecPayloadID);
        final int esi = ParameterIO.extractEncodingSymbolID(fecPayloadID);
        return parsePacket(dec, sbn, esi, symbols, false);
    }

    // requires valid symbLen
    private static Parsed<EncodingPacket> parsePacket(
        DataDecoder dec,
        int sbn,
        int esi,
        ByteBuffer symbols,
        int symbLen,
        boolean copySymbols) {

        final int Z = dec.numberOfSourceBlocks();
        if (!ParameterChecker.isValidFECPayloadID(sbn, esi, Z)) {
            return Parsed.invalid(ParameterChecker.getFECPayloadIDErrorString(sbn, esi, Z));
        }

        final int T = dec.symbolSize();
        final int K = dec.sourceBlock(sbn).numberOfSourceSymbols();
        final int numSymbols = ExtraMath.ceilDiv(symbLen, T); // account for smaller last symbol
        if (numSymbols == 0) {
            return Parsed.invalid("there is no symbols data");
        }

        if (esi < K) { // source symbols
            if (numSymbols <= K - esi) {
                return Parsed.of(EncodingPacket.newSourcePacket(
                    sbn, esi, getSymbolData(symbols, symbLen, copySymbols), numSymbols));
            }
            else {
                return Parsed.invalid(String.format(
                    "an ESI of %d requires a number of source symbols (%d) of at most %d",
                    esi, numSymbols, K - esi));
            }
        }
        else { // repair symbols
            final int maxESI = ParameterChecker.maxEncodingSymbolID();
            if (numSymbols <= (1 + maxESI - esi)) {
                return Parsed.of(EncodingPacket.newRepairPacket(
                    sbn, esi, getSymbolData(symbols, symbLen, copySymbols), numSymbols));
            }
            else {
                return Parsed.invalid(String.format(
                    "an ESI of %d requires a number of repair symbols (%d) of at most %d",
                    esi, numSymbols, 1 + maxESI - esi));
            }
        }
    }

    // requires valid symbolsLen
    private static ByteBuffer getSymbolData(ByteBuffer symbols, int symbLen, boolean copySymbols) {

        if (copySymbols) {
            final ByteBuffer copy = ByteBuffer.allocate(symbLen);
            copy.put(symbols); // advances both buffer positions
            copy.flip();
            return copy;
        }
        else {
            final int prevLim = symbols.limit();
            final int sliceLim = symbols.position() + symbLen;

            // prepare slice but restore the limit afterwards
            symbols.limit(sliceLim);
            final ByteBuffer slice = symbols.slice();
            symbols.limit(prevLim);

            symbols.position(sliceLim); // advance the position
            return slice;
        }
    }
}
