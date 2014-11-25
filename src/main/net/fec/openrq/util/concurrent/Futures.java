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
package net.fec.openrq.util.concurrent;


import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * 
 */
public final class Futures {

    public static void awaitCompletion(Iterable<Future<?>> futures) throws InterruptedException {

        Objects.requireNonNull(futures);

        boolean done = false;
        try {
            for (Future<?> f : futures) {
                if (!f.isDone()) {
                    try {
                        f.get();
                    }
                    catch (CancellationException | ExecutionException e) {/* ignore */}
                }
            }
            done = true;
        }
        finally {
            if (!done) {
                for (Future<?> f : futures) {
                    f.cancel(true);
                }
            }
        }
    }

    public static void awaitCompletionQuietly(Iterable<SilentFuture<?>> futures) {

        Objects.requireNonNull(futures);

        boolean done = false;
        try {
            for (SilentFuture<?> f : futures) {
                if (!f.isDone()) {
                    f.get();
                }
            }
            done = true;
        }
        finally {
            if (!done) {
                for (SilentFuture<?> f : futures) {
                    f.cancel(true);
                }
            }
        }
    }

    private Futures() {

        // not instantiable
    }
}
