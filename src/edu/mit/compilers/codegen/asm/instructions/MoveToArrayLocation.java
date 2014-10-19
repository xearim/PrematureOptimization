package edu.mit.compilers.codegen.asm.instructions;

import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Literal;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.Value;
import edu.mit.compilers.codegen.asm.VariableReference;
import edu.mit.compilers.codegen.asm.Label.LabelType;

public class MoveToArrayLocation implements Instruction {
    private InstructionType type = InstructionType.MOV;
    private final VariableReference target;
    private final Value offset;
    private final long elementSize;
    private final Value source;

    public MoveToArrayLocation(Value source, Value offset, long elementSize, VariableReference target) {
        this.source = source;
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
    	syntax += Instructions.compareFlagged(offset, new Literal(target.getScope().getFromScope(target.getName()).get().getLength().get())).inAttSyntax() + "\n";
    	// TODO(xearim): Fix this once we can generate anonymous labels, jump to the error state 1 location
    	syntax += Instructions.jumpTyped(JumpType.JG, new Label(LabelType.CONTROL_FLOW, "errOneBadArrayBounds")).inAttSyntax() + "\n";
    	// move the desired array location into our target
    	// assuming that this type of array access is valid stuff
    	syntax += "mov "; 
    	syntax += source.inAttSyntax() + ", ";
    	syntax += "(" + offset.inAttSyntax() + "*" + Long.toString(elementSize) + " + " + 
    			   target.inAttSyntax() +  ")";
    	return syntax;
    }

}
