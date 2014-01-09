package RQLibrary;

public class Partition {

	private int Il;
	private int Is;
	private int Jl;
	private int Js;
	
	public Partition(int I, int J) {
		
		//if(I < 0 || J < 1) throw new IllegalArgumentException("All arguments must be positive.");
		
		Il = Encoder.ceil((double)I/J);
		Is = Encoder.floor((double)I/J);
		Jl = I - (Is * J);
		Js = J - Jl;
	}

	public int get(int i){
		
		switch(i){
			case 1:
				return Il;
			case 2:
				return Is;
			case 3:
				return Jl;
			case 4:
				return Js;
			default:
				throw new IllegalArgumentException("Argument must be from 1 to 4.");
		}
		
	}
}
