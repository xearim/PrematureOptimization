package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Label;

public class Jump implements Instruction {
    private InstructionType type = InstructionType.JMP;
    private final Label target;

    public Jump(Label target) {
        this.target = target;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "jmp ";
    	// and our target
    	syntax += target.inAttSyntax();
    	return syntax;
    }

}