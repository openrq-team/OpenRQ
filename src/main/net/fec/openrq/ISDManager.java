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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                            final String decClassName = "net.fec.openrq.openrq." + ISD_PREFIX + Kprime;
                            final IntermediateSymbolsDecoder isd = newISDInstance(decClassName, System.err);
                            if (isd != null) {
                                isdsList.add(isd);
                            }
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

    private static IntermediateSymbolsDecoder newISDInstance(String className, PrintStream errStream) {

        /*
         * Find the class
         */
        final Class<?> decClass;
        try {
            decClass = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            errStream.printf("Could not find class %s%n",
                className);
            return null;
        }

        /*
         * Check if it implements IntermediateSymbolsDecoder
         */
        if (!IntermediateSymbolsDecoder.class.isAssignableFrom(decClass)) {
            errStream.printf("Class %s must be a subclass of net.fec.openrq.openrq.util.rq.IntermediateSymbolsDecoder%n",
                className);
            return null;
        }

        /*
         * Retrieve the default constructor
         */
        final Constructor<?> decConstructor;
        try {
            decConstructor = decClass.getConstructor();
        }
        catch (NoSuchMethodException e) {
            errStream.printf("Class %s does not contain an available default constructor%n",
                className);
            return null;
        }

        /*
         * Call the constructor and return a new instance
         */
        try {
            return (IntermediateSymbolsDecoder)decConstructor.newInstance();
        }
        catch (IllegalAccessException e) {
            errStream.println(e.getMessage());
            return null;
        }
        catch (InstantiationException e) {
            errStream.printf("Class %s must not be an abstract class%n",
                className);
            return null;
        }
        catch (InvocationTargetException e) {
            errStream.printf("Exception while calling default constructor of class %s:%n",
                className);
            e.getCause().printStackTrace(errStream);
            return null;
        }
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
}
