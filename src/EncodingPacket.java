	
public class EncodingPacket {
	
	private int SBN; // Source Block Number
	private int K; // Number of source symbols
	private int T; // Size of each source symbol
	private EncodingSymbol[] encoding_symbols;

	public EncodingPacket(int sBN, EncodingSymbol[] encoding_symbols, int k, int t) {

		if(sBN < 0) throw new IllegalArgumentException("Source Block Number must be non-negative.");
		if(encoding_symbols == null || encoding_symbols.length == 0) throw new IllegalArgumentException("The array of encoding symbols must be initialized/allocated.");
		if(t   < 1) throw new IllegalArgumentException("Size of source symbol must be positive.");
		if(k   < 1) throw new IllegalArgumentException("Number of source symbols must be positive.");
		
		SBN = sBN;
		this.encoding_symbols = encoding_symbols;
		K = k;
		T = t;
	}

	public int getK() {
		return K;
	}

	public int getT() {
		return T;
	}

	public int getSBN() {
		
		return SBN;
	}

	public EncodingSymbol[] getEncoding_symbols() {
		
		return encoding_symbols;
	}	
}
