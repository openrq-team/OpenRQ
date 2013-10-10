package RQLibrary;

public class Tuple{

	private long d,a,b,d1,a1,b1;

	public Tuple(int K, long X){

		int Ki = SystematicIndices.getKIndex(K);
		int S = SystematicIndices.S(Ki);
		int H = SystematicIndices.H(Ki);
		int W = SystematicIndices.W(Ki);
		int L = K + S + H;
		int J = SystematicIndices.J(Ki);
		int P = L - W;
		long P1 = Utilities.ceilPrime(P);

		long A = 53591 + J*997;
		if(A % 2 == 0)
			A++;

		long B = 10267 * (J+1);

		long y = (B + X*A) % 4294967296L; // 2^^32

		long v = Rand.rand(y, 0, 1048576L); // 2^^20

		this.d = Deg.deg(v,W);
		this.a = 1 + Rand.rand(y, 1, W-1);
		this.b = Rand.rand(y, 2, W);
		if(this.d<4)
			d1 = 2 + Rand.rand(X, 3, 2L);
		else
			d1 = 2;
		this.a1 = 1 + Rand.rand(X, 4, P1-1);
		this.b1 = Rand.rand(X, 5, P1);
	}

	protected long getD() {
		
		return d;
	}

	protected long getA() {
		
		return a;
	}

	protected long getB() {
		
		return b;
	}

	protected long getD1() {
		
		return d1;
	}

	protected long getA1() {
		
		return a1;
	}

	protected long getB1() {
		
		return b1;
	}
}