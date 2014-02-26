package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class BoundedIntArithmetic {

	// *** ADDITION *** //

	static int addition(int a, int b) {

		if (IntTests.aPlusBOverflowsThroughMaxValue(a, b)) {
			return IntTests.MAX_VALUE;
		}
		else if (IntTests.aPlusBOverflowsThroughMinValue(a, b)) {
			return IntTests.MIN_VALUE;
		}
		else {
			return a + b;
		}
	}

	// *** SUBTRACTION *** //

	static int subtraction(int left, int right) {

		if (IntTests.leftMinusRightOverflowsThroughMaxValue(left, right)) {
			return IntTests.MAX_VALUE;
		}
		else if (IntTests.leftMinusRightOverflowsThroughMinValue(left, right)) {
			return IntTests.MIN_VALUE;
		}
		else {
			return left - right;
		}
	}

	// *** MULTIPLICATION *** //

	static int multiplication(int a, int b) {

		if (b > 0) {
			if (IntTests.aTimesPositiveBOverflowsThroughMaxValue(a, b)) {
				return IntTests.MAX_VALUE;
			}
			else if (IntTests.aTimesPositiveBOverflowsThroughMinValue(a, b)) {
				return IntTests.MIN_VALUE;
			}
		}
		else if (b < -1) {
			if (IntTests.aTimesSubMinusOneBOverflowsThroughMaxValue(a, b)) {
				return IntTests.MAX_VALUE;
			}
			else if (IntTests.aTimesSubMinusOneBOverflowsThroughMinValue(a, b)) {
				return IntTests.MIN_VALUE;
			}
		}
		else if (b == -1 && IntTests.aTimesMinusOneOverflowsThroughMaxValue(a)) {
			return IntTests.MAX_VALUE;
		}

		return a * b;
	}

	// *** DIVISION *** //

	static int division(int left, int right) {

		if (IntTests.leftOverRightOverflowsThroughMaxValue(left, right)) {
			return IntTests.MAX_VALUE;
		}
		else {
			return left / right;
		}
	}

	// *** ABSOLUTE VALUE *** //

	static int absoluteValue(int val) {

		if (IntTests.absoluteValueOverflowsThroughMaxValue(val)) {
			return IntTests.MAX_VALUE;
		}
		else {
			return Math.abs(val);
		}
	}

	// *** NARROWING CAST (LONG -> INT) *** //

	static int narrowingCast(long val) {

		if (IntTests.narrowingCastOverflowsThroughMinValue(val)) {
			return IntTests.MIN_VALUE;
		}
		else if (IntTests.narrowingCastOverflowsThroughMaxValue(val)) {
			return IntTests.MAX_VALUE;
		}
		else {
			return (int)val;
		}
	}

	private BoundedIntArithmetic() {

		// not used
	}
}
