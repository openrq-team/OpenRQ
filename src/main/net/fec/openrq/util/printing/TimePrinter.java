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
package net.fec.openrq.util.printing;


import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.util.printing.appendable.PrintableAppendable;
import net.fec.openrq.util.printing.appendable.QuietPrintableAppendable;


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

        TL_INTERVAL.get().begin(System.nanoTime());
    }

    public static void markTimestamp() {

        TL_INTERVAL.get().end(System.nanoTime());
    }

    public static double getEllapsedTime(TimeUnit unit) {

        return ((double)TL_INTERVAL.get().getEllapsedNanos()) / unit.toNanos(1L);
    }

    // ======================== PRINT ======================== //

    public static void printEllapsedTime(PrintStream ps, TimeUnit unit) {

        printEllapsedTime(quiet(ps), unit);
    }

    public static void printEllapsedTime(PrintWriter pw, TimeUnit unit) {

        printEllapsedTime(quiet(pw), unit);
    }

    public static void printEllapsedTime(PrintableAppendable pa, TimeUnit unit) throws IOException {

        printEllapsedTime(pa, "", unit);
    }

    public static void printEllapsedTime(QuietPrintableAppendable pa, TimeUnit unit) {

        printEllapsedTime(pa, "", unit);
    }

    // ======================== PRINT WITH PREFIX ======================== //

    public static void printEllapsedTime(PrintStream ps, String prefix, TimeUnit unit) {

        printEllapsedTime(quiet(ps), prefix, unit);
    }

    public static void printEllapsedTime(PrintWriter pw, String prefix, TimeUnit unit) {

        printEllapsedTime(quiet(pw), prefix, unit);
    }

    public static void printEllapsedTime(PrintableAppendable pa, String prefix, TimeUnit unit) throws IOException {

        _printEllapsedTime(pa, prefix, unit);
    }

    public static void printEllapsedTime(QuietPrintableAppendable pa, String prefix, TimeUnit unit) {

        _printEllapsedTime(pa, prefix, unit);
    }

    // ======================== PRINT WITH NEW LINE ======================== //

    public static void printlnEllapsedTime(PrintStream ps, TimeUnit unit) {

        printlnEllapsedTime(quiet(ps), unit);
    }

    public static void printlnEllapsedTime(PrintWriter pw, TimeUnit unit) {

        printlnEllapsedTime(quiet(pw), unit);
    }

    public static void printlnEllapsedTime(PrintableAppendable pa, TimeUnit unit) throws IOException {

        printlnEllapsedTime(pa, "", unit);
    }

    public static void printlnEllapsedTime(QuietPrintableAppendable pa, TimeUnit unit) {

        printlnEllapsedTime(pa, "", unit);
    }

    // ======================== PRINT WITH PREFIX AND NEW LINE ======================== //

    public static void printlnEllapsedTime(PrintStream ps, String prefix, TimeUnit unit) {

        printlnEllapsedTime(quiet(ps), prefix, unit);
    }

    public static void printlnEllapsedTime(PrintWriter pw, String prefix, TimeUnit unit) {

        printlnEllapsedTime(quiet(pw), prefix, unit);
    }

    public static void printlnEllapsedTime(PrintableAppendable pa, String prefix, TimeUnit unit) throws IOException {

        _printEllapsedTime(pa, prefix, unit);
        pa.println();
    }

    public static void printlnEllapsedTime(QuietPrintableAppendable qpa, String prefix, TimeUnit unit) {

        _printEllapsedTime(qpa, prefix, unit);
        qpa.println();
    }

    // ======================== DOES THE ACTUAL PRINTING ======================== //

    private static void _printEllapsedTime(PrintableAppendable pa, String prefix, TimeUnit unit) throws IOException {

        pa.printf("%s%.03f %s", prefix, getEllapsedTime(unit), unit.name().toLowerCase());
    }

    private static void _printEllapsedTime(QuietPrintableAppendable qpa, String prefix, TimeUnit unit) {

        qpa.printf("%s%.03f %s", prefix, getEllapsedTime(unit), unit.name().toLowerCase());
    }

    // ======================== QUIET WRAPPER ======================== //

    private static QuietPrintableAppendable quiet(PrintStream ps) {

        return QuietPrintableAppendable.of(ps, false);
    }

    private static QuietPrintableAppendable quiet(PrintWriter pw) {

        return QuietPrintableAppendable.of(pw, false);
    }

    private TimePrinter() {

        // not instantiable
    }
}
