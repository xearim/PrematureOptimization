package edu.mit.compilers.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

public class Graph<T> {
    private final ImmutableMultimap<Node<T>, Node<T>> forwardEdges;
    /** Just the inverse of forwardEdges, for convenience/speed. */
    private final ImmutableMultimap<Node<T>, Node<T>> backwardEdges;
    private final ImmutableMap<Node<T>, JumpDestination<T>> jumpDestinations;

    private final Node<T> start;
    private final Node<T> end;

    public Node<T> getStart() {
        return start;
    }

    public Node<T> getEnd() {
        return end;
    }

    public Collection<Node<T>> getSuccessors(Node<T> node) {
        return forwardEdges.get(node);
    }

    public Collection<Node<T>> getPredecessors(Node<T> node) {
        return backwardEdges.get(node);
    }

    public boolean isBranch(Node<T> node) {
        return jumpDestinations.containsKey(node);
    }

    public Node<T> getNonJumpSuccessor(Node<T> node) {
        checkArgument(isBranch(node), "Node %s is not a branch node.", node);
        // Return the only successor that is not a jumpDestination.
        for (Node<T> successor : forwardEdges.get(node)) {
            if (!successor.equals(jumpDestinations.get(node).destination)) {
                return successor;
            }
        }
        throw new AssertionError("No non-jump successor found for node " + node);
    }

    public Node<T> getJumpSuccessor(Node<T> node) {
        checkArgument(isBranch(node), "Node %s is not a branch node.", node);
        return jumpDestinations.get(node).destination;
    }

    public JumpType getJumpType(Node<T> node) {
        checkArgument(isBranch(node), "Node %s is not a branch node.", node);
        return jumpDestinations.get(node).jumpType;
    }

    /** Specifies how to choose which successor to go to, when there are multiple successors. */
    private static final class JumpDestination<T> {
        final JumpType jumpType;
        final Node<T> destination;
        JumpDestination(JumpType jumpType, Node<T> destination) {
            this.jumpType = jumpType;
            this.destination = destination;
        }
    }

    private Graph(Multimap<Node<T>, Node<T>> forwardEdges, Map<Node<T>,
            JumpDestination<T>> jumpDestinations, Node<T> start, Node<T> end) {
        this.forwardEdges = ImmutableMultimap.copyOf(forwardEdges);
        // Use `this.` because only the ImmutableMultimap has the inverse() method.
        this.backwardEdges = this.forwardEdges.inverse();
        this.jumpDestinations = ImmutableMap.copyOf(jumpDestinations);
        this.start = start;
        this.end = end;
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static class Builder<T> {
        private final Multimap<Node<T>, Node<T>> edges = HashMultimap.create();
        private final Map<Node<T>, JumpDestination<T>> jumpDestinations =
                new HashMap<Node<T>, JumpDestination<T>>();
        /**
         * All nodes that have been given a "default" branch.  These nodes must also be given
         * a "jump" branch before the Graph is built.
         */
        private final Set<Node<T>> haveNonJumpBranch = new HashSet<Node<T>>();
        private Node<T> start;
        private Node<T> end;

        public Builder() {
            // Each new buidler gets a NOP node to begin with. This allows us to
            // maintain the invariant that all graphs always have a start node
            // and an end node.
            Node<T> seedNode = Node.nop();
            start = seedNode;
            end = seedNode;
        }

        public Graph<T> build() {
            // We couldn't enforce this as the graph was being build up, so we
            // check it right now.
            checkState(jumpDestinations.keySet().equals(haveNonJumpBranch));
            return new Graph<T>(edges, jumpDestinations, start, end);
        }

        /**
         * Indicate that control flows from source to sink.
         *
         * @param source The node from which control flows.
         * @param sink Then node into which control flows.
         * @return This builder, for daisy chaining.
         */
        public Builder<T> link(Node<T> source, Node<T> sink) {
            checkArgument(!edges.containsKey(source),
                    "Tried to add a second sink %s to a non-branch node %s.", sink, source);
            edges.put(source, sink);
            return this;
        }

        public Builder<T> linkNonJumpBranch(Node<T> branchPoint, Node<T> nonJumpBranch) {
            checkArgument(!haveNonJumpBranch.contains(branchPoint),
                    "Tried to add second Default Branch %s at branch point %s.",
                    nonJumpBranch, branchPoint);
            edges.put(branchPoint, nonJumpBranch);
            haveNonJumpBranch.add(branchPoint);
            return this;
        }

        public Builder<T> linkJumpBranch(Node<T> branchPoint, JumpType type, Node<T> jumpBranch) {
            checkArgument(!jumpDestinations.containsKey(branchPoint),
                    "Tried to add a second Jump Branch %s at branch point %s.",
                    jumpBranch, branchPoint);
            edges.put(branchPoint, jumpBranch);
            jumpDestinations.put(branchPoint, new JumpDestination<>(type, jumpBranch));
            return this;
        }

        public Builder<T> linkNonJumpBranch(Node<T> branchPoint, Builder<T> nonJumpBranch) {
            copyIn(nonJumpBranch);
            return linkNonJumpBranch(branchPoint, nonJumpBranch.start);
        }

        public Builder<T> linkJumpBranch(Node<T> branchPoint, JumpType type, Builder<T> jumpBranch) {
            copyIn(jumpBranch);
            return linkJumpBranch(branchPoint, type, jumpBranch.start);
        }

        public Builder<T> append(Node<T> node) {
            link(end, node);
            end = node;
            return this;
        }

        public Builder<T> append(T value) {
            return append(Node.of(value));
        }

        public Builder<T> append(Builder<T> builder) {
            copyIn(builder);
            link(end, builder.start);
            end = builder.end;
            return this;
        }

        /**
         * Make a NOP node, make two nodes point to it, and set it as the graph's end node.
         *
         * <p>It is possible that we would want to use this for some number of nodes
         * other than two.  But, two is by far the most common case.
         *
         * @return This graph, for daisy chaining.
         */
        public Builder<T> setEndToSinkFor(Node<T> node, Node<T> otherNode) {
            end = Node.nop();
            link(node, end);
            link(otherNode, end);
            return this;
        }

        /**
         * Copy all links from another builder into this builder.
         *
         * <p>This method checks that all links are "compatible" with the links that
         * are already present in this graph.  For example, if this graph already contains
         * a jump destination for a node, and the other graph has a different jump
         * destination for the same node, it will throw an AssertionError.
         */
        private void copyIn(Builder<T> builder) {
            for (java.util.Map.Entry<Node<T>, Node<T>> entry : builder.edges.entries()) {
                Node<T> source = entry.getKey();
                Node<T> sink = entry.getValue();

                JumpDestination<T> jumpDestination = builder.jumpDestinations.get(source);
                if (jumpDestination != null && jumpDestination.destination.equals(sink)) {
                    linkJumpBranch(source, jumpDestination.jumpType, jumpDestination.destination);
                } else if(builder.haveNonJumpBranch.contains(source)) {
                    // This wasn't a jump branch, so it must be a default branch.
                    linkNonJumpBranch(source, sink);
                } else {
                    // Just perform a normal link.
                    link(source, sink);
                }
            }
        }
    }
}
