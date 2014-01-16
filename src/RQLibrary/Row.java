package RQLibrary;

import java.util.Set;

class Row{
	
	public int id;
	public int nonZeros;
	public int degree;
	public Set<Integer> edges = null;
	public boolean isHDPC;
	
	protected Row(int i, int r, int d, boolean hdpc){
		
		id = i;
		nonZeros = r;
		degree = d;
		isHDPC = hdpc;
	}
	
	protected Row(int i, int r, int d, boolean hdpc, Set<Integer> e){
	
		id = i;
		nonZeros = r;
		degree = d;
		edges = e;
		isHDPC = hdpc;
	}
	
	@Override
	public boolean equals(Object o){
		
		if(!o.getClass().getName().equals(this.getClass().getName())) 
			return false;
		else
			if(((Row) o).id == this.id)
				return true;
			else
				return false;
	}	
}