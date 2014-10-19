package edu.mit.compilers.codegen.asm;

import static com.google.common.base.Preconditions.checkState;

public class Label implements Value {
    public static enum LabelType {
        // A global variable.
        GLOBAL("$.g_"),
        // The start of a method.  Has no prefix, for easy compatibility with callouts.
        METHOD(""),
        // An instruction inside a method.
        CONTROL_FLOW(".cf_"),
        // A string element
        STRING("$.s_");

        private final String prefix;
        private static long globalID = 0, controlFlowID = 0, stringID = 0;

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
