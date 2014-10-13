package edu.mit.compilers.codegen.controllinker;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.instructions.Instruction;

// TODO(jasonpr): Determine whether we need to specify this begin/end fact.
// I'm not sure we're ever going to represent any graph that doesn't satisfy that condition.
/** A control flow graph with a a beginning node and and end node. */
public class TerminaledGraph {
    private final ControlFlowNode beginning;
    private final SequentialControlFlowNode end;

    public TerminaledGraph(ControlFlowNode beginning, SequentialControlFlowNode end) {
        this.beginning = beginning;
        this.end = end;
    }

    public ControlFlowNode getBeginning() {
        return beginning;
    }
    
    public SequentialControlFlowNode getEnd() {
        return end;
    }
    
    public static TerminaledGraph sequenceOf(TerminaledGraph first, TerminaledGraph... rest) {
        ControlFlowNode beginning = first.getBeginning();
        SequentialControlFlowNode end = first.getEnd();

        for (TerminaledGraph graph : rest) {
            end.setNext(graph.getBeginning());
            end = graph.getEnd();
        }
        
        return new TerminaledGraph(beginning, end);
    }
    
    public static TerminaledGraph sequenceOf(GraphFactory first, GraphFactory... rest) {
        TerminaledGraph[] graphs = new TerminaledGraph[rest.length];
        for (int i = 0; i < rest.length; i++) {
            graphs[i] = rest[i].getGraph();
        }
        
        return sequenceOf(first.getGraph(), graphs);        
    }

    public static TerminaledGraph ofInstructions(Instruction... instructions) {
        List<Instruction> instructionsList = ImmutableList.copyOf(instructions);
        SequentialControlFlowNode end = SequentialControlFlowNode.nopTerminal();
        
        SequentialControlFlowNode beginning = end;
        for (Instruction instr : Lists.reverse(instructionsList)) {
            beginning = SequentialControlFlowNode.WithNext(instr, beginning);
        }

        return new TerminaledGraph(beginning, end);
    }
}
