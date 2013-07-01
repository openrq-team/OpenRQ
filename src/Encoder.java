
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

public class Encoder {

	// TODO check patents for better default values
	public static final int MAX_PAYLOAD_SIZE = 1; //1; //512 // P'
	public static final int ALIGN_PARAM = 1;//1;  // Al // 4
	public static final int MAX_SIZE_BLOCK = 76800; // WS // B
	public static final int SSYMBOL_LOWER_BOUND = 1;//1; // SS // 8
	public static final int KMAX = 56403;
	public static final byte ALPHA = 2;

	private static int T = 1; // symbol size
	private int Z; // number of source blocks
	private int N; // number of sub-blocks in each source block
	private int F; // transfer length
	private int Kt; // total number of symbols required to represent the source data of the object
	private byte[] data;
	
	/* Simulation variables */
	private int LOSS;
	private int OVERHEAD;
	
	public static int INIT_REPAIR_SYMBOL = 0;
	
	public Encoder(byte[] file, int loss, int overhead){

		LOSS = loss;
		OVERHEAD = overhead;
		
		F = file.length;

		T = derivateT(MAX_PAYLOAD_SIZE);
		Kt = ceil((double)F/T);
		
		int N_max = floor((double)T/(SSYMBOL_LOWER_BOUND*ALIGN_PARAM));

		Z = derivateZ(N_max);
		/* FIXME SIMULATION for tests*/
		Z = 1;
		
		N = derivateN(N_max);

		if(F>Kt*T)
			data = new byte[F];
		else
			data = new byte[Kt*T];	

		// TODO check if this copy is really necessary, maybe it will be performance drawback
		for(int i=0; i<F; i++)
			data[i] = file[i];
	}

	private int derivateT(int pLinha){
		return pLinha;		
	}

	private int derivateZ(int N_max){

		return(ceil((double)Kt/SystematicIndices.KL(N_max, MAX_SIZE_BLOCK, ALIGN_PARAM, T)));
	}

	private int derivateN(int N_max){

		int n = 1;
		
		for(; n<=N_max && ceil((double)Kt/Z)>SystematicIndices.KL(n, MAX_SIZE_BLOCK, ALIGN_PARAM, T); n++);

		return n;
	}

	public static final int ceil(double x){
		return((int)Math.ceil(x));
	}

	public static final int floor(double x){		
		return((int)Math.floor(x));
	}

	public SourceBlock[] partition(){

		Partition KZ = new Partition(Kt, Z);
		int KL = KZ.get(1);
		int KS = KZ.get(2);
		int ZL = KZ.get(3);
		int ZS = KZ.get(4);

		Partition TN = new Partition(T/ALIGN_PARAM, N);
		int TL = TN.get(1);
		int TS = TN.get(2);
		int NL = TN.get(3);
		int NS = TN.get(4);

		SourceBlock[] object = new SourceBlock[Z];

		int i;
		int index_master = 0;
		for(i=0; i<ZL; i++){
			// Source block i

			byte[] aux2 = new byte[KL*T];

			int index_aux2 = 0;
			for(int k=0; k<KL; k++){

				int j = 0;
				int index_data  = index_master;

				for(; j<NL; j++, index_data+=KL*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TL*ALIGN_PARAM);
				}

				for(; j<N; j++, index_data+=KL*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TS*ALIGN_PARAM);
				}
				
				index_master += TL*ALIGN_PARAM;
			}

