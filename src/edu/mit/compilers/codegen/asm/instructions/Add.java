package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

/**
 * A representation of the x86 ADD instruction.
 *
 * <p>See Intel documentation for the spec.
 */
public class Add extends Instruction {
    private InstructionType type = InstructionType.ADD;
    private final Value leftArgument;
    private final Value rightArgument;

    public Add(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "addq ";
    	// Add left arg into right arg, result is stored there
    	syntax += leftArgument.inAttSyntax() + ", " + rightArgument.inAttSyntax();
    	return syntax;
    }

	public Value getLeftArgument() {
		return leftArgument;
	}

	public Value getRightArgument() {
		return rightArgument;
	}
}
