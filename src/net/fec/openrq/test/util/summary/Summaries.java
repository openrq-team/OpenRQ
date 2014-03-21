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
package net.fec.openrq.test.util.summary;


import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import net.fec.openrq.test.util.StringConverter;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Summaries {

    public static <T extends StringConverter<LongSummaryStatistics>>
        String printToString(Map<T, LongSummaryStatistics> map)
    {

        final StringBuilder builder = new StringBuilder();
        for (Entry<T, LongSummaryStatistics> entry : map.entrySet()) {
            builder.append(entry.getKey().toString(entry.getValue()));
        }

        return builder.toString();
    }

    public static <T extends StringConverter<LongSummaryStatistics>>
        void printToStream(Map<T, LongSummaryStatistics> map, PrintStream stream)
    {

        for (Entry<T, LongSummaryStatistics> entry : map.entrySet()) {
            stream.print(entry.getKey().toString(entry.getValue()));
        }
    }

    public static <T extends StringConverter<LongSummaryStatistics>>
        void printlnToStream(Map<T, LongSummaryStatistics> map, PrintStream stream)
    {

        for (Entry<T, LongSummaryStatistics> entry : map.entrySet()) {
            stream.println(entry.getKey().toString(entry.getValue()));
        }
    }

    public static <T extends StringConverter<LongSummaryStatistics>>
        void printToWriter(Map<T, LongSummaryStatistics> map, PrintWriter writer)
    {

        for (Entry<T, LongSummaryStatistics> entry : map.entrySet()) {
            writer.print(entry.getKey().toString(entry.getValue()));
        }
    }

    public static <T extends StringConverter<LongSummaryStatistics>>
        void printlnToWriter(Map<T, LongSummaryStatistics> map, PrintWriter writer)
    {

        for (Entry<T, LongSummaryStatistics> entry : map.entrySet()) {
            writer.println(entry.getKey().toString(entry.getValue()));
        }
    }

    private Summaries() {

        // not instantiable
    }
}
