package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Value;

public class Increment extends Instruction {
    private InstructionType type = InstructionType.INC;
    private final Value Argument;

    public Increment(Value target) {
        this.Argument = target;
    }

    @Override
    public String inAttSyntax() {
    	// Inc is just a fancy wrapper for an addition of 1
    	return Instructions.add(new Literal(1), Argument).inAttSyntax();
    }
}
