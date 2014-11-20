package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;

public class AvailabilitySpec implements AnalysisSpec<Subexpression> {
    /**
     * Map each node to its GEN set of Subexpressions, filtering out Expressions
     * that contain MethodCalls.
     *
     * <p>This function assignments that the StatementDataFlowNodes all have an expression.
     */
    @Override
    public Multimap<DataFlowNode, Subexpression> getGenSets(
            Set<StatementDataFlowNode> statementNodes) {
        ImmutableMultimap.Builder<DataFlowNode,Subexpression> builder = ImmutableMultimap.builder();

        for (StatementDataFlowNode node : statementNodes) {
            // Design Decision: don't recurse into method calls
            NativeExpression ne = node.getExpression().get();
            if (containsMethodCall(ne)) {
                /*
                 * "b+foo()" can return different values on different calls,
                 * so we don't want to claim that "b+foo()" is available.
                 */
                continue;
            }

            builder.put(node, new Subexpression(ne, node.getScope()));
        }

        return builder.build();
    }

    @Override
    public Multimap<DataFlowNode, Subexpression> getKillSets(
            Set<StatementDataFlowNode> statementNodes) {
        ImmutableMultimap.Builder<DataFlowNode, Subexpression> builder = ImmutableMultimap.builder();

        Multimap<StatementDataFlowNode,ScopedVariable> victimVariables = getPotentiallyChangedVariables(statementNodes);
        Multimap<ScopedVariable,Subexpression> expressionsContaining = getExpressionsContaining(statementNodes);
        for (StatementDataFlowNode node : statementNodes) {
            for (ScopedVariable victimVariable : victimVariables.get(node)) {
                // If a subexpression contains a changed variable, it must be killed.
                for (Subexpression victim : expressionsContaining.get(victimVariable)) {
                    builder.put(node,victim);
                }
            }
        }

        return builder.build();
    }

    @Override
    public Set<Subexpression> getOriginalOutSet(Set<StatementDataFlowNode> nodes) {
        ImmutableSet.Builder<Subexpression> builder = ImmutableSet.builder();

        for (StatementDataFlowNode node : nodes) {
            builder.add(new Subexpression(node.getExpression().get(), node.getScope()));
        }

        return builder.build();
    }

    /**
     * Returns the union of all the sets.
     */
    @Override
    public Set<Subexpression> getInSet(Iterable<Set<Subexpression>> outSets,
            Set<Subexpression> seed) {
        Set<Subexpression> newInSet = new HashSet<Subexpression>(seed);

        for (Set<Subexpression> predecessorOutSet : outSets) {
            newInSet.retainAll(predecessorOutSet);
        }

        return newInSet;
    }

    /**
     * Returns (gen U in) - kill.
     */
    @Override
    public Set<Subexpression> getOutSetFromInSet(Collection<Subexpression> gen,
            Collection<Subexpression> in, Collection<Subexpression> kill) {
        Set<Subexpression> newOutSet = new HashSet<Subexpression>(gen);

        newOutSet.addAll(in);
        newOutSet.removeAll(kill);

        return newOutSet;
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

    private static Multimap<StatementDataFlowNode,ScopedVariable> getPotentiallyChangedVariables(
            Set<StatementDataFlowNode> statementNodes) {
        ImmutableMultimap.Builder<StatementDataFlowNode,ScopedVariable> builder = ImmutableMultimap.builder();
        Set<ScopedVariable> globals = getGlobals(statementNodes);

        for (StatementDataFlowNode node : statementNodes) {
            if (node instanceof AssignmentDataFlowNode) {
                builder.put(node, ScopedVariable.getAssigned((AssignmentDataFlowNode) node));
            }

            if (containsMethodCall(node.getExpression().get())) {
                builder.putAll(node, globals);
            }
        }

        return builder.build();
    }

    private static Multimap<ScopedVariable, Subexpression> getExpressionsContaining(
            Set<StatementDataFlowNode> statementNodes) {
        ImmutableMultimap.Builder<Subexpression, ScopedVariable> variablesIn = ImmutableMultimap.builder();

        for (StatementDataFlowNode node : statementNodes) {
            Subexpression newSubexpr = new Subexpression(node.getExpression().get(), node.getScope());
            variablesIn.putAll(newSubexpr, newSubexpr.getVariables());
        }

        return variablesIn.build().inverse();
    }

    /**
     * Returns the set of all global variables. Intended for generating kill
     * sets of StatementDataFlowNodes containing MethodCalls.
     */
    private static Set<ScopedVariable> getGlobals(Set<StatementDataFlowNode> nodes) {
        Set<ScopedVariable> globalVars = new HashSet<ScopedVariable>();
        for (DataFlowNode node : nodes){
            for (ScopedVariable var :
                ScopedVariable.getVariablesOf((StatementDataFlowNode) node)){
                if (var.isGlobal()){
                    globalVars.add(var);
                }
            }
        }

        // TODO(Manny): Make immutable
        return globalVars;
    }
}
