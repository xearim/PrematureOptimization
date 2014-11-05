package edu.mit.compilers.optimization;

import static edu.mit.compilers.codegen.SequentialDataFlowNode.link;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.CompareDataFlowNode;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.MethodCallDataFlowNode;
import edu.mit.compilers.codegen.NopDataFlowNode;
import edu.mit.compilers.codegen.ReturnStatementDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.StatementDataFlowNode;
import edu.mit.compilers.codegen.dataflow.DataFlow;
import edu.mit.compilers.codegen.dataflow.DataFlow.DataControlNodes;
import edu.mit.compilers.codegen.dataflow.DataFlowUtil;
import edu.mit.compilers.common.Variable;

/**
 * Preforms CSE (common subexpression elimination).
 *
 * <p>Currently, subexpressions aren't eliminated-- only full expressions are eliminated.
 * That is, if (x + y) is available, we will not (yet!) optimize (1 + x + y).
 *
 * <p>This should really be a non-instantiable class with a static optimize method, but
 * it needs to be instantiable to conform to DataFlowOptimizer's interface.
 */
public class CommonExpressionEliminator implements DataFlowOptimizer {

    private static final String TEMP_VAR_PREFIX = "cse_temp";

    @Override
    public void optimize(DataFlowIntRep ir) {
        new Eliminator(ir).optimize();
    }

    // TODO(jasonpr): Figure out why this helper class feels so hacky, and unhack it.
    /**
     * Helper class for actually performing the optimization.
     *
     * <p>It allows us to store the AvailabilityCalculator and some other
     * values in instance variables, so we don't need to keep passing them
     * around as parameters.
     *
     */
    private static final class Eliminator {
        private final DataFlowIntRep ir;
        private final AvailabilityCalculator availCalc;
        private final Collection<DataFlowNode> nodes;
        // TODO(jasonpr): Use ScopedExpression, not NativeExpression.
        private final Map<NativeExpression, Variable> tempVars;

        public Eliminator(DataFlowIntRep ir) {
            this.ir = ir;
            this.availCalc = new AvailabilityCalculator(ir.getDataFlowGraph().getBeginning());
            this.nodes = DataFlowUtil.nodesIn(ir);
            this.tempVars = tempVars(expressions(nodes));
        }

        public void optimize() {
            for (DataFlowNode node : nodes) {
                if (!(node instanceof StatementDataFlowNode)) {
                    // Only optimize Statement nodes.
                    continue;
                }
                StatementDataFlowNode statementNode = (StatementDataFlowNode) node;
                for (NativeExpression expr : nodeExprs(statementNode)) {
                    if (availCalc.isAvailable(expr, statementNode)) {
                        replace(statementNode, useTemp(node, expr));
                    } else if(AvailabilityCalculator.isComplexEnough(expr)) {
                        // For now, we alway generate if it's not available.
                        // TODO(jasonpr): Use 'reasons' or 'benefactors' to reduce
                        // amount of unnecessary temp filling.
                        if (statementNode instanceof MethodCallDataFlowNode) {
                            // Just skip it!  We only call it for its side effects.
                            continue;
                        }
                        addToScope(statementNode, expr);
                        replace(statementNode, fillAndUseTemp(node, expr));
                    }
                }
            }
        }


        /**
         * Return a replacement for 'node' that does not call for 'expr' to be
         * evaluated, but instead loads its value from the corresponding temp
         * variable.
         *
         * <p>Requires that the expression is available at the node.
         */
        private DataFlow useTemp(DataFlowNode node, GeneralExpression expr) {
        	Preconditions.checkState(node instanceof StatementDataFlowNode);
        	Preconditions.checkState(tempVars.get(expr) != null);
        	
        	StatementDataFlowNode statementNode = (StatementDataFlowNode) node;
        	Scope statementScope = statementNode.getScope();
        	Preconditions.checkState(statementNode.getExpression().isPresent());
        	
        	Location temp = new ScalarLocation(tempVars.get(expr), LocationDescriptor.machineCode());
        	
        	NopDataFlowNode doNothing = NopDataFlowNode.nop();
        	
        	StatementDataFlowNode newStatement = getReplacement(statementNode, statementScope, temp);
        	
        	link(doNothing, newStatement);
        	
        	return new DataFlow(doNothing, newStatement, new DataControlNodes());
        }

        /**
         * Return a replacement for 'node' that stores the value of 'expr' in
         * its temp variable, and uses that temp variable when executing the
         * node's statement.
         */
        private DataFlow fillAndUseTemp(DataFlowNode node, NativeExpression expr) {
            // TODO(jasonpr): Implement!
        	// The node to replace should actually contain statements
        	Preconditions.checkState(node instanceof StatementDataFlowNode);
        	Preconditions.checkState(tempVars.get(expr) != null);
        	
        	StatementDataFlowNode statementNode = (StatementDataFlowNode) node;
        	Scope statementScope = statementNode.getScope();
        	Preconditions.checkState(statementNode.getExpression().isPresent());
        	
        	addToScope(statementNode, expr);
        	Location temp = new ScalarLocation(tempVars.get(expr), LocationDescriptor.machineCode());
        	
        	AssignmentDataFlowNode newTemp = new AssignmentDataFlowNode(
        			Assignment.compilerAssignment(temp, expr),
        			statementScope
        			);
        	
        	StatementDataFlowNode newStatement = getReplacement(statementNode, statementScope, temp);
        	
        	link(newTemp, newStatement);
        	
        	return new DataFlow(newTemp, newStatement, new DataControlNodes());
        }
        
