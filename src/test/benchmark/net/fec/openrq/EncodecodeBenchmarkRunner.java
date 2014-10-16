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


import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


/**
 * 
 */
public final class EncodecodeBenchmarkRunner {

    private static final int DEFAULT_SOURCE_SYMBOL_SIZE = 1500; // in bytes


    private static Runner newEncodeRunner(int datalen, int srcsymbs, int forks) {

        Options opt = new OptionsBuilder()
            .include(SourceBlockEncodingTest.class.getSimpleName())
            .param("datalen", datalen + "")
            .param("srcsymbs", srcsymbs + "")
            .forks(forks)
            .build();

        return new Runner(opt);
    }

    private static Runner newDecoderRunner(int datalen, int srcsymbs, int symbover, int forks) {

        Options opt = new OptionsBuilder()
            .include(SourceBlockDecodingTest.class.getSimpleName())
            .param("datalen", datalen + "")
            .param("srcsymbs", srcsymbs + "")
            .param("symbover", symbover + "")
            .forks(forks)
            .build();

        return new Runner(opt);
    }

    private static int deriveDataLength(int srcsymbs, int symbsize) {

        return srcsymbs * symbsize;
    }

    public static void main(String[] args) {

        
    }

    private EncodecodeBenchmarkRunner() {

        // not instantiable
    }
}
