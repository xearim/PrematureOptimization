package edu.mit.compilers.codegen.asm;

public class Label implements Value {
    public static enum LabelType {
    	// I dont believe that we need to have global variables handled separately from any other variable
        GLOBAL("g"), // A global variable.
        METHOD("m"), // The start of a method.
        CONTROL_FLOW("cf"); // An instruction inside a method.

        private final String prefix;

        private LabelType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    private final LabelType type;
    private final String name;

    public Label(LabelType type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String inAttSyntax() {
        return "." + type.getPrefix() + "_" + name;
    }
}
