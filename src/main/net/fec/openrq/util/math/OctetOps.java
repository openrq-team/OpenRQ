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

package net.fec.openrq.util.math;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.datatype.UnsignedTypes;
import net.fec.openrq.util.io.BufferOperation;
import net.fec.openrq.util.io.ByteBuffers;


/**
 */
public final class OctetOps {

    public static int UNSIGN(int b) {

        return UnsignedTypes.getUnsignedByte(b);
    }

    public static byte alphaPower(int i) {

        return (byte)getExp(i);
    }

    public static byte aPlusB(byte u, byte v) {

        return (byte)(u ^ v);
    }

    public static long aLongPlusBLong(long a, long b) {

        return a ^ b;
    }

    public static byte aMinusB(byte u, byte v) {

        return aPlusB(u, v);
    }

    public static byte aTimesB(byte u, byte v) {

        return MULT_TABLE[UNSIGN(u)][UNSIGN(v)];
    }

    public static byte aDividedByB(byte u, byte v) {

        if (v == 0) throw new ArithmeticException("cannot divide by zero");
        return DIV_TABLE[UNSIGN(u)][UNSIGN(v)];
    }

    public static byte minByte() {

        return 0;
    }

    public static byte maxByte() {

        return (byte)-1;
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

    public static void valueVectorProduct(byte value, byte[] vector, byte[] result) {

        valueVectorProduct(value, vector, 0, result, 0, result.length);
    }

    public static void valueVectorProduct(byte value, byte[] vector, int vecPos, byte[] result, int length) {

        valueVectorProduct(value, vector, vecPos, result, 0, length);
    }

    public static void valueVectorProduct(byte value, byte[] vector, byte[] result, int resPos, int length) {

        valueVectorProduct(value, vector, 0, result, resPos, length);
    }

    public static void valueVectorProduct(byte value, byte[] vector, int vecPos, byte[] result, int resPos, int length) {

        if (value == 1) { // if multiplied by one, simply copy the source vector data and return
            if (vector != result || vecPos != resPos) { // avoid unnecessary copy if in-place product
                System.arraycopy(vector, vecPos, result, resPos, length); // uses offset and length
            }
        }
        else {
            final int resEnd = resPos + length;
            if (value == 0) { // if multiplied by zero, simply fill the result with zeros and return
                Arrays.fill(result, resPos, resEnd, (byte)0); // uses from and to indexes
            }
            else {
                for (int rr = resPos, vv = vecPos; rr < resEnd; rr++, vv++) {
                    result[rr] = aTimesB(value, vector[vv]);
                }
            }
        }
    }

    public static void valueVectorProduct(byte value, ByteBuffer vector, ByteBuffer result) {

        valueVectorProduct(value, vector, result, vector.remaining());
    }

    public static void valueVectorProduct(byte value, ByteBuffer vector, ByteBuffer result, int length) {

        if (value == 1) { // if multiplied by one, simply copy the source vector data and return
            if (vector != result) { // avoid unnecessary copy if in-place product
                BufferOperation op = BufferOperation.RESTORE_POSITION;
                ByteBuffers.copy(vector, op, result, op, length);
            }
        }
        else if (value == 0) { // if multiplied by zero, simply fill the result with zeros and return
            ByteBuffers.putZeros(result, length, BufferOperation.RESTORE_POSITION);
        }
        else {
            final int vPos = vector.position();
            final int rPos = result.position();
            final int rEnd = rPos + length;
            for (int vv = vPos, rr = rPos; rr < rEnd; vv++, rr++) {
                result.put(rr, aTimesB(value, vector.get(vv))); // absolute access to buffer
            }
        }
    }

    public static void valueVectorDivision(byte value, byte[] vector, byte[] result) {

        valueVectorDivision(value, vector, 0, result, 0, result.length);
    }

    public static void valueVectorDivision(byte value, byte[] vector, int vecPos, byte[] result, int length) {

        valueVectorDivision(value, vector, vecPos, result, 0, length);
    }

    public static void valueVectorDivision(byte value, byte[] vector, byte[] result, int resPos, int length) {

        valueVectorDivision(value, vector, 0, result, resPos, length);
    }

    public static void valueVectorDivision(byte value, byte[] vector, int vecPos, byte[] result, int resPos, int length) {

        if (value == 1) { // if divided by one, simply copy the source vector data and return
            if (vector != result || vecPos != resPos) { // avoid unnecessary copy if in-place division
                System.arraycopy(vector, vecPos, result, resPos, length); // uses offset and length
            }
        }
        else {
            final int resEnd = resPos + length;
            for (int rr = resPos, vv = vecPos; rr < resEnd; rr++, vv++) {
                result[rr] = aDividedByB(vector[vv], value);
            }
        }
    }

    public static void valueVectorDivision(byte value, ByteBuffer vector, ByteBuffer result) {

        valueVectorDivision(value, vector, result, vector.remaining());
    }

    public static void valueVectorDivision(byte value, ByteBuffer vector, ByteBuffer result, int length) {

        if (value == 1) { // if divided by one, simply copy the source vector data and return
            if (vector != result) { // avoid unnecessary copy if in-place division
                BufferOperation op = BufferOperation.RESTORE_POSITION;
                ByteBuffers.copy(vector, op, result, op, length);
            }
        }
        else {
            final int sol = SizeOf.LONG;
            final int vPos = vector.position();
            final int rPos = result.position();

            final int rEnd = rPos + length;
            final int rLongEnd = rPos + ((length / sol) * sol);

            int vv = vPos;
            int rr = rPos;
            for (; rr < rLongEnd; vv += sol, rr += sol) {
                final long quot = divisionAsLong(value, vector, vv);
                result.putLong(rr, quot);
            }

            for (; rr < rEnd; vv++, rr++) {
                final byte quot = aDividedByB(vector.get(vv), value);
                result.put(rr, quot);
            }
        }
    }

    public static void vectorVectorAddition(byte[] vector1, byte[] vector2, byte[] result) {

        vectorVectorAddition(vector1, 0, vector2, 0, result, 0, result.length);
    }

    public static void vectorVectorAddition(
        byte[] vector1,
        int vecPos1,
        byte[] vector2,
        int vecPos2,
        byte[] result,
        int resPos,
        int length)
    {

        final int resEnd = resPos + length;
        for (int v1 = vecPos1, v2 = vecPos2, r = resPos; r < resEnd; v1++, v2++, r++) {
            result[r] = aPlusB(vector1[v1], vector2[v2]);
        }
    }

    public static void vectorVectorAddition(ByteBuffer vector1, ByteBuffer vector2, ByteBuffer result) {

        vectorVectorAddition(vector1, vector2, result, result.remaining());
    }

    public static void vectorVectorAddition(ByteBuffer vector1, ByteBuffer vector2, ByteBuffer result, int length) {

        final int sol = SizeOf.LONG;
        final int v1Pos = vector1.position();
        final int v2Pos = vector2.position();
        final int rPos = result.position();

        final int rEnd = rPos + length;
        final int rLongEnd = rPos + ((length / sol) * sol);

        int v1 = v1Pos;
        int v2 = v2Pos;
        int rr = rPos;
        for (; rr < rLongEnd; v1 += sol, v2 += sol, rr += sol) {
            final long sum = aLongPlusBLong(vector1.getLong(v1), vector2.getLong(v2));
            result.putLong(rr, sum);
        }

        for (; rr < rEnd; v1++, v2++, rr++) {
            final byte sum = aPlusB(vector1.get(v1), vector2.get(v2));
            result.put(rr, sum);
        }
    }

    public static void vectorVectorAddition(byte vec1Multiplier, byte[] vector1, byte[] vector2, byte[] result) {

        vectorVectorAddition(vec1Multiplier, vector1, 0, vector2, 0, result, 0, result.length);
    }

    public static void vectorVectorAddition(
        byte vec1Multiplier,
        byte[] vector1,
        int vecPos1,
        byte[] vector2,
        int vecPos2,
        byte[] result,
        int resPos,
        int length)
    {

        if (vec1Multiplier == 1) {
            vectorVectorAddition(vector1, vecPos1, vector2, vecPos2, result, resPos, length);
        }
        else {
            final int resEnd = resPos + length;
            for (int v1 = vecPos1, v2 = vecPos2, r = resPos; r < resEnd; v1++, v2++, r++) {
                result[r] = aPlusB(aTimesB(vec1Multiplier, vector1[v1]), vector2[v2]);
            }
        }
    }

    public static void vectorVectorAddition(
        byte vec1Multiplier,
        ByteBuffer vector1,
        ByteBuffer vector2,
        ByteBuffer result)
    {

        vectorVectorAddition(vec1Multiplier, vector1, vector2, result, result.remaining());
    }

    public static void vectorVectorAddition(
        byte vec1Multiplier,
        ByteBuffer vector1,
        ByteBuffer vector2,
        ByteBuffer result,
        int length)
    {

        if (vec1Multiplier == 1) { // no need to multiply, just add
            vectorVectorAddition(vector1, vector2, result, length);
        }
        else {
            final int sol = SizeOf.LONG;
            final int v1Pos = vector1.position();
            final int v2Pos = vector2.position();
            final int rPos = result.position();

            final int rEnd = rPos + length;
            final int rLongEnd = rPos + ((length / sol) * sol);

            int v1 = v1Pos;
            int v2 = v2Pos;
            int rr = rPos;
            for (; rr < rLongEnd; v1 += sol, v2 += sol, rr += sol) {
                final long prod = productAsLong(vec1Multiplier, vector1, v1);
                final long sum = aLongPlusBLong(prod, vector2.getLong(v2));
                result.putLong(rr, sum);
            }

            for (; rr < rEnd; v1++, v2++, rr++) {
                final byte prod = aTimesB(vec1Multiplier, vector1.get(v1));
                final byte sum = aPlusB(prod, vector2.get(v2));
                result.put(rr, sum);
            }
        }
    }

    /*
     * Reads 8 bytes, multiplying each one by the multiplier,
     * and stores the products inside one long value.
     */
    private static long productAsLong(byte multiplier, ByteBuffer vector, int vecPos) {

        long ret = 0L;
        if (vector.order() == ByteOrder.BIG_ENDIAN) {
            for (int n = SizeOf.LONG - 1, vv = vecPos; n >= 0; n--, vv++) {
                ret |= (aTimesB(multiplier, vector.get(vv)) & 0xFFL) << (n * Byte.SIZE);
            }
        }
        else {
            for (int n = 0, vv = vecPos; n < SizeOf.LONG; n++, vv++) {
                ret |= (aTimesB(multiplier, vector.get(vv)) & 0xFFL) << (n * Byte.SIZE);
            }
        }

        return ret;
    }

    /*
     * Reads 8 bytes, dividing each one by the divisor,
     * and stores the quotients inside one long value.
     */
    private static long divisionAsLong(byte divisor, ByteBuffer vector, int vecPos) {

        long ret = 0L;
        if (vector.order() == ByteOrder.BIG_ENDIAN) {
            for (int n = SizeOf.LONG - 1, vv = vecPos; n >= 0; n--, vv++) {
                ret |= (aDividedByB(vector.get(vv), divisor) & 0xFFL) << (n * Byte.SIZE);
            }
        }
        else {
            for (int n = 0, vv = vecPos; n < SizeOf.LONG; n++, vv++) {
                ret |= (aDividedByB(vector.get(vv), divisor) & 0xFFL) << (n * Byte.SIZE);
            }
        }

        return ret;
    }

    private static int getExp(int i) {

        return EXP_TABLE[i];
    }

    private static int getLog(int i) {

        return LOG_TABLE[i];
    }


    private static final int[] EXP_TABLE =
    {
     1, 2, 4, 8, 16, 32, 64, 128, 29, 58, 116, 232, 205, 135, 19, 38, 76,
     152, 45, 90, 180, 117, 234, 201, 143, 3, 6, 12, 24, 48, 96, 192, 157,
     39, 78, 156, 37, 74, 148, 53, 106, 212, 181, 119, 238, 193, 159, 35,
     70, 140, 5, 10, 20, 40, 80, 160, 93, 186, 105, 210, 185, 111, 222,
     161, 95, 190, 97, 194, 153, 47, 94, 188, 101, 202, 137, 15, 30, 60,
     120, 240, 253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223, 163,
     91, 182, 113, 226, 217, 175, 67, 134, 17, 34, 68, 136, 13, 26, 52,
     104, 208, 189, 103, 206, 129, 31, 62, 124, 248, 237, 199, 147, 59,
     118, 236, 197, 151, 51, 102, 204, 133, 23, 46, 92, 184, 109, 218,
     169, 79, 158, 33, 66, 132, 21, 42, 84, 168, 77, 154, 41, 82, 164, 85,
     170, 73, 146, 57, 114, 228, 213, 183, 115, 230, 209, 191, 99, 198,
     145, 63, 126, 252, 229, 215, 179, 123, 246, 241, 255, 227, 219, 171,
     75, 150, 49, 98, 196, 149, 55, 110, 220, 165, 87, 174, 65, 130, 25,
     50, 100, 200, 141, 7, 14, 28, 56, 112, 224, 221, 167, 83, 166, 81,
     162, 89, 178, 121, 242, 249, 239, 195, 155, 43, 86, 172, 69, 138, 9,
     18, 36, 72, 144, 61, 122, 244, 245, 247, 243, 251, 235, 203, 139, 11,
     22, 44, 88, 176, 125, 250, 233, 207, 131, 27, 54, 108, 216, 173, 71,
     142, 1, 2, 4, 8, 16, 32, 64, 128, 29, 58, 116, 232, 205, 135, 19, 38,
     76, 152, 45, 90, 180, 117, 234, 201, 143, 3, 6, 12, 24, 48, 96, 192,
     157, 39, 78, 156, 37, 74, 148, 53, 106, 212, 181, 119, 238, 193, 159,
     35, 70, 140, 5, 10, 20, 40, 80, 160, 93, 186, 105, 210, 185, 111,
     222, 161, 95, 190, 97, 194, 153, 47, 94, 188, 101, 202, 137, 15, 30,
     60, 120, 240, 253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223,
     163, 91, 182, 113, 226, 217, 175, 67, 134, 17, 34, 68, 136, 13, 26,
     52, 104, 208, 189, 103, 206, 129, 31, 62, 124, 248, 237, 199, 147,
     59, 118, 236, 197, 151, 51, 102, 204, 133, 23, 46, 92, 184, 109, 218,
     169, 79, 158, 33, 66, 132, 21, 42, 84, 168, 77, 154, 41, 82, 164, 85,
     170, 73, 146, 57, 114, 228, 213, 183, 115, 230, 209, 191, 99, 198,
     145, 63, 126, 252, 229, 215, 179, 123, 246, 241, 255, 227, 219, 171,
     75, 150, 49, 98, 196, 149, 55, 110, 220, 165, 87, 174, 65, 130, 25,
     50, 100, 200, 141, 7, 14, 28, 56, 112, 224, 221, 167, 83, 166, 81,
     162, 89, 178, 121, 242, 249, 239, 195, 155, 43, 86, 172, 69, 138, 9,
     18, 36, 72, 144, 61, 122, 244, 245, 247, 243, 251, 235, 203, 139, 11,
     22, 44, 88, 176, 125, 250, 233, 207, 131, 27, 54, 108, 216, 173, 71,
     142
    };

    private static final int[] LOG_TABLE =
    {
     0, 1, 25, 2, 50, 26, 198, 3, 223, 51, 238, 27, 104, 199, 75, 4, 100,
     224, 14, 52, 141, 239, 129, 28, 193, 105, 248, 200, 8, 76, 113, 5,
     138, 101, 47, 225, 36, 15, 33, 53, 147, 142, 218, 240, 18, 130, 69,
     29, 181, 194, 125, 106, 39, 249, 185, 201, 154, 9, 120, 77, 228, 114,
     166, 6, 191, 139, 98, 102, 221, 48, 253, 226, 152, 37, 179, 16, 145,
     34, 136, 54, 208, 148, 206, 143, 150, 219, 189, 241, 210, 19, 92,
     131, 56, 70, 64, 30, 66, 182, 163, 195, 72, 126, 110, 107, 58, 40,
     84, 250, 133, 186, 61, 202, 94, 155, 159, 10, 21, 121, 43, 78, 212,
     229, 172, 115, 243, 167, 87, 7, 112, 192, 247, 140, 128, 99, 13, 103,
     74, 222, 237, 49, 197, 254, 24, 227, 165, 153, 119, 38, 184, 180,
     124, 17, 68, 146, 217, 35, 32, 137, 46, 55, 63, 209, 91, 149, 188,
     207, 205, 144, 135, 151, 178, 220, 252, 190, 97, 242, 86, 211, 171,
     20, 42, 93, 158, 132, 60, 57, 83, 71, 109, 65, 162, 31, 45, 67, 216,
     183, 123, 164, 118, 196, 23, 73, 236, 127, 12, 111, 246, 108, 161,
     59, 82, 41, 157, 85, 170, 251, 96, 134, 177, 187, 204, 62, 90, 203,
     89, 95, 176, 156, 169, 160, 81, 11, 245, 22, 235, 122, 117, 44, 215,
     79, 174, 213, 233, 230, 231, 173, 232, 116, 214, 244, 234, 168, 80,
     88, 175
    };

    private static final byte[][] MULT_TABLE;
    private static final byte[][] DIV_TABLE;

    static {
        final int size = 1 << Byte.SIZE;

        MULT_TABLE = new byte[size][size];
        DIV_TABLE = new byte[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                MULT_TABLE[x][y] = expLogATimesB((byte)x, (byte)y);
                if (y != 0) {
                    DIV_TABLE[x][y] = expLogADividedByB((byte)x, (byte)y);
                }
            }
        }
    }


    private static byte expLogATimesB(byte u, byte v) {

        if (u == 0 || v == 0) return 0;
        if (u == 1) return v;
        if (v == 1) return u;

        return (byte)getExp(getLog(UNSIGN(u - 1)) + getLog(UNSIGN(v - 1)));
    }

    private static byte expLogADividedByB(byte u, byte v) {

        if (v == 0) throw new ArithmeticException("cannot divide by zero");
        if (u == 0) return 0;

        return (byte)getExp(getLog(UNSIGN(u - 1)) - getLog(UNSIGN(v - 1)) + 255);
    }

    private OctetOps() {

        // not instantiable
    }
}
