package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;
import edu.mit.compilers.codegen.asm.VariableReference;

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

    public static Instruction pop(Value target) {
        return new PlaceHolder("POP " + target);
    }

    /** Do target += operand. */
    public static Instruction add(Value operand, Value target) {
        return new Add(operand, target);
    }

    /** Do target -= operand. */
    public static Instruction subtract(Value operand, Value target) {
        return new Subtract(operand, target);
    }

    /** Do target *= operand. */
    public static Instruction multiply(Value operand, Value target) {
        return new SignedMultiply(operand, target);
    }

    /** Do target /= operand. */
    public static Instruction divide(Value operand, Value target) {
        return new SignedDivide(operand, target);
    }

    /** Do target %= operand. */
    public static Instruction modulo(Value operand, Value target) {
        return new Modulo(operand, target);
    }

    /** Do target = -target. */
    public static Instruction negate(Value target) {
        return new Negate(target);
    }

    /** Do target += 1. */
    public static Instruction increment(Value target) {
        return new Increment(target);
    }
    
    /** Do target -= 1. */
    public static Instruction decrement(Value target) {
        return new Decrement(target);
    }
    
    /** Does a compare that produces flags */
    public static Instruction compareFlagged(Value lhs, Value rhs) {
        return new CompareFlagged(lhs, rhs);
    }
    
    /** Does a compare that produces an output */
    public static Instruction compare(BinaryOperator cmp, Value lhs, Value rhs) {
        return new Compare(cmp, lhs, rhs);
    }

    /** Does "mov referenceOffset(referenceBase, multipliedOffset, multiplier), target" */
    public static Instruction moveVariable(VariableReference reference, Value multipliedOffset,
            long multiplier, Value target) {
        return new PlaceHolder("MOV " + reference + " " + multipliedOffset + "*" + multiplier
                + " to " + target);
    }

    /** Does "mov referenceOffset(referenceBase), target". */
    public static Instruction moveVariable(VariableReference reference, Value target) {
        return new PlaceHolder("MOV " + reference + " " + target);
    }
    
    /** Does "mov reference, target". */
    public static Instruction move(Value reference, Value target) {
        return new PlaceHolder("MOV " + reference + " " + target);
    }

    /** Does `call .m_methodName`. */
    public static Instruction call(String methodName) {
        Label methodLabel = new Label(LabelType.METHOD, methodName);
        return new PlaceHolder("CALL " + methodLabel);
    }

}
