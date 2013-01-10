import java.io.IOException;
import java.util.List;


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
		
		System.out.println("over'n'out");
	}

}
