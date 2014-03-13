/* 
 * Copyright 2014 Jose Lopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testing;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import RQLibrary.Encoder;
import RQLibrary.EncodingPacket;
import RQLibrary.SingularMatrixException;
import RQLibrary.SourceBlock;


public class testHashAndMeasureTime {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length != 4){
			StringBuilder s = new StringBuilder();
			s.append("Usage: \n");
			s.append("    java -jar testHashAndMeasureTime K LOSS OVERHEAD N\n");
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
		
		int failed_runs = 0, media = 0;
		for(int run = 0; run < N; run++){

			rand.nextBytes(data);

			byte[] antes = null;
			 try {
				 MessageDigest md = MessageDigest.getInstance("SHA-512");
				 md.update(data);
			     antes = md.digest();
			 } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			EncodingPacket[] encoded_symbols = null;
			Encoder enc = new Encoder(data, LOSS, OVERHEAD);
			SourceBlock[] blocks = enc.partition();
			
			long tempoAntes = System.currentTimeMillis();
			
			try {
				encoded_symbols = enc.encode(blocks);
			} catch (SingularMatrixException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
				System.err.println("\nBOOM @ Encoding !?!??! \n\n ABORT \n");
				System.exit(-113);
			}

			byte[] newdata = null;
			try {
				blocks = Encoder.decode(encoded_symbols);
			} catch (SingularMatrixException e) {
				run--;
				failed_runs++;
				continue;
			}

			long tempoDepois = System.currentTimeMillis();

			newdata = enc.unPartition(blocks);
			
			media += tempoDepois - tempoAntes;

			byte[] depois = null;
			try {
				 MessageDigest md = MessageDigest.getInstance("SHA-512");
			     md.update(newdata);
			     depois = md.digest();
			 } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			boolean igual = Arrays.equals(antes, depois);
			
			if(!igual){
				
				media = media / (run+1);
				
				System.err.println("HASH MISMATCH after "+run+" runs! (tempo medio parcial: "+media+"ms)");

				System.out.println(Arrays.toString(data));
				System.err.println(Arrays.toString(newdata));

				System.exit(-2391);
			}
		}

		media = media / N;
		
		System.out.println("SUCCESS - tempo medio: "+media+" (failed: "+failed_runs+" times)");
		return 0;
	}	
}
