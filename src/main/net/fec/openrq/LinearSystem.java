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


import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.concurrent.SilentFuture;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.dense.RowIndirected2DByteMatrix;
import net.fec.openrq.util.linearalgebra.vector.dense.BasicByteVector;
import net.fec.openrq.util.math.OctetOps;
import net.fec.openrq.util.rq.Rand;
import net.fec.openrq.util.rq.SystematicIndices;
import net.fec.openrq.util.time.TimeUnits;
import net.fec.openrq.util.time.TimerUtils;


/**
 */
final class LinearSystem {

    private static final Factory DENSE_FACTORY = LinearAlgebra.BASIC2D_FACTORY;
    private static final Factory SPARSE_FACTORY = LinearAlgebra.CRS_FACTORY;

    // there is no benefit for a dense matrix in all values of K
    private static final long A_SPARSE_THRESHOLD = 0L;
    private static final long MT_SPARSE_THRESHOLD = 0L;

    private static final boolean TIMER_CODE_ENABLED = false; // DEBUG
    private static final PrintStream TIMER_PRINTABLE = System.out; // DEBUG


    private static void debugStartTimer() {

        if (TIMER_CODE_ENABLED) {
            TimerUtils.beginTimer();
        }
    }

    private static void debugEndTimer() {

        if (TIMER_CODE_ENABLED) {
            TimerUtils.markTimestamp();
        }
    }

    private static long debugEllapsedNanos() {

        if (TIMER_CODE_ENABLED) {
            return TimerUtils.getEllapsedTimeLong(TimeUnit.NANOSECONDS);
        }
        else {
            return 0L;
        }
    }

    private static void debugPrintMillis(String prefix, long nanos) {

        if (TIMER_CODE_ENABLED) {
            final double millis = TimeUnits.fromNanosDouble(nanos, TimeUnit.MILLISECONDS);
            TIMER_PRINTABLE.printf("%s: %.03f ms%n", prefix, millis);
        }
    }

    private static void debugPrintNewLine() {

        if (TIMER_CODE_ENABLED) {
            TIMER_PRINTABLE.println();
        }
    }

    private static void debugPrintLine(String message) {

        if (TIMER_CODE_ENABLED) {
            TIMER_PRINTABLE.println(message);
        }
    }

    private static Factory getMatrixAfactory(int L, int overheadRows) {

        if ((long)L * (L + overheadRows) < A_SPARSE_THRESHOLD) {
            return DENSE_FACTORY;
        }
        else {
            return SPARSE_FACTORY;
        }
    }

    private static Factory getMatrixMTfactory(int H, int Kprime, int S) {

        if ((long)H * (Kprime + S) < MT_SPARSE_THRESHOLD) {
            return DENSE_FACTORY;
        }
        else {
            return SPARSE_FACTORY;
        }
    }

    /**
     * Initializes the G_LDPC1 submatrix.
     * 
     * @param A
     * @param B
     * @param S
     */
    private static void initializeG_LPDC1(ByteMatrix A, int B, int S)
    {

        int circulant_matrix = -1;

        for (int col = 0; col < B; col++)
        {
            int circulant_matrix_column = col % S;

            if (circulant_matrix_column != 0)
            {
                // cyclic down-shift
                A.set(0, col, A.get(S - 1, col - 1));

                for (int row = 1; row < S; row++)
                {
                    A.set(row, col, A.get(row - 1, col - 1));
                }
            }
            else
            {   // if 0, then it's the first column of the current circulant matrix

                circulant_matrix++;

                // 0
                A.set(0, col, (byte)1);

                // (i + 1) mod S
                A.set((circulant_matrix + 1) % S, col, (byte)1);

                // (2 * (i + 1)) mod S
                A.set((2 * (circulant_matrix + 1)) % S, col, (byte)1);
            }
        }
    }

    /**
     * Initializes the G_LPDC2 submatrix.
     * 
     * @param A
     * @param S
     * @param P
     * @param W
     */
    private static void initializeG_LPDC2(ByteMatrix A, int S, int P, int W) {

        for (int row = 0; row < S; row++)
        {
            // consecutive 1s modulo P
            A.set(row, (row % P) + W, (byte)1);
            A.set(row, ((row + 1) % P) + W, (byte)1);
        }
    }

    /**
     * Initializes the I_S submatrix.
     * 
     * @param A
     * @param S
     * @param B
     */
    private static void initializeIs(ByteMatrix A, int S, int B) {

        for (int n = 0; n < S; n++) {
            A.set(n, n + B, (byte)1);
        }
    }

