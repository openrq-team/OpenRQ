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
			s.append("    java -jar Receiver.jar pathToFile fileSize overhead port\n");
			s.append("\n        - pathToFile  : path to store the received file.");
			s.append("\n        - fileSize    : the size of the file that will be received. (in bytes)");
			s.append("\n        - overhead    : the necessary symbol overhead needed for decoding (usually between 0 and 2).");
			s.append("\n        - portNumber  : the port the receiver will be listening on.");
			System.out.println(s.toString());
			System.exit(1);
		}

		String fileName = args[0];

		// check fileSize
		int fileSize = -1;

		try
		{
			fileSize = Integer.valueOf(args[1]);
		}
		catch(NumberFormatException e)
		{
			System.err.println("Invalid file size. (must be positive)");
			System.exit(-1);
		}

		if(fileSize <= 0)
		{
			System.err.println("Invalid file size. (must be positive)");
			System.exit(-1);
		}

		// check overhead
		int overhead = -1;

		try
		{
			overhead = Integer.valueOf(args[2]);
		}
		catch(NumberFormatException e)
		{
			System.err.println("Invalid overhead value. (must be a positive integer)");
			System.exit(-1);
		}

		if(overhead < 0)
		{
			System.err.println("Invalid overhead value. (must be a positive integer)");
			System.exit(-1);
		}

		// check source port
		int srcPort = -1;

		try
		{
			srcPort = Integer.valueOf(args[3]);
		}
		catch(NumberFormatException e)
		{
			System.err.println("Invalid port. (must be above 1024)");
			System.exit(-1);
		}

		if(srcPort < 1024 || srcPort >= 65535)
		{
			System.err.println("Invalid port. (must be above 1024)");
			System.exit(-1);
		}

		System.out.println("Listening for file " + fileName + " (" + fileSize + " bytes) at port " + srcPort + "\n");

		// create a new Encoder instance (usually one per file)
		Encoder encoder = new Encoder(fileSize);

		// total number of source symbols (for all source blocks)
		int Kt = encoder.getKt();
		System.out.println("# source symbols: " + Kt);

		// number of source blocks
		int no_blocks = encoder.Z;
		System.out.println("# source blocks: " + no_blocks);
		
		// the minimum amount of symbols we'll be waiting for before trying to decode
		int total_symbols = Kt + no_blocks * overhead;
		
		Partition KZ = new Partition(Kt, no_blocks);
		int KL = KZ.get(1);
		int KS = KZ.get(2);
		int ZL = KZ.get(3);
		
		// create socket and wait for packets
		DatagramSocket serverSocket = null;
		try 
		{
			serverSocket = new DatagramSocket(srcPort);
		}
		catch (SocketException e1) 
		{
			e1.printStackTrace();
			System.err.println("Error opening socket.");
			System.exit(1);
		}
		
		// where we will store the received symbols
		Set<EncodingSymbol> received_packets = new HashSet<EncodingSymbol>();

		try 
		{
			System.out.println("\nWaiting for packets...");
			
			// wait for all the symbols that we need...
			for(int recv = 0; recv < total_symbols; recv++)
			{	
				// allocate some memory for receiving the packets
				byte[] receiveData = new byte[51024];
				
				// create a UDP packet
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				
				// (wait untill) receive the packet
				serverSocket.receive(receivePacket);
				
				//System.out.println("Received packet " + recv);
				
				// get the packet's payload
				byte[] packetData = receivePacket.getData();
				
				// deserialize it
				ByteArrayInputStream bis = new ByteArrayInputStream(packetData);
				ObjectInput in = null;
				try 
				{
					in = new ObjectInputStream(bis);
					received_packets.add((EncodingSymbol) in.readObject());
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.err.println("Socket error.");
			System.exit(1);
		}

		/*
		 *  organize into source blocks
		 */
		
		// order received packets
		int maxESI = -1;
		
		for(EncodingSymbol es : received_packets)
			if(es.getESI() > maxESI)
				maxESI = es.getESI();
		
		Iterator<EncodingSymbol> it = received_packets.iterator();
		EncodingSymbol[][] aux = new EncodingSymbol[no_blocks][maxESI+1];
		
		while(it.hasNext())
		{
			EncodingSymbol pack = it.next();
			aux[pack.getSBN()][pack.getESI()] = pack;
		}
				
		/*
		 *  decoding
		 */
		
		// where the decoded data will be stored
		byte[] decoded_data = null;
		
		// where the source blocks will be stored
		SourceBlock[] blocks = new SourceBlock[no_blocks];

		boolean successfulDecoding = true;
		
		// for each block
		for (int sblock = 0; sblock < no_blocks; sblock++)
		{
			System.out.println("\nDecoding block: " + sblock);
			try 
			{
				// get the time before decoding
				long before = System.currentTimeMillis();
				
				// decode
				if(sblock < ZL)
					blocks[sblock] = Encoder.decode(new EncodingPacket(0, aux[sblock], KL, Encoder.MAX_PAYLOAD_SIZE));
				else
					blocks[sblock] = Encoder.decode(new EncodingPacket(0, aux[sblock], KS, Encoder.MAX_PAYLOAD_SIZE));
				
				// get time after decoding
				long after = System.currentTimeMillis();
				
				long diff = (long) (after-before);
				System.out.println("\nSuccessfully decoded block: " + sblock + " (in " + diff + " milliseconds)");
								
			} 
			catch (SingularMatrixException e)
			{
				System.out.println("\nDecoding failed!");
				successfulDecoding = false;
			}
			catch (RuntimeException e)
			{
				if(e.getMessage().equals("\nNot enough repair symbols received."))
				{
					System.out.println(e.getMessage());
					successfulDecoding = false;
					continue;
				}
				else
				{
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

		// if the decoding was successful for all blocks, we can unpartition the data
		if(successfulDecoding)
			decoded_data = encoder.unPartition(blocks);
		else
			System.exit(-1);
		
		// and finally, write the decoded data to the file
		File file = new File(fileName);
		
		try
		{
			if (file.exists())
				file.createNewFile();
		
			Files.write(file.toPath(), decoded_data);
		}
		catch (IOException e)
		{
			System.err.println("Could not open file.");
			e.printStackTrace();
			System.exit(1);
		}
	}
}