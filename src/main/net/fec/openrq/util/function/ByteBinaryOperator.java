package net.fec.openrq.util.function;

/**
 * Represents an operation upon two {@code byte}-valued operands and producing an {@code byte}-valued result.
 */
public interface ByteBinaryOperator {

    /**
     * Applies this operator to the given operands.
     * 
     * @param left
     *            the first operand
     * @param right
     *            the second operand
     * @return the operator result
     */
    byte applyAsByte(byte left, byte right);
}
