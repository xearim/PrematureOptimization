package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Label;

public class WriteLabel implements Instruction {
    private InstructionType type = InstructionType.LABEL;
    private final Label label;

    public WriteLabel(Label label) {
        this.label = label;
    }

    @Override
    public String inAttSyntax() {
    	return label.inAttSyntax() + "\n";
    }

}
