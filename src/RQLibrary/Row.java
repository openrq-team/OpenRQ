package RQLibrary;

import java.util.Set;

class Row{
	
	public int id;
	public int nonZeros;
	public int degree;
	public Set<Integer> edges = null;
	
	protected Row(int i, int r, int d){
		
		id = i;
		nonZeros = r;
		degree = d;
	}
	
	protected Row(int i, int r, int d, Set<Integer> e){
	
		id = i;
		nonZeros = r;
		degree = d;
		edges = e;
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