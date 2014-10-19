package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class SignedMultiply implements Instruction {
    private InstructionType type = InstructionType.IMUL;
    private final Value leftArgument;
    private final Value rightArgument;

    public SignedMultiply(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "imul ";
    	// mul left arg into right arg, result is stored there
    	syntax += leftArgument.inAttSyntax() + ", " + rightArgument.inAttSyntax();
    	return syntax;
    }

}