    /**
     * Initializes the I_H submatrix.
     * 
     * @param A
     * @param W
     * @param U
     * @param H
     * @param S
     */
    private static void initializeIh(ByteMatrix A, int W, int U, int H, int S)
    {

        int lower_limit_col = W + U;

        for (int n = 0; n < H; n++) {
            A.set(n + S, n + lower_limit_col, (byte)1);
        }
    }

    /**
     * Generates the MT matrix that is used to generate G_HDPC submatrix.
     * 
     * @param H
     * @param Kprime
     * @param S
     * @return MT
     */
    private static ByteMatrix generateMT(int H, int Kprime, int S)
    {

        ByteMatrix MT = getMatrixMTfactory(H, Kprime, S).createMatrix(H, Kprime + S);

        for (int row = 0; row < H; row++)
        {
            for (int col = 0; col < Kprime + S - 1; col++)
            {
                if (row == (int)Rand.rand(col + 1, 6, H) ||
                    row == (((int)Rand.rand(col + 1, 6, H) + (int)Rand.rand(col + 1, 7, H - 1) + 1) % H))
                {
                    MT.set(row, col, (byte)1);
                }
            }
        }

        for (int row = 0; row < H; row++) {
            MT.set(row, Kprime + S - 1, OctetOps.alphaPower(row));
        }

        return MT;
    }

    /**
     * Generates the GAMMA matrix that is used to generate G_HDPC submatrix.
     * 
     * @param Kprime
     * @param S
     * @return GAMMA
     */
    private static ByteMatrix generateGAMMA(int Kprime, int S)
    {

        // FIXME this needs a more efficient representation since it is a lower triangular matrix
        ByteMatrix GAMMA = DENSE_FACTORY.createMatrix(Kprime + S, Kprime + S);

        for (int row = 0; row < Kprime + S; row++)
        {
            for (int col = 0; col < Kprime + S; col++)
            {
                if (row >= col) {
                    GAMMA.set(row, col, OctetOps.alphaPower((row - col) % 256));
                }
            }
        }

        return GAMMA;
    }

    /**
     * Initializes the G_ENC submatrix.
     * 
     * @param A
     * @param S
     * @param H
     * @param L
     * @param Kprime
     */
    private static void initializeG_ENC(ByteMatrix A, int S, int H, int L, int Kprime)
    {

        for (int row = S + H; row < L; row++)
        {
            Tuple tuple = new Tuple(Kprime, row - S - H);

            Set<Integer> indexes = encIndexes(Kprime, tuple);

            for (Integer j : indexes)
            {
                A.set(row, j, (byte)1);
            }
        }
    }

    /**
     * Generates the constraint matrix.
     * 
     * @param Kprime
     * @return a constraint matrix
     */
    static ByteMatrix generateConstraintMatrix(int Kprime) {

        return generateConstraintMatrix(Kprime, 0);
    }

    /**
     * Generates the constraint matrix.
     * 
     * @param Kprime
     * @param overheadRows
     * @return a constraint matrix
     */
    static ByteMatrix generateConstraintMatrix(int Kprime, int overheadRows) {

        // calculate necessary parameters
        final int Ki = SystematicIndices.getKIndex(Kprime);
        final int S = SystematicIndices.S(Ki);
        final int H = SystematicIndices.H(Ki);
        final int W = SystematicIndices.W(Ki);
        final int L = Kprime + S + H;
        final int P = L - W;
        final int U = P - H;
        final int B = W - S;

        debugStartTimer(); // DEBUG

        // allocate memory for the constraint matrix
        ByteMatrix A = getMatrixAfactory(L, overheadRows).createMatrix(L + overheadRows, L);

        /*
         * upper half
         */

        // initialize G_LPDC2
        initializeG_LPDC2(A, S, P, W);

        // initialize G_LPDC1
        initializeG_LPDC1(A, B, S);

        // initialize I_s
        initializeIs(A, S, B);

        /*
         * bottom half
         */

        // initialize I_h
        initializeIh(A, W, U, H, S);

        // initialize G_HDPC

        // MT
        ByteMatrix MT = generateMT(H, Kprime, S);

        // GAMMA
        ByteMatrix GAMMA = generateGAMMA(Kprime, S);

        // G_HDPC = MT * GAMMA
        ByteMatrix G_HDPC = MT.multiply(GAMMA);

        // initialize G_HDPC
        for (int row = S; row < S + H; row++) {
            for (int col = 0; col < W + U; col++) {
                A.set(row, col, G_HDPC.get(row - S, col));
            }
        }

        // initialize G_ENC
        initializeG_ENC(A, S, H, L, Kprime);

        // DEBUG
        debugEndTimer();
        debugPrintNewLine();
        debugPrintMillis("constraint matrix gen", debugEllapsedNanos());

        // return the constraint matrix
        return A;
    }

