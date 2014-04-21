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

package net.fec.openrq.parameters;


import net.fec.openrq.util.numericaltype.SizeOf;
import net.fec.openrq.util.numericaltype.UnsignedTypes;


/**
 * This class provides methods to read/write FEC parameters from/to a specific <i>Object Transmission Information</i>,
 * and methods to read/write encoding packet parameters from/to a <i>FEC Payload ID</i>, as specified in RFC 6330.
 */
public class ParameterIO {

    // =========== data length - F ========== //

    private static long unsignDataLength(long dataLen) {

        // 40-bit value
        return UnsignedTypes.getLongUnsignedBytes(dataLen, InternalConstants.F_num_bytes);
    }

    // For Common FEC OTI.
    private static int dataLengthShift() {

        return (1 + SizeOf.UNSIGNED_SHORT) * Byte.SIZE;
    }

    /**
     * Extracts the source data length from a <i>Common FEC Object Transmission Information</i>.
     * 
     * @param commonFecOTI
     *            A <i>Common FEC Object Transmission Information</i> as specified in RFC 6330
     * @return a source data length in number of bytes
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
     * Extracts the symbol size from a <i>Common FEC Object Transmission Information</i>.
     * 
     * @param commonFecOTI
     *            A <i>Common FEC Object Transmission Information</i> as specified in RFC 6330
     * @return a symbol size in number of bytes
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
     * Extracts the number of source blocks from a <i>Scheme-specific FEC Object Transmission Information</i>.
     * 
     * @param schemeSpecFecOTI
     *            A <i>Scheme-specific FEC Object Transmission Information</i> as specified in RFC 6330
     * @return a number of source blocks
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
     * Extracts the number of sub-blocks from a <i>Scheme-specific FEC Object Transmission Information</i>.
     * 
     * @param schemeSpecFecOTI
     *            A <i>Scheme-specific FEC Object Transmission Information</i> as specified in RFC 6330
     * @return a number of sub-blocks
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
     * Extracts the symbol alignment from a <i>Scheme-specific FEC Object Transmission Information</i>.
     * 
     * @param schemeSpecFecOTI
     *            A <i>Scheme-specific FEC Object Transmission Information</i> as specified in RFC 6330
     * @return a symbol alignment parameter
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

        return InternalConstants.ESI_num_bytes * Byte.SIZE;
    }

    /**
     * Extracts the source block number from a <i>FEC Payload ID</i>.
     * 
     * @param fecPayloadID
     *            A <i>FEC Payload ID</i> as specified in RFC 6330
     * @return a source block number
     */
    public static int extractSourceBlockNumber(int fecPayloadID) {

        return unsignSourceBlockNumber(fecPayloadID >>> sourceBlockNumberShift());
    }

    // =========== encoding symbol identifier - ESI ========== //

    private static int unsignEncodingSymbolID(int esi) {

        // 24-bit value
        return UnsignedTypes.getUnsignedBytes(esi, InternalConstants.ESI_num_bytes);
    }

    /**
     * Extracts the encoding symbol identifier from a <i>FEC Payload ID</i>.
     * 
     * @param fecPayloadID
     *            A <i>FEC Payload ID</i> as specified in RFC 6330
     * @return an encoding symbol identifier
     */
    public static int extractEncodingSymbolID(int fecPayloadID) {

        return unsignEncodingSymbolID(fecPayloadID);
    }

    // =========== Encoded Common FEC OTI - F|reserved|T ========== //

    /**
     * Constructs a <i>Common FEC Object Transmission Information</i> from the provided FEC parameters.
     * 
     * @param dataLen
     *            The source data length in number of bytes
     * @param symbolSize
     *            The symbol size in number of bytes
     * @return a <i>Common FEC Object Transmission Information</i> as specified in RFC 6330
     */
    public static long buildCommonFecOTI(long dataLen, int symbolSize) {

        final long usF = unsignDataLength(dataLen);
        final int usT = unsignSymbolSize(symbolSize);

        return (usF << dataLengthShift()) | usT;
    }

    // =========== Encoded Scheme-specific FEC OTI - Z|N|Al ========== //

    /**
     * Constructs a <i>Scheme-specific FEC Object Transmission Information</i> from the provided FEC parameters.
     * 
     * @param numSourceBlocks
     *            The number of source blocks into which the source data is divided
     * @param numSubBlocks
     *            The number of sub-blocks per source block into which the source data is divided
     * @param sAlign
     *            The symbol alignment parameter
     * @return a <i>Scheme-specific FEC Object Transmission Information</i> as specified in RFC 6330
     */
    public static int buildSchemeSpecFecOTI(int numSourceBlocks, int numSubBlocks, int sAlign) {

        final int usZ = unsignNumSourceBlocks(numSourceBlocks);
        final int usN = unsignNumSubBlocks(numSubBlocks);
        final int usAl = unsignSymbolAlignment(sAlign);

        return (usZ << numSourceBlocksShift()) | (usN << numSubBlocksShift()) | usAl;

    }

    // =========== FEC payload ID - SBN|ESI ========== //

    /**
     * Constructs a <i>FEC Payload ID</i> from the provided encoding packet parameters.
     * 
     * @param sbn
     *            The source block number
     * @param esi
     *            The encoding symbol identifier
     * @return a <i>FEC Payload ID</i> as specified in RFC 6330
     */
    public static int buildFECpayloadID(int sbn, int esi) {

        final int usSBN = unsignSourceBlockNumber(sbn);
        final int usESI = unsignEncodingSymbolID(esi);

        return (usSBN << sourceBlockNumberShift()) | usESI;
    }

    private ParameterIO() {

        // not instantiable
    }
}
