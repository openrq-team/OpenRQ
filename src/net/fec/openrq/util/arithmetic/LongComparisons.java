package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class LongComparisons {

	// *** MAX *** //

	// optimization
	static long max(long a, long b) {

		return Math.max(a, b);
	}

	// optimization
	static long max(long a, long b, long c) {

		return Math.max(Math.max(a, b), c);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static long max(long... values) {

		final long len = values.length;
		if (len == 0) throw new IllegalArgumentException("need at least one value");

		long max = values[0];
		for (int i = 1; i < len; i++) {
			max = Math.max(max, values[i]);
		}

		return max;
	}

	// *** MIN *** //

	// optimization
	static long min(long a, long b) {

		return Math.min(a, b);
	}

	// optimization
	static long min(long a, long b, long c) {

		return Math.min(Math.min(a, b), c);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static long min(long... values) {

		final long len = values.length;
		if (len == 0) throw new IllegalArgumentException("need at least one value");

		long min = values[0];
		for (int i = 1; i < len; i++) {
			min = Math.min(min, values[i]);
		}

		return min;
	}

	private LongComparisons() {

		// not used
	}
}
