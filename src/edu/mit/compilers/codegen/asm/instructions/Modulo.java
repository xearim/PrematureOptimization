package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class Modulo implements Instruction {
    private InstructionType type = InstructionType.MODULO;
    private final Value leftArgument;
    private final Value rightArgument;

    public Modulo(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	// A modulo is a special type of signed divide in that you need to use %rax and %rdx as intermediates
    	String syntax = "";
    	// Put the division target in %rax
    	syntax += Instructions.move(leftArgument, Register.RAX);
    	// Zero out %rdx just in case (it shouldn't hold any values)
    	syntax += Instructions.move(Literal.FALSE, Register.RDX);
    	// Divide
    	syntax += "idiv " + rightArgument.inAttSyntax() + "\n";
    	// Return the resultant remainder into the rightArgument as we expect
    	syntax += Instructions.move(Register.RDX, rightArgument);
    	return syntax;
    }
}