    /**
     * Returns the indexes of the intermediate symbols that should be XORed to encode
     * the symbol for the given tuple.
     * 
     * @param Kprime
     * @param tuple
     * @return Set of indexes.
     */
    static Set<Integer> encIndexes(int Kprime, Tuple tuple)
    {

        // allocate memory for the indexes
        final Set<Integer> indexes = new HashSet<>(Kprime);

        // parameters
        final int Ki = SystematicIndices.getKIndex(Kprime);
        final int S = SystematicIndices.S(Ki);
        final int H = SystematicIndices.H(Ki);
        final int W = SystematicIndices.W(Ki);
        final long L = Kprime + S + H;
        final long P = L - W;
        final long P1 = MatrixUtilities.ceilPrime(P);

        // tuple parameters
        final long d = tuple.getD();
        final long a = tuple.getA();

        long b = tuple.getB();

        final long d1 = tuple.getD1();
        final long a1 = tuple.getA1();

        long b1 = tuple.getB1();

        /*
         * simulated encoding -- refer to section 5.3.3.3 of RFC 6330
         */

        indexes.add((int)b);

        for (long j = 0; j < d; j++)
        {
            b = (b + a) % W;
            indexes.add((int)b);
        }

        while (b1 >= P)
        {
            b1 = (b1 + a1) % P1;
        }

        indexes.add((int)(W + b1));

        for (long j = 1; j < d1; j++)
        {
            do
                b1 = (b1 + a1) % P1;
            while (b1 >= P);

            indexes.add((int)(W + b1));
        }

        return indexes;
    }

    /**
     * Encodes a source symbol.
     * 
     * @param Kprime
     * @param C
     * @param tuple
     * @param T
     * @return an encoding symbol
     */
    static byte[] enc(int Kprime, byte[][] C, Tuple tuple, int T) {

        // necessary parameters
        final int Ki = SystematicIndices.getKIndex(Kprime);
        final int S = SystematicIndices.S(Ki);
        final int H = SystematicIndices.H(Ki);
        final int W = SystematicIndices.W(Ki);
        final long L = Kprime + S + H;
        final long P = L - W;
        final int P1 = (int)MatrixUtilities.ceilPrime(P);
        final long d = tuple.getD();
        final int a = (int)tuple.getA();

        int b = (int)tuple.getB();

        final long d1 = tuple.getD1();
        final int a1 = (int)tuple.getA1();

        int b1 = (int)tuple.getB1();

        // allocate memory and initialize the encoding symbol
        final byte[] result = Arrays.copyOf(C[b], T);

        /*
         * encoding -- refer to section 5.3.5.3 of RFC 6330
         */

        for (long j = 0; j < d; j++)
        {
            b = (b + a) % W;
            OctetOps.vectorVectorAddition(C[b], result, result);
        }

        while (b1 >= P)
            b1 = (b1 + a1) % P1;

        OctetOps.vectorVectorAddition(C[W + b1], result, result);

        for (long j = 1; j < d1; j++)
        {
            do
                b1 = (b1 + a1) % P1;
            while (b1 >= P);

            OctetOps.vectorVectorAddition(C[W + b1], result, result);
        }

        return result;
    }

    /**
     * Solves the decoding system of linear equations using the permanent inactivation technique.
     * 
     * @param A
     *            The constraint matrix
     * @param D
     *            The vector with available symbols (each row of the matrix contains one symbol)
     * @param Kprime
     *            The total number of source symbols for decoding
     * @return the intermediate symbols
     * @throws SingularMatrixException
     *             If the decoding fails
     */
    static byte[][] PInactivationDecoding(ByteMatrix A, byte[][] D, int Kprime)
        throws SingularMatrixException
    {

        // decoding parameters
        int Ki = SystematicIndices.getKIndex(Kprime);
        int S = SystematicIndices.S(Ki);
        int H = SystematicIndices.H(Ki);
        int W = SystematicIndices.W(Ki);
        int L = Kprime + S + H;
        int P = L - W;
        int M = A.rows();

        // ISDCodeWriter.instance().prepare(); // DEBUG
        // ISDCodeWriter.instance().writeKprimeCode(Kprime); // DEBUG

        return pidPhase1(A, D, S, H, L, P, M);
    }


    private static final long ISHDPC_BITS = 0xFFFFFFFF00000000L;
    private static final long DEGREE_BITS = 0x00000000FFFFFFFFL;