			object[i] = new SourceBlock(i, aux2, T, KL);
		}

		for(; i < Z; i++){
			// Source block i

			byte[] aux2 = new byte[KS*T];

			int index_aux2 = 0;
			for(int k=0; k<KS; k++){

				int j = 0;
				int index_data  = index_master;

				for(; j<NL; j++, index_data+=KS*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TL*ALIGN_PARAM);
				}

				for(; j<N; j++, index_data+=KS*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TS*ALIGN_PARAM);
				}

				index_master += TL*ALIGN_PARAM;
			}

			object[i] = new SourceBlock(i, aux2, T, KS);
		}

		return object;
	}

	public byte[] unPartition(SourceBlock[] object){

		byte[] data;

		if(F>Kt*T)
			data = new byte[(int) F];
		else
			data = new byte[(int) Kt*T];


		int index_master = 0;
		for(int i=0; i<object.length; i++){
			// Source block i

			SourceBlock sb = object[i];
			int Kappa   = sb.getK();
			int t_      = sb.getT();
			byte[] aux2 = sb.getSymbols();

			Partition TN = new Partition(t_/ALIGN_PARAM, N);
			int TL = TN.get(1);
			int TS = TN.get(2);
			int NL = TN.get(3);
			int NS = TN.get(4);

			for(int k=0; k<Kappa; k++){

				int j = 0;
				//int index_data  = (i*Kappa*T) + k*TL*ALIGN_PARAM; // always TL because you always start by the sub-block
				int index_data  = index_master + k*TL*ALIGN_PARAM; // always TL because you always start by the sub-block
				int index_aux2 = k*t_;

				for(; j<NL; j++, index_data+=Kappa*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(aux2, index_aux2, data, index_data, TL*ALIGN_PARAM);
				}

				for(; j<NS; j++, index_data+=Kappa*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(aux2, index_aux2, data, index_data, TS*ALIGN_PARAM);
				}
			}
		
			index_master += Kappa * T;
		}

		return data;		
	}

	public SourceBlock[] decode(EncodingPacket[] encoded_blocks) throws SingularMatrixException {

		int num_blocks = encoded_blocks.length;
		
		SourceBlock[] recovered = new SourceBlock[num_blocks];
		
		// Decode each block
		for(int source_block_index = 0; source_block_index<encoded_blocks.length; source_block_index++){
			
			EncodingPacket eb = encoded_blocks[source_block_index];
			EncodingSymbol[] enc_symbols = eb.getEncoding_symbols();
			int num_symbols = enc_symbols.length;
			int K = eb.getK();
			int T = eb.getT();
			int kLinha = SystematicIndices.ceil(K);
			int Ki = SystematicIndices.getKIndex(K);
			int S = SystematicIndices.S(Ki);
			int H = SystematicIndices.H(Ki);
			int L = kLinha + S + H;
			
			// Analyze received symbols "topology"
			Set<Integer> missing_symbols = new TreeSet<Integer>();
			Set<EncodingSymbol> source_symbols = new TreeSet<EncodingSymbol>();
			Set<EncodingSymbol> repair_symbols = new TreeSet<EncodingSymbol>();
			int num_source_symbols = 0;
			int num_repair_symbols = 0;
			int missing_delta_index = 0;
			
			for(int symbol=0; symbol<num_symbols; symbol++){
				
				EncodingSymbol enc_symbol = enc_symbols[symbol];
				
				if(enc_symbol == null){ // TODO think about this
					
					if(symbol < K){
						missing_symbols.add(symbol);
						missing_delta_index++;
					}

					continue;
				}
								
				if(enc_symbol.getESI() < K){ // acho que afinal nao //isto aqui falha se tiver a faltar o ultimo source symbol
					
					if(enc_symbol.getESI() != symbol){ //+ missing_delta_index){
						
						missing_symbols.add(symbol + missing_delta_index);						
						missing_delta_index++;
					}
					
					source_symbols.add(enc_symbol);
					num_source_symbols++;
				}
				else{
					
					repair_symbols.add(enc_symbol);
					num_repair_symbols++;
				}
			}
			
			/*
			// Print topology
			StringBuilder st = new StringBuilder();
			st.append("Symbols topology: \n");
			st.append("# Source: ");
			st.append(num_source_symbols);
			st.append("\n# Repair: ");
			st.append(num_repair_symbols);
			st.append("\n# Missing: ");
			st.append(missing_delta_index);
			st.append("\n Missing indexes:\n");
			for(Integer i : missing_symbols)
				st.append(i + ", ");
			System.out.println(st.toString());
			*/
			
			if(num_repair_symbols < missing_delta_index) throw new RuntimeException("Not enough repair symbols received."); // TODO shouldnt be runtime exception, too generic
			
			int overhead = num_repair_symbols - missing_symbols.size();
			int M = L + overhead;
			
			Map<Integer, byte[]> esiToLTCode = new TreeMap<Integer, byte[]>();
			
			byte[] decoded_data = new byte[K*T];
			
			// All source symbols received :D
/*			if(num_source_symbols == K){
				
				// Collect all payloads from the source symbols				
				for(EncodingSymbol enc_symbol : source_symbols){
					
					System.arraycopy(enc_symbol.getData(), 0, decoded_data, enc_symbol.getESI() * T, T);
				}
				
				recovered[source_block_index] = new SourceBlock(eb.getSBN(), decoded_data, T, K);
			}	
			
			// Not so lucky
			else{
			*/
				byte[][] constraint_matrix = new byte[M][];

				// Generate original constraint matrix
				byte[][] lConstraint = generateConstraintMatrix(kLinha, T);
				/*//FIXME ATTACK */
/*				boolean independent = true;
				for(int esi = 115; independent; esi++){
					
					Set<Integer> indexes = encIndexes(kLinha, new Tuple(kLinha, kLinha+esi));
					
					byte[] newLine = new byte[L];
					for(Integer col : indexes)
							newLine[col] = 1;
					
					lConstraint[L-1] = newLine;
	
					byte[][] A = (new Matrix(lConstraint)).getData();
					
					reduceToRowEchelonForm(A, 0, L, 0, L);
					
					(new Matrix(A)).show();
					
					if(!validateRank(A, 0, 0, L, L, L))
						independent = false;
				}
*/
				
				for(int row = 0; row < L; row++)
					constraint_matrix[row] = lConstraint[row];
				
				// initialize D
				byte[][] D = new byte[M][T];
				
				if(num_source_symbols != 0){

					Iterator<EncodingSymbol> it = source_symbols.iterator();

					do{
						EncodingSymbol source_symbol = (EncodingSymbol) it.next();

						D[source_symbol.getESI() + S + H] = source_symbol.getData();
						
					}while(it.hasNext());
				}

				/*
				System.out.println("---- A ---- (initial)");
				(new Matrix(constraint_matrix)).show();
				System.out.println("---------------------");
				
				System.out.println("---- D ---- (initial)");
				(new Matrix(D)).show();
				System.out.println("---------------------");				
				*/

				Iterator<EncodingSymbol> repair_symbol = repair_symbols.iterator();
				
				// Identify missing source symbols and replace their lines with "repair lines"
				for(Integer missing_ESI : missing_symbols){
					
					EncodingSymbol repair = (EncodingSymbol) repair_symbol.next();
					int row = S+H+missing_ESI;
					
					// Substituir S + H + missing_ESI pela linha equivalente ao encIndexes do repair simbolo
					Set<Integer> indexes = encIndexes(kLinha, new Tuple(kLinha, repair.getISI(K)));
					
					byte[] newLine = new byte[L];
					
					for(Integer col : indexes)
							newLine[col] = 1;
					
					esiToLTCode.put(missing_ESI, constraint_matrix[row]);
					constraint_matrix[row] = newLine;
					
					System.out.println("ISI: "+repair.getISI(K)+" | "+row+") "+Arrays.toString(newLine));
					
					// Fill in missing source symbols in D with the repair symbols
					D[row] = repair.getData();
				}

				byte[][] B = {
						
						
						// SISTEMA IMPOSSIVEL 
						{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
						{0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0},
						{0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0},
						{0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0},
						{0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0},
						{0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0}
						
						/*
						{0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0},
						{0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0},
						{1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0},
						{0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0},
						{1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0},
						{0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}*/
				};
				
				constraint_matrix[20] = B[0];
				constraint_matrix[21] = B[1];
				constraint_matrix[23] = B[2];
				constraint_matrix[24] = B[3];
				constraint_matrix[25] = B[4];
				constraint_matrix[26] = B[5];

				/* // TESTE c(Ab)
				 (new Matrix(constraint_matrix)).show();
	 
				 byte[][] Ab = new byte[L+1][L];
				 
				 for(int row = 0; row < L; row++){
					 
					 for(int col = 0; col < L; col++){
						 
						 Ab[row][col] = constraint_matrix[col][row];
					 }
					 
					 Ab[L][row] = D[row][0];
				 }

				 (new Matrix(Ab)).show();
				 
				 reduceToRowEchelonForm(Ab, 0, L+1, 0, L);
				 
				 (new Matrix(Ab)).show();

				 reduceToRowEchelonForm(constraint_matrix, 0, L, 0, L);
				 
				 (new Matrix(constraint_matrix)).show();
				 
				 System.out.println(validateRank(Ab, 0, 0, L+1, L, L));
				 
				 */
				
				
				// add values for overhead symbols					
				for(int row = L; row < M; row++){

					EncodingSymbol repair = (EncodingSymbol) repair_symbol.next();

					// Generate the overhead lines
					Tuple tuple = new Tuple(K, repair.getISI(K));

					Set<Integer> indexes = encIndexes(K, tuple);

					byte[] newLine = new byte[L];
					for(Integer col : indexes)
						newLine[col] = 1;
					
					constraint_matrix[row] = newLine;
					
					// update D with the data for that symbol
					D[row] = repair.getData();
				}
				
				/*
				System.out.println("---- A ---- (recovered)");
				(new Matrix(constraint_matrix)).show();
				System.out.println("---------------------");
				
				System.out.println("---- D ---- (recovered)");
				(new Matrix(D)).show();
				System.out.println("---------------------");
				*/
				
				byte[] intermediate_symbols = generateIntermediateSymbols(constraint_matrix, D, T, kLinha);
				
				/*
				System.out.println("---- C ----");
				for(int bite=0; bite<intermediate_symbols.length; bite++){
					if(bite % T == 0) System.out.println("");
					System.out.printf("| %02X |", intermediate_symbols[bite]);
				}
				System.out.println("---------------------");
				*/
				
				// Recover missing source symbols
				for(Map.Entry<Integer, byte[]> missing : esiToLTCode.entrySet()){
					
					byte[] original_symbol = multiplyByteLineBySymbolVector(missing.getValue(), intermediate_symbols, T);
					//byte[] original_symbol = multiplyByteLineBySymbolVector(missing.getValue(), D, T);
					
					System.arraycopy(original_symbol, 0, decoded_data, missing.getKey() * T, T);
				}
			
				// Merge with received source symbols
				for(EncodingSymbol enc_symbol : source_symbols){
					
					System.arraycopy(enc_symbol.getData(), 0, decoded_data, enc_symbol.getESI() * T, T);
				}
				
				/*
				System.out.println("\n\n RECOVERED");

				for(int g=0; g<decoded_data.length; g++){
					if(g % T == 0) System.out.println("");
					System.out.printf("| %02X |", decoded_data[g]);
				}
				System.out.println("");
				 */
				
				recovered[source_block_index] = new SourceBlock(eb.getSBN(), decoded_data, T, K);
			//}			
		}				

		return recovered;
	}
	
	// A * x = b
	public static byte[] supahGauss(byte[][] A, byte[][] b) throws SingularMatrixException{
		
		if (A.length != A[0].length || b.length != A.length)
			throw new RuntimeException("Illegal matrix dimensions.");
		
		int num_cols = b[0].length;
		//byte[][] x = new byte[b.length][b[0].length];
		byte[] x = new byte[b.length * num_cols];
		
		(new Matrix(A)).show();
		
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
				(new Matrix(b)).show();
				(new Matrix(A)).show();
            	throw new SingularMatrixException("LINHA QUE DEU SINGULAR: "+row);
            }
 
            // pivot within A and b
            for(int i=row+1; i<ROWS; i++) {
            	
            	byte alpha = OctectOps.division(A[i][row], A[row][row]);
            	
            	temp = OctectOps.betaProduct(alpha, b[row]);
            	
            	b[i] = xorSymbol(b[i], temp);
            	
            	
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
            	
            	sum = xorSymbol(sum, temp);
            }
            
            byte[] temp = xorSymbol(b[i], sum);
            
            //x[i] = OctectOps.betaDivision(temp, A[i][i]);
            for(int bite = 0; bite < num_cols; bite++){
            	
            	x[(i*num_cols) + bite] = OctectOps.division(temp[bite], A[i][i]);
            }
        }
		
		return x;
	}
	
	public EncodingPacket[] encode(SourceBlock[] object) throws SingularMatrixException{

		int num_src_symbols = object.length;
		EncodingPacket[] encoded_blocks = new EncodingPacket[num_src_symbols];
		
		for(int source_block_index = 0; source_block_index<num_src_symbols; source_block_index++){
			
			SourceBlock sb = object[source_block_index];
			int SBN = sb.getSBN();
			byte[] ssymbols = sb.getSymbols();
			int K = sb.getK();
			int kLinha = SystematicIndices.ceil(K);
			
			/*//FIXME SIMULATION */
			int num_to_lose_symbols = ceil(0.01*LOSS*K);
			int num_repair_symbols = OVERHEAD + num_to_lose_symbols;
//			System.out.println("To lose: "+num_to_lose_symbols +"\nRepair: "+num_repair_symbols);
			
			EncodingSymbol[] encoded_symbols = new EncodingSymbol[K + num_repair_symbols]; // FIXME arbitrary size ASAP /*//FIXME SIMULATION */
			
			// First encoding step
			byte[] intermediate_symbols = generateIntermediateSymbols(sb);
			
			// Second encoding step
			// Sending original source symbols
			int source_symbol;
			int source_symbol_index;

			/*//FIXME SIMULATION */
			Random lost = new Random(System.currentTimeMillis() + System.nanoTime());
			// Select indexes to be lost.
			Set<Integer> oopsi_daisy = new TreeSet<Integer>();
	
			while(num_to_lose_symbols > 0){
				
				int selected_index = lost.nextInt(K);
				
				if(!oopsi_daisy.contains(selected_index)){
					oopsi_daisy.add(selected_index);
					num_to_lose_symbols--;
				}
				else
					continue;
			}
			
			for(source_symbol = 0, source_symbol_index = 0; source_symbol<K; source_symbol++, source_symbol_index+=sb.getT()){

				/*//FIXME SIMULATION */
				if(oopsi_daisy.contains(source_symbol)) continue;
				
				encoded_symbols[source_symbol] = new EncodingSymbol(SBN,source_symbol, Arrays.copyOfRange(ssymbols, source_symbol_index, (int) (source_symbol_index+sb.getT())));
			}
			
			/*//FIXME ATTACK */
/*			boolean independent = true;
			byte[] enc_data2 = null;
			int ki = SystematicIndices.getKIndex(kLinha);
			int S = SystematicIndices.S(ki);
			int H = SystematicIndices.H(ki);
			byte[][] constraint_matrix = generateConstraintMatrix(kLinha, sb.getT());
			dependence:
			for(int esi=0; independent; esi++){
				
				for(int row = 0; row < S+H; row++){

					enc_data2 = enc(kLinha, intermediate_symbols, new Tuple(kLinha, kLinha+esi));
					byte[] auxRow = constraint_matrix[row];
	
					byte[][] A = new byte[2][enc_data2.length];
					A[0] = enc_data2;
					A[1] = auxRow;
					byte[][] D = {{0}, {0}};
					byte[] result = supahGauss(A,D);
					
					if(result[0] != 0 || result[1] != 0){ // TEMOS DEPENDENCIA
						
						independent = false;
						break dependence;
					}
				}
				
				for(int row = S+H; row < K-1; row++){

					enc_data2 = enc(kLinha, intermediate_symbols, new Tuple(kLinha, kLinha+esi));
					byte[] auxRow = new byte[enc_data2.length];
					
					Tuple tuple = new Tuple(kLinha, row);
					Set<Integer> indexes = encIndexes(K, tuple);
					for(Integer j : indexes)
						auxRow[j] = 1;	
					
					byte[][] A = new byte[2][enc_data2.length];
					A[0] = enc_data2;
					A[1] = auxRow;
					byte[][] D = {{0}, {0}};
					byte[] result = supahGauss(A,D);
					
					if(result[0] != 0 || result[1] != 0){ // TEMOS DEPENDENCIA
						
						independent = false;
						break dependence;
					}
				}
			}

			encoded_symbols[K] = new EncodingSymbol(SBN,kLinha, enc_data2);
			*/
			// Generating/sending repair symbols
			for(int repair_symbol = INIT_REPAIR_SYMBOL; repair_symbol<num_repair_symbols + INIT_REPAIR_SYMBOL; repair_symbol++){
				
				int	isi = kLinha + repair_symbol;
				int esi = K + repair_symbol;

				byte[] enc_data = enc(kLinha, intermediate_symbols, new Tuple(kLinha, isi));
				
				encoded_symbols[source_symbol + repair_symbol] = new EncodingSymbol(SBN,esi, enc_data);
			}
			
			/*//FIXME ATTACK */
/*			EncodingSymbol[] aux = new EncodingSymbol[K + num_repair_symbols];
			for(int i = 0; i < K + num_repair_symbols; i++)
				if(i<K)
					aux[i] = encoded_symbols[i];
				else
					aux[i] = encoded_symbols[i+INIT_REPAIR_SYMBOL];
			encoded_symbols = aux;
	*/		
			encoded_blocks[source_block_index] = new EncodingPacket(SBN, encoded_symbols, K, sb.getT());
		}

		return encoded_blocks;
	}
	
	private static void initializeG_LPDC2(byte[][] constraint_matrix, int S, int P, int W){
		
		for(int row=0; row<S; row++){

			// Consecutives 1's modulo P
			constraint_matrix[row][( row    %P) + W] = 1;
			constraint_matrix[row][((row+1) %P) + W] = 1;
		}
	}
	
	private static void initializeG_LPDC1(byte[][] constraint_matrix, int B, int S){
		
		int circulant_matrix = -1;
		
		for(int col=0; col<B; col++){

			int circulant_matrix_column = col % S;

			if(circulant_matrix_column != 0){ 

				// cyclic down-shift	
				constraint_matrix[0][col] = constraint_matrix[S-1][col-1];

				for(int row=1; row<S; row++){

					constraint_matrix[row][col] = constraint_matrix[row-1][col-1];
				}
			}
			else{ // if 0, then its the first column of the current circulant matrix

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
	
	private static void initializeIs(byte[][] constraint_matrix, int S, int B){
	
		for(int row=0; row<S; row++){
			for(int col=0; col<S; col++){

				if(col != row)
					continue;
				else
					constraint_matrix[row][col+B] = 1;
			}
		}
	}
	
	private static void initializeIh(byte[][] constraint_matrix, int W, int U, int H, int S){
		
		int lower_limit_col = W + U;

		for(int row=0; row<H; row++){
			for(int col=0; col<H; col++){

				if(col != row)
					continue;
				else
					constraint_matrix[row+S][col+lower_limit_col] = 1;
			}
		}
	}
	
	private static byte[][] generateMT(int H, int K, int S){
		
		byte[][] MT = new byte[H][K+S];

		for(int row=0; row<H; row++){
			for(int col=0; col<K+S-1; col++){

				if(row != (int)(Rand.rand(col+1, 6, H)) && row != (((int)(Rand.rand(col+1, 6, H)) + (int)(Rand.rand(col+1, 7, H-1)) + 1) % H))
					continue;
				else
					MT[row][col] = 1;
			}
		}
		
		for(int row=0; row<H; row++)
			MT[row][K+S-1] = OctectOps.getExp(row);
		
		return(MT);
	}
	
	private static byte[][] generateGAMMA(int K, int S){
		
		byte[][] GAMMA = new byte[K+S][K+S];

		for(int row = 0; row<K+S; row++){
			for(int col = 0; col<K+S; col++){

				if(row >= col)
					GAMMA[row][col] = OctectOps.getExp((row-col) % 256);
				else
					continue;				
			}	
		}
		
		return GAMMA;
	}
	
	public static byte[][] multiplyMatrices(byte[][] A, byte[][] B){
		
        if (A[0].length != B.length) throw new RuntimeException("Illegal matrix dimensions.");
        
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
		
	public static byte[][] multiplyMatrices(byte[][] A, int first_rowA, int first_colA, int last_rowA, int last_colA, 
											byte[][] B, int first_rowB, int first_colB, int last_rowB, int last_colB){
		
        if ((last_colA  - first_colA) != (last_rowB - first_rowB)) throw new RuntimeException("Illegal matrix dimensions.");
        
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
	
	// A.B = C
	public static void multiplyMatrices(byte[][] A, int first_rowA, int first_colA, int last_rowA, int last_colA, 
										byte[][] B, int first_rowB, int first_colB, int last_rowB, int last_colB,
										byte[][] C, int first_rowC, int first_colC, int last_rowC, int last_colC){

		if ((last_colA  - first_colA) != (last_rowB - first_rowB)) throw new RuntimeException("Illegal matrix dimensions.");

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
	
	public static byte[] multiplyByteLineBySymbolVector(byte[] line, byte[] vector, int symbol_size){
		
		if((line.length * symbol_size) != vector.length) throw new RuntimeException("Illegal line/vector dimensions.");
		
		byte[] result = new byte[symbol_size];
		
		for(int octet=0; octet<symbol_size; octet++){

			for(int colRow=0; colRow<line.length; colRow++){
				
				byte temp = OctectOps.product(line[colRow], vector[(colRow * symbol_size) + octet]);
				
				result[octet] = (byte) (result[octet] ^ temp);
			}
		}
		
		return result;
	}
	
	public static byte[] multiplyByteLineBySymbolVector(byte[] line, byte[][] vector, int symbol_size){
		
		if((line.length * symbol_size) != vector.length) throw new RuntimeException("Illegal line/vector dimensions.");
		
		byte[] result = new byte[symbol_size];
		
		for(int octet=0; octet<symbol_size; octet++){

			for(int colRow=0; colRow<line.length; colRow++){
				
				byte temp = OctectOps.product(line[colRow], vector[colRow][octet]);
				
				result[octet] = (byte) (result[octet] ^ temp);
			}
		}
		
		return result;
	}
	
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
	
	private static void initializeG_ENC(byte[][] constraint_matrix, int S, int H, int L, int K){
		
		for(int row=S+H; row<L; row++){

			Tuple tuple = new Tuple(K, row-S-H);

			Set<Integer> indexes = encIndexes(K, tuple);

			for(Integer j : indexes){

				constraint_matrix[row][j] = 1;	
			}
		}
	}
	
	public static byte[][] generateConstraintMatrix(int K, int T){
		
		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		int L = K + S + H;
		int P = L - W;
		int U = P - H;
		int B = W - S;
		
		byte[][] constraint_matrix = new byte[L][L]; // A
		
		// Upper half
		// Initialize G_LPDC2
		initializeG_LPDC2(constraint_matrix, S, P, W);

		// Initialize G_LPDC1
		initializeG_LPDC1(constraint_matrix, B, S);

		// Initialize I_s			
		initializeIs(constraint_matrix, S, B);

		// Botton half
		// Initialize I_h
		initializeIh(constraint_matrix, W, U, H, S);

		// Initialize G_HDPC
		// MT
		byte[][] MT = generateMT(H, K, S);

		// GAMMA
		byte[][] GAMMA = generateGAMMA(K, S);

		// G_HDPC = MT * GAMMA
		byte[][] G_HDPC = multiplyMatrices(MT, GAMMA);

		// Initialize G_HDPC
		for(int row=S; row<S+H; row++)
			for(int col=0; col<W+U; col++)
				constraint_matrix[row][col] = G_HDPC[row-S][col];

		// Initialize G_ENC
		initializeG_ENC(constraint_matrix, S, H, L, K);
		
		return constraint_matrix;
	}
	
	private byte[] generateIntermediateSymbols(byte[][] A, byte[][] D, int symbol_size, int K) throws SingularMatrixException{
		
		byte[] C = supahGauss(A, D);
		
		// PInactivation Decoding
		//byte[] C = PInactivationDecoding(A, D, symbol_size, K);
		
		return C;
	}
	
	public static byte[] PInactivationDecoding(byte[][] A, byte[][] D, int symbol_size, int K) throws SingularMatrixException {

		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		int L = K + S + H;
		int P = L - W;
		int M = A.length;
		
		
		/* SIMULATION */
/*		K = 3;	S = 1;	H = 1;	W = 4;	L = 5;	P = 1;	M = 5; T = 3;
		
		byte[][] zzz = {
				{1, 0, 1, 1, 1},
				{0, 1, 0, 1, 1},
				{1, 1, 1, 0, 1},
				{1, 0, 1, 0, 0},
				{1, 1, 0, 0, 1}
		}; 	A = zzz;
		
		byte[][] sss = {
				{1, 0, 0},
				{1, 0, 1},
				{1, 1, 0},
				{1, 1, 1},
				{0, 1, 1}	
		}; D = sss;		
		/* END */
		
		// initialize c and d
		int[] c = new int[L];
		int[] d = new int[M];
		
		for(int i = 0; i < L; i++){
			c[i] = i;
			d[i] = i;
		}
		
		for(int i = L; i < M; i++){
			d[i] = i;
		}
		
		// Allocate X and copy A into X
		byte[][] X = new byte[M][L];
		for(int row = 0; row < M; row++)
			System.arraycopy(A[row], 0, X[row], 0, L);

		int i = 0, u = P;
		
		/* PRINTING BLOCK */
		System.out.println("--------- A ---------");
		(new Matrix(A)).show();
		System.out.println("---------------------");
		/* END OF PRINTING */
		
		/* 
		 * First phase 
		 * */
		int chosenRowsCounter = 0;
		int nonHDPCRows = S + H;

		Map<Integer, Integer> originalDegrees = new HashMap<Integer, Integer>();

		/*
		 *  TODO Optimização: ao inves de percorrer isto todas as vezes, ver só as linhas quer perderam
		 *  um non-zero, e subtrair ao 'r' original. Como lidar com as novas dimensoes de V? 
		 */
		
		while(i + u != L){
			
			/* PRINTING BLOCK */
			System.out.println("STEP: "+i);
			/* END OF PRINTING */
			
			int r = L+1, rLinha = 0	;			
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

				/*// TODO testar tempos com isto o.O
				if(i != 0){

					if(nonZeros == 2 && (row < S || row >= S+H)) // FIXME do some branch prediction
						rows.put(row, new Row(row, nonZeros, originalDegrees.get(d[row]), edges));
					else
						rows.put(row, new Row(row, nonZeros, originalDegrees.get(d[row])));	

				}
				else{

					if(nonZeros == 2 && (row < S || row >= S+H))
						rows.put(row, new Row(row, nonZeros, degree, edges));
					else
						rows.put(row, new Row(row, nonZeros, degree));	

					originalDegrees.put(row, degree);
				}
				*/
				
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
			
			/* PRINTING BLOCK */
			System.out.println("r: "+r);
			/* END OF PRINTING */

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
					
					/* 
					 * TODO Optimizaçao: - tentar fazer sem este while... nao ha de ser dificil
					 * 					 - já procurei, e ha algoritmos optimizados para achar connected components
					 * 					é só depois ver qual o maior...
					 */
					
			//		while(maximumSize != 0 && !found){ 
					
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
								
								maximumSize 	  = visited.size();
								greatestComponent = visited;
							}
							
						/*	
							// is it big?
							if(visited.size() >= maximumSize) // yes it is
								found = true;
							*/
						}/*
						
						maximumSize--;
					}*/
					
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
			
			/*  PRINT ROWS  */
/*			for(Row row : rows.values()){
				System.out.println("id: "+row.id+"  nZ: "+row.nonZeros+"  deg: "+row.degree);				
			}
			/* END OF PRINT */
			
			// 'rLinha' is the chosen row
			Row chosenRow = rows.get(rLinha);
			/* PRINTING BLOCK */
			System.out.println("----- CHOSEN ROW -----");
			System.out.println("id : "+chosenRow.id);
			System.out.println("nZ : "+chosenRow.nonZeros);
			System.out.println("deg: "+chosenRow.degree);
			System.out.println("----------------------");
			/* END OF PRINTING */		
			
			if(rLinha != i){
				 
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
				
				/* PRINTING BLOCK */
				System.out.println("TROCA DE LINHA: "+i+" by "+ rLinha);
				System.out.println("--------- A ---------");
				(new Matrix(A)).show();
				System.out.println("---------------------");
				/* END OF PRINTING */			
			}
			
			
			// re-order columns
			if(chosenRow.degree > 0){
				Stack<Integer> nonZeros = new Stack();
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
				
					// decoding process - swap i and coluna in c
					int auxIndex = c[i];
					c[i] = c[coluna];
					c[coluna] = auxIndex;
				
					/* PRINTING BLOCK */
					System.out.println("TROCA DE COLUNA: "+i+" by "+ coluna);
					System.out.println("--------- A ---------");
					(new Matrix(A)).show();
					System.out.println("---------------------");
					/* END OF PRINTING */
				}
				else
					nonZeros.remove((Integer)i);
				
				for(int remainingNZ = nonZeros.size(); remainingNZ > 0; remainingNZ--){		

					coluna = nonZeros.pop();
					
					// swap
					swapColumns(A, coluna, L-u-remainingNZ);
					swapColumns(X, coluna, L-u-remainingNZ);

					// decoding process - swap coluna with L-u-remainingNZ in c
					int auxIndex = c[L-u-remainingNZ];
					c[L-u-remainingNZ] = c[coluna];
					c[coluna] = auxIndex;
					
					/* PRINTING BLOCK */
					System.out.println("TROCA DE COLUNA: "+(L-u-remainingNZ)+" by "+ coluna);
					System.out.println("--------- A ---------");
					(new Matrix(A)).show();
					System.out.println("---------------------");
					/* END OF PRINTING */
					
				}
			
				// beta/alpha gewdness
				byte alpha = A[i][i];
				
				for(int row = i+1; row < M; row++){
					
					if(A[row][i] == 0)
						continue;
					else{ 				// TODO Queue these row operations for when (if) the row is chosen - RFC 6330 @ Page 35 1st Par.
						
						// beta/alpha
						byte beta   = A[row][i];
						byte balpha = OctectOps.division(beta, alpha);
						
						// multiplication 
						byte[] product = OctectOps.betaProduct(balpha, A[i]);
						
						// addition 
						A[row] = xorSymbol(A[row], product);

						// decoding process - (beta * D[d[i]]) + D[d[row]]
						product = OctectOps.betaProduct(balpha, D[d[i]]);
						D[d[row]] = xorSymbol(D[d[row]], product);
						
						/* PRINTING BLOCK */
/*						System.out.println("ELIMINATING");
						System.out.println("--------- A ---------");
						(new Matrix(A)).show();
						System.out.println("---------------------");
						/* END OF PRINTING */
					}
				}
			}
			
			/* PRINTING BLOCK */
			System.out.println("END OF STEP "+i);
			System.out.println("--------- A ---------");
			(new Matrix(A)).show();
			System.out.println("---------------------");
			/* END OF PRINTING */

			// update 'i' and 'u'
			i++;
			u += r-1;
		}
		// END OF FIRST PHASE
		
		/* 
		 * Second phase 
		 * */
		// X is now ixi

		reduceToRowEchelonForm(A, i, M, L-u, L, d, D);

		/* PRINTING BLOCK */
		System.out.println("GAUSSIAN U_lower");
		System.out.println("M: "+M+"\nL: "+L+"\ni: "+i+"\nu: "+u);
		System.out.println("--------- A ---------");
		(new Matrix(A)).show();
		System.out.println("---------------------");
		/* END OF PRINTING */
		
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
		
		// decoding process
		byte[][] reordereD = new byte[L][];
		
		for(int index = 0; index < L; index++) 
			reordereD[index] = D[d[index]]; // reorder D
		
		for(int row = 0; row < i; row++) // multiply X by D
			D[d[row]] = multiplyByteLineBySymbolVector(X[row], i, reordereD, T);
		
		/*  TEST BLOCK  */
/*		for(int row = 0; row < i; row++){
			for(int col = 0; col < i; col++){
				if(X[row][col] != A[row][col]){
					System.out.println("ERRO NA FASE 3 (row: "+row+" ; col: "+col+" )");
					System.out.println("--------- X ---------");
					(new Matrix(X)).show();
					System.out.println("---------------------");
					System.out.println("--------- A ---------");
					(new Matrix(A)).show();
					System.out.println("---------------------");
					System.exit(-543534);
				}
			}
		}
		/* END OF BLOCK */
		
		/* PRINTING BLOCK */
/*		System.out.println("SPARSED U_upper");
		System.out.println("--------- A ---------");
		(new Matrix(A)).show();
		System.out.println("---------------------");
/*		System.out.println("--------- X ---------");
		(new Matrix(X)).show();
		System.out.println("---------------------");
		/* END OF PRINTING */
		
		/* 
		 * Fourth phase 
		 * */

		for(int row=0; row < i; row++){														
			for(int j = i; j < L; j++){														
				if(A[row][j] != 0){															

					byte b    = A[row][j];
					A[row][j] = 0;

					// decoding process - (beta * D[d[j]]) + D[d[row]]
					byte[] product = OctectOps.betaProduct(b, D[d[j]]);
					D[d[row]] = xorSymbol(D[d[row]], product);
				}
			}
		}

		/* PRINTING BLOCK */
/*		System.out.println("ZEROED U_upper");
		System.out.println("--------- A ---------");
		(new Matrix(A)).show();
		System.out.println("---------------------");
		/* END OF PRINTING */
		
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
				
				// decoding process - D[d[j]] / beta
				D[d[j]] = OctectOps.betaDivision(D[d[j]], beta);
			}
			
			for(int l = 0; l < j; l++){
				
				if(A[j][l] != 0){
					
					byte beta = A[j][l];
					byte[] product = OctectOps.betaProduct(beta, A[l]);
					
					A[j] = xorSymbol(A[j], product);

					// decoding process - D[d[j]] + (A[j][l] * D[d[l]])
					product = OctectOps.betaProduct(beta, D[d[l]]);
					D[d[j]] = xorSymbol(D[d[j]], product);
					
				}
			}
		}
		
		/* PRINTING BLOCK */
/*		System.out.println("IDENTITY");
		System.out.println("--------- A ---------");
		(new Matrix(A)).show();
		System.out.println("---------------------");
		
		if(!checkIdentity(A,L)) System.exit(231);
		/* END OF PRINTING */
		
		byte[] C = new byte[L*T];
		for(int symbol = 0; symbol < L; symbol++)
			System.arraycopy(D[d[symbol]], 0, C, c[symbol]*T, T);
			
		return C;
	}
	
	private static boolean checkIdentity(byte[][] A, int L) {
		
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
			
			// singular or nearly singular
            if (A[row][row] == 0) {
				System.err.println("LINHA QUE DEU SINGULAR: "+row);
            	throw new SingularMatrixException("LINHA QUE DEU SINGULAR: "+row);
            }
 
            // pivot within A
            for(int i=row+1; i<ROWS; i++) {
            	
            	byte alpha = OctectOps.division(A[i][row], A[row][row]);
            	
            	for(int j=row; j<ROWS; j++) {
            	
            		byte aux = OctectOps.product(alpha, A[row][j]);
            		
            		A[i][j] = OctectOps.subtraction(A[i][j], aux);
            	}
            }
		}
	}
	
	private static void reduceToRowEchelonForm(byte[][] A, int first_row, int last_row, int first_col, int last_col, int[] d, byte[][] D){
		
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
	
	private static void swapColumns(byte[][] matrix, int a, int b){
		
		// check sizes and limits and whatnot bla bla bla
		
		byte auxPos;
		
		for(int row = 0; row < matrix.length; row++){
			// swap
			auxPos = matrix[row][a];
			matrix[row][a] = matrix[row][b];
			matrix[row][b] = auxPos;
		}
	}

	private void swapRows(byte[][] matrix, int a, int b){

		// check sizes and limits and whatnot bla bla bla

		byte[] auxRow = matrix[a];
		matrix[a] = matrix[b];
		matrix[b] = auxRow;
	}
	
	private byte[] generateIntermediateSymbols(SourceBlock sb) throws SingularMatrixException{
		
		byte[] ssymbols = sb.getSymbols();

		int K = SystematicIndices.ceil((int)sb.getK());
		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int L = K + S + H;

		int k = sb.getK();
		int t = sb.getT();
		
		/* Generate LxL Constraint  Matrix*/
		byte[][] constraint_matrix = generateConstraintMatrix(K, t); // A
		//(new Matrix(constraint_matrix)).show();
		// D
		byte[][] D = new byte[L][t];
		for(int row=S+H, index=0; row<k+S+H; row++, index+=t)
			D[row] = Arrays.copyOfRange(ssymbols, index, (index+t));

		// Solve system of equations
		byte[] C = PInactivationDecoding(constraint_matrix, D, t, K);
		//byte[] C = supahGauss(constraint_matrix, D);
		
		return C;
	}

	public static final byte convert(byte in){

		byte hex1 = (byte) (in << 4);
		byte hex2 = (byte) ((in >>> 4) & 0xF);

		return (byte) (hex2 | hex1);
	}

	public boolean symbolIsZero(byte[] symbol){

		for(byte b : symbol)
			if(b != 0)
				return false;

		return true;
	}

	public Map<SourceBlock, Tuple[]> generateTuples(SourceBlock[] object){

		Map<SourceBlock, Tuple[]> sbTuple = new HashMap<SourceBlock, Tuple[]>();

		for(int i=0; i<object.length; i++){
			SourceBlock sb = object[i];

			int kLinha = sb.getSymbols().length;
			Tuple[] tuples = new Tuple[kLinha];

			for(int j=0; j<kLinha; j++){
				tuples[j] = new Tuple(kLinha, j);
			}

			sbTuple.put(sb, tuples);
		}

		return sbTuple;
	}

	public byte[] enc(int K, byte[] C, Tuple tuple){

		int Ki  = SystematicIndices.getKIndex(K);
		int S   = SystematicIndices.S(Ki);
		int H   = SystematicIndices.H(Ki);
		int W   = SystematicIndices.W(Ki);
		long L  = K + S + H;
		long P  = L - W;
		int P1  = (int)ceilPrime(P);
		long d  = tuple.getD();
		int a   = (int)tuple.getA();
		int b   = (int)tuple.getB();
		long d1 = tuple.getD1();
		int a1  = (int)tuple.getA1();
		int b1  = (int)tuple.getB1();

		byte[] result = Arrays.copyOfRange(C, (int)(b*T), (int)((b+1)*T));

		for(long j=0; j<d; j++){
			b = (b+a) % W;
			result = xorSymbol(result, 0, C, b*T, T);
		}

		while(b1 >= P){
			b1 = (b1 + a1) % P1;
		}

		result = xorSymbol(result, 0, C, (W+b1)*T, T);

		for(long j=1; j<d1; j++){

			do
				b1 = (b1 + a1) % P1;
			while(b1 >= P);

			result = xorSymbol(result, 0, C, (W+b1)*T, T);
		}

		return result;
	}

	public static Set<Integer> encIndexes(int K, Tuple tuple){

		Set<Integer> indexes = new TreeSet<Integer>();

		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		long L = K + S + H;
		long P = L - W;
		long P1 = ceilPrime(P);
		
		// Tuple
		long d = tuple.getD();
		long a = tuple.getA();
		long b = tuple.getB();
		long d1 = tuple.getD1();
		long a1 = tuple.getA1();
		long b1 = tuple.getB1();

		indexes.add((int) b);
		
		for(long j=0; j<d; j++){
			b = (b+a) % W;
			indexes.add((int) b);
		}

		while(b1 >= P){
			b1 = (b1 + a1) % P1;
		}

		indexes.add((int) (W + b1));

		for(long j=1; j<d1; j++){

			do
				b1 = (b1 + a1) % P1;
			while(b1 >= P);

			indexes.add((int) (W+b1));
		}

		return indexes;
	}

	public static byte[] xorSymbol(byte[] s1, byte[] s2){
		
		// FIXME this verifications are unecessary because this is only used internally, drawbacks performance
		if(s1.length != s2.length){
			throw new IllegalArgumentException("Symbols must be of the same size.");
		}

		byte[] xor = new byte[s1.length];

		for(int i=0; i<s1.length; i++){
			xor[i] = (byte) (s1[i] ^ s2[i]);
		}

		return xor;
	}

	public static byte[] xorSymbol(byte[] s1, int pos1, byte[] s2, int pos2, int length){

		// FIXME this verifications are unecessary because this is only used internally, drawbacks performance
		if((s1.length - pos1) < length || (s2.length - pos2) < length ){
			throw new IllegalArgumentException("Symbols must be of the same size.");
		}

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
	
	public static Set<Integer> getAttackTargets(int intermediateSymbolIndex, int kLinha, int size){
		
		Set<Integer> targetISIs = new HashSet<Integer>(size);
		
		for(int targetedISIs = 0, i = 0; targetedISIs < size; i++){
			
			// create tuple for ISI i
			Tuple tuple = new Tuple(kLinha, i);
			
			// get the set of indexes to be summed
			Set<Integer> summedSymbolsIndexes = encIndexes(kLinha, tuple);
 			
			// check if our targeted intermediate symbol is one of them
			if(summedSymbolsIndexes.contains(intermediateSymbolIndex)){
				
				// found one target ISI
				targetISIs.add(i);
				targetedISIs++;
			}
		}
		
		return targetISIs;
	}
	
	public static Set<Integer> getAttackTargets(Set<Integer> intermediateSymbolIndex, int kLinha, int size){
		
		Set<Integer> targetISIs = new HashSet<Integer>(size);
		
		for(int targetedISIs = 0, i = 0; targetedISIs < size; i++){
			
			// create tuple for ISI i
			Tuple tuple = new Tuple(kLinha, i);
			
			// get the set of indexes to be summed
			Set<Integer> summedSymbolsIndexes = encIndexes(kLinha, tuple);
 			
			Iterator<Integer> it = intermediateSymbolIndex.iterator();
			
			while(it.hasNext()){
				
				// get one our targeted intermediate symbols
				Integer index = it.next();
				
				// check if this targeted intermediate symbol is one of them
				if(summedSymbolsIndexes.contains(index)){

					// found one target ISI
					targetISIs.add(i);
					targetedISIs++;
					break;
				}
			}
		}
		
		return targetISIs;
	}
}

class Row{
	
	public int id;
	public int nonZeros;
	public int degree;
	public Set<Integer> edges = null;
	
	public Row(int i, int r, int d){
		
		id = i;
		nonZeros = r;
		degree = d;
	}
	
	public Row(int i, int r, int d, Set<Integer> e){
	
		id = i;
		nonZeros = r;
		degree = d;
		edges = e;
	}
	
	public boolean equals(Object o){
		
		if(!o.getClass().getName().equals(this.getClass().getName())) 
			return false;
		else
			if(((Row) o).id == this.id)
				return true;
			else
				return false;
	}
	
}

class Tuple{

	long d,a,b,d1,a1,b1;

	Tuple(int K, long X){

		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		int L = K + S + H;
		int J = SystematicIndices.J(Ki);
		int P = L - W;
		long P1 = Encoder.ceilPrime(P);

		long A = 53591 + J*997;
		if(A % 2 == 0)
			A++;

		long B = 10267 * (J+1);

		long y = (B + X*A) % 4294967296L; // 2^^32

		long v = Rand.rand(y, 0, 1048576L); // 2^^20

		this.d = Deg.deg(v,W);
		this.a = 1 + Rand.rand(y, 1, W-1);
		this.b = Rand.rand(y, 2, W);
		if(this.d<4)
			d1 = 2 + Rand.rand(X, 3, 2L);
		else
			d1 = 2;
		this.a1 = 1 + Rand.rand(X, 4, P1-1);
		this.b1 = Rand.rand(X, 5, P1);
	}

	public long getD() {
		return d;
	}

	public long getA() {
		return a;
	}

	public long getB() {
		return b;
	}

	public long getD1() {
		return d1;
	}

	public long getA1() {
		return a1;
	}

	public long getB1() {
		return b1;
	}
}

abstract class Deg{

	public static long deg(long v, long W){

		int i;
		for(i=0; i<31; i++){
			if(v < table[i])
				break;
		}

		if(i==31) throw new RuntimeException("Something went BOOM!");

		return(Math.min(i, W-2));	
	}

	private static long[] table = {
		0, 
		5243,
		529531,
	    704294,
		791675,
		844104,
		879057,
		904023,
		922747,
		937311,
		948962,
		958494,
		966438,
		973160,
		978921,
		983914,
		988283,
		992138,
		995565,
		998631,
		1001391,
		1003887,
		1006157,
		1008229,
		1010129,
		1011876,
		1013490,
		1014983,
		1016370,
		1017662,
		1048576
	};
}
