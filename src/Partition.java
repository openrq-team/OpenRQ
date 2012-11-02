

public class Partition {

	private long Is;
	private long Il;
	private long Js;
	private long Jl;
	
	public Partition(long I, long J) {
		Il = Encoder.ceil((double)I/J);
		Is = Encoder.floor((double)I/J);
		Jl = I - (Is*J);
		Js = J - Jl;
	}
	
	// Metodo mais estupido da minha vida
	/*
	public static Partition partition(long I, long J){
		long Is, Il, Js, Jl;
		
		Il = Encoder.ceil(I/J);
		Is = Encoder.floor(I/J);
		Jl = I - (Is*J);
		Js = J - Jl;
		
		return new Partition(Is, Il, Js, Jl);
	}*/

	public long getIs() {
		return Is;
	}

	public void setIs(long is) {
		Is = is;
	}

	public long getIl() {
		return Il;
	}

	public void setIl(long il) {
		Il = il;
	}

	public long getJs() {
		return Js;
	}

	public void setJs(long js) {
		Js = js;
	}

	public long getJl() {
		return Jl;
	}

	public void setJl(long jl) {
		Jl = jl;
	}
	
	
	
}
