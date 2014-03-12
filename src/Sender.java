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
import java.nio.file.Files;

import RQLibrary.Encoder;
import RQLibrary.EncodingPacket;
import RQLibrary.EncodingSymbol;
import RQLibrary.SourceBlock;

/**
 * Simple class that sends a given file to an specified address via UDP.
 * 
 * @author José Lopes
 *
 */

public class Sender {

	public static void main(String[] args) {

		if (args.length != 5) {
			StringBuilder s = new StringBuilder();
			s.append("Usage: \n");
			s.append("    java -jar Sender.jar pathToFile expectedLoss overhead destIP portNumber\n");
			s.append("\n        - pathToFile  : path to the file that shall be sent.");
			s.append("\n        - expectedLoss: the expected loss rate for the network being used (in %).");
			s.append("\n        - overhead    : the necessary symbol overhead needed for decoding (usually between 0 and 2).");
			s.append("\n        - destIP      : the IP address of the receiver.");
			s.append("\n        - portNumber  : the port the receiver will be listening on.");
			System.out.println(s.toString());
			System.exit(1);
		}
		
		// open file and convert to bytes
		String fileName = args[0];
		File file = new File(fileName);
		
		byte[] data = null;
		try
		{
			data = Files.readAllBytes(file.toPath());
		}
		catch (IOException e1)
		{
			System.err.println("Could not open file.");
			e1.printStackTrace();
			System.exit(1);
		}

		// check loss
		float percentageLoss = -1;
		
		try
		{
			percentageLoss = Integer.valueOf(args[1]);
		}
		catch(NumberFormatException e)
		{
			System.err.println("Invalid network loss percentage. (must be a float in [0.0, 100.0[)");
			System.exit(-1);
		}
		
		if(percentageLoss < 0 || percentageLoss >= 100)
		{
			System.err.println("Invalid network loss percentage. (must be a float in [0.0, 100.0[)");
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
		
		// get IP and transform to InetAddress
		InetAddress destIP = null;
		
		try
		{
			destIP = InetAddress.getByName(args[3]);
		}
		catch (UnknownHostException e2)
		{
			e2.printStackTrace();
			System.err.println("invalid IP");
			System.exit(1);
		}
		
		// check destination port
		int destPort = -1;
		
		try
		{
			destPort = Integer.valueOf(args[4]);
		}
		catch(NumberFormatException e)
		{
			System.err.println("Invalid destination port. (must be above 1024)");
			System.exit(-1);
		}
		
		if(destPort < 1024 || destPort >= 65535)
		{
			System.err.println("Invalid destination port. (must be above 1024)");
			System.exit(-1);
		}
		
		System.out.println("Transmiting file " + fileName + " to " + destIP.toString() + ":" + destPort + "\n");
		
		// create a new Encoder instance (usually one per file)
		Encoder encoder = new Encoder(data, percentageLoss, overhead);
		System.out.println("# repair symbols: "+encoder.getNumRepairSymbols() + " (per block)");
		
		// array that will contain the encoded symbols
		EncodingPacket[] encoded_symbols = null;
		
		// array of source blocks
		SourceBlock[] source_blocks = null;
		int no_blocks;
		
		// total number of source symbols (for all source blocks)
		int Kt = encoder.getKt();
		System.out.println("# source symbols: " + Kt);
		
		// partition the data into source blocks
		source_blocks = encoder.partition();
		no_blocks = source_blocks.length;
		System.out.println("# source blocks: " + no_blocks);

		try
		{
			System.out.println("\nPress 'Enter' to continue and start sending the file.");
			System.in.read();
		} 
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
		
		
		// open UDP socket
		DatagramSocket clientSocket = null;
		try
		{
			clientSocket = new DatagramSocket();
		}
		catch (SocketException e1) 
		{
			e1.printStackTrace();
			System.err.println("Error opening socket.");
			System.exit(1);
		}

		// allocate memory for all the encoded symbols
		encoded_symbols = new EncodingPacket[no_blocks];

		/*
		 *  encode each block and send the respective encoded symbols
		 */
		for(int block = 0; block < no_blocks; block++)
		{
			// the block we'll be encoding+sending
			SourceBlock sb = source_blocks[block];
			System.out.println("Sending block: "+ block+" K: "+sb.getK());
			
			// encode 'sb'
			encoded_symbols[block] = encoder.encode(sb);

			// encoded symbols
			EncodingSymbol[] symbols = encoded_symbols[block].getEncoding_symbols();
			int no_symbols = symbols.length;
			
			/*
			 *  serialize and send the encoded symbols
			 */
			ObjectOutput out = null;
			byte[] serialized_data = null;
			
			try
			{
				// serialize and send each encoded symbols
				for(int i = 0; i < no_symbols; i++)
				{
					// simple serialization
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					out = new ObjectOutputStream(bos);
					out.writeObject(symbols[i]);
					serialized_data = bos.toByteArray();
					out.close();
					bos.close();

					// setup an UDP packet with the serialized symbol and the destination info
					DatagramPacket sendPacket = new DatagramPacket(serialized_data,
							serialized_data.length,	destIP, destPort);

					// send the symbol
					clientSocket.send(sendPacket);
					
					// if you're sending to the localhost, you probably want to give some time
					//   between packets, otherwise you'll be overfilling the send buffers (too fast)
					Thread.sleep(1);
				}
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				System.err.println("Socket error.");
				System.exit(1);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}	
		}	

		// close the socket
		clientSocket.close();
	}
}