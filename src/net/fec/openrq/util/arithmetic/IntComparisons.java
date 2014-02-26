package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class IntComparisons {

	// *** MAX *** //

	// optimization
	static int max(int a, int b) {

		return Math.max(a, b);
	}

	// optimization
	static int max(int a, int b, int c) {

		return Math.max(Math.max(a, b), c);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static int max(int... values) {

		final int len = values.length;
		if (len == 0) throw new IllegalArgumentException("need at least one value");

		int max = values[0];
		for (int i = 1; i < len; i++) {
			max = Math.max(max, values[i]);
		}

		return max;
	}

	// *** MIN *** //

	// optimization
	static int min(int a, int b) {

		return Math.min(a, b);
	}

	// optimization
	static int min(int a, int b, int c) {

		return Math.min(Math.min(a, b), c);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static int min(int... values) {

		final int len = values.length;
		if (len == 0) throw new IllegalArgumentException("need at least one value");

		int min = values[0];
		for (int i = 1; i < len; i++) {
			min = Math.min(min, values[i]);
		}

		return min;
	}

	private IntComparisons() {

		// not used
	}
}
