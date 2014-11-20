package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Subtract extends Instruction {
    private InstructionType type = InstructionType.SUB;
    private final Value leftArgument;
    private final Value rightArgument;

    public Subtract(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "sub ";
    	// sub left arg into right arg, result is stored there
    	syntax += leftArgument.inAttSyntax() + ", " + rightArgument.inAttSyntax();
    	return syntax;
    }

}