    private static long buildRowInfo(boolean isHDPC, int degree) {

        return (isHDPC ? ISHDPC_BITS : 0L) | (degree & DEGREE_BITS);
    }

    private static boolean isHDPCRow(long rowInfo) {

        return (rowInfo & ISHDPC_BITS) != 0;
    }

    private static int getRowDegree(long rowInfo) {

        return (int)(rowInfo & DEGREE_BITS);
    }

    private static byte[][] pidPhase1(
        final ByteMatrix A,
        final byte[][] D,
        final int S,
        final int H,
        final int L,
        final int P,
        final int M)
        throws SingularMatrixException
    {

        /*
         * In the first phase of the Gaussian elimination, the matrix A is
         * conceptually partitioned into submatrices and, additionally, a matrix
         * X is created. This matrix has as many rows and columns as A, and it
         * will be a lower triangular matrix throughout the first phase. At the
         * beginning of this phase, the matrix A is copied into the matrix X.
         * 
         * The submatrix sizes are parameterized by non-negative integers i and
         * u, which are initialized to 0 and P, the number of PI symbols,
         * respectively. The submatrices of A are:
         * 
         * 1. The submatrix I defined by the intersection of the first i rows
         * and first i columns. This is the identity matrix at the end of
         * each step in the phase.
         * 
         * 2. The submatrix defined by the intersection of the first i rows and
         * all but the first i columns and last u columns. All entries of
         * this submatrix are zero.
         * 
         * 3. The submatrix defined by the intersection of the first i columns
         * and all but the first i rows. All entries of this submatrix are
         * zero.
         * 
         * 4. The submatrix U defined by the intersection of all the rows and
         * the last u columns.
         * 
         * 5. The submatrix V formed by the intersection of all but the first i
         * columns and the last u columns and all but the first i rows.
         * 
         * The Figure bellow illustrates the submatrices of A. At the beginning
         * of the first phase, V consists of the first L-P columns of A, and U
         * consists of the last P columns corresponding to the PI symbols. In
         * each step, a row of A is chosen.
         * 
         * 
         * +-----------+-----------------+---------+
         * |...........|.................|.........|
         * |.... I ....|....All Zeros....|.........|
         * |...........|.................|.........|
         * +-----------+-----------------+... U ...|
         * |...........|.................|.........|
         * |...........|.................|.........|
         * | All Zeros |...... V ........|.........|
         * |...........|.................|.........|
         * |...........|.................|.........|
         * +-----------+-----------------+---------+
         * 
         * Figure: Submatrices of A in the First Phase
         */

        // DEBUG
        long initNanos = 0L;
        long findRNanos = 0L;
        long chooseRowUsingGraphNanos = 0L;
        long swapRowsNanos = 0L;
        long swapColumnsNanos = 0L;
        long addMultiplyNanos = 0L;

        // DEBUG
        debugStartTimer();

        /*
         * initialize c and d vectors
         */
        final int[] c = new int[L];
        final int[] d = new int[M];

        for (int i = 0; i < L; i++) {
            c[i] = i;
            d[i] = i;
        }

        for (int i = L; i < M; i++) {
            d[i] = i;
        }

        final ByteMatrix X = A.copy();

        // initialize i and u parameters, for the submatrices sizes
        int i = 0, u = P;

        // maps the index of a row to row information
        final long[] rowInfos = new long[M];
        for (int row = 0; row < M; row++) {
            final boolean isHDPC = (row >= S && row < S + H);
            int degree = 0;
            ByteVectorIterator it = A.nonZeroRowIterator(row, 0, L - u);
            while (it.hasNext()) {
                it.next();
                degree += OctetOps.UNSIGN(it.get()); // add to the degree of this row
            }

            rowInfos[row] = buildRowInfo(isHDPC, degree); // store the row information
        }

        // DEBUG
        debugEndTimer();
        initNanos += debugEllapsedNanos();

        // at most L steps
        while (i + u != L)
        {
            /*
             * The following graph defined by the structure of V is used in
             * determining which row of A is chosen. The columns that intersect V
             * are the nodes in the graph, and the rows that have exactly 2 nonzero
             * entries in V and are not HDPC rows are the edges of the graph that
             * connect the two columns (nodes) in the positions of the two ones. A
             * component in this graph is a maximal set of nodes (columns) and edges
             * (rows) such that there is a path between each pair of nodes/edges in
             * the graph. The size of a component is the number of nodes (columns)
             * in the component.
             * 
             * There are at most L steps in the first phase. The phase ends
             * successfully when i + u = L, i.e., when V and the all zeros submatrix
             * above V have disappeared, and A consists of I, the all zeros
             * submatrix below I, and U. The phase ends unsuccessfully in decoding
             * failure if at some step before V disappears there is no nonzero row
             * in V to choose in that step. In each step, a row of A is chosen as
             * follows:
             * 
             * - If all entries of V are zero, then no row is chosen and decoding
             * fails.
             * 
             * - Let r be the minimum integer such that at least one row of A has
             * exactly r nonzeros in V. [HERE WE ASSUME r > 0]
             * 
             * --- If r != 2, then choose a row with exactly r nonzeros in V with
             * minimum original degree among all such rows, except that HDPC
             * rows should not be chosen until all non-HDPC rows have been
             * processed. [HERE WE CONSIDER 'ALL NON-HDPC ROWS' TO BE INCLUDED
             * IN THE SET OF ALL ROWS WITH EXACTLY R NONZEROS]
             * 
             * --- If r = 2 and there is a row with exactly 2 ones in V, then
             * choose any row with exactly 2 ones in V that is part of a
             * maximum size component in the graph described above that is
             * defined by V.
             * 
             * --- If r = 2 and there is no row with exactly 2 ones in V, then
             * choose any row with exactly 2 nonzeros in V. [HERE THE CHOSEN ROW
             * CAN ONLY BE AN HDPC ROW] [FURTHERMORE, THE CHOSEN ROW WILL HAVE
             * THE MINIMUM ORIGINAL DEGREE AMONG ALL CURRENT HDPC ROWS]
             */

            // DEBUG
            debugStartTimer();

            int regularRow = -1;
            int regularRowNonZeros = Integer.MAX_VALUE;
            int regularRowOriginalDegree = Integer.MAX_VALUE;

            int hdpcRow = -1;
            int hdpcRowNonZeros = Integer.MAX_VALUE;
            int hdpcRowOriginalDegree = Integer.MAX_VALUE;

            for (int row = i; row < M; row++) {
                // count the number of nonzeros in V inside A
                final int nonZeros = A.nonZerosInRow(row, i, L - u);
                if (nonZeros == 0) continue; // we never choose a row with only zeros in it

                if (isHDPCRow(rowInfos[row])) {
                    if (nonZeros <= hdpcRowNonZeros) {
                        final int rowDegree = getRowDegree(rowInfos[row]);
                        if (nonZeros < hdpcRowNonZeros || rowDegree < hdpcRowOriginalDegree) {
                            hdpcRow = row;
                            hdpcRowNonZeros = nonZeros;
                            hdpcRowOriginalDegree = rowDegree;
                        }
                    }
                }
                else {
                    if (nonZeros <= regularRowNonZeros) {
                        final int rowDegree = getRowDegree(rowInfos[row]);
                        if (nonZeros < regularRowNonZeros || rowDegree < regularRowOriginalDegree) {
                            regularRow = row;
                            regularRowNonZeros = nonZeros;
                            regularRowOriginalDegree = rowDegree;
                        }
                    }
                }
            }

            // DEBUG
            debugEndTimer();
            findRNanos += debugEllapsedNanos();

            /*
             * choose the row
             */

            final int r; // the real value of r is not found yet
            final int chosenRow; // the actual chosen row is not yet known

            if (regularRow == -1 && hdpcRow == -1) { // decoding failure (no row is available)
                throw new SingularMatrixException("@ Phase 1: all entries in V are zero");
            }
            else if (regularRow == -1) { // only HDPC rows are available
                r = hdpcRowNonZeros;
                chosenRow = hdpcRow;
            }
            else { // a regular row is available (an HDPC row may or may not be available)
                if (hdpcRow != -1 && hdpcRowNonZeros < regularRowNonZeros) {
                    // an HDPC row is available and has less nonzeros than the regular one
                    r = hdpcRowNonZeros;
                    chosenRow = hdpcRow;
                }
                else {
                    r = regularRowNonZeros;
                    if (r != 2) { // easy scenario where we already have the chosen row
                        chosenRow = regularRow;
                    }
                    else { // tricky scenario where we have to choose the row using the graph

                        // DEBUG
                        debugStartTimer();

                        /*
                         * create graph
                         */

                        // conservative initial capacities to remove re-hashes
                        final Map<Integer, int[]> edges = new HashMap<>(M - i + 1, 1.0f);
                        final Map<Integer, Set<Integer>> nodes = new HashMap<>(L - u - i + 1, 1.0f);
                        final Set<Integer> nodesToVisit = new HashSet<>(L - u - i + 1, 1.0f); // for later search

                        for (int edge = i; edge < M; edge++) {
                            if (isHDPCRow(rowInfos[edge])) continue; // shortcut

                            // count the number of nonzeros in V inside A
                            if (A.nonZerosInRow(edge, i, L - u) != 2) continue; // not an edge

                            final int[] nodesInThisEdge = A.nonZeroPositionsInRow(edge, i, L - u);
                            edges.put(edge, nodesInThisEdge);

                            for (int node : nodesInThisEdge) {
                                Set<Integer> edgesInThisNode = nodes.get(node);

                                // if we don't have this node yet
                                if (edgesInThisNode == null) {
                                    edgesInThisNode = new HashSet<>();
                                    nodes.put(node, edgesInThisNode);
                                    nodesToVisit.add(node); // add node for later traversal
                                }

                                edgesInThisNode.add(edge);
                            }
                        }

                        /*
                         * the graph is complete, now we must
                         * find the maximum size component
                         */

                        // size of the largest component so far
                        int maximumComponentSize = 1;

                        // any edge contained in largest component
                        int edgeInMaximumComponent = regularRow;

                        // used repeatedly throughout the BFSes
                        final Queue<Integer> queue = new ArrayDeque<>();
                        final Set<Integer> visited = new HashSet<>();

                        // perform multiple BFS to find the connected components
                        while (nodesToVisit.size() > 1) {
                            final int start = nodesToVisit.iterator().next(); // any node
                            nodesToVisit.remove(start);

                            visited.clear();
                            queue.clear();

                            visited.add(start);
                            queue.add(start);

                            while (!queue.isEmpty()) {
                                final int node = queue.poll();
                                for (int edge : nodes.get(node)) {
                                    final int[] nodesInEdge = edges.get(edge);
                                    final int adj = (nodesInEdge[0] == node) ? nodesInEdge[1] : nodesInEdge[0];
                                    if (!visited.contains(adj)) {
                                        visited.add(adj);
                                        queue.add(adj);

                                        nodesToVisit.remove(adj); // don't start another BFS with repeated nodes
                                    }
                                }
                            }

                            if (visited.size() > maximumComponentSize) {
                                maximumComponentSize = visited.size();
                                edgeInMaximumComponent = nodes.get(start).iterator().next(); // any edge
                            }
                        }

                        chosenRow = edgeInMaximumComponent;

                        // DEBUG
                        debugEndTimer();
                        chooseRowUsingGraphNanos += debugEllapsedNanos();
                    }
                }
            }

            /*
             * a row has been chosen! -- 'chosenRow'
             */

            /*
             * "After the row is chosen in this step, the first row of A that intersects V is exchanged
             * with the chosen row so that the chosen row is the first row that intersects V."
             */

            // if the chosen row is not 'i' already
            if (chosenRow != i) {
                // DEBUG
                debugStartTimer();

                // swap in A
                A.swapRows(i, chosenRow);

                // swap in X
                X.swapRows(i, chosenRow);

                // decoding process - swap in d
                ArrayUtils.swapInts(d, i, chosenRow);

                // also swap in row information table
                ArrayUtils.swapLongs(rowInfos, i, chosenRow);

                // DEBUG
                debugEndTimer();
                swapRowsNanos += debugEllapsedNanos();
            }

            /*
             * "The columns of A among those that intersect V are reordered so that one of the r nonzeros
             * in the chosen row appears in the first column of V and so that the remaining r-1 nonzeros
             * appear in the last columns of V."
             */

            // DEBUG
            debugStartTimer();

            // an array with the positions (column indices) of the non-zeros
            final int[] nonZeroPos = A.nonZeroPositionsInRow(i, i, L - u);

            /*
             * lets start swapping columns!
             */

            // is the first column in V already the place of a non-zero?
            final int firstNZpos = nonZeroPos[0]; // the chosen row always has at least one non-zero
            if (i != firstNZpos) {
                // no, so swap the first column in V (i) with the first non-zero column

                // swap columns
                A.swapColumns(i, firstNZpos);
                X.swapColumns(i, firstNZpos);

                // decoding process - swap in c
                ArrayUtils.swapInts(c, i, firstNZpos);
            }

            // swap the remaining non-zeros' columns so that they're the last columns in V
            for (int nzp = nonZeroPos.length - 1, currCol = L - u - 1; nzp > 0; nzp--, currCol--) {
                // is the current column already the place of a non-zero?
                final int currNZpos = nonZeroPos[nzp];
                if (currCol != currNZpos) {
                    // no, so swap the current column in V with the current non-zero column

                    // swap columns
                    A.swapColumns(currCol, currNZpos);
                    X.swapColumns(currCol, currNZpos);

                    // decoding process - swap in c
                    ArrayUtils.swapInts(c, currCol, currNZpos);
                }
            }

            // DEBUG
            debugEndTimer();
            swapColumnsNanos += debugEllapsedNanos();

            /*
             * "... if a row below the chosen row has entry beta in the first column of V, and the chosen
             * row has entry alpha in the first column of V, then beta/alpha multiplied by the chosen
             * row is added to this row to leave a zero value in the first column of V."
             */

            // DEBUG
            debugStartTimer();

            // "the chosen row has entry alpha in the first column of V"
            final byte alpha = A.get(i, i);

            // let's look at all rows below the chosen one
            for (int row = i + 1; row < M; row++)
            // Page35@RFC6330 1st Par.
            {
                // "if a row below the chosen row has entry beta in the first column of V"
                final byte beta = A.get(row, i);

                // if it's already 0, no problem
                if (beta == 0) {
                    continue;
                }
                // if it's a non-zero we've got to "zerofy" it
                else
                {
                    /*
                     * "then beta/alpha multiplied by the chosen row is added to this row"
                     */

                    // division
                    byte betaOverAlpha = OctetOps.aDividedByB(beta, alpha);

                    // multiplication and addition
                    A.addRowsInPlace(betaOverAlpha, i, row);

                    // decoding process - D[d[row]] + (betaOverAlpha * D[d[i]])
                    OctetOps.vectorVectorAddition(betaOverAlpha, D[d[i]], D[d[row]], D[d[row]]);

                    // ISDCodeWriter.instance().writePhase1Code(betaOverAlpha, d[i], d[row]); // DEBUG
                }
            }

            // DEBUG
            debugEndTimer();
            addMultiplyNanos += debugEllapsedNanos();

            /*
             * "Finally, i is incremented by 1 and u is incremented by r-1, which completes the step."
             */
            i++;
            u += r - 1;
        }

        // DEBUG
        debugPrintNewLine();
        debugPrintLine("1st:");
        debugPrintMillis("  init", initNanos);
        debugPrintMillis("  find r", findRNanos);
        debugPrintMillis("  choose row with graph", chooseRowUsingGraphNanos);
        debugPrintMillis("  swap rows", swapRowsNanos);
        debugPrintMillis("  swap columns", swapColumnsNanos);
        debugPrintMillis("  add/mult row", addMultiplyNanos);

        return pidPhase2(A, X, D, d, c, L, M, i, u);
    }

