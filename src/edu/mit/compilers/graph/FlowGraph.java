package edu.mit.compilers.graph;

import java.util.Set;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

/**
 * General representation of a Flow Graph.
 *
 * <p>A flow graph is a digraph with annotations to specify how
 * control flow is affected by conditions.  If a node has two successors,
 * the graph indicates the condition under which its "jump successor"
 * should be jumped to.  Execution flows to the "non-jump successor"
 * when that condition is not met.  If a node has only one successor,
 * execution always flows to that one successor.
 *
 * <p>A FlowGraph could represent a Control Flow Graph, a Data Flow Graph,
 * or potentially other types of Flow Graphs.  More concretely, a Control
 * Flow Graph can be represented as a FlowGraph<Instruction>.
 *
 * @param <T> The type of element at this graph's nodes.
 */
public interface FlowGraph<T> extends Graph<T> {
    /** Gets the node at which execution starts. */
    public Node<T> getStart();

    /** Gets the node at which execution ends. */
    public Node<T> getEnd();

    /** Gets all the nodes referenced by this graph. */
    public Set<Node<T>> getNodes();

    /**
     * Gets all the successors of a node.
     *
     * <p>The size of the result set is in [0, 2], because this is a flow graph.
     */
    public Set<Node<T>> getSuccessors(Node<T> node);

    /** Gets all the predecessors of a node. */
    public Set<Node<T>> getPredecessors(Node<T> node);

    /**
     * Returns whether this node is a branch node.
     *
     * <p>If it is a branch node, it has a jump successor, a non-jump successor,
     * and a jump type.
     */
    public boolean isBranch(Node<T> node);

    /**
     * Gets the node to which execution flows when the jump condition is not met.
     *
     * <p>This is the "true" branch, when we follow the standard convention of
     * letting execution flow directly to the true branch, and only reaching
     * the false branch via a jump.
     */
    public Node<T> getNonJumpSuccessor(Node<T> node);

    /**
     * Gets the node to which execution flows when the jump condition is met.
     *
     * <p>This is the "false" branch, when we follow the standard convention of
     * letting execution flow directly to the true branch, and only reaching
     * the false branch via a jump.
     */
    public Node<T> getJumpSuccessor(Node<T> node);

    /**
     * Gets the jump type associated with this branch.
     *
     * <p>Prior to reaching this branch, we will have specified the comparison to
     * make.  The jump type simply specifies how to interpret the result of the
     * comparison.
     */
    public JumpType getJumpType(Node<T> node);
}
