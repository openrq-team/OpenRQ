
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Encoder {
	
	public static final short MAX_PAYLOAD_SIZE = 512; // P
	public static final short ALIGN_PARAM = 4; // Al
	public static final int MAX_SIZE_BLOCK = 76800; // WS // B
	public static final int SSYMBOL_LOWER_BOUND = 8; // SS
	public static final int KMAX = 56403;
	
	private int T; // symbol size
	private int Z; // number of source blocks
	private int N; // number of sub-blocks in each source block
	private int F; // transfer length
	private int Kt; // total number of symbols required to represent the source data of the object
	private byte[] data;
	
	public Encoder(byte[] file){
		
		F = file.length;
		
		data = new byte[F];
		
		for(int i=0; i<F; i++)
			data[i] = file[i];
		
		T = derivateT(MAX_PAYLOAD_SIZE);
		Kt = ceil((double)F/T);
		Z = derivateZ();
		N = derivateN();
	}

	private short derivateT(short pLinha){
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
		
		int j, kl;
		short i;
		
		for(i=0,j=0; i<ZL; i++, j+=KL*T){
			
			byte[] aux = Arrays.copyOfRange(data, j, j+KL*T);
			SubBlock[] sblocks = new SubBlock[N+1]; // +1 for extension (padding K'-K)
			
			int n, k;
			for(n=0,k=0; n<NL; n++, k+=KL*TL*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KL*TL*ALIGN_PARAM));
			}
			
			for(int x=0; x<NS; x++, n++, k+=KL*TS*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KL*TS*ALIGN_PARAM));
			}
			
			sblocks[N] = new SubBlock(new byte[(SystematicIndices.ceil(KL)-KL)*T]);
			
			object[i] = new SourceBlock(i,sblocks,KL);
			
		}
		
		for(int z=0; z<ZS; z++, i++, j+=KS*T){
			
			byte[] aux = Arrays.copyOfRange(data, j, j+KS*T);
			SubBlock[] sblocks = new SubBlock[N+1]; // +1 for extension (padding K'-K)
			
			int n, k;
			for(n=0,k=0; n<NL; n++, k+=KL*TL*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KS*TL*ALIGN_PARAM));
			}
			
			for(int x=0; x<NS; x++, n++, k+=KL*TS*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KS*TS*ALIGN_PARAM));
			}
			
			sblocks[N] = new SubBlock(new byte[(SystematicIndices.ceil(KS)-KS)*T]);
			
			object[i] = new SourceBlock(i,sblocks,KS);
		}
		
		return object;
	}
	
	public void encode(SourceBlock[] object){
		
		generateIntermediateSymbols(object);
		
		
	}
	
	public void generateIntermediateSymbols(SourceBlock[] object){
		
		Map<SourceBlock, Tuple[]> sbTuple = generateTuples(object);
		
		
		
	}
	
	public Map<SourceBlock, Tuple[]> generateTuples(SourceBlock[] object){
		
		Map<SourceBlock, Tuple[]> sbTuple = new HashMap<SourceBlock, Tuple[]>();
		
		for(int i=0; i<object.length; i++){
			SourceBlock sb = object[i];
			
			int kLinha = sb.getSub_blocks().length;
			Tuple[] tuples = new Tuple[kLinha];
			
			for(int j=0; j<kLinha; j++){
				tuples[j] = new Tuple(kLinha, j);
			}
			
			sbTuple.put(sb, tuples);
		}

		return sbTuple;
	}
	
	public void preCodingRelation(int k){
		 
		int S = SystematicIndices.S(k);
		int H = SystematicIndices.H(k);
		int W = SystematicIndices.W(k);
		int L = k + S + H;
		int P = L - W;
		int U = P - H;
		int B = W - S;
		
		int[] D = new int[S];
		
		for(int i=0; i<S; i++){
			// D[i] = C[B+i];
		}
	}
	
	public byte[] enc(int K, byte[] C, Tuple tuple){
		
		int S = SystematicIndices.S(K);
		int H = SystematicIndices.H(K);
		int W = SystematicIndices.W(K);
		int L = K + S + H;
		int P = L - W;
		int B = W - S;
		int P1 = ceilPrime(P);
		int d = tuple.getD();
		int a = tuple.getA();
		int b = tuple.getB();
		int d1 = tuple.getD1();
		int a1 = tuple.getA1();
		int b1 = tuple.getB1();
		
		byte[] result = Arrays.copyOfRange(C, b, b+T);
		
		for(int j=0; j<d; j++){
			b = (b+a) % W;
			result = //TODO CONTINUAR
		}
		
		
		
	}
	
	public int ceilPrime(int p){
		
		while(!isPrime(p)) p++;
		
		return p;
	}
	
	public boolean isPrime(int n){
		//check if n is a multiple of 2
	    if (n%2==0) return false;
	    //if not, then just check the odds
	    for(int i=3;i*i<=n;i+=2) {
	        if(n%i==0)
	            return false;
	    }
	    return true;
	}
}

class Tuple{
	
	int d,a,b,d1,a1,b1;
	
	Tuple(int K, int X){
		
	}

	public int getD() {
		return d;
	}

	public void setD(int d) {
		this.d = d;
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getD1() {
		return d1;
	}

	public void setD1(int d1) {
		this.d1 = d1;
	}

	public int getA1() {
		return a1;
	}

	public void setA1(int a1) {
		this.a1 = a1;
	}

	public int getB1() {
		return b1;
	}

	public void setB1(int b1) {
		this.b1 = b1;
	}
	
}
