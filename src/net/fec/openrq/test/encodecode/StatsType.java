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
package net.fec.openrq.test.encodecode;


import java.util.concurrent.TimeUnit;

import net.fec.openrq.test.util.StringConverter;
import net.fec.openrq.test.util.summary.LongSummaryStatistics;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public enum StatsType implements StringConverter<LongSummaryStatistics> {

    ENCODER_INIT_TIME {

        @Override
        public String toString(LongSummaryStatistics stats) {

            return String.format("Encoder initialization time (in %s):{ avg = %03.3f, min = %03.3f, max = %03.3f }",
                STATS_UNIT.name().toLowerCase(), t(stats.getAverage()), t(stats.getMin()), t(stats.getMax()));
        }
    },
    SYMBOL_ENCODING_TIME {

        @Override
        public String toString(LongSummaryStatistics stats) {

            return String.format("Symbol encoding time (in %s):{ avg = %03.3f, min = %03.3f, max = %03.3f }",
                STATS_UNIT.name().toLowerCase(), t(stats.getAverage()), t(stats.getMin()), t(stats.getMax()));
        }
    },
    DECODER_INIT_TIME {

        @Override
        public String toString(LongSummaryStatistics stats) {

            return String.format("Decoder initialization time (in %s):{ avg = %03.3f, min = %03.3f, max = %03.3f }",
                STATS_UNIT.name().toLowerCase(), t(stats.getAverage()), t(stats.getMin()), t(stats.getMax()));
        }
    },
    SYMBOL_INPUT_TIME {

        @Override
        public String toString(LongSummaryStatistics stats) {

            return String.format("Symbol input time (in %s):{ avg = %03.3f, min = %03.3f, max = %03.3f }",
                STATS_UNIT.name().toLowerCase(), t(stats.getAverage()), t(stats.getMin()), t(stats.getMax()));
        }
    },
    DECODING_TIME {

        @Override
        public String toString(LongSummaryStatistics stats) {

            return String.format("Decoding time (in %s):{ avg = %03.3f, min = %03.3f, max = %03.3f }",
                STATS_UNIT.name().toLowerCase(), t(stats.getAverage()), t(stats.getMin()), t(stats.getMax()));
        }
    },
    NUM_DECODING_FAILURES {

        @Override
        public String toString(LongSummaryStatistics stats) {

            final long numFailures = stats.getCount();
            if (stats.hasNext()) {
                final long totalDecodings = stats.getNext().getCount();
                return String.format("Number of decoding failures:{ %d (%03.3f %% of %d total decodings) }",
                    numFailures, ((double)numFailures / totalDecodings), totalDecodings);
            }
            else {
                return String.format("Number of decoding failures:{ %d }", numFailures);
            }
        }
    },
    DECODING_FAILURE_TIME {

        @Override
        public String toString(LongSummaryStatistics stats) {

            return String.format("Decoding failure time (in %s):{min=%03.3f, average=%03.3f, max=%03.3f}",
                STATS_UNIT.name().toLowerCase(), t(stats.getMin()), t(stats.getAverage()), t(stats.getMax()));
        }
    };

    private static final TimeUnit STATS_UNIT = TimeUnit.MICROSECONDS;


    private static double t(double value) {

        return value / STATS_UNIT.toNanos(1L);
    }
}
