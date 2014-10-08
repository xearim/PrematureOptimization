package edu.mit.compilers.codegen;

import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


public class SequentialControlFlowNode implements ControlFlowNode {

    private final Optional<ControlFlowNode> next;

    // TODO(manny): Figure out if it's feasible to pass "next" as a parameter.
    // It would force us to build up sequences of nodes "backwards", which may
    // or may not be ugly.
    private SequentialControlFlowNode(Optional<ControlFlowNode> next) {
        this.next = next;
    }

    public static SequentialControlFlowNode withNext(ControlFlowNode next) {
        return new SequentialControlFlowNode(Optional.of(next));
    }
    
    public static SequentialControlFlowNode terminal() {
        return new SequentialControlFlowNode(Optional.<ControlFlowNode>absent());
    }
    
    public boolean hasNext() {
        return next.isPresent();
    }
    
    /**
     * Gets the next node.
     * 
     * <p>Throws IllegalStateExcpetion if there is no next node.
     */
    public ControlFlowNode getNext() {
        return next.get();
    }

    @Override
    public Collection<ControlFlowNode> getSinks() {
        return hasNext()
                ? ImmutableList.<ControlFlowNode>of(getNext())
                : ImmutableList.<ControlFlowNode>of();
    }
}
