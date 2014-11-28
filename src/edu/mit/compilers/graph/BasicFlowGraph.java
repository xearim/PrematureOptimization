package edu.mit.compilers.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.mit.compilers.codegen.asm.instructions.JumpType;

/** A basic implementation of FlowGraph. */
public class BasicFlowGraph<T> implements FlowGraph<T> {
    private final ImmutableMultimap<Node<T>, Node<T>> forwardEdges;
    /** Just the inverse of forwardEdges, for convenience/speed. */
    private final ImmutableMultimap<Node<T>, Node<T>> backwardEdges;
    private final ImmutableMap<Node<T>, JumpDestination<T>> jumpDestinations;

    private final Node<T> start;
    private final Node<T> end;

    @Override
    public Node<T> getStart() {
        return start;
    }

    @Override
    public Node<T> getEnd() {
        return end;
    }

    @Override
    public Set<Node<T>> getNodes() {
        return Sets.union(
                ImmutableSet.of(start, end),
                    Sets.union(
                            forwardEdges.keySet(),
                            backwardEdges.keySet()));
    }

    @Override
    public Set<Node<T>> getSuccessors(Node<T> node) {
        return ImmutableSet.copyOf(forwardEdges.get(node));
    }

    @Override
    public Set<Node<T>> getPredecessors(Node<T> node) {
        return ImmutableSet.copyOf(backwardEdges.get(node));
    }

    @Override
    public boolean isBranch(Node<T> node) {
        return jumpDestinations.containsKey(node);
    }

    @Override
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

    @Override
    public Node<T> getJumpSuccessor(Node<T> node) {
        checkArgument(isBranch(node), "Node %s is not a branch node.", node);
        return jumpDestinations.get(node).destination;
    }

    @Override
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

