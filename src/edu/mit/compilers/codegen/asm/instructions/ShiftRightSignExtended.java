package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;

public class ShiftRightSignExtended extends Instruction {

    private final Register register;
    private final Literal distance;

    public ShiftRightSignExtended(Register register, Literal distance) {
        this.register = register;
        this.distance = distance;
    }

    @Override
    public String inAttSyntax() {
        return String.format("sarq %s, %s", distance.inAttSyntax(), register.inAttSyntax());
    }

}
