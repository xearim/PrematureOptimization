package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Move implements Instruction {
    private InstructionType type = InstructionType.ADD;
    private final Value source;
    private final Value dest;

    public Move(Value source, Value dest) {
        this.source = source;
        this.dest = dest;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "movq ";
    	// Add left arg into right arg, result is stored there
    	syntax += source.inAttSyntax() + ", " + dest.inAttSyntax();
    	return syntax;
    }

}
