package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Longs {

	// ***************************** COMPARISONS *************************** //

	// MAX //

	public static long max(long a, long b) {

		return LongComparisons.max(a, b);
	}

	public static long max(long a, long b, long c) {

		return LongComparisons.max(a, b, c);
	}

	public static long max(long... values) {

		return LongComparisons.max(values);
	}

	// MIN //

	public static long min(long a, long b) {

		return LongComparisons.min(a, b);
	}

	public static long min(long a, long b, long c) {

		return LongComparisons.min(a, b, c);
	}

	public static long min(long... values) {

		return LongComparisons.min(values);
	}

	// ****************************** ADDITION ***************************** //

	// TESTS //

	public static boolean addOverflows(long a, long b) {

		return LongTests.additionOverflows(a, b);
	}

	public static boolean addOverflows(long... values) {

		return LongTests.additionOverflows(values);
	}

	public static boolean addOverflowsOrIsNegative(long a, long b) {

		return LongTests.areAnyNegativeAddition(a, b);
	}

	public static boolean addOverflowsOrIsNegative(long... values) {

		return LongTests.areAnyNegativeAddition(values);
	}

	// EXACT ARITHMETIC //

	public static long addExact(long a, long b) {

		return ExactLongArithmetic.addition(a, b);
	}

	public static long addExact(long... values) {

		return ExactLongArithmetic.addition(values);
	}

	public static long addExactNonNegative(long a, long b) {

		return ExactLongArithmetic.allNonNegativeAddition(a, b);
	}

	public static long addExactNonNegative(long... values) {

		return ExactLongArithmetic.allNonNegativeAddition(values);
	}

	// BOUNDED ARITHMETIC //

	public static long addBounded(long a, long b) {

		return BoundedLongArithmetic.addition(a, b);
	}

	// **************************** SUBTRACTION **************************** //

	// TESTS //

	public static boolean subtOverflows(long left, long right) {

		return LongTests.subtractionOverflows(left, right);
	}

	public static boolean subtOverflowsOrIsNegative(long left, long right) {

		return LongTests.areAnyNegativeSubtraction(left, right);
	}

	// EXACT ARITHMETIC //

	public static long subtExact(long left, long right) {

		return ExactLongArithmetic.subtraction(left, right);
	}

	public static long subtExactNonNegative(long left, long right) {

		return ExactLongArithmetic.allNonNegativeSubtraction(left, right);
	}

	// BOUNDED ARITHMETIC //

	public static long subtBounded(long left, long right) {

		return BoundedLongArithmetic.subtraction(left, right);
	}

	// *************************** MULTIPLICATION ************************** //

	// TESTS //

	public static boolean multOverflows(long a, long b) {

		return LongTests.multiplicationOverflows(a, b);
	}

	public static boolean multOverflows(long... values) {

		return LongTests.multiplicationOverflows(values);
	}

	public static boolean multOverflowsOrIsNegative(long a, long b) {

		return LongTests.areAnyNegativeMultiplication(a, b);
	}

	public static boolean multOverflowsOrIsNegative(long... values) {

		return LongTests.areAnyNegativeMultiplication(values);
	}

	// EXACT ARITHMETIC //

	public static long multExact(long a, long b) {

		return ExactLongArithmetic.multiplication(a, b);
	}

	public static long multExact(long... values) {

		return ExactLongArithmetic.multiplication(values);
	}

	public static long multExactNonNegative(long a, long b) {

		return ExactLongArithmetic.allNonNegativeMultiplication(a, b);
	}

	public static long multExactNonNegative(long... values) {

		return ExactLongArithmetic.allNonNegativeMultiplication(values);
	}

	// BOUNDED ARITHMETIC //

	public static long multBounded(long a, long b) {

		return BoundedLongArithmetic.multiplication(a, b);
	}

	// ****************************** DIVISION ***************************** //

	// TESTS //

	public static boolean divOverflows(long left, long right) {

		return LongTests.divisionOverflows(left, right);
	}

	public static boolean divOverflowsOrIsNegative(long left, long right) {

		return LongTests.areAnyNegativeDivision(left, right);
	}

	// EXACT ARITHMETIC //

	public static long divExact(long left, long right) {

		return ExactLongArithmetic.division(left, right);
	}

	public static long divExactNonNegative(long left, long right) {

		return ExactLongArithmetic.allNonNegativeDivision(left, right);
	}

	// BOUNDED ARITHMETIC //

	public static long divBounded(long left, long right) {

		return BoundedLongArithmetic.division(left, right);
	}

	// *************************** ABSOLUTE VALUE ************************** //

	// TESTS //

	public static boolean absOverflows(long value) {

		return LongTests.absoluteValueOverflows(value);
	}

	// EXACT ARITHMETIC //

	public static long absExact(long val) {

		return ExactLongArithmetic.absoluteValue(val);
	}

	// BOUNDED ARITHMETIC //

	public static long absBounded(long val) {

		return BoundedLongArithmetic.absoluteValue(val);
	}

	// ************************ NON-NEGATIVE ASSERTION ********************* //

	public static long assertNonNegative(long val) {

		ExactLongArithmetic.assertIsNonNegative(val);
		return val;
	}

	public static void assertNonNegative(long a, long b) {

		ExactLongArithmetic.assertAllAreNonNegative(a, b);
	}

	public static void assertNonNegative(long a, long b, long c) {

		ExactLongArithmetic.assertAllAreNonNegative(a, b, c);
	}

	public static void assertNonNegative(long... values) {

		ExactLongArithmetic.assertAllAreNonNegative(values);
	}

	private Longs() {

		// not used
	}
}
