package edu.mit.compilers.codegen.controllinker.statements;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BreakStatement;
import edu.mit.compilers.ast.ContinueStatement;
import edu.mit.compilers.ast.ForLoop;
import edu.mit.compilers.ast.IfStatement;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.ast.WhileLoop;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraph;
import edu.mit.compilers.codegen.controllinker.ControlTerminalGraphFactory;

public class StatementGraphFactory implements ControlTerminalGraphFactory {
    private final ControlTerminalGraph graph;

    public StatementGraphFactory(Statement statement, Scope scope) {
        this.graph = calculateGraph(statement, scope);
    }

    private ControlTerminalGraph calculateGraph(Statement statement, Scope scope) {
        if (statement instanceof Assignment) {
            Assignment assignment = (Assignment) statement;
            return new ControlTerminalGraph(
                    new AssignmentGraphFactory(assignment.getLocation(),
                            assignment.getOperation(), assignment.getExpression(),
                            scope).getGraph());
        } else if (statement instanceof BreakStatement) {
            return new BreakStatementGraphFactory().getGraph();
        } else if (statement instanceof ContinueStatement) {
            return new ContinueStatementGraphFactory().getGraph();
        } else if (statement instanceof ForLoop) {
            return new ForLoopGraphFactory((ForLoop) statement, scope).getGraph();
        } else if (statement instanceof IfStatement) {
            return new IfStatementGraphFactory((IfStatement) statement, scope).getGraph();
        } else if (statement instanceof MethodCall) {
            // TODO(xearim): figure out how to retrieve Method from MethodCall
            throw new RuntimeException("MethodCallFactory not implemented");
        } else if (statement instanceof ReturnStatement) {
            return new ReturnStatementGraphFactory((ReturnStatement) statement, scope).getGraph();
        } else if (statement instanceof WhileLoop) {
            return new WhileLoopGraphFactory((WhileLoop) statement, scope).getGraph();
        } else {
            throw new AssertionError(
                    "Received something that isn't a known statement");
        }
    }

    @Override
    public ControlTerminalGraph getGraph() {
        return graph;
    }

}
