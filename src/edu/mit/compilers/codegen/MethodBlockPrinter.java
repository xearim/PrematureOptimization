package edu.mit.compilers.codegen;

import static com.google.common.base.Preconditions.checkState;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.codegen.asm.Label;
import edu.mit.compilers.codegen.asm.Label.LabelType;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.common.UniqueIdentifier;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

public class MethodBlockPrinter {
    // We use a static unique identifier so that nodes in different methods have
    // different ids.
    // If we don't like having a static UniqueIdentifier, we could generate
    // per-instance unique ids, and prepend a unique prefix like methodName
    // to any generated values.
    private static final UniqueIdentifier<Node<Instruction>> uniqueIdentifier =
            new UniqueIdentifier<Node<Instruction>>();

    private final FlowGraph<Instruction> methodGraph;
    private final Set<Node<Instruction>> multiSourced;
    private final Set<Node<Instruction>> visited = new HashSet<Node<Instruction>>();

    MethodBlockPrinter(Method method, Set<String> optimizationNames) {
        this.methodGraph = Targets.controlFlowGraph(method, optimizationNames);
        this.multiSourced = getMultiSourceNodes(this.methodGraph);
    }

    public void printStream(PrintStream outputStream){
        printNodeChain(methodGraph.getStart(), outputStream);
    }

    private void printNodeChain(Node<Instruction> currentNode, PrintStream outputStream){
        if(visited.contains(currentNode)){
            // If we have already visited the node, it must be a label node, so we just want to jump to that label.
            checkState(multiSourced.contains(currentNode));
            outputStream.println(Instructions.jump(getMultiSourceLabel(currentNode)).inAttSyntax());
            return;
        }
        visited.add(currentNode);
        printDirectNode(currentNode, outputStream);
        if (methodGraph.isBranch(currentNode)) {
            printBranchingNode(currentNode, outputStream);
        } else {
            Collection<Node<Instruction>> successors = methodGraph.getSuccessors(currentNode);
            if (!successors.isEmpty()) {
                printNodeChain(Iterables.getOnlyElement(successors), outputStream);
            }
        }
    }

    private void printDirectNode(Node<Instruction> currentNode, PrintStream outputStream){
        if (multiSourced.contains(currentNode)) {
            // Multiple sources point to this node. Later we'll need to jump to this node.
            // We print a label to allow this.
            printLabel(getMultiSourceLabel(currentNode), outputStream);
        }
        if (currentNode.hasValue()) {
            outputStream.println(currentNode.value().inAttSyntax());
        }
    }

    private void printBranchingNode(Node<Instruction> currentNode, PrintStream outputStream){
        Node<Instruction> nonJumpSuccessor= methodGraph.getNonJumpSuccessor(currentNode);
        Node<Instruction> jumpSuccessor = methodGraph.getJumpSuccessor(currentNode);
        // Figure out where to jump.
        // Current node has only one jump branch, so this is guaranteed to be unique.
        Label jumpLabel = getJumpLabel(currentNode);
        outputStream.println(Instructions.jumpTyped(methodGraph.getJumpType(currentNode), jumpLabel).inAttSyntax());
        printNodeChain(nonJumpSuccessor, outputStream);
        // Print the label for the false node, so we can jump to it.
        printLabel(jumpLabel, outputStream);
        printNodeChain(jumpSuccessor, outputStream);
    }

    private Label getJumpLabel(Node<Instruction> jumpNode){
        // The first node of a false node sequence should always be a writeLabel instruction
        return new Label(LabelType.CONTROL_FLOW, uniqueIdentifier.getId(jumpNode) + "_false");
    }

    private Label getMultiSourceLabel(Node<Instruction> node) {
        return new Label(LabelType.CONTROL_FLOW, uniqueIdentifier.getId(node) +"_multi_source");
    }

    private void printLabel(Label label, PrintStream outputStream) {
        outputStream.println(label.labelText() + ":");
    }

    /** Get the set of nodes that are the 'next' node of multiple other nodes. */
    private static <T> Set<Node<T>> getMultiSourceNodes(FlowGraph<T> flowGraph) {
        ImmutableSet.Builder<Node<T>> resultBuilder = ImmutableSet.builder();
        for (Node<T> sink : flowGraph.getNodes()) {
            if (flowGraph.getPredecessors(sink).size() > 1) {
                resultBuilder.add(sink);
            }
        }
        return resultBuilder.build();
    }
}
