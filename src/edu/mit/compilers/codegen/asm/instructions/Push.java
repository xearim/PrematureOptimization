package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Push implements Instruction {
    private InstructionType type = InstructionType.PUSH;
    private final Value Argument;

    public Push(Value Argument) {
        this.Argument = Argument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "push ";
    	// push it onto the stack
    	syntax += Argument.inAttSyntax() + "\n";
    	return syntax;
    }

}