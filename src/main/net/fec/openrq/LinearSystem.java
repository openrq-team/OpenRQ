/*
 * Copyright 2014 Jose Lopes
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.fec.openrq.util.arithmetic.OctetOps;
import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.io.ByteVectorIterator;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrices;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.matrix.functor.MatrixFunction;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.ByteVectors;
import net.fec.openrq.util.printing.TimePrinter;
import net.fec.openrq.util.rq.Rand;
import net.fec.openrq.util.rq.SystematicIndices;


/**
 */
final class LinearSystem {

    private static final Factory DENSE_FACTORY = LinearAlgebra.BASIC2D_FACTORY;
    private static final Factory SPARSE_FACTORY = LinearAlgebra.CRS_FACTORY;
    private static final long A_SPARSE_THRESHOLD = 1L;
    private static final long MT_SPARSE_THRESHOLD = 1L;


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
            MT.set(row, Kprime + S - 1, (byte)OctetOps.getExp(row));
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
                    GAMMA.set(row, col, (byte)OctetOps.getExp((row - col) % 256));
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
    static byte[] enc(int Kprime, byte[][] C, Tuple tuple, int T)
    {

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
            MatrixUtilities.xorSymbolInPlace(result, C[b]);
        }

        while (b1 >= P)
            b1 = (b1 + a1) % P1;

        MatrixUtilities.xorSymbolInPlace(result, C[W + b1]);

        for (long j = 1; j < d1; j++)
        {
            do
                b1 = (b1 + a1) % P1;
            while (b1 >= P);

            MatrixUtilities.xorSymbolInPlace(result, C[W + b1]);
        }

