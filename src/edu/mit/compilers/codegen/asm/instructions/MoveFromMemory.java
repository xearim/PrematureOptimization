package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class MoveFromMemory implements Instruction {
    private final Register offset;
    private final Register reference;
    private final Register index;
    private final Literal elementSize;
    private final Value destination;

    public MoveFromMemory(Register offset, Register reference, Register index,
            Literal elementSize, Register destination) {
        this.offset = offset;
        this.reference = reference;
        this.index = index;
        this.elementSize = elementSize;
        this.destination = destination;
    }
    
    @Override
    public String inAttSyntax() {
        return String.format("movq %s(%s,%s,%d), %s",
                offset.inAttSyntax(), reference.inAttSyntax(),
                index.inAttSyntax(), elementSize.inAttSyntax(), destination.inAttSyntax());
    }
}
