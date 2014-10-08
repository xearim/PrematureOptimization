package edu.mit.compilers.codegen.asm;

// TODO(manny): Figure out if we need to separate this into "PseudoInstruction"
// and "RealInstruction."
/** An assembly instruction. */
public interface Instruction {
	/** Get an ASCII representation of this instruction in AT&T syntax. */
	public String inAttSyntax();
}
