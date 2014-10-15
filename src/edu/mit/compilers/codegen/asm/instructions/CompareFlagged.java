package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class CompareFlagged implements Instruction {
    private InstructionType type = InstructionType.CMPF;
    private final Value leftArgument;
    private final Value rightArgument;

    public CompareFlagged(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "cmp ";
    	// because of x86, gotta flip arguments to maintain abstraction
    	syntax += rightArgument.inAttSyntax() + ", " + leftArgument.inAttSyntax() + "\n";
    	return syntax;
    }

}