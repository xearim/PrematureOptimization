package edu.mit.compilers.codegen;

import java.io.PrintStream;

import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.ast.StringLiteral;
import edu.mit.compilers.codegen.asm.Architecture;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;

public class AssemblyWriter {
    private final static String METHODS_COMMENT = "// All methods.";
    private final static String STRINGS_COMMENT = "\n// All strings.";
    private final static String GLOBALS_COMMENT = "\n// All globals.";
    private final static String DATA_DECLARATION = "\n.data";
    private final static String GLOBALS_DECLARATION = "\n.bss";
    private final static int GLOBAL_INITIAL_VALUE = 1;

    public static void writeAttAssembly(Program program, PrintStream outputStream) {
        // Get one graph per method
        outputStream.println(METHODS_COMMENT);
        for (Method method : program.getMethods()) {
            methodPrinter(method,outputStream);
        }


        // Get all String Literals
        outputStream.println(STRINGS_COMMENT);
        outputStream.println(DATA_DECLARATION);
        for (StringLiteral sl : StringLiteral.getStringLiterals()) {
            stringPrinter(sl, outputStream);
        }


        // Get all globals
        outputStream.println(GLOBALS_COMMENT);
        outputStream.println(GLOBALS_DECLARATION);
        for (FieldDescriptor global : program.getGlobals().getVariables()){
            globalPrinter(global, outputStream);
        }
        for (FieldDescriptor errorVar : Architecture.ERROR_VARIABLES.getVariables()){
        	errorVarPrinter(errorVar, outputStream);
        }
    }

    private static void methodPrinter(Method method, PrintStream outputStream) {
        if (method.isMain()) {
            outputStream.println("\t.globl main");
        }
        outputStream.println(method.getName() + ":");
        MethodBlockPrinter methodGraph = new MethodBlockPrinter(method);
        methodGraph.printStream(outputStream);
        outputStream.println();
    }

    private static void stringPrinter(StringLiteral sl, PrintStream outputStream) {
        outputStream.println(String.format("\t%s: .asciz %s",
                new Label(LabelType.STRING, sl.getID()).labelText(),
                sl.getName()));
    }

    private static void globalPrinter(FieldDescriptor fd, PrintStream outputStream) {
        outputStream.println(String.format("\t.comm %s, %d*%d",
                new Label(LabelType.GLOBAL, fd.getName()).labelText(),
                Architecture.WORD_SIZE,
                (fd.getLength().isPresent())
                        ? fd.getLength().get().get64BitValue()
                        : GLOBAL_INITIAL_VALUE));
    }
    
    private static void errorVarPrinter(FieldDescriptor fd, PrintStream outputStream) {
        outputStream.println(String.format("\t%s: .quad %d",
                new Label(LabelType.ERROR, fd.getName()).inAttSyntax(),
                (fd.getLength().isPresent())
                ? fd.getLength().get().get64BitValue()
                        : GLOBAL_INITIAL_VALUE));
    }
}
