
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Encoder {
	
	public static final long MAX_PAYLOAD_SIZE = 512; // P
	public static final long ALIGN_PARAM = 4; // Al
	public static final long MAX_SIZE_BLOCK = 76800; // WS // B
	public static final long SSYMBOL_LOWER_BOUND = 8; // SS
	public static final long KMAX = 56403;
	
	private long T; // symbol size
	private long Z; // number of source blocks
	private long N; // number of sub-blocks in each source block
	private long F; // transfer length
	private long Kt; // total number of symbols required to represent the source data of the object
	private byte[] data;
	
	public Encoder(byte[] file){
		
		F = file.length;
		
		data = new byte[(int) F];
		
		for(long i=0; i<F; i++)
			data[(int) i] = file[(int) i];
		
		T = derivateT(MAX_PAYLOAD_SIZE);
		Kt = ceil((double)F/T);
		Z = derivateZ();
		N = derivateN();
	}

	private long derivateT(long pLinha){
		return pLinha;		
	}
	
	private long derivateZ(){
				
		long N_max = floor((double)T/(SSYMBOL_LOWER_BOUND*ALIGN_PARAM));
		
		return(ceil((double)Kt/SystematicIndices.KL(N_max, MAX_SIZE_BLOCK, ALIGN_PARAM, T)));
	}
	
	private long derivateN(){
		
		long N_max = floor((double)(T/(SSYMBOL_LOWER_BOUND*ALIGN_PARAM)));

		long n = 1;
		for(; n<=N_max && ceil((double)Kt/Z)>SystematicIndices.KL(n, MAX_SIZE_BLOCK, ALIGN_PARAM, T); n++);
		
		return n;
	}
	
	public static long ceil(double x){
		return((long)Math.ceil(x));
	}
	
	public static long floor(double x){
		return((long)Math.floor(x));
	}
	
	public SourceBlock[] partition(){
		
		Partition KZ = new Partition(Kt, Z);
		long KL = KZ.getIl();
		long KS = KZ.getIs();
		long ZL = KZ.getJl();
		long ZS = KZ.getJs();
		
		Partition TN = new Partition(T/ALIGN_PARAM, N);
		long TL = TN.getIl();
		long TS = TN.getIs();
		long NL = TN.getJl();
		long NS = TN.getJs();
		
		SourceBlock.NL = NL;
		SourceBlock.NS = NS;
		SourceBlock[] object = new SourceBlock[(int) Z];
		
		long j;
		long i;
		
		for(i=0,j=0; i<ZL; i++, j+=KL*T){
			
			byte[] aux = Arrays.copyOfRange(data, (int)j, (int)(j+KL*T));
			SubBlock[] sblocks = new SubBlock[(int)N+1]; // +1 for extension (padding K'-K)
			
			long n, k;
			for(n=0,k=0; n<NL; n++, k+=KL*TL*ALIGN_PARAM){
				sblocks[(int)n] = new SubBlock(Arrays.copyOfRange(aux,(int)k, (int)(k+KL*TL*ALIGN_PARAM)),TL*ALIGN_PARAM);
			}
			
			for(long x=0; x<NS; x++, n++, k+=KL*TS*ALIGN_PARAM){
				sblocks[(int)n] = new SubBlock(Arrays.copyOfRange(aux, (int)k, (int)(k+KL*TS*ALIGN_PARAM)),TS*ALIGN_PARAM );
			}
			
			sblocks[(int)N] = new SubBlock(new byte[(int)((SystematicIndices.ceil(KL)-KL)*T)],T);
			
			object[(int)i] = new SourceBlock(i,sblocks,KL);
			
		}
		
		for(long z=0; z<ZS; z++, i++, j+=KS*T){
			
			byte[] aux = Arrays.copyOfRange(data, (int)j, (int)(j+KS*T));
			SubBlock[] sblocks = new SubBlock[(int)N+1]; // +1 for extension (padding K'-K)
			
			long n, k;
			for(n=0,k=0; n<NL; n++, k+=KL*TL*ALIGN_PARAM){
				sblocks[(int)n] = new SubBlock(Arrays.copyOfRange(aux, (int)k, (int)(k+KS*TL*ALIGN_PARAM)),TL*ALIGN_PARAM);
			}
			
			for(long x=0; x<NS; x++, n++, k+=KL*TS*ALIGN_PARAM){
				sblocks[(int)n] = new SubBlock(Arrays.copyOfRange(aux, (int)k, (int)(k+KS*TS*ALIGN_PARAM)),TS*ALIGN_PARAM);
			}
			
			sblocks[(int)N] = new SubBlock(new byte[(int)((SystematicIndices.ceil(KS)-KS)*T)],T);
			
			object[(int)i] = new SourceBlock(i,sblocks,KS);
		}
		
		return object;
	}
	
	public void encode(SourceBlock[] object){
		
		generateIntermediateSymbols(object);
		
		
	}
	
	public void generateIntermediateSymbols(SourceBlock[] object){
		
		Map<SourceBlock, Tuple[]> sbTuple = generateTuples(object);
		
		for(int i=0; i<object.length; i++){
			long K = SystematicIndices.ceil(object[i].getK());
			long S = SystematicIndices.S(K);
			long H = SystematicIndices.H(K);
			long W = SystematicIndices.W(K);
			long L = K + S + H;
			
			
			
		}
		
	}
	
	public Map<SourceBlock, Tuple[]> generateTuples(SourceBlock[] object){
		
		Map<SourceBlock, Tuple[]> sbTuple = new HashMap<SourceBlock, Tuple[]>();
		
		for(long i=0; i<object.length; i++){
			SourceBlock sb = object[(int)i];
			
			long kLinha = sb.getSub_blocks().length;
			Tuple[] tuples = new Tuple[(int)kLinha];
			
			for(long j=0; j<kLinha; j++){
				tuples[(int)j] = new Tuple(kLinha, j);
			}
			
			sbTuple.put(sb, tuples);
		}

		return sbTuple;
	}
	
	public byte[] enc(long K, byte[] C, Tuple tuple){
		
		long S = SystematicIndices.S(K);
		long H = SystematicIndices.H(K);
		long W = SystematicIndices.W(K);
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
	
	Tuple(long K, long X){
		
		long S = SystematicIndices.S(K);
		long H = SystematicIndices.H(K);
		long W = SystematicIndices.W(K);
		long L = K + S + H;
		long J = SystematicIndices.J(K);
		long P = L - W;
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
