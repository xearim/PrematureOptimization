package edu.mit.compilers.codegen.asm;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.common.Variable;

public class Architecture {
    public final static String MAIN_METHOD_NAME = "main";

    public static final int WORD_SIZE = 8;
    public static final long BYTES_PER_ENTRY = 8;
    
    public static final Label MAIN_BASE_POINTER_ERROR_VARIABLE =
            new Label(LabelType.ERROR, Variable.forCompiler("mainbaseptr"));
    
    public static final Scope ERROR_VARIABLES = constructErrors();
    
	public static final long FOR_LOOP_VAR_SIZE = 0;
	public static final long WHILE_LOOP_VAR_SIZE = 0;
    
    private static Scope constructErrors() {
        return new Scope(ImmutableList.of(new FieldDescriptor(
                Variable.forCompiler("mainbaseptr"),
                BaseType.INTEGER,
                new LocationDescriptor("unknown", -1, -1))));
    }


}