        return result;
    }

    /**
     * Solves the decoding system of linear equations using the permanent inactivation technique
     * 
     * @param A
     *            The constraint matrix
     * @param D
     *            The vector with available symbols (each row of the matrix contains one symbol)
     * @param Kprime
     *            The total number of source symbols for decoding
     * @return the intermediate symbols
     * @throws SingularMatrixException
     */
    static byte[][] PInactivationDecoding(ByteMatrix A, byte[][] D, int Kprime)
        throws SingularMatrixException {

        // decoding parameters
        final int Ki = SystematicIndices.getKIndex(Kprime);
        final int S = SystematicIndices.S(Ki);
        final int H = SystematicIndices.H(Ki);
        final int W = SystematicIndices.W(Ki);
        final int L = Kprime + S + H;
        final int P = L - W;
        final int M = A.rows();

        // DEBUG
        // PRINTER.println(printVarDeclar(int.class, "Kprime", String.valueOf(Kprime)));

        /*
         * initialize c and d vectors
         */
        int[] c = new int[L];
        int[] d = new int[M];

        for (int i = 0; i < L; i++)
        {
            c[i] = i;
            d[i] = i;
        }

        for (int i = L; i < M; i++)
        {
            d[i] = i;
        }

        // TODO see if X can be deleted (this requires saving some modifications on A (during 1st phase))
        // allocate X and copy A into X
        ByteMatrix X = A.copy();

        // initialize i and u parameters, for the submatrices sizes
        int i = 0, u = P;

        /*
         * DECODING
         */

        final PrintStream timerPrintable = System.out; // DEBUG
        // final QuietPrintableAppendable timerPrintable = QuietPrintableAppendable.ofNull(); // DEBUG

        /*
         * First phase
         */

        timerPrintable.println(); // DEBUG
        TimePrinter.beginTimer(); // DEBUG

        // counts how many rows have been chosen already
        int chosenRowsCounter = 0;

        // the number of rows that are not HDPC
        // (these should be chosen first)
        int nonHDPCRows = S + Kprime;

        /*
         * TODO Optimization: Instead of traversing this every time, we could find just the lines that lost a non-zero,
         * and then decrement the original 'r' (and degree as well). How to deal with the dimensions of V?
         */

        // maps the index of a row to an object Row (which stores that row's characteristics)
        final Map<Integer, Row> rows = new HashMap<>(M + 1, 1.0f);
        for (int row = 0; row < M; row++) {
            // retrieve the number of non-zeros in the row
            final int nonZeros = A.nonZerosInRow(row, 0, L - u); // exclude last u columns
            // is this a HDPC row?
            final boolean isHDPC = (row >= S && row < S + H);

            // this is an optimization
            if (nonZeros == 2 && !isHDPC) {
                int originalDegree = 0;
                final Set<Integer> nodes = new HashSet<>(2 + 1, 1.0f); // we already know there are only 2 non zeros

                ByteVectorIterator it = A.nonZeroRowIterator(row, 0, L - u);
                while (it.hasNext()) {
                    it.next();
                    originalDegree += OctetOps.UNSIGN(it.get()); // add to the degree of this row
                    nodes.add(it.index()); // add the column index to the nodes
                }

                rows.put(row, new Row(row, nonZeros, originalDegree, isHDPC, nodes));
            }
            else {
                int originalDegree = 0;

                ByteVectorIterator it = A.nonZeroRowIterator(row, 0, L - u);
                while (it.hasNext()) {
                    it.next();
                    originalDegree += OctetOps.UNSIGN(it.get()); // add to the degree of this row
                }

                rows.put(row, new Row(row, nonZeros, originalDegree, isHDPC));
            }
        }

        TimePrinter.markTimestamp(); // DEBUG
        TimePrinter.printlnEllapsedTime(timerPrintable, "1st(a): ", TimeUnit.MILLISECONDS); // DEBUG

        // at most L steps
        while (i + u != L)
        {
            // the degree of the 'currently chosen' row
            int minDegree = 256 * L;

            // number of non-zeros in the 'currently chosen' row
            int r = L + 1;

            // currently chosen row
            Row chosenRow = null;

            // decoding failure?
            boolean allZeros = true;

            // there is a row with exactly two ones
            boolean two1s = false;

            /*
             * find r
             */

            TimePrinter.beginTimer(); // DEBUG

            for (Row row : rows.values()) {
                if (row.nonZeros != 0) allZeros = false;
                if (row.isHDPC && chosenRowsCounter < nonHDPCRows) continue;

                // if it's an edge, then it must have exactly two 1's
                if (row.nodes != null) two1s = true;

                if (row.nonZeros < r && row.nonZeros > 0) {
                    chosenRow = row;
                    r = chosenRow.nonZeros;
                    minDegree = chosenRow.originalDegree;
                }
                else if (row.nonZeros == r && row.originalDegree < minDegree) {
                    chosenRow = row;
                    minDegree = chosenRow.originalDegree;
                }
            }

            TimePrinter.markTimestamp(); // DEBUG
            TimePrinter.printlnEllapsedTime(timerPrintable, "1st(iter " + (i + u) + "; find r)", TimeUnit.MILLISECONDS); // DEBUG

            if (allZeros) {// DECODING FAILURE
                throw new SingularMatrixException(
                    "Decoding Failure - PI Decoding @ Phase 1: All entries in V are zero.");
            }

            /*
             * choose the row
             */

            TimePrinter.beginTimer(); // DEBUG

            if (r == 2 && two1s) {

                /*
                 * create graph
                 */

                // allocate memory
                Map<Integer, Set<Integer>> graph = new HashMap<>(L - u - i + 1, 1.0f);

                // lets go through all the rows... (yet again!)
                for (Row row : rows.values())
                {
                    // is this row an edge?
                    if (row.nodes != null)
                    {
                        // get the nodes connected through this edge
                        Integer[] edge = row.nodes.toArray(new Integer[2]);
                        int node1 = edge[0];
                        int node2 = edge[1];

                        // node1 already in graph?
                        if (graph.keySet().contains(node1))
                        { // it is

                            // then lets add node 2 to its neighbours
                            graph.get(node1).add(node2);
                        }
                        else
                        { // it isn't

                            // allocate memory for its neighbours
                            Set<Integer> edges = new HashSet<>(L - u - i + 1, 1.0f);

                            // add node 2 to its neighbours
                            edges.add(node2);

                            // finally, add node 1 to the graph along with its neighbours
                            graph.put(node1, edges);
                        }

                        // node2 already in graph?
                        if (graph.keySet().contains(node2))
                        { // it is

                            // then lets add node 1 to its neighbours
                            graph.get(node2).add(node1);
                        }
                        else
                        { // it isn't

                            // allocate memory for its neighbours
                            Set<Integer> edges = new HashSet<>(L - u - i + 1, 1.0f);

                            // add node 1 to its neighbours
                            edges.add(node1);

                            // finally, add node 2 to the graph along with its neighbours
                            graph.put(node2, edges);
                        }
                    }
                    else continue;
                }

                /*
                 * the graph is complete, now we must
                 * find the maximum size component
                 */

                // set of visited nodes
                Set<Integer> visited = null;

                /*
                 * TODO Optimization: I already searched, and there are optimized algorithms to find connected
                 * components. Then we just find and use the best one available...
                 */

                // what is the size of the largest component we've already found
                int maximumSize = 0;

                // the maximum size component
                Set<Integer> greatestComponent = null;

                // which nodes have already been used (either in visited or in toVisit)
                Set<Integer> used = new HashSet<>(L - u - i + 1, 1.0f);

                // iterates the nodes in the graph
                Iterator<Map.Entry<Integer, Set<Integer>>> it = graph.entrySet().iterator();

                // let's iterate through the nodes in the graph, looking for the maximum
                // size component. we will be doing a breadth first search // TODO optimize this with a better
                // algorithm?
                while (it.hasNext())
                {
                    // get our initial node
                    Map.Entry<Integer, Set<Integer>> node = it.next();
                    int initialNode = node.getKey();

                    // we can't have used it before!
                    if (used.contains(initialNode)) continue;

                    // what are the edges of our initial node?
                    Integer[] edges = node.getValue().toArray(new Integer[node.getValue().size()]);

                    // allocate memory for the set of visited nodes
                    visited = new HashSet<>(L - u - i + 1, 1.0f);

                    // the set of nodes we must still visit
                    List<Integer> toVisit = new LinkedList<>();

                    // add the initial node to the set of used and visited nodes
                    visited.add(initialNode);
                    used.add(initialNode);

                    // add my edges to the set of nodes we must visit
                    // and also put them in the used set
                    for (Integer edge : edges)
                    {
                        toVisit.add(edge);
                        used.add(edge);
                    }

                    // start the search!
                    while (toVisit.size() != 0)
                    {
                        // the node we are visiting
                        int no = toVisit.remove(0);

                        // add node to visited set
                        visited.add(no);

                        // queue edges to be visited (if they haven't been already
                        for (Integer edge : graph.get(no))
                            if (!visited.contains(edge)) toVisit.add(edge);
                    }

                    // is the number of visited nodes, greater than the 'currently' largest component?
                    if (visited.size() > maximumSize)
                    { // it is! we've found a greater component then...

                        // update the maximum size
                        maximumSize = visited.size();

                        // update our greatest component
                        greatestComponent = visited;
                    }
                    else continue;
                }

                /*
                 * we've found the maximum size connected component -- 'greatestComponent'
                 */

                // let's choose the row
                for (Row row : rows.values())
                {
                    // is it a node in the graph?
                    if (row.nodes != null)
                    { // it is

                        // get the nodes connected through this edge
                        Integer[] edge = row.nodes.toArray(new Integer[2]);
                        int node1 = edge[0];
                        int node2 = edge[1];

                        // is this row an edge in the maximum size component?
                        if (greatestComponent.contains(node1) && greatestComponent.contains(node2))
                        {
                            chosenRow = row;
                            break;
                        }
                        else continue;
                    }
                    else continue;
                }

                chosenRowsCounter++;

                TimePrinter.markTimestamp(); // DEBUG
                TimePrinter.printlnEllapsedTime(timerPrintable, "1st(iter " + (i + u) + "; choose row)",
                    TimeUnit.MILLISECONDS); // DEBUG
            }
            else {

                // already chosen (in 'find r')
                chosenRowsCounter++;
            }

            /*
             * a row has been chosen! -- 'chosenRow'
             */

            /*
             * "After the row is chosen in this step, the first row of A that intersects V is exchanged
             * with the chosen row so that the chosen row is the first row that intersects V."
             */

            final int chosenRowPos = chosenRow.position;

            // if the chosen row is not 'i' already
            if (chosenRowPos != i) {
                TimePrinter.beginTimer(); // DEBUG

                // swap in A
                A.swapRows(i, chosenRowPos);

                // swap in X
                X.swapRows(i, chosenRowPos);

                // decoding process - swap in d
                ArrayUtils.swapInts(d, i, chosenRowPos);

                // update values in 'rows' map
                Row other = rows.remove(i);
                rows.put(chosenRowPos, other);
                other.position = chosenRowPos;
                chosenRow.position = i;

                TimePrinter.markTimestamp(); // DEBUG
                TimePrinter.printlnEllapsedTime(timerPrintable, "1st(iter " + (i + u) + "; swap rows)",
                    TimeUnit.MILLISECONDS); // DEBUG
            }

            /*
             * "The columns of A among those that intersect V are reordered so that one of the r nonzeros
             * in the chosen row appears in the first column of V and so that the remaining r-1 nonzeros
             * appear in the last columns of V."
             */

            TimePrinter.beginTimer(); // DEBUG

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

            TimePrinter.markTimestamp(); // DEBUG
            TimePrinter.printlnEllapsedTime(timerPrintable, "1st(iter " + (i + u) + "; swap columns)",
                TimeUnit.MILLISECONDS); // DEBUG

            /*
             * "... if a row below the chosen row has entry beta in the first column of V, and the chosen
             * row has entry alpha in the first column of V, then beta/alpha multiplied by the chosen
             * row is added to this row to leave a zero value in the first column of V."
             */

            TimePrinter.beginTimer(); // DEBUG

            // "the chosen row has entry alpha in the first column of V"
            final byte alpha = A.get(i, i);
            final ByteVector rowVector = A.getRow(i); // getRow is fast when the matrix is row compressed

            byte prevBeta = alpha;
            byte beta;

            // let's look at all rows below the chosen one
            for (int row = i + 1; row < M; row++)
            // Page35@RFC6330 1st Par.
            {
                // "if a row below the chosen row has entry beta in the first column of V"
                beta = A.get(row, i);

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

                    /*
                     * Usually, we would copy the row i to a vector, every iteration, and update
                     * the row vector by multiplying it by beta/alpha.
                     * 
                     * What we do instead is avoid the repeated copy and reuse the same row vector.
                     * To do so, we update the row vector by multiplying it by beta/(previous beta),
                     * where previous beta is equal to alpha at the first iteration.
                     */

                    // division
                    byte betaOverPrevBeta = OctetOps.aDividedByB(beta, prevBeta);
                    prevBeta = beta; // update the previous beta for the next iteration

                    // multiplication
                    if (betaOverPrevBeta != 1) {
                        rowVector.updateNonZero(ByteVectors.asMulFunction(betaOverPrevBeta), i, rowVector.length());
                    }

                    // addition
                    final ByteVectorIterator it = rowVector.nonZeroIterator(i, rowVector.length());
                    final MatrixFunction addition = new MatrixFunction() {

                        @Override
                        public byte evaluate(int i, int j, byte value) {

                            return OctetOps.aPlusB(value, it.get());
                        }
                    };
                    while (it.hasNext()) {
                        it.next();
                        A.update(row, it.index(), addition);
                    }

                    // decoding process - (beta * D[d[i]]) + D[d[row]]
                    byte[] p = OctetOps.betaProduct(betaOverPrevBeta, D[d[i]]);
                    MatrixUtilities.xorSymbolInPlace(D[d[row]], p);
                    // DEBUG
                    // PRINTER.println(
                    // printVarDeclar(byte[].class, "p",
                    // "betaProduct((byte)" + balpha + ",D[" + d[i] + "])"));
                    // PRINTER.println(
                    // "xorSymbolInPlace(D[" + d[row] + "],p);");
                }
            }

            TimePrinter.markTimestamp(); // DEBUG
            TimePrinter.printlnEllapsedTime(timerPrintable, "1st(iter " + (i + u) + "; row add/mult)",
                TimeUnit.MILLISECONDS); // DEBUG

            /*
             * "Finally, i is incremented by 1 and u is incremented by r-1, which completes the step."
             */
            i++;
            u += r - 1;

            TimePrinter.beginTimer(); // DEBUG

            // update nonZeros
            for (Row row : rows.values()) {
                // update the non zero count
                row.nonZeros = A.nonZerosInRow(row.position, i, L - u);

                if (row.nonZeros != 2 || row.isHDPC) {
                    row.nodes = null;
                }
                else {
                    final Set<Integer> nodes = new HashSet<>(2 + 1, 1.0f); // we know there will only be two non zeros
                    ByteVectorIterator it = A.nonZeroRowIterator(row.position, i, L - u);
                    while (it.hasNext()) {
                        it.next();
                        nodes.add(it.index()); // add node to this edge (column index)
                    }

                    row.nodes = nodes;
                }
            }

            TimePrinter.markTimestamp(); // DEBUG
            TimePrinter.printlnEllapsedTime(timerPrintable, "1st(iter " + (i + u) + "; update non zeros)",
                TimeUnit.MILLISECONDS); // DEBUG
        }

        // END OF FIRST PHASE

        /*
         * Second phase
         */

        TimePrinter.beginTimer(); // DEBUG

        /*
         * "At this point, all the entries of X outside the first i rows and i columns are discarded, so that X
         * has lower triangular form. The last i rows and columns of X are discarded, so that X now has i
         * rows and i columns."
         */

        /*
         * "Gaussian elimination is performed in the second phase on U_lower either to determine that its rank is
         * less than u (decoding failure) or to convert it into a matrix where the first u rows is the identity
         * matrix (success of the second phase)."
         */

        // reduce U_lower to row echelon form
        MatrixUtilities.reduceToRowEchelonForm(A, i, M, L - u, L, d, D);

        // check U_lower's rank, if it's less than 'u' we've got a decoding failure
        if (MatrixUtilities.nonZeroRows(A, i, M, i, L) < u) {
            throw new SingularMatrixException(
                "Decoding Failure - PI Decoding @ Phase 2: U_lower's rank is less than u.");
        }

        /*
         * "After this phase, A has L rows and L columns."
         */

        TimePrinter.markTimestamp(); // DEBUG
        TimePrinter.printlnEllapsedTime(timerPrintable, "2nd: ", TimeUnit.MILLISECONDS); // DEBUG

        // END OF SECOND PHASE

        /*
         * Third phase
         */

        TimePrinter.beginTimer(); // DEBUG

        // decoding process
        byte[][] noOverheadD = new byte[L][];
        // DEBUG
        // PRINTER.println(
        // printVarDeclar(byte[][].class, "E", "new byte[" + L + "][]"));

        // create a copy of D
        for (int index = 0; index < L; index++) {
            noOverheadD[index] = D[d[index]];
            // DEBUG
            // PRINTER.println(
            // "E[" + index + "]=" + "D[" + d[index] + "];");
        }

        for (int row = 0; row < i; row++) {
            // multiply X[row] by D
            D[d[row]] = MatrixUtilities.multiplyByteLineBySymbolVector(X.getRow(row), i, noOverheadD);
            // DEBUG
            // PRINTER.println(
            // "D[" + d[row] + "]=multiplyByteLineBySymbolVector(" +
            // printByteArray(X[row]) + "," + i + "," + "E);");
        }

        /*
         * "... the matrix X is multiplied with the submatrix of A consisting of the first i rows of A."
         */

        // This multiplies X by A and stores the product in X (this destroys X)
        // Utilities.multiplyMatricesHack(X, 0, i, 0, i, A, 0, i, 0, L, X, 0, i, 0, L);

        // A can be safely re-assigned because the product matrix has the same dimensions of A
        A = MatrixUtilities.multiplyMatrices(X, 0, i, 0, i, A, 0, i, 0, L);

        // copy the product X to A
        // for (int row = 0; row < i; row++)
        // A[row] = XA[row];

        TimePrinter.markTimestamp(); // DEBUG
        TimePrinter.printlnEllapsedTime(timerPrintable, "3rd: ", TimeUnit.MILLISECONDS); // DEBUG

        /*
         * Fourth phase
         */

        TimePrinter.beginTimer(); // DEBUG

        /*
         * "For each of the first i rows of U_upper, do the following: if the row has a nonzero entry at position j,
         * and if the value of that nonzero entry is b, then add to this row b times row j of I_u."
         */

        // "For each of the first i rows of U_upper"
        for (int row = 0; row < i; row++)
        {
            for (int j = i; j < L; j++)
            {
                // "if the row has a nonzero entry at position j"
                // "if the value of that nonzero entry is b"
                byte b = A.get(row, j);
                if (b != 0)
                {
                    // "add to this row b times row j" -- this would "zerofy" that position, thus we can save the
                    // complexity
                    A.set(row, j, (byte)0);

                    // decoding process - (beta * D[d[j]]) + D[d[row]]
                    byte[] product = OctetOps.betaProduct(b, D[d[j]]);
                    MatrixUtilities.xorSymbolInPlace(D[d[row]], product);
                    // DEBUG
                    // PRINTER.println(
                    // printVarDeclar(byte[].class, "p",
                    // "betaProduct((byte)" + b + ",D[" + d[j] + "])"));
                    // PRINTER.println(
                    // "xorSymbolInPlace(D[" + d[row] + "],p);");
                }
            }
        }

        TimePrinter.markTimestamp(); // DEBUG
        TimePrinter.printlnEllapsedTime(timerPrintable, "4th: ", TimeUnit.MILLISECONDS); // DEBUG

        /*
         * Fifth phase
         */

        TimePrinter.beginTimer(); // DEBUG

        // "For j from 1 to i, perform the following operations:"
        for (int j = 0; j < i; j++)
        {
            {
                // "If A[j,j] is not one"
                byte beta = A.get(j, j);
                if (beta != 1)
                {
                    // "then divide row j of A by A[j,j]."
                    A.updateRow(j, ByteMatrices.asDivFunction(beta));

                    // decoding process - D[d[j]] / beta
                    OctetOps.betaDivision(beta, D[d[j]], D[d[j]]); // in place division
                    // DEBUG
                    // PRINTER.println(
                    // "betaDivision((byte)" + beta + ",D[" + d[j] + "],D[" + d[j] + "]);");
                }
            }

            // "For eL from 1 to j-1"
            for (int eL = 0; eL < j; eL++) {

                // "if A[j,l] is nonzero"
                byte beta = A.get(j, eL);
                if (beta != 0)
                { // "then add A[j,l] multiplied with row l of A to row j of A."

                    // multiply A[j][l] by row 'l' of A
                    final ByteVector product = A.getRow(eL);
                    product.updateNonZero(ByteVectors.asMulFunction(beta));

                    // add the product to row 'j' of A
                    A.updateRow(j, new MatrixFunction() {

                        @Override
                        public byte evaluate(int notUsed, int j, byte value) {

                            return OctetOps.aPlusB(product.get(j), value);
                        }
                    });

                    // decoding process - D[d[j]] + (A[j][l] * D[d[l]])
                    byte[] p = OctetOps.betaProduct(beta, D[d[eL]]);
                    MatrixUtilities.xorSymbolInPlace(D[d[j]], p);
                    // DEBUG
                    // PRINTER.println(
                    // printVarDeclar(byte[].class, "p",
                    // "betaProduct((byte)" + beta + ",D[" + d[l] + "])"));
                    // PRINTER.println(
                    // "xorSymbolInPlace(D[" + d[j] + "],p);");
                }
            }
        }

        TimePrinter.markTimestamp(); // DEBUG
        TimePrinter.printlnEllapsedTime(timerPrintable, "5th: ", TimeUnit.MILLISECONDS); // DEBUG

        // use the already allocated matrix for the matrix C
        final byte[][] C = noOverheadD;

        // reorder C
        for (int index = 0; index < L; index++) {
            final int ci = c[index];
            final int di = d[index];
            C[ci] = D[di];
            // DEBUG
            // PRINTER.println(
            // "E[" + ci + "]=D[" + di + "];");
        }

        // DEBUG
        // PRINTER.println("return E;");
        return C;

        // allocate memory for the decoded symbols
        // byte[] C = new byte[L*symbol_size];

        // copy the decoded source symbols from D to C
        // for(int symbol = 0; symbol < L; symbol++)
        // System.arraycopy(D[d[symbol]], 0, C, c[symbol]*symbol_size, symbol_size);

        // return the decoded source symbols
        // return C;
    }

    private LinearSystem() {

        // not instantiable
    }
}
