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
package net.fec.openrq.util.linearalgebra;


import net.fec.openrq.util.rq.OctetOps;


/**
 * This class includes arithmetic operations over bytes.
 */
public final class ByteOps {

    public static byte aPlusB(byte a, byte b) {

        return OctetOps.addition(a, b);
    }

    public static byte aMinusB(byte a, byte b) {

        return OctetOps.subtraction(a, b);
    }

    public static byte aTimesB(byte a, byte b) {

        return OctetOps.product(a, b);
    }

    public static byte aDividedByB(byte a, byte b) {

        return OctetOps.division(a, b);
    }

    public static byte minByte() {

        return 0;
    }

    public static byte maxByte() {

        return (byte)-1;
    }

    // for completeness sake
    public static boolean aIsEqualToB(byte a, byte b) {

        return a == b;
    }

    public static boolean aIsLessThanB(byte a, byte b) {

        return OctetOps.UNSIGN(a) < OctetOps.UNSIGN(b);
    }

    public static boolean aIsLessThanOrEqualToB(byte a, byte b) {

        return OctetOps.UNSIGN(a) <= OctetOps.UNSIGN(b);
    }

    public static boolean aIsGreaterThanB(byte a, byte b) {

        return OctetOps.UNSIGN(a) > OctetOps.UNSIGN(b);
    }

    public static boolean aIsGreaterThanOrEqualToB(byte a, byte b) {

        return OctetOps.UNSIGN(a) >= OctetOps.UNSIGN(b);
    }

    public static byte maxOfAandB(byte a, byte b) {

        return aIsGreaterThanOrEqualToB(a, b) ? a : b;
    }

    public static byte minOfAandB(byte a, byte b) {

        return aIsLessThanOrEqualToB(a, b) ? a : b;
    }

    private ByteOps() {

        // not instantiable
    }
}
