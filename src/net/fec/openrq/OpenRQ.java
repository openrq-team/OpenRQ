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


import net.fec.openrq.decoder.DataDecoder;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.array.ArrayUtils;


/**
 * The entry point for the OpenRQ API.
 * <p>
 * This class provides methods for creating encoder objects from source data and FEC parameters, and methods for
 * creating decoder objects that decode source data according to FEC parameters.
 * <p>
 * This class also provides miscellaneous utility methods.
 */
public final class OpenRQ {

    /**
     * Returns a {@link DataEncoder} object with an array of bytes as the source data, configured according to the
     * provided FEC parameters.
     * 
     * @param fecParams
     *            FEC parameters that configure the returned data encoder object
     * @param data
     *            An array of bytes containing the source data to be encoded
     * @return a data encoder object backed by an array of bytes
     */
    public static ArrayDataEncoder newEncoder(byte[] data, FECParameters fecParams) {

        return newEncoder(data, 0, fecParams);
    }

    /**
     * Returns a {@link DataEncoder} object with an array of bytes as the source data, configured according to the
     * provided FEC parameters.
     * 
     * @param fecParams
     *            FEC parameters that configure the returned data encoder object
     * @param data
     *            An array of bytes containing the source data to be encoded
     * @param offset
     *            The index in the array where the source data begins
     * @return a data encoder object backed by an array of bytes
     */
    public static ArrayDataEncoder newEncoder(byte[] data, int offset, FECParameters fecParams) {

        final long longDataLen = fecParams.dataLength();
        if (longDataLen > Integer.MAX_VALUE) throw new IllegalArgumentException("data length is too large");
        ArrayUtils.checkArrayBounds(offset, (int)longDataLen, data.length);

        return ArrayDataEncoder.newEncoder(data, offset, fecParams);
    }

    /**
     * Returns a {@link DataDecoder} object that decodes source data into an array of bytes, configured according to the
     * provided FEC parameters and repair symbol overhead.
     * <p>
     * The repair symbol overhead is given as a number of extra repair symbols that add up together with the amount of
     * source symbols as the total number of encoding symbols that trigger a decoding operation. The larger the symbol
     * overhead, the less likely is a decoding failure to occur, at the cost of more data to be transmitted. Naturally,
     * this only applies when some source symbols are missing and repair symbols are available to substitute them.
     * 
     * @param fecParams
     *            FEC parameters that configure the returned data decoder object
     * @param extraSymbols
     *            Repair symbol overhead (must be non-negative)
     * @return a data decoder object that decodes source data into an array of bytes
     */
    public static ArrayDataDecoder newDecoder(FECParameters fecParams, int extraSymbols) {

        return ArrayDataDecoder.newDecoder(fecParams, extraSymbols);
    }

    /**
     * Calculates the minimum number of repair symbols from a source block to be transmitted for a given network loss
     * rate.
     * 
     * @param numSourceSymbols
     *            The number of source symbols in the source block
     * @param extraSymbols
     *            Number of extra repair symbols necessary for decoding
     * @param loss
     *            The expected network loss rate (must be between 0 and 1).
     * @return the minimum number of repair symbols that should be transmitted
     */
    public static final int minRepairSymbols(int numSourceSymbols, int extraSymbols, double loss) {

        if (numSourceSymbols < 1 || numSourceSymbols > ParameterChecker.maxNumSourceSymbolsPerBlock()) {
            throw new IllegalArgumentException("invalid number of source symbols");
        }
        if (extraSymbols < 0 || extraSymbols > ParameterChecker.maxEncodingSymbolID() - numSourceSymbols) {
            throw new IllegalArgumentException("invalid number of extra symbols");
        }
        if (loss < 0.0 || loss > 1.0) {
            throw new IllegalArgumentException("invalid network loss rate");
        }

        double temp_var = loss;

        // calculate
        temp_var *= numSourceSymbols;
        temp_var += extraSymbols;
        temp_var /= (1 - loss);

        // ceil to an integer and return
        return (int)Math.ceil(temp_var);
    }

    private OpenRQ() {

        // not instantiable
    }
}
