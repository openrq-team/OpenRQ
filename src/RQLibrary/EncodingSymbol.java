package RQLibrary;

import java.io.Serializable;

public final class EncodingSymbol implements Comparable<EncodingSymbol>, Serializable{

	private static final long serialVersionUID = 2326119933571426817L;
	private int SBN;
	private int ESI;
	private byte[] data;
	
	public EncodingSymbol(int sBN, int eSI, byte[] data) {
		
		/*
		if(sBN < 0) throw new IllegalArgumentException("Source Block Number must be non-negative.");
		if(eSI < 0) throw new IllegalArgumentException("Encoding Symbol ID must be non-negative.");
		if(data == null || data.length == 0) throw new IllegalArgumentException("The encoding symbol must be initialized/allocated.");
		*/
		
		SBN = sBN;
		ESI = eSI;
		this.data = data;
	}
	
	public int getSBN() {
		
		return SBN;
	}

	public int getESI() {
		
		return ESI;
	}
	
	public byte[] getData() {
		
		return data;
	}
	
	public int getISI(int K){
		
		int kLinha = SystematicIndices.ceil(K);
		
		return ESI + (kLinha - K); // yes, I know its comutative: it's just for a better code reading experience.
	}

	@Override
	public int compareTo(EncodingSymbol arg0) {
		
		EncodingSymbol s1 = (EncodingSymbol) this;
		EncodingSymbol s2 = (EncodingSymbol) arg0;
		
		if(s1.getESI()<s2.getESI()){
			
			return -1;
		}
		else{
			
			if(s1.getESI()>s2.getESI()){
				
				return 1;
			}
			else{
				
				return 0;
			}
		}
	}
}