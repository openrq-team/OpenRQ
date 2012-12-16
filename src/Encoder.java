
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.misc.Unsafe;

public class Encoder {

	public static final int MAX_PAYLOAD_SIZE = 512; // P
	public static final int ALIGN_PARAM = 4; // Al
	public static final int MAX_SIZE_BLOCK = 76800; // WS // B
	public static final long SSYMBOL_LOWER_BOUND = 8; // SS
	public static final int KMAX = 56403;
	public static final byte ALPHA = 2;

	private int T; // symbol size
	private int Z; // number of source blocks
	private int N; // number of sub-blocks in each source block
	private long F; // transfer length
	private int Kt; // total number of symbols required to represent the source data of the object
	private byte[] data;


	public Encoder(byte[] file){

		F = file.length;

		T = derivateT(MAX_PAYLOAD_SIZE);
		Kt = ceil((double)F/T);
		Z = derivateZ();
		N = derivateN();

		if(F>Kt*T)
			data = new byte[(int) F];
		else
			data = new byte[(int) Kt*T];

		for(int i=0; i<F; i++)
			data[i] = file[i];

	}

	private static final short UNSIGN(byte b){
		return((short) (b & 0xFF));
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

	public static int ceil(double x){
		return((int)Math.ceil(x));
	}

	public static int floor(double x){
		return((int)Math.floor(x));
	}

	public SourceBlock[] partition(){

		Partition KZ = new Partition(Kt, Z);
		int KL = KZ.getIl();
		int KS = KZ.getIs();
		int ZL = KZ.getJl();
		int ZS = KZ.getJs();

		Partition TN = new Partition(T/ALIGN_PARAM, N);
		int TL = TN.getIl();
		int TS = TN.getIs();
		int NL = TN.getJl();
		int NS = TN.getJs();

		SourceBlock[] object = new SourceBlock[Z];

		int i;
		for(i=0; i<ZL; i++){
			// Source block i

			byte[] aux2 = new byte[KL*T+1]; //TODO NAO ESQUECER OS EFEITOS QUE ESTE '+1' PODE TER EM FICHEIROS QUE NAO STRING

			int k;
			for(k=0; k<KL; k++){

				int j = 0;
				int index_data  = (i*KL*T) + (k*TL*ALIGN_PARAM); // sempre TL pq começa-se sempre pelo primeiro sub-bloco
				int index_aux2 = 0;

				for(; j<NL; j++, index_data+=KL*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TL*ALIGN_PARAM);
				}

				for(; j<NS; j++, index_data+=KL*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TS*ALIGN_PARAM);
				}
			}

			object[i] = new SourceBlock(i, aux2, T, N, KL);	
		}

		for(; i<ZS; i++){
			// Source block i

			byte[] aux2 = new byte[KS*T+1]; //TODO NAO ESQUECER OS EFEITOS QUE ESTE '+1' PODE TER EM FICHEIROS QUE NAO STRING

			int k;
			for(k=0; k<KS; k++){

				int j = 0;
				int index_data  = (i*KS*T) + (k*TL*ALIGN_PARAM); // sempre TL pq começa-se sempre pelo primeiro sub-bloco
				int index_aux2 = 0;

				for(; j<NL; j++, index_data+=KS*TL*ALIGN_PARAM, index_aux2+=TL*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TL*ALIGN_PARAM);
				}

				for(; j<NS; j++, index_data+=KS*TS*ALIGN_PARAM, index_aux2+=TS*ALIGN_PARAM){
					System.arraycopy(data, index_data, aux2, index_aux2, TS*ALIGN_PARAM);
				}
			}

			object[i] = new SourceBlock(i, aux2, T, N, KS);
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
			int Kappa   = (int)sb.getK();
			int n_      = (int)sb.getN();
			int t_      = (int)sb.getT();
			byte[] aux2 = sb.getSymbols();

			Partition TN = new Partition(t_/ALIGN_PARAM, n_);
			int TL = TN.getIl();
			int TS = TN.getIs();
			int NL = TN.getJl();
			int NS = TN.getJs();



