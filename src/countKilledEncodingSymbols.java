import java.util.Set;


public class countKilledEncodingSymbols {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int OVERHEAD = 2;
		int esi;
		int attacked = -1;
		
		for(int run = 0; run < targetK.length; run++, attacked = -1){

			int K = targetK[run];

			byte[][] constraint_matrix = Encoder.generateConstraintMatrix(K, 1);
			int L = constraint_matrix.length;
			
			for(esi = 0; attacked < OVERHEAD; esi++){
				
				Set<Integer> indexes = Encoder.encIndexes(K, new Tuple(K, K+esi));
				
				byte[] newLine = new byte[L];
				for(Integer col : indexes)
						newLine[col] = 1;
				
				constraint_matrix[L-1] = newLine;
				
				byte[][] A = (new Matrix(constraint_matrix)).getData();
				
				Encoder.reduceToRowEchelonForm(A, 0, L, 0, L);
								
				if(!Encoder.validateRank(A, 0, 0, L, L, L)){
					attacked++;
					int lost = esi + 1 + attacked;
					System.out.println("K': " + K + " Lost: "+ lost + " Overhead: "+attacked);
				}
			}
		}
		
	}
	
	public boolean success(byte[][] matrix){
		
		if(matrix[matrix.length-1][matrix[0].length-2] == 0)
			return true;
		else
			return false;
	}
	
	public static int[] targetK = {
		10,
		26,
		32,
		42,
		55,
		62,
		75,
		84,
		91,
		101,
		153,
		200,
		248,
		301,
		355,
		405,
		453,
		511,
		549,
		600,
		648,
		703,
		747,
		802,
		845,
		903,
		950,
		1002,
		2005,
		3015,
		4015,
		5008,
		6039,
		7033,
		8030,
		9019,
		10017,
		20152,
		30037,
		40398,
		50511,
		56403		
	};
}
