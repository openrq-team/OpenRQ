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


import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * An {@link ExecutorService} that executes submitted tasks in caller threads.
 */
public class SameThreadExecutorService extends AbstractExecutorService {

    private final ExecutorService delegate = SameThreadThreadPool.createInstance();


    @Override
    public void shutdown() {

        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {

        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {

        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {

        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {

        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {

        delegate.execute(command);
    }


    private static final class SameThreadThreadPool extends ThreadPoolExecutor {

        static ExecutorService createInstance() {

            final SameThreadThreadPool instance = new SameThreadThreadPool();

            // submit task to keep the thread pool full (simply waits for termination)
            instance.submit(new Runnable() {

                @Override
                public void run() {

                    try {
                        instance.signal.await();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

            return Executors.unconfigurableExecutorService(instance);
        }


        // for shutdown
        private final CountDownLatch signal = new CountDownLatch(1);


        private SameThreadThreadPool() {

            super(
                1, 1, // core/max threads
                0, TimeUnit.NANOSECONDS, // keep-alive time
                new SynchronousQueue<Runnable>(), // hand-off queue
                new DaemonThreadFactory(), // created thread are daemon threads
                new CallerRunsPolicy()); // current thread executes submitted tasks
        }

        @Override
        public void shutdown() {

            super.shutdown();
            signal.countDown();
        }
    }
}
