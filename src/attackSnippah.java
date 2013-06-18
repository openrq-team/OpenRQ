
public class attackSnippah {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Encoder enc = new Encoder(data, 60, 0);
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
			System.out.println("Attacked");
		}
	}

	public static byte[] data = {-16, -33, -22, -85, -120, 82, 32, 87, 37, 115};
	public static int[]  lose = {3, 4, 6, 7, 8, 9};
}
