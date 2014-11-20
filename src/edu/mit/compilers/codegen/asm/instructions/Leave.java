package edu.mit.compilers.codegen.asm.instructions;


public class Leave extends Instruction {
    private InstructionType type = InstructionType.LEAVE;

    public Leave() {}

    @Override
    public String inAttSyntax() {
    	// Leave is just leave literally
    	return "leave";
    }

}