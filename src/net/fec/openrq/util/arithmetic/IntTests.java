package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class IntTests {

	static final int	MIN_VALUE	= Integer.MIN_VALUE;
	static final int	MAX_VALUE	= Integer.MAX_VALUE;


	// *** ADDITION *** //

	static boolean aPlusBOverflowsThroughMaxValue(int a, int b) {

		return (b > 0 && a > MAX_VALUE - b);
	}

	static boolean aPlusBOverflowsThroughMinValue(int a, int b) {

		return (b < 0 && a < MIN_VALUE - b);
	}

	static boolean aPlusBOverflows(int a, int b) {

		return (aPlusBOverflowsThroughMaxValue(a, b) || aPlusBOverflowsThroughMinValue(a, b));
	}

	// optimization
	static boolean additionOverflows(int a, int b) {

		return aPlusBOverflows(a, b);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one addition when passing two arguments or more.

	static boolean additionOverflows(int... values) {

		final int len = values.length;
		if (len > 1) {
			final int first = values[0], second = values[1];
			if (aPlusBOverflows(first, second)) {
				return true;
			}

			int sum = first + second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				if (aPlusBOverflows(sum, val)) {
					return true;
				}
				sum += val;
			}
		}

		return false;
	}

	// requires non-negative parameters
	static boolean nnAPlusBOverflows(int nnA, int nnB) {

		return (nnA > MAX_VALUE - nnB);
	}

	// optimization
	static boolean areAnyNegativeAddition(int a, int b) {

		return (areAnyNegative(a, b) || nnAPlusBOverflows(a, b));
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one addition when passing two arguments or more.

	static boolean areAnyNegativeAddition(int... values) {

		final int len = values.length;
		if (values.length > 1) {
			final int first = values[0], second = values[1];
			if ((areAnyNegative(first, second) || nnAPlusBOverflows(first, second))) {
				return true;
			}

			int sum = first + second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				if (isNegative(val) || nnAPlusBOverflows(sum, val)) {
					return true;
				}
				sum += val;
			}
		}

		return false;
	}

	// *** SUBTRACTION *** //

	static boolean leftMinusRightOverflowsThroughMaxValue(int left, int right) {

		return (right < 0 && left > MAX_VALUE + right);
	}

	static boolean leftMinusRightOverflowsThroughMinValue(int left, int right) {

		return (right > 0 && left < MIN_VALUE + right);
	}

	static boolean subtractionOverflows(int left, int right) {

		return (leftMinusRightOverflowsThroughMaxValue(left, right) || leftMinusRightOverflowsThroughMinValue(left,
				right));
	}

	static boolean areAnyNegativeSubtraction(int left, int right) {

		// if right is non-negative, then the last test will always succeed for negative left values
		return (/* isNegative(left) || */isNegative(right) || right > left);
	}

	// *** MULTIPLICATION *** //

	// requires a positive second parameter > 0
	static boolean aTimesPositiveBOverflowsThroughMaxValue(int a, int posB) {

		return (a > MAX_VALUE / posB);
	}

	// requires a second parameter > 0
	static boolean aTimesPositiveBOverflowsThroughMinValue(int a, int posB) {

		return (a < MIN_VALUE / posB);
	}

	// requires a second parameter < -1
	static boolean aTimesSubMinusOneBOverflowsThroughMaxValue(int a, int underMinusOneB) {

		return (a < MAX_VALUE / underMinusOneB);
	}

	// requires a second parameter < -1
	static boolean aTimesSubMinusOneBOverflowsThroughMinValue(int a, int underMinusOneB) {

		return (a > MIN_VALUE / underMinusOneB);
	}

	static boolean aTimesMinusOneOverflowsThroughMaxValue(int a) {

		return a == MIN_VALUE;
	}

	static boolean aTimesBOverflows(int a, int b) {

		if (b > 0) {
			return (aTimesPositiveBOverflowsThroughMaxValue(a, b) || aTimesPositiveBOverflowsThroughMinValue(a, b));
		}
		else if (b < -1) {
			return (aTimesSubMinusOneBOverflowsThroughMaxValue(a, b) || aTimesSubMinusOneBOverflowsThroughMinValue(a, b));
		}
		else {
			return (b == -1 && aTimesMinusOneOverflowsThroughMaxValue(a));
		}
	}

	// optimization
	static boolean multiplicationOverflows(int a, int b) {

		return aTimesBOverflows(a, b);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one multiplication when passing two arguments or more.

	static boolean multiplicationOverflows(int... values) {

		final int len = values.length;
		if (len > 1) {
			final int first = values[0], second = values[1];
			if (aTimesBOverflows(first, second)) {
				return true;
			}

			int prod = first * second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				if (aTimesBOverflows(prod, val)) {
					return true;
				}
				prod *= val;
			}
		}

		return false;
	}

	// requires non-negative parameters
	static boolean nnATimesBOverflows(int nnA, int nnB) {

		return (nnB != 0 && nnA > MAX_VALUE / nnB);
	}

	// optimization
	static boolean areAnyNegativeMultiplication(int a, int b) {

		return (areAnyNegative(a, b) || nnATimesBOverflows(a, b));
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one multiplication when passing two arguments or more.

	static boolean areAnyNegativeMultiplication(int... values) {

		final int len = values.length;
		if (len > 1) {
			final int first = values[0], second = values[1];
			if (areAnyNegative(first, second) || nnATimesBOverflows(first, second)) {
				return true;
			}

			int prod = first * second;
			for (int i = 2; i < len; i++) {
				final int val = values[i];
				if (isNegative(val) || nnATimesBOverflows(prod, val)) {
					return true;
				}
				prod *= val;
			}
		}

		return false;
	}

	// *** DIVISION *** //

	static boolean leftOverRightOverflowsThroughMaxValue(int left, int right) {

		return (left == MIN_VALUE && right == -1);
	}

	static boolean divisionOverflows(int left, int right) {

		return leftOverRightOverflowsThroughMaxValue(left, right);
	}

	static boolean areAnyNegativeDivision(int left, int right) {

		// a non negative division never overflows
		return areAnyNegative(left, right);
	}

	// *** ABSOLUTE VALUE *** //

	static boolean absoluteValueOverflowsThroughMaxValue(int val) {

		return (val == MIN_VALUE);
	}

	static boolean absoluteValueOverflows(int val) {

		return absoluteValueOverflowsThroughMaxValue(val);
	}

	// *** NARROWING CAST (LONG -> INT) *** //

	static boolean narrowingCastOverflowsThroughMinValue(long val) {

		return val < MIN_VALUE;
	}

	static boolean narrowingCastOverflowsThroughMaxValue(long val) {

		return val > MAX_VALUE;
	}

	static boolean narrowingCastOverflows(long val) {

		return (narrowingCastOverflowsThroughMinValue(val) || narrowingCastOverflowsThroughMaxValue(val));
	}

	static boolean isNegativeNarrowingCast(long val) {

		return (val < 0L || narrowingCastOverflowsThroughMaxValue(val));
	}

	// *** IS NEGATIVE *** //

	static boolean isNegative(int val) {

		return val < 0;
	}

	// optimization
	static boolean areAnyNegative(int a, int b) {

		return isNegative(a) || isNegative(b);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static boolean areAnyNegative(int... values) {

		for (int val : values) {
			if (isNegative(val)) {
				return true;
			}
		}

		return false;
	}

	private IntTests() {

		// not used
	}
}
