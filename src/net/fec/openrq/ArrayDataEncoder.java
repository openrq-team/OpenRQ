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


import java.nio.ByteBuffer;

import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ArrayDataEncoder implements DataEncoder {

    static ArrayDataEncoder newEncoder(byte[] array, int offset, int length, FECParameters fecParams) {

        if (!fecParams.isValid()) throw new IllegalArgumentException("invalid parameters");
        final ByteBuffer data = ByteBuffer.wrap(array, offset, length);
        return new ArrayDataEncoder(data, fecParams);
    }


    private final ByteBuffer data;
    private final FECParameters fecParams;


    private ArrayDataEncoder(ByteBuffer data, FECParameters fecParams) {

        this.data = data;
        this.fecParams = fecParams;
    }

    /**
     * Returns an array of bytes containing the encodable data.
     * 
     * @return an array of bytes containing the encodable data
     */
    public byte[] dataArray() {

        return data.array();
    }

    /**
     * Returns the index in the data array of the first encodable byte.
     * 
     * @return the index in the data array of the first encodable byte
     */
    public int dataOffset() {

        return data.arrayOffset();
    }

    @Override
    public FECParameters fecParameters() {

        return fecParams;
    }

    @Override
    public long dataLength() {

        return fecParams.dataLength();
    }

    @Override
    public int symbolSize() {

        return fecParams.symbolSize();
    }

    @Override
    public int numberOfSourceBlocks() {

        return fecParams.numberOfSourceBlocks();
    }

    @Override
    public SourceBlockEncoder encoderForSourceBlock(int sourceBlockNum) {

        // TODO Auto-generated method stub
        return null;
    }
}
