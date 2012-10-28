package siemens;

public class Encoder {

	/* Static */
	
	/**
	 * A symbol alignment parameter, in bytes.
	 */
	public static final int A = 4; 
	
	/**
	 * A target on the sub-block size, in bytes.
	 */
	public static final int W = 262144;
	
	/**
	 * The maximum packet payload size, in bytes, which is assumed to be a multiple of A.
	 */
	public static final int P = 512;
	
	/**
	 * The maximum number of source symbols per source block.
	 */
	public static final int KMAX = 8192;
	
	/**
	 * The minimum target on the number of symbols per source block.
	 */
	public static final int KMIN = 1024;
	
	/**
	 * A maximum target number of symbols per packet.
	 */
	public static final int GMAX = 10;
	
	/**
	 * The maximum source block size, in bytes.
	 */
	public static final int B;
	
	/* Intance */
	
	/**
	 * The symbol size, in bytes, which must be a multiple of A.
	 */
	private int T;
	
	/**
	 * The size of the file, in bytes.
	 */
	private int F;
	
	/**
	 * The number of source blocks.
	 */
	private int Z;
	
	/**
	 * The number of sub-blocks in each source block.
	 */
	private int N;
	
	/**
	 * File.
	 */
	private byte[] file;
	
	/**
	 * The number of sub-blocks in each source block.
	 */
	
	/* Methods */
	
	public static int ceil(double a){
		return((int) Math.ceil(a));
	}
	
	public static int floor(double a){
		return((int) Math.floor(a));
	}
	
	public Encoder(byte[] f){
		this.F = f.length;
		this.file = new byte[F];
		
		for(int i=0; i<F; i++){
			this.file[i] = f[i];
		}
		
		int G = Math.min(Encoder.ceil((P*KMIN)/F), P/A);
		G = Math.min(G, GMAX); //TODO increase to the nearest power of two
							   //TODO divides P/A, to use packet size P
		
		this.T = Encoder.floor(P/(A*G))*A;
		
		int Kt = Encoder.ceil(F/T);
		
		this.Z = Encoder.ceil(Kt/KMAX);
		
		this.N = Math.min(Encoder.ceil(Encoder.ceil(Kt/Z)*T/W), T/A); //TODO increase to the nearest power of two
	}
}
