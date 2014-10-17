package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Label;

public class JumpTyped implements Instruction {
    private InstructionType type = InstructionType.JMP;
    private final JumpType jumpType;
    private final Label target;

    public JumpTyped(JumpType jumpType, Label target) {
    	this.jumpType = jumpType;
        this.target = target;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = jumpType.getName() + " ";
    	// and our target
    	syntax += target.inAttSyntax() + "\n";
    	return syntax;
    }

}