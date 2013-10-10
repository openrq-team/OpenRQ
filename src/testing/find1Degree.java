package testing;

import java.util.Set;
import RQLibrary.Encoder;
import RQLibrary.SystematicIndices;
import RQLibrary.Tuple;

public class find1Degree {

	public static void main(String[] args){
		
		int K = 10;
		
		while(K <= 56403){

			int Ki = SystematicIndices.getKIndex(K);
			int S = SystematicIndices.S(Ki);
			int H = SystematicIndices.H(Ki);
			int L = K + S + H;

			for (int row = S + H; row < L; row++) {

				Tuple tuple = new Tuple(K, row - S - H);

				Set<Integer> indexes = Encoder.encIndexes(K, tuple);

				if(indexes.size() == 1)
					System.out.println("\nFound! - K: "+K+" row: "+(row-S-H));
			}

			K = SystematicIndices.ceil(++K);
		}
	}
}