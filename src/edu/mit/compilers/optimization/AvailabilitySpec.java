package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Node;

public class AvailabilitySpec implements AnalysisSpec<ScopedExpression> {
    /**
     * Map each node to its GEN set of Subexpressions, filtering out Expressions
     * that contain MethodCalls.
     *
     * <p>Requires that the statements all have an expression.
     */
    @Override
    public Multimap<Node<ScopedStatement>, ScopedExpression> getGenSets(
            Set<Node<ScopedStatement>> statementNodes) {
        ImmutableMultimap.Builder<Node<ScopedStatement>,ScopedExpression> builder = ImmutableMultimap.builder();

        for (Node<ScopedStatement> node : statementNodes) {
            // Design Decision: don't recurse into method calls
            NativeExpression ne = node.value().getStatement().getExpression();
            if (containsMethodCall(ne)) {
                /*
                 * "b+foo()" can return different values on different calls,
                 * so we don't want to claim that "b+foo()" is available.
                 */
                continue;
            }

            builder.put(node, new ScopedExpression(ne, node.value().getScope()));
        }

        return builder.build();
    }

    @Override
    public Multimap<Node<ScopedStatement>, ScopedExpression> getKillSets(
            Set<Node<ScopedStatement>> statementNodes) {
        ImmutableMultimap.Builder<Node<ScopedStatement>, ScopedExpression> builder = ImmutableMultimap.builder();

        Multimap<Node<ScopedStatement>,ScopedVariable> victimVariables = getPotentiallyChangedVariables(statementNodes);
        Multimap<ScopedVariable,ScopedExpression> expressionsContaining = getExpressionsContaining(statementNodes);
        for (Node<ScopedStatement> node : statementNodes) {
            for (ScopedVariable victimVariable : victimVariables.get(node)) {
                // If a subexpression contains a changed variable, it must be killed.
                for (ScopedExpression victim : expressionsContaining.get(victimVariable)) {
                    builder.put(node,victim);
                }
            }
        }

        return builder.build();
    }

    /** Returns the set of all expressions */
    @Override
    public Set<ScopedExpression> getInfinum(Set<Node<ScopedStatement>> nodes) {
        ImmutableSet.Builder<ScopedExpression> builder = ImmutableSet.builder();

        for (Node<ScopedStatement> node : nodes) {
            builder.add(new ScopedExpression(node.value().getStatement().getExpression(), node.value().getScope()));
        }

        return builder.build();
    }

    /**
     * Returns the union of all the sets.
     */
    @Override
    public Set<ScopedExpression> getInSetFromPredecessors(Iterable<Collection<ScopedExpression>> outSets,
            Collection<ScopedExpression> seed) {
        Set<ScopedExpression> newInSet = new HashSet<ScopedExpression>(seed);

        for (Collection<ScopedExpression> predecessorOutSet : outSets) {
            newInSet.retainAll(predecessorOutSet);
        }

        return newInSet;
    }

    /**
     * Returns (gen U in) - kill.
     */
    @Override
    public Set<ScopedExpression> getOutSetFromInSet(Collection<ScopedExpression> gen,
            Collection<ScopedExpression> in, Collection<ScopedExpression> kill) {
        Set<ScopedExpression> newOutSet = new HashSet<ScopedExpression>(gen);

        newOutSet.addAll(in);
        newOutSet.removeAll(kill);

        return newOutSet;
    }

    /** Filters all the nodes that do not have an expression. */
    @Override 
    public Set<Node<ScopedStatement>> filterNodes(Iterable<Node<ScopedStatement>> nodes) {
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

    /**
     * Returns true is there is a MethodCall in any part of the
     * GeneralExpression.
     */
    private static boolean containsMethodCall(GeneralExpression ge) {
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
     * Maps StatementNode<ScopedStatement>s to variables they may change during
     * execution.
     */
    private static Multimap<Node<ScopedStatement>, ScopedVariable> getPotentiallyChangedVariables(
            Set<Node<ScopedStatement>> statementNodes) {
        ImmutableMultimap.Builder<Node<ScopedStatement>, ScopedVariable> builder = ImmutableMultimap.builder();
        Set<ScopedVariable> globals = getGlobals(statementNodes);

        for (Node<ScopedStatement> node : statementNodes) {
            StaticStatement statement = node.value().getStatement();
            if (statement instanceof Assignment) {
                builder.put(node, ScopedVariable.getAssigned(
                        (Assignment) node.value().getStatement(), node.value().getScope()));
            }

            if (containsMethodCall(statement.getExpression())) {
                builder.putAll(node, globals);
            }
        }

        return builder.build();
    }

    /**
     * Maps variables to expressions that contain them.
     */
    private static Multimap<ScopedVariable, ScopedExpression> getExpressionsContaining(
            Set<Node<ScopedStatement>> statementNodes) {
        ImmutableMultimap.Builder<ScopedExpression, ScopedVariable> variablesIn = ImmutableMultimap.builder();

        for (Node<ScopedStatement> node : statementNodes) {
            ScopedExpression newSubexpr = new ScopedExpression(node.value().getStatement().getExpression(), node.value().getScope());
            variablesIn.putAll(newSubexpr, newSubexpr.getVariables());
        }

        return variablesIn.build().inverse();
    }

    /**
     * Returns the set of all global variables. Intended for generating kill
     * sets of StatementNode<ScopedStatement>s containing MethodCalls.
     */
    private static Set<ScopedVariable> getGlobals(Set<Node<ScopedStatement>> nodes) {
        Set<ScopedVariable> globalVars = new HashSet<ScopedVariable>();
        for (Node<ScopedStatement> node : nodes){
            for (ScopedVariable var :
                ScopedVariable.getVariablesOf(node.value())){
                if (var.isGlobal()){
                    globalVars.add(var);
                }
            }
        }

        return ImmutableSet.copyOf(globalVars);
    }
}
