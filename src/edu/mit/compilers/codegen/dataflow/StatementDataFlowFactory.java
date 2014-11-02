package edu.mit.compilers.codegen.dataflow;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.MethodCallDataFlowNode;

public class StatementDataFlowFactory implements DataFlowFactory{
	   private Statement statement;
	    private Scope scope;

	    public StatementDataFlowFactory(Statement statement, Scope scope) {
	        this.statement = statement;
	        this.scope = scope;
	    }

	    private DataFlow calculateDataFlow(Statement statement, Scope scope) {
	        if (statement instanceof Assignment) {
	            return DataFlow.ofNodes(new AssignmentDataFlowNode((Assignment) statement, scope));
	        } else if (statement instanceof BreakStatement) {
	            return new BreakStatementDataFlowFactory().getDataFlow();
	        } else if (statement instanceof ContinueStatement) {
	            return new ContinueStatementDataFlowFactory().getDataFlow();
	        } else if (statement instanceof ForLoop) {
	            return new ForLoopDataFlowFactory((ForLoop) statement, scope).getDataFlow();
	        } else if (statement instanceof IfStatement) {
	            return new IfStatementDataFlowFactory((IfStatement) statement, scope).getDataFlow();
	        } else if (statement instanceof MethodCall) {
	            return DataFlow.ofNodes(new MethodCallDataFlowNode((MethodCall) statement, scope));
	        } else if (statement instanceof ReturnStatement) {
	            return new ReturnStatementDataFlowFactory((ReturnStatement) statement, scope).getDataFlow();
	        } else if (statement instanceof WhileLoop) {
	            return new WhileLoopDataFlowFactory((WhileLoop) statement, scope).getDataFlow();
	        } else {
	            throw new AssertionError(
	                    "Received something that isn't a known statement");
	        }
	    }

	    @Override
	    public DataFlow getDataFlow() {
	        return calculateDataFlow(statement, scope);
	    }
}
