
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import sun.misc.Unsafe;
import Jama.Matrix;

public class Encoder {
	
	public static final int MAX_PAYLOAD_SIZE = 512; // P
	public static final int ALIGN_PARAM = 4; // Al
	public static final int MAX_SIZE_BLOCK = 76800; // WS // B
	public static final long SSYMBOL_LOWER_BOUND = 8; // SS
	public static final int KMAX = 56403;
	
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
					System.arraycopy(data, index_data, aux2, index_aux2, TS*ALIGN_PARAM); //TODO OFF BY ONE KUNG-FU
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
	
	public void encode(SourceBlock[] object){
		
		generateIntermediateSymbols(object);
		
		
	}
	
	public void generateIntermediateSymbols(SourceBlock[] object){
		
		Map<SourceBlock, Tuple[]> sbTuple = generateTuples(object);
		
		for(int i=0; i<object.length; i++){
			
			SourceBlock sb = object[i];
			byte[] ssymbols = sb.getSymbols();
			
			int K = SystematicIndices.ceil((int)sb.getK());
			int S = SystematicIndices.S(K);
			int H = SystematicIndices.H(K);
			int W = SystematicIndices.W(K);
			int L = K + S + H;
			int P = L - W;
			int U = P - H;
			int B = W - S;
			
			int k = (int) sb.getK();
			int t = (int) sb.getT();
			int kt = k*t; // inutil?
			
			
			/* Generate LxL Constraint  Matrix*/
			byte[][] constraint_matrix = new byte[S+H][L];
			
			// Initialize G_LPDC2
			for(int row=0; row<S; row++){
				for(int col=W; col<L; col++){
					
					// Consecutives 1's modulo P
					constraint_matrix[row][col  %P] = 1;
					constraint_matrix[row][col+1%P] = 1;
					
				}
			}
			
			// Initialize G_LPDC1
			for(int row=0; row<S; row++){
				
				constraint_matrix[row][0] = 1;
				constraint_matrix[row]
						
				for(int col=1; col<B; col++){

										
											// TODO implementar em C --> memoria contigua pros cyclic down-shift :(
					
				}
			}
			
			
			/*
			// initialize C
			byte[] C = new byte[L];
			for(int j=0; j<kt; j++){
				C[j] = ssymbols[i];
			}
			
			// Generate SxB G_LDPC1
			for(int j=0; j<B; j++){
				
				int a = 1 + Encoder.floor((double)j/S);
				int b = j % S;
				
				// D[b] = D[b] + C[i]
				xorSymbol(C,(B + b) * t, C, j, t);
				
				b = (b + a) % S;
				
				// D[b] = D[b] + C[i]
				xorSymbol(C,(B + b) * t, C, j, t);
				
				b = (b + a) % S;
				
				// D[b] = D[b] + C[i]
				xorSymbol(C,(B + b) * t, C, j, t);
			}
			
			// Generate SxP G_LDPC2
			for(int j=0; j<S; j++){

				int a = j % P;
				int b = (j + 1) % S;

				// D[i] = D[i] + C[W+a] + C[W+b]
				// D[i] = D[i] + C[W+a]
				xorSymbol(C, (B + j) * t, C, (W + a) * t, t);
				
				// D[i] = D[i] + C[W+b]
				xorSymbol(C, (B + j) * t, C, (W + b) * t, t);				
			}
			*/
			
			
		}
		
	}
	
	public Map<SourceBlock, Tuple[]> generateTuples(SourceBlock[] object){
		
		Map<SourceBlock, Tuple[]> sbTuple = new HashMap<SourceBlock, Tuple[]>();
		
		for(long i=0; i<object.length; i++){
			SourceBlock sb = object[(int)i];
			
			int kLinha = sb.getSymbols().length;
			Tuple[] tuples = new Tuple[(int)kLinha];
			
			for(long j=0; j<kLinha; j++){
				tuples[(int)j] = new Tuple(kLinha, j);
			}
			
			sbTuple.put(sb, tuples);
		}

		return sbTuple;
	}
	
	public byte[] enc(int K, byte[] C, Tuple tuple){
		
		int S = SystematicIndices.S(K);
		int H = SystematicIndices.H(K);
		int W = SystematicIndices.W(K);
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
	
	public byte[] xorSymbol(byte[] s1, byte[] s2){
		
		byte[] xor = new byte[(int)T];
		
		for(long i=0; i<T; i++){
			xor[(int)i] = (byte) (s1[(int)i] ^ s2[(int)i]);
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
}

class Tuple{
	
	long d,a,b,d1,a1,b1;
	
	Tuple(int K, long X){
		
		int S = SystematicIndices.S(K);
		int H = SystematicIndices.H(K);
		int W = SystematicIndices.W(K);
		int L = K + S + H;
		int J = SystematicIndices.J(K);
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
