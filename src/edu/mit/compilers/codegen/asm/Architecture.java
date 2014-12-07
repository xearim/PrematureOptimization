package edu.mit.compilers.codegen.asm;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.common.variableordering.ExpressionOrdering;
import edu.mit.compilers.common.variableordering.LeftAssociative;

public class Architecture {
    public final static String MAIN_METHOD_NAME = "main";

    public static final int WORD_SIZE = 8;
    public static final long BYTES_PER_ENTRY = 8;
    
    private static final Variable MAIN_BASE_POINTER_ERROR_VAR = Variable.forCompiler("$mainbaseptr");   		
    
    public static final Scope ERROR_VARIABLES = constructErrors();
    public static final VariableReference MAIN_BASE_POINTER_ERROR_VARIABLE =
    		new VariableReference(MAIN_BASE_POINTER_ERROR_VAR, ERROR_VARIABLES);
    
    
    public static final ExpressionOrdering EXPRESSION_ORDERING = new LeftAssociative();
    
    public static boolean CONTAINS_ARRAYS = false;
    
    public static final long ARRAY_INIT_SIZE = 2 * Architecture.BYTES_PER_ENTRY;
	public static final long LOOP_VAR_SIZE = 1;
    
    private static Scope constructErrors() {
        return new Scope(ImmutableList.of(new FieldDescriptor(
                Variable.forCompiler("$mainbaseptr"),
                BaseType.INTEGER,
                new LocationDescriptor("unknown", -1, -1))));
    }


}
