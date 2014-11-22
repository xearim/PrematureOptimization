package edu.mit.compilers.graph;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.graph.BasicFlowGraph.Builder;

/**
 * A FlowGraph with Break, Continue, and Return terminals, in addition to
 * the standard Start and End terminals.
 *
 * @param <T>  The type of element at this graph's nodes.
 */
public class BcrFlowGraph<T> implements FlowGraph<T> {

    /** Nearly all functionality is delegated to this underlying FlowGraph. */
    private final FlowGraph<T> flowGraph;
    private final Node<T> breakTerminal;
    private final Node<T> continueTerminal;
    private final Node<T> returnTerminal;

    private BcrFlowGraph(FlowGraph<T> flowGraph,
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
    public Set<Node<T>> getNodes() {
        return Sets.union(
                ImmutableSet.of(breakTerminal, continueTerminal, returnTerminal),
                flowGraph.getNodes());
    }

    @Override
    public Set<Node<T>> getSuccessors(Node<T> node) {
        return flowGraph.getSuccessors(node);
    }

    @Override
    public Set<Node<T>> getPredecessors(Node<T> node) {
        return flowGraph.getPredecessors(node);
    }

    @Override
    public boolean isBranch(Node<T> node) {
        return flowGraph.isBranch(node);
    }

    @Override
    public Node<T> getNonJumpSuccessor(Node<T> node) {
        return flowGraph.getNonJumpSuccessor(node);
    }

    @Override
    public Node<T> getJumpSuccessor(Node<T> node) {
        return flowGraph.getJumpSuccessor(node);
    }

    @Override
    public JumpType getJumpType(Node<T> node) {
        return flowGraph.getJumpType(node);
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

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static <T> Builder<T> builderOf(BcrFlowGraph<T> graph) {
        return new Builder<T>().append(graph);
    }

    public static class Builder<T> {
        private Node<T> breakTerminal = Node.nop();
        private Node<T> continueTerminal = Node.nop();
        private Node<T> returnTerminal = Node.nop();
        private final BasicFlowGraph.Builder<T> basicBuilder = BasicFlowGraph.builder();

        public Node<T> getBreakTerminal() {
            return breakTerminal;
        }

        public Builder<T> setBreakTerminal(Node<T> breakTerminal) {
            this.breakTerminal = breakTerminal;
            return this;
        }

        public Node<T> getContinueTerminal() {
            return continueTerminal;
        }

        public Builder<T> setContinueTerminal(Node<T> continueTerminal) {
            this.continueTerminal = continueTerminal;
            return this;
        }

        public Node<T> getReturnTerminal() {
            return returnTerminal;
        }

        public Builder<T> setReturnTerminal(Node<T> returnTerminal) {
            this.returnTerminal = returnTerminal;
            return this;
        }

        public Node<T> getStartTerminal() {
            return basicBuilder.getStart();
        }

        public Builder<T> setStartTerminal(Node<T> start) {
            basicBuilder.setStart(start);
            return this;
        }

        public Node<T> getEndTerminal() {
            return basicBuilder.getEnd();
        }

        public Builder<T> setEndTerminal(Node<T> end) {
            basicBuilder.setEnd(end);
            return this;
        }

        public Builder<T> link(Node<T> source, Node<T> sink) {
            basicBuilder.link(source, sink);
            return this;
        }

        public Builder<T> linkNonJumpBranch(Node<T> branchPoint, Node<T> nonJumpBranch) {
            basicBuilder.linkNonJumpBranch(branchPoint, nonJumpBranch);
            return this;
        }

        public Builder<T> linkJumpBranch(Node<T> branchPoint, JumpType type, Node<T> jumpBranch) {
            basicBuilder.linkJumpBranch(branchPoint, type, jumpBranch);
            return this;
        }

        /** Link a node to the current end node, and point the end node at the new end. */
        public Builder<T> append(Node<T> node) {
            basicBuilder.append(node);
            return this;
        }

        /** Link a node to the current end node, and point the end node at the new end. */
        public Builder<T> append(T value) {
            basicBuilder.append(value);
            return this;
        }

        public Builder<T> append(FlowGraph<T> graph) {
            basicBuilder.append(graph);
            return this;
        }

        /** Link a graph to the current end node, and point the end node at the new end. */
        public Builder<T> copyIn(FlowGraph<T> graph) {
            basicBuilder.copyIn(graph);
            return this;
        }

        public Builder<T> setEndToSinkFor(Node<T> node, Node<T> otherNode) {
            basicBuilder.setEndToSinkFor(node, otherNode);
            return this;
        }

        public Builder<T> replace(Node<T> node, FlowGraph<T> replacement) {
            basicBuilder.replace(node, replacement);
            if (node.equals(returnTerminal)) {
                setReturnTerminal(replacement.getEnd());
            }
            return this;
        }

        public BcrFlowGraph<T> build() {
            return new BcrFlowGraph<T>(
                    basicBuilder.build(), breakTerminal, continueTerminal, returnTerminal);
        }
    }
}
