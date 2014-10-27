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
     * Does nothing to the buffer.
     * <p>
     * <ul>
     * <li>The buffer position will remain updated after another operation.
     * <li>The buffer limit will remain the same as before another operation.
     * </ul>
     */
    DO_NOTHING {

        @Override
        public void apply(@SuppressWarnings("unused") Buffer buf, @SuppressWarnings("unused") int originalBufPos) {

            // nothing, really
        }
    },

    /**
     * Restore the original buffer position.
     * <p>
     * <ul>
     * <li>The buffer position will remain the same as before another operation.
     * <li>The buffer limit will remain the same as before another operation.
     * </ul>
     */
    RESTORE_POSITION {

        @Override
        public void apply(Buffer buf, int originalBufPos) {

            buf.position(originalBufPos);
        }
    },

    /**
     * Flips the buffer relatively.
     * <p>
     * <ul>
     * <li>The buffer position will remain the same as before another operation.
     * <li>The buffer limit will be equal to the updated position after another operation.
     * </ul>
     */
    FLIP_RELATIVELY {

        @Override
        public void apply(Buffer buf, int originalBufPos) {

            buf.limit(buf.position());
            buf.position(originalBufPos);
        }
    },

    /**
     * Flips the buffer absolutely (the same behavior as calling the method {@link Buffer#flip()}).
     * <p>
     * <ul>
     * <li>The buffer position will be equal to 0.
     * <li>The buffer limit will be equal to the updated position after another operation.
     * </ul>
     */
    FLIP_ABSOLUTELY {

        @Override
        public void apply(Buffer buf, @SuppressWarnings("unused") int originalBufPos) {

            buf.flip();
        }
    };

    /**
     * Applies this operation to the provided buffer.
     * 
     * @param buf
     *            The buffer to be affected after another operation
     * @param originalBufPos
     *            The position of the buffer before another operation
     */
    public abstract void apply(Buffer buf, int originalBufPos);
}
