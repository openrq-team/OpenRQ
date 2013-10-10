package RQLibrary;

public class SourceBlock {

	private int SBN; // Source Block Number
	private byte[] symbols; 
	private int K; // Number of source symbols
	private int T; // Size of each source symbol
	
	protected SourceBlock(int sBN, byte[] sblocks, int t, int k){
		
		/*
		if(sBN < 0) throw new IllegalArgumentException("Source Block Number must be non-negative.");
		if(t   < 1) throw new IllegalArgumentException("Size of source symbol must be positive.");
		if(k   < 1) throw new IllegalArgumentException("Number of source symbols must be positive.");
		if(sblocks == null || sblocks.length == 0) throw new IllegalArgumentException("Array of source symbols must be initialized/allocated.");
*/
		
		this.SBN = sBN;
		this.symbols = sblocks;
		this.T = t;
		this.K = k;
	}
	
	public int getK() {
		
		return K;
	}

	protected int getSBN() {
		
		return SBN;
	}

	protected byte[] getSymbols() {
		
		return symbols;
	}
	
	protected int getT() {
		
		return T;
	}
}