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


import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Objects;


/**
 * Wraps an {@link IOException} with an unchecked exception.
 * <p>
 * <b>NOTE:</b> This class was imported from JDK 8.
 */
public class UncheckedIOException extends RuntimeException {

    private static final long serialVersionUID = -5271489036111947161L;


    /**
     * Constructs an instance of this class.
     * 
     * @param message
     *            the detail message, can be null
     * @param cause
     *            the {@code IOException}
     * @throws NullPointerException
     *             if the cause is {@code null}
     */
    public UncheckedIOException(String message, IOException cause) {

        super(message, Objects.requireNonNull(cause));
    }

    /**
     * Constructs an instance of this class.
     * 
     * @param cause
     *            the {@code IOException}
     * @throws NullPointerException
     *             if the cause is {@code null}
     */
    public UncheckedIOException(IOException cause) {

        super(Objects.requireNonNull(cause));
    }

    /**
     * Returns the cause of this exception.
     * 
     * @return the {@code IOException} which is the cause of this exception.
     */
    @Override
    public synchronized IOException getCause() {

        return (IOException)super.getCause();
    }

    /**
     * Called to read the object from a stream.
     * 
     * @param s
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidObjectException
     *             if the object is invalid or has a cause that is not
     *             an {@code IOException}
     */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {

        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof IOException)) {
            throw new InvalidObjectException("Cause must be an IOException");
        }
    }
}
