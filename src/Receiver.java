import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import RQLibrary.Encoder;
import RQLibrary.EncodingPacket;
import RQLibrary.EncodingSymbol;
import RQLibrary.Partition;
import RQLibrary.SingularMatrixException;
import RQLibrary.SourceBlock;

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
		int no_blocks = encoder.Z;
		Partition KZ = new Partition(Kt, no_blocks);
		int KL = KZ.get(1);
		int KS = KZ.get(2);
		int ZL = KZ.get(3);
		System.out.println("# packets: "+Kt);
		
		// create socket and wait for packets
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(srcPort);
		} catch (SocketException e1) {
			e1.printStackTrace();
			System.err.println("erro ao criar socket");
			System.exit(1);
		}
		
		//EncodingPacket[] encoded_symbols = new EncodingPacket[1];
		Set<EncodingSymbol> received_packets = new HashSet<EncodingSymbol>();
		
		try {
			System.out.println("Waiting for packets...");
			for(int recv = 0; recv < Kt; recv++){
				
				byte[] receiveData = new byte[51024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				
				serverSocket.receive(receivePacket);
				System.out.println("Received packet "+recv);
				//System.out.println(Arrays.toString(receivePacket.getData()));
				
				byte[] packetData = receivePacket.getData();
				//System.out.println(Arrays.toString(packetData));
				
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
				catch (StreamCorruptedException sda){
					recv --;
					continue;
				}
			
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Socket error");
			System.exit(1);
		}

		// split into source blocks
		// order received packets
		int maxESI = -1;
		
		for(EncodingSymbol es : received_packets)
			if(es.getESI() > maxESI)
				maxESI = es.getESI();
		
		Iterator<EncodingSymbol> it = received_packets.iterator();
		EncodingSymbol[][] aux = new EncodingSymbol[no_blocks][maxESI+1];
		while(it.hasNext()){
			EncodingSymbol pack = it.next();
			aux[pack.getSBN()][pack.getESI()] = pack;
		}
		
		//encoded_symbols[0] = new EncodingPacket(0, aux, Kt, Encoder.MAX_PAYLOAD_SIZE);
		
		// decode
		byte[] decoded_data = null;
		SourceBlock[] blocks = new SourceBlock[no_blocks];

		boolean successfulDecoding = true;
		
		for (int sblock = 0; sblock < no_blocks; sblock++) {
		
			System.out.println("\nDecoding block: "+sblock);
			try {

				long antes = System.currentTimeMillis();
				
				if(sblock < ZL)
					blocks[sblock] = Encoder.decode(new EncodingPacket(0, aux[sblock], KL, Encoder.MAX_PAYLOAD_SIZE));
				else
					blocks[sblock] = Encoder.decode(new EncodingPacket(0, aux[sblock], KS, Encoder.MAX_PAYLOAD_SIZE));
				
				long depois = System.currentTimeMillis();
				
				System.out.println("\nSuccessfully decoded block: "+sblock);
				
				long diff = (long) (depois-antes); // 1000;
				System.out.println("Time elapsed: " + diff + " (micro-seconds)");
				
			} catch (SingularMatrixException e) {
				System.out.println("\nDECODING FAILED!\n");
				successfulDecoding = false;
			}
			catch (RuntimeException e) {
				if(e.getMessage().equals("Not enough repair symbols received.")){
					System.out.println(e.getMessage());
					successfulDecoding = false;
					continue;
				}
				else{
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

		if(successfulDecoding)
			decoded_data = encoder.unPartition(blocks);
		else
			System.exit(-1);
		
		File file = new File(fileName);
		try {
			if (file.exists())
				file.createNewFile();
			Files.write(file.toPath(), decoded_data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
