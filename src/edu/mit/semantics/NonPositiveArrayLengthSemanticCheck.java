package edu.mit.semantics;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ast.*;
import edu.mit.semantics.errors.NonPositiveArrayLengthSemanticError;
import edu.mit.semantics.errors.SemanticError;

public class NonPositiveArrayLengthSemanticCheck implements SemanticCheck {
    private final static String HEX_START = "0x";
    private final static String NEGATIVE_START = "-";
    private final static String ZERO = "(0)+";
    private final static String NegativeHexStart = "89ABCDEF";
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

                /*
                 * Examples of invalid int literals for array indices:
                 * "-3"
                 * "0x8000
                 * "0x00"
                 * "000"
                 */
                if (length.startsWith(NEGATIVE_START) ||
                        isNegativeHex(length) ||
                        length.matches(HEX_START + ZERO) ||
                        length.matches(ZERO)) {
                    errors.add(new NonPositiveArrayLengthSemanticError(
                            field.getLocationDescriptor(), this.prog.getName(),
                            field.getName(), length));
                }
            }
        }
    }
    
    /**
     * Returns true if length in the range 0x8000 - 0xFFFF
     */
    private boolean isNegativeHex(String length) {
        return length.startsWith(HEX_START) &&
                (NegativeHexStart.indexOf(
                        length.charAt(HEX_START.length())) != -1);
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
