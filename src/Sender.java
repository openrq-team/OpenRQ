import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import RQLibrary.Encoder;
import RQLibrary.EncodingPacket;
import RQLibrary.EncodingSymbol;
import RQLibrary.SingularMatrixException;
import RQLibrary.SourceBlock;

import com.google.common.io.Files;

public class Sender {


	public static void main(String[] args) {


		if (args.length != 4) {
			StringBuilder s = new StringBuilder();
			s.append("Usage: \n");
			s.append("    java -jar Sender.jar file overhead IP port\n");
			System.out.println(s.toString());
			System.exit(1);
		}

		String fileName = args[0];
		int overhead = Integer.valueOf(args[1]);
		InetAddress destIP = null;
		try {
			destIP = InetAddress.getByName(args[2]);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			System.err.println("IP invalido");
			System.exit(1);
		}
		int destPort = Integer.valueOf(args[3]);

		Encoder.INIT_REPAIR_SYMBOL = 0;

		File file = new File(fileName);
		byte[] data = null;
		try {
			data = Files.toByteArray(file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}

		Encoder enc = new Encoder(data, 0, overhead);
		EncodingPacket[] encoded_symbols = null;
		SourceBlock[] aux = null;
		int no_blocks = 0;
		int Kt = enc.getKt();
		System.out.println("# packets: "+Kt);
		aux = enc.partition();
		no_blocks = aux.length;
		System.out.println("Number of blocks: "+no_blocks);

		try {
			System.in.read();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		// Serialize and send EncodingPacket[]
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.err.println("Erro ao abrir socket");
			System.exit(1);
		}

		encoded_symbols = new EncodingPacket[no_blocks];

		for(int block = 0; block < no_blocks; block++){

			SourceBlock sb = aux[block];
			encoded_symbols[block] = enc.encode(sb);

			ObjectOutput out = null;
			byte[] serialized_data = null;
			try {


				System.out.println("Sending block: "+ block+" K: "+sb.getK());

				for (int i = 0; i < encoded_symbols[block]
						.getEncoding_symbols().length; i++) {

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					out = new ObjectOutputStream(bos);
					out.writeObject(encoded_symbols[block]
							.getEncoding_symbols()[i]);
					serialized_data = bos.toByteArray();
					out.close();
					bos.close();

					DatagramPacket sendPacket = new DatagramPacket(
							serialized_data, serialized_data.length,
							destIP, destPort);

					clientSocket.send(sendPacket);

					Thread.sleep(1);
				}

				//clientSocket.send(new DatagramPacket(new byte[1024], 1024, destIP, destPort));					

				Thread.sleep(100);

			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("IO Exception");
				System.exit(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}	

		clientSocket.close();

	}

}