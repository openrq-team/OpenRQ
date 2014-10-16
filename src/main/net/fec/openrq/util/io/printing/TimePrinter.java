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
package net.fec.openrq.util.io.printing;


import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.util.io.printing.appendable.NoisyPrintableAppendable;
import net.fec.openrq.util.io.printing.appendable.PrintableAppendable;


/**
 * 
 */
public final class TimePrinter {

    private static final class MutableTimeInterval {

        private long beginNanos;
        private long endNanos;


        MutableTimeInterval(long beginNanos) {

            begin(beginNanos);
        }

        void begin(long beginNanos) {

            this.beginNanos = beginNanos;
            this.endNanos = beginNanos;
        }

        void end(long endNanos) {

            this.endNanos = endNanos;
        }

        long getEllapsedNanos() {

            return endNanos - beginNanos;
        }
    }


    private static final ThreadLocal<MutableTimeInterval> TL_INTERVAL = new ThreadLocal<MutableTimeInterval>() {

        @Override
        protected MutableTimeInterval initialValue() {

            return new MutableTimeInterval(System.nanoTime());
        }
    };


    public static void beginTimer() {

        MutableTimeInterval mti = TL_INTERVAL.get();
        mti.begin(System.nanoTime());
    }

    public static void markTimestamp() {

        long nanos = System.nanoTime();
        TL_INTERVAL.get().end(nanos);
    }

    public static double getEllapsedTime(TimeUnit unit) {

        return ((double)TL_INTERVAL.get().getEllapsedNanos()) / unit.toNanos(1L);
    }

    // ======================== PRINT ======================== //

    public static void printEllapsedTime(PrintStream ps, TimeUnit unit) {

        printEllapsedTime(pa(ps), unit);
    }

    public static void printEllapsedTime(PrintWriter pw, TimeUnit unit) {

        printEllapsedTime(pa(pw), unit);
    }

    public static void printEllapsedTime(PrintableAppendable pa, TimeUnit unit) {

        printEllapsedTime(pa, "", unit);
    }

    public static void printEllapsedTime(NoisyPrintableAppendable npa, TimeUnit unit) throws IOException {

        printEllapsedTime(npa, "", unit);
    }

    // ======================== PRINT WITH PREFIX ======================== //

    public static void printEllapsedTime(PrintStream ps, String prefix, TimeUnit unit) {

        printEllapsedTime(pa(ps), prefix, unit);
    }

    public static void printEllapsedTime(PrintWriter pw, String prefix, TimeUnit unit) {

        printEllapsedTime(pa(pw), prefix, unit);
    }

    public static void printEllapsedTime(PrintableAppendable pa, String prefix, TimeUnit unit) {

        _printEllapsedTime(pa, prefix, unit);
    }

    public static void printEllapsedTime(NoisyPrintableAppendable npa, String prefix, TimeUnit unit) throws IOException {

        _printEllapsedTime(npa, prefix, unit);
    }

    // ======================== PRINT WITH NEW LINE ======================== //

    public static void printlnEllapsedTime(PrintStream ps, TimeUnit unit) {

        printlnEllapsedTime(pa(ps), unit);
    }

    public static void printlnEllapsedTime(PrintWriter pw, TimeUnit unit) {

        printlnEllapsedTime(pa(pw), unit);
    }

    public static void printlnEllapsedTime(PrintableAppendable pa, TimeUnit unit) {

        printlnEllapsedTime(pa, "", unit);
    }

    public static void printlnEllapsedTime(NoisyPrintableAppendable npa, TimeUnit unit) throws IOException {

        printlnEllapsedTime(npa, "", unit);
    }

    // ======================== PRINT WITH PREFIX AND NEW LINE ======================== //

    public static void printlnEllapsedTime(PrintStream ps, String prefix, TimeUnit unit) {

        printlnEllapsedTime(pa(ps), prefix, unit);
    }

    public static void printlnEllapsedTime(PrintWriter pw, String prefix, TimeUnit unit) {

        printlnEllapsedTime(pa(pw), prefix, unit);
    }

    public static void printlnEllapsedTime(PrintableAppendable pa, String prefix, TimeUnit unit) {

        _printEllapsedTime(pa, prefix, unit);
        pa.println();
    }

    public static void printlnEllapsedTime(NoisyPrintableAppendable npa, String prefix, TimeUnit unit)
        throws IOException {

        _printEllapsedTime(npa, prefix, unit);
        npa.println();
    }

    // ======================== DOES THE ACTUAL PRINTING ======================== //

    private static void _printEllapsedTime(PrintableAppendable pa, String prefix, TimeUnit unit) {

        pa.printf("%s%.03f %s", prefix, getEllapsedTime(unit), unit.name().toLowerCase());
    }

    private static void _printEllapsedTime(NoisyPrintableAppendable npa, String prefix, TimeUnit unit)
        throws IOException {

        npa.printf("%s%.03f %s", prefix, getEllapsedTime(unit), unit.name().toLowerCase());
    }

    // ======================== PRINTABLE APPENDABLE WRAPPER ======================== //

    private static PrintableAppendable pa(PrintStream ps) {

        return PrintableAppendable.of(ps, false);
    }

    private static PrintableAppendable pa(PrintWriter pw) {

        return PrintableAppendable.of(pw, false);
    }

    private TimePrinter() {

        // not instantiable
    }
}
