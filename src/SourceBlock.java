
public class SourceBlock {

	private short SBN;
	private SubBlock[] sub_blocks;
	
	public SourceBlock(short sBN, SubBlock[] sblocks){
		this.SBN = sBN;
		this.sub_blocks = sblocks;
	}

	public short getSBN() {
		return SBN;
	}

	public void setSBN(short sBN) {
		SBN = sBN;
	}

	public SubBlock[] getSub_blocks() {
		return sub_blocks;
	}

	public void setSub_blocks(SubBlock[] sub_blocks) {
		this.sub_blocks = sub_blocks;
	}
	
}