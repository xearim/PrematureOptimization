package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Not implements Instruction {
    private InstructionType type = InstructionType.NOT;
    private final Value Argument;

    public Not(Value Argument) {
        this.Argument = Argument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "not ";
    	// and our two arguments
    	syntax += Argument.inAttSyntax() + "\n";
    	return syntax;
    }

}
