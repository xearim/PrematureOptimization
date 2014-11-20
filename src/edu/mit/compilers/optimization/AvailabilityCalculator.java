package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import com.google.common.collect.ImmutableSet;

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
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.FlowGraph;

/**
 * Given a basic block, the AvailabilityCalculator computes all available
 * subexpressions at each block accessible from the input basic block.
 */
public class AvailabilityCalculator {
    private Map<DataFlowNode, Set<ScopedExpression>> inSets;

    public AvailabilityCalculator (DataFlowNode entryBlock) {
        calculateAvailability(entryBlock);
    }

    public AvailabilityCalculator(FlowGraph<ScopedStatement> dataFlowGraph) {
        // TODO(jasonpr): Implement once manny's refactoring has been merged.
        throw new RuntimeException("Not yet implemented!");
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

        // Initialize the parameter sets
        Set<ScopedExpression> allSubexpressions = new HashSet<ScopedExpression>();
        Map<DataFlowNode, Set<ScopedExpression>> genSets = new HashMap<DataFlowNode, Set<ScopedExpression>>();
        Map<DataFlowNode, Set<ScopedExpression>> killSets = new HashMap<DataFlowNode, Set<ScopedExpression>>();
        calculateConstants(allBlocks, allSubexpressions,genSets,killSets);

        // Set up OUT
        Map<DataFlowNode, Set<ScopedExpression>> outSets =
                new HashMap<DataFlowNode, Set<ScopedExpression>>();
        Set<DataFlowNode> changed;


        // Run algorithm
        for (DataFlowNode block : inSets.keySet()) {
            outSets.put(block, new HashSet<ScopedExpression>(allSubexpressions));
        }

        outSets.put(entryBlock, new HashSet<ScopedExpression>(
                genSets.get(entryBlock)));

        changed = allBasicBlocks();
        checkState(changed.remove(entryBlock),
                "entryBlock is not in set of all blocks.");

        while (!changed.isEmpty()) {
            DataFlowNode block;
            Set<ScopedExpression> newOut;

            block = changed.iterator().next();
            changed.remove(block);

            this.inSets.put(block, new HashSet<ScopedExpression>(allSubexpressions));
            for (DataFlowNode predecessor : block.getPredecessors()) {
                this.inSets.get(block).retainAll(outSets.get(predecessor));
            }

            newOut = new HashSet<ScopedExpression>(this.inSets.get(block));
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
        inSets = new HashMap<DataFlowNode, Set<ScopedExpression>>();
        Deque<DataFlowNode> queue = new ArrayDeque<DataFlowNode>();

        queue.push(entryBlock);

        while (!queue.isEmpty()) {
            DataFlowNode node = queue.pop();
            if (inSets.containsKey(node)) {
                continue;
            }

            // Add a new, currently empty, set of subexpressions.
            inSets.put(node, new HashSet<ScopedExpression>());

            for (DataFlowNode child : node.getSuccessors()) {
                queue.push(child);
            }
            for (DataFlowNode child : node.getPredecessors()) {
                queue.push(child);
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
     * TODO(Manny): make sure that += and -= work like how they should
     */
    private static void calculateConstants(Set<DataFlowNode> allNodes,
            Set<ScopedExpression> allSubexpressions,
            Map<DataFlowNode,Set<ScopedExpression>> genSets,
            Map<DataFlowNode, Set<ScopedExpression>> killSets) {

        // Set up sets necessary to determine killSets
        Map<DataFlowNode, Set<ScopedVariable>> varSets = new HashMap<DataFlowNode, Set<ScopedVariable>>();
        Map<ScopedVariable, Set<ScopedExpression>> varKillSets = new HashMap<ScopedVariable, Set<ScopedExpression>>();
        Set<ScopedVariable> globals = getGlobals(allNodes);
        
        // All globals need a kill set since they could be edited solely
        // by a function call that we never encounter (lack of idempotence)
        for (ScopedVariable global : globals){
        	varKillSets.put(global, new HashSet<ScopedExpression>());
        }

        for (DataFlowNode node : allNodes) {
            genSets.put(node, new HashSet<ScopedExpression>());
            killSets.put(node, new HashSet<ScopedExpression>());
            varSets.put(node, new HashSet<ScopedVariable>());

            if (node instanceof StatementDataFlowNode
                    && ((StatementDataFlowNode) node).getExpression().isPresent()) {
                
                // Kill subexpressions with an assigned variable
                if (node instanceof AssignmentDataFlowNode) {
                    // Kill subexpressions with assigned variable
                    ScopedVariable assignmentTarget = ScopedVariable.getAssigned((AssignmentDataFlowNode) node);
                    varSets.get(node).add(assignmentTarget);
                    // Note that the variable exists
                    if (!varKillSets.containsKey(assignmentTarget)) {
                        varKillSets.put(assignmentTarget, new HashSet<ScopedExpression>());
                    }
                }
                
                // Deal with subexpressions 
                NativeExpression ne = ((StatementDataFlowNode) node).getExpression().get();
                if (isComplexEnough(ne)) {
                    if (containsMethodCall(ne)) {
                        // Kill subexpressions with globals
                        varSets.get(node).addAll(globals);
                    } else {
                        ScopedExpression newSubexpr = new ScopedExpression(ne, ((StatementDataFlowNode) node).getScope());

                        // Put it in allSubexpressions and the GEN set of this node.
                        allSubexpressions.add(newSubexpr);
                        genSets.get(node).add(newSubexpr);

                        // Note what kills that subexpression
                        for (ScopedVariable var : newSubexpr.getVariables()) {
                            if (!varKillSets.containsKey(var)) {
                                varKillSets.put(var, new HashSet<ScopedExpression>());
                            }
                            varKillSets.get(var).add(newSubexpr);
                        }
                    }
                }
            }
        }

        for (DataFlowNode node : allNodes) {
            for (ScopedVariable var : varSets.get(node)) {
                for (ScopedExpression subexpr : varKillSets.get(var)) {
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
    private static Set<ScopedVariable> getGlobals(Set<DataFlowNode> allNodes) {
    	Set<ScopedVariable> globalVars = new HashSet<ScopedVariable>();
        for(DataFlowNode node : allNodes){
        	if(node instanceof StatementDataFlowNode){
	        	for(ScopedVariable var : 
	        		ScopedVariable.getVariablesOf((StatementDataFlowNode) node)){
	        		if(var.isGlobal()){
	        			globalVars.add(var);
	        		}
	        	}
        	}
        }
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
     * Determines if a NativeExpression is complex enough to be worth tracking.
     * Does not check for MethodCalls inside of GeneralExpression.
     * Any GeneralExpression that passes this check may be considered a
     * NativeExpression.
     */
    public static boolean isComplexEnough(GeneralExpression ge) {
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
    public Set<ScopedExpression> availableSubexpressionsAt(DataFlowNode node) {
        return this.inSets.get(node);
    }


    /** Return whether an expression is available at a DataFlowNode. */
    public boolean isAvailable(GeneralExpression expr, StatementDataFlowNode node) {
        if (!(expr instanceof NativeExpression)) {
            // Only NativeExpressions are ever available.
            return false;
        }
        ScopedExpression scopedExpr = new ScopedExpression((NativeExpression) expr, node.getScope());

        // TODO Figure out why a direct contains doesnt work
        for(ScopedExpression ex : inSets.get(node)){
        	if(ex.equals(scopedExpr)){
        		return true;
        	}
        }
        return false;
    }

    public boolean isAvailable(GeneralExpression expr, ScopedStatement node) {
        // TODO(jasonpr): Implement once manny has merged our refactoring.
        throw new RuntimeException("Not yet implemented.");
    }
}
