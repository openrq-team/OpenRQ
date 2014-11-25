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


import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.fec.openrq.properties.SupportedProperties;
import net.fec.openrq.util.concurrent.DaemonThreadFactory;
import net.fec.openrq.util.concurrent.SameThreadExecutorService;
import net.fec.openrq.util.concurrent.SilentFuture;


final class Parallelism {

    static <T> SilentFuture<T> submitTask(Callable<T> task) {

        return SilentFuture.of(INSTANCE.submit(task));
    }

    static SilentFuture<?> submitTask(Runnable task) {

        return SilentFuture.of(INSTANCE.submit(task));
    }


    private static final Parallelism INSTANCE = init();


    private static Parallelism init() {

        final ExecutorService executor;

        final int maxThreads = getMaxThreads();
        if (maxThreads == 0) {
            executor = new SameThreadExecutorService();
        }
        else {
            executor = Executors.newFixedThreadPool(maxThreads, new DaemonThreadFactory());
        }

        return new Parallelism(executor);
    }

    private static int getMaxThreads() {

        // we never use more than this number of threads
        int maxThreads = Runtime.getRuntime().availableProcessors();

        String maxThreadsProp = System.getProperty(SupportedProperties.MAX_PARALLEL_THREADS.toString());
        if (maxThreadsProp != null) {
            try {
                final int configuredMaxThreads = Integer.parseInt(maxThreadsProp);
                if (configuredMaxThreads >= 0 && configuredMaxThreads < maxThreads) {
                    maxThreads = configuredMaxThreads;
                }
            }
            catch (NumberFormatException ex) {/* ignore */}
        }

        return maxThreads;
    }


    private final ExecutorService executor;


    private Parallelism(ExecutorService executor) {

        this.executor = Objects.requireNonNull(executor);
    }

    private <T> Future<T> submit(Callable<T> task) {

        return executor.submit(task);
    }

    private Future<?> submit(Runnable task) {

        return executor.submit(task);
    }
}
