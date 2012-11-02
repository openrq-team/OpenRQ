
public class SubBlock {

	private byte[] data;
	private long T;
	
	public SubBlock(byte[] d, long t){
		this.data = d;
		this.T = t;
	}

	public long getT() {
		return T;
	}

	public void setT(long t) {
		T = t;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	
	
}
