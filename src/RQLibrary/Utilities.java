package RQLibrary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public final class Utilities {

	public static final void printMatrix(byte[][] matrix) {
		
		int M = matrix.length;
		int N = matrix[0].length;

		for(int i = 0; i < M; i++)
			if(matrix[i].length != N)
				throw new IllegalArgumentException("Invalid matrix dimensions.");
		
		System.out.printf("    ");
		for (int j = 0; j < N; j++)
			System.out.printf("* %02d ", j);

		System.out.println("|");

		for (int i = 0; i < M; i++) {
			System.out.printf(" %02d)", i);
			for (int j = 0; j < N; j++)
				System.out.printf("| %02X ", (matrix[i][j]));
			System.out.println("|");
		}
	}
	
	/**
	 * Multiplies Matrix A by Matrix B. Requires the number of columns in A 
	 * to be equal to the number of rows in B.
	 * 
	 * @param A Matrix A
	 * @param B Matrix B
	 *
	 * @return A�B
	 */
	public static final byte[][] multiplyMatrices(byte[][] A, byte[][] B){
		
        //if (A[0].length != B.length) throw new RuntimeException("Illegal matrix dimensions.");
        
        byte[][] C = new byte[A.length][B[0].length];
        
        for (int i = 0; i < C.length; i++){
            for (int j = 0; j < C[0].length; j++){
                for (int k = 0; k < A[0].length; k++){
                	
                	byte temp = OctectOps.product(A[i][k], B[k][j]);

               		C[i][j] = (byte) (C[i][j] ^ temp);
                }
            }
        }
        
        return C;
	}
	
	/**
	 * Multiplies a sub-matrix of Matrix A by a sub-matrix of Matrix B. Requires the number
	 * of columns in A's sub-matrix to be equal to the number of rows in B's sub-matrix.
	 * 
	 * @param A Matrix A
	 * @param B Matrix B
	 *
	 * @return A�B
	 */
	public static byte[][] multiplyMatrices(byte[][] A, int first_rowA, int first_colA, int last_rowA, int last_colA, 
											byte[][] B, int first_rowB, int first_colB, int last_rowB, int last_colB){
		
        //if ((last_colA  - first_colA) != (last_rowB - first_rowB)) throw new RuntimeException("Illegal matrix dimensions.");
        
        int colsA = last_colA - first_colA;
        int colsB = last_colB - first_colB;
        int rowsA = last_rowA - first_rowA;
        
        byte[][] C = new byte[rowsA][colsB];
        
        for (int i = 0; i < C.length; i++){
            for (int j = 0; j < C[0].length; j++){
          
            	
            	for (int k = 0; k < colsA; k++){
                	
                	byte temp = OctectOps.product(A[i+first_rowA][k+first_colA], B[k+first_rowB][j+first_colB]);

               		C[i][j] = (byte) (C[i][j] ^ temp);
                }
            }
        }
        
        return C;
	}
	
	/**
	 * Multiplies a sub-matrix of Matrix A by a sub-matrix of Matrix B. Requires the number
	 * of columns in A's sub-matrix to be equal to the number of rows in B's sub-matrix.
	 * 
	 * @param A Matrix A
	 * @param B Matrix B
	 * @param C Matrix where the results should be stored.
	 * 
	 * @return C
	 */
	public static void multiplyMatrices(byte[][] A, int first_rowA, int first_colA, int last_rowA, int last_colA, 
										byte[][] B, int first_rowB, int first_colB, int last_rowB, int last_colB,
										byte[][] C, int first_rowC, int first_colC, int last_rowC, int last_colC){

		//if ((last_colA  - first_colA) != (last_rowB - first_rowB)) throw new RuntimeException("Illegal matrix dimensions.");

		int colsA = last_colA  - first_colA;
		int colsC = last_colC  - first_colC;
		int rowsC = last_rowC - first_rowC;

		for (int i = 0; i < rowsC; i++){
			for (int j = 0; j < colsC; j++){
				for (int k = 0; k < colsA; k++){

					byte temp = OctectOps.product(A[i+first_rowA][k+first_colA], B[k+first_rowB][j+first_colB]);

					C[i+first_rowC][j+first_colC] = (byte) (C[i+first_rowC][j+first_colC] ^ temp);
				}
			}
		}
	}
	
	/**
	 * Multiplies a row (<code>line</code>) by a vector.
	 * The number of columns in <code>line</code> must be equal to the number of rows
	 * in <code>vector</coder>
	 * 
	 * @param line
	 * @param vector
	 * @param symbol_size
	 * 
	 * @return
	 */
	public static byte[] multiplyByteLineBySymbolVector(byte[] line, byte[] vector, int symbol_size){
		
		//if((line.length * symbol_size) != vector.length) throw new RuntimeException("Illegal line/vector dimensions.");
		
		byte[] result = new byte[symbol_size];
		
		for(int octet=0; octet<symbol_size; octet++){

			for(int colRow=0; colRow<line.length; colRow++){
				
				byte temp = OctectOps.product(line[colRow], vector[(colRow * symbol_size) + octet]);
				
				result[octet] = (byte) (result[octet] ^ temp);
			}
		}
		
		return result;
	}
	
	/**
	 * Multiplies a row (<code>line</code>) by a vector.
	 * The number of columns in <code>line</code> must be equal to the number of rows
	 * in <code>vector</coder>
	 * 
	 * @param line
	 * @param vector
	 * @param symbol_size
	 * 
	 * @return
	 */
	public static byte[] multiplyByteLineBySymbolVector(byte[] line, int line_length, byte[][] vector, int symbol_size){
				
		byte[] result = new byte[symbol_size];
		
		for(int octet=0; octet<symbol_size; octet++){

			for(int colRow=0; colRow<line_length; colRow++){
				
				byte temp = OctectOps.product(line[colRow], vector[colRow][octet]);
				
				result[octet] = (byte) (result[octet] ^ temp);
			}
		}
		
		return result;
	}
	
	public static void reduceToRowEchelonForm(byte[][] A, int first_row, int last_row, int first_col, int last_col, int[] d, byte[][] D){
		
		int lead = 0;
		int rowCount    = last_row - first_row;
		int columnCount = last_col - first_col;
		
		for(int r = 0; r < rowCount; r++){
			
			if(columnCount <= lead)
				return;
			
			int i = r;
			while(A[i+first_row][lead+first_col] == 0){
				
				i++;
				
				if(rowCount == i){
					
					i = r;
					lead++;
					if(columnCount == lead)
						return;
				}
			}
			
			if( i != r){

				byte[] auxRow = A[i+first_row];
				A[i+first_row] = A[r+first_row];
				A[r+first_row] = auxRow;
				
				// decoding process - swap d[i] with d[r] in d
				int auxIndex = d[i+first_row];
				d[i+first_row] = d[r+first_row];
				d[r+first_row] = auxIndex;
				
			}

			byte beta = A[r+first_row][lead+first_col];
			if(beta != 0){	
				for(int col = 0; col < columnCount; col++)
					A[r+first_row][col+first_col] = OctectOps.division(A[r+first_row][col+first_col], beta);
			
				// decoding process - divide D[d[r]] by U_lower[r][lead]
				D[d[r+first_row]] = OctectOps.betaDivision(D[d[r+first_row]], beta);
			}
			
			for(i = 0; i < rowCount; i++){
				
				beta = A[i+first_row][lead+first_col];
				
				if(i != r){
					// U_lower[i] - (U_lower[i][lead] * U_lower[r])
					byte[] product = OctectOps.betaProduct(beta, A[r+first_row], first_col, columnCount);
					
					for(int col = 0; col < columnCount; col++)
						A[i+first_row][col+first_col] = OctectOps.subtraction(A[i+first_row][col+first_col], product[col]);				
				
					// decoding process - D[d[i+first_row]] - (U_lower[i][lead] * D[d[r+first_row]])
					product = OctectOps.betaProduct(beta, D[d[r+first_row]]);
					D[d[i+first_row]] = xorSymbol(D[d[i+first_row]], product);
				}	
			}
			
			lead++;
		}
	}
	
	public static void reduceToRowEchelonForm(byte[][] A, int first_row, int last_row, int first_col, int last_col){
		
		int lead = 0;
		int rowCount    = last_row - first_row;
		int columnCount = last_col - first_col;
		
		for(int r = 0; r < rowCount; r++){
			
			if(columnCount <= lead)
				return;
			
			int i = r;
			while(A[i+first_row][lead+first_col] == 0){
				
				i++;
				
				if(rowCount == i){
					
					i = r;
					lead++;
					if(columnCount == lead)
						return;
				}
			}
			
			if( i != r){

				byte[] auxRow = A[i+first_row];
				A[i+first_row] = A[r+first_row];
				A[r+first_row] = auxRow;
			}

			byte beta = A[r+first_row][lead+first_col];
			if(beta != 0){	
				for(int col = 0; col < columnCount; col++)
					A[r+first_row][col+first_col] = OctectOps.division(A[r+first_row][col+first_col], beta);
			
			}
			
			for(i = 0; i < rowCount; i++){
				
				beta = A[i+first_row][lead+first_col];
				
				if(i != r){
					// U_lower[i] - (U_lower[i][lead] * U_lower[r])
					byte[] product = OctectOps.betaProduct(beta, A[r+first_row], first_col, columnCount);
					
					for(int col = 0; col < columnCount; col++)
						A[i+first_row][col+first_col] = OctectOps.subtraction(A[i+first_row][col+first_col], product[col]);				
				
				}	
			}
			
			lead++;
		}
	}
	
	public static void swapColumns(byte[][] matrix, int a, int b){
		
		// check sizes and limits and whatnot bla bla bla
		
		byte auxPos;
		
		for(int row = 0; row < matrix.length; row++){
			// swap
			auxPos = matrix[row][a];
			matrix[row][a] = matrix[row][b];
			matrix[row][b] = auxPos;
		}
	}

	public void swapRows(byte[][] matrix, int a, int b){

		// check sizes and limits and whatnot bla bla bla

		byte[] auxRow = matrix[a];
		matrix[a] = matrix[b];
		matrix[b] = auxRow;
	}
	
	public static byte[] xorSymbol(byte[] s1, byte[] s2){
		
		/*
		if(s1.length != s2.length){
			throw new IllegalArgumentException("Symbols must be of the same size.");
		}
		*/

		byte[] xor = new byte[s1.length];

		for(int i=0; i<s1.length; i++){
			xor[i] = (byte) (s1[i] ^ s2[i]);
		}

		return xor;
	}

	public static byte[] xorSymbol(byte[] s1, int pos1, byte[] s2, int pos2, int length){

		/*
		if((s1.length - pos1) < length || (s2.length - pos2) < length ){
			throw new IllegalArgumentException("Symbols must be of the same size.");
		}
		*/

		byte[] xor = new byte[length];
		
		for(int i=0; i<length; i++){
			xor[i] = (byte) (s1[pos1+i] ^ s2[pos2+i]);
		}
		
		return xor;
	}

	public static long ceilPrime(long p){

		if(p == 1)
			p++;
		
		while(!isPrime(p)) 
			p++;

		return p;
	}

	public static boolean isPrime(long n){
		
		//check if n is a multiple of 2
		if (n % 2 == 0) return false;
		
		//if not, then just check the odds
		for(long i = 3; i * i <= n; i += 2)
			if(n % i == 0)
				return false;
		
		return true;
	}
	
	public static boolean checkIdentity(byte[][] A, int L) {
		
		for(int row = 0; row < L; row++)
			for(int col = 0; col < L; col++)
				if(row != col && A[row][col] != 0)
					return false;
				else if(row == col && A[row][col] != 1)
					return false;
		
		return true;
	}
	
	// works only for matrices in row echelon form
		public static boolean validateRank(byte[][] matrix, int first_row, int first_col, int last_row, int last_col, int rank_lower_limit){
			
			int nonZeroRows = 0;
			
			for(int row = first_row; row < last_row; row++){
				
				for(int col = first_col; col < last_col; col++){

					if(matrix[row][col] == 0)
						continue;
					else{
						nonZeroRows++;
						break;
					}
				}
			}
			
			if(nonZeroRows < rank_lower_limit)
				return false;
			else
				return true;
		}

		public static void rowEchelonForm(byte[][] A) throws SingularMatrixException{
			
			int ROWS = A.length;
			int COLS = A[0].length;
			
			for(int row=0; row<COLS; row++){
				
				int max = row;
				
				// find pivot row and swap
				for (int i = row + 1; i < ROWS; i++)
					if (OctectOps.UNSIGN(A[i][row]) > OctectOps.UNSIGN(A[max][row]))
						max = i;

				// this destroys the original matrixes... dont really need a fix, but should be kept in mind
				byte[] temp = A[row];
				A[row] = A[max];
				A[max] = temp;
				
				// singular
	            if (A[row][row] == 0)
					throw new SingularMatrixException();
	 
	            // pivot within A
	            for(int i=row+1; i<ROWS; i++) {
	            	
	            	byte alpha = OctectOps.division(A[i][row], A[row][row]);
	            	
	            	for(int j=row; j<COLS; j++) {
	            	
	            		byte aux = OctectOps.product(alpha, A[row][j]);
	            		
	            		A[i][j] = OctectOps.subtraction(A[i][j], aux);
	            	}
	            }
			}
		}
		
		public boolean symbolIsZero(byte[] symbol){

			for(byte b : symbol)
				if(b != 0)
					return false;

			return true;
		}
		
		/**
		 * Solves a 'A.x = b' system using regular Gauss elimination.
		 * @param A
		 * @param b
		 * @return Array of symbols 'x'
		 * @throws SingularMatrixException
		 */
		public static byte[] gaussElimination(byte[][] A, byte[][] b) throws SingularMatrixException
		{
			if (A.length != A[0].length || b.length != A.length)
				throw new RuntimeException("Illegal matrix dimensions.");
			
			int num_cols = b[0].length;
		
			byte[] x = new byte[b.length * num_cols];
			
			Utilities.printMatrix(A);
			
			int ROWS = b.length;
			
			for(int row=0; row<ROWS; row++){
				
				int max = row;
				
				// find pivot row and swap
				for (int i = row + 1; i < ROWS; i++)
					if (OctectOps.UNSIGN(A[i][row]) > OctectOps.UNSIGN(A[max][row]))
						max = i;

				// this destroys the original matrixes... dont really need a fix, but should be kept in mind
				byte[] temp = A[row];
				A[row] = A[max];
				A[max] = temp;
				
				temp = b[row];
				b[row] = b[max];
				b[max] = temp;

				// singular or nearly singular
	            if (A[row][row] == 0) {
					//System.err.println("LINHA QUE DEU SINGULAR: "+row);
					Utilities.printMatrix(b);
					Utilities.printMatrix(A);
	            	throw new SingularMatrixException("LINHA QUE DEU SINGULAR: "+row);
	            }
	 
	            // pivot within A and b
	            for(int i=row+1; i<ROWS; i++) {
	            	
	            	byte alpha = OctectOps.division(A[i][row], A[row][row]);
	            	
	            	temp = OctectOps.betaProduct(alpha, b[row]);
	            	
	            	b[i] = Utilities.xorSymbol(b[i], temp);
	            	
	            	
	            	for(int j=row; j<ROWS; j++) {
	            	
	            		byte aux = OctectOps.product(alpha, A[row][j]);
	            		
	            		A[i][j] = OctectOps.subtraction(A[i][j], aux);
	            	}
	            }
			}
			
			// back substitution
	        for(int i = ROWS-1; i >= 0; i--) {
	        	
	            byte[] sum = new byte[num_cols];
	            
	            for(int j=i+1; j<ROWS; j++) {

	            													// i*num_cols+j
	            	byte[] temp = OctectOps.betaProduct(A[i][j], x, j*num_cols, num_cols);
	            	
	            	sum = Utilities.xorSymbol(sum, temp);
	            }
	            
	            byte[] temp = Utilities.xorSymbol(b[i], sum);
	            
	            //x[i] = OctectOps.betaDivision(temp, A[i][i]);
	            for(int bite = 0; bite < num_cols; bite++){
	            	
	            	x[(i*num_cols) + bite] = OctectOps.division(temp[bite], A[i][i]);
	            }
	        }
			
			return x;
		}
		
		
		/*
		 * can't remember why I made this. it's not used.
		 *  looks like a endian converter
		 */
		public static final byte convert(byte in){

			byte hex1 = (byte) (in << 4);
			byte hex2 = (byte) ((in >>> 4) & 0xF);

			return (byte) (hex2 | hex1);
		}
		
		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		
		 public static void PInactivationDecoding(byte[][] A, int K) throws SingularMatrixException {

             int Ki = SystematicIndices.getKIndex(K);
             int S = SystematicIndices.S(Ki);
             int H = SystematicIndices.H(Ki);
             int W = SystematicIndices.W(Ki);
             int L = K + S + H;
             int P = L - W;
             int M = A.length;
             
             // Allocate X and copy A into X
             byte[][] X = new byte[M][L];
             for(int row = 0; row < M; row++)
                     System.arraycopy(A[row], 0, X[row], 0, L);

             int i = 0, u = P;
             
             /* 
              * First phase 
              * */
             int chosenRowsCounter = 0;
             int nonHDPCRows = S + H;

             Map<Integer, Integer> originalDegrees = new HashMap<Integer, Integer>();

             /*
              *  TODO Optimizacao: ao inves de percorrer isto todas as vezes, ver so as linhas quer perderam
              *  um non-zero, e subtrair ao 'r' original. Como lidar com as novas dimensoes de V? 
              */
             
             while(i + u != L){
                     
                     int r = L+1, rLinha = 0        ;                        
                     Map<Integer, Row> rows = new HashMap<Integer, Row>();
                     int minDegree = 256*L;
                     
                     // find r
                     for(int row = i, nonZeros = 0, degree = 0; row < M; row++, nonZeros = 0, degree = 0){

                             Set<Integer> edges = new HashSet<Integer>();
                             
                             for(int col = i; col < L-u; col++){
                                     if(A[row][col] == 0) // branch prediction
                                             continue;
                                     else{
                                             nonZeros++;
                                             degree += OctectOps.UNSIGN(A[row][col]);
                                             edges.add(col);
                                     }
                             }
                             
                             
                             if(nonZeros == 2 && (row < S || row >= S+H))
                                     rows.put(row, new Row(row, nonZeros, degree, edges));
                             else
                                     rows.put(row, new Row(row, nonZeros, degree));        

                             
                             if(nonZeros > r || nonZeros == 0 || degree == 0) // branch prediction
                                     continue;
                             else{
                                     if(nonZeros == r){
                                             if(degree < minDegree){
                                                     rLinha = row;
                                                     minDegree = degree;
                                             }
                                     }
                                     else{
                                             r = nonZeros;
                                             rLinha = row;
                                             minDegree = degree;                                                        
                                     }
                             }
                             
                     }
                     
                     if(r == L+1) // DECODING FAILURE
                             throw new SingularMatrixException("Decoding Failure - PI Decoding @ Phase 1: All entries in V are zero.");
                     
                     // choose the row
                     if(r != 2){
                             // check if rLinha is OK
                             if(rLinha >= S && rLinha < S+H && chosenRowsCounter != nonHDPCRows){ // choose another line
                                     
                                     int newDegree = 256*L;
                                     int newR = L+1;
                                     
                                     for(Row row : rows.values()){
                                             
                                             if((row.id < S || row.id >= S+H) && row.degree != 0){
                                                     if(row.nonZeros <= newR){
                                                             if(row.nonZeros == newR){
                                                                     if(row.degree < newDegree){
                                                                             rLinha = row.id;
                                                                             newDegree = row.degree;
                                                                     }
                                                             }
                                                             else{
                                                                     newR = row.nonZeros;
                                                                     rLinha = row.id;
                                                                     newDegree = row.degree;
                                                             }
                                                     }
                                             }
                                             else
                                                     continue;
                                     }
                             }
                             // choose rLinha
                             chosenRowsCounter++;
                     }
                     else{

                             if(minDegree == 2){
                                     
                                     // create graph
                                     Map<Integer, Set<Integer>> graph = new HashMap<Integer,Set<Integer>>();

                                     for(Row row : rows.values()){
                                             
                                             //edge?
                                             if(row.edges != null){
                                                     
                                                     Integer[] edge = row.edges.toArray(new Integer[2]);
                                                     int node1 = edge[0];
                                                     int node2 = edge[1];
                                                     
                                                     // node1 already in graph?
                                                     if(graph.keySet().contains(node1)){
                                                             
                                                             graph.get(node1).add(node2);
                                                     }
                                                     else{
                                                             Set<Integer> edges = new HashSet<Integer>();
                                                             
                                                             edges.add(node2);
                                                             graph.put(node1, edges);
                                                     }
                                                     
                                                     // node2 already in graph?
                                                     if(graph.keySet().contains(node2)){
                                                             
                                                             graph.get(node2).add(node1);
                                                     }
                                                     else{
                                                             Set<Integer> edges = new HashSet<Integer>();
                                                             
                                                             edges.add(node1);
                                                             graph.put(node2, edges);
                                                     }
                                             }
                                             else
                                                     continue;
                                     } // graph'd
                                     
                                     // find largest component 
                                     //int maximumSize = graph.size();
                                     boolean found = false;
                                     Set<Integer> visited = null;
                   
                                             int maximumSize = 0;
                                             Set<Integer> greatestComponent = null; // TODO testar tempos com isto o.O
                                             
                                             Set<Integer> used = new HashSet<Integer>();
                                             Iterator<Map.Entry<Integer, Set<Integer>>> it = graph.entrySet().iterator();
                                             
                                             while(it.hasNext() && !found){ // going breadth first, TODO optimize this with a better algorithm

                                                     Map.Entry<Integer, Set<Integer>> node = it.next();
                                                     int initialNode = node.getKey();
                                                     
                                                     if(used.contains(initialNode))
                                                             continue;
                                                     
                                                     Integer[] edges = (Integer[]) node.getValue().toArray(new Integer[1]);
                                                     visited = new HashSet<Integer>();
                                                     List<Integer> toVisit = new LinkedList<Integer>();
                                                     
                                                     // add self
                                                     visited.add(initialNode);
                                                     used.add(initialNode);
                                                     
                                                     // add my edges
                                                     for(Integer edge : edges){
                                                             toVisit.add(edge);
                                                             used.add(edge);
                                                     }
                                                     
                                                     // start visiting
                                                     while(toVisit.size() != 0){
                                                             
                                                             int no = toVisit.remove(0);
                                                             
                                                             // add node to visited set
                                                             visited.add(no);
                                                             
                                                             // queue edges
                                                             for(Integer edge : graph.get(no))
                                                                     if(!visited.contains(edge))
                                                                                     toVisit.add(edge);
                                                     }
                                             
                                                             
                                                     if(visited.size() > maximumSize){
                                                             
                                                             maximumSize           = visited.size();
                                                             greatestComponent = visited;
                                                     }
                                                     
                                           
                                             }
                                     
                                     // 'visited' is now our connected component
                                     for(Row row : rows.values()){
                                             
                                             if(row.edges != null){
                                             
                                                     Integer[] edge = row.edges.toArray(new Integer[2]);
                                                     int node1 = edge[0];
                                                     int node2 = edge[1];

                                                     if(visited.contains(node1) && visited.contains(node2)){ // found 2 ones (edge) in component
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
                             else{ // no rows with 2 ones
                                     chosenRowsCounter++;
                             }
                     }
                     
                   
                     // 'rLinha' is the chosen row
                     Row chosenRow = rows.get(rLinha);
                    
                     if(rLinha != i){
                              
                             // swap i with rLinha in A
                             byte[] auxRow = A[i];
                             A[i] = A[rLinha];
                             A[rLinha] = auxRow;

                             // swap i with rLinha in X
                             auxRow = X[i];
                             X[i] = X[rLinha];
                             X[rLinha] = auxRow;
          
                     }
                     
                     
                     // re-order columns
                     if(chosenRow.degree > 0){
                             Stack<Integer> nonZeros = new Stack<Integer>();
                             for(int nZ = 0, col = i; nZ < chosenRow.nonZeros; col++){

                                     if(A[i][col] == 0)
                                             continue;
                                     else{
                                             nZ++;
                                             nonZeros.push(col);
                                     }
                             }

                             int coluna;
                             if(A[i][i] == 0){
                                     
                                     coluna = nonZeros.pop();
                                     swapColumns(A, coluna, i);
                                     swapColumns(X, coluna, i);
                             
                                   
                             }
                             else
                                     nonZeros.remove((Integer)i);
                             
                             for(int remainingNZ = nonZeros.size(); remainingNZ > 0; remainingNZ--){                

                                     coluna = nonZeros.pop();
                                     
                                     // swap
                                     swapColumns(A, coluna, L-u-remainingNZ);
                                     swapColumns(X, coluna, L-u-remainingNZ);

                                     
                             }
                     
                             // beta/alpha gewdness
                             byte alpha = A[i][i];
                             
                             for(int row = i+1; row < M; row++){
                                     
                                     if(A[row][i] == 0)
                                             continue;
                                     else{                                 // TODO Queue these row operations for when (if) the row is chosen - RFC 6330 @ Page 35 1st Par.
                                             
                                             // beta/alpha
                                             byte beta   = A[row][i];
                                             byte balpha = OctectOps.division(beta, alpha);
                                             
                                             // multiplication 
                                             byte[] product = OctectOps.betaProduct(balpha, A[i]);
                                             
                                             // addition 
                                             A[row] = xorSymbol(A[row], product);

                                     }
                             }
                     }
                     
                     // update 'i' and 'u'
                     i++;
                     u += r-1;
             }
             // END OF FIRST PHASE
             
             /* 
              * Second phase 
              * */
             // X is now ixi

             reduceToRowEchelonForm(A, i, M, L-u, L);

             if(!validateRank(A, i, i, M, L, u)) // DECODING FAILURE
                     throw new SingularMatrixException("Decoding Failure - PI Decoding @ Phase 2: U_lower's rank is less than u.");
             
             // A is now LxL
             
             // END OF SECOND PHASE
             
             /* 
              * Third phase 
              * */
             
             // multiply X by A submatrix
             byte[][] XA = multiplyMatrices(X, 0, 0, i, i, A, 0, 0, i, L);
             for(int row = 0; row < i; row++)
                     A[row] = XA[row];
             
             /* 
              * Fourth phase 
              * */

             for(int row=0; row < i; row++){                                                                                                                
                     for(int j = i; j < L; j++){                                                                                                                
                             if(A[row][j] != 0){                                                                                                                        

                                     byte b    = A[row][j];
                                     A[row][j] = 0;
                             }
                     }
             }
            
             /* 
              * Fifth phase 
              * */
             
             /*
              * TODO Optimizacao: acho que da para zerar directamente o A, e deixar apenas as operacoes em D...
              */
             
             for(int j = 0; j < i; j++){
                     
                     if(A[j][j] != 1){ //A[j][j] != 0
                             
                             byte beta = A[j][j];
                             A[j] = OctectOps.betaDivision(A[j], beta);
                             
                     }
                     
                     for(int l = 0; l < j; l++){
                             
                             if(A[j][l] != 0){
                                     
                                     byte beta = A[j][l];
                                     byte[] product = OctectOps.betaProduct(beta, A[l]);
                                     
                                     A[j] = xorSymbol(A[j], product);

                             }
                     }
             }
     }
		
		
}