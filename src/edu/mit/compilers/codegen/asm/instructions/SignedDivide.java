package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;
import edu.mit.compilers.codegen.asm.VariableReference;

public class SignedDivide implements Instruction {
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
    	// Put the division target in %rax
    	syntax += Instructions.move(leftArgument, Register.RAX).inAttSyntax() + "\n";
    	// Zero out %rdx just in case (it shouldn't hold any values)
    	syntax += Instructions.move(Literal.FALSE, Register.RDX).inAttSyntax() + "\n";
    	// Divide
    	syntax += "idiv " + rightArgument.inAttSyntax() + "\n";
    	// Return the result into the rightArgument as we expect
    	syntax += Instructions.move(Register.RAX, rightArgument);
    	return syntax;
    }
}
