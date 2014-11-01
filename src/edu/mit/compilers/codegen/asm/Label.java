package edu.mit.compilers.codegen.asm;

import edu.mit.compilers.common.Variable;

public class Label implements Value {
    public static enum LabelType {
        // A global variable.
        GLOBAL(".g_"),
        // Special Globals used for error handling
        ERROR(".e_"),
        // The start of a method.  Has no prefix, for easy compatibility with callouts.
        METHOD(""),
        // An instruction inside a method.
        CONTROL_FLOW(".cf_"),
        // A string element
        STRING(".s_");

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
    
    public Label(LabelType type, Variable variable) {
        this(type, variable.generateName());
    }

    @Override
    public String inAttSyntax() {
        return type == LabelType.STRING
                ? "$" + labelText()
                : labelText();
    }
    
    public String labelText() {
        return type.getPrefix() + name;
    }
}
