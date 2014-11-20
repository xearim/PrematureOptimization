package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Value;

public class Negate extends Instruction {
    private InstructionType type = InstructionType.NEG;
    private final Value Argument;

    public Negate(Value target) {
        this.Argument = target;
    }

    @Override
    public String inAttSyntax() {
    	// Negate is just a fancy wrapper for a multiplication by -1
    	return Instructions.multiply(new Literal(-1), Argument).inAttSyntax();
    }
}