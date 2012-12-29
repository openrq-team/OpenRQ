	
public class EncodingPacket {
	
	int SBN; // Source Block Number
	
	EncodingSymbol[] encoding_symbols;

	public EncodingPacket(int sBN, EncodingSymbol[] encoding_symbols) {

		if(sBN < 0) throw new IllegalArgumentException("Source Block Number must be non-negative.");
		if(encoding_symbols == null || encoding_symbols.length == 0) throw new IllegalArgumentException("The array of encoding symbols must be initialized/allocated.");
		
		SBN = sBN;
		this.encoding_symbols = encoding_symbols;
	}

	public int getSBN() {
		
		return SBN;
	}

	public EncodingSymbol[] getEncoding_symbols() {
		
		return encoding_symbols;
	}	
}
