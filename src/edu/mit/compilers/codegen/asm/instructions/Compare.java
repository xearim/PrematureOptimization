package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;

public class Compare implements Instruction {
    private InstructionType type = InstructionType.CMP;
    private final BinaryOperator op;
    private final Value leftArgument;
    private final Value rightArgument;

    public Compare(BinaryOperator cmp, Value leftArgument, Value rightArgument) {
    	this.op = cmp;
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    @Override
    public String inAttSyntax() {
    	String syntax = "";
    	// compare the arguments for a flag set
    	syntax += Instructions.compareFlagged(leftArgument, rightArgument);
    	// We are going to start stupid, assume that we are wrong, put false in R11
    	// And load the possible true into R10
    	syntax += Instructions.move(Literal.FALSE, Register.R11);
    	syntax += Instructions.move(Literal.TRUE, Register.R10);
    	switch(op){
		case DOUBLE_EQUALS:
			syntax += "cmove ";
			break;
		case GREATER_THAN:
			syntax += "cmovg ";
			break;
		case GREATER_THAN_OR_EQUAL:
			syntax += "cmovge ";
			break;
		case LESS_THAN:
			syntax += "cmovl ";
			break;
		case LESS_THAN_OR_EQUAL:
			syntax += "cmovle ";
			break;
		case NOT_EQUALS:
			syntax += "cmovne ";
			break;
		default:
			throw new AssertionError("Bad CMP instruction, unsupported comparitor: " + op.getSymbol());
    	}
    	syntax += Register.R10.inAttSyntax() + ", " + Register.R11.inAttSyntax() + "\n";
    	// now write out the resultant value back to our expected output location
    	syntax += Instructions.move(Register.R11, rightArgument);
    	return syntax;
    }

}