    private static byte[][] pidPhase2(
        final ByteMatrix A,
        final ByteMatrix X,
        final byte[][] D,
        final int[] d,
        final int[] c,
        final int L,
        final int M,
        final int i,
        final int u)
        throws SingularMatrixException
    {

        // DEBUG
        debugStartTimer();

        /*
         * "At this point, all the entries of X outside the first i rows and i columns are discarded, so that X
         * has lower triangular form. The last i rows and columns of X are discarded, so that X now has i
         * rows and i columns."
         */

        // ISDCodeWriter.instance().writePhase2Code(A, i, M, L - u, L, d); // DEBUG MUST be called before decoding code!

        /*
         * "Gaussian elimination is performed in the second phase on U_lower either to determine that its rank is
         * less than u (decoding failure) or to convert it into a matrix where the first u rows is the identity
         * matrix (success of the second phase)."
         */

        // reduce U_lower to row echelon form
        MatrixUtilities.reduceToRowEchelonForm(A, i, M, L - u, L, d, D);

        // check U_lower's rank, if it's less than 'u' we've got a decoding failure
        if (MatrixUtilities.nonZeroRows(A, i, M, i, L) < u) {
            throw new SingularMatrixException("@ Phase 2: U_lower's rank is less than u");
        }

        /*
         * "After this phase, A has L rows and L columns."
         */

        // DEBUG
        debugEndTimer();
        debugPrintMillis("2nd", debugEllapsedNanos());

        return pidPhase3(A, X, D, d, c, L, i);
    }

