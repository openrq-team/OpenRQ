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
package net.fec.openrq.properties;

/**
 * This enum contains all {@linkplain System#getProperties() system properties} supported by OpenRQ. Use method
 * {@link #toString()} to get the property key.
 */
public enum SupportedProperties {

    /**
     * If present, this property limits the maximum number of threads that OpenRQ uses for running parallel tasks.
     * <p>
     * Set this property value to 0 if you wish to disable parallelism.
     */
    MAX_PARALLEL_THREADS {

        @Override
        public String toString() {

            return "openrq.parallel.maxthreads";
        }
    }
}