    private BasicFlowGraph(Multimap<Node<T>, Node<T>> forwardEdges, Map<Node<T>,
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

    public static <T> Builder<T> builderOf(FlowGraph<T> graph) {
        return new Builder<T>().append(graph);
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

        public BasicFlowGraph<T> build() {
            // We couldn't enforce these constraints as the graph was being built up,
            // so we check them now.
            for (Node<T> source : edges.keySet()) {
                if (jumpDestinations.containsKey(source)) {
                    checkState(edges.get(source).size() == 2);
                }
                if (haveNonJumpBranch.contains(source)) {
                    checkState(edges.get(source).size() == 2);
                }
                if (edges.get(source).size() == 2) {
                    checkState(jumpDestinations.containsKey(source));
                    checkState(haveNonJumpBranch.contains(source));
                }
            }
            return new BasicFlowGraph<T>(edges, jumpDestinations, start, end);
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

        /**
         * Specify the flow of execution when the jump condition is not met.
         *
         * <p>The jump condition is specified when #linkJumpBranch() is called with
         * the same branchPoint.
         */
        public Builder<T> linkNonJumpBranch(Node<T> branchPoint, Node<T> nonJumpBranch) {
            checkArgument(!haveNonJumpBranch.contains(branchPoint),
                    "Tried to add second Default Branch %s at branch point %s.",
                    nonJumpBranch, branchPoint);
            edges.put(branchPoint, nonJumpBranch);
            haveNonJumpBranch.add(branchPoint);
            return this;
        }

        /** Specify the jump destination, and the condition on which the jump occurs. */
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

        public Builder<T> linkNonJumpBranch(Node<T> branchPoint, FlowGraph<T> nonJumpBranch) {
            copyIn(nonJumpBranch);
            return linkNonJumpBranch(branchPoint, nonJumpBranch.getStart());
        }

        public Builder<T> linkJumpBranch(Node<T> branchPoint, JumpType type, FlowGraph<T> jumpBranch) {
            copyIn(jumpBranch);
            return linkJumpBranch(branchPoint, type, jumpBranch.getStart());
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

        public Builder<T> append(FlowGraph<T> graph) {
            copyIn(graph);
            link(end, graph.getStart());
            end = graph.getEnd();
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

        // There's something strange about this function.  It exists to factor
        // out the ton of common code from #replaceEdgeStart and #replaceEdgeEnd.
        // But, it's not a very natural interface-- something about the only-replace-
        // one-node requirement is odd.  I can't figure out how to make anything more
        // natural, though.
        /**
         * Replaces either the start node or the end node of an edge.
         *
         * <p>Requires that either the start node or the end node is unchanged.
         */
        private void replaceEdge(Node<T> originalStart, Node<T> newStart,
                Node<T> originalEnd, Node<T> newEnd) {
            // This method is only meant to replace the edge's start OR its end.
            // Trying to replace both would be strange, and probably would not be
            // done on purpose.
            checkState(originalStart.equals(newStart) || originalEnd.equals(newEnd));

            checkState(edges.get(originalStart).contains(originalEnd));
            edges.remove(originalStart, originalEnd);
            if (haveNonJumpBranch.contains(originalStart)
                    || jumpDestinations.containsKey(originalStart)) {
                // It's a branch node.
                if (originalEnd.equals(jumpDestinations.get(originalStart))) {
                    JumpDestination<T> destination = jumpDestinations.remove(originalStart);
                    linkJumpBranch(newStart, destination.jumpType, newEnd);
                } else {
                    haveNonJumpBranch.remove(originalStart);
                    linkNonJumpBranch(newStart, newEnd);
                }
            } else {
                link(newStart, newEnd);
            }
        }

        /** Replaces the end node of an edge. */
        private void replaceEdgeEnd(Node<T> start, Node<T> originalEnd, Node<T> newEnd) {
            replaceEdge(start, start, originalEnd, newEnd);
        }

        /** Replaces the start node of an edge. */
        private void replaceEdgeStart(Node<T> end, Node<T> originalStart, Node<T> newStart) {
            replaceEdge(originalStart, newStart, end, end);
        }

        /** Replaces an Node with a FlowGraph. */
        public Builder<T> replace(Node<T> node, FlowGraph<T> replacement) {
            copyIn(replacement);

            // Either this graph is at the beginning, or it needs to be linked up
            // to its predecessors.
            if (node.equals(start)) {
                start = replacement.getStart();
            } else {
                // TODO(jasonpr): Deal with this supreme inefficiency. Maybe
                // use a BiMultiMap lookalike?
                for (Node<T> predecessor : ImmutableMultimap.copyOf(edges).inverse().get(node)) {
                    replaceEdgeEnd(predecessor, node, replacement.getStart());
                }
            }

            // Either this graph is at the end, or it needs to be linked up to
            // its successors.
            if (node.equals(end)) {
                end = replacement.getEnd();
            } else {
                for (Node<T> successor : edges.get(node)) {
                    replaceEdgeStart(successor, node, replacement.getEnd());
                }
            }

            return this;
        }

        public Node<T> getStart() {
            return start;
        }

        public Node<T> getEnd() {
            return end;
        }

        public Builder<T> setStart(Node<T> start) {
            this.start = start;
            return this;
        }

        public Builder<T> setEnd(Node<T> end) {
            this.end = end;
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
            for (Entry<Node<T>, Node<T>> entry : builder.edges.entries()) {
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

        /**
         * Copy all links from a graph into this builder.
         *
         * <p>This method checks that all links are "compatible" with the links that
         * are already present in this graph.  For example, if this graph already contains
         * a jump destination for a node, and the other graph has a different jump
         * destination for the same node, it will throw an AssertionError.
         */
        public Builder<T> copyIn(FlowGraph<T> graph) {
            for (Node<T> source : graph.getNodes()) {
                if (graph.isBranch(source)) {
                    linkNonJumpBranch(source, graph.getNonJumpSuccessor(source));
                    linkJumpBranch(source, graph.getJumpType(source), graph.getJumpSuccessor(source));
                } else if (graph.getSuccessors(source).size() > 0){
                    link(source, Iterables.getOnlyElement(graph.getSuccessors(source)));
                }
            }
            return this;
        }
    }
}
