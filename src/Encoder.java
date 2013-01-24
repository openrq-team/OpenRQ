
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import sun.misc.Unsafe;

public class Encoder {

	// TODO check patents for better default values
	public static final int MAX_PAYLOAD_SIZE = 512; // P
	public static final int ALIGN_PARAM = 4; // Al
	public static final int MAX_SIZE_BLOCK = 76800; // WS // B
	public static final long SSYMBOL_LOWER_BOUND = 8; // SS
	public static final int KMAX = 56403;
	public static final byte ALPHA = 2;

	private int T; // symbol size
	private int Z; // number of source blocks
	private int N; // number of sub-blocks in each source block
	private int F; // transfer length
	private int Kt; // total number of symbols required to represent the source data of the object
	private byte[] data;

	public Encoder(byte[] file){

		F = file.length;

		T = derivateT(MAX_PAYLOAD_SIZE);
		Kt = ceil((double)F/T);
		Z = derivateZ();
		N = derivateN();

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

	private int derivateZ(){

		int N_max = floor((double)T/(SSYMBOL_LOWER_BOUND*ALIGN_PARAM));

		return(ceil((double)Kt/SystematicIndices.KL(N_max, MAX_SIZE_BLOCK, ALIGN_PARAM, T)));
	}

	private int derivateN(){

		int N_max = floor((double)(T/(SSYMBOL_LOWER_BOUND*ALIGN_PARAM)));

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
		for(i=0; i<ZL; i++){
			// Source block i

			byte[] aux2 = new byte[KL*T+1]; //TODO NAO ESQUECER OS EFEITOS QUE ESTE '+1' PODE TER EM FICHEIROS QUE NAO STRING

			int k;
			for(k=0; k<KL; k++){

				int j = 0;
				int index_data  = (i*KL*T) + (k*TL*ALIGN_PARAM); // always TL because you always start by the sub-block
				int index_aux2 = 0;

				for(; j<NL; j++, index_data+=KL*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TL*ALIGN_PARAM);
				}

				for(; j<NS; j++, index_data+=KL*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TS*ALIGN_PARAM);
				}
			}

			object[i] = new SourceBlock(i, aux2, T, KL);
		}

		for(; i<ZS; i++){
			// Source block i

			byte[] aux2 = new byte[KS*T+1]; //TODO NAO ESQUECER OS EFEITOS QUE ESTE '+1' PODE TER EM FICHEIROS QUE NAO STRING

			int k;
			for(k=0; k<KS; k++){

				int j = 0;
				int index_data  = (i*KS*T) + (k*TL*ALIGN_PARAM); // always TL because you always start by the sub-block
				int index_aux2 = 0;

				for(; j<NL; j++, index_data+=KS*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TL*ALIGN_PARAM);
				}

				for(; j<NS; j++, index_data+=KS*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TS*ALIGN_PARAM);
				}
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
				int index_data  = (i*Kappa*T) + k*TL*ALIGN_PARAM; // always TL because you always start by the sub-block
				int index_aux2 = k*t_;

				for(; j<NL; j++, index_data+=Kappa*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(aux2, index_aux2, data, index_data, TL*ALIGN_PARAM);
				}

