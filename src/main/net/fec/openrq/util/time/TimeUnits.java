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
public final class TimeUnits {

    // =========== NANOSECONDS =========== //

    public static long fromNanos(long nanos, TimeUnit destUnit) {

        return destUnit.convert(nanos, TimeUnit.NANOSECONDS);
    }

    public static double fromNanosDouble(long nanos, TimeUnit destUnit) {

        return (double)nanos / destUnit.toNanos(1L);
    }

    // =========== MICROSECONDS =========== //

    public static long fromMicros(long micros, TimeUnit destUnit) {

        return destUnit.convert(micros, TimeUnit.MICROSECONDS);
    }

    // =========== MILLISECONDS =========== //

    public static long fromMillis(long millis, TimeUnit destUnit) {

        return destUnit.convert(millis, TimeUnit.MILLISECONDS);
    }

    // =========== SECONDS =========== //

    public static long fromSeconds(long seconds, TimeUnit destUnit) {

        return destUnit.convert(seconds, TimeUnit.SECONDS);
    }

    // =========== MINUTES =========== //

    public static long fromMinutes(long minutes, TimeUnit destUnit) {

        return destUnit.convert(minutes, TimeUnit.MINUTES);
    }

    // =========== HOURS =========== //

    public static long fromHours(long hours, TimeUnit destUnit) {

        return destUnit.convert(hours, TimeUnit.HOURS);
    }

    // =========== DAYS =========== //

    public static long fromDays(long days, TimeUnit destUnit) {

        return destUnit.convert(days, TimeUnit.DAYS);
    }

    private TimeUnits() {

        // not instantiable
    }
}
