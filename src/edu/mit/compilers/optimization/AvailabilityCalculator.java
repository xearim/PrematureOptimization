package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.NativeExpression;
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
        Map<DataFlowNode, Set<Subexpression>> genSets = calculateGenSets(allBlocks);
        Map<DataFlowNode, Set<Subexpression>> killSets = calculateKillSets(allBlocks);
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
        changed = copyOfBasicBlocksSet();
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
    private Map<DataFlowNode, Set<Subexpression>> calculateGenSets(
            Set<DataFlowNode> blocks) {
        Map<DataFlowNode, Set<Subexpression>> genSets =
                new HashMap<DataFlowNode, Set<Subexpression>>();

        for (DataFlowNode block : blocks) {
            if (block instanceof AssignmentDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: AssignmentDataFlowNode path unimplemented.");
            } else if (block instanceof CompareDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: CompareDataFlowNode path unimplemented.");
            } else if (block instanceof MethodCallDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: MethodCallDataFlowNode path unimplemented.");
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

        return genSets;
    }

    /**
     * Produces a mapping from DataFlowNodes to a list of subexpressions that
     * they kill. The types of DataFlowNodes that makes subexpressions
     * unavailable are Assignments, MethodCalls, and Return Statements.
     * 
     * Assignments kill all subexpressions that rely on the variable being
     * assigned.
     * 
     * MethodCalls kill all subexpressions that rely on global variables.
     * TODO(Manny): Make methods only kill globals that they change.
     * 
     * ReturnStatements kill all subexpressions of that method scope level.
     */
    // TODO(Manny): Should this be calculated separately, or with the GEN sets?
    private Map<DataFlowNode, Set<Subexpression>> calculateKillSets(
            Set<DataFlowNode> blocks) {
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

        return killSets;
    }

    /**
     * Returns shallow copy. Want this to be modifiable but to not mess up the
     * original keyset.
     */
    private Set<DataFlowNode> copyOfBasicBlocksSet() {
        return new HashSet<DataFlowNode>(this.inSets.keySet());
    }

    /**
     * Goes through all DataFlowNodes and gets their subexpressions.
     */
    private ImmutableSet<Subexpression> getAllSubexpressions(Set<DataFlowNode> blocks) {
        Set<Subexpression> subexpressions = new HashSet<Subexpression>();

        /*
         * TODO(Manny): figure out when NativeExpression is "complex-enough" to
         * be worth saving.
         */
        for (DataFlowNode block : blocks) {
            if (block instanceof AssignmentDataFlowNode) {
                // Native expression on the right hand side
                subexpressions.add(
                        new Subexpression(
                                ((AssignmentDataFlowNode) block).getAssignment().getExpression(),
                                ((AssignmentDataFlowNode) block).getScope()));
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: AssignmentDataFlowNode path unimplemented.");
                // TODO(Manny): figure out what happens with += and -=
            } else if (block instanceof CompareDataFlowNode) {
                // leftArg
                subexpressions.add(
                        new Subexpression(
                                ((CompareDataFlowNode) block).getLeftArg(),
                                ((CompareDataFlowNode) block).getScope()));

                // rightArg
                subexpressions.add(
                        new Subexpression(
                                ((CompareDataFlowNode) block).getRightArg(),
                                ((CompareDataFlowNode) block).getScope()));
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: CompareDataFlowNode path unimplemented.");
            } else if (block instanceof MethodCallDataFlowNode) {
                // Add each parameter to the method call
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: MethodCallDataFlowNode path unimplemented.");
            } else if (block instanceof ReturnStatementDataFlowNode) {
                throw new UnsupportedOperationException("AvailabilityCalculator#calculateGenSets: MethodCallDataFlowNode path unimplemented.");
            }
            // The other nodes don't produce any subexpressions
        }

        return ImmutableSet.<Subexpression>copyOf(subexpressions);
    }

    public Set<Subexpression> getAvailableSubexpressionsOfBasicBlock(DataFlowNode b) {
        return inSets.get(b);
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
