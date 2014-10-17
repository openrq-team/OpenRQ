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
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.NoSuchFileException;


/**
 * 
 */
public final class Resources {

    public static ReadableByteChannel openResourceChannel(Class<?> clazz, String resourceName) {

        try {
            return Channels.newChannel(getResourceAsStream(clazz, resourceName));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static InputStream getResourceAsStream(Class<?> clazz, String resourceName) throws IOException {

        final InputStream is = clazz.getResourceAsStream(resourceName);
        if (is == null) throw new NoSuchFileException(resourceName, null, "resource not found");
        return is;
    }

    private Resources() {

        // not instantiable
    }
}
