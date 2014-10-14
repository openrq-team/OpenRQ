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
package net.fec.openrq.util.linearalgebra.serialize;


import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import net.fec.openrq.util.numericaltype.SizeOf;
import net.fec.openrq.util.numericaltype.UnsignedTypes;
import net.fec.openrq.util.text.Words;


/**
 * 
 */
public final class Serialization {

    public static enum Type {

        DENSE_VECTOR,
        SPARSE_VECTOR,
        DENSE_1D_MATRIX,
        DENSE_2D_MATRIX,
        SPARSE_ROW_MATRIX,
        SPARSE_COLUMN_MATRIX;

        private static byte typeToByte(Type t) {

            return (byte)t.ordinal();
        }

        private static Type byteToType(byte b) {

            return values()[UnsignedTypes.getUnsignedByte(b)];
        }

        private static boolean isInvalidByte(byte b) {

            final int unsigned = UnsignedTypes.getUnsignedByte(b);
            return unsigned < 0 || values().length <= unsigned;
        }
    }


    public static final int SERIALIZATION_TYPE_NUMBYTES = SizeOf.UNSIGNED_BYTE;
    public static final int VECTOR_LENGTH_NUMBYTES = SizeOf.INT;
    public static final int VECTOR_INDEX_NUMBYTES = SizeOf.INT;
    public static final int VECTOR_CARDINALITY_NUMBYTES = SizeOf.INT;
    public static final int MATRIX_ROWS_NUMBYTES = SizeOf.INT;
    public static final int MATRIX_COLUMNS_NUMBYTES = SizeOf.INT;
    public static final int MATRIX_ROW_INDEX_NUMBYTES = SizeOf.INT;
    public static final int MATRIX_COLUMN_INDEX_NUMBYTES = SizeOf.INT;
    public static final int MATRIX_ROW_CARDINALITY_NUMBYTES = SizeOf.INT;
    public static final int MATRIX_COLUMN_CARDINALITY_NUMBYTES = SizeOf.INT;


    public static void writeType(Type type, ByteBuffer buffer) {

        writeByte(buffer, Type.typeToByte(type), "serialization type");
    }

    public static Type readType(ByteBuffer buffer) throws DeserializationException {

        final byte type = readByte(buffer, "serialization type");
        if (Type.isInvalidByte(type)) throw new DeserializationException("invalid serialization type");
        return Type.byteToType(type);
    }

    public static void writeVectorLength(int length, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, length, "vector length");
    }

    public static int readVectorLength(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "vector length");
    }

    public static void writeVectorCardinality(int length, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, length, "vector cardinality");
    }

    public static int readVectorCardinality(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "vector cardinality");
    }

    public static void writeVectorIndex(int index, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, index, "vector index");
    }

    public static int readVectorIndex(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "vector index");
    }

    public static void writeVectorValue(byte value, ByteBuffer buffer) {

        writeByte(buffer, value, "vector value");
    }

    public static byte readVectorValue(ByteBuffer buffer) throws DeserializationException {

        return readByte(buffer, "vector value");
    }

    public static void writeMatrixRows(int rows, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, rows, "matrix rows");
    }

    public static int readMatrixRows(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "matrix rows");
    }

    public static void writeMatrixColumns(int columns, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, columns, "matrix columns");
    }

    public static int readMatrixColumns(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "matrix columns");
    }

    public static void writeMatrixRowCardinality(int rows, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, rows, "matrix row cardinality");
    }

    public static int readMatrixRowCardinality(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "matrix row cardinality");
    }

    public static void writeMatrixColumnCardinality(int columns, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, columns, "matrix column cardinality");
    }

    public static int readMatrixColumnCardinality(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "matrix column cardinality");
    }

    public static void writeMatrixRowIndex(int index, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, index, "matrix row index");
    }

    public static int readMatrixRowIndex(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "matrix row index");
    }

    public static void writeMatrixColumnIndex(int index, ByteBuffer buffer) {

        writeNonNegativeInt(buffer, index, "matrix column index");
    }

    public static int readMatrixColumnIndex(ByteBuffer buffer) throws DeserializationException {

        return readNonNegative(buffer, "matrix column index");
    }

    public static void writeMatrixValue(byte value, ByteBuffer buffer) {

        writeByte(buffer, value, "matrix value");
    }

    public static byte readMatrixValue(ByteBuffer buffer) throws DeserializationException {

        return readByte(buffer, "matrix value");
    }

    private static void writeByte(ByteBuffer buffer, byte value, String target) {

        assertAvailableInBuffer(buffer, SizeOf.BYTE, target);
        buffer.put(value);
    }

    private static byte readByte(ByteBuffer buffer, String target) throws DeserializationException {

        try {
            return buffer.get();
        }
        catch (BufferUnderflowException e) {
            throw new DeserializationException("incomplete " + target);
        }
    }

    private static void writeNonNegativeInt(ByteBuffer buffer, int value, String target) {

        if (value < 0) throw new IllegalArgumentException(target + " is negative");
        assertAvailableInBuffer(buffer, SizeOf.INT, target);
        buffer.putInt(value);
    }

    private static int readNonNegative(ByteBuffer buffer, String target) throws DeserializationException {

        try {
            final int value = buffer.getInt();
            if (value < 0) throw new DeserializationException(target + " is negative");
            return value;
        }
        catch (BufferUnderflowException e) {
            throw new DeserializationException("incomplete " + target);
        }
    }

    private static void assertAvailableInBuffer(ByteBuffer buffer, int numBytes, String target) {

        if (buffer.remaining() < numBytes) {
            throw new IllegalArgumentException(
                "buffer must have at least " + Words.bytes(numBytes) + " available for " + target);
        }
    }

    private Serialization() {

        // not instantiable
    }
}
