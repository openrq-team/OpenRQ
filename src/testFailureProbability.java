import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class testFailureProbability {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 4){
			StringBuilder s = new StringBuilder();
			s.append("Usage: \n");
			s.append("    java -jar testFailureProbability K LOSS OVERHEAD N\n");
			s.append("        - K       : \n");
			s.append("        - LOSS    : \n");
			s.append("        - OVERHEAD: \n");
			s.append("        - N       : Number of times to repeat the experience (usually between 10^7 and 2*10^7).\n");			
			System.out.println(s.toString());
		}
		else{
			
			int K = Integer.valueOf(args[0]);
			int L = Integer.valueOf(args[1]);
			int O = Integer.valueOf(args[2]);
			int N = Integer.valueOf(args[3]);
			
			test(K, L, O, N);
		}
	}

	public static int test(int K, int LOSS, int OVERHEAD, int N){
		
		Random rand = new Random(System.currentTimeMillis() / K + System.nanoTime());
		byte[] data = new byte[K];

		try{
			Encoder.INIT_REPAIR_SYMBOL = 0;
			
			int failed_runs = 0;
			int run;
			for(run = 0; run < N; run++){

				rand.nextBytes(data);

				Encoder enc = new Encoder(data, LOSS, OVERHEAD);
				EncodingPacket[] encoded_symbols = null;
				try {
					encoded_symbols = enc.encode(enc.partition());
				} catch (SingularMatrixException e) {
					e.printStackTrace();
					System.err.println(e.getMessage());
					System.err.println("\nBOOM @ Encoding !?!??! \n\n ABORT \n");
					System.exit(-113);
				}

				try {
					enc.unPartition(enc.decode(encoded_symbols));
				} catch (SingularMatrixException e) {
					failed_runs++;
					//System.out.println("EUREKA");
				}
				
				if(run % 10000 == 0){
					File file = new File("results/" + K + "_" + LOSS + "_" + OVERHEAD + ".txt");

					if (file.exists()) {
						file.createNewFile();
					}

					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					
					bw.write(" - K: " + K + "\n - Loss: " 
							+ LOSS + "\n - Overhead: " + OVERHEAD + "\n ");
					bw.flush();
					
					bw.write("\n LATEST RUN : " + run + 
							 "\n FAILED RUNS: " + failed_runs);
					bw.flush();
					bw.close();
				}
			}

			File file = new File("results/" + K + "_" + LOSS + "_" + OVERHEAD + ".txt");
			if (file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(" - K: " + K + "\n - Loss: " 
					+ LOSS + "\n - Overhead: " + OVERHEAD + "\n ");
			bw.flush();
			
			bw.write("\n LATEST RUN : " + run + 
					 "\n FAILED RUNS: " + failed_runs);
			bw.flush();
			bw.close();
			
			return failed_runs;
		}catch(IOException e){
			e.printStackTrace();
			System.err.println("IO Error, aborting...");
			System.exit(-331);
		}
		
		return -1;
	}	
}
