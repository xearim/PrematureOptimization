package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class Modulo extends Instruction {
    private InstructionType type = InstructionType.MODULO;
    private final Value leftArgument;
    private final Value rightArgument;

    public Modulo(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    // TODO(jasonpr): Do not let one Instruction contain multiple x86 instructions!
    @Override
    public String inAttSyntax() {
    	// A modulo is a special type of signed divide in that you need to use %rax and %rdx as intermediates
    	String syntax = "";
    	// Gotta store away RDX incase it is being used by the caller
    	syntax += Instructions.push(Register.RDX).inAttSyntax() + "\n";
    	// Put the division target in %rax
    	syntax += Instructions.move(rightArgument, Register.RAX).inAttSyntax() + "\n";
    	// TODO(jasonpr): Factor out common code from here and SignedDivide.
    	// Make %rdx:%rax be a 128-bit representation of %rax.
        // %rdx is either 0b00...0 or 0b11...1, in order to sign-extend properly.
    	syntax += Instructions.move(Register.RAX, Register.RDX).inAttSyntax() + "\n";
        syntax += Instructions.shiftRightSignExtended(Register.RDX, new Literal(63)).inAttSyntax() + "\n";

    	// Divide
    	syntax += "idiv " + leftArgument.inAttSyntax() + "\n";
    	// Return the resultant remainder into the rightArgument as we expect
    	syntax += Instructions.move(Register.RDX, rightArgument).inAttSyntax() + "\n";
    	// Restore the value of RDX
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