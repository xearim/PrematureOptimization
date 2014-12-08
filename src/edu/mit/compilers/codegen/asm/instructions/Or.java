package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Or extends Instruction {
    private InstructionType type = InstructionType.OR;
    private final Value leftArgument;
    private final Value rightArgument;

    public Or(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "or ";
    	// or our two arguments
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
