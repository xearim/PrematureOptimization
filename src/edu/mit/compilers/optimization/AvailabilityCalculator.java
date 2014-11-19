package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;

/**
 * Given a basic block, the AvailabilityCalculator computes all available
 * subexpressions at each block accessible from the input basic block.
 */
public class AvailabilityCalculator {
    private Map<DataFlowNode, Set<Subexpression>> inSets;

    public AvailabilityCalculator (DataFlowNode entryBlock) {
        calculateAvailability(entryBlock);
    }

    /**
     * Runs the fixed-point algorithm for available expressions when created.
     * Afterwards, can be asked what the available expressions are at each
     * basic block.
     *
     * TODO(Manny): refactor so it's not one mega function.
     */
    private void calculateAvailability(DataFlowNode entryBlock) {
        // Set up IN
        createInSets(entryBlock);
        Set<DataFlowNode> allBlocks = ImmutableSet.copyOf(this.inSets.keySet());

        Set<StatementDataFlowNode> savableNodes = getNodesWithSavableExpressions(allBlocks);
        Set<Subexpression> allSubexpressions = getAllExpressions(savableNodes);
        Multimap<DataFlowNode, Subexpression> genSets = getGenSets(savableNodes);
        Multimap<DataFlowNode, Subexpression> killSets = getKillSets(savableNodes);

        // Set up OUT
        Map<DataFlowNode, Set<Subexpression>> outSets =
                new HashMap<DataFlowNode, Set<Subexpression>>();
        Set<DataFlowNode> changed;


        // Run algorithm
        for (DataFlowNode block : inSets.keySet()) {
            outSets.put(block, new HashSet<Subexpression>(allSubexpressions));
        }

        outSets.put(entryBlock, new HashSet<Subexpression>(
                genSets.get(entryBlock)));

        changed = allBasicBlocks();
        checkState(changed.remove(entryBlock),
                "entryBlock is not in set of all blocks.");

        while (!changed.isEmpty()) {
            DataFlowNode block;
            Set<Subexpression> newOut;

            block = changed.iterator().next();
            changed.remove(block);

            this.inSets.put(block, new HashSet<Subexpression>(allSubexpressions));
            for (DataFlowNode predecessor : block.getPredecessors()) {
                this.inSets.get(block).retainAll(outSets.get(predecessor));
            }

            newOut = new HashSet<Subexpression>(this.inSets.get(block));
            newOut.addAll(genSets.get(block));
            newOut.removeAll(killSets.get(block));

            if (!newOut.equals(outSets.get(block))) {
                outSets.put(block, newOut);
                changed.addAll(block.getSuccessors());
            }
        }
    }

    /**
     * Creates IN sets for all blocks. Does not initialize any values. Makes
     * sure that each block is included only once.
     */
    private void createInSets(DataFlowNode entryBlock) {
        // Just do DFS. Use inSets as the visted set!
        inSets = new HashMap<DataFlowNode, Set<Subexpression>>();
        Deque<DataFlowNode> queue = new ArrayDeque<DataFlowNode>();

        queue.push(entryBlock);

        while (!queue.isEmpty()) {
            DataFlowNode node = queue.pop();
            if (inSets.containsKey(node)) {
                continue;
            }

            // Add a new, currently empty, set of subexpressions.
            inSets.put(node, new HashSet<Subexpression>());

            for (DataFlowNode child : node.getSuccessors()) {
                queue.push(child);
            }
            for (DataFlowNode child : node.getPredecessors()) {
                queue.push(child);
            }
        }
    }

    /*
     * This could've been beautiful - Jason - Manny
     */
    private static Set<StatementDataFlowNode> getNodesWithSavableExpressions(Iterable<DataFlowNode> allNodes) {
        ImmutableSet.Builder<StatementDataFlowNode> builder = ImmutableSet.builder();

        for (DataFlowNode node : allNodes) {
            if (!(node instanceof StatementDataFlowNode)) {
                continue;
            }

            StatementDataFlowNode statementNode = (StatementDataFlowNode) node;
            if (!(statementNode.getExpression().isPresent())) {
                continue;
            }

            builder.add(statementNode);
        }

        return builder.build();
    }

    private static Multimap<DataFlowNode, Subexpression> getKillSets(Set<StatementDataFlowNode> statementNodes) {
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
     * Map each node to its GEN set of Subexpressions, filtering out Expressions
     * that contain MethodCalls.
     *
     * <p>This function assignments that the StatementDataFlowNodes all have an expression.
     */
    private static Multimap<DataFlowNode, Subexpression> getGenSets(Set<StatementDataFlowNode> statementNodes) {
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

    /**
     * Get a set of all the subexpressions inside the nodes.
     *
     * <p>Assumes that all the StatementDataFlowNodes have a Expression.
     */
    private static Set<Subexpression> getAllExpressions(Set<StatementDataFlowNode> nodes) {
        ImmutableSet.Builder<Subexpression> builder = ImmutableSet.builder();

        for (StatementDataFlowNode node : nodes) {
            builder.add(new Subexpression(node.getExpression().get(), node.getScope()));
        }

        return builder.build();
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

    /**
     * Returns shallow copy. The intention is for this to be modifiable but to
     * not mess up the original keyset.
     */
    private Set<DataFlowNode> allBasicBlocks() {
        return new HashSet<DataFlowNode>(this.inSets.keySet());
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

    /** Returns all subexpressions available at that node. */
    public Set<Subexpression> availableSubexpressionsAt(DataFlowNode node) {
        return this.inSets.get(node);
    }


    /** Return whether an expression is available at a DataFlowNode. */
    public boolean isAvailable(GeneralExpression expr, StatementDataFlowNode node) {
        if (!(expr instanceof NativeExpression)) {
            // Only NativeExpressions are ever available.
            return false;
        }
        Subexpression scopedExpr = new Subexpression((NativeExpression) expr, node.getScope());

        // TODO Figure out why a direct contains doesnt work
        for(Subexpression ex : inSets.get(node)){
            if(ex.equals(scopedExpr)){
                return true;
            }
        }
        return false;
    }
}