			for(int k=0; k<Kappa; k++){

				int j = 0;
				int index_data  = (i*Kappa*T) + k*TL*ALIGN_PARAM; // sempre TL pq começa-se sempre pelo primeiro sub-bloco
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

	public List<EncodedSymbol> encode(SourceBlock[] object){

		List<EncodedSymbol> encoded_symbols = new ArrayList<EncodedSymbol>(50);
		
		for(int source_block_index = 0; source_block_index<object.length; source_block_index++){
			SourceBlock sb = object[source_block_index];
			byte[] ssymbols = sb.getSymbols();
			int K = (int)sb.getK();
			int kLinha = SystematicIndices.ceil(K);
			
			// First encoding step
			byte[] intermediate_symbols = generateIntermediateSymbols(sb);
			
			// Second encoding step
			// Sending original source symbols
			for(int source_symbol = 0, source_symbol_index = 0; source_symbol<sb.getK(); source_symbol++, source_symbol_index+=sb.getT()){
				
				//encoded_symbols.add(new EncodedSymbol(source_symbol, Arrays.copyOfRange(ssymbols, source_symbol_index, (int) (source_symbol_index+sb.getT()))));
			}
			
			// Generating/sending repair symbols
			for(int repair_symbol = 0; repair_symbol<20; repair_symbol++){
				
				long isi = kLinha+repair_symbol;
				
				byte[] enc_data = enc(kLinha, intermediate_symbols, new Tuple(kLinha, isi));
				
				encoded_symbols.add(new EncodedSymbol(isi, enc_data));
			}
			
		}

		return encoded_symbols;
	}

	// TODO dividir em varios metodos
	public byte[] generateIntermediateSymbols(SourceBlock sb){

		byte[] ssymbols = sb.getSymbols();
		// TODO create ssymbols + padding

		int K = SystematicIndices.ceil((int)sb.getK());
		int Ki = SystematicIndices.getK(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		int L = K + S + H;
		int P = L - W;
		int U = P - H;
		int B = W - S;

		int k = (int) sb.getK();
		int t = (int) sb.getT();
		int kt = k*t; // inutil?


		/* Generate LxL Constraint  Matrix*/
		byte[][] constraint_matrix = new byte[L][L]; // A

		// Upper half

		// Initialize G_LPDC2
		for(int row=0; row<S; row++){

			// Consecutives 1's modulo P
			constraint_matrix[row][(row   %P) + W] = 1;
			constraint_matrix[row][(row+1 %P) + W] = 1;

		}

		// Initialize G_LPDC1
		int circulant_matrix = -1;
		for(int col=0; col<B; col++){

			int circulant_matrix_column = col % S;

			if(circulant_matrix_column != 0){ 

				// cyclic down-shift													// FIXME implementar em C --> memoria contigua pros cyclic down-shift :(
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

		// Initialize I_s			
		for(int row=0; row<S; row++){
			for(int col=0; col<S; col++){

				if(col != row)
					continue;
				else
					constraint_matrix[row][col+B] = 1;
			}
		}

		// Botton half

		// Initialize I_h
		int lower_limit_col = W + U;

		for(int row=0; row<H; row++){
			for(int col=0; col<H; col++){

				if(col != row)
					continue;
				else
					constraint_matrix[row+S][col+lower_limit_col] = 1;
			}
		}

		// Initialize G_HDPC
		// MT
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

		// GAMMA
		byte[][] GAMMA = new byte[K+S][K+S];

		for(int row = 0; row<K+S; row++){
			for(int col = 0; col<K+S; col++){

				if(row >= col)
					GAMMA[row][col] = OctectOps.getExp(row-col);
				else
					continue;

				/*	// By Livro 
					if( (row == col) || (row == col+1) )
						GAMMA[row][col] = OctectOps.getExp(row-col);
					else
						continue; */					
			}	
		}

		// G_HDPC = MT * GAMMA				// TODO implementar multiplicacao directamente pra constraint_matrix
		Matrix _mt    = new Matrix(MT);
		Matrix _gamma = new Matrix(GAMMA); 

		Matrix g_hdpc = _mt.times(_gamma);

		byte[][] G_HDPC = g_hdpc.getData(); 

		System.out.println("\n\n\n Initializing G_HDPC\n");
		// Initialize G_HDPC
		for(int row=S; row<S+H; row++)
			for(int col=0; col<W+U; col++)
				constraint_matrix[row][col] = G_HDPC[row-S][col];

		// G_ENC
		for(int row=S+H; row<L; row++){

			Tuple tuple = new Tuple(K, row-S-H);

			List<Integer> indexes = encIndexes(K, tuple);

			for(int j=0; j<indexes.size(); j++){

				constraint_matrix[row][indexes.get(j)] = 1;	
			}
		}

		// D
		// TODO converter isto pra array
		byte[][] D = new byte[L][t]; 

		for(int row=S+H, index=0; row<k+S+H; row++, index+=t){

			D[row] = Arrays.copyOfRange(ssymbols, index, (index+t));
		}

		/*
			System.out.println("------- MT MATRIX -------");
			_mt.show();
			System.out.println("\n\n------- GAMMA MATRIX -------");
			_gamma.show();
			System.out.println("\n\n------- G_HDPC MATRIX -------");
			g_hdpc.show();

		 */

		System.out.println("\n\n------- CONSTRAINT MATRIX -------");
		for(int row=0; row<L; row++){
			for(int col=0; col<L; col++){
				if(row<S || (row>=S && col>=W+U) || row > S+H)
					System.out.printf("|  %01X ",constraint_matrix[row][col]);
				else
					System.out.printf("| %02X ", convert(constraint_matrix[row][col]));
				//System.out.printf("| %02X ", constraint_matrix[row][col]);
			}
			System.out.println("|");
		}
		System.out.println("------- END -------");

		System.out.println("\n\n------- D COLUMN MATRIX -------"+t);
		(new Matrix(D)).show();
		System.out.println("------- END -------");

		//System.exit(-123);

		// Gauss elim
		Matrix C = gaussElim(constraint_matrix, D, t);

		System.out.println("\n\n------- INTERMEDIATE SYMBOLS -------");
		C.show();
		System.out.println("------- END -------");

		byte[] intermediate_symbols = new byte[C.getData().length*C.getData()[0].length];
		
		for(int intermediate_symbols_index = 0, intermediate_symbol=0; intermediate_symbol < t; intermediate_symbols_index += t, intermediate_symbol++){
			System.arraycopy(C.getData()[intermediate_symbol], 0, intermediate_symbols, intermediate_symbols_index, t);
		}
		
		return intermediate_symbols;
	}

	public static final byte convert(byte in){

		byte hex1 = (byte) (in << 4);
		byte hex2 = (byte) ((in >>> 4) & 0xF);

		return (byte) (hex2 | hex1);
	}

	// Ax = b // TODO testar
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

					_aDiv = OctectOps.getExp(OctectOps.getLog(UNSIGN(A[j][i])) - OctectOps.getLog(UNSIGN(A[i][i])) + 255); // TODO octDivide
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

		int Ki = SystematicIndices.getK(K);
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

		byte[] result = Arrays.copyOfRange(C, (int)b, (int)((b+1)*T));

		for(long j=0; j<d; j++){
			b = (b+a) % W;
			result = xorSymbol(result, Arrays.copyOfRange(C, (int)b, (int)((b+1)*T)));
		}

		while(b1 >= P){
			b1 = (b1 + a1) % P1;
		}

		result = xorSymbol(result, Arrays.copyOfRange(C, (int)(W+b1), (int)((W+b1+1)*T)));

		for(long j=1; j<d1; j++){

			do
				b1 = (b1 + a1) % P1;
			while(b1 >= P);

			result = xorSymbol(result, Arrays.copyOfRange(C, (int)(W+b1), (int)((W+b1+1)*T)));
		}

		return result;
	}

	public List<Integer> encIndexes(int K, Tuple tuple){

		List<Integer> indexes = new ArrayList<Integer>();

		int Ki = SystematicIndices.getK(K);
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
			//TODO throw exception
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

	public void decode(List<EncodedSymbol> encoded_symbols) {
		// TODO Auto-generated method stub
		
	} 

}

class Tuple{

	long d,a,b,d1,a1,b1;

	Tuple(int K, long X){

		int Ki = SystematicIndices.getK(K);
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

	public void setD(long d) {
		this.d = d;
	}

	public long getA() {
		return a;
	}

	public void setA(long a) {
		this.a = a;
	}

	public long getB() {
		return b;
	}

	public void setB(long b) {
		this.b = b;
	}

	public long getD1() {
		return d1;
	}

	public void setD1(long d1) {
		this.d1 = d1;
	}

	public long getA1() {
		return a1;
	}

	public void setA1(long a1) {
		this.a1 = a1;
	}

	public long getB1() {
		return b1;
	}

	public void setB1(long b1) {
		this.b1 = b1;
	}	
}

abstract class Deg{

	public static long deg(long v, long W){

		int i;
		for(i=0; i<table.length; i++){ // TODO constant - 31
			if(v < table[i][0])
				break;
		}

		// TODO if(i==31) exception

		return(Math.min(i, W-2));	
	}

	private static long[][] table = { // TODO its an array stupid
		{ 0,     0      },
		{ 1,     5243   },
		{ 2,     529531 },
		{ 3,     704294 },
		{ 4,     791675 },
		{ 5,     844104 },
		{ 6,     879057 },
		{ 7,     904023 },
		{ 8,     922747 },
		{ 9,     937311 },
		{10,     948962 },
		{11,     958494 },
		{12,     966438 },
		{13,     973160 },
		{14,     978921 },
		{15,     983914 },
		{16,     988283 },
		{17,     992138 },
		{18,     995565 },
		{19,     998631 },
		{20,     1001391},
		{21,     1003887},
		{22,     1006157},
		{23,     1008229},
		{24,     1010129},
		{25,     1011876},
		{26,     1013490},
		{27,     1014983},
		{28,     1016370},
		{29,     1017662},
		{30,     1048576}
	};
}
