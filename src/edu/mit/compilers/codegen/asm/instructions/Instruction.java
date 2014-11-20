package edu.mit.compilers.codegen.asm.instructions;

// TODO(manny): Figure out if we need to separate this into "PseudoInstruction"
// and "RealInstruction."
/** An assembly instruction. */
public abstract class Instruction {
	/** Get an ASCII representation of this instruction in AT&T syntax. */
	public abstract String inAttSyntax();

	@Override
	public String toString() {
	    return inAttSyntax();
	}
}
