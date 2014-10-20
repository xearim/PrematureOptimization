package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;

public class MoveToMemory implements Instruction {
    private final Register offset;
    private final Register reference;
    private final Register index;
    private final Literal elementSize;
    private final Register source;

    public MoveToMemory(Register source, Register offset, Register reference, Register index,
            Literal elementSize) {
        this.offset = offset;
        this.reference = reference;
        this.index = index;
        this.elementSize = elementSize;
        this.source = source;
    }
    
    @Override
    public String inAttSyntax() {
        return String.format("movq %s, %s(%s,%s,%d)",
                source.inAttSyntax(), offset.inAttSyntax(), reference.inAttSyntax(),
                index.inAttSyntax(), elementSize.inAttSyntax());
    }
}
