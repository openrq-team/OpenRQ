package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class BoundedLongArithmetic {

	// *** ADDITION *** //

	static long addition(long a, long b) {

		if (LongTests.aPlusBOverflowsThroughMaxValue(a, b)) {
			return LongTests.MAX_VALUE;
		}
		else if (LongTests.aPlusBOverflowsThroughMinValue(a, b)) {
			return LongTests.MIN_VALUE;
		}
		else {
			return a + b;
		}
	}

	// *** SUBTRACTION *** //

	static long subtraction(long left, long right) {

		if (LongTests.leftMinusRightOverflowsThroughMaxValue(left, right)) {
			return LongTests.MAX_VALUE;
		}
		else if (LongTests.leftMinusRightOverflowsThroughMinValue(left, right)) {
			return LongTests.MIN_VALUE;
		}
		else {
			return left - right;
		}
	}

	// *** MULTIPLICATION *** //

	static long multiplication(long a, long b) {

		if (b > 0) {
			if (LongTests.aTimesPositiveBOverflowsThroughMaxValue(a, b)) {
				return LongTests.MAX_VALUE;
			}
			else if (LongTests.aTimesPositiveBOverflowsThroughMinValue(a, b)) {
				return LongTests.MIN_VALUE;
			}
		}
		else if (b < -1) {
			if (LongTests.aTimesSubMinusOneBOverflowsThroughMaxValue(a, b)) {
				return LongTests.MAX_VALUE;
			}
			else if (LongTests.aTimesSubMinusOneBOverflowsThroughMinValue(a, b)) {
				return LongTests.MIN_VALUE;
			}
		}
		else if (b == -1 && LongTests.aTimesMinusOneOverflowsThroughMaxValue(a)) {
			return LongTests.MAX_VALUE;
		}

		return a * b;
	}

	// *** DIVISION *** //

	static long division(long left, long right) {

		if (LongTests.leftOverRightOverflowsThroughMaxValue(left, right)) {
			return LongTests.MAX_VALUE;
		}
		else {
			return left / right;
		}
	}

	// *** ABSOLUTE VALUE *** //

	static long absoluteValue(long val) {

		if (LongTests.absoluteValueOverflowsThroughMaxValue(val)) {
			return LongTests.MAX_VALUE;
		}
		else {
			return Math.abs(val);
		}
	}

	private BoundedLongArithmetic() {

		// not used
	}
}
