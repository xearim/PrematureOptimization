package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.Block;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;
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
        return new Push(value);
    }

    public static Instruction pop(Value target) {
        return new Pop(target);
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
    
    /** Does a logical AND */
    public static Instruction and(Value operand, Value target) {
        return new And(operand, target);
    }
    
    /** Does a logical OR */
    public static Instruction or(Value operand, Value target) {
        return new Or(operand, target);
    }
    
    /** Does a logical NOT */
    public static Instruction not(Value target) {
        return new Not(target);
    }
    
    /** Does a compare that produces an output */
    public static Instruction compare(BinaryOperator cmp, Value lhs, Value rhs) {
        return new Compare(cmp, lhs, rhs);
    }

    /** Does "mov referenceOffset(referenceBase, multipliedOffset, multiplier), target" */
    public static Instruction moveFromArray(VariableReference reference, Value multipliedOffset,
            long multiplier, Value target) {
        return new MoveFromArrayLocation(reference, multipliedOffset, multiplier, target);
    }
    
    /** Does "mov source, targetOffset(targetBase, multipliedOffset, multiplier)" */
    public static Instruction moveToArray(Value source, Value multipliedOffset,
            long multiplier, VariableReference target) {
        return new MoveToArrayLocation(source, multipliedOffset, multiplier, target);
    }
    
    /** Does "mov reference, target". */
    public static Instruction move(Value reference, Value target) {
        return new Move(reference, target);
    }

    /** Does `call .m_methodName`. */
    public static Instruction call(String methodName) {
        Label methodLabel = new Label(LabelType.METHOD, methodName);
        return new Call(methodLabel);
    }
    
    /** Does `jmp label.inAttSyntax`. */
    public static Instruction jump(Label target) {
    	return new Jump(target);
    }
    
    /** Does `jmp(type) label.inAttSyntax`. */
    public static Instruction jumpTyped(JumpType type, Label target) {
    	return new JumpTyped(type, target);
    }
    
    /** Does `enter $x, $0`. Calculated appropriately for a given method */
    public static Instruction enter(Block methodBlock){
    	return new Enter(methodBlock);
    }
    
    /** Does `leave` */
    public static Instruction leave(){
    	return new Leave();
    }
    
    /** Writes out the label given */
    public static Instruction writeLabel(Label label){
    	return new WriteLabel(label);
    }

    /** Exits with some error code. */
    public static Instruction errorExit() {
        return new PlaceHolder("Halt and catch fire.");
    }

    /** Issue a return instruction. */
    public static Instruction ret() {
        return new PlaceHolder("RET");
    }

}
