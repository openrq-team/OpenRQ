package testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import RQLibrary.Encoder;
import RQLibrary.SystematicIndices;
import RQLibrary.Tuple;

public class conceptAttack {

	public static void main(String[] args) {

		if(args.length != 4){
			StringBuilder s = new StringBuilder();
			s.append("Usage: \n");
			s.append("    java -jar conceptAttack.jar K initialLossLimit Epsilon overhead\n");
			System.out.println(s.toString());
		}
		else{
			
			int K         = Integer.valueOf(args[0]);
			int limit     = Integer.valueOf(args[1]);
			float epsilon = Float.valueOf(  args[2]);
			int overhead  = Integer.valueOf(args[3]);
			
			int bodycount = trim(K, limit, epsilon, overhead);
			
			System.out.println("Total sniped symbols: "+bodycount);
		}
	}
	
	private static int trim(int K, int upperLimit, float Epsilon, int OVERHEAD){ // FIXME for now overhead is 0

		// INITIALIZATION
		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int LT_start = S + H;
		int L = K + S + H;
		
		// repair lines
		byte[][] repairs = new byte[upperLimit][L];
		for(int repair = 0; repair < upperLimit; repair++){
			
			Tuple tuple = new Tuple(K, K + OVERHEAD + repair);
			Set<Integer> indexes = Encoder.encIndexes(K, tuple);

			for(Integer col : indexes)
				repairs[repair][col] = 1;
		}

		// constraint matrix
		byte[][] lConstraint = Encoder.generateConstraintMatrix(K, 1);

		byte[][] constraint_matrix;
		if (OVERHEAD == 0) {
			
			constraint_matrix = lConstraint;
		} 
		else {

			constraint_matrix = new byte[L + OVERHEAD][L];
			
			for(int row = 0; row < L; row++)
				constraint_matrix[row] = lConstraint[row];
			
			for(int row = L; row < L + OVERHEAD; row++){
				
				Tuple tuple = new Tuple(K, row - L + K);
				Set<Integer> indexes = Encoder.encIndexes(K, tuple);

				for(Integer col : indexes)
					constraint_matrix[row][col] = 1;
			}
		}

		// set of lines that can be targeted
		Integer[] lineNumbers = new Integer[K+OVERHEAD];
		for(int row = 0; row < K+OVERHEAD; row++)
			lineNumbers[row] = S+H+row;
		
		// set of ISIs that can be used as payload
		Integer[] ISIs = new Integer[upperLimit];
		for(int isi = 0; isi < upperLimit; isi++)
			ISIs[isi] = K + OVERHEAD + isi;
			
		// number of symbols killed
		int bodycount = 99999999;
		
		// ALGORITHM
		/*for(int lines = 1; lines <= K && ((bodycount*1.0) / (K+OVERHEAD)) > Epsilon && bodycount != lines; lines++){
			System.out.println("Testing attack on " + lines + " lines.");
			
			// combinations for lines
			ICombinatoricsVector<Integer> initialVector = Factory.createVector(lineNumbers);
			Generator<Integer> combLines = Factory.createSimpleCombinationGenerator(initialVector, lines);
			
			for(ICombinatoricsVector<Integer> combination : combLines) { // for each combination of lines
			*/	
				// lines to be replaced
				//List<Integer> targetLines = combination.getVector();
				List<Integer> targetLines = new ArrayList<Integer>();
				targetLines.add(S + H + K - 1);
				int lines = 1;
				
				// combinations for ISIs
				ICombinatoricsVector<Integer> initialVector = Factory.createVector(ISIs);
				Generator<Integer> combISIs = Factory.createSimpleCombinationGenerator(initialVector, lines);				
				
				for(ICombinatoricsVector<Integer> combination2 : combISIs) { // test all combinations of payloads in those lines
										
					// set of repair symbols to be replace the target lines 
					List<Integer> ISIpayload = combination2.getVector();
					
					//System.out.println("Attacking lines " + Arrays.toString(targetLines.toArray(new Integer[lines]))
						//				+ " with ISIs " + Arrays.toString(ISIpayload.toArray(new Integer[lines])));
					
					// the upperlimit might have changed, we dont want to waste time with useless gaussian eliminations
					if(containsBiggerOrEqual(ISIpayload, upperLimit + K))
						continue;
					
					// build the decoding matrix (A), replacing the target lines with the payload
					byte[][] A = new byte[L+OVERHEAD][];
					Iterator<Integer> it = ISIpayload.iterator();
					
					for(int row = 0; row < L+OVERHEAD; row++){
					
						if(!targetLines.contains(row))
							A[row] = Arrays.copyOf(constraint_matrix[row], L);
						else{
							int repair = it.next();
							A[row] = Arrays.copyOf(repairs[repair - K - OVERHEAD], L);
						}
					}
					
					// reduce A to row echelon form
					Encoder.rowEchelonForm(A);
					
					// rank < L?
					if(A[L-1][L-1] == 0){
						
						// update the value of the body count
						upperLimit = Collections.max(ISIpayload);

						// target ISIs (corresponding  to the target lines)
						List<Integer> targetISIs = new ArrayList<Integer>(lines+1);
						for(int line : targetLines)
							targetISIs.add(line - LT_start);
						
						// number of symbols killed in this attack
						bodycount = upperLimit - K - OVERHEAD + 1;
						
						// persist trimmed attack
						try {
							
							File file = new File("concept/attack_" + K + "_" + OVERHEAD + ".txt");

							if (file.exists())
								file.createNewFile();

							FileWriter fw = new FileWriter(file.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							
							bw.write(" - K: " + K + "\n - Overhead: " + OVERHEAD + "\n - Epsilon: " + Epsilon + "\n");
							bw.write("\n Target lines: " + Arrays.toString(targetLines.toArray(new Integer[lines])) +
									 "\n Target  ISIs: " + Arrays.toString(targetISIs.toArray(new Integer[lines])) +
									 "\n Payload ISIs: " + Arrays.toString(ISIpayload.toArray(new Integer[lines])) +
									 "\n Body count  : " + bodycount + " (" + ((bodycount*1.0) / (K+OVERHEAD))*100 + "%)");

							bw.write("\n\n\n------ PAYLOAD LINES ------\n\n");
							
							Iterator<Integer> isi = ISIpayload.iterator();
							while(isi.hasNext()){
								
								int repair = isi.next();
								bw.write(Arrays.toString(repairs[repair - K - OVERHEAD])+"\n");
							}
							bw.write("\n---------------------------\n\n");
							
							bw.flush();
							bw.close();
							
						} catch (IOException e) {
							System.err.println(e.getMessage());
							e.printStackTrace();
							System.err.println("IO error - KABOOOM!");
							System.exit(-10);
						}
					}
				}
//			}
//		}
		
		return bodycount;
	}
	
	// collection.contains(>= X)
	private static <T extends Object & Comparable<? super T>> boolean containsBiggerOrEqual(Collection<T> collection, T limit){
		
		if(collection == null || collection.isEmpty())
			return false;
		
		Iterator<T> it = collection.iterator();
		while(it.hasNext()){
			
			T element = it.next();
			
			if(element.compareTo(limit) >= 0)
				return true;
		}
		
		return false;
	}

}