    private static byte[][] pidPhase3(
        ByteMatrix A,
        final ByteMatrix X,
        final byte[][] D,
        final int[] d,
        final int[] c,
        final int L,
        final int i)
    {

        // DEBUG
        debugStartTimer();

        final ByteMatrix oldA = A;
        final int Arows = i;
        final int Acols = L;
        final int Xrows = Arows;
        final int Xcols = Arows;

        // multiply in parallel
        SilentFuture<ByteMatrix> lateA = Parallelism.submitTask(new Callable<ByteMatrix>() {

            @Override
            public ByteMatrix call() {

                /*
                 * "... the matrix X is multiplied with the submatrix of A consisting of the first i rows of A."
                 */
                return X.multiply(oldA, 0, Xrows, 0, Xcols, 0, Arows, 0, Acols);
            }
        });

        // decoding process
        final int Drows = Xrows;
        final int Dcols = (D.length == 0) ? 0 : D[0].length;
        final byte[][] DShallowCopy = Arrays.copyOf(D, D.length);
        final ByteMatrix DM = new RowIndirected2DByteMatrix(Drows, Dcols, DShallowCopy, d);
        for (int row = 0; row < Xrows; row++) {
            // multiply X[row] by D
            BasicByteVector prod = (BasicByteVector)X.multiplyRow(row, DM, 0, Xcols, LinearAlgebra.BASIC2D_FACTORY);
            D[d[row]] = prod.getInternalArray();
        }

        // ISDCodeWriter.instance().writePhase3Code(X, Xrows, Xcols, d); // DEBUG

        // A can be safely re-assigned because the product matrix has the same dimensions of A
        A = lateA.get();

        // DEBUG
        debugEndTimer();
        debugPrintMillis("3rd", debugEllapsedNanos());

        return pidPhase4(A, D, d, c, L, i);
    }

