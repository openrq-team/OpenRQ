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


import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

import net.fec.openrq.util.io.Resources;
import net.fec.openrq.util.io.UncheckedIOException;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.dense.RowIndirected2DByteMatrix;
import net.fec.openrq.util.linearalgebra.vector.dense.BasicByteVector;
import net.fec.openrq.util.math.OctetOps;
import net.fec.openrq.util.rq.IntermediateSymbolsDecoder;


/**
 * Abstract class with minimal method names to help reduce generated code size.
 */
abstract class AbstractISD implements IntermediateSymbolsDecoder {

    protected static final void a(int srcMult, byte[] src, byte[] dest) {

        MatrixUtilities.addSymbolsWithMultiplierInPlace((byte)srcMult, src, dest);
    }

    protected static final void b(int beta, byte[] src, byte[] dst) {

        OctetOps.betaDivision((byte)beta, src, dst);
    }

    protected static final int[] c(int[] array) {

        return Arrays.copyOf(array, array.length);
    }

    protected static final byte[][] c(byte[][] matrix) {

        return Arrays.copyOf(matrix, matrix.length);
    }

    protected static final ByteMatrix sparse(int rows, int cols) {

        return LinearAlgebra.SPARSE_FACTORY.createMatrix(rows, cols);
    }

    protected static final ByteMatrix rowInd(int rows, int columns, byte[][] matrix, int[] rowIndirection) {

        return new RowIndirected2DByteMatrix(rows, columns, matrix, rowIndirection);
    }

    protected static final int cols(byte[][] matrix) {

        return (matrix.length == 0) ? 0 : matrix[0].length;
    }

    protected static final byte[] m(ByteMatrix leftMat, int row, ByteMatrix rightMat, int fromCol, int toCol) {

        return ((BasicByteVector)leftMat.multiplyRow(
            row, rightMat, fromCol, toCol, LinearAlgebra.BASIC1D_FACTORY)).getInternalArray();
    }

    protected static final void s(ByteMatrix mat, int i, int j, int value) {

        mat.set(i, j, (byte)value);
    }


    protected final ByteMatrix A;
    protected final ByteMatrix X;
    protected final int[] dForPhase2;
    protected final int[] dForPhase3;


    protected AbstractISD(int Kprime) {

        // try-with-resources (channel is automatically closed at the end)
        try (ReadableByteChannel ch = Resources.openResourceChannel(getClass(), resourceName(Kprime))) {
            this.A = ISDUtils.readMatrix(ch);
            this.X = ISDUtils.readMatrix(ch);
            this.dForPhase2 = ISDUtils.readIntArray(ch);
            this.dForPhase3 = ISDUtils.readIntArray(ch);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String resourceName(int Kprime) {

        return "ISD_" + Kprime + ".dat";
    }
}
