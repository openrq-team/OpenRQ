package RQLibrary;

public class Partition {

	private int Is;
	private int Il;
	private int Js;
	private int Jl;
	
	public Partition(int I, int J) {
		
		//if(I < 0 || J < 1) throw new IllegalArgumentException("All arguments must be positive.");
		
		Il = Encoder.ceil((double)I/J);
		Is = Encoder.floor((double)I/J);
		Jl = I - (Is*J);
		Js = J - Jl;
	}
	
	public int get(int i){
		
		switch(i){
			case 1:
				return Is;
			case 2:
				return Il;
			case 3:
				return Js;
			case 4:
				return Jl;
			default:
				throw new IllegalArgumentException("Argument must be from 1 to 4.");
		}
		
	}
}
