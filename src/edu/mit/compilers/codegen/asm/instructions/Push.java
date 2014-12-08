package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Push extends Instruction {
    private InstructionType type = InstructionType.PUSH;
    private final Value Argument;

    public Push(Value Argument) {
        this.Argument = Argument;
    }
    
    public Value getArguement() {
    	return this.Argument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "push ";
    	// push it onto the stack
    	syntax += Argument.inAttSyntax();
    	return syntax;
    }

}