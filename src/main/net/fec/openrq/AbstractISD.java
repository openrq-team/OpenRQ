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


import java.util.Arrays;

import net.fec.openrq.util.arithmetic.OctetOps;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.vector.dense.BasicByteVector;
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

    protected static final ByteMatrix sparse(int rows, int cols) {

        return LinearAlgebra.SPARSE_FACTORY.createMatrix(rows, cols);
    }

    protected static final ByteMatrix dense(byte[][] matrix) {

        return LinearAlgebra.BASIC2D_FACTORY.createMatrix(matrix);
    }

    protected static final byte[] m(ByteMatrix leftMat, int row, ByteMatrix rightMat, int fromCol, int toCol) {

        return ((BasicByteVector)leftMat.multiplyRow(
            row, rightMat, fromCol, toCol, LinearAlgebra.BASIC1D_FACTORY)).getInternalArray();
    }

    protected static final void s(ByteMatrix mat, int i, int j, int value) {

        mat.set(i, j, (byte)value);
    }
}
