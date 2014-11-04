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


import java.nio.ByteBuffer;
import java.util.Objects;

import net.fec.openrq.util.io.BufferOperation;
import net.fec.openrq.util.io.ByteBuffers;
import net.fec.openrq.util.io.ByteBuffers.BufferType;


/**
 * Container of repair symbol data.
 */
final class RepairSymbol {

    /**
     * Returns a new repair symbol containing the provided data (no data copies are performed).
     * 
     * @param dataBuf
     *            A buffer containing data (the new repair symbol will contain a reference to this buffer)
     * @return a new repair symbol containing the provided data
     */
    static RepairSymbol wrapData(ByteBuffer dataBuf) {

        return new RepairSymbol(dataBuf);
    }

    /**
     * Returns a new repair symbol containing a copy of the provided data.
     * 
     * @param dataBuf
     *            A buffer containing symbol data
     * @return a new repair symbol containing a copy of the provided data
     */
    static RepairSymbol copyData(ByteBuffer dataBuf) {

        return new RepairSymbol(ByteBuffers.getCopy(dataBuf));
    }


    private final ByteBuffer dataBuf;


    private RepairSymbol(ByteBuffer dataBuf) {

        this.dataBuf = Objects.requireNonNull(dataBuf);
    }

    /**
     * Returns the size of the data from this symbol.
     * 
     * @return the size of the data from this symbol
     */
    int dataSize() {

        return dataBuf.remaining();
    }

    /**
     * Returns a new read-only buffer containing the data from this symbol (no data copies are performed).
     * 
     * @return a new read-only buffer containing the data from this symbol
     */
    ByteBuffer readOnlyData() {

        return dataBuf.asReadOnlyBuffer();
    }

    /**
     * Returns a new buffer with a copy of the data from this symbol.
     * 
     * @param type
     *            The type of buffer to be returned
     * @return a new buffer with a copy of the data from this symbol
     */
    ByteBuffer copyOfData(BufferType type) {

        ByteBuffer copy = ByteBuffers.allocate(dataSize(), type);
        ByteBuffers.copy(
            readOnlyData(), BufferOperation.ADVANCE_POSITION,
            copy, BufferOperation.FLIP_ABSOLUTELY,
            dataSize());
        return copy;
    }
}
