package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class SignedDivide extends Instruction {
    private InstructionType type = InstructionType.IDIV;
    private final Value leftArgument;
    private final Value rightArgument;

    public SignedDivide(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	// A signed divide is special in that you need to use %rax and %rdx as intermediates
    	String syntax = "";
    	// Gotta store the value of RDX incase the caller expects it
    	syntax += Instructions.push(Register.RDX).inAttSyntax()  + "\n";
    	// Put the division target in %rax
    	syntax += Instructions.move(rightArgument, Register.RAX).inAttSyntax() + "\n";
    	// Make %rdx:%rax be a 128-bit representation of %rax.
    	// %rdx is either 0b00...0 or 0b11...1, in order to sign-extend properly.
        syntax += Instructions.move(Register.RAX, Register.RDX).inAttSyntax() + "\n";
        syntax += Instructions.shiftRightSignExtended(Register.RDX, new Literal(63)).inAttSyntax() + "\n";
    	// Divide
    	syntax += "idiv " + leftArgument.inAttSyntax() + "\n";
    	// Return the result into the rightArgument as we expect
    	syntax += Instructions.move(Register.RAX, rightArgument).inAttSyntax() + "\n";
    	// Restore the value of RDX for the caller
    	syntax += Instructions.pop(Register.RDX).inAttSyntax();
    	return syntax;
    }

	public Value getLeftArgument() {
		return leftArgument;
	}

	public Value getRightArgument() {
		return rightArgument;
	}
}
