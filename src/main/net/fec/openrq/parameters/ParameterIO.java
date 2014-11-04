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

package net.fec.openrq.parameters;


import static net.fec.openrq.parameters.InternalConstants.ESI_num_bytes;
import static net.fec.openrq.parameters.InternalConstants.common_OTI_reserved_inverse_mask;
import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.datatype.UnsignedTypes;


/**
 * This class provides methods to read/write FEC parameters from/to a specific
 * <em>Object Transmission Information (OTI)</em>, and methods to read/write encoding packet parameters from/to a
 * <em>FEC Payload ID</em>, as specified in RFC 6330.
 * <p>
 * <a name="common-fec-oti">
 * <h5>Common FEC Object Transmission Information</h5></a>
 * <p>
 * The Common FEC OTI is represented as the following 8-byte bit field:
 * <pre>
 * 0 1 2 3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Source Data Length |
 * + +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | | Zeros | Symbol Size |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * <a name="schemespec-fec-oti">
 * <h5>Scheme-Specific FEC Object Transmission Information</h5></a>
 * <p>
 * The Scheme-Specific FEC OTI is represented as the following 4-byte bit field:
 * <pre>
 * 0 1 2 3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Number of | Interleaver | Symbol |
 * | Source Blocks | Length | Alignment |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * <a name="fec-payload-id">
 * <h5>FEC Payload ID</h5></a>
 * <p>
 * The FEC Payload ID is represented as the following 4-byte bit field:
 * <pre>
 * 0 1 2 3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Source Block | Encoding Symbol |
 * | Number | ID |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
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
     * Extracts the source data length from a <em>Common FEC Object Transmission Information</em>.
     * 
     * @param commonFecOTI
     *            A <em>Common FEC Object Transmission Information</em> as specified in RFC 6330
     * @return a source data length, in number of bytes
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
     * Extracts the symbol size from a <em>Common FEC Object Transmission Information</em>.
     * 
     * @param commonFecOTI
     *            A <em>Common FEC Object Transmission Information</em> as specified in RFC 6330
     * @return a symbol size, in number of bytes
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

    private static int unsignNumSourceBlocks(int numSrcBs) {

        // extended 8-bit value
        return UnsignedTypes.getExtendedUnsignedByte(numSrcBs);
    }

    // For Scheme-specific FEC OTI.
    private static int numSourceBlocksShift() {

        return (SizeOf.UNSIGNED_SHORT + 1) * Byte.SIZE;
    }

    /**
     * Extracts the number of source blocks from a <em>Scheme-specific FEC Object Transmission Information</em>.
     * 
     * @param schemeSpecFecOTI
     *            A <em>Scheme-specific FEC Object Transmission Information</em> as specified in RFC 6330
     * @return a number of source blocks
     */
    public static int extractNumSourceBlocks(int schemeSpecFecOTI) {

        return unsignNumSourceBlocks(schemeSpecFecOTI >>> numSourceBlocksShift());
    }

    // =========== interleaver length - N ========== //

    private static int unsignInterleaverLength(int interLen) {

        // 16-bit value
        return UnsignedTypes.getUnsignedShort(interLen);
    }

    // For Scheme-specific FEC OTI.
    private static int interleaverLengthShift() {

        return 1 * Byte.SIZE;
    }

    /**
     * Extracts the interleaver length from a <em>Scheme-specific FEC Object Transmission Information</em>.
     * 
     * @param schemeSpecFecOTI
     *            A <em>Scheme-specific FEC Object Transmission Information</em> as specified in RFC 6330
     * @return an interleaver length
     */
    public static int extractInterleaverLength(int schemeSpecFecOTI) {

        return unsignInterleaverLength(schemeSpecFecOTI >>> interleaverLengthShift());
    }

    // =========== symbol alignment - Al ========== //

    private static int unsignSymbolAlignment(int symbolAlign) {

        // 8-bit value
        return UnsignedTypes.getUnsignedByte(symbolAlign);
    }

    /**
     * Extracts the symbol alignment from a <em>Scheme-specific FEC Object Transmission Information</em>.
     * 
     * @param schemeSpecFecOTI
     *            A <em>Scheme-specific FEC Object Transmission Information</em> as specified in RFC 6330
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

        return ESI_num_bytes * Byte.SIZE;
    }

    /**
     * Extracts the source block number from a <em>FEC Payload ID</em>.
     * 
     * @param fecPayloadID
     *            A <em>FEC Payload ID</em> as specified in RFC 6330
     * @return a source block number
     */
    public static int extractSourceBlockNumber(int fecPayloadID) {

        return unsignSourceBlockNumber(fecPayloadID >>> sourceBlockNumberShift());
    }

    // =========== encoding symbol identifier - ESI ========== //

    private static int unsignEncodingSymbolID(int esi) {

        // 24-bit value
        return UnsignedTypes.getUnsignedBytes(esi, ESI_num_bytes);
    }

    /**
     * Extracts the encoding symbol identifier from a <em>FEC Payload ID</em>.
     * 
     * @param fecPayloadID
     *            A <em>FEC Payload ID</em> as specified in RFC 6330
     * @return an encoding symbol identifier
     */
    public static int extractEncodingSymbolID(int fecPayloadID) {

        return unsignEncodingSymbolID(fecPayloadID);
    }

    // =========== Encoded Common FEC OTI - F|reserved|T ========== //

    /**
     * Constructs a <em>Common FEC Object Transmission Information</em> from the provided FEC parameters.
     * 
     * @param dataLen
     *            The source data length, in number of bytes
     * @param symbolSize
     *            The symbol size, in number of bytes
     * @return a <em>Common FEC Object Transmission Information</em> as specified in RFC 6330
     */
    public static long buildCommonFecOTI(long dataLen, int symbolSize) {

        final long usF = unsignDataLength(dataLen);
        final int usT = unsignSymbolSize(symbolSize);

        return (usF << dataLengthShift()) | usT; // reserved bits are all zeroes
    }

    /*
     * Puts reserved bits to zeroes.
     */
    static long canonicalizeCommonFecOTI(long commonFecOTI) {

        return commonFecOTI & common_OTI_reserved_inverse_mask;
    }

    // =========== Encoded Scheme-specific FEC OTI - Z|N|Al ========== //

    /**
     * Constructs a <em>Scheme-specific FEC Object Transmission Information</em> from the provided FEC parameters.
     * 
     * @param numSrcBs
     *            The number of source blocks into which the source data is divided
     * @param interLen
     *            The interleaver length, in number of sub-blocks per source block
     * @param sAlign
     *            The symbol alignment parameter
     * @return a <em>Scheme-specific FEC Object Transmission Information</em> as specified in RFC 6330
     */
    public static int buildSchemeSpecFecOTI(int numSrcBs, int interLen, int sAlign) {

        final int usZ = unsignNumSourceBlocks(numSrcBs);
        final int usN = unsignInterleaverLength(interLen);
        final int usAl = unsignSymbolAlignment(sAlign);

        return (usZ << numSourceBlocksShift()) | (usN << interleaverLengthShift()) | usAl;

    }

    // =========== FEC payload ID - SBN|ESI ========== //

    /**
     * Constructs a <em>FEC Payload ID</em> from the provided encoding packet parameters.
     * 
     * @param sbn
     *            The source block number
     * @param esi
     *            The encoding symbol identifier
     * @return a <em>FEC Payload ID</em> as specified in RFC 6330
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
