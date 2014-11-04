package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Architecture;

public class Enter implements Instruction {
    private InstructionType type = InstructionType.ENTER;
    private final int entries;

    public Enter(int entries) {
        this.entries = entries;
    }

    @Override
    public String inAttSyntax() {
        // get the stack size of the block, call enter for that size
        return String.format("enter %d, 0", getSize());
    }

    private long getSize() {
        return Architecture.BYTES_PER_ENTRY * entries +
                (Architecture.CONTAINS_ARRAYS ? Architecture.ARRAY_INIT_SIZE : 0);
    }
}
