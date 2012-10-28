import java.io.IOException;


public class Main {


	public static void main(String[] args) {
		
		System.out.print("Message: ");
		
		byte[] msg = new byte[32000];
		
		try {
			System.in.read(msg, 0, 32000);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-2);
		}
		
		System.out.println("\n\n------ YOUR MESSAGE ------\n" + new String(msg));
		
		Encoder enc = new Encoder(msg);
		
		System.out.println("------ PARTITIONED MESSAGE ------");
		
		SourceBlock[] data = enc.partition();
		
		for(int i = 0; i<data.length; i++){
			
			SubBlock[] sblocks = data[i].getSub_blocks();
			
			for(int j=0; j<sblocks.length; j++){
				System.out.print(new String(sblocks[j].getData()));
			}
		}
		
		
	}

}
