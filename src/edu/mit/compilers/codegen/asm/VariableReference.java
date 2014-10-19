package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Label.LabelType;

/** A reference to a variable, in a certain scope. */
public class VariableReference implements Value{
    private final String name;
    /** The scope in which this variable is referenced. */
    private final Scope scope;

    public VariableReference(String name, Scope scope) {
        this.name = name;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public Scope getScope() {
        return scope;
    }
    

    @Override
    public String inAttSyntax() {
    	Scope targetScope = scope.getLocation(name);
		switch(targetScope.getScopeType()){
		case GLOBAL:
			// Globals are their name
			return new Label(LabelType.GLOBAL, name).inAttSyntax();
		case LOCAL:
			// Locals are some offset of the base pointer
			// its under the return address, then offset by all the other scopes
			return Long.toString(-1*(Architecture.WORD_SIZE + Architecture.WORD_SIZE*targetScope.offsetFromBasePointer(name))) + 
								"(" + Register.RBP.inAttSyntax() + ")";
		case PARAMETER:
			// Parameters are either a register or a base pointer offset
			// dont know if i love this cast
			switch((int) targetScope.offset(name)){
			case 0:
				// Holds the first arg
				return Register.RDI.inAttSyntax();
			case 1:
				// Holds the second arg
				return Register.RSI.inAttSyntax();
			case 2:
				// Holds the third arg
				return Register.RDX.inAttSyntax();
			case 3:
				// Holds the fourth arg
				return Register.RCX.inAttSyntax();
			case 4:
				// Holds the fifth arg
				return Register.R8.inAttSyntax();
			case 5:
				// Holds the sixth arg
				return Register.R9.inAttSyntax();
			default:
				// All other args are at some offset to the base pointer
				// its the old %rbp and %rsp then up to the arg
				return Long.toString(Architecture.WORD_SIZE*2 + Architecture.WORD_SIZE*targetScope.offsetInParameterSet(name)) + 
									"(" + Register.RBP.inAttSyntax() + ")";
			}
		default:
			throw new AssertionError("Variable " + name + " not in scope, this should never happen");
		}
    }
}
