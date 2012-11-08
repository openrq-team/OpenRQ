

public class Partition {

	private int Is;
	private int Il;
	private int Js;
	private int Jl;
	
	public Partition(int I, int J) {
		Il = Encoder.ceil((double)I/J);
		Is = Encoder.floor((double)I/J);
		Jl = I - (Is*J);
		Js = J - Jl;
	}
	
	// Metodo mais estupido da minha vida
	/*
	public static Partition partition(int I, int J){
		int Is, Il, Js, Jl;
		
		Il = Encoder.ceil(I/J);
		Is = Encoder.floor(I/J);
		Jl = I - (Is*J);
		Js = J - Jl;
		
		return new Partition(Is, Il, Js, Jl);
	}*/

	//TODO change to generic gets and sets
	public int getIs() {
		return Is;
	}

	public void setIs(int is) {
		Is = is;
	}

	public int getIl() {
		return Il;
	}

	public void setIl(int il) {
		Il = il;
	}

	public int getJs() {
		return Js;
	}

	public void setJs(int js) {
		Js = js;
	}

	public int getJl() {
		return Jl;
	}

	public void setJl(int jl) {
		Jl = jl;
	}
	
	
	
}
