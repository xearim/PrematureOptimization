package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

/**
 * A million static factory methods for creating assembly instructions.
 *
 * <p>This class currently exists to give a nice API to the classes in the
 * controllinker package.  Once the APIs of individual instructions are stable,
 * we may decide that this class is a useless layer of indirection, and remove it. 
 */
public final class Instructions {

    private Instructions() {}

    public static Instruction push(Value value) {
        return new PlaceHolder("PUSH " + value);
    }

    public static Instruction pop(Register register) {
        return new PlaceHolder("POP " + register);
    }

    /** Do target += operand. */
    public static Instruction add(Register operand, Register target) {
        return new Add(operand, target);
    }

    /** Do target -= operand. */
    public static Instruction subtract(Register operand, Register target) {
        return new Subtract(operand, target);
    }

    /** Do target *= operand. */
    public static Instruction multiply(Register operand, Register target) {
        return new SignedMultiply(operand, target);
    }

    /** Do target /= operand. */
    public static Instruction divide(Register operand, Register target) {
        return new SignedDivide(operand, target);
    }

    /** Do target %= operand. */
    public static Instruction modulo(Register operand, Register target) {
        return new PlaceHolder("MODULO " + operand + " " + target);
    }

    /** Do target = -target. */
    public static Instruction negate(Register target) {
        return new PlaceHolder("NEG " + target);
    }
}
