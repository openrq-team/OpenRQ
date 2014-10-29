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
package net.fec.openrq;


import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Objects;

import net.fec.openrq.util.array.ArrayIO;
import net.fec.openrq.util.datatype.UnsignedTypes;
import net.fec.openrq.util.io.ExtraChannels;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.dense.RowIndirected2DByteMatrix;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.dense.BasicByteVector;
import net.fec.openrq.util.math.OctetOps;


/**
 * 
 */
final class ISDOps {

    static ISDOperation newPhase1Operation(byte srcMult, int srcRow, int dstRow) {

        return new SymbolAddition(srcMult, srcRow, dstRow);
    }

    static ISDOperation newPhase2Operation(ByteMatrix A, int fromRow, int toRow, int fromCol, int toCol, int[] d) {

        return new ReduceMatrixToRowEchelon(A, fromRow, toRow, fromCol, toCol, d);
    }

    static ISDOperation newPhase3Operation(ByteMatrix X, int Xrows, int Xcols, int[] d) {

        return new MatrixVectorMultiplication(X, Xrows, Xcols, d);
    }

    static ISDOperation newPhase4Operation(byte srcMult, int srcRow, int dstRow) {

        return new SymbolAddition(srcMult, srcRow, dstRow);
    }

    static ISDOperation newPhase5_1Operation(byte beta, int row) {

        return new SymbolBetaDivision(beta, row);
    }

    static ISDOperation newPhase5_2Operation(byte srcMult, int srcRow, int dstRow) {

        return new SymbolAddition(srcMult, srcRow, dstRow);
    }

    static ISDOperation newReorderOperation(int L, int[] c, int[] d) {

        return new SymbolReordering(L, c, d);
    }

    static ISDOperation readOperation(ReadableByteChannel ch) throws EOFException, IOException {

        final byte idByte = ExtraChannels.readByte(ch);
        if (OpID.isInvalidByte(idByte)) throw new IOException("unknown operation ID");

        switch (OpID.fromByte(idByte)) {
            case SYMBOL_ADDITION:
                return SymbolAddition.deserializeFromChannel(ch);

            case SYMBOL_BETA_DIVISION:
                return SymbolBetaDivision.deserializeFromChannel(ch);

            case REDUCE_MATRIX_TO_ROW_ECHELON:
                return ReduceMatrixToRowEchelon.deserializeFromChannel(ch);

            case MATRIX_VECTOR_MULTIPLICATION:
                return MatrixVectorMultiplication.deserializeFromChannel(ch);

            case SYMBOL_REORDERING:
                return SymbolReordering.deserializeFromChannel(ch);

            default:
                // should never happen
                throw new AssertionError("unknown enum type");
        }
    }


    private static enum OpID {

        SYMBOL_ADDITION,
        SYMBOL_BETA_DIVISION,
        REDUCE_MATRIX_TO_ROW_ECHELON,
        MATRIX_VECTOR_MULTIPLICATION,
        SYMBOL_REORDERING;

        private static byte toByte(OpID t) {

            return (byte)t.ordinal();
        }

        private static OpID fromByte(byte b) {

            return values()[UnsignedTypes.getUnsignedByte(b)];
        }

        private static boolean isInvalidByte(byte b) {

            final int unsigned = UnsignedTypes.getUnsignedByte(b);
            return unsigned < 0 || values().length <= unsigned;
        }
    }

    private static final class SymbolAddition implements ISDOperation {

        static SymbolAddition deserializeFromChannel(ReadableByteChannel ch) throws IOException {

            final byte srcMult = ExtraChannels.readByte(ch);
            final int srcRow = ExtraChannels.readInt(ch);
            final int dstRow = ExtraChannels.readInt(ch);
            return new SymbolAddition(srcMult, srcRow, dstRow);
        }


        private final byte srcMult;
        private final int srcRow;
        private final int dstRow;


        SymbolAddition(byte srcMult, int srcRow, int dstRow) {

            this.srcMult = srcMult;
            this.srcRow = srcRow;
            this.dstRow = dstRow;
        }

        @Override
        public byte[][] apply(byte[][] D) {

            OctetOps.vectorVectorAddition(srcMult, D[srcRow], D[dstRow], D[dstRow]);
            return D;
        }

        @Override
        public void serializeToChannel(WritableByteChannel ch) throws IOException {

            ExtraChannels.writeByte(ch, OpID.toByte(OpID.SYMBOL_ADDITION));
            ExtraChannels.writeByte(ch, srcMult);
            ExtraChannels.writeInt(ch, srcRow);
            ExtraChannels.writeInt(ch, dstRow);
        }

        @Override
        public int hashCode() {

            return Objects.hash(srcMult, srcRow, dstRow);
        }

        @Override
        public boolean equals(Object other) {

            return other instanceof SymbolAddition && this.equals((SymbolAddition)other);
        }

