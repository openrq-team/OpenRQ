/* 
 * Copyright 2014 Jose Lopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fec.openrq.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import net.fec.openrq.core.util.rq.OctectOps;
import net.fec.openrq.core.util.rq.Rand;
import net.fec.openrq.core.util.rq.Utilities;
import net.fec.openrq.core.util.rq.SingularMatrixException;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class LinearSystem {

	/**
	 * Initializes the G_LDPC1 submatrix.
	 * @param constraint_matrix
	 * @param B
	 * @param S
	 */
	private static void initializeG_LPDC1(byte[][] constraint_matrix, int B, int S)
	{
		int circulant_matrix = -1;
		
		for(int col = 0; col < B; col++)
		{
			int circulant_matrix_column = col % S;

			if(circulant_matrix_column != 0)
			{ 
				// cyclic down-shift	
				constraint_matrix[0][col] = constraint_matrix[S-1][col-1];

				for(int row = 1; row < S; row++)
				{
					constraint_matrix[row][col] = constraint_matrix[row-1][col-1];
				}
			}
			else
			{ 	// if 0, then its the first column of the current circulant matrix

				circulant_matrix++;

				// 0
				constraint_matrix[0][col] = 1;

				// (i + 1) mod S
				constraint_matrix[(circulant_matrix + 1) % S][col] = 1;

				// (2 * (i + 1)) mod S
				constraint_matrix[(2 * (circulant_matrix + 1)) % S][col] = 1;
			}
		}
	}
	
	/**
	 * Initializes the G_LPDC2 submatrix.
	 * @param constraint_matrix
	 * @param S
	 * @param P
	 * @param W
	 */
	private static void initializeG_LPDC2(byte[][] constraint_matrix, int S, int P, int W){
		
		for(int row = 0; row < S; row++)
		{
			// consecutives 1's modulo P
			constraint_matrix[row][( row    %P) + W] = 1;
			constraint_matrix[row][((row+1) %P) + W] = 1;
		}
	}
	
	/**
	 * Initializes the I_S submatrix.
	 * @param constraint_matrix
	 * @param S
	 * @param B
	 */
	private static void initializeIs(byte[][] constraint_matrix, int S, int B){
	
		for(int row = 0; row < S; row++)
		{
			for(int col = 0; col < S; col++)
			{
				if(col != row)
					continue;
				else
					constraint_matrix[row][col+B] = 1;
			}
		}
	}
	
	/**
	 * Initializes the I_H submatrix.
	 * @param constraint_matrix
	 * @param W
	 * @param U
	 * @param H
	 * @param S
	 */
	private static void initializeIh(byte[][] constraint_matrix, int W, int U, int H, int S)
	{
		int lower_limit_col = W + U;

		for(int row = 0; row < H; row++)
		{
			for(int col = 0; col < H; col++)
			{
				if(col != row)
					continue;
				else
					constraint_matrix[row + S][col + lower_limit_col] = 1;
			}
		}
	}
	
	/**
	 * Generates the MT matrix that is used to generate G_HDPC submatrix.
	 * @param H
	 * @param Kprime
	 * @param S
	 * @return MT
	 */
	private static byte[][] generateMT(int H, int Kprime, int S)
	{
		byte[][] MT = new byte[H][Kprime+S];

		for(int row = 0; row < H; row++)
		{
			for(int col = 0; col < Kprime + S - 1; col++)
			{
				if(row != (int)(Rand.rand(col + 1, 6, H)) && row != (((int)(Rand.rand(col + 1, 6, H)) + (int)(Rand.rand(col + 1, 7, H - 1)) + 1) % H))
					continue;
				else
					MT[row][col] = 1;
			}
		}
		
		for(int row = 0; row < H; row++)
			MT[row][Kprime + S - 1] = OctectOps.getExp(row);
		
		return(MT);
	}
	
	/**
	 * Generates the GAMMA matrix that is used to generate G_HDPC submatrix.
	 * @param Kprime
	 * @param S
	 * @return GAMMA
	 */
	private static byte[][] generateGAMMA(int Kprime, int S)
	{	
		byte[][] GAMMA = new byte[Kprime + S][Kprime + S];

		for(int row = 0; row < Kprime + S; row++)
		{
			for(int col = 0; col<Kprime+S; col++)
			{
				if(row >= col)
					GAMMA[row][col] = OctectOps.getExp((row - col) % 256);
				else
					continue;				
			}	
		}
		
		return GAMMA;
	}
	
	/**
	 * Initializes the G_ENC submatrix.
	 * @param constraint_matrix
	 * @param S
	 * @param H
	 * @param L
	 * @param Kprime
	 */
	private static void initializeG_ENC(byte[][] constraint_matrix, int S, int H, int L, int Kprime)
	{
		for(int row = S + H; row < L; row++)
		{
			Tuple tuple = new Tuple(Kprime, row - S - H);

			Set<Integer> indexes = encIndexes(Kprime, tuple);

			for(Integer j : indexes)
			{
				constraint_matrix[row][j] = 1;	
			}
		}
	}
	
	/**
	 * Generates the constraint matrix.
	 * @param Kprime
	 * @param T
	 * @return
	 */	
	public static byte[][] generateConstraintMatrix(int Kprime, int T)
	{
		// calculate necessary parameters
		int Ki = SystematicIndices.getKIndex(Kprime);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		int L = Kprime + S + H;
		int P = L - W;
		int U = P - H;
		int B = W - S;
		
		// allocate memory for the constraint matrix
		byte[][] constraint_matrix = new byte[L][L]; // A
		
		/*
		 * upper half
		 */
		
		// initialize G_LPDC2
		initializeG_LPDC2(constraint_matrix, S, P, W);

		// initialize G_LPDC1
		initializeG_LPDC1(constraint_matrix, B, S);

		// initialize I_s			
		initializeIs(constraint_matrix, S, B);

		/*
		 *  botton half
		 */
		
		// initialize I_h
		initializeIh(constraint_matrix, W, U, H, S);

		// initialize G_HDPC
		
		// MT
		byte[][] MT = generateMT(H, Kprime, S);

		// GAMMA
		byte[][] GAMMA = generateGAMMA(Kprime, S);

		// G_HDPC = MT * GAMMA
		byte[][] G_HDPC = Utilities.multiplyMatrices(MT, GAMMA);

		// initialize G_HDPC
		for(int row=S; row<S+H; row++)
			for(int col=0; col<W+U; col++)
				constraint_matrix[row][col] = G_HDPC[row-S][col];

		// initialize G_ENC
		initializeG_ENC(constraint_matrix, S, H, L, Kprime);
		
		// return the constraint matrix
		return constraint_matrix;
	}
	
	/**
	  * Returns the indexes of the intermediate symbols that should be XORed to encode
	  *  the symbol for the given tuple.
	  * @param Kprime
	  * @param tuple
	  * @return Set of indexes.
	  */
	 public static Set<Integer> encIndexes(int Kprime, Tuple tuple)
	 {
		 // allocate memory for the indexes
		 Set<Integer> indexes = new TreeSet<Integer>();

		 // parameters
		 int Ki = SystematicIndices.getKIndex(Kprime);
		 int S = SystematicIndices.S(Ki);
		 int H = SystematicIndices.H(Ki);
		 int W = SystematicIndices.W(Ki);
		 long L = Kprime + S + H;
		 long P = L - W;
		 long P1 = Utilities.ceilPrime(P);

		 // tuple parameters
		 long d = tuple.getD();
		 long a = tuple.getA();
		 long b = tuple.getB();
		 long d1 = tuple.getD1();
		 long a1 = tuple.getA1();
		 long b1 = tuple.getB1();

		 /*
		  * simulated encoding -- refer to section 5.3.3.3 of RFC 6330
		  */
		 
		 indexes.add((int) b);

		 for(long j = 0; j < d; j++)
		 {
			 b = (b + a) % W;
			 indexes.add((int) b);
		 }

		 while(b1 >= P)
		 {
			 b1 = (b1 + a1) % P1;
		 }

		 indexes.add((int) (W + b1));

		 for(long j = 1; j < d1; j++)
		 {
			 do
				 b1 = (b1 + a1) % P1;
			 while(b1 >= P);

			 indexes.add((int) (W + b1));
		 }

		 return indexes;
	 }
	 
	 /**
	  * Encodes a source symbol.
	  * @param Kprime
	  * @param C
	  * @param tuple
	  * @return
	  */
	 public static byte[] enc(int Kprime, byte[] C, Tuple tuple, int T)
	 {
		 // necessary parameters
		 int Ki  = SystematicIndices.getKIndex(Kprime);
		 int S   = SystematicIndices.S(Ki);
		 int H   = SystematicIndices.H(Ki);
		 int W   = SystematicIndices.W(Ki);
		 long L  = Kprime + S + H;
		 long P  = L - W;
		 int P1  = (int)Utilities.ceilPrime(P);
		 long d  = tuple.getD();
		 int a   = (int)tuple.getA();
		 int b   = (int)tuple.getB();
		 long d1 = tuple.getD1();
		 int a1  = (int)tuple.getA1();
		 int b1  = (int)tuple.getB1();

		 // allocate memory and initialize the encoding symbol
		 byte[] result = Arrays.copyOfRange(C, (int)(b*T), (int)((b+1)*T));

		 /*
		  * encoding -- refer to section 5.3.5.3 of RFC 6330
		  */
		 
		 for(long j = 0; j < d; j++)
		 {
			 b = (b + a) % W;
			 result = Utilities.xorSymbol(result, 0, C, b * T, T);
		 }

		 while(b1 >= P)
			 b1 = (b1 + a1) % P1;

		 result = Utilities.xorSymbol(result, 0, C, (W + b1) * T, T);

		 for(long j = 1; j < d1; j++)
		 {
			 do
				 b1 = (b1 + a1) % P1;
			 while(b1 >= P);

			 result = Utilities.xorSymbol(result, 0, C, (W + b1) * T, T);
		 }

		 return result;
	 }
	 
	 /**
		 * Solves the decoding system of linear equations using the permanent inactivation technique
		 * @param A
		 * @param D
		 * @param symbol_size
		 * @param Kprime
		 * @return
		 * @throws SingularMatrixException
		 */
		 public static byte[] PInactivationDecoding(byte[][] A, byte[][] D, int symbol_size, int Kprime) throws SingularMatrixException {

			 // decoding parameters
	         int Ki = SystematicIndices.getKIndex(Kprime);
	         int S = SystematicIndices.S(Ki);
	         int H = SystematicIndices.H(Ki);
	         int W = SystematicIndices.W(Ki);
	         int L = Kprime + S + H;
	         int P = L - W;
	         int M = A.length;
	         
	         /*
	          *  initialize c and d vectors
	          */
	         int[] c = new int[L];
	         int[] d = new int[M];
	         
	         for(int i = 0; i < L; i++)
	         {
	                 c[i] = i;
	                 d[i] = i;
	         }
	         
	         for(int i = L; i < M; i++)
	         {
	                 d[i] = i;
	         }
	                  
	         // allocate X and copy A into X
	         byte[][] X = new byte[M][L];
	         
	         for(int row = 0; row < M; row++)
	                 System.arraycopy(A[row], 0, X[row], 0, L);

	         // initialize i and u parameters, for the submatrices sizes
	         int i = 0, u = P;
	         
	         /*
	          * DECODING
	          */
	         
	         /* 
	          * First phase 
	          * */
	         
	         // counts how many rows have been chosen already
	         int chosenRowsCounter = 0;
	         
	         // the number of rows that are not HDPC
	         //   (these should be chosen first)
	         int nonHDPCRows = S + H;
	         
	         /*
	          *  TODO Optimizacao: ao inves de percorrer isto todas as vezes, ver so as linhas que perderam
	          *   um non-zero, e subtrair ao 'r' original (e ao grau). Como lidar com as novas dimensoes de V? 
	          */
	         
	         // at most L steps
	         while(i + u != L)
	         {        
	        	 
	        	 // number of non-zeros in the 'currently chosen' row
	        	 int r = L + 1;
	        	 
	        	 // the index of the 'currently chosen' row
	        	 int rLinha = 0;

	             // the degree of the 'currently chosen' row
	             int minDegree = 256*L;
	                 
	        	 // maps the index of a row to an object Row (which stores that row's characteristics)
	             Map<Integer, Row> rows = new HashMap<Integer, Row>();
	             
	             // is the row HDPC or not?
	             boolean isHDPC, isHDPC2 = false;
	             
	             /*
	              *  find r
	              */
	             
	             // go through all matrix rows counting non-zeros
	             for(int row = i, nonZeros = 0, degree = 0; row < M; row++, nonZeros = 0, degree = 0)
	             {
	            	 Set<Integer> edges = new HashSet<Integer>();
	              
	            	 // check all columns for non-zeros
	            	 for(int col = i; col < L - u; col++)
	            	 {
	            		 if(A[row][col] == 0) // branch prediction
	            			 continue;
	            		 else
	            		 {
	            			 // count the non-zero
	            			 nonZeros++;
	            			 
	            			 // add to the degree of this row
	            			 degree += OctectOps.UNSIGN(A[row][col]);
	            			 
	            			 edges.add(col);
	            		 }
	            	 }
	            	 
	            	 // is this a HDPC row? this is not 100% accurate (false negatives) but is accurate enough)
	            	 if(degree <= nonZeros)
	            	 {
	            		 isHDPC = false;
	            	 }
	            	 else
	            	 {
	            		 isHDPC = true;
	            	 }
	      
	                 if(nonZeros == 2 && !isHDPC)
	                	 rows.put(row, new Row(row, nonZeros, degree, isHDPC, edges));
	                 else
	                	 rows.put(row, new Row(row, nonZeros, degree, isHDPC));        
	                  
	                 if(nonZeros > r || nonZeros == 0 || degree == 0) // branch prediction
	                	 continue;
	                 else
	                 {
	                	 if(nonZeros == r)
	                	 {
	                		 if(degree < minDegree)
	                		 {
	                			 rLinha = row;
	                			 minDegree = degree;
	                			 isHDPC2 = isHDPC;
	                		 }
	                		 else
	                			 continue;
	                	 }
	                	 else
	                	 {
	                		 r = nonZeros;
	                		 rLinha = row;
	                		 minDegree = degree;  
	            			 isHDPC2 = isHDPC;
	                	 }
	                 }
	             }
	                 
	             if(r == L + 1) // DECODING FAILURE
	            	 throw new SingularMatrixException("Decoding Failure - PI Decoding @ Phase 1: All entries in V are zero.");
	             
	             /*
	              *  choose the row
	              */
	             
	             if(r != 2)
	             {
	               	 // "HDPC rows should not be chosen untill all non-HDPC rows have been processed"
	            	 if(isHDPC2 && chosenRowsCounter < nonHDPCRows)
	            	 { // if it is, then we must choose another line
	                                 
	            		 int newDegree = 256*L;
	            		 int newR = L+1;
	                                
	            		 // lets go search all the rows
	            		 for(Row row : rows.values())
	            		 {	 
	            			 // if they're not HDPC rows
	            			if(!row.isHDPC && row.degree != 0)
	            			{
	            				 // does it have less non-zeros than our 'currently chosen' one?
	            				 if(row.nonZeros < newR)
	            				 {// if it does, since it's non-HDPC it'll also have a lower degree
	            					 
	            					 //  so lets update our 'currently chosen' row
	            					 newR = row.nonZeros;
	            					 rLinha = row.id;
	            					 newDegree = row.degree;
	            				 }
	            			 }
	            			 else
	            				 continue;
	            		 }
	            	 }
	            	 
	            	 // choose rLinha
	            	 chosenRowsCounter++;        
	             }
	             else 
	             { // r = 2
	            	 
	            	 // do we have a row with exactly two 1's? (remember we already know that there are rows with only 2 non-zeros)
	            	 if(minDegree == 2)
	            	 {
	            		 /*
	            		  *  create graph
	            		  */
	            		 
	            		 // allocate memory
	            		 Map<Integer, Set<Integer>> graph = new HashMap<Integer,Set<Integer>>();

	            		 // lets go through all the rows... (yet again!)
	            		 for(Row row : rows.values())
	            		 {
	            			 // is this row an edge?
	            			 if(row.edges != null)
	            			 {                        
	            				 // get the nodes connected through this edge
	            				 Integer[] edge = row.edges.toArray(new Integer[2]);
	            				 int node1 = edge[0];
	            				 int node2 = edge[1];

	            				 // node1 already in graph?
	            				 if(graph.keySet().contains(node1))
	            				 { // it is
	            					 
	            					 // then lets add node 2 to its neighbours
	            					 graph.get(node1).add(node2);
	            				 }
	            				 else
	            				 { // it isn't
	            					 
	            					 // allocate memory for its neighbours
	            					 Set<Integer> edges = new HashSet<Integer>();

	            					 // add node 2 to its neighbours
	            					 edges.add(node2);
	            					 
	            					 // finally, add node 1 to the graph along with its neighbours
	            					 graph.put(node1, edges);
	            				 }

	            				 // node2 already in graph?
	            				 if(graph.keySet().contains(node2))
	            				 { // it is
	            					 
	            					 // then lets add node 1 to its neighbours
	            					 graph.get(node2).add(node1);
	            				 }
	            				 else
	            				 { // it isn't
	            					 
	            					 // allocate memory for its neighbours
	            					 Set<Integer> edges = new HashSet<Integer>();

	            					 // add node 1 to its neighbours
	            					 edges.add(node1);
	            					 
	            					 // finally, add node 2 to the graph along with its neighbours
	            					 graph.put(node2, edges);
	            				 }
	            			 }
	            			 else
	            				 continue;
	            		 }
	            		 
	            		 /*
	            		  * the graph is complete, now we must
	            		  *  find the maximum size component
	            		  */

	            		 // have we found the maximum size component yet?
	            		 boolean found = false;
	            		 
	            		 // set of visited nodes
	            		 Set<Integer> visited = null;

	            		 /* 
	            		  * TODO Optimizacao: - ja procurei, e ha algoritmos optimizados para achar connected components
	            		  *                      e so depois ver qual o maior...
	            		  */
	            		 
	            		 // what is the size of the largest component we've already found
	            		 int maximumSize = 0;
	            		 
	            		 // the maximum size component
	            		 Set<Integer> greatestComponent = null; // TODO testar tempos com isto

	            		 // which nodes have already been used (either in visited or in toVisit)
	            		 Set<Integer> used = new HashSet<Integer>();
	            		 
	            		 // iterates the nodes in the graph
	            		 Iterator<Map.Entry<Integer, Set<Integer>>> it = graph.entrySet().iterator();

	            		 // let's iterate through the nodes in the graph, looking for the maximum
	            		 //  size component. we will be doing a breadth first seach // TODO optimize this with a better algorithm?
	            		 while(it.hasNext() && !found)
	            		 { 
	            			 // get our initial node
	            			 Map.Entry<Integer, Set<Integer>> node = it.next();
	            			 int initialNode = node.getKey();

	            			 // we can't have used it before!
	            			 if(used.contains(initialNode))
	            				 continue;

	            			 // what are the edges of our initial node?
	            			 Integer[] edges = (Integer[]) node.getValue().toArray(new Integer[1]);
	            			 
	            			 // allocate memory for the set of visited nodes
	            			 visited = new HashSet<Integer>();
	            			 
	            			 // the set of nodes we must still visit
	            			 List<Integer> toVisit = new LinkedList<Integer>();

	            			 // add the initial node to the set of used and visited nodes
	            			 visited.add(initialNode);
	            			 used.add(initialNode);

	            			 // add my edges to the set of nodes we must visit
	            			 //  and also put them in the used set
	            			 for(Integer edge : edges)
	            			 {
	            				 toVisit.add(edge);
	            				 used.add(edge);
	            			 }

	            			 // start the search!
	            			 while(toVisit.size() != 0)
	            			 {
	            				 // the node we are visiting
	            				 int no = toVisit.remove(0);

	            				 // add node to visited set
	            				 visited.add(no);

	            				 // queue edges to be visited (if they haven't been already
	            				 for(Integer edge : graph.get(no))
	            					 if(!visited.contains(edge))
	            						 toVisit.add(edge);
	            			 }

	            			 // is the number of visited nodes, greater than the 'currently' largest component?
	            			 if(visited.size() > maximumSize)
	            			 { // it is! we've found a greater component then...
	            				 
	            				 // update the maximum size
	            				 maximumSize = visited.size();
	            				 
	            				 // update our greatest component
	            				 greatestComponent = visited;
	            			 }
	            			 else
	            				 continue;
	            		 }
	            		 
	            		 /*
	            		  *  we've found the maximum size connected component -- 'greatestComponent'
	            		  */
	            		 
	            		 // let's choose the row 
	            		 for(Row row : rows.values())
	            		 { 
	            			 // is it a node in the graph?
	            			 if(row.edges != null)
	            			 { // it is

	            				 // get the nodes connected through this edge
	            				 Integer[] edge = row.edges.toArray(new Integer[2]);
	            				 int node1 = edge[0];
	            				 int node2 = edge[1];

	            				 // is this row an edge in the maximum size component?
	            				 if(greatestComponent.contains(node1) && greatestComponent.contains(node2))
	            				 { 
	            					 rLinha = row.id;
	            					 break;
	            				 }
	            				 else
	            					 continue;
	            			 }
	            			 else 
	            				 continue;
	            		 }

	            		 chosenRowsCounter++;
	            	 }
	            	 else
	            	 { // no rows with 2 ones
	            		 chosenRowsCounter++;
	            	 }
	             }

	             /*
	              *   a row has been chosen! -- 'rLinha'
	              */
	             
	             // get the chosen row
	             Row chosenRow = rows.get(rLinha);            

	             /* 
	              * "After the row is chosen in this step, the first row of A that intersects V is exchanged
	              *   with the chosen row so that the chosen row is the first row that intersects V."
	              */
	             
	             // if the chosen row is not 'i' already
	             if(rLinha != i)
	             {
	            	 // swap i with rLinha in A
	            	 byte[] auxRow = A[i];
	            	 A[i] = A[rLinha];
	            	 A[rLinha] = auxRow;

	            	 // swap i with rLinha in X
	            	 auxRow = X[i];
	            	 X[i] = X[rLinha];
	            	 X[rLinha] = auxRow;

	            	 // decoding process - swap i with rLinha in d
	            	 int auxIndex = d[i];
	            	 d[i] = d[rLinha];
	            	 d[rLinha] = auxIndex;                     
	             }


	             /*
	              * "The columns of A among those that intersect V are reordered so that one of the r nonzeros 
	              *   in the chosen row appears in the first column of V and so that the remaining r-1 nonzeros
	              *   appear in the last columns of V."
	              */
	             
	             // if there are non-zeros
	             if(chosenRow.degree > 0)
	             {
	            	 // stack of non-zeros in the chosen row
	            	 Stack<Integer> nonZeros = new Stack();
	            	 
	            	 // search the chosen row for the positions of the non-zeros
	            	 for(int nZ = 0, col = i; nZ < chosenRow.nonZeros; col++) 		// TODO the positions of the non-zeros could be stored as a Row attribute
	            	 {																//       this would spare wasting time in this for (little optimization)
	            		 if(A[i][col] == 0) // a zero
	            			 continue;
	            		 else
	            		 { // a non-zero
	            			 nZ++;
	            			 
	            			 // add this non-zero's position to the stack
	            			 nonZeros.push(col);
	            		 }
	            	 }

	            	 /*
	            	  *  lets start swapping columns!
	            	  */
	            	 
	            	 // swap a non-zero's column to the first column in V
	            	 int column;
	            	 if(A[i][i] == 0) // is the first column in V already the place of a non-zero?
	            	 {
	            		 // column to be swapped
	            		 column = nonZeros.pop();
	            		 
	            		 // swap columns
	            		 Utilities.swapColumns(A, column, i);
	            		 Utilities.swapColumns(X, column, i);

	            		 // decoding process - swap i and column in c
	            		 int auxIndex = c[i];
	            		 c[i] = c[column];
	            		 c[column] = auxIndex;
	            	 }
	            	 else // it is, so let's remove 'i' from the stack
	            		 nonZeros.remove((Integer)i);

	            	 // swap the remaining non-zeros' columns so that they're the last columns in V 
	            	 for(int remainingNZ = nonZeros.size(); remainingNZ > 0; remainingNZ--)
	            	 {                
	            		 // column to be swapped
	            		 column = nonZeros.pop();

	            		 // swap columns
	            		 Utilities.swapColumns(A, column, L-u-remainingNZ);
	            		 Utilities.swapColumns(X, column, L-u-remainingNZ);

	            		 // decoding process - swap column with L-u-remainingNZ in c
	            		 int auxIndex = c[L-u-remainingNZ];
	            		 c[L-u-remainingNZ] = c[column];
	            		 c[column] = auxIndex;
	            	 }

	            	 /*
	            	  * "... if a row below the chosen row has entry beta in the first column of V, and the chosen
	            	  *   row has entry alpha in the first column of V, then beta/alpha multiplied by the chosen 
	            	  *   row is added to this row to leave a zero value in the first column of V."
	            	  */
	            	 
	            	 // "the chosen row has entry alpha in the first column of V"
	            	 byte alpha = A[i][i];

	            	 // let's look at all rows below the chosen one
	            	 for(int row = i+1; row < M; row++)				// TODO queue these row operations for when/if the row is chosen - Page35@RFC6330 1st Par.
	            	 {
	            		 // if it's already 0, no problem
	            		 if(A[row][i] == 0)
	            			 continue;
	            		 
	            		 // if it's a non-zero we've got to "zerofy" it
	            		 else
	            		 {                                 
	            			 // "if a row below the chosen row has entry beta in the first column of V"
	            			 byte beta   = A[row][i];
	            			 
	            			 /*
	            			  *  "then beta/alpha multiplied by the chosen row is added to this row"
	            			  */
	            			 
	            			 // division
	            			 byte balpha = OctectOps.division(beta, alpha);

	            			 // multiplication 
	            			 byte[] product = OctectOps.betaProduct(balpha, A[i]);

	            			 // addition 
	            			 A[row] = Utilities.xorSymbol(A[row], product);

	            			 // decoding process - (beta * D[d[i]]) + D[d[row]]
	            			 product = OctectOps.betaProduct(balpha, D[d[i]]);
	            			 D[d[row]] = Utilities.xorSymbol(D[d[row]], product);
	            		 }
	            	 }
	             }

	             /*
	              * "Finally, i is incremented by 1 and u is incremented by r-1, which completes the step."
	              */
	             i++;
	             u += r-1;
	         }
	         // END OF FIRST PHASE

	         /* 
	          * Second phase 
	          * */

	         /*
	          * "At this point, all the entries of X outside the first i rows and i columns are discarded, so that X
	          *   has lower triangular form.  The last i rows and columns of X are discarded, so that X now has i
	          *    rows and i columns."
	          */

	         /*
	          * "Gaussian elimination is performed in the second phase on U_lower either to determine that its rank is
	          *   less than u (decoding failure) or to convert it into a matrix where the first u rows is the identity
	          *	  matrix (success of the second phase)."
	          */

	         // reduce U_lower to row echelon form
	         Utilities.reduceToRowEchelonForm(A, i, M, L-u, L, d, D);

	         // check U_lower's rank, if it's less than 'u' we've got a decoding failure
	         if(!Utilities.validateRank(A, i, i, M, L, u))
	        	 throw new SingularMatrixException("Decoding Failure - PI Decoding @ Phase 2: U_lower's rank is less than u.");

	         /*
	          * "After this phase, A has L rows and L columns."
	          */

	         // END OF SECOND PHASE

	         /* 
	          * Third phase 
	          * */

	         /*
	          * "... the matrix X is multiplied with the submatrix of A consisting of the first i rows of A."
	          */
	         byte[][] XA = Utilities.multiplyMatrices(X, 0, 0, i, i, A, 0, 0, i, L);

	         // copy the product (XA) to A
	         for(int row = 0; row < i; row++)
	        	 A[row] = XA[row];

	         // decoding process
	         byte[][] reorderD = new byte[L][];

	         // create a copy of D
	         for(int index = 0; index < L; index++) 
	        	 reorderD[index] = D[d[index]]; 

	         // multiply D by X
	         for(int row = 0; row < i; row++) // multiply X by D
	        	 D[d[row]] = Utilities.multiplyByteLineBySymbolVector(X[row], i, reorderD, symbol_size);

	         /* 
	          * Fourth phase 
	          * */

	         /*
	          * "For each of the first i rows of U_upper, do the following: if the row has a nonzero entry at position j,
	          *   and if the value of that nonzero entry is b, then add to this row b times row j of I_u."
	          */
	         
	         // "For each of the first i rows of U_upper"
	         for(int row = 0; row < i; row++)
	         {                                                                                                                
	        	 for(int j = i; j < L; j++)
	        	 {       
	        		 // "if the row has a nonzero entry at position j"
	        		 if(A[row][j] != 0)
	        		 {                                                                                                          
	        			 // "if the value of that nonzero entry is b"
	        			 byte b = A[row][j];
	        			 
	        			 // "add to this row b times row j" -- this would "zerofy" that position, thus we can save the complexity
	        			 A[row][j] = 0;

	        			 // decoding process - (beta * D[d[j]]) + D[d[row]]
	        			 byte[] product = OctectOps.betaProduct(b, D[d[j]]);
	        			 D[d[row]] = Utilities.xorSymbol(D[d[row]], product);
	        		 }
	        	 }
	         }

	         /* 
	          * Fifth phase 
	          * */
	         
	         // TODO Optimizacao: acho que da para zerar directamente o A, e deixar apenas as operacoes em D...          

	         
	         // "For j from 1 to i, perform the following operations:"
	         for(int j = 0; j < i; j++)
	         {
	        	 // "If A[j,j] is not one" 
	        	 if(A[j][j] != 1)
	        	 {
	        		 byte beta = A[j][j];
	        		 
	        		 // "then divide row j of A by A[j,j]."
	        		 A[j] = OctectOps.betaDivision(A[j], beta);

	        		 // decoding process - D[d[j]] / beta
	        		 D[d[j]] = OctectOps.betaDivision(D[d[j]], beta);
	        	 }

	        	 // "For l from 1 to j-1"
	        	 for(int l = 0; l < j; l++){

	        		 // "if A[j,l] is nonzero"
	        		 if(A[j][l] != 0)
	        		 { // "then add A[j,l] multiplied with row l of A to row j of A."
	        			          			 
	        			 byte beta = A[j][l];
	        			 
	        			 // multiply A[j][l] by row 'l' of A
	        			 byte[] product = OctectOps.betaProduct(beta, A[l]);

	        			 // add the product to row 'j' of A
	        			 A[j] = Utilities.xorSymbol(A[j], product);

	        			 // decoding process - D[d[j]] + (A[j][l] * D[d[l]])
	        			 product = OctectOps.betaProduct(beta, D[d[l]]);
	        			 D[d[j]] = Utilities.xorSymbol(D[d[j]], product);
	        		 }
	        	 }
	         }

	         // allocate memory for the decoded symbols
	         byte[] C = new byte[L*symbol_size];
	         
	         // copy the decoded source symbols from D to C
	         for(int symbol = 0; symbol < L; symbol++)
	        	 System.arraycopy(D[d[symbol]], 0, C, c[symbol]*symbol_size, symbol_size);

	         // return the decoded source symbols
	         return C;
		 }
}
