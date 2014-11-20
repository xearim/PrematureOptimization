package edu.mit.compilers.codegen.controllinker;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.Condition;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.controllinker.statements.AssignmentGraphFactory;
import edu.mit.compilers.codegen.controllinker.statements.CompareGraphFactory;
import edu.mit.compilers.codegen.controllinker.statements.ReturnStatementGraphFactory;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

public class DataFlowToControlFlowConverter {

    public static BcrFlowGraph<Instruction> convert(BcrFlowGraph<ScopedStatement> dataFlowGraph) {
        Map<Node<ScopedStatement>, FlowGraph<Instruction>> expansions = expansions(dataFlowGraph);

        BcrFlowGraph.Builder<Instruction> cfgBuilder = BcrFlowGraph.builder();

        for (Node<ScopedStatement> node : dataFlowGraph.getNodes()) {
            // Copy in all the expansions.
            cfgBuilder.copyIn(expansions.get(node));

            // Copy in all the outgoing links.
            Node<Instruction> nodeExpansionEnd = expansions.get(node).getEnd();
            for (Node<ScopedStatement> sink : dataFlowGraph.getSuccessors(node)) {
                Node<Instruction> sinkExpansionStart = expansions.get(sink).getStart();
                if (!dataFlowGraph.isBranch(node)) {
                    cfgBuilder.link(nodeExpansionEnd, sinkExpansionStart);
                    continue;
                }
                if (sink.equals(dataFlowGraph.getNonJumpSuccessor(node))) {
                    cfgBuilder.linkNonJumpBranch(nodeExpansionEnd, sinkExpansionStart);
                } else if (sink.equals(dataFlowGraph.getJumpSuccessor(node))) {
                    cfgBuilder.linkJumpBranch(
                            nodeExpansionEnd, dataFlowGraph.getJumpType(node), sinkExpansionStart);
                } else {
                    throw new AssertionError(
                            "Branch successor is neither the jump successor "
                            + "nor the nonjump successor!");
                }
            }
        }

        cfgBuilder.setStartTerminal(
                expansions.get(dataFlowGraph.getStart()).getStart());
        cfgBuilder.setEndTerminal(
                expansions.get(dataFlowGraph.getEnd()).getEnd());
        cfgBuilder.setBreakTerminal(
                expansions.get(dataFlowGraph.getBreakTerminal()).getEnd());
        cfgBuilder.setContinueTerminal(
                expansions.get(dataFlowGraph.getContinueTerminal()).getEnd());
        cfgBuilder.setReturnTerminal(
                expansions.get(dataFlowGraph.getReturnTerminal()).getEnd());

        return cfgBuilder.build();
    }

    private static Map<Node<ScopedStatement>, FlowGraph<Instruction>>
            expansions(BcrFlowGraph<ScopedStatement> dataFlowGraph) {
        ImmutableMap.Builder<Node<ScopedStatement>, FlowGraph<Instruction>> expansions =
                ImmutableMap.builder();
        for (Node<ScopedStatement> node : dataFlowGraph.getNodes()) {
            expansions.put(node, expansion(node));
        }
        return expansions.build();
    }

    private static FlowGraph<Instruction> expansion(Node<ScopedStatement> node) {
        if (!node.hasValue()) {
            return BasicFlowGraph.<Instruction>builder().build();
        }

        ScopedStatement scopedStatement = node.value();
        Statement statement = scopedStatement.getStatement();
        Scope scope = scopedStatement.getScope();

        if (statement instanceof Assignment) {
            return new AssignmentGraphFactory((Assignment) statement, scope).getGraph();
        } else if (statement instanceof Condition) {
            return new CompareGraphFactory((Condition) statement, scope).getGraph();
        } else if (statement instanceof MethodCall) {
            return new MethodCallGraphFactory((MethodCall) statement, scope).getGraph();
        } else if (statement instanceof ReturnStatement) {
            return new ReturnStatementGraphFactory((ReturnStatement) statement, scope).getGraph();
        } else {
            throw new AssertionError(
                    "Unexpected statement type in Data Flow Graph for: " + scopedStatement);
        }

    }
}
