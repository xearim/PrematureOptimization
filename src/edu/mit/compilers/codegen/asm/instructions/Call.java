package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;

public class Call implements Instruction {
    private InstructionType type = InstructionType.CALL;
    private final Label function;

    public Call(Label function) {
        this.function = function;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "";
    	// We are going to be dumb/safe right now and always set %rax to 0 going into a call, so it will be universal
    	syntax += Instructions.move(new Literal(0), Register.RAX);
    	// and our two arguments
    	syntax += "call " + function.inAttSyntax() + "\n";
    	return syntax;
    }

}