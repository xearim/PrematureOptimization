package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.CompareDataFlowNode;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.MethodCallDataFlowNode;
import edu.mit.compilers.codegen.ReturnStatementDataFlowNode;
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
        /*
         * Set up variables for algorithm:
         * - IN and OUT
         * - GEN and KILL
         * - changed
         * - all subexpressions (E)
         */
        createInSets(entryBlock);
        Set<DataFlowNode> allBlocks = ImmutableSet.copyOf(this.inSets.keySet());

        Set<Subexpression> allSubexpressions = null;
        Map<DataFlowNode, Set<Subexpression>> genSets = null;
        Map<DataFlowNode, Set<Subexpression>> killSets = null;
        calculateConstants(allBlocks, allSubexpressions,genSets,killSets);

        Map<DataFlowNode, Set<Subexpression>> outSets =
                new HashMap<DataFlowNode, Set<Subexpression>>();
        Set<DataFlowNode> changed;

        // Run algorithm
        // for all nodes n in N
        for (DataFlowNode block : inSets.keySet()) {
            // OUT[n] = E;
            outSets.put(block, new HashSet<Subexpression>(allSubexpressions));
        }
        // IN[Entry] = emptySet;
        // All IN sets are already initialized to the emptySet.

        // OUT[Entry] = GEN[Entry];
        outSets.put(entryBlock, new HashSet<Subexpression>(
                genSets.get(entryBlock)));

        // Changed = N - {Entry};
        changed = allBasicBlocks();
        checkState(changed.remove(entryBlock),
                "entryBlock is not in set of all blocks.");

        // While (Changed != emptyset)
        while (!changed.isEmpty()) {
            DataFlowNode block;
            Set<Subexpression> newOut;

            // Choose a node n in Changed
            block = changed.iterator().next();
            // Changed = N - {n}
            changed.remove(block);

            // IN[n] = E
            this.inSets.put(block, new HashSet<Subexpression>(allSubexpressions));
            // for all nodes p in predecessors(n)
            for (DataFlowNode predecessor : block.getPredecessors()) {
                // IN[n] = intersection( IN[n], OUT[p] )
                this.inSets.get(block).retainAll(outSets.get(predecessor));
            }

            // OUT[n] = union(GEN[n], IN[n]) - KILL[n]
            newOut = new HashSet<Subexpression>(this.inSets.get(block));
            newOut.addAll(genSets.get(block));
            newOut.removeAll(killSets.get(block));

            // If (OUT[n] changed)
            if (!newOut.equals(outSets.get(block))) {
                // Store new OUT[n], note: not explicitly in algorithm
                outSets.put(block, newOut);

                /*
                 * for all nodes s in successors(n)
                 *   Changed = union( Changed, {s} )
                 */
                changed.addAll(block.getSuccessors());
            }
        }
    }

    /**
     * Creates IN sets for all blocks. Does not initialize any values. Makes
     * sure that each block is included only once.
     */
    private void createInSets(DataFlowNode entryBlock) {
        Set<DataFlowNode> visited = new HashSet<DataFlowNode>();
        Set<DataFlowNode> toBeVisited = new HashSet<DataFlowNode>();
        toBeVisited.add(entryBlock);
        this.inSets = new HashMap<DataFlowNode, Set<Subexpression>>();

        while (!toBeVisited.isEmpty()) {
            DataFlowNode block = toBeVisited.iterator().next();
            toBeVisited.remove(block);

            for (DataFlowNode newBlock : block.getSuccessors()) {
                // In case there are multiple paths to that block
                if (!visited.contains(newBlock)) {
                    visited.add(newBlock);
                    this.inSets.put(newBlock, new HashSet<Subexpression>());
                }
            }
        }
    }

    /**
     * Calculates allSubexpressions (E), the GEN sets, and the KILL sets, and
     * sets them to the appropriate variable. The only one of these parameters
     * that needs to be initialized is allNodes.
     *
     * GEN sets are generated by Assignments, Compares, MethodCalls, and
     * ReturnStatements.
     *
     * KILL sets are generated by Assignments and MethodCalls.
     *
     * @param allNodes - all the DataFlowNodes of interest in this program
     */
    private static void calculateConstants(Set<DataFlowNode> allNodes,
            Set<Subexpression> allSubexpressions,
            Map<DataFlowNode,Set<Subexpression>> genSets,
            Map<DataFlowNode, Set<Subexpression>> killSets) {

        // Initialize the parameter sets
        allSubexpressions = new HashSet<Subexpression>();
        genSets = new HashMap<DataFlowNode, Set<Subexpression>>();
        killSets = new HashMap<DataFlowNode, Set<Subexpression>>();

        // Set up sets necessary to determine killSets
        Map<DataFlowNode, Set<Variable>> varSets = new HashMap<DataFlowNode, Set<Variable>>();
        Map<Variable, Set<Subexpression>> varKillSets = new HashMap<Variable, Set<Subexpression>>();
        Set<Variable> globals = getGlobals(allNodes);

        for (DataFlowNode block : allNodes) {
            genSets.put(block, new HashSet<Subexpression>());
            varSets.put(block, new HashSet<Variable>());

            if (block instanceof AssignmentDataFlowNode) {
                addAssignmentData((AssignmentDataFlowNode) block,
                        allSubexpressions, genSets, varSets, varKillSets,
                        globals);

            } else if (block instanceof CompareDataFlowNode) {
                addCompareData((CompareDataFlowNode) block,allSubexpressions,genSets);

            } else if (block instanceof MethodCallDataFlowNode) {
                addMethodCallData((MethodCallDataFlowNode) block,
                        allSubexpressions, genSets, varSets, varKillSets,
                        globals);

            } else if (block instanceof ReturnStatementDataFlowNode) {
                addReturnStatementData((ReturnStatementDataFlowNode) block,
                        allSubexpressions, genSets, varSets, varKillSets,
                        globals);

            }
            // The other nodes don't produce any subexpressions
        }

        for (DataFlowNode node : allNodes) {
            for (Variable var : varSets.get(node)) {
                for (Subexpression subexpr : varKillSets.get(var)) {
                    killSets.get(node).add(subexpr);
                }
            }
        }

        // TODO(Manny): convert all of these to immutables
    }

    /**
     * Returns the set of all global variables. Intended for generating kill
     * sets of DataFlowNodes containing MethodCalls.
     */
    private static Set<Variable> getGlobals(Set<DataFlowNode> allNodes) {
        throw new UnsupportedOperationException("AC#getGlobals unimplemented.");
    }


    /**
     * Takes cares of adding information for an AssignmentDataFlowNode to
     * allSubexpressions, genSets and killsets.
     */
    private static void addAssignmentData(AssignmentDataFlowNode assignmentNode,
            Set<Subexpression> allSubexpressions,
            Map<DataFlowNode,Set<Subexpression>> genSets,
            Map<DataFlowNode, Set<Variable>> varSets,
            Map<Variable, Set<Subexpression>> varKillSets,
            Set<Variable> globals) {

        NativeExpression ne = assignmentNode.getAssignment().getExpression();
        if (isComplexEnough(ne)) {
            if (containsMethodCall(ne)) {
                // Kill subexpressions with globals
                varSets.get(assignmentNode).addAll(globals);

            } else {
                // Gen the subexpression
                Subexpression newSubexpr = new Subexpression(ne,
                        assignmentNode.getScope());

                allSubexpressions.add(newSubexpr);
                genSets.get(assignmentNode).add(newSubexpr);

                // Note what kills the subexpression
                for (Variable var : newSubexpr.getVariables()) {
                    if (!varKillSets.containsKey(var)) {
                        varKillSets.put(var, new HashSet<Subexpression>());
                    }
                }
            }

            // Kill subexpressions with assigned variable
            varSets.get(assignmentNode).add(Variable.getAssigned(assignmentNode));
        }

        // TODO(Manny): figure out what happens with += and -=
    }

    /**
     * Takes care of adding information for a CompareDataFlowNode to
     * allSubexpressions, and genSets.
     *
     * TODO(Manny): fix this when jason done refactoring.
     */
    private static void addCompareData(CompareDataFlowNode compareNode,
            Set<Subexpression> allSubexpressions,
            Map<DataFlowNode, Set<Subexpression>> genSets) {

//        for (GeneralExpression ge : compareNode.getExpressions()) {
//            if (worthTracking(ge)) {
//                Subexpression newSubexpr =
//                        new Subexpression((NativeExpression) ge,
//                                compareNode.getScope());
//
//                allSubexpressions.add(newSubexpr);
//                genSets.get(compareNode).add(newSubexpr);
//            }
//        }
        throw new UnsupportedOperationException("AC#addCompareData unimplemented.");
    }

    /**
     * Takes care of adding information for a MethodCallDataFlowNode to
     * allSubexpressions, genSets, and killSets.
     *
     * For now, due to our simplified optimization, MCDFNs just kill all
     * globals.
     */
    private static void addMethodCallData(MethodCallDataFlowNode mcNode,
            Set<Subexpression> allSubexpressions,
            Map<DataFlowNode, Set<Subexpression>> genSets,
            Map<DataFlowNode, Set<Variable>> varSets,
            Map<Variable, Set<Subexpression>> varKillSets,
            Set<Variable> globals) {

        // Kill all globals
        varSets.get(mcNode).addAll(globals);
    }

    /**
     * Takes care of adding information for a ReturnStatementDataFlowNode to
     * allSubexpressions and genSets.
     */
    private static void addReturnStatementData(ReturnStatementDataFlowNode returnNode,
            Set<Subexpression> allSubexpressions,
            Map<DataFlowNode, Set<Subexpression>> genSets,
            Map<DataFlowNode, Set<Variable>> varSets,
            Map<Variable, Set<Subexpression>> varKillSets,
            Set<Variable> globals) {

        /*
         * Contains methodcall:
         * -kill globals
         *
         * doesn't:
         * -gen subexpression
         */
        if (returnNode.getReturnStatement().getValue().isPresent()) {
            NativeExpression ne = returnNode.getReturnStatement().getValue().get();
            if (isComplexEnough(ne)) {
                if (containsMethodCall(ne)) {
                    // Kill subexpressions with globals
                    varSets.get(returnNode).addAll(globals);

                } else {
                    // Gen the subexpression
                    Subexpression newSubexpr =
                            new Subexpression(
                                    ne,
                                    returnNode.getScope());

                    allSubexpressions.add(newSubexpr);
                    genSets.get(returnNode).add(newSubexpr);

                    // Note what kills the subexpression
                    for (Variable var : newSubexpr.getVariables()) {
                        if (!varKillSets.containsKey(var)) {
                            varKillSets.put(var, new HashSet<Subexpression>());
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns shallow copy. The intention is for this to be modifiable but to
     * not mess up the original keyset.
     */
    private Set<DataFlowNode> allBasicBlocks() {
        return new HashSet<DataFlowNode>(this.inSets.keySet());
    }


//    /**
//     * Used to determine if a GeneralExpression is worth tracking. Every
//     * GeneralExpression that passes this check is an instance of
//     * NativeExpression.
//     *
//     * Examples of cases not worth tracking:
//     *
//     * "a = c;"
//     * There's no computation to save time on here.
//     *
//     * "a = 3 + foo();"
//     * foo() is a MethodCall. For now we are assuming that we cannot predict
//     * MethodCall behavior.
//     */
//    private static boolean worthTracking(GeneralExpression ge) {
//        return isComplexEnough(ge) && !(containsMethodCall(ge));
//    }



    /**
     * Determines if a NativeExpression is complex enough to be worth tracking.
     * Does not check for MethodCalls inside of GeneralExpression.
     * Any GeneralExpression that passes this check may be considered a
     * NativeExpression.
     */
    private static boolean isComplexEnough(GeneralExpression ge) {
        return (ge instanceof BinaryOperation)
                || (ge instanceof MethodCall)
                || (ge instanceof TernaryOperation)
                || (ge instanceof UnaryOperation);
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

        return inSets.get(node).contains(scopedExpr);
    }
}
