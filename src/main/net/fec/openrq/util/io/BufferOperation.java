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
package net.fec.openrq.util.io;


import java.nio.Buffer;


/**
 * An operation to perform on a {@link Buffer} after a previous operation on the buffer causes its position to change.
 */
public enum BufferOperation {

    /**
     * Advance the position of the buffer. It is specific to each operation how much the position is advanced.
     * <p>
     * <ul>
     * <li>The buffer position will be advanced
     * <li>The buffer limit will not be changed
     * </ul>
     */
    ADVANCE_POSITION {

        @Override
        public void apply(
            Buffer buf,
            @SuppressWarnings("unused") int beforeBufPos,
            int afterBufPos)
        {

            buf.position(afterBufPos);
        }
    },

    /**
     * Restore the buffer position to its value before a specific operation took place.
     * <p>
     * <ul>
     * <li>The buffer position will be restored
     * <li>The buffer limit will not be changed
     * </ul>
     */
    RESTORE_POSITION {

        @Override
        public void apply(
            Buffer buf,
            int beforeBufPos,
            @SuppressWarnings("unused") int afterBufPos)
        {

            buf.position(beforeBufPos);
        }
    },

    /**
     * Flips the buffer relatively. The position will be restored to its value before a specific operation took place.
     * The limit will be set to the position value after the specific operation took place.
     * <p>
     * <ul>
     * <li>The buffer position will be restored
     * <li>The buffer limit will be set to the current position
     * </ul>
     */
    FLIP_RELATIVELY {

        @Override
        public void apply(
            Buffer buf,
            int beforeBufPos,
            int afterBufPos)
        {

            buf.position(beforeBufPos);
            buf.limit(afterBufPos);
        }
    },

    /**
     * Flips the buffer absolutely. The position will be reset. The limit will be set to the position value after a
     * specific operation took place.
     * <p>
     * <ul>
     * <li>The buffer position will be set to 0
     * <li>The buffer limit will be set to the current position
     * </ul>
     */
    FLIP_ABSOLUTELY {

        @Override
        public void apply(
            Buffer buf,
            @SuppressWarnings("unused") int beforeBufPos,
            int afterBufPos) {

            buf.position(0);
            buf.limit(afterBufPos);
        }
    };

    /**
     * Applies this operation to the provided buffer.
     * 
     * @param buf
     *            The buffer to be affected by this operation
     * @param beforeBufPos
     *            The position of the buffer before a specific operation took place
     * @param afterBufPos
     *            The position of the buffer after a specific operation took place
     */
    public abstract void apply(Buffer buf, int beforeBufPos, int afterBufPos);
}
