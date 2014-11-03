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
import edu.mit.compilers.codegen.SequentialDataFlowNode;

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
        Set<Subexpression> allSubexpressions = getAllSubexpressions(allBlocks);
        Map<DataFlowNode, Set<Subexpression>> outSets =
                new HashMap<DataFlowNode, Set<Subexpression>>();
        Map<DataFlowNode, Set<Subexpression>> genSets =
                calculateGenSets(allBlocks, allSubexpressions);
        Map<DataFlowNode, Set<Subexpression>> killSets =
                calculateKillSets(allBlocks, allSubexpressions);
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

            // OUT[n] = union( GEN[n], IN[n] - KILL[n] )
            newOut = new HashSet<Subexpression>(this.inSets.get(block));
            newOut.removeAll(killSets.get(block));
            newOut.addAll(genSets.get(block));

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
     * Produces a mapping from DataFlowNodes to a list of subexpressions that
     * they generate. The types of DataFlowNodes that generate subexpressions
     * are Assignments, Compare, MethodCalls and Return Statements.
     * 
     * Assignments generate whatever is evaluated on the right.
     * 
     * Compares generate their left and right arguments.
     * 
     * MethodCalls generate their parameters.
     * 
     * ReturnStatements generate their return values.
     */
    private static Map<DataFlowNode, Set<Subexpression>> calculateGenSets(
            Set<DataFlowNode> blocks, Set<Subexpression> allSubexpressions) {

        Map<DataFlowNode, Set<Subexpression>> genSets =
                new HashMap<DataFlowNode, Set<Subexpression>>();

        for (DataFlowNode block : blocks) {
            genSets.put(block, new HashSet<Subexpression>());

            if (block instanceof AssignmentDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: AssignmentDataFlowNode path unimplemented.");
            } else if (block instanceof CompareDataFlowNode) {
                // Add leftArg
                conditionalAdd(((CompareDataFlowNode) block).getLeftArg(),
                        ((CompareDataFlowNode) block).getScope(), genSets.get(block),
                        allSubexpressions);

                // Add rightArg
                conditionalAdd(((CompareDataFlowNode) block).getRightArg(),
                        ((CompareDataFlowNode) block).getScope(), genSets.get(block),
                        allSubexpressions);

            } else if (block instanceof MethodCallDataFlowNode) {
                // Add each parameter to the method call
                for (GeneralExpression ge : ((MethodCallDataFlowNode) block).getMethodCall().getParameterValues()) {
                    conditionalAdd(ge, ((MethodCallDataFlowNode) block).getScope(),
                            genSets.get(block), allSubexpressions);
                }

            } else if (block instanceof ReturnStatementDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: ReturnStatementDataFlowNode path unimplemented.");
            } else {
                /*
                 * If not any of these nodes, then it doesn't have any
                 * expressions. The GEN set is empty for this block.
                 */
                genSets.put(block, ImmutableSet.<Subexpression>of());
            }
        }

        // convert to immutable sets
        for (DataFlowNode block : genSets.keySet()) {
            genSets.put(block,
                    ImmutableSet.<Subexpression>copyOf(genSets.get(block)));
        }
        return genSets;
    }

    /**
     * Produces a mapping from DataFlowNodes to a list of subexpressions that
     * they kill. The types of DataFlowNodes that makes subexpressions
     * unavailable are Assignments and MethodCalls.
     * 
     * Assignments kill all subexpressions that rely on the variable being
     * assigned.
     * 
     * MethodCalls kill all subexpressions that rely on global variables.
     * TODO(Manny): Make methods only kill globals that they change.
     */
    // TODO(Manny): Should this be calculated separately, or with the GEN sets?
    private static Map<DataFlowNode, Set<Subexpression>> calculateKillSets(
            Set<DataFlowNode> blocks, Set<Subexpression> allSubexpressions) {

        Map<DataFlowNode, Set<Subexpression>> killSets =
                new HashMap<DataFlowNode, Set<Subexpression>>();

        for (DataFlowNode block : blocks) {
            if (block instanceof AssignmentDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: AssignmentDataFlowNode path unimplemented.");
            } else if (block instanceof MethodCallDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: MethodCallDataFlowNode path unimplemented.");
            } else if (block instanceof ReturnStatementDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: ReturnStatementDataFlowNode path unimplemented.");
            } else {
                /*
                 * If not any of these nodes, then it doesn't have any
                 * expressions. The KILL set is empty for this block.
                 */
                killSets.put(block, ImmutableSet.<Subexpression>of());
            }
        }

        // convert to immutable sets
        for (DataFlowNode block : killSets.keySet()) {
            killSets.put(block,
                    ImmutableSet.<Subexpression>copyOf(killSets.get(block)));
        }
        return killSets;
    }

    /**
     * Returns shallow copy. The intention is for this to be modifiable but to
     * not mess up the original keyset.
     */
    private Set<DataFlowNode> allBasicBlocks() {
        return new HashSet<DataFlowNode>(this.inSets.keySet());
    }

    /**
     * Goes through all DataFlowNodes and gets their subexpressions.
     */
    private static ImmutableSet<Subexpression> getAllSubexpressions(Set<DataFlowNode> blocks) {
        Set<Subexpression> subexpressions = new HashSet<Subexpression>();

        for (DataFlowNode block : blocks) {
            if (block instanceof AssignmentDataFlowNode) {
                // Add NativeExpression on the right hand side
                conditionalAdd(((AssignmentDataFlowNode) block).getAssignment().getExpression(),
                        ((AssignmentDataFlowNode) block).getScope(), subexpressions);
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: AssignmentDataFlowNode path unimplemented.");
                // TODO(Manny): figure out what happens with += and -=

            } else if (block instanceof CompareDataFlowNode) {
                // Add leftArg
                conditionalAdd(((CompareDataFlowNode) block).getLeftArg(),
                        ((CompareDataFlowNode) block).getScope(), subexpressions);

                // Add rightArg
                conditionalAdd(((CompareDataFlowNode) block).getRightArg(),
                        ((CompareDataFlowNode) block).getScope(), subexpressions);

            } else if (block instanceof MethodCallDataFlowNode) {
                // Add each parameter to the method call
                for (GeneralExpression ge : ((MethodCallDataFlowNode) block).getMethodCall().getParameterValues()) {
                    conditionalAdd(ge, ((MethodCallDataFlowNode) block).getScope(), subexpressions);
                }

            } else if (block instanceof ReturnStatementDataFlowNode) {
                // Add the expression if the return statement has one
                if (((ReturnStatementDataFlowNode) block).getReturnStatement().getValue().isPresent()) {
                    conditionalAdd(
                            ((ReturnStatementDataFlowNode) block).getReturnStatement().getValue().get(),
                            ((ReturnStatementDataFlowNode) block).getScope(),
                            subexpressions);
                }

            }
            // The other nodes don't produce any subexpressions
        }

        return ImmutableSet.<Subexpression>copyOf(subexpressions);
    }

    /**
     * Adds the potential subexpression to the list only if it is complex
     * enough and has no method calls.
     */
    private static void conditionalAdd(GeneralExpression ge, Scope scope,
            Collection<Subexpression> collection) {
        if (isComplexEnough(ge) && !(containsMethodCall(ge))) {
            collection.add(new Subexpression((NativeExpression)ge,scope));
        }
    }
    
    /**
     * Adds the potential subexpression to the list only if it is complex
     * enough and has no method calls. Asserts that the subexpression is in
     * allExpressions.
     *
     * Used when calculating gen and kill sets to make sure that the
     * subexpression is in the calculated set of all subexpressions (E).
     */
    private static void conditionalAdd(GeneralExpression ge, Scope scope,
            Collection<Subexpression> collection, Set<Subexpression> allExpressions) {
        if (isComplexEnough(ge) && !(containsMethodCall(ge))) {
            Subexpression toBeAdded = new Subexpression((NativeExpression) ge, scope);
            checkState(allExpressions.contains(toBeAdded),"getAllSubexpressions didn't generate all expressions.");
            collection.add(toBeAdded);
        }
        
    }

    /**
     * Determines if a NativeExpression is complex enough to be worth saving.
     * Does not check for MethodCalls inside of GeneralExpression.
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

    /**
     * Returns all subexpressions available at that node.
     */
    public Set<Subexpression> availableSubexpressionsAt(DataFlowNode node) {
        return this.inSets.get(node);
    }

    /**
     * Return whether an expression is available at a DataFlowNode.
     */
    public boolean isAvailable(GeneralExpression expr, SequentialDataFlowNode node) {
        if (!(expr instanceof NativeExpression)) {
            // Only NativeExpressions are ever available.
            return false;
        }
        Subexpression scopedExpr = new Subexpression((NativeExpression) expr, node.getScope());

        return inSets.get(node).contains(scopedExpr);
    }
}
