package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.codegen.ControlFlowNode;

/**
 * A plan for link instructions together into a Control Flow Graph.
 *
 * <p>This interface can be thought of as a control-flow-graph builder, where
 * #linkTo is the #build method.
 *
 * <p>Using ControlLinkers, allows us to plan out how the Control Flow Graph
 * gets linked up, and then actually make that graph all at once, at the end.
 *
 * <p>This allows to keep the benefits of an immutable Control Flow Graph
 * representation, but frees us from the awkwardness of "building it up backwards".
 */
public interface ControlLinker {

    /**
     * Materialize this ControlLinker's plan as a network of ControlFlowNodes.
     *
     * @param sink The node to which to connect the final (last executed) node.
     * @return The head (first executed) node of the materialized network.
     */
    public ControlFlowNode linkTo(ControlFlowNode sink);
}