        private StatementDataFlowNode getReplacement(StatementDataFlowNode node, 
        		Scope scope, NativeExpression replacement){
        	if(node instanceof AssignmentDataFlowNode){
        		return replaceAssignment((AssignmentDataFlowNode) node, replacement, scope);
        	} else if(node instanceof CompareDataFlowNode){
        		return replaceCompare(replacement, scope);
        	} else if(node instanceof MethodCallDataFlowNode){
        		throw new AssertionError("Right now we cannot replace methods, we dont know if they are idempotent");
        	} else if(node instanceof ReturnStatementDataFlowNode){
        		return replaceReturnStatement(replacement, scope);
        	} else {
        		throw new AssertionError("Somehow a StatementDataFlowNode that isn't one");
        	}
        }
        
        private AssignmentDataFlowNode replaceAssignment(AssignmentDataFlowNode node,
        		NativeExpression replacement, Scope scope){
        	return new AssignmentDataFlowNode(
                    Assignment.assignmentWithReplacementExpr(node.getAssignment(), replacement),
                    scope);
        }
        
        private CompareDataFlowNode replaceCompare(NativeExpression replacement, Scope scope){
        	return new CompareDataFlowNode(replacement, scope);
        }
        
        private ReturnStatementDataFlowNode replaceReturnStatement(NativeExpression replacement, Scope scope){
        	return new ReturnStatementDataFlowNode(
        			ReturnStatement.compilerReturn(replacement),
        			scope
        			);
        }
        

        /**
         * Replace a data flow node.
         *
         * <p>The replacement will be hooked up to the old node's predecessor and successor.
         */
        private void replace(SequentialDataFlowNode old, DataFlow replacement) {
            // Link the new one in.
            if (old.hasPrev()) {
                old.getPrev().replaceSuccessor(old, replacement.getBeginning());
                replacement.getBeginning().setPrev(old.getPrev());
            }
            if (old.hasNext()) {
                old.getNext().replacePredecessor(old, replacement.getEnd());
                replacement.getEnd().setNext(old.getNext());
            }

            // If the old node was a beginning or end node, let the DataFlow know
            // what its new terminal is.
            // (We will never replace any control nodes, because those nodes
            // are never StatementDataFlowNodes.)
            if (old.equals(ir.getDataFlowGraph().getBeginning())) {
                ir.getDataFlowGraph().setBeginning(replacement.getBeginning());
            }
            if (old.equals(ir.getDataFlowGraph().getEnd())) {
                ir.getDataFlowGraph().setEnd(replacement.getEnd());
            }
        }

        private void addToScope(StatementDataFlowNode node, NativeExpression expr) {
            Variable var = tempVars.get(expr);
            FieldDescriptor fieldDesc = FieldDescriptor.forCompilerVariable(var);
            // TODO(jasonpr): Add to most specific scope possible.
            ir.getScope().addVariable(fieldDesc);
        }
    }

    /**
     * Generate a map from expression to temporary variable, for some expressions.
     *
     * <p>Non-native expressions are ignored-- we can't store them in variables!
     */
    private static Map<NativeExpression, Variable> tempVars(Iterable<NativeExpression> exprs) {
        ImmutableMap.Builder<NativeExpression, Variable> builder = ImmutableMap.builder();
        int tempNumber = 0;
        for (NativeExpression expr : exprs) {
                builder.put(expr, Variable.forCompiler(TEMP_VAR_PREFIX + tempNumber++));
        }
        return builder.build();
    }

    /** Get all the optimizable expressions from some nodes. */
    private static Iterable<NativeExpression> expressions(Iterable<DataFlowNode> nodes) {
        ImmutableSet.Builder<NativeExpression> builder = ImmutableSet.builder();
        for (DataFlowNode node : nodes) {
            if (node instanceof StatementDataFlowNode) {
                builder.addAll(nodeExprs((StatementDataFlowNode) node));
            }
        }
        return builder.build();
    }

    /**
     * Get all the expressions in the node to try to optimize.
     *
     * For now, we ONLY optimize the top-level expressions-- no subexpressions!
     */
    private static Collection<NativeExpression> nodeExprs(StatementDataFlowNode node) {
        Optional<? extends NativeExpression> expr = node.getExpression();
        return expr.isPresent()
                ? ImmutableList.<NativeExpression>of(expr.get())
                : ImmutableList.<NativeExpression>of();
    }
}