        public boolean equals(SymbolAddition other) {

            return (this.srcMult == other.srcMult) &&
                   (this.srcRow == other.srcRow) &&
                   (this.dstRow == other.dstRow);
        }
    }

    private static final class SymbolBetaDivision implements ISDOperation {

        static SymbolBetaDivision deserializeFromChannel(ReadableByteChannel ch) throws IOException {

            final byte beta = ExtraChannels.readByte(ch);
            final int row = ExtraChannels.readInt(ch);
            return new SymbolBetaDivision(beta, row);
        }


        private final byte beta;
        private final int row;


        SymbolBetaDivision(byte beta, int row) {

            this.beta = beta;
            this.row = row;
        }

        @Override
        public byte[][] apply(byte[][] D) {

            OctetOps.valueVectorDivision(beta, D[row], D[row]); // in place division
            return D;
        }

        @Override
        public void serializeToChannel(WritableByteChannel ch) throws IOException {

            ExtraChannels.writeByte(ch, OpID.toByte(OpID.SYMBOL_BETA_DIVISION));
            ExtraChannels.writeByte(ch, beta);
            ExtraChannels.writeInt(ch, row);
        }

        @Override
        public int hashCode() {

            return Objects.hash(beta, row);
        }

        @Override
        public boolean equals(Object other) {

            return other instanceof SymbolBetaDivision && this.equals((SymbolBetaDivision)other);
        }

        public boolean equals(SymbolBetaDivision other) {

            return (this.beta == other.beta) &&
                   (this.row == other.row);
        }
    }

    private static final class ReduceMatrixToRowEchelon implements ISDOperation {

        static ReduceMatrixToRowEchelon deserializeFromChannel(ReadableByteChannel ch) throws IOException {

            final ByteMatrix A = readMatrix(ch);
            final int fromRow = ExtraChannels.readInt(ch);
            final int toRow = ExtraChannels.readInt(ch);
            final int fromCol = ExtraChannels.readInt(ch);
            final int toCol = ExtraChannels.readInt(ch);
            final int[] d = readIntArray(ch);
            return new ReduceMatrixToRowEchelon(A, fromRow, toRow, fromCol, toCol, d);
        }


        private final ByteMatrix A;
        private final int fromRow;
        private final int toRow;
        private final int fromCol;
        private final int toCol;
        private final int[] d;


        ReduceMatrixToRowEchelon(ByteMatrix A, int fromRow, int toRow, int fromCol, int toCol, int[] d) {

            this.A = Objects.requireNonNull(A);
            this.fromRow = fromRow;
            this.toRow = toRow;
            this.fromCol = fromCol;
            this.toCol = toCol;
            this.d = Objects.requireNonNull(d);
        }

        private ByteMatrix AMatrix() {

            return A.copy();
        }

        private int[] dArray() {

            return Arrays.copyOf(d, d.length);
        }

        @Override
        public byte[][] apply(byte[][] D) {

            MatrixUtilities.reduceToRowEchelonForm(AMatrix(), fromRow, toRow, fromCol, toCol, dArray(), D);
            return D;
        }

        @Override
        public void serializeToChannel(WritableByteChannel ch) throws IOException {

            ExtraChannels.writeByte(ch, OpID.toByte(OpID.REDUCE_MATRIX_TO_ROW_ECHELON));
            writeMatrix(ch, A);
            ExtraChannels.writeInt(ch, fromRow);
            ExtraChannels.writeInt(ch, toRow);
            ExtraChannels.writeInt(ch, fromCol);
            ExtraChannels.writeInt(ch, toCol);
            writeIntArray(ch, d);
        }

        @Override
        public int hashCode() {

            return Arrays.deepHashCode(new Object[] {A, fromRow, toRow, fromCol, toCol, d});
        }

        @Override
        public boolean equals(Object other) {

            return other instanceof ReduceMatrixToRowEchelon && this.equals((ReduceMatrixToRowEchelon)other);
        }

        public boolean equals(ReduceMatrixToRowEchelon other) {

            return (this.A.equals(other.A)) &&
                   (this.fromRow == other.fromRow) &&
                   (this.toRow == other.toRow) &&
                   (this.fromCol == other.fromCol) &&
                   (this.toCol == other.toCol) &&
                   (Arrays.equals(this.d, other.d));
        }
    }

    private static final class MatrixVectorMultiplication implements ISDOperation {

        static MatrixVectorMultiplication deserializeFromChannel(ReadableByteChannel ch) throws IOException {

            final ByteMatrix X = readMatrix(ch);
            final int Xrows = ExtraChannels.readInt(ch);
            final int Xcols = ExtraChannels.readInt(ch);
            final int[] d = readIntArray(ch);
            return new MatrixVectorMultiplication(X, Xrows, Xcols, d);
        }


