package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Subtract implements Instruction {
    private InstructionType type = InstructionType.SUB;
    private final Value leftArgument;
    private final Value rightArgument;

    public Subtract(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
        // TODO Auto-generated method stub
        return null;
    }

}
