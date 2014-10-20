package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class CompareFlagged implements Instruction {
    private InstructionType type = InstructionType.CMPF;
    private final Value leftArgument;
    private final Value rightArgument;

    public CompareFlagged(Register leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
        // Yes, the right argument comes to the left of the left argument.
        return String.format("cmp %s, %s",
                rightArgument.inAttSyntax(), leftArgument.inAttSyntax());
    }

}