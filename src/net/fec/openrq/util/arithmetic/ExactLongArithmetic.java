package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class ExactLongArithmetic {

	private static final long	ADD_IDENTITY	= 0;
	private static final long	MULT_IDENTITY	= 1;


	// *** ADDITION *** //

	private static void checkIfAPlusBOverflows(long a, long b) {

		if (LongTests.aPlusBOverflows(a, b)) {
			throw new ArithmeticException("addition overflows");
		}
	}

	// optimization
	static long addition(long a, long b) {

		checkIfAPlusBOverflows(a, b);
		return a + b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static long addition(long... values) {

		final int len = values.length;
		if (len == 0) {
			return ADD_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final long first = values[0], second = values[1];
			checkIfAPlusBOverflows(first, second);

			long sum = first + second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				checkIfAPlusBOverflows(val, sum);
				sum += val;
			}

			return sum;
		}
	}

	private static void checkIfIsAnyNegativeAPlusB(long a, long b) {

		if (LongTests.areAnyNegativeAddition(a, b)) {
			throw new ArithmeticException("some values or their addition are negative");
		}
	}

	// requires a non-negative second parameter
	private static void checkIfIsAnyNegativeAPlusNonNegativeB(long a, long nnB) {

		if (LongTests.isNegative(a) || LongTests.nnAPlusBOverflows(a, nnB)) {
			throw new ArithmeticException("some values or their addition are negative");
		}
	}

	// optimization
	static long allNonNegativeAddition(long a, long b) {

		checkIfIsAnyNegativeAPlusB(a, b);
		return a + b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static long allNonNegativeAddition(long... values) {

		final int len = values.length;
		if (len == 0) {
			return ADD_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final long first = values[0], second = values[1];
			checkIfIsAnyNegativeAPlusB(first, second);

			long sum = first + second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				checkIfIsAnyNegativeAPlusNonNegativeB(val, sum);
				sum += val;
			}

			return sum;
		}
	}

	// *** SUBTRACTION *** //

	static long subtraction(long left, long right) {

		if (LongTests.subtractionOverflows(left, right)) {
			throw new ArithmeticException("subtraction overflows");
		}

		return left - right;
	}

	static long allNonNegativeSubtraction(long left, long right) {

		if (LongTests.areAnyNegativeSubtraction(left, right)) {
			throw new ArithmeticException("some values or their subtraction are negative");
		}

		return left - right;
	}

	// *** MULTIPLICATION *** //

	private static void checkIfATimesBOverflows(long a, long b) {

		if (LongTests.aTimesBOverflows(a, b)) {
			throw new ArithmeticException("multiplication overflows");
		}
	}

	// optimization
	static long multiplication(long a, long b) {

		checkIfATimesBOverflows(a, b);
		return a * b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static long multiplication(long... values) {

		final int len = values.length;
		if (len == 0) {
			return MULT_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final long first = values[0], second = values[1];
			checkIfATimesBOverflows(first, second);

			long prod = first * second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				checkIfATimesBOverflows(val, prod);
				prod *= val;
			}

			return prod;
		}
	}

	private static void checkIfIsAnyNegativeATimesB(long a, long b) {

		if (LongTests.areAnyNegativeMultiplication(a, b)) {
			throw new ArithmeticException("some values or their multiplication are negative");
		}
	}

	// requires a non-negative second parameter
	private static void checkIfIsAnyNegativeATimesNonNegativeB(long a, long nnB) {

		if (LongTests.isNegative(a) || LongTests.nnATimesBOverflows(a, nnB)) {
			throw new ArithmeticException("some values or their multiplication are negative");
		}
	}

	// optimization
	static long allNonNegativeMultiplication(long a, long b) {

		checkIfIsAnyNegativeATimesB(a, b);
		return a * b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static long allNonNegativeMultiplication(long... values) {

		final int len = values.length;
		if (len == 0) {
			return MULT_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final long first = values[0], second = values[1];
			checkIfIsAnyNegativeATimesB(first, second);

			long prod = first * second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				checkIfIsAnyNegativeATimesNonNegativeB(val, prod);
				prod *= val;
			}

			return prod;
		}
	}

	// *** DIVISION *** //

	static long division(long left, long right) {

		if (LongTests.divisionOverflows(left, right)) {
			throw new ArithmeticException("division overflows");
		}

		return left / right;
	}

	static long allNonNegativeDivision(long left, long right) {

		if (LongTests.areAnyNegativeDivision(left, right)) {
			throw new ArithmeticException("some values or their division are negative");
		}

		return left / right;
	}

	// *** ABSOLUTE VALUE *** //

	static long absoluteValue(long val) {

		if (LongTests.absoluteValueOverflows(val)) {
			throw new ArithmeticException("absolute value overflows");
		}

		return Math.abs(val);
	}

	// *** NON-NEGATIVE ASSERTION *** //

	static void assertIsNonNegative(long val) {

		if (LongTests.isNegative(val)) {
			throw new ArithmeticException("the value is negative");
		}
	}

	// optimization
	static void assertAllAreNonNegative(long a, long b) {

		if (LongTests.areAnyNegative(a, b)) {
			throw new ArithmeticException("some value is negative");
		}
	}

	// optimization
	static void assertAllAreNonNegative(long a, long b, long c) {

		if (LongTests.areAnyNegative(a, b, c)) {
			throw new ArithmeticException("some value is negative");
		}
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static void assertAllAreNonNegative(long... values) {

		if (LongTests.areAnyNegative(values)) {
			throw new ArithmeticException("some value is negative");
		}
	}

	private ExactLongArithmetic() {

		// not used
	}
}