        private final ByteMatrix X;
        private final int Xrows;
        private final int Xcols;
        private final int[] d;


        MatrixVectorMultiplication(ByteMatrix X, int Xrows, int Xcols, int[] d) {

            this.X = Objects.requireNonNull(X);
            this.Xrows = Xrows;
            this.Xcols = Xcols;
            this.d = Objects.requireNonNull(d);
        }

        @Override
        public byte[][] apply(byte[][] D) {

            ByteMatrix DM = new RowIndirected2DByteMatrix(Xrows, Dcols(D), DShallowCopy(D), d);
            for (int row = 0; row < Xrows; row++) {
                D[d[row]] = getInnerArray(X.multiplyRow(row, DM, 0, Xcols, LinearAlgebra.BASIC1D_FACTORY));
            }

            return D;
        }

        private int Dcols(byte[][] D) {

            return (D.length == 0) ? 0 : D[0].length;
        }

        private byte[][] DShallowCopy(byte[][] D) {

            return Arrays.copyOf(D, D.length);
        }

        private byte[] getInnerArray(ByteVector v) {

            return ((BasicByteVector)v).getInternalArray();
        }

        @Override
        public void serializeToChannel(WritableByteChannel ch) throws IOException {

            ExtraChannels.writeByte(ch, OpID.toByte(OpID.MATRIX_VECTOR_MULTIPLICATION));
            writeMatrix(ch, X);
            ExtraChannels.writeInt(ch, Xrows);
            ExtraChannels.writeInt(ch, Xcols);
            writeIntArray(ch, d);
        }

        @Override
        public int hashCode() {

            return Arrays.deepHashCode(new Object[] {X, Xrows, Xcols, d});
        }

        @Override
        public boolean equals(Object other) {

            return other instanceof MatrixVectorMultiplication && this.equals((MatrixVectorMultiplication)other);
        }

        public boolean equals(MatrixVectorMultiplication other) {

            return (this.X.equals(other.X)) &&
                   (this.Xrows == other.Xrows) &&
                   (this.Xcols == other.Xcols) &&
                   (Arrays.equals(this.d, other.d));
        }
    }

    private static final class SymbolReordering implements ISDOperation {

        static SymbolReordering deserializeFromChannel(ReadableByteChannel ch) throws IOException {

            final int L = ExtraChannels.readInt(ch);
            final int[] c = readIntArray(ch);
            final int[] d = readIntArray(ch);
            return new SymbolReordering(L, c, d);
        }


        private final int L;
        private final int[] c;
        private final int[] d;


        SymbolReordering(int L, int[] c, int[] d) {

            this.L = L;
            this.c = Objects.requireNonNull(c);
            this.d = Objects.requireNonNull(d);
        }

        @Override
        public byte[][] apply(byte[][] D) {

            final byte[][] C = new byte[L][];
            for (int i = 0; i < L; i++) {
                C[c[i]] = D[d[i]];
            }

            return C;
        }

        @Override
        public void serializeToChannel(WritableByteChannel ch) throws IOException {

            ExtraChannels.writeByte(ch, OpID.toByte(OpID.SYMBOL_REORDERING));
            ExtraChannels.writeInt(ch, L);
            writeIntArray(ch, c);
            writeIntArray(ch, d);
        }

        @Override
        public int hashCode() {

            return Arrays.deepHashCode(new Object[] {L, c, d});
        }

        @Override
        public boolean equals(Object other) {

            return other instanceof SymbolReordering && this.equals((SymbolReordering)other);
        }

        public boolean equals(SymbolReordering other) {

            return (this.L == other.L) &&
                   (Arrays.equals(this.c, other.c)) &&
                   (Arrays.equals(this.d, other.d));
        }
    }


    private static void writeMatrix(WritableByteChannel ch, ByteMatrix mat) throws IOException {

        mat.serializeToChannel(ch);
    }

    private static void writeIntArray(WritableByteChannel ch, int[] array) throws IOException {

        ExtraChannels.writeInt(ch, array.length);
        ArrayIO.writeInts(ch, array);
    }

    private static ByteMatrix readMatrix(ReadableByteChannel ch) throws IOException {

        try {
            return ByteMatrices.deserializeMatrix(ch);
        }
        catch (DeserializationException e) {
            throw new IOException("deserialization error: " + e.getMessage());
        }
    }

    private static int[] readIntArray(ReadableByteChannel ch) throws IOException {

        int[] array = new int[readIntArraySize(ch)];
        ArrayIO.readInts(ch, array);
        return array;
    }

    private static int readIntArraySize(ReadableByteChannel ch) throws IOException {

        final int dataLen = ExtraChannels.readInt(ch);
        if (dataLen < 0) {
            throw new IOException("unexpected negative data length: " + dataLen);
        }

        return dataLen;
    }

    private ISDOps() {

        // not instantiable
    }
}
