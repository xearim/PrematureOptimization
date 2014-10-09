package edu.mit.compilers.codegen.controllinker;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import edu.mit.compilers.codegen.ControlFlowNode;

/** A plan to sequentially execute some sequences of instructions. */
public class SequentialControlLinker implements ControlLinker{
    private final List<ControlLinker> steps;
    
    public SequentialControlLinker() {
        this.steps = new ArrayList<ControlLinker>();
    }

    /** Add a subplan to the end of the current plan. */
    public SequentialControlLinker append(ControlLinker step) {
        steps.add(step);
        return this;
    }   

    @Override
    public ControlFlowNode linkTo(ControlFlowNode sink) {
        ControlFlowNode head = sink;
        for (ControlLinker step : Lists.reverse(steps)) {
            head = step.linkTo(head);
        }
        return head;
    }
}
