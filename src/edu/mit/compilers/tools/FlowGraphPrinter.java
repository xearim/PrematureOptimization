package edu.mit.compilers.tools;

import java.io.PrintStream;

import edu.mit.compilers.common.UniqueIdentifier;
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
            printStream.println(Dot.node(uniqueIds.getId(node), node.contentString()));
            for (Node<T> successor : flowGraph.getSuccessors(node)) {
                printStream.println(
                        Dot.edge(uniqueIds.getId(node), uniqueIds.getId(successor)));
            }
        }
        printStream.println("}");
    }
}
