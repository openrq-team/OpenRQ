package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class LongTests {

	static final long	MIN_VALUE	= Long.MIN_VALUE;
	static final long	MAX_VALUE	= Long.MAX_VALUE;


	// *** ADDITION *** //

	static boolean aPlusBOverflowsThroughMaxValue(long a, long b) {

		return (b > 0 && a > MAX_VALUE - b);
	}

	static boolean aPlusBOverflowsThroughMinValue(long a, long b) {

		return (b < 0 && a < MIN_VALUE - b);
	}

	static boolean aPlusBOverflows(long a, long b) {

		return (aPlusBOverflowsThroughMaxValue(a, b) || aPlusBOverflowsThroughMinValue(a, b));
	}

	// optimization
	static boolean additionOverflows(long a, long b) {

		return aPlusBOverflows(a, b);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one addition when passing two arguments or more.

	static boolean additionOverflows(long... values) {

		final int len = values.length;
		if (len > 1) {
			final long first = values[0], second = values[1];
			if (aPlusBOverflows(first, second)) {
				return true;
			}

			long sum = first + second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				if (aPlusBOverflows(sum, val)) {
					return true;
				}
				sum += val;
			}
		}

		return false;
	}

	// requires non-negative parameters
	static boolean nnAPlusBOverflows(long nnA, long nnB) {

		return (nnA > MAX_VALUE - nnB);
	}

	// optimization
	static boolean areAnyNegativeAddition(long a, long b) {

		return (areAnyNegative(a, b) || nnAPlusBOverflows(a, b));
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one addition when passing two arguments or more.

	static boolean areAnyNegativeAddition(long... values) {

		final int len = values.length;
		if (values.length > 1) {
			final long first = values[0], second = values[1];
			if ((areAnyNegative(first, second) || nnAPlusBOverflows(first, second))) {
				return true;
			}

			long sum = first + second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				if (isNegative(val) || nnAPlusBOverflows(sum, val)) {
					return true;
				}
				sum += val;
			}
		}

		return false;
	}

	// *** SUBTRACTION *** //

	static boolean leftMinusRightOverflowsThroughMaxValue(long left, long right) {

		return (right < 0 && left > MAX_VALUE + right);
	}

	static boolean leftMinusRightOverflowsThroughMinValue(long left, long right) {

		return (right > 0 && left < MIN_VALUE + right);
	}

	static boolean subtractionOverflows(long left, long right) {

		return (leftMinusRightOverflowsThroughMaxValue(left, right) || leftMinusRightOverflowsThroughMinValue(left,
			right));
	}

	static boolean areAnyNegativeSubtraction(long left, long right) {

		// if right is non-negative, then the last test will always succeed for negative left values
		return (/* isNegative(left) || */isNegative(right) || right > left);
	}

	// *** MULTIPLICATION *** //

	// requires a positive second parameter > 0
	static boolean aTimesPositiveBOverflowsThroughMaxValue(long a, long posB) {

		return (a > MAX_VALUE / posB);
	}

	// requires a second parameter > 0
	static boolean aTimesPositiveBOverflowsThroughMinValue(long a, long posB) {

		return (a < MIN_VALUE / posB);
	}

	// requires a second parameter < -1
	static boolean aTimesSubMinusOneBOverflowsThroughMaxValue(long a, long underMinusOneB) {

		return (a < MAX_VALUE / underMinusOneB);
	}

	// requires a second parameter < -1
	static boolean aTimesSubMinusOneBOverflowsThroughMinValue(long a, long underMinusOneB) {

		return (a > MIN_VALUE / underMinusOneB);
	}

	static boolean aTimesMinusOneOverflowsThroughMaxValue(long a) {

		return a == MIN_VALUE;
	}

	static boolean aTimesBOverflows(long a, long b) {

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
	static boolean multiplicationOverflows(long a, long b) {

		return aTimesBOverflows(a, b);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one multiplication when passing two arguments or more.

	static boolean multiplicationOverflows(long... values) {

		final int len = values.length;
		if (len > 1) {
			final long first = values[0], second = values[1];
			if (aTimesBOverflows(first, second)) {
				return true;
			}

			long prod = first * second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				if (aTimesBOverflows(prod, val)) {
					return true;
				}
				prod *= val;
			}
		}

		return false;
	}

	// requires non-negative parameters
	static boolean nnATimesBOverflows(long nnA, long nnB) {

		return (nnB != 0 && nnA > MAX_VALUE / nnB);
	}

	// optimization
	static boolean areAnyNegativeMultiplication(long a, long b) {

		return (areAnyNegative(a, b) || nnATimesBOverflows(a, b));
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object
	// and calculates at least one multiplication when passing two arguments or more.

	static boolean areAnyNegativeMultiplication(long... values) {

		final int len = values.length;
		if (len > 1) {
			final long first = values[0], second = values[1];
			if (areAnyNegative(first, second) || nnATimesBOverflows(first, second)) {
				return true;
			}

			long prod = first * second;
			for (int i = 2; i < len; i++) {
				final long val = values[i];
				if (isNegative(val) || nnATimesBOverflows(prod, val)) {
					return true;
				}
				prod *= val;
			}
		}

		return false;
	}

	// *** DIVISION *** //

	static boolean leftOverRightOverflowsThroughMaxValue(long left, long right) {

		return (left == MIN_VALUE && right == -1);
	}

	static boolean divisionOverflows(long left, long right) {

		return leftOverRightOverflowsThroughMaxValue(left, right);
	}

	static boolean areAnyNegativeDivision(long left, long right) {

		// a non negative division never overflows
		return areAnyNegative(left, right);
	}

	// *** ABSOLUTE VALUE *** //

	static boolean absoluteValueOverflowsThroughMaxValue(long val) {

		return (val == MIN_VALUE);
	}

	static boolean absoluteValueOverflows(long val) {

		return absoluteValueOverflowsThroughMaxValue(val);
	}

	// *** IS NEGATIVE *** //

	static boolean isNegative(long val) {

		return val < 0;
	}

	// optimization
	static boolean areAnyNegative(long a, long b) {

		return isNegative(a) || isNegative(b);
	}

	// optimization
	static boolean areAnyNegative(long a, long b, long c) {

		return isNegative(a) || isNegative(b) || isNegative(c);
	}

	// More optimized methods may be added here, each with a distinct number of arguments,
	// so as to avoid calling the varargs overload, which always requires an array object.

	static boolean areAnyNegative(long... values) {

		for (long val : values) {
			if (isNegative(val)) {
				return true;
			}
		}

		return false;
	}

	private LongTests() {

		// not used
	}
}
