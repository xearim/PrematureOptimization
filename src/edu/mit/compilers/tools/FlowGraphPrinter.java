package edu.mit.compilers.tools;

import java.io.PrintStream;

import edu.mit.compilers.common.UniqueIdentifier;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

/** Prints a FlowGraph in DOT format. */
public class FlowGraphPrinter<T> {
    // TODO(jasonpr): Reenable unreachable node pruning.
    // TODO(jasonpr): Allow this to be passed from the command line.
    private static final boolean PRINT_UNREACHABLE_NODES = false;

    private final PrintStream printStream;
    private final UniqueIdentifier<Node<T>> uniqueIds;

    /** Constructs a Printer that outputs to the given PrintStream. */
    public FlowGraphPrinter(PrintStream printStream) {
        this.printStream = printStream;
        uniqueIds = new UniqueIdentifier<Node<T>>();
    }

    public void print(FlowGraph<T> flowGraph) {
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
