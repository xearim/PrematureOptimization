package edu.mit.compilers.optimization;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class Util {
    private Util() {}

    /**
     * Returns true is there is a MethodCall in any part of the
     * GeneralExpression.
     */
    public static boolean containsMethodCall(GeneralExpression ge) {
        if (ge instanceof BinaryOperation) {
            return containsMethodCall( ((BinaryOperation) ge).getLeftArgument())
                    || containsMethodCall( ((BinaryOperation) ge).getRightArgument());
        }  else if (ge instanceof MethodCall) {
            return true;
        } else if (ge instanceof TernaryOperation) {
            return containsMethodCall( ((TernaryOperation) ge).getCondition())
                    || containsMethodCall( ((TernaryOperation) ge).getTrueResult())
                    || containsMethodCall( ((TernaryOperation) ge).getFalseResult());
        } else if (ge instanceof UnaryOperation) {
            return containsMethodCall( ((UnaryOperation) ge).getArgument());
        } else {
            return false;
        }
    }

    /**
     * Get all the global variables.
     *
     * @param scope Any scope in the program.  (We find the global scope by climbing
     * this scopes lineage.)
     */
    public static Set<ScopedVariable> getGlobalVariables(Scope scope) {
        ImmutableSet.Builder<ScopedVariable> builder = ImmutableSet.builder();
        Scope globalScope = scope.getGlobalScope();
        for (FieldDescriptor descriptor : globalScope.getVariables()) {
            for (Location location : descriptor.getLocations()) {
                builder.add(new ScopedVariable(location.getVariable(), globalScope));
            }
        }
        return builder.build();
    }

    /**
     * Filters out nodes that don't have an expression.
     */
    public static Set<Node<ScopedStatement>> filterNodesWithoutExpressions(
            Iterable<Node<ScopedStatement>> nodes) {
        ImmutableSet.Builder<Node<ScopedStatement>> builder = ImmutableSet.builder();

        for (Node<ScopedStatement> node : nodes) {
            if (!node.hasValue()) {
                continue;
            }
            if (!node.value().getStatement().hasExpression()) {
                continue;
            }
            builder.add(node);
        }

        return builder.build();
    }
}
