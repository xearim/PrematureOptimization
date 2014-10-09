package edu.mit.compilers.codegen.controllinker;

import edu.mit.compilers.codegen.SequentialControlFlowNode;

// TODO(jasonpr): Determine whether to eliminate this, and just let TerminaledGraph
// have a beginning and an end.
public class Terminals {
    private final SequentialControlFlowNode beginning;
    private final SequentialControlFlowNode end;
    
    public Terminals(SequentialControlFlowNode beginning, SequentialControlFlowNode end) {
        this.beginning = beginning;
        this.end = end;
    }
    
    public SequentialControlFlowNode getBeginning() {
        return beginning;
    }
    
    public SequentialControlFlowNode getEnd() {
        return end;
    }
}
