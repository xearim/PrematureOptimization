package edu.mit.compilers.codegen.asm;

public class Label implements Value {
    public static enum LabelType {
        // A global variable.
        GLOBAL(".g_"),
        // The start of a method.  Has no prefix, for easy compatibility with callouts.
        METHOD(""),
        // An instruction inside a method.
        CONTROL_FLOW(".cf_");

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
        return type.getPrefix() + name;
    }
}
