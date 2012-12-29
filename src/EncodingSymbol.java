
public class EncodingSymbol {

	private int   ESI;
	private byte[] data;
	
	public EncodingSymbol(int eSI, byte[] data) {
		
		if(eSI < 0) throw new IllegalArgumentException("Encoding Symbol ID must be non-negative.");
		if(data == null || data.length == 0) throw new IllegalArgumentException("The encoding symbol must be initialized/allocated.");
		
		ESI = eSI;
		this.data = data;
	}
	
	public int getESI() {
		
		return ESI;
	}
	
	public byte[] getData() {
		
		return data;
	}
	
	public int getISI(int K){
		
		int kLinha = SystematicIndices.ceil(K);
		
		return ESI + (kLinha - K); // yes, i know its comutative: it's just for better code reading experience.
	}
}
