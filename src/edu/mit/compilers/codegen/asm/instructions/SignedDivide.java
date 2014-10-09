package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class SignedDivide implements Instruction {
    private InstructionType type = InstructionType.IDIV;
    private final Value leftArgument;
    private final Value rightArgument;

    public SignedDivide(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
        // TODO Auto-generated method stub
        return null;
    }
}
