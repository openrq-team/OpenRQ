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


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fec.openrq.util.io.Resources;
import net.fec.openrq.util.io.UncheckedIOException;
import net.fec.openrq.util.rq.IntermediateSymbolsDecoder;
import net.fec.openrq.util.rq.SystematicIndices;


/**
 * 
 */
final class ISDManager {

    private static final int MAX_K_PRIME_CHARS = "56403".length();
    private static final String K_PRIME_FORMAT = "[0-9]+";
    private static final String ISD_PREFIX = "ISD_";

    private static final ISDManager INSTANCE;
    static {
        final List<IntermediateSymbolsDecoder> isdsList = new ArrayList<>();

        final InputStream in = ISDManager.class.getResourceAsStream("ISDs");
        if (in == null) {
            System.err.println("Could not find \"Intermediate Symbols Decoders\" file");
        }
        else {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    if (isValidKPrimeLine(line)) {
                        final int Kprime = Integer.parseInt(line); // should always succeed
                        if (SystematicIndices.containsKPrime(Kprime)) {
                            isdsList.add(new ISD(Kprime));
                        }
                        else {
                            System.err.printf(
                                "Line %d of \"Intermediate Symbols Decoders\" file was skipped: value %d is an unknown K'%n",
                                lineNumber, Kprime);
                        }
                    }

                    lineNumber++;
                }
            }
            catch (UncheckedIOException e) {
                System.err.println("Error while reading \"Intermediate Symbols Decoders\" file:");
                e.getCause().printStackTrace(System.err);
            }
            catch (IOException e) {
                System.err.println("Error while reading \"Intermediate Symbols Decoders\" file:");
                e.printStackTrace(System.err);
            }
        }

        INSTANCE = new ISDManager(isdsList);
    }


    private static boolean isValidKPrimeLine(String line) {

        if (line.isEmpty() || line.length() > MAX_K_PRIME_CHARS || line.startsWith("#")) {
            return false;
        }

        return line.matches(K_PRIME_FORMAT);
    }

    /**
     * Returns an optimized decoder for the given value of K' (see RFC 6330), or {@code null} if there is none
     * registered for the given value.
     * 
     * @param Kprime
     *            The number of source (and padding) symbols in an extended source block
     * @return an optimized decoder for the given value of K', or {@code null} if there is none registered for the given
     *         value
     */
    static IntermediateSymbolsDecoder get(int Kprime) {

        return INSTANCE.getDecoder(Kprime);
    }


    private final Map<Integer, IntermediateSymbolsDecoder> map;


    private ISDManager(Iterable<IntermediateSymbolsDecoder> decoders) {

        this.map = new HashMap<>();
        for (IntermediateSymbolsDecoder dec : decoders) {
            map.put(dec.supportedKPrime(), dec);
        }
    }

    private IntermediateSymbolsDecoder getDecoder(int Kprime) {

        return map.get(Kprime);
    }


    private static final class ISD implements IntermediateSymbolsDecoder {

        private final int Kprime;
        private final List<ISDOperation> ops;


        ISD(int Kprime) throws IOException {

            this.Kprime = Kprime;
            this.ops = new ArrayList<>();

            // try-with-resources (channel is automatically closed at the end)
            try (ReadableByteChannel ch = Resources.openResourceChannel(getClass(), resourceName(Kprime))) {
                while (true) {
                    ops.add(ISDOps.readOperation(ch));
                }
            }
            catch (EOFException e) {
                // do nothing, we expect this exception to occur
            }
        }

        private static String resourceName(int Kprime) {

            return ISD_PREFIX + Kprime + ".dat";
        }

        @Override
        public final int supportedKPrime() {

            return Kprime;
        }

        @Override
        public final byte[][] decode(byte[][] D) {

            byte[][] symbols = D;
            for (ISDOperation op : ops) {
                symbols = op.apply(symbols);
            }
            return symbols;
        }
    }
}
