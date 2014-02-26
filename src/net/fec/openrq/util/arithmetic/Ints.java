package net.fec.openrq.util.arithmetic;

/**
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Ints {

	// ***************************** COMPARISONS *************************** //

	// MAX //

	public static int max(int a, int b) {

		return IntComparisons.max(a, b);
	}

	public static int max(int a, int b, int c) {

		return IntComparisons.max(a, b, c);
	}

	public static int max(int... values) {

		return IntComparisons.max(values);
	}

	// MIN //

	public static int min(int a, int b) {

		return IntComparisons.min(a, b);
	}

	public static int min(int a, int b, int c) {

		return IntComparisons.min(a, b, c);
	}

	public static int min(int... values) {

		return IntComparisons.min(values);
	}

	// ****************************** ADDITION ***************************** //

	// TESTS //

	public static boolean addOverflows(int a, int b) {

		return IntTests.additionOverflows(a, b);
	}

	public static boolean addOverflows(int... values) {

		return IntTests.additionOverflows(values);
	}

	public static boolean addOverflowsOrIsNegative(int a, int b) {

		return IntTests.areAnyNegativeAddition(a, b);
	}

	public static boolean addOverflowsOrIsNegative(int... values) {

		return IntTests.areAnyNegativeAddition(values);
	}

	// EXACT ARITHMETIC //

	public static int addExact(int a, int b) {

		return ExactIntArithmetic.addition(a, b);
	}

	public static int addExact(int... values) {

		return ExactIntArithmetic.addition(values);
	}

	public static int addExactNonNegative(int a, int b) {

		return ExactIntArithmetic.allNonNegativeAddition(a, b);
	}

	public static int addExactNonNegative(int... values) {

		return ExactIntArithmetic.allNonNegativeAddition(values);
	}

	// BOUNDED ARITHMETIC //

	public static int addBounded(int a, int b) {

		return BoundedIntArithmetic.addition(a, b);
	}

	// **************************** SUBTRACTION **************************** //

	// TESTS //

	public static boolean subtOverflows(int left, int right) {

		return IntTests.subtractionOverflows(left, right);
	}

	public static boolean subtOverflowsOrIsNegative(int left, int right) {

		return IntTests.areAnyNegativeSubtraction(left, right);
	}

	// EXACT ARITHMETIC //

	public static int subtExact(int left, int right) {

		return ExactIntArithmetic.subtraction(left, right);
	}

	public static int subtExactNonNegative(int left, int right) {

		return ExactIntArithmetic.allNonNegativeSubtraction(left, right);
	}

	// BOUNDED ARITHMETIC //

	public static int subtBounded(int left, int right) {

		return BoundedIntArithmetic.subtraction(left, right);
	}

	// *************************** MULTIPLICATION ************************** //

	// TESTS //

	public static boolean multOverflows(int a, int b) {

		return IntTests.multiplicationOverflows(a, b);
	}

	public static boolean multOverflows(int... values) {

		return IntTests.multiplicationOverflows(values);
	}

	public static boolean multOverflowsOrIsNegative(int a, int b) {

		return IntTests.areAnyNegativeMultiplication(a, b);
	}

	public static boolean multOverflowsOrIsNegative(int... values) {

		return IntTests.areAnyNegativeMultiplication(values);
	}

	// EXACT ARITHMETIC //

	public static int multExact(int a, int b) {

		return ExactIntArithmetic.multiplication(a, b);
	}

	public static int multExact(int... values) {

		return ExactIntArithmetic.multiplication(values);
	}

	public static int multExactNonNegative(int a, int b) {

		return ExactIntArithmetic.allNonNegativeMultiplication(a, b);
	}

	public static int multExactNonNegative(int... values) {

		return ExactIntArithmetic.allNonNegativeMultiplication(values);
	}

	// BOUNDED ARITHMETIC //

	public static int multBounded(int a, int b) {

		return BoundedIntArithmetic.multiplication(a, b);
	}

	// ****************************** DIVISION ***************************** //

	// TESTS //

	public static boolean divOverflows(int left, int right) {

		return IntTests.divisionOverflows(left, right);
	}

	public static boolean divOverflowsOrIsNegative(int left, int right) {

		return IntTests.areAnyNegativeDivision(left, right);
	}

	// EXACT ARITHMETIC //

	public static int divExact(int left, int right) {

		return ExactIntArithmetic.division(left, right);
	}

	public static int divExactNonNegative(int left, int right) {

		return ExactIntArithmetic.allNonNegativeDivision(left, right);
	}

	// BOUNDED ARITHMETIC //

	public static int divBounded(int left, int right) {

		return BoundedIntArithmetic.division(left, right);
	}

	// *************************** ABSOLUTE VALUE ************************** //

	// TESTS //

	public static boolean absOverflows(int value) {

		return IntTests.absoluteValueOverflows(value);
	}

	// EXACT ARITHMETIC //

	public static int absExact(int val) {

		return ExactIntArithmetic.absoluteValue(val);
	}

	// BOUNDED ARITHMETIC //

	public static int absBounded(int val) {

		return BoundedIntArithmetic.absoluteValue(val);
	}

	// ******************** NARROWING CAST (LONG -> INT) ******************* //

	// TESTS //

	public static boolean castOverflows(long value) {

		return IntTests.narrowingCastOverflows(value);
	}

	// EXACT ARITHMETIC //

	public static int castExact(long val) {

		return ExactIntArithmetic.narrowingCast(val);
	}

	public static int castExactNonNegative(long val) {

		return ExactIntArithmetic.nonNegativeNarrowingCast(val);
	}

	// BOUNDED ARITHMETIC //

	public static int castBounded(long val) {

		return BoundedIntArithmetic.narrowingCast(val);
	}

	// ************************ NON-NEGATIVE ASSERTION ********************* //

	public static int assertNonNegative(int val) {

		ExactIntArithmetic.assertIsNonNegative(val);
		return val;
	}

	public static void assertNonNegative(int a, int b) {

		ExactIntArithmetic.assertAllAreNonNegative(a, b);
	}

	public static void assertNonNegative(int... values) {

		ExactIntArithmetic.assertAllAreNonNegative(values);
	}

	private Ints() {

		// not used
	}
}
