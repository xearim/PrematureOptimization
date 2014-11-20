package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class And extends Instruction {
    private InstructionType type = InstructionType.AND;
    private final Value leftArgument;
    private final Value rightArgument;

    public And(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "and ";
    	// and our two arguments
    	syntax += leftArgument.inAttSyntax() + ", " + rightArgument.inAttSyntax();
    	return syntax;
    }

}
