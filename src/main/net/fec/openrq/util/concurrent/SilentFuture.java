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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A future object that wraps inside a {@link RuntimeException} any non-runtime exception thrown by method
 * {@link #get()}.
 * 
 * @param <V>
 *            The type of the result returned by method {@link #get()}
 */
public class SilentFuture<V> implements Future<V> {

    /**
     * Wraps a future object inside a silent future.
     * 
     * @param <V>
     *            The type of the result returned by method {@link #get()}
     * @param future
     *            The encapsulated future object
     * @return a silent future
     */
    public static <V> SilentFuture<V> of(Future<V> future) {

        return new SilentFuture<>(future);
    }


    private final Future<V> delegate;


    private SilentFuture(Future<V> future) {

        this.delegate = Objects.requireNonNull(future);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {

        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {

        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {

        return delegate.isDone();
    }

    @Override
    public V get() {

        try {
            return delegate.get();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws TimeoutException {

        try {
            return delegate.get(timeout, unit);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
