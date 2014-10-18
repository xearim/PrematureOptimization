package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Value;

public class Leave implements Instruction {
    private InstructionType type = InstructionType.LEAVE;

    public Leave() {}

    @Override
    public String inAttSyntax() {
    	// Leave is just leave literally
    	return "leave";
    }

}