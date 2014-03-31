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

package net.fec.openrq.core;


import net.fec.openrq.core.parameters.FECParameters;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class OpenRQ {

    /**
     * @param fecParams
     * @param data
     * @return
     */
    public static ArrayDataEncoder newEncoder(byte[] data, FECParameters fecParams) {

        return newEncoder(data, 0, fecParams);
    }

    /**
     * @param fecParams
     * @param data
     * @param offset
     * @return
     */
    public static ArrayDataEncoder newEncoder(byte[] data, int offset, FECParameters fecParams) {

        final long longDataLen = fecParams.dataLength();
        if (longDataLen > Integer.MAX_VALUE) throw new IllegalArgumentException("data length is too large");
        checkArrayBounds(offset, (int)longDataLen, data.length);

        return ArrayDataEncoder.newEncoder(data, offset, fecParams);
    }

    /**
     * @param fecParams
     * @param extraSymbols
     * @return
     */
    public static ArrayDataDecoder newDecoder(FECParameters fecParams, int extraSymbols) {

        return ArrayDataDecoder.newDecoder(fecParams, extraSymbols);
    }

    private static final void checkArrayBounds(int arrOff, int arrLen, int length) {

        // retrieved from java.nio.Buffer class
        if ((arrOff | arrLen | (arrOff + arrLen) | (length - (arrOff + arrLen))) < 0) {
            throw new IndexOutOfBoundsException(getArrayBoundsMsg(arrOff, arrLen, length));
        }
    }

    // separate method in order to avoid the string concatenation in cases where the exception is NOT thrown
    private static final String getArrayBoundsMsg(int off, int len, int arrLength) {

        return "region off = " + off + "; region length = " + len + "; array length = " + arrLength;
    }

    private OpenRQ() {

        // not instantiable
    }
}
