import java.io.IOException;
import java.util.List;
import java.util.Random;


public class Main {


	public static void main(String[] args) {
		
		/* TEST VECTORS */
		/*
		System.out.println("\n\nTABELA\n");
		for(int i=0; i<510; i++)
			System.out.println(i + ": " + (int)OctectOps.OCT_EXP[i]);
		
		byte[][] m2 = {
				{10, 15, 20},
				{20, 15, 10},
				{15, 10, 20}
		};
		
		byte[][] m3 = {
				{3,3,3},
				{4,4,4},
				{1,1,1}
		};
		
		byte[] m211 = {10, 15, 20};
		byte[] m212 = {20, 15, 10};
		byte[] m213 = {15, 10, 20};
		
		byte[] m31 = {3, 3, 3, 4, 4, 4, 1, 1, 1};
		
		byte[][] m23 = Encoder.multiplyMatrices(m2,m3);
		
		System.out.println("\n\nM2");
		(new Matrix(m2)).show();
		
		System.out.println("\nM3");
		(new Matrix(m3)).show();
			
		System.out.println("\nM2 x M3 - (multiplyMatrixes)");
		(new Matrix(m23)).show();
	
		System.out.println("\n---- Individual octets");				
		System.out.println("\nSum - (M2[0][0] + M3[1][1])");
		System.out.println(OctectOps.addition(m2[0][0], m3[1][1]));
	
		System.out.println("\nSubtraction - (M2[0][0] - M3[1][1])");
		System.out.println(OctectOps.subtraction(m2[0][0], m3[1][1]));

		System.out.println("\nProduct - (M2[0][0] * M3[1][1])");
		System.out.println(OctectOps.product(m2[0][0], m3[1][1]));
		
		System.out.println("\nDivision - (M2[0][0] / M3[1][1])");
		System.out.println(OctectOps.UNSIGN(OctectOps.division(m2[0][0], m3[1][1])));
		
		System.out.println("\nALPHA^^i - (2^^28)");
		System.out.println(OctectOps.alphaPower(28));				

		System.out.println("\n---- Symbolz");				
		System.out.println("\nSum - (m2[0] + m3[1])");
		byte[] tempz = Encoder.xorSymbol(m2[0], m3[1]);
		System.out.printf("| ");
		System.out.printf("%02X | ", tempz[0]);
		System.out.printf("%02X | ", tempz[1]);
		System.out.printf("%02X |\n", tempz[2]);
		
		
		System.out.println("\nSubtraction - (m2[0] - m3[1])");
		tempz = Encoder.xorSymbol(m2[0], m3[1]);
		System.out.printf("| ");
		System.out.printf("%02X | ", tempz[0]);
		System.out.printf("%02X | ", tempz[1]);
		System.out.printf("%02X |\n", tempz[2]);
		
		System.out.println("\nbetaProduct - (2*m211)");
		tempz = OctectOps.betaProduct((byte)2, m211);
		System.out.printf("| ");
		System.out.printf("%02X | ", tempz[0]);
		System.out.printf("%02X | ", tempz[1]);
		System.out.printf("%02X |\n", tempz[2]);
		
		System.out.println("\nbetaDivision - (m211(tempz/2))");
		tempz = OctectOps.betaDivision(tempz, (byte)2);
		System.out.printf("| ");
		System.out.printf("%02X | ", tempz[0]);
		System.out.printf("%02X | ", tempz[1]);
		System.out.printf("%02X |\n", tempz[2]);
	
		// Gauss elim
		System.out.println("\nGAUSS - M3");
		
		byte[][] newM3 = Encoder.supahGauss(m2, m23);
		(new Matrix(newM3)).show();				
		
		System.out.println("\nM2 x M3 - (multiplyByteLineBySymbolVector)");

		// linha 1
		System.out.printf("| ");
		System.out.printf("%02X | ", Encoder.multiplyByteLineBySymbolVector(m211,m31,3)[0]);
		System.out.printf("%02X | ", Encoder.multiplyByteLineBySymbolVector(m211,m31,3)[1]);
		System.out.printf("%02X |\n", Encoder.multiplyByteLineBySymbolVector(m211,m31,3)[2]);
		
		// linha 2
		System.out.printf("| ");
		System.out.printf("%02X | ", Encoder.multiplyByteLineBySymbolVector(m212,m31,3)[0]);
		System.out.printf("%02X | ", Encoder.multiplyByteLineBySymbolVector(m212,m31,3)[1]);
		System.out.printf("%02X |\n", Encoder.multiplyByteLineBySymbolVector(m212,m31,3)[2]);
		
		// linha 3
		System.out.printf("| ");
		System.out.printf("%02X | ", Encoder.multiplyByteLineBySymbolVector(m213,m31,3)[0]);
		System.out.printf("%02X | ", Encoder.multiplyByteLineBySymbolVector(m213,m31,3)[1]);
		System.out.printf("%02X |\n", Encoder.multiplyByteLineBySymbolVector(m213,m31,3)[2]);
		
		System.out.println("---- LET THE SAUNIT BEGIN!!! ----");

		boolean ran1 = false;
		for(byte i = -128; !ran1; i++){
			if(i==127) ran1 = true;
			boolean ran2 = false;
			System.out.println("Testing byte: " + i);
			for(byte j = -128; !ran2; j++){
				
				if(j==127) ran2 = true;
				
				// Calculate
				// Product
				byte tempz3 = OctectOps.product(i, j);
				// Quotient
				if(j==0) continue;
				byte tempz2 = OctectOps.division(tempz3, j);
				
				if(tempz2 != i){
					System.err.println("\nBOOM! @ " + i + "/" + j +"\nProduct:");
					System.err.printf("| %02X |\n ", tempz3);
					System.err.printf("Quotient:\n| %02X |\n", tempz2);
					System.exit(-23);
				}
			}
		}
		
		System.out.println("Stage 1 (Octect operations over finite fields) [PASSED]");
		
		for(int runs=0; runs < 10; runs++){
			System.out.println("RUN: "+runs);
			// Generate random bytes matrix
			// Alocate
			int dimension = 500;
			byte[][] randomBytes1 = new byte[dimension][dimension];
			byte[][] randomBytes2 = new byte[dimension][dimension];

			// Populate
			Random rand = new Random(System.currentTimeMillis() + System.nanoTime());

			// Populate first
			for(int row=0; row<dimension; row++)
				rand.nextBytes(randomBytes1[row]);

			// Populate second
			for(int row=0; row<dimension; row++)
				rand.nextBytes(randomBytes2[row]);
			
			// Test it motherfucker!!!
			byte[][] product  = Encoder.multiplyMatrices(randomBytes1, randomBytes2);
			byte[][] gaussian;
			try{
				gaussian = Encoder.supahGauss(randomBytes1, product);
			}catch(RuntimeException e){
				System.err.println("Matrix is singular! Continuing...");
				continue;
			}

			// Check if equals
			boolean passed = true;
			for(int row=0; row<dimension; row++)
				for(int col=0; col<dimension; col++)
					if(gaussian[row][col] != randomBytes2[row][col]){
						passed = false;
						break;
					}

			// Print if not
			if(!passed){
				System.out.println("\nBOOM!\n\n Matrix 1");
				(new Matrix(randomBytes1)).show();
				
				System.out.println("\nMatrix 2");
				(new Matrix(randomBytes2)).show();

				System.out.println("\nProduct");
				(new Matrix(product)).show();
				
				System.out.println("\nGaussian");
				(new Matrix(gaussian)).show();
				System.exit(-23);
			}
		}

		System.out.println("Stage 2 (Lots of GAUSS using random matrixes) [PASSED]");

		for(int runs=0; runs < 0; runs++){
			System.out.println("RUN: "+runs);
			// Generate random bytes matrix
			// Alocate
			int dimension = 11000;
			byte[][] randomBytes = new byte[dimension][dimension];

			// Populate
			Random rand = new Random(System.currentTimeMillis() + System.nanoTime());

			for(int row=0; row<dimension; row++)
				rand.nextBytes(randomBytes[row]);

			// Test it motherfucker!!!
			for(int row=0; row<dimension; row++){
				ran1 = false;
				for(byte i = -128; !ran1; i++){
					if(i==0) continue;
					if(i==127) ran1 = true;
					// Calculate
					// Product
					tempz = OctectOps.betaProduct(i, randomBytes[row]);
					// Quotient
					byte[] tempz2 = OctectOps.betaDivision(tempz, i);

					// Check if equals
					boolean passed = true;
					for(int j=0; j<dimension; j++)
						if(tempz2[j] != randomBytes[row][j])
							passed = false;

					// Print if not
					if(!passed){
						System.err.println("\nBOOM! @ " + row + "/" + i +"\nProduct:");
						System.err.printf("| %02X | ", tempz[0]);
						for(int j=1; j<dimension; j++)
							System.err.printf("%02X | ", tempz[j]);
						System.err.println("");
						System.err.printf("Quotient:\n| %02X | ", tempz2[0]);
						for(int j=1; j<dimension; j++)
							System.err.printf("%02X | ", tempz2[j]);
						System.err.println("");
						System.exit(-23);
					}
				}
			}
		}

		System.out.println("Stage 3 (Lots of random octect operations over finite fields) [PASSED]");
		System.out.println("-------------DONE----------------");
		
		
		System.exit(2);
		*/
		
		System.out.print("Message: ");
		
		/*byte[] msg = new byte[32000];
		
		try {
			System.in.read(msg, 0, 32000);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-2);
		}
		
		int i;
		for(i=0;i<msg.length;i++)
        {
                int bint = new Byte(msg[i]).intValue();
                if(bint == 0)
                {
                     break;
                }
        }
		
		byte[] msg2 = (new String(msg,0,i)).getBytes();
		
		System.out.println("\n\n------ YOUR MESSAGE ------\n" + new String(msg2));
*/
		byte[] msg2 = {
			0x01,
			0x02,
			0x03,
			0x04,
			0x05,
			0x06,
			0x07,
			0x08,
			0x09,
			0x0A,
		};
		
		Encoder enc = new Encoder(msg2);
		
		System.out.println("------ (un)PARTITIONED MESSAGE ------");
		
		byte[] data = enc.unPartition(enc.partition());


		System.out.println(new String(data));

		System.out.println("\n\n------ going for the encoding, buckle up...\n");	

		EncodingPacket[] encoded_symbols = enc.encode(enc.partition());
	
		/*
		for(i=0; i<encoded_symbols.length; i++){
		
			System.out.println("TESTE: " + new String(encoded_symbols[i].getEncoding_symbols()));
		}
		*/
		enc.decode(encoded_symbols);
		
		System.out.println(new String(msg2)+"\nover'n'out");
	}

}
