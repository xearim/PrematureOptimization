package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class MoveFromMemory extends Instruction {
    private final long offset;
    private final Register reference;
    private final Register index;
    private final long elementSize;
    private final Value destination;

    public MoveFromMemory(long offset, Register reference, Register index,
            long elementSize, Register destination) {
        this.offset = offset;
        this.reference = reference;
        this.index = index;
        this.elementSize = elementSize;
        this.destination = destination;
    }
    
    @Override
    public String inAttSyntax() {
        return String.format("movq %d(%s,%s,%d), %s",
                offset, reference.inAttSyntax(),
                index.inAttSyntax(), elementSize, destination.inAttSyntax());
    }

	public Register getReference() {
		return reference;
	}

	public Register getIndex() {
		return index;
	}

	public Value getDestination() {
		return destination;
	}
}
