package edu.mit.compilers.graph;

import java.util.Set;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

public interface FlowGraph<T> {
    public Node<T> getStart();
    public Node<T> getEnd();
    public Set<Node<T>> getNodes();
    public Set<Node<T>> getSuccessors(Node<T> node);
    public Set<Node<T>> getPredecessors(Node<T> node);
    public boolean isBranch(Node<T> node);
    public Node<T> getNonJumpSuccessor(Node<T> node);
    public Node<T> getJumpSuccessor(Node<T> node);
    public JumpType getJumpType(Node<T> node);
}
