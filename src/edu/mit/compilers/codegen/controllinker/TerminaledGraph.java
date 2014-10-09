package edu.mit.compilers.codegen.controllinker;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.instructions.Instruction;

public class TerminaledGraph {
    private final Terminals terminals;
    
    public TerminaledGraph(Terminals terminals) {
        this.terminals = terminals;
    }
    
    public TerminaledGraph(SequentialControlFlowNode beginning, SequentialControlFlowNode end) {
        this(new Terminals(beginning, end));
    }

    public Terminals getTerminals() {
        return terminals;
    }
    
    public static TerminaledGraph sequenceOf(TerminaledGraph first, TerminaledGraph... rest) {
        SequentialControlFlowNode beginning = first.getTerminals().getBeginning();
        
        SequentialControlFlowNode end = first.getTerminals().getBeginning();
        for (TerminaledGraph graph : rest) {
            end.setNext(graph.getTerminals().getBeginning());
            end = graph.getTerminals().getEnd();
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
