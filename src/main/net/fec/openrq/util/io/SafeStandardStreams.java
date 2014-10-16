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
import java.io.PrintStream;


/**
 * This class wraps standard streams (stdin/stdout/stderr) inside uncloseable InputStream/PrintStream objects in order
 * to avoid (typically unwanted) accidental closing of these streams.
 */
public final class SafeStandardStreams {

    public static InputStream safeSTDIN() {

        return new UncloseableInputStream(System.in);
    }

    public static PrintStream safeSTDOUT() {

        return new UncloseablePrintStream(System.out);
    }

    public static PrintStream safeSTDERR() {

        return new UncloseablePrintStream(System.err);
    }


    private static final class UncloseableInputStream extends InputStream {

        private final InputStream is;


        UncloseableInputStream(InputStream is) {

            this.is = is;
        }

        @Override
        public int read() throws IOException {

            return is.read();
        }

        @Override
        public void close() {

            // do nothing
        }

    }

    private static final class UncloseablePrintStream extends PrintStream {

        UncloseablePrintStream(PrintStream ps) {

            super(ps);
        }

        @Override
        public final void close() {

            // do nothing
        }
    }


    private SafeStandardStreams() {

        // not instantiable
    }
}
