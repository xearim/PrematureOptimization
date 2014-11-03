package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.common.Variable;

/** A reference to a variable, in a certain scope. */
public class VariableReference implements Value{
    private final Variable variable;
    /** The scope in which this variable is referenced. */
    private final Scope scope;
    private final boolean inMethodCall;

    public VariableReference(Variable name, Scope scope, boolean inMethodCall) {
        this.variable = name;
        this.scope = scope;
        this.inMethodCall = inMethodCall;
    }
    
    public VariableReference(Variable name, Scope scope) {
        this(name, scope, false);
    }

    public Variable getVariable() {
        return variable;
    }

    public Scope getScope() {
        return scope;
    }
    
    @Override
    public String inAttSyntax() {
        Scope targetScope = scope.getLocation(variable);
        switch(targetScope.getScopeType()){
        case GLOBAL:
            // Globals are their name
            return new Label(LabelType.GLOBAL, variable).inAttSyntax();
        case LOCAL:
            // Locals are some offset of the base pointer
            // its under the return address, then offset by all the other scopes
            return new Location(Register.RBP, 
                    -1*(Architecture.WORD_SIZE*targetScope.offsetFromBasePointer(variable))).inAttSyntax();
        case PARAMETER:
            // Parameters are either a register or a base pointer offset
            // dont know if i love this cast
            switch((int) targetScope.offset(variable)){
            case 0:
                // Holds the first arg
                return inMethodCall
                		? new Location(Register.RSP, Architecture.WORD_SIZE*(5L)).inAttSyntax()
                		: Register.RDI.inAttSyntax();
            case 1:
                // Holds the second arg
                return inMethodCall
                		? new Location(Register.RSP, Architecture.WORD_SIZE*(4L)).inAttSyntax()
                		: Register.RSI.inAttSyntax();
            case 2:
                // Holds the third arg
                return inMethodCall
                		? new Location(Register.RSP, Architecture.WORD_SIZE*(3L)).inAttSyntax()
                		: Register.RDX.inAttSyntax();
            case 3:
                // Holds the fourth arg
                return inMethodCall
                		? new Location(Register.RSP, Architecture.WORD_SIZE*(2L)).inAttSyntax()
                		: Register.RCX.inAttSyntax();
            case 4:
                // Holds the fifth arg
                return inMethodCall
                		? new Location(Register.RSP, Architecture.WORD_SIZE*(1L)).inAttSyntax()
                		: Register.R8.inAttSyntax();
            case 5:
                // Holds the sixth arg
                return inMethodCall
                		? new Location(Register.RSP, Architecture.WORD_SIZE*(0L)).inAttSyntax()
                		: Register.R9.inAttSyntax();
            default:
                // All other args are at some offset to the base pointer
                // its the old %rbp and %rsp then up to the arg
                return new Location(Register.RBP,
                        Architecture.WORD_SIZE*2 + Architecture.WORD_SIZE*targetScope.offsetInParameterSet(variable)).inAttSyntax();
            }
        default:
            throw new AssertionError("Variable " + variable + " not in scope, this should never happen");
        }
    }
}
