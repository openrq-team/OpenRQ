
public class SourceBlock {

	private long SBN;
	private byte[] symbols;
	private long K;
	public  long T, N; // TODO remover
	
	public SourceBlock(long sBN, byte[] sblocks, long t, long n, long k){
		this.SBN = sBN;
		this.symbols = sblocks;
		this.T = t;
		this.N = n;
		this.K = k;
	}
	
	public long getK() {
		return K;
	}

	public void setK(long k) {
		K = k;
	}

	public long getSBN() {
		return SBN;
	}

	public void setSBN(long sBN) {
		SBN = sBN;
	}

	public byte[] getSymbols() {
		return symbols;
	}

	public void setSymbols(byte[] symbols) {
		this.symbols = symbols;
	}

	public long getT() {
		return T;
	}

	public void setT(long t) {
		T = t;
	}

	public long getN() {
		return N;
	}

	public void setN(long n) {
		N = n;
	}
	
}