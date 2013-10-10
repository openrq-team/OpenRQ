import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import RQLibrary.Encoder;
import RQLibrary.EncodingPacket;
import RQLibrary.EncodingSymbol;
import RQLibrary.SingularMatrixException;
import RQLibrary.SourceBlock;

import com.google.common.io.Files;

public class Receiver {

	public static void main(String[] args) {

		if (args.length != 4) {
			StringBuilder s = new StringBuilder();
			s.append("Usage: \n");
			s.append("    java -jar Receiver.jar fileName fileSize port overhead\n");
			System.out.println(s.toString());
			System.exit(1);
		}

		String fileName = args[0];
		int fileSize = Integer.valueOf(args[1]);
		int srcPort = Integer.valueOf(args[2]);
		int overhead = Integer.valueOf(args[3]);

		Encoder encoder = new Encoder(fileSize);
		int Kt = encoder.getKt();
		
		// create socket and wait for packets
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(srcPort);
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.err.println("erro ao criar socket");
			System.exit(1);
		}
		
		EncodingPacket[] encoded_symbols = new EncodingPacket[1];
		Set<EncodingSymbol> received_packets = new HashSet<EncodingSymbol>();
		
		try {
			System.out.println("Waiting for packets...");
			for(int recv = 0; recv < Kt; recv++){
				
				byte[] receiveData = new byte[51024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				
				serverSocket.receive(receivePacket);
				System.out.println("Received packet "+recv);
				
				byte[] packetData = receivePacket.getData();

				// deserialize
				ByteArrayInputStream bis = new ByteArrayInputStream(packetData);
				ObjectInput in = null;
				try {
					in = new ObjectInputStream(bis);
					received_packets.add((EncodingSymbol) in.readObject());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Socket error");
			System.exit(1);
		}

		// order received packets
		Iterator<EncodingSymbol> it = received_packets.iterator();
		EncodingSymbol[] aux = new EncodingSymbol[received_packets.size() + overhead];
		while(it.hasNext()){
			EncodingSymbol pack = it.next();
			aux[pack.getESI()] = pack;
		}
		
		encoded_symbols[0] = new EncodingPacket(0, aux, Kt, Encoder.MAX_PAYLOAD_SIZE);
		
		// decode
		byte[] decoded_data = null;
		try {
			SourceBlock[] aux1 = Encoder.decode(encoded_symbols);
			decoded_data = encoder.unPartition(aux1);
		} catch (SingularMatrixException e) {
			System.out.println("DECODING FALHOU!");
			System.exit(1);
		}

		File file = new File(fileName);
		try {
			if (file.exists())
				file.createNewFile();
			Files.write(decoded_data, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