    private static byte[][] pidPhase4(
        final ByteMatrix A,
        final byte[][] D,
        final int[] d,
        final int[] c,
        final int L,
        final int i)
    {

        // DEBUG
        debugStartTimer();

        /*
         * "For each of the first i rows of U_upper, do the following: if the row has a nonzero entry at position j,
         * and if the value of that nonzero entry is b, then add to this row b times row j of I_u."
         */

        // "For each of the first i rows of U_upper"
        for (int row = 0; row < i; row++) {
            ByteVectorIterator it = A.nonZeroRowIterator(row, i, L);
            while (it.hasNext()) {
                it.next();

                // "if the row has a nonzero entry at position j"
                final int j = it.index();
                // "if the value of that nonzero entry is b"
                final byte b = it.get();

                // "add to this row b times row j of I_u" -- this would "zerofy"
                // that position, thus we can save the complexity
                // (no need to actually "zerofy" it, since this part of the matrix will not be used again)
                // it.set((byte)0);

                // ISDCodeWriter.instance().writePhase4Code(b, d[j], d[row]); // DEBUG

                // decoding process - (beta * D[d[j]]) + D[d[row]]
                OctetOps.vectorVectorAddition(b, D[d[j]], D[d[row]], D[d[row]]);
            }
        }

        // DEBUG
        debugEndTimer();
        debugPrintMillis("4th", debugEllapsedNanos());

        return pidPhase5(A, D, d, c, L, i);
    }

