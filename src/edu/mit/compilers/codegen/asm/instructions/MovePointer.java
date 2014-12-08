package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Value;

public class MovePointer extends Instruction {
    private InstructionType type = InstructionType.MOV;
    private final Label source;
    private final Value dest;

    public MovePointer(Label source, Value dest) {
        this.source = source;
        this.dest = dest;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "movq ";
    	// Add left arg into right arg, result is stored there
        syntax += "$" + source.inAttSyntax() + ", " + dest.inAttSyntax();
    	return syntax;
    }

	public Label getSource() {
		return source;
	}

	public Value getDest() {
		return dest;
	}

}
