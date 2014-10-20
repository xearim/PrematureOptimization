package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class CompareFlagged implements Instruction {
    private InstructionType type = InstructionType.CMPF;
    private final Value leftArgument;
    private final Value rightArgument;

    public CompareFlagged(Value leftArgument, Value rightArgument) {
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "";
    	// For now we are going to be extra safe and register everything up
    	// TODO:(xearim) fix specs or something so this isnt so dirty
    	syntax += Instructions.push(rightArgument).inAttSyntax() + "\n";
    	syntax += Instructions.push(leftArgument).inAttSyntax() + "\n";
    	syntax += Instructions.pop(Register.R10).inAttSyntax() + "\n";
    	syntax += Instructions.pop(Register.R11).inAttSyntax() + "\n";
    	syntax += "cmp ";
    	// because of x86, gotta flip arguments to maintain abstraction
    	syntax += Register.R11.inAttSyntax() + ", " + Register.R10.inAttSyntax();
    	return syntax;
    }

}