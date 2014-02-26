package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class ExactIntArithmetic {

	private static final int	ADD_IDENTITY	= 0;
	private static final int	MULT_IDENTITY	= 1;


	// *** ADDITION *** //

	private static void checkIfAPlusBOverflows(int a, int b) {

		if (IntTests.aPlusBOverflows(a, b)) {
			throw new ArithmeticException("addition overflows");
		}
	}

	// optimization
	static int addition(int a, int b) {

		checkIfAPlusBOverflows(a, b);
		return a + b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static int addition(int... values) {

		final int len = values.length;
		if (len == 0) {
			return ADD_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final int first = values[0], second = values[1];
			checkIfAPlusBOverflows(first, second);

			int sum = first + second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				checkIfAPlusBOverflows(val, sum);
				sum += val;
			}

			return sum;
		}
	}

	private static void checkIfIsAnyNegativeAPlusB(int a, int b) {

		if (IntTests.areAnyNegativeAddition(a, b)) {
			throw new ArithmeticException("some values or their addition are negative");
		}
	}

	// requires a non-negative second parameter
	private static void checkIfIsAnyNegativeAPlusNonNegativeB(int a, int nnB) {

		if (IntTests.isNegative(a) || IntTests.nnAPlusBOverflows(a, nnB)) {
			throw new ArithmeticException("some values or their addition are negative");
		}
	}

	// optimization
	static int allNonNegativeAddition(int a, int b) {

		checkIfIsAnyNegativeAPlusB(a, b);
		return a + b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static int allNonNegativeAddition(int... values) {

		final int len = values.length;
		if (len == 0) {
			return ADD_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final int first = values[0], second = values[1];
			checkIfIsAnyNegativeAPlusB(first, second);

			int sum = first + second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				checkIfIsAnyNegativeAPlusNonNegativeB(val, sum);
				sum += val;
			}

			return sum;
		}
	}

	// *** SUBTRACTION *** //

	static int subtraction(int left, int right) {

		if (IntTests.subtractionOverflows(left, right)) {
			throw new ArithmeticException("subtraction overflows");
		}

		return left - right;
	}

	static int allNonNegativeSubtraction(int left, int right) {

		if (IntTests.areAnyNegativeSubtraction(left, right)) {
			throw new ArithmeticException("some values or their subtraction are negative");
		}

		return left - right;
	}

	// *** MULTIPLICATION *** //

	private static void checkIfATimesBOverflows(int a, int b) {

		if (IntTests.aTimesBOverflows(a, b)) {
			throw new ArithmeticException("multiplication overflows");
		}
	}

	// optimization
	static int multiplication(int a, int b) {

		checkIfATimesBOverflows(a, b);
		return a * b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static int multiplication(int... values) {

		final int len = values.length;
		if (len == 0) {
			return MULT_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final int first = values[0], second = values[1];
			checkIfATimesBOverflows(first, second);

			int prod = first * second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				checkIfATimesBOverflows(val, prod);
				prod *= val;
			}

			return prod;
		}
	}

	private static void checkIfIsAnyNegativeATimesB(int a, int b) {

		if (IntTests.areAnyNegativeMultiplication(a, b)) {
			throw new ArithmeticException("some values or their multiplication are negative");
		}
	}

	// requires a non-negative second parameter
	private static void checkIfIsAnyNegativeATimesNonNegativeB(int a, int nnB) {

		if (IntTests.isNegative(a) || IntTests.nnATimesBOverflows(a, nnB)) {
			throw new ArithmeticException("some values or their multiplication are negative");
		}
	}

	// optimization
	static int allNonNegativeMultiplication(int a, int b) {

		checkIfIsAnyNegativeATimesB(a, b);
		return a * b;
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static int allNonNegativeMultiplication(int... values) {

		final int len = values.length;
		if (len == 0) {
			return MULT_IDENTITY;
		}
		else if (len == 1) {
			return values[0];
		}
		else {
			final int first = values[0], second = values[1];
			checkIfIsAnyNegativeATimesB(first, second);

			int prod = first * second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				checkIfIsAnyNegativeATimesNonNegativeB(val, prod);
				prod *= val;
			}

			return prod;
		}
	}

	// *** DIVISION *** //

	static int division(int left, int right) {

		if (IntTests.divisionOverflows(left, right)) {
			throw new ArithmeticException("division overflows");
		}

		return left / right;
	}

	static int allNonNegativeDivision(int left, int right) {

		if (IntTests.areAnyNegativeDivision(left, right)) {
			throw new ArithmeticException("some values or their division are negative");
		}

		return left / right;
	}

	// *** ABSOLUTE VALUE *** //

	static int absoluteValue(int val) {

		if (IntTests.absoluteValueOverflows(val)) {
			throw new ArithmeticException("absolute value overflows");
		}

		return Math.abs(val);
	}

	// *** NARROWING CAST (LONG -> INT) *** //

	static int narrowingCast(long val) {

		if (IntTests.narrowingCastOverflows(val)) {
			throw new ArithmeticException("cast from long to int overflows");
		}

		return (int)val;
	}

	static int nonNegativeNarrowingCast(long val) {

		if (IntTests.isNegativeNarrowingCast(val)) {
			throw new ArithmeticException("the value or its cast from int to long are negative");
		}

		return (int)val;
	}

	// *** NON-NEGATIVE ASSERTION *** //

	static void assertIsNonNegative(int val) {

		if (IntTests.isNegative(val)) {
			throw new ArithmeticException("the value is negative");
		}
	}

	// optimization
	static void assertAllAreNonNegative(int a, int b) {

		if (IntTests.areAnyNegative(a, b)) {
			throw new ArithmeticException("some value is negative");
		}
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static void assertAllAreNonNegative(int... values) {

		if (IntTests.areAnyNegative(values)) {
			throw new ArithmeticException("some value is negative");
		}
	}

	private ExactIntArithmetic() {

		// not used
	}
}
