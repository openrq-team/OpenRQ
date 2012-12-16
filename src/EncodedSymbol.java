
public class EncodedSymbol {

	private long   ISI;
	private byte[] data;
	
	
	public EncodedSymbol(long iSI, byte[] data) {
		super();
		ISI = iSI;
		this.data = data;
	}
	
	public long getISI() {
		return ISI;
	}

	public void setISI(long iSI) {
		ISI = iSI;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
}