				for(; j<NS; j++, index_data+=Kappa*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(aux2, index_aux2, data, index_data, TS*ALIGN_PARAM);
				}
			}
		}

		return data;		
	}

	public void decode(EncodingPacket[] encoded_blocks) {

		// Decode each block
		for(int source_block_index = 0; source_block_index<encoded_blocks.length; source_block_index++){
			
			EncodingPacket eb = encoded_blocks[source_block_index];
			EncodingSymbol[] enc_symbols = eb.getEncoding_symbols();
			int num_symbols = enc_symbols.length;
			int SBN = eb.getSBN();
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
				
				if(enc_symbol == null){
					
					missing_symbols.add(symbol + missing_delta_index);
					missing_delta_index++;
					
					continue;
				}
				
				if(enc_symbol.getESI() < K){
					
					if(enc_symbol.getESI() != (symbol + missing_delta_index)){
						
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
			
			if(num_repair_symbols < (K - num_source_symbols)) throw new RuntimeException("Not enough repair symbols received."); // TODO shouldnt be runtime exception, too generic
			
			Map<Integer, byte[]> esiToLTCode = new TreeMap<Integer, byte[]>();
			
			byte[] decoded_data = new byte[K*T];
			
			// All source symbols received :D
			if(num_source_symbols == K){
				
				// Collect all payloads from the source symbols				
				for(EncodingSymbol enc_symbol : source_symbols){
					
					System.arraycopy(enc_symbol.getData(), 0, decoded_data, enc_symbol.getESI() * T, T);
				}
				
				System.out.println("\n\n ALL SOURCE SYMBOLS RECEIVED");
				System.out.println(new String(decoded_data));
			}
			// Not so lucky
			else{
				
				// Generate original constraint matrix
				byte[][] constraint_matrix = generateConstraintMatrix(kLinha, T);
				
				// initialize D
				byte[][] D = new byte[L][T];
				
				if(num_source_symbols != 0){

					Iterator<EncodingSymbol> it = source_symbols.iterator();

					EncodingSymbol source_symbol = (EncodingSymbol) it.next();

					for(int row=S+H; row<S+H+K; row++){

						if(source_symbol.getESI() == row-S-H){

							D[row] = source_symbol.getData();

							if(it.hasNext()) source_symbol = (EncodingSymbol) it.next();
							else break;
						}
						else continue;
					}
				}

				System.out.println("---- A ---- (initial)");
				(new Matrix(constraint_matrix)).show();
				System.out.println("---------------------");
				
				System.out.println("---- D ---- (initial)");
				(new Matrix(D)).show();
				System.out.println("---------------------");
				
				// Identify missing source symbols and replace their lines with "repair lines"
				Iterator<EncodingSymbol> repair_symbol = repair_symbols.iterator();
				for(Integer missing_ESI : missing_symbols){
					
					EncodingSymbol repair = (EncodingSymbol) repair_symbol.next();
					int row = S+H+missing_ESI;
					
					// Substituir S + H + missing_ESI pela linha equivalente ao encIndexes do repair simbolo
					Set<Integer> indexes = encIndexes(kLinha, new Tuple(kLinha, repair.getISI(K)));
					
					byte[] newLine = new byte[L];
					
					for(int col=0; col<L; col++){
						
						if(!indexes.contains(col)){
							
							continue;
						}
						else{
							
							newLine[col] = 1;
						}
					}
					
					esiToLTCode.put(missing_ESI, constraint_matrix[row]);
					constraint_matrix[row] = newLine;
					
					// Fill in missing source symbols in D with the repair symbols
					D[row] = repair.getData();
				}
				
				System.out.println("---- A ---- (recovered)");
				(new Matrix(constraint_matrix)).show();
				System.out.println("---------------------");
				
				System.out.println("---- D ---- (recovered)");
				(new Matrix(D)).show();
				System.out.println("---------------------");
				
				// Generate the intermediate symbols
				byte[] intermediate_symbols = generateIntermediateSymbols(constraint_matrix, D, T);
				
				System.out.println("---- C ----");
				(gaussElim(constraint_matrix, D, T)).show();
				System.out.println("---------------------");
				
				// Recover missing source symbols
				for(Map.Entry<Integer, byte[]> missing : esiToLTCode.entrySet()){
					
					byte[] original_symbol = multiplyByteLineBySymbolVector(missing.getValue(), intermediate_symbols, T);
					
					System.arraycopy(original_symbol, 0, decoded_data, missing.getKey() * T, T);
				}
			
				// Merge with received source symbols
				for(EncodingSymbol enc_symbol : source_symbols){
					
					System.arraycopy(enc_symbol.getData(), 0, decoded_data, enc_symbol.getESI() * T, T);
				}
				
				System.out.println("\n\n RECOVERED MOTHERFUCKER");
				System.out.println(new String(decoded_data));
				
				System.out.println("\n\n\nMy way, or the highway.\n");
				byte[][] newD = new byte[L][T];
				
				for(int row=0; row<L; row++){
					
					newD[row] = multiplyByteLineBySymbolVector(constraint_matrix[row], intermediate_symbols, T);
				}
				
				(new Matrix(newD)).show();
				
				System.out.println("teste");
				
				byte[][] m1 = 
					  {  {1,1,0,0},
	                     {1,1,0,0},
	                     {1,0,1,1},
	                     {0,0,0,1}
	                  };
				
				byte[][] m2 = {
						{10, 15, 20},
						{10, 15, 20},
						{10, 15, 20}
				};
				
				byte[][] m3 = {
						{3,3,3},
						{4,4,4},
						{1,1,1}
				};
				
				byte[] m21 = {10, 15, 20};
				byte[] m31 = {3, 3, 3, 4, 4, 4, 1, 1, 1};
				
				(new Matrix(multiplyMatrixes(m1,m1))).show();
				
				byte[][] m23 = multiplyMatrixes(m2,m3);
				
				(new Matrix(m23)).show();
				
				System.out.println(multiplyByteLineBySymbolVector(m21,m31,3)[0]);
				System.out.println(multiplyByteLineBySymbolVector(m21,m31,3)[1]);
				System.out.println(multiplyByteLineBySymbolVector(m21,m31,3)[2]);
				
				// Gauss elim
				
				Matrix M23 = gaussElim(m2, m23, 3);

				M23.show();
				
			}
			
		}
		
		
	}

	public EncodingPacket[] encode(SourceBlock[] object){

		EncodingPacket[] encoded_blocks = new EncodingPacket[object.length];
		
		for(int source_block_index = 0; source_block_index<object.length; source_block_index++){
			
			SourceBlock sb = object[source_block_index];
			int SBN = sb.getSBN();
			byte[] ssymbols = sb.getSymbols();
			int K = (int)sb.getK();
			int kLinha = SystematicIndices.ceil(K);
			
			EncodingSymbol[] encoded_symbols = new EncodingSymbol[21]; // FIXME arbitrary size ASAP
			
			// First encoding step
			byte[] intermediate_symbols = generateIntermediateSymbols(sb);
			
			// Second encoding step
			// Sending original source symbols
			int source_symbol;
			int source_symbol_index;
			for(source_symbol = 0, source_symbol_index = 0; source_symbol<sb.getK(); source_symbol++, source_symbol_index+=sb.getT()){
				
				//encoded_symbols[source_symbol] = new EncodingSymbol(SBN,source_symbol, Arrays.copyOfRange(ssymbols, source_symbol_index, (int) (source_symbol_index+sb.getT())));
			}
			
			// Generating/sending repair symbols
			for(int repair_symbol = 0; repair_symbol<20; repair_symbol++){
				
				int	isi = kLinha + repair_symbol;
				int esi = K + repair_symbol;
				
				byte[] enc_data = enc(kLinha, intermediate_symbols, new Tuple(kLinha, isi));
				
				encoded_symbols[source_symbol + repair_symbol] = new EncodingSymbol(SBN,esi, enc_data);
			}
			
			encoded_blocks[source_block_index] = new EncodingPacket(SBN, encoded_symbols, K, sb.getT());
		}

		return encoded_blocks;
	}
	
	private void initializeG_LPDC2(byte[][] constraint_matrix, int S, int P, int W){
		
		for(int row=0; row<S; row++){

			// Consecutives 1's modulo P
			constraint_matrix[row][(row   %P) + W] = 1;
			constraint_matrix[row][(row+1 %P) + W] = 1;

		}
	}
	
	private void initializeG_LPDC1(byte[][] constraint_matrix, int B, int S){
		
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
	
	private void initializeIs(byte[][] constraint_matrix, int S, int B){
	
		for(int row=0; row<S; row++){
			for(int col=0; col<S; col++){

				if(col != row)
					continue;
				else
					constraint_matrix[row][col+B] = 1;
			}
		}
	}
	
	private void initializeIh(byte[][] constraint_matrix, int W, int U, int H, int S){
		
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
	
	private byte[][] generateMT(int H, int K, int S){
		
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
	
	private byte[][] generateGAMMA(int K, int S){
		
		byte[][] GAMMA = new byte[K+S][K+S];

		for(int row = 0; row<K+S; row++){
			for(int col = 0; col<K+S; col++){

				if(row >= col)
					GAMMA[row][col] = OctectOps.getExp(row-col);
				else
					continue;				
			}	
		}
		
		return GAMMA;
	}
	
	public byte[][] multiplyMatrixes(byte[][] A, byte[][] B){
		
        if (A[0].length != B.length) throw new RuntimeException("Illegal matrix dimensions.");
        
        byte[][] C = new byte[A.length][B[0].length];
        
        for (int i = 0; i < C.length; i++){
            for (int j = 0; j < C[0].length; j++){
                for (int k = 0; k < A[0].length; k++){
                	
                	byte temp = OctectOps.octProduct(A[i][k], B[k][j]);
                	
                	C[i][j] = (byte) (C[i][j] ^ temp);
                }
            }
        }
        
        return C;
	}
	
	public byte[] multiplyByteLineBySymbolVector(byte[] line, byte[] vector, int symbol_size){
		
		// TODO verify matrix sizes
		
		byte[] result = new byte[symbol_size];
		
		for(int octet=0; octet<symbol_size; octet++){

			for(int colRow=0; colRow<line.length; colRow++){
				
				byte temp = OctectOps.octProduct(line[colRow], vector[(colRow * symbol_size) + octet]);
				
				result[octet] = (byte) (result[octet] ^ temp);
			}
		}
		
		return result;
	}
	
	private void initializeG_ENC(byte[][] constraint_matrix, int S, int H, int L, int K){
		
		for(int row=S+H; row<L; row++){

			Tuple tuple = new Tuple(K, row-S-H);

			Set<Integer> indexes = encIndexes(K, tuple);

			for(Integer j : indexes){

				constraint_matrix[row][j] = 1;	
			}
		}
	}
	
	private byte[][] generateConstraintMatrix(int K, int T){
		
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
		byte[][] G_HDPC = multiplyMatrixes(MT, GAMMA);

		// Initialize G_HDPC
		for(int row=S; row<S+H; row++)
			for(int col=0; col<W+U; col++)
				constraint_matrix[row][col] = G_HDPC[row-S][col];

		// Initialize G_ENC
		initializeG_ENC(constraint_matrix, S, H, L, K);
		
		return constraint_matrix;
	}
	
	private byte[] generateIntermediateSymbols(byte[][] A, byte[][] D, int symbol_size){
		
		// TODO verify size of matrixes?
		
		int L = A.length;
		
		// Gauss elim
		Matrix C = gaussElim(A, D, symbol_size);

		byte[] intermediate_symbols = new byte[L*symbol_size];

		for(int intermediate_symbols_index = 0, intermediate_symbol=0; intermediate_symbol < L; intermediate_symbols_index += symbol_size, intermediate_symbol++){
			
			System.arraycopy(C.getData()[intermediate_symbol], 0, intermediate_symbols, intermediate_symbols_index, symbol_size);
		}

		return intermediate_symbols;
	}
	
	private byte[] generateIntermediateSymbols(SourceBlock sb){
		
		byte[] ssymbols = sb.getSymbols();
		// TODO create ssymbols + padding

		int K = SystematicIndices.ceil((int)sb.getK());
		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		int L = K + S + H;
		int P = L - W;
		int U = P - H;
		int B = W - S;

		int k = sb.getK();
		int t = sb.getT();

		/* Generate LxL Constraint  Matrix*/
		byte[][] constraint_matrix = generateConstraintMatrix(K, t); // A
		
		// D
		byte[][] D = new byte[L][t]; 

		for(int row=S+H, index=0; row<k+S+H; row++, index+=t){

			D[row] = Arrays.copyOfRange(ssymbols, index, (index+t));
		}

		System.out.println("\n\n------- CONSTRAINT MATRIX -------");
		for(int row=0; row<L; row++){
			for(int col=0; col<L; col++){
				if(row<S || row > S+H-1 || col>=W+U)
					System.out.printf("|  %01X ",constraint_matrix[row][col]);
				else
					System.out.printf("| %02X ", convert(constraint_matrix[row][col]));
			}
			System.out.println("|");
		}
		System.out.println("------- END -------");

		System.out.println("\n\n------- D COLUMN MATRIX -------"+t);
		(new Matrix(D)).show();
		System.out.println("------- END -------");

		// Gauss elim
		Matrix C = gaussElim(constraint_matrix, D, t);

		System.out.println("\n\n------- INTERMEDIATE SYMBOLS -------");
		C.show();
		System.out.println("------- END -------");

		byte[] intermediate_symbols = new byte[L*T];
		
		for(int intermediate_symbols_index = 0, intermediate_symbol=0; intermediate_symbol < L; intermediate_symbols_index += t, intermediate_symbol++){
			System.arraycopy(C.getData()[intermediate_symbol], 0, intermediate_symbols, intermediate_symbols_index, t);
		}
		
		System.out.println("\n\n\nMy way, or the highway.\n");
		byte[][] newD = new byte[L][T];
		
		for(int row=0; row<L; row++){
			
			newD[row] = multiplyByteLineBySymbolVector(constraint_matrix[row], intermediate_symbols, T);
		}
		
		(new Matrix(newD)).show();
		
		return intermediate_symbols;
	}

	public static final byte convert(byte in){

		byte hex1 = (byte) (in << 4);
		byte hex2 = (byte) ((in >>> 4) & 0xF);

		return (byte) (hex2 | hex1);
	}

	public Matrix gaussElim(byte[][] A, byte[][] D, int symbol_size){

		if (A.length != A[0].length || D.length != A.length || D[0].length != symbol_size)
			throw new RuntimeException("Illegal matrix dimensions.");

		int A_columns = A[0].length;

		// Gaussian elimination with partial pivoting
		for (int i = 0; i < A_columns; i++) {

			// find pivot row and swap
			int max = i;
			for (int j = i + 1; j < A_columns; j++)
				if (Math.abs(A[j][i]) > Math.abs(A[max][i]))
					max = j;

			// A.swap(i, max);
			byte[] temp = A[i];
			A[i] = A[max];
			A[max] = temp;

			// b.swap(i, max);
			temp = D[i];
			D[i] = D[max];
			D[max] = temp;         

			// singular
			if (A[i][i] == 0) {
				System.err.println("LINHA QUE DEU SINGULAR: "+i);
				throw new RuntimeException("Matrix is singular.");
			}

			// pivot within b
			for (int j = i + 1; j < A_columns; j++){
				// b.data[j][0] -= b.data[i][0] * A.data[j][i] / A.data[i][i];

				//A.data[j][i] / A.data[i][i]; // _aDiv
				byte _aDiv;
				if(A[j][i] == 0){

					_aDiv = 0;
				}
				else{

					_aDiv = OctectOps.octDivision(A[j][i], A[i][i]);
				}

				// b.data[i][0] * _aDiv // _bMul
				byte[] _bMul;

				if(_aDiv == 0 || symbolIsZero(D[i])){

					_bMul = new byte[D[i].length];
				}
				else{

					_bMul = OctectOps.betaProduct(_aDiv, D[i]);
				}

				// b.data[j][0] -= _bMul
				D[j] = xorSymbol(D[j], _bMul);
			}

			// pivot within A
			for(int j = i + 1; j < A_columns; j++){ 

				byte m = OctectOps.octDivision(A[j][i], A[i][i]);

				for (int k = i+1; k < A_columns; k++) {
					// A.data[j][k] -= A.data[i][k] * m;

					// A.data[i][k] * m // _aXOR
					byte _aXOR = OctectOps.octProduct(A[i][k], m);

					// A.data[j][k] -= _aXOR
					A[j][k] = (byte) (A[j][k] ^ _aXOR);
				}

				A[j][i] = 0;
			}
		}

		// back substitution
		Matrix x = new Matrix(A_columns, D[0].length);
		for (int j = A_columns - 1; j >= 0; j--) {

			byte[] sum = new byte[D[0].length];

			for (int k = j + 1; k < A_columns; k++)
				sum = xorSymbol(sum, OctectOps.betaProduct(A[j][k], x.data[k]));
			x.data[j] = OctectOps.betaDivision(xorSymbol(D[j], sum), A[j][j]);
		}

		return x;
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

		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		long L = K + S + H;
		long P = L - W;
		//long B = W - S;
		long P1 = ceilPrime(P);
		long d = tuple.getD();
		long a = tuple.getA();
		long b = tuple.getB();
		long d1 = tuple.getD1();
		long a1 = tuple.getA1();
		long b1 = tuple.getB1();

		// TODO mudar todos estes copyOfRange para arrays originais com src e dst
		byte[] result = Arrays.copyOfRange(C, (int)(b*T), (int)((b+1)*T));

		for(long j=0; j<d; j++){
			b = (b+a) % W;
			result = xorSymbol(result, Arrays.copyOfRange(C, (int)(b*T), (int)((b+1)*T)));
		}

		while(b1 >= P){
			b1 = (b1 + a1) % P1;
		}

		result = xorSymbol(result, Arrays.copyOfRange(C, (int)((W+b1)*T), (int)((W+b1+1)*T)));

		for(long j=1; j<d1; j++){

			do
				b1 = (b1 + a1) % P1;
			while(b1 >= P);

			result = xorSymbol(result, Arrays.copyOfRange(C, (int)((W+b1)*T), (int)((W+b1+1)*T)));
		}

		return result;
	}

	public Set<Integer> encIndexes(int K, Tuple tuple){

		//TODO make it a set
		Set<Integer> indexes = new TreeSet<Integer>();

		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		long L = K + S + H;
		long P = L - W;
		//long B = W - S;
		long P1 = ceilPrime(P);
		long d = tuple.getD();
		long a = tuple.getA();
		long b = tuple.getB();
		long d1 = tuple.getD1();
		long a1 = tuple.getA1();
		long b1 = tuple.getB1();

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

	public byte[] xorSymbol(byte[] s1, byte[] s2){
		if(s1.length != s2.length){
			throw new IllegalArgumentException("Symbols must be of the same size.");
		}

		byte[] xor = new byte[s1.length];

		for(int i=0; i<s1.length; i++){
			xor[i] = (byte) (s1[i] ^ s2[i]);
		}

		return xor;
	}

	public void xorSymbol(byte[] s1, int pos1, byte[] s2, int pos2, int length){

		for(int i=0; i<length; i++, pos1++, pos2++){
			s1[pos1] = (byte) (s1[pos1] ^ s2[pos2]);
		}
	}

	public static long ceilPrime(long p){

		while(!isPrime(p)) p++;

		return p;
	}

	public static boolean isPrime(long n){
		//check if n is a multiple of 2
		if (n%2==0) return false;
		//if not, then just check the odds
		for(long i=3;i*i<=n;i+=2) {
			if(n%i==0)
				return false;
		}
		return true;
	}

	private static long power(int x,int y){

		long p=1;
		long b=((long)y) & 0x00000000ffffffffL;
		long powerN=x;

		while(b!=0){

			if((b & 1) != 0) 
				p*=powerN;

			b>>>=1;
		powerN=powerN*powerN;
		}

		return p;
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

		long B = 10267*(J+1);

		long y = (B + X*A) % 4294967296L; // 2^^32

		long v = Rand.rand(y, 0, 1048576L); // 2^^20

		this.d = Deg.deg(v,W);
		this.a = 1 + Rand.rand(y, 1, W-1);
		this.b = Rand.rand(y, 2, W);
		if(d<4)
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
