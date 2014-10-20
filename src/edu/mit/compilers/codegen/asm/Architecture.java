package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.codegen.asm.Label.LabelType;

public class Architecture {
	public final static String MAIN_METHOD_NAME = "main";

	public static final int WORD_SIZE = 8;
	public static final long BYTES_PER_ENTRY = 8;
	
	public static final Label CONTROL_FALLOFF_ERROR_VARIABLE = new Label(LabelType.ERROR, "mainbaseptr");
	
}
