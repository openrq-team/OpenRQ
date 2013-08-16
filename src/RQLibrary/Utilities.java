package RQLibrary;

enum Utilities {;

	public static void show(byte[][] matrix) {

		// TODO insert checks

		int M = matrix[0].length;
		int N = matrix[0].length;

		System.out.printf("    ");
		for (int j = 0; j < N; j++)
			System.out.printf("* %02d ", j);

		System.out.println("|");

		for (int i = 0; i < M; i++) {
			System.out.printf(" %02d)", i);
			for (int j = 0; j < N; j++)
				System.out.printf("| %02X ", (matrix[i][j]));
			System.out.println("|");
		}
	}
}
