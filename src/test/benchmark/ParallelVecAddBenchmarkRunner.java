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

import net.fec.openrq.ParallelVectorAdditionTest;
import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.util.io.SafeStandardStreams;
import net.fec.openrq.util.math.ExtraMath;

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
public final class ParallelVecAddBenchmarkRunner {

    private static final PrintStream STDOUT = SafeStandardStreams.safeSTDOUT();
    private static final int DEFAULT_NUM_SYMBOLS = 1000;
    private static final int DEFAULT_GRANULARITY = 100;
    private static final int DEFAULT_SYMBOL_SIZE = 1500; // in bytes
    private static final int DEFAULT_FORKS = 1;


    public static void main(String[] args) {

        final JCommander jCommander = new JCommander();

        try {
            final InputOptions options = parseOptions(jCommander, args);

            final List<Integer> subKList = getNumSymbolsSubsetList(options.numSourceSymbols, options.granularity);
            final List<Integer> TList = options.symbolSizeList();
            final List<Integer> pTasksList = getNumParallelTasksList(options.varyParTasks);
            final int forks = options.forks;
            final VerboseMode verbMode = getVerboseMode(options);

            final List<RunResult> results = new ArrayList<>();
            final PrintWriter pw = new PrintWriter(STDOUT, true); // true means autoflush is on
            final ResultFormat resultFormat = ResultFormatFactory.getInstance(ResultFormatType.TEXT, pw);

            final long startNanos = System.nanoTime();
            STDOUT.println("Starting benchmark runners...");

            for (int subK : subKList) {
                for (int T : TList) {
                    for (int pTasks : pTasksList) {
                        STDOUT.printf("Running benchmark for SUB_K = %d; T = %d; PAR_TASKS = %d%n", subK, T, pTasks);
                        Runner encRunner = newRunner(subK, T, pTasks, forks, verbMode);
                        results.addAll(encRunner.run());
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

    /*
     * Requires valid K and gran > 0
     */
    private static List<Integer> getNumSymbolsSubsetList(int K, int gran) {

        final List<Integer> list = new ArrayList<>(ExtraMath.ceilDiv(K, gran));

        int subK = gran;
        while (subK < K) {
            list.add(subK);
            subK = ExtraMath.addExact(subK, gran);
        }
        list.add(K);

        return list;
    }

    private static List<Integer> getNumParallelTasksList(boolean varyParTasks) {

        final int maxT = ParallelVectorAdditionTest.MAX_THREAD_POOL_SIZE;
        if (varyParTasks) {
            final int size = (maxT == 1) ? 1 : ExtraMath.ceilLog2(maxT);

            final List<Integer> list = new ArrayList<>(size);
            for (int n = 0; n < size; n++) {
                long powerOf2 = 1L << (n + 1);
                list.add((int)Math.min(powerOf2, maxT));
            }

            return list;
        }
        else {
            return Collections.singletonList(maxT);
        }
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

    private static Runner newRunner(int numvecs, int vecsize, int numpartasks, int forks, VerboseMode mode) {

        Options opt = new OptionsBuilder()
            .include(ParallelVectorAdditionTest.class.getName() + ".*")
            .param("numvecs", numvecs + "")
            .param("vecsize", vecsize + "")
            .param("numpartasks", numpartasks + "")
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
            description = "The number of source symbols in one block",
            required = true,
            validateValueWith = NumSourceSymbolsValidator.class)
        private int numSourceSymbols = DEFAULT_NUM_SYMBOLS;

        @Parameter(names = {"-g", "-G", "--granularity"},
            description = "The size of a subset of source symbols in one block",
            validateValueWith = PositiveValidator.class)
        private int granularity = DEFAULT_GRANULARITY;

        @Parameter(names = {"-t", "-T", "--symbsize"},
            description = "Space separated list of symbol sizes in number of bytes",
            variableArity = true,
            validateValueWith = SymbolSizeValidator.class)
        private List<String> symbolSizeList = defaultSymbolSizeList();

        @Parameter(names = {"-p", "-P", "--varypartasks"},
            description = "Vary number of parallel tasks in powers of 2 until number of cores")
        private boolean varyParTasks = false;

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


        private static List<String> defaultSymbolSizeList() {

            List<String> list = new ArrayList<>();
            list.add(DEFAULT_SYMBOL_SIZE + "");
            return list;
        }

        List<Integer> symbolSizeList() {

            final List<Integer> list = new ArrayList<>(symbolSizeList.size());
            for (String T : symbolSizeList) {
                list.add(Integer.valueOf(T));
            }
            Collections.sort(list);
            return list;
        }
    }

    public static final class NumSourceSymbolsValidator implements IValueValidator<Integer> {

        @Override
        public void validate(@SuppressWarnings("unused") String name, Integer K) throws ParameterException {

            if (ParameterChecker.isNumSourceSymbolsPerBlockOutOfBounds(K)) {
                throw new ParameterException(String.format(
                    "Number of source symbols (%d) must be within [%d, %d]",
                    K,
                    ParameterChecker.minNumSourceSymbolsPerBlock(),
                    ParameterChecker.maxNumSourceSymbolsPerBlock()));
            }
        }
    }

    public static final class SymbolSizeValidator implements IValueValidator<List<String>> {

        @Override
        public void validate(@SuppressWarnings("unused") String name, List<String> value) throws ParameterException {

            try {
                for (String Tstr : value) {
                    final int T = Integer.parseInt(Tstr);
                    if (ParameterChecker.isSymbolSizeOutOfBounds(T)) {
                        throw new ParameterException(String.format(
                            "Symbol size (%d) must be within [%d, %d] bytes",
                            T,
                            ParameterChecker.minSymbolSize(),
                            ParameterChecker.maxSymbolSize()));
                    }
                }
            }
            catch (NumberFormatException e) {
                throw new ParameterException("Invalid symbol size: " + e.getMessage());
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

    public static final class PositiveValidator implements IValueValidator<Integer> {

        @Override
        public void validate(String name, Integer value) throws ParameterException {

            if (value <= 0) {
                throw new ParameterException("Option \"" + name + "\": number must be positive");
            }
        }
    }


    private ParallelVecAddBenchmarkRunner() {

        // not instantiable
    }
}
