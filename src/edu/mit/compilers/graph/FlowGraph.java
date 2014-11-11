package edu.mit.compilers.graph;

import java.util.Collection;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

public interface FlowGraph<T> {
    public Node<T> getStart();
    public Node<T> getEnd();
    public Collection<Node<T>> getSuccessors(Node<T> node);
    public boolean isBranch(Node<T> node);
    public Node<T> getNonJumpSuccessor(Node<T> node);
    public Node<T> getJumpSuccessor(Node<T> node);
    public JumpType getJumpType(Node<T> node);
}
