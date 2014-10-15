package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Pop implements Instruction {
    private InstructionType type = InstructionType.PUSH;
    private final Value Argument;

    public Pop(Value Argument) {
        this.Argument = Argument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "pop ";
    	// pop it from the stack
    	syntax += Argument.inAttSyntax() + "\n";
    	return syntax;
    }

}
