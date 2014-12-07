package edu.mit.compilers.tools;

import java.io.PrintStream;

import edu.mit.compilers.common.UniqueIdentifier;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.DiGraph;
import edu.mit.compilers.graph.Node;

public class DiGraphPrinter {

    public static <T> void print(PrintStream printStream,
            DiGraph<T> diGraph) {

        UniqueIdentifier<Node<T>> uniqueIds = new UniqueIdentifier<Node<T>>();

        printStream.println("digraph AST {");
        for (Node<T> node : diGraph.getNodes()) {
            String label = getLabel(node, diGraph);
            printStream.println(Dot.node(uniqueIds.getId(node), label));
            for (Node<T> successor : diGraph.getSuccessors(node)) {
                long nodeId = uniqueIds.getId(node);
                long successorId = uniqueIds.getId(successor);
                printStream.println(Dot.edge(nodeId, successorId));
            }
        }
        printStream.println("}");
    }

    /** Gets the label describing a node in a flow graph. */
    private static <T> String getLabel(Node<T> node, DiGraph<T> diGraph) {
        String label = node.contentString();
        // TODO(jasonpr): Make a separate method for BcrFlowGraphs, so we can avoid
        // this runtime type check.
        if (diGraph instanceof BcrFlowGraph
            && node.equals(((BcrFlowGraph<T>) diGraph).getReturnTerminal())) {
            label += " [RETURN]";
        }
        return label;
    }

}
