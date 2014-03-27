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

package net.fec.openrq.core.parameters;


import java.nio.ByteBuffer;

import net.fec.openrq.core.util.numericaltype.SizeOf;
import net.fec.openrq.core.util.numericaltype.UnsignedTypes;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public class ParameterIO {

    // =========== data length - F ========== //

    private static long unsignDataLength(long dataLen) {

        // 40-bit value
        return UnsignedTypes.getLongUnsignedBytes(dataLen, InternalConstants.NUM_BYTES_F);
    }

    // For Common FEC OTI.
    private static int dataLengthShift() {

        return (1 + SizeOf.UNSIGNED_SHORT) * Byte.SIZE;
    }

    /**
     * @param commonFecOTI
     * @return
     */
    public static long extractDataLength(long commonFecOTI) {

        return unsignDataLength(commonFecOTI >>> dataLengthShift());
    }

    // =========== symbol size - T ========== //

    private static int unsignSymbolSize(int symbolSize) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(symbolSize);
    }

    /**
     * @param commonFecOTI
     * @return
     */
    public static int extractSymbolSize(long commonFecOTI) {

        return unsignSymbolSize((int)commonFecOTI);
    }

    // =========== number of source blocks - Z ========== //

    /*
     * The RFC specifies a minimum of 1 and a maximum of 2^8 for the number of source blocks.
     * An unsigned byte only fits values from [0, (2^8)-1].
     * So, consider value 0 as 2^8 (unsignedByte(2^8) == 0)
     */

    private static int unsignNumSourceBlocks(int numSourceBlocks) {

        // extended 8-bit value
        return UnsignedTypes.getExtendedUnsignedByte(numSourceBlocks);
    }

    // For Scheme-specific FEC OTI.
    private static int numSourceBlocksShift() {

        return (SizeOf.UNSIGNED_SHORT + 1) * Byte.SIZE;
    }

    /**
     * @param schemeSpecFecOTI
     * @return
     */
    public static int extractNumSourceBlocks(int schemeSpecFecOTI) {

        return unsignNumSourceBlocks(schemeSpecFecOTI >>> numSourceBlocksShift());
    }

    // =========== number of sub-blocks - N ========== //

    private static int unsignNumSubBlocks(int numSubBlocks) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(numSubBlocks);
    }

    // For Scheme-specific FEC OTI.
    private static int numSubBlocksShift() {

        return 1 * Byte.SIZE;
    }

    /**
     * @param schemeSpecFecOTI
     * @return
     */
    public static int extractNumSubBlocks(int schemeSpecFecOTI) {

        return unsignNumSubBlocks(schemeSpecFecOTI >>> numSubBlocksShift());
    }

    // =========== symbol alignment - Al ========== //

    private static int unsignSymbolAlignment(int symbolAlign) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(symbolAlign);
    }

    /**
     * @param schemeSpecFecOTI
     * @return
     */
    public static int extractSymbolAlignment(int schemeSpecFecOTI) {

        return unsignSymbolAlignment(schemeSpecFecOTI);
    }

    // =========== source block number - SBN ========== //

    private static int unsignSourceBlockNumber(int sbn) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(sbn);
    }

    // For FEC Payload ID.
    private static int sourceBlockNumberShift() {

        return InternalConstants.NUM_BYTES_ESI * Byte.SIZE;
    }

    /**
     * @param fecPayloadID
     * @return
     */
    public static int extractSourceBlockNumber(int fecPayloadID) {

        return unsignSourceBlockNumber(fecPayloadID >>> sourceBlockNumberShift());
    }

    // =========== encoding symbol identifier - ESI ========== //

    private static int unsignEncodingSymbolID(int esi) {

        // 24-bit value
        return UnsignedTypes.getUnsignedBytes(esi, InternalConstants.NUM_BYTES_ESI);
    }

    /**
     * @param fecPayloadID
     * @return
     */
    public static int extractEncodingSymbolID(int fecPayloadID) {

        return unsignEncodingSymbolID(fecPayloadID);
    }

    // =========== Encoded Common FEC OTI - F|reserved|T ========== //

    /**
     * @param dataLen
     * @param symbolSize
     * @return
     */
    public static long buildCommonFecOTI(long dataLen, int symbolSize) {

        final long usF = unsignDataLength(dataLen);
        final int usT = unsignSymbolSize(symbolSize);

        return (usF << dataLengthShift()) | usT;
    }

    /**
     * @param commonFecOTI
     * @param buffer
     */
    public static void writeCommonFecOTI(long commonFecOTI, ByteBuffer buffer) {

        // 64-bit value
        buffer.putLong(commonFecOTI);
    }

    /**
     * @param buffer
     * @return
     */
    public static long readCommonFecOTI(ByteBuffer buffer) {

        // 64-bit value
        return buffer.getLong();
    }

    // =========== Encoded Scheme-specific FEC OTI - Z|N|Al ========== //

    /**
     * @param numSourceBlocks
     * @param numSubBlocks
     * @param symbolAlign
     * @return
     */
    public static int buildSchemeSpecFecOTI(int numSourceBlocks, int numSubBlocks) {

        final int usZ = unsignNumSourceBlocks(numSourceBlocks);
        final int usN = unsignNumSubBlocks(numSubBlocks);
        final int usAl = unsignSymbolAlignment(InternalConstants.ALIGN_VALUE);

        return (usZ << numSourceBlocksShift()) | (usN << numSubBlocksShift()) | usAl;

    }

    /**
     * @param schemeSpecFecOTI
     * @param buffer
     */
    public static void writeSchemeSpecFecOTI(int schemeSpecFecOTI, ByteBuffer buffer) {

        // 32-bit value
        buffer.putInt(schemeSpecFecOTI);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readSchemeSpecFecOTI(ByteBuffer buffer) {

        // 32-bit value
        return buffer.getInt();
    }

    // =========== FEC payload ID - SBN|ESI ========== //

    /**
     * @param sbn
     * @param esi
     * @return
     */
    public static int buildFECpayloadID(int sbn, int esi) {

        final int usSBN = unsignSourceBlockNumber(sbn);
        final int usESI = unsignEncodingSymbolID(esi);

        return (usSBN << sourceBlockNumberShift()) | usESI;
    }

    /**
     * @param fecPayloadID
     * @param buffer
     */
    public static void writeFECpayloadID(int fecPayloadID, ByteBuffer buffer) {

        // 32-bit value
        buffer.putInt(fecPayloadID);
    }

    /**
     * @param buffer
     * @return
     */
    public static int readFECpayloadID(ByteBuffer buffer) {

        // 32-bit value
        return buffer.getInt();
    }

    private ParameterIO() {

        // not instantiable
    }
}
