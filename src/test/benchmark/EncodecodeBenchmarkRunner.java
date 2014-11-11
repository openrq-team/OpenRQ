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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.SourceBlockDecodingTest;
import net.fec.openrq.SourceBlockEncodingTest;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.io.SafeStandardStreams;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormat;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;


/**
 * 
 */
public final class EncodecodeBenchmarkRunner {

    private static final PrintStream STDOUT = SafeStandardStreams.safeSTDOUT();
    private static final int DEFAULT_SYMBOL_SIZE = 1500; // in bytes
    private static final int DEFAULT_SYMBOL_OVERHEAD = 0;
    private static final int DEFAULT_FORKS = 1;


    public static void main(String[] args) {

        final JCommander jCommander = new JCommander();

        try {
            final InputOptions options = parseOptions(jCommander, args);

            final List<Integer> Ks = options.numSourceSymbolsList();
            final int T = options.symbolSize();
            final List<Integer> sos = options.symbolOverheadList();
            final boolean runEncoding = options.runEncodingBenchmarks();
            final boolean runDecoding = options.runDecodingBenchmarks();
            final int forks = options.forks();
            final VerboseMode verbMode = getVerboseMode(options);

            final List<RunResult> results = new ArrayList<>();
            final PrintWriter pw = new PrintWriter(STDOUT, true); // true means autoflush is on
            final ResultFormat resultFormat = ResultFormatFactory.getInstance(ResultFormatType.TEXT, pw);

            final long startNanos = System.nanoTime();
            STDOUT.println("Starting benchmark runners...");

            if (runEncoding) {
                for (int K : Ks) {
                    final int F = deriveDataLength(K, T);

                    STDOUT.println("Running encoding benchmark for K = " + K);
                    Runner encRunner = newEncodingRunner(F, K, forks, verbMode);
                    results.addAll(encRunner.run());
                }
            }

            if (runDecoding) {
                for (int K : Ks) {
                    final int F = deriveDataLength(K, T);

                    for (int symbover : sos) {
                        STDOUT.println("Running decoding benchmark for K = " + K + " and symbol overhead = " + symbover);
                        Runner decRunner = newDecodingRunner(F, K, symbover, forks, verbMode);
                        results.addAll(decRunner.run());
                    }
                }
            }

            final long ellapsed = System.nanoTime() - startNanos;
            STDOUT.println("Done. Benchmark time: " + TimeUnit.NANOSECONDS.toSeconds(ellapsed) + "s");
            STDOUT.println();
            STDOUT.println("Benchmark results:");
            resultFormat.writeOut(results);
            pw.flush();
        }
        catch (ParameterException e) {
            STDOUT.println(e.getMessage());
            STDOUT.println();
            printUsage(jCommander);
        }
        catch (RunnerException e) {
            e.printStackTrace(STDOUT);
        }
    }

    private static int deriveDataLength(int srcsymbs, int symbsize) {

        return srcsymbs * symbsize;
    }

    private static VerboseMode getVerboseMode(InputOptions options) {

        if (options.extraVerbose) {
            return VerboseMode.EXTRA;
        }
        else if (options.verbose) {
            return VerboseMode.NORMAL;
        }
        else {
            return VerboseMode.SILENT;
        }
    }

    private static Runner newEncodingRunner(int datalen, int srcsymbs, int forks, VerboseMode mode) {

        Options opt = new OptionsBuilder()
            .include(SourceBlockEncodingTest.class.getName() + ".*")
            .param("datalen", datalen + "")
            .param("srcsymbs", srcsymbs + "")
            .forks(forks)
            .build();

        return new Runner(opt, getOutputFormat(mode));
    }

    private static Runner newDecodingRunner(int datalen, int srcsymbs, int symbover, int forks, VerboseMode mode) {

        Options opt = new OptionsBuilder()
            .include(SourceBlockDecodingTest.class.getName() + ".*")
            .param("datalen", datalen + "")
            .param("srcsymbs", srcsymbs + "")
            .param("symbover", symbover + "")
            .forks(forks)
            .build();

        return new Runner(opt, getOutputFormat(mode));
    }

    private static OutputFormat getOutputFormat(VerboseMode mode) {

        return OutputFormatFactory.createFormatInstance(STDOUT, mode);
    }

    private static InputOptions parseOptions(JCommander jCommander, String[] args) throws ParameterException {

        final InputOptions options = new InputOptions();
        jCommander.addObject(options);
        jCommander.parse(args);
        return options;
    }

    private static void printUsage(JCommander jCommander) {

        StringBuilder usageBuilder = new StringBuilder();
        jCommander.usage(usageBuilder);
        STDOUT.println(usageBuilder);
    }


    private static final class InputOptions {

        @Parameter(names = {"-k", "-K", "--srcsymbs"},
            description = "Space separated list of K values (K is the number of source symbols in one block)",
            required = true,
            variableArity = true,
            validateValueWith = NumSourceSymbolsValidator.class)
        private final List<String> numSourceSymbolsList = new ArrayList<>();

