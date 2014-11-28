package edu.mit.compilers.tools;

import java.io.PrintStream;

import edu.mit.compilers.common.UniqueIdentifier;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

/** Prints a FlowGraph in DOT format. */
public class FlowGraphPrinter {
    // TODO(jasonpr): Reenable unreachable node pruning.
    // TODO(jasonpr): Allow this to be passed from the command line.
    private static final boolean PRINT_UNREACHABLE_NODES = false;

    private FlowGraphPrinter() {}

    /** Print a flowGraph in DOT format to a printStream. */
    public static <T> void print(PrintStream printStream, FlowGraph<T> flowGraph) {
        UniqueIdentifier<Node<T>> uniqueIds = new UniqueIdentifier<Node<T>>();

        printStream.println("digraph AST {");
        for (Node<T> node : flowGraph.getNodes()) {
            String label = getLabel(node, flowGraph);
            printStream.println(Dot.node(uniqueIds.getId(node), label));
            for (Node<T> successor : flowGraph.getSuccessors(node)) {
                long nodeId = uniqueIds.getId(node);
                long successorId = uniqueIds.getId(successor);
                boolean isJumpEdge = flowGraph.isBranch(node)
                        && flowGraph.getJumpSuccessor(node).equals(successor);
                String edge = isJumpEdge
                        ? Dot.labeledEdge(nodeId, successorId,
                                flowGraph.getJumpType(node).toString())
                        : Dot.edge(nodeId, successorId);
                printStream.println(edge);
            }
        }
        printStream.println("}");
    }

    /** Gets the label describing a node in a flow graph. */
    private static <T> String getLabel(Node<T> node, FlowGraph<T> flowGraph) {
        String label = node.contentString();
        if (node.equals(flowGraph.getStart())) {
            label += " [START]";
        }
        if (node.equals(flowGraph.getEnd())) {
            label += " [END]";
        }
        // TODO(jasonpr): Make a separate method for BcrFlowGraphs, so we can avoid
        // this runtime type check.
        if (flowGraph instanceof BcrFlowGraph
            && node.equals(((BcrFlowGraph<T>) flowGraph).getReturnTerminal())) {
            label += " [RETURN]";
        }
        return label;
    }
}
