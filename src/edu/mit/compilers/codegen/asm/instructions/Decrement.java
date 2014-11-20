package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Value;

public class Decrement extends Instruction {
    private InstructionType type = InstructionType.INC;
    private final Value Argument;

    public Decrement(Value target) {
        this.Argument = target;
    }

    @Override
    public String inAttSyntax() {
    	// Inc is just a fancy wrapper for an addition of 1
    	return Instructions.subtract(new Literal(1), Argument).inAttSyntax();
    }
}