    private static byte[][] pidPhase5(
        final ByteMatrix A,
        final byte[][] D,
        final int[] d,
        final int[] c,
        final int L,
        final int i)
    {

        // DEBUG
        debugStartTimer();

        // "For j from 1 to i, perform the following operations:"
        for (int j = 0; j < i; j++) {
            // "If A[j,j] is not one"
            byte beta = A.get(j, j);
            if (beta != 1) {
                // "then divide row j of A by A[j,j]."
                A.divideRowInPlace(j, beta);

                // ISDCodeWriter.instance().writePhase5Code_1(beta, d[j]); // DEBUG

                // decoding process - D[d[j]] / beta
                OctetOps.valueVectorDivision(beta, D[d[j]], D[d[j]]); // in place division
            }

            // "For eL from 1 to j-1"
            ByteVectorIterator it = A.nonZeroRowIterator(j, 0, j);
            while (it.hasNext()) {
                it.next();

                // "then add A[j,eL] multiplied with row eL of A to row j of A."
                final int eL = it.index();
                beta = it.get();

                // We do not actually have to perform this operation on the matrix A
                // because it will not be used again.
                // A.addRowsInPlace(beta, eL, j);

                // ISDCodeWriter.instance().writePhase5Code_2(beta, d[eL], d[j]); // DEBUG

                // decoding process - (beta * D[d[eL]]) + D[d[j]]
                OctetOps.vectorVectorAddition(beta, D[d[eL]], D[d[j]], D[d[j]]);
            }
        }

        // DEBUG
        debugEndTimer();
        debugPrintMillis("5th", debugEllapsedNanos());

        final byte[][] C = new byte[L][];

        // reorder C
        for (int index = 0; index < L; index++) {
            C[c[index]] = D[d[index]];
        }

        // ISDCodeWriter.instance().writeReorderCode(L, c, d); // DEBUG
        // ISDCodeWriter.instance().generateCode(); // DEBUG

        return C;
    }

    private LinearSystem() {

        // not instantiable
    }
}
