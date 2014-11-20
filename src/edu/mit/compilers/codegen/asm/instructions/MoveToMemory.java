package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class MoveToMemory extends Instruction {
    private final long offset;
    private final Register reference;
    private final Register index;
    private final long elementSize;
    private final Value source;

    public MoveToMemory(Value source, long offset, Register reference, Register index,
            long elementSize) {
        this.offset = offset;
        this.reference = reference;
        this.index = index;
        this.elementSize = elementSize;
        this.source = source;
    }
    
    @Override
    public String inAttSyntax() {
        return String.format("movq %s, %d(%s,%s,%d)",
                source.inAttSyntax(), offset, reference.inAttSyntax(),
                index.inAttSyntax(), elementSize);
    }
}
