package edu.mit.compilers.semantics;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ast.*;
import edu.mit.compilers.semantics.errors.NonPositiveArrayLengthSemanticError;
import edu.mit.compilers.semantics.errors.SemanticError;

public class NonPositiveArrayLengthSemanticCheck implements SemanticCheck {
    private final static String HEX_START = "0x";
    private final static String NEG_HEX_DIGIT = "[89ABCDEFabcdef]";
    private final static String HEX_DIGIT = "[0123456789ABCDEFabcdef]";
    private final static String NEGATIVE_DECIMAL_START = "-";
    private final static String ZERO = "(0)+";
    Program prog;
    List<SemanticError> errors = new ArrayList<SemanticError>();

    public NonPositiveArrayLengthSemanticCheck(Program prog) {
        this.prog = prog;
    }

    /**
     * Check all scopes, global, parameter, local
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SemanticError> doCheck() {
        // Global
        checkScope(this.prog.getGlobals());
        // Check methods for parameters and locals
        checkMethods((Iterable<Method>) this.prog.getMethods().getChildren());

        return errors;
    }

    private void checkScope(Scope scope) {
        List<FieldDescriptor> fields = scope.getVariables();

        for (FieldDescriptor field : fields) {
            // Check if field is array
            if (field.getType() != BaseType.VOID && field.getLength().isPresent()) {
                /*
                 *  TODO(Manny): Confirm that this is how to check for
                 *  non-positive numbers, specifically do negative hex lits
                 *  start with - 
                 */ 
                String length = field.getLength().get().getName();

                if (isNonPositiveIntLiteral(length)) {
                    errors.add(new NonPositiveArrayLengthSemanticError(
                            field.getLocationDescriptor(), this.prog.getName(),
                            field.getName(), length));
                }
            }
        }
    }

    /**
     * Doesn't do boundary checking
     */
    public static boolean isNonPositiveIntLiteral(String i) {
        // TODO(Manny) implement
        return i.startsWith(NEGATIVE_DECIMAL_START) ||  // ie. -7
                i.matches(HEX_START + NEG_HEX_DIGIT + HEX_DIGIT+"{15}") || // ie. 0xFEDCBA9876543210
                i.matches(HEX_START+ZERO) || // ie. 0x000
                i.matches(ZERO); // ie. 000
    }

    private void checkMethods(Iterable<Method> methods) {
        for (Method method : methods) {
            // Parameters
            checkScope(method.getParameters());
            // Check blocks for locals
            checkBlock(method.getBlock());
        }
    }

    private void checkBlock(Block block) {
        // Local
        checkScope(block.getScope());

        // Check statements for sublocal fields
        Iterable<Statement> statements = block.getStatements();

        for (Statement statement : statements) {
            Iterable<Block> subBlocks = statement.getBlocks();

            for (Block subBlock: subBlocks) {
                // recurse on sub-blocks
                checkBlock(subBlock);
            }
        }
    }
}
