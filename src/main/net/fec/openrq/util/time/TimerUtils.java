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
package net.fec.openrq.util.time;


import java.util.concurrent.TimeUnit;


/**
 * 
 */
public final class TimerUtils {

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

    public static long getEllapsedTimeLong(TimeUnit unit) {

        return TimeUnits.fromNanos(TL_INTERVAL.get().getEllapsedNanos(), unit);
    }

    public static double getEllapsedTimeDouble(TimeUnit unit) {

        return TimeUnits.fromNanosDouble(TL_INTERVAL.get().getEllapsedNanos(), unit);
    }

    private TimerUtils() {

        // not instantiable
    }
}
