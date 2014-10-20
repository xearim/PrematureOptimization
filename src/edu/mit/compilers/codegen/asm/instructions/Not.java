package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class Not implements Instruction {
    private InstructionType type = InstructionType.NOT;
    private final Value Argument;

    public Not(Value Argument) {
        this.Argument = Argument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "";
    	// We gonna do the whole x = -1*(x - 1) thing for negation
    	syntax += Instructions.move(Argument, Register.R10).inAttSyntax() + "\n";
    	syntax += Instructions.subtract(new Literal(1), Register.R10).inAttSyntax() + "\n";
    	syntax += Instructions.multiply(new Literal(-1), Register.R10).inAttSyntax() + "\n";
    	syntax += Instructions.move(Register.R10, Argument).inAttSyntax();
    	return syntax;
    }

}
