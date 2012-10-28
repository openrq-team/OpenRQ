
import java.lang.reflect.Array;
import java.util.Arrays;


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
		
		int j;
		short i;
		for(i=0,j=0; i<ZL; i++, j+=KL*T){
			
			byte[] aux = Arrays.copyOfRange(data, j, j+KL*T);
			SubBlock[] sblocks = new SubBlock[N];
			
			int n, k;
			for(n=0,k=0; n<NL; n++, k+=KL*TL*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KL*TL*ALIGN_PARAM));
			}
			
			for(int x=0; x<NS; x++, n++, k+=KL*TS*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KL*TS*ALIGN_PARAM));
			}
			
			object[i] = new SourceBlock(i,sblocks);
			
		}
		
		for(int z=0; z<ZS; z++, i++, j+=KS*T){
			byte[] aux = Arrays.copyOfRange(data, j, j+KS*T);
			SubBlock[] sblocks = new SubBlock[N];
			
			int n, k;
			for(n=0,k=0; n<NL; n++, k+=KL*TL*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KS*TL*ALIGN_PARAM));
			}
			
			for(int x=0; x<NS; x++, n++, k+=KL*TS*ALIGN_PARAM){
				sblocks[n] = new SubBlock(Arrays.copyOfRange(aux, k, k+KS*TS*ALIGN_PARAM));
			}
			
			object[i] = new SourceBlock(i,sblocks);
		}
		
		return object;
	}
	
}
