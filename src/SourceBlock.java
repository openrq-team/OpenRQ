
public class SourceBlock {

	private long SBN;
	private SubBlock[] sub_blocks;
	private long K;
	public static long NL, NS; // TODO remover
	
	public SourceBlock(long sBN, SubBlock[] sblocks, long k){
		this.SBN = sBN;
		this.sub_blocks = sblocks;
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

	public SubBlock[] getSub_blocks() {
		return sub_blocks;
	}

	public void setSub_blocks(SubBlock[] sub_blocks) {
		this.sub_blocks = sub_blocks;
	}
	
}