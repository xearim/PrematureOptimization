package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;
import edu.mit.compilers.codegen.asm.VariableReference;

public class MoveScalar implements Instruction {
    private InstructionType type = InstructionType.MOV;
    private final VariableReference reference;
    private final Value target;

    public MoveScalar(VariableReference reference, Value target) {
        this.reference = reference;
        this.target = target;
    }

    @Override
    public String inAttSyntax() {
    	// since its just a scalar, we just grab it where it is, and move it where we want it
    	return Instructions.move(reference, target).inAttSyntax();
    }

}
