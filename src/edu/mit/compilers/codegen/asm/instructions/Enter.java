package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.ast.Block;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Literal;

public class Enter implements Instruction {
    private InstructionType type = InstructionType.ENTER;
    private final Block methodBlock;

    public Enter(Block methodBlock) {
        this.methodBlock = methodBlock;
    }

    @Override
    public String inAttSyntax() {
        // get the stack size of the block, call enter for that size
        return "enter " + new Literal(Architecture.CONTAINS_ARRAYS
        		? Architecture.BYTES_PER_ENTRY * methodBlock.getMemorySize() + Architecture.ARRAY_INIT_SIZE
        		: Architecture.BYTES_PER_ENTRY * methodBlock.getMemorySize()).inAttSyntax() + 
               ", " + new Literal(0).inAttSyntax();
    }
}
