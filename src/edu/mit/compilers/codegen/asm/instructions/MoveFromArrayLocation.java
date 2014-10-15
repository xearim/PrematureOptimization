package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;
import edu.mit.compilers.codegen.asm.VariableReference;

public class MoveFromArrayLocation implements Instruction {
    private InstructionType type = InstructionType.MOV;
    private final VariableReference reference;
    private final Value offset;
    private final long elementSize;
    private final Value target;

    public MoveFromArrayLocation(VariableReference reference, Value offset, long elementSize, Value target) {
        this.reference = reference;
        this.offset = offset;
        this.elementSize = elementSize;
        this.target = target;
    }

    @Override
    // Ight, this sucker is special, we have to add in the code to do a run time check that the location
    // in the array we are trying to access is actually valid
    // it will likely be optimal in the future to actually call a separate function that does
    // this check for us
    public String inAttSyntax() {
    	String syntax = "";
    	// Compare our desired access point to make sure it is in bounds
    	syntax += Instructions.compareFlagged(offset, new Literal(reference.getScope().getFromScope(reference.getName()).get().getLength().get()));
    	// TODO(xearim): Fix this once we can generate anonymous labels, jump to the error state 1 location
    	syntax += Instructions.jumpTyped(JumpType.JG, new Label(LabelType.CONTROL_FLOW, "errOneBadArrayBounds"));
    	// move the desired array location into our target
    	// assuming that this type of array access is valid stuff
    	syntax += "mov "; 
    	syntax += "(" + Long.toString(-8 + -8*reference.getScope().offsetFromBasePointer(reference.getName())) + " + " +
    			   offset.inAttSyntax() + "*" + Long.toString(elementSize) + " + " + Register.RBP.inAttSyntax() + ")";
    	syntax += ", " + target.inAttSyntax() + "\n";
    	return syntax;
    }

}
