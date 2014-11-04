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
package net.fec.openrq.util.linearalgebra.serialize;


import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.fec.openrq.util.datatype.SizeOf;
import net.fec.openrq.util.datatype.UnsignedTypes;
import net.fec.openrq.util.io.ExtraChannels;
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

        private static byte toByte(Type t) {

            return (byte)t.ordinal();
        }

        private static Type fromByte(byte b) {

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


    public static void writeType(ByteBuffer buffer, Type type) {

        writeByte(buffer, Type.toByte(type), "serialization type");
    }

    public static void writeType(WritableByteChannel buffer, Type type) throws IOException {

        writeByte(buffer, Type.toByte(type));
    }

    public static Type readType(ByteBuffer buffer) throws DeserializationException {

        final byte type = readByte(buffer, "serialization type");
        if (Type.isInvalidByte(type)) throw new DeserializationException("invalid serialization type");
        return Type.fromByte(type);
    }

    public static Type readType(ReadableByteChannel ch) throws IOException, DeserializationException {

        final byte type = readByte(ch);
        if (Type.isInvalidByte(type)) throw new DeserializationException("invalid serialization type");
        return Type.fromByte(type);
    }

    public static void writeVectorLength(ByteBuffer buffer, int length) {

        writeNonNegativeInt(buffer, length, "vector length");
    }

    public static void writeVectorLength(WritableByteChannel ch, int length) throws IOException {

        writeNonNegativeInt(ch, length, "vector length");
    }

    public static int readVectorLength(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "vector length");
    }

    public static int readVectorLength(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "vector length");
    }

    public static void writeVectorCardinality(ByteBuffer buffer, int cardinality) {

        writeNonNegativeInt(buffer, cardinality, "vector cardinality");
    }

    public static void writeVectorCardinality(WritableByteChannel ch, int cardinality) throws IOException {

        writeNonNegativeInt(ch, cardinality, "vector cardinality");
    }

    public static int readVectorCardinality(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "vector cardinality");
    }

    public static int readVectorCardinality(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "vector cardinality");
    }

    public static void writeVectorIndex(ByteBuffer buffer, int index) {

        writeNonNegativeInt(buffer, index, "vector index");
    }

    public static void writeVectorIndex(WritableByteChannel ch, int index) throws IOException {

        writeNonNegativeInt(ch, index, "vector index");
    }

    public static int readVectorIndex(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "vector index");
    }

    public static int readVectorIndex(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "vector index");
    }

    public static void writeVectorValue(ByteBuffer buffer, byte value) {

        writeByte(buffer, value, "vector value");
    }

    public static void writeVectorValue(WritableByteChannel ch, byte value) throws IOException {

        writeByte(ch, value);
    }

    public static byte readVectorValue(ByteBuffer buffer) throws DeserializationException {

        return readByte(buffer, "vector value");
    }

    public static byte readVectorValue(ReadableByteChannel ch) throws IOException {

        return readByte(ch);
    }

    public static void writeMatrixRows(ByteBuffer buffer, int rows) {

        writeNonNegativeInt(buffer, rows, "matrix rows");
    }

    public static void writeMatrixRows(WritableByteChannel ch, int rows) throws IOException {

        writeNonNegativeInt(ch, rows, "matrix rows");
    }

    public static int readMatrixRows(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "matrix rows");
    }

    public static int readMatrixRows(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "matrix rows");
    }

    public static void writeMatrixColumns(ByteBuffer buffer, int columns) {

        writeNonNegativeInt(buffer, columns, "matrix columns");
    }

    public static void writeMatrixColumns(WritableByteChannel ch, int columns) throws IOException {

        writeNonNegativeInt(ch, columns, "matrix columns");
    }

    public static int readMatrixColumns(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "matrix columns");
    }

    public static int readMatrixColumns(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "matrix columns");
    }

    public static void writeMatrixRowCardinality(ByteBuffer buffer, int rowCard) {

        writeNonNegativeInt(buffer, rowCard, "matrix row cardinality");
    }

    public static void writeMatrixRowCardinality(WritableByteChannel ch, int rowCard) throws IOException {

        writeNonNegativeInt(ch, rowCard, "matrix row cardinality");
    }

    public static int readMatrixRowCardinality(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "matrix row cardinality");
    }

    public static int readMatrixRowCardinality(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "matrix row cardinality");
    }

    public static void writeMatrixColumnCardinality(ByteBuffer buffer, int columnCard) {

        writeNonNegativeInt(buffer, columnCard, "matrix column cardinality");
    }

    public static void writeMatrixColumnCardinality(WritableByteChannel ch, int columnCard) throws IOException {

        writeNonNegativeInt(ch, columnCard, "matrix column cardinality");
    }

    public static int readMatrixColumnCardinality(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "matrix column cardinality");
    }

    public static int readMatrixColumnCardinality(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "matrix column cardinality");
    }

    public static void writeMatrixRowIndex(ByteBuffer buffer, int index) {

        writeNonNegativeInt(buffer, index, "matrix row index");
    }

    public static void writeMatrixRowIndex(WritableByteChannel ch, int index) throws IOException {

        writeNonNegativeInt(ch, index, "matrix row index");
    }

    public static int readMatrixRowIndex(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "matrix row index");
    }

    public static int readMatrixRowIndex(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "matrix row index");
    }

    public static void writeMatrixColumnIndex(ByteBuffer buffer, int index) {

        writeNonNegativeInt(buffer, index, "matrix column index");
    }

    public static void writeMatrixColumnIndex(WritableByteChannel ch, int index) throws IOException {

        writeNonNegativeInt(ch, index, "matrix column index");
    }

    public static int readMatrixColumnIndex(ByteBuffer buffer) throws DeserializationException {

        return readNonNegativeInt(buffer, "matrix column index");
    }

    public static int readMatrixColumnIndex(ReadableByteChannel ch) throws IOException, DeserializationException {

        return readNonNegativeInt(ch, "matrix column index");
    }

    public static void writeMatrixValue(ByteBuffer buffer, byte value) {

        writeByte(buffer, value, "matrix value");
    }

    public static void writeMatrixValue(WritableByteChannel ch, byte value) throws IOException {

        writeByte(ch, value);
    }

    public static byte readMatrixValue(ByteBuffer buffer) throws DeserializationException {

        return readByte(buffer, "matrix value");
    }

    public static byte readMatrixValue(ReadableByteChannel ch) throws IOException {

        return readByte(ch);
    }

    private static void writeByte(ByteBuffer buffer, byte value, String target) {

        assertAvailableInBuffer(buffer, SizeOf.BYTE, target);
        buffer.put(value);
    }

    private static void writeByte(WritableByteChannel ch, byte value) throws IOException {

        ExtraChannels.writeByte(ch, value);
    }

    private static byte readByte(ByteBuffer buffer, String target) throws DeserializationException {

        try {
            return buffer.get();
        }
        catch (BufferUnderflowException e) {
            throw new DeserializationException("incomplete " + target);
        }
    }

    private static byte readByte(ReadableByteChannel ch) throws IOException {

        return ExtraChannels.readByte(ch);
    }

    private static void writeNonNegativeInt(ByteBuffer buffer, int value, String target) {

        if (value < 0) throw new IllegalArgumentException(target + " is negative");
        assertAvailableInBuffer(buffer, SizeOf.INT, target);
        buffer.putInt(value);
    }

    private static void writeNonNegativeInt(WritableByteChannel ch, int value, String target) throws IOException {

        if (value < 0) throw new IllegalArgumentException(target);
        ExtraChannels.writeInt(ch, value);
    }

    private static int readNonNegativeInt(ByteBuffer buffer, String target) throws DeserializationException {

        try {
            final int value = buffer.getInt();
            if (value < 0) throw new DeserializationException(target + " is negative");
            return value;
        }
        catch (BufferUnderflowException e) {
            throw new DeserializationException("incomplete " + target);
        }
    }

    private static int readNonNegativeInt(ReadableByteChannel ch, String target)
        throws IOException,
        DeserializationException
    {

        final int value = ExtraChannels.readInt(ch);
        if (value < 0) throw new DeserializationException(target + " is negative");
        return value;
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
