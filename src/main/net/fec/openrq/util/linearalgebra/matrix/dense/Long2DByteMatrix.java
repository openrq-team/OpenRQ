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
package net.fec.openrq.util.linearalgebra.matrix.dense;


import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.array.BytesAsLongs;
import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.source.MatrixSource;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.serialize.Serialization.Type;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.dense.LongByteVector;


public class Long2DByteMatrix extends AbstractBasicByteMatrix {

    public static Long2DByteMatrix copyOf(byte[][] array) {

        final int rows = array.length;
        final LongByteVector[] vecs = new LongByteVector[rows];
        for (int i = 0; i < rows; i++) {
            vecs[i] = LongByteVector.copyOf(array[i]);
        }

        return new Long2DByteMatrix(vecs);
    }


    private final LongByteVector[] self;


    public Long2DByteMatrix() {

        this(0, 0);
    }

    public Long2DByteMatrix(ByteMatrix matrix) {

        this(ByteMatrices.asMatrixSource(matrix));
    }

    public Long2DByteMatrix(MatrixSource source) {

        this(source.rows(), source.columns());

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                self[i].set(j, source.get(i, j));
            }
        }
    }

    public Long2DByteMatrix(int rows, int columns) {

        super(LinearAlgebra.LONG2D_FACTORY, rows, columns);

        this.self = new LongByteVector[rows];
        for (int i = 0; i < rows; i++) {
            this.self[i] = new LongByteVector(columns);
        }
    }
    

    public Long2DByteMatrix(BytesAsLongs[] bytes) {

        super(LinearAlgebra.LONG2D_FACTORY, bytes.length, bytes.length == 0 ? 0 : bytes[0].sizeInBytes());

        final int rows = bytes.length;
        this.self = new LongByteVector[rows];
        for (int i = 0; i < rows; i++) {
            this.self[i] = new LongByteVector(bytes[i]);
        }
    }
    
    public Long2DByteMatrix(int rows, int columns, BytesAsLongs[] bytes) {
        
        super(LinearAlgebra.LONG2D_FACTORY, rows, columns);
        
        this.self = new LongByteVector[rows];
        for (int i = 0; i < rows; i++) {
            this.self[i] = new LongByteVector(bytes[i]);
        }
    }

    private Long2DByteMatrix(LongByteVector[] self) {

        super(LinearAlgebra.LONG2D_FACTORY, self.length, self.length == 0 ? 0 : self[0].length());
        this.self = self;
    }

    @Override
    public byte safeGet(int i, int j) {

        return self[i].get(j);
    }

    @Override
    public void safeSet(int i, int j, byte value) {

        self[i].set(j, value);
    }

    @Override
    public void swapRows(int i, int j) {

        Indexables.checkIndexBounds(i, rows());
        Indexables.checkIndexBounds(j, rows());

        if (i != j) {
            ArrayUtils.swapObjects(self, i, j);
        }
    }

    @Override
    public void swapColumns(int i, int j) {

        Indexables.checkIndexBounds(i, columns());
        Indexables.checkIndexBounds(j, columns());

        if (i != j) {
            for (int ii = 0; ii < rows(); ii++) {
                self[ii].swap(i, j);
            }
        }
    }

    @Override
    public ByteVector getRow(int i) {

        Indexables.checkIndexBounds(i, rows());

        return self[i].copy();
    }

    @Override
    public ByteMatrix copy() {

        final LongByteVector[] vecs = new LongByteVector[rows()];
        for (int i = 0; i < rows(); i++) {
            vecs[i] = self[i].copy();
        }

        return new Long2DByteMatrix(vecs);
    }

    @Override
    public ByteMatrix resize(int rows, int columns) {

        ensureDimensionsAreCorrect(rows, columns);

        if (this.rows() == rows && this.columns() == columns) {
            return copy();
        }

        LongByteVector[] $self = new LongByteVector[rows];

        for (int i = 0; i < Math.min(this.rows(), rows); i++) {
            $self[i] = self[i].resize(columns);
        }

        return new Long2DByteMatrix($self);
    }

    @Override
    public byte[][] toArray() {

        byte result[][] = new byte[rows()][];

        for (int i = 0; i < rows(); i++) {
            result[i] = self[i].toArray();
        }

        return result;
    }

    public BytesAsLongs[] getInternalBytes() {

        BytesAsLongs[] bals = new BytesAsLongs[rows()];

        for (int i = 0; i < rows(); i++) {
            bals[i] = self[i].getInternalBytes();
        }

        return bals;
    }

    @Override
    protected Type getSerializationType() {

        return Serialization.Type.DENSE_2D_MATRIX;
    }
}