        @Parameter(names = {"-t", "-T", "--symbsize"},
            description = "The size of each symbol in number of bytes",
            validateValueWith = SymbolSizeValidator.class)
        private int symbolSize = DEFAULT_SYMBOL_SIZE;

        @Parameter(names = {"-o", "-O", "--symbover"},
            description = "Space separated list of symbol overhead values (used only in decoding benchmarks)",
            variableArity = true,
            validateValueWith = SymbolOverheadsValidator.class)
        private final List<String> symbolOverheadList = defaultSymbolOverheadList();

        @Parameter(names = {"-e", "-E", "--encodingonly"},
            description = "Only run encoding benchmarks (unless option \"-d\" is used as well)")
        private boolean runEncodingOnly = false;

        @Parameter(names = {"-d", "-D", "--decodingonly"},
            description = "Only run decoding benchmarks  (unless option \"-e\" is used as well)")
        private boolean runDecodingOnly = false;

        @Parameter(names = {"-f", "--forks"},
            description = "How many times to fork a single benchmark",
            variableArity = true,
            validateValueWith = NonNegativeValidator.class)
        private int forks = DEFAULT_FORKS;

        @Parameter(names = {"-v", "-V"},
            description = "Details about the current benchmark will be printed to the standard output")
        private boolean verbose = false;

        @Parameter(names = {"-vv", "-VV"},
            description = "Extra details about the current benchmark will be printed to the standard output")
        private boolean extraVerbose = false;


        private static List<String> defaultSymbolOverheadList() {

            List<String> list = new ArrayList<>();
            list.add(DEFAULT_SYMBOL_OVERHEAD + "");
            return list;
        }

        List<Integer> numSourceSymbolsList() {

            final List<Integer> list = new ArrayList<>(numSourceSymbolsList.size());
            for (String K : numSourceSymbolsList) {
                list.add(Integer.valueOf(K));
            }
            Collections.sort(list);
            return list;
        }

        int symbolSize() {

            return symbolSize;
        }

        List<Integer> symbolOverheadList() {

            final List<Integer> list = new ArrayList<>(symbolOverheadList.size());
            for (String symbover : symbolOverheadList) {
                list.add(Integer.valueOf(symbover));
            }
            Collections.sort(list);
            return list;
        }

        boolean runEncodingBenchmarks() {

            return runEncodingOnly || !runDecodingOnly;
        }

        boolean runDecodingBenchmarks() {

            return runDecodingOnly || !runEncodingOnly;
        }

        int forks() {

            return forks;
        }
    }

    public static final class NumSourceSymbolsValidator implements IValueValidator<List<String>> {

        @Override
        public void validate(@SuppressWarnings("unused") String name, List<String> value) throws ParameterException {

            try {
                for (String Kstr : value) {
                    final int K = Integer.parseInt(Kstr);
                    if (ParameterChecker.isNumSourceSymbolsPerBlockOutOfBounds(K)) {
                        throw new ParameterException(String.format(
                            "Number of source symbols (%d) must be within [%d, %d]",
                            K,
                            ParameterChecker.minNumSourceSymbolsPerBlock(),
                            ParameterChecker.maxNumSourceSymbolsPerBlock()));
                    }
                }
            }
            catch (NumberFormatException e) {
                throw new ParameterException("Invalid number of source symbols: " + e.getMessage());
            }
        }
    }

    public static final class SymbolSizeValidator implements IValueValidator<Integer> {

        @Override
        public void validate(@SuppressWarnings("unused") String name, Integer value) throws ParameterException {

            if (ParameterChecker.isSymbolSizeOutOfBounds(value)) {
                throw new ParameterException(String.format(
                    "Symbol size (%d) must be within [%d, %d] bytes",
                    value,
                    ParameterChecker.minSymbolSize(),
                    ParameterChecker.maxSymbolSize()));
            }
        }
    }

    public static final class SymbolOverheadsValidator implements IValueValidator<List<String>> {

        private static final NonNegativeValidator NON_NEGATIVE_VALIDATOR = new NonNegativeValidator();


        @Override
        public void validate(String name, List<String> value) throws ParameterException {

            try {
                for (String symboverStr : value) {
                    NON_NEGATIVE_VALIDATOR.validate(name, Integer.valueOf(symboverStr));
                }
            }
            catch (NumberFormatException e) {
                throw new ParameterException("Invalid symbol overhead: " + e.getMessage());
            }
        }
    }

    public static final class NonNegativeValidator implements IValueValidator<Integer> {

        @Override
        public void validate(String name, Integer value) throws ParameterException {

            if (value < 0) {
                throw new ParameterException("Option \"" + name + "\": number must be non-negative");
            }
        }
    }


    private EncodecodeBenchmarkRunner() {

        // not instantiable
    }
}
