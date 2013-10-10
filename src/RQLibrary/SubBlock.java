package RQLibrary;

class SubBlock {

	private byte[] data;
	private long T;
	
	protected SubBlock(byte[] d, long t){
		
		this.data = d;
		this.T = t;
	}

	protected long getT() {
		
		return T;
	}

	protected void setT(long t) {
		
		T = t;
	}

	protected byte[] getData() {
		
		return data;
	}

	protected void setData(byte[] data) {
		
		this.data = data;
	}	
}