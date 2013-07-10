import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class trimmedAttack {

	public static void main(String[] args) {

		if(args.length != 4){
			StringBuilder s = new StringBuilder();
			s.append("Usage: \n");
			s.append("    java -jar raptorTrimer K initialLossLimit Epsilon\n");
			System.out.println(s.toString());
		}
		else{
			
			int K       = Integer.valueOf(args[0]);
			int limit   = Integer.valueOf(args[1]);
			int epsilon = Integer.valueOf(args[2]);
			
			int bodycount = trim(K, limit, epsilon);
			
			System.out.println("Total sniped symbols: "+bodycount);
		}
	}
	
	private static int trim(int K, int upperLimit, int Epsilon){
		
		// INITIALIZATION
		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int L = K + S + H;
		
		// repair lines
		byte[][] repairs = new byte[upperLimit][L];
		for(int repair = 0; repair < upperLimit; repair++){
			
			Tuple tuple = new Tuple(K, K + repair);
			Set<Integer> indexes = Encoder.encIndexes(K, tuple);

			for(Integer col : indexes)
				repairs[repair][col] = 1;
		}

		// constraint matrix
		byte[][] constraint_matrix = Encoder.generateConstraintMatrix(K, 1);
		
		// set of lines that can be targeted
		Integer[] lineNumbers = new Integer[K];
		for(int row = 0; row < K; row++)
			lineNumbers[row] = S+H+row;
		
		// set of ISIs that can be used as payload
		Integer[] ISIs = new Integer[upperLimit];
		for(int isi = 0; isi < upperLimit; isi++)
			ISIs[isi] = K + isi;
			
		// ALGORITHM
		for(int lines = K; lines > 0 && (upperLimit/K) > Epsilon; lines--){ // lets start with a big set of targetable lines, to try to reduce the upperlimit ASAP
			
			// combinations for lines
			ICombinatoricsVector<Integer> initialVector = Factory.createVector(lineNumbers);
			Generator<Integer> combLines = Factory.createSimpleCombinationGenerator(initialVector, lines);

			for(ICombinatoricsVector<Integer> combination : combLines) { // for each combination of lines
				
				// lines to be replaced
				List<Integer> targetLines = combination.getVector();
				
				// combinations for ISIs
				initialVector = Factory.createVector(ISIs);
				Generator<Integer> combISIs = Factory.createSimpleCombinationGenerator(initialVector, lines);

				for(ICombinatoricsVector<Integer> combination2 : combISIs) { // test all combinations of payloads in those lines
					
					// set of repair symbols to be replace the target lines 
					List<Integer> ISIpayload = combination2.getVector();
					
					// the upperlimit might have changed, we dont want to waste time with useless gaussian eliminations
					if(containsBiggerOrEqual(ISIpayload, upperLimit))
						continue;
					
					// build the decoding matrix (A), replacing the target lines with the payload
					byte[][] A = new byte[L][];
					Iterator<Integer> it = ISIpayload.iterator();
					
					for(int row = 0; row < L; row++){
					
						if(!targetLines.contains(row))
							A[row] = Arrays.copyOf(constraint_matrix[row], L);
						else{
							int repair = it.next();
							A[row] = Arrays.copyOf(repairs[repair - K], L);
						}
					}
					
					// reduce A to row echelon form
					Encoder.rowEchelonForm(A);
					
					// rank < L?
					if(A[L-1][L-1] == 0){
						
						// update the value of the body count
						upperLimit = Collections.max(ISIpayload);
					
						// persist trimmed attack
						try {

							File file = new File("results/attack_" + K + "_" + 0 + ".txt"); // FIXME for now overhead is 0

							if (file.exists())
								file.createNewFile();

							FileWriter fw = new FileWriter(file.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);

							bw.write(" - K: " + K + "\n - Overhead: " + 0 + "\n"); // FIXME for now overhead is 0
							bw.write("\n Target lines: " + Arrays.toString(targetLines.toArray(new Integer[targetLines.size()])) + 
									 "\n Payload ISIs: " + Arrays.toString(ISIpayload.toArray(new Integer[ISIpayload.size()])) +
									 "\n Body count  : " + upperLimit);

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
			}
		}
		
		return upperLimit;
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
