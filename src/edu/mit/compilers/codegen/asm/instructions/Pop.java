package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Pop extends Instruction {
    private InstructionType type = InstructionType.PUSH;
    public final Value Argument;

    public Pop(Value Argument) {
        this.Argument = Argument;
    }
    
    public Value getArguement() {
    	return this.Argument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "pop ";
    	// pop it from the stack
    	syntax += Argument.inAttSyntax();
    	return syntax;
    }

}
