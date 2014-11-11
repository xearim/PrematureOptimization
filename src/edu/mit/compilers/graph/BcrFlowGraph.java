package edu.mit.compilers.graph;

import java.util.Collection;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class BcrFlowGraph<T> implements FlowGraph<T> {

    private final FlowGraph<T> flowGraph;
    private final Node<T> breakTerminal;
    private final Node<T> continueTerminal;
    private final Node<T> returnTerminal;

    public BcrFlowGraph(FlowGraph<T> flowGraph,
            Node<T> breakTerminal, Node<T> continueTerminal, Node<T> returnTerminal) {
        this.flowGraph = flowGraph;
        this.breakTerminal = breakTerminal;
        this.continueTerminal = continueTerminal;
        this.returnTerminal = returnTerminal;
    }

    @Override
    public Node<T> getStart() {
        return flowGraph.getStart();
    }

    @Override
    public Node<T> getEnd() {
        return flowGraph.getEnd();
    }

    @Override
    public Collection<Node<T>> getSuccessors(Node<T> node) {
        return flowGraph.getSuccessors(node);
    }

    @Override
    public boolean isBranch(Node<T> node) {
        return flowGraph.isBranch(node);
    }

    @Override
    public Node<T> getNonJumpSuccessor(Node<T> node) {
        return getNonJumpSuccessor(node);
    }

    @Override
    public Node<T> getJumpSuccessor(Node<T> node) {
        return getJumpSuccessor(node);
    }

    @Override
    public JumpType getJumpType(Node<T> node) {
        return getJumpType(node);
    }
    
    public Node<T> getBreakTerminal() {
        return breakTerminal;
    }

    public Node<T> getContinueTerminal() {
        return continueTerminal;
    }

    public Node<T> getReturnTerminal() {
        return returnTerminal;
    }

    
}
