package edu.mit.compilers.codegen.controllinker;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.mit.compilers.codegen.BranchingControlFlowNode;
import edu.mit.compilers.codegen.ControlFlowNode;
import edu.mit.compilers.codegen.SequentialControlFlowNode;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

// TODO(jasonpr): Determine whether we need to specify this begin/end fact.
// I'm not sure we're ever going to represent any graph that doesn't satisfy that condition.
/** A control flow graph with a a beginning node and and end node. */
@Deprecated
public class BiTerminalGraph {
    private final ControlFlowNode beginning;
    private final SequentialControlFlowNode end;

    public BiTerminalGraph(ControlFlowNode beginning, SequentialControlFlowNode end) {
        this.beginning = beginning;
        this.end = end;
    }

    public ControlFlowNode getBeginning() {
        return beginning;
    }
    
    public SequentialControlFlowNode getEnd() {
        return end;
    }
    
    public static BiTerminalGraph sequenceOf(BiTerminalGraph first, BiTerminalGraph... rest) {
        ControlFlowNode beginning = first.getBeginning();
        SequentialControlFlowNode end = first.getEnd();

        for (BiTerminalGraph graph : rest) {
            end.setNext(graph.getBeginning());
            end = graph.getEnd();
        }
        
        return new BiTerminalGraph(beginning, end);
    }
    
    public static BiTerminalGraph sequenceOf(GraphFactory first, GraphFactory... rest) {
        BiTerminalGraph[] graphs = new BiTerminalGraph[rest.length];
        for (int i = 0; i < rest.length; i++) {
            graphs[i] = rest[i].getGraph();
        }
        
        return sequenceOf(first.getGraph(), graphs);        
    }

    public static BiTerminalGraph ofInstructions(Instruction... instructions) {
        List<Instruction> instructionsList = ImmutableList.copyOf(instructions);
        
        SequentialControlFlowNode end = SequentialControlFlowNode.namedNop("BiTerminal End");
        SequentialControlFlowNode currentHead = end;

        for (Instruction instr : Lists.reverse(instructionsList)) {
            currentHead = SequentialControlFlowNode.WithNext(instr, currentHead);
        }
        SequentialControlFlowNode beginning = SequentialControlFlowNode.namedNop("BiTerminal Beginning");
        beginning.setNext(currentHead);
        return new BiTerminalGraph(beginning, end);
    }

    /**
     * Get the FlowGraph representation of this BiTerminalGraph.
     *
     * <p>BiTerminalGraphs are now deprecated in favor of FlowGraphs.  This
     * method exists to help us transition.
     */
    public FlowGraph<Instruction> asFlowGraph() {
        BasicFlowGraph.Builder<Instruction> flowGraphBuilder = BasicFlowGraph.builder();
        // The newNode corresponding to the parent of a node.
        Map<ControlFlowNode, Node<Instruction>> newNodes =
                new HashMap<ControlFlowNode, Node<Instruction>>();
        newNodes.put(beginning, newNode(beginning));

        // Do DFS over the BiTerminalGraph.  For each new node encountered, add its
        // outgoing edges to the flow graph.
        Set<ControlFlowNode> visited = new HashSet<ControlFlowNode>();
        Deque<ControlFlowNode> agenda = new ArrayDeque<ControlFlowNode>();

        agenda.push(beginning);
        while(!agenda.isEmpty()) {
            ControlFlowNode node = agenda.pop();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);

            if (node instanceof SequentialControlFlowNode) {
                SequentialControlFlowNode seqNode = (SequentialControlFlowNode) node;
                if (seqNode.hasNext()){
                    ControlFlowNode next = seqNode.getNext();
                    agenda.push(next);

                    if (!newNodes.containsKey(next)) {
                        newNodes.put(next, newNode(next));
                    }
                    flowGraphBuilder.link(newNodes.get(node), newNodes.get(next));
                }
            } else if (node instanceof BranchingControlFlowNode) {
                BranchingControlFlowNode branchNode = (BranchingControlFlowNode) node;

                ControlFlowNode nonJump = branchNode.getTrueBranch();
                agenda.push(nonJump);
                if (!newNodes.containsKey(nonJump)) {
                    newNodes.put(nonJump, newNode(nonJump));
                }
                flowGraphBuilder.linkNonJumpBranch(newNodes.get(node), newNodes.get(nonJump));

                ControlFlowNode jump = branchNode.getFalseBranch();
                agenda.push(jump);
                if (!newNodes.containsKey(jump)) {
                    newNodes.put(jump, newNode(jump));
                }
                flowGraphBuilder.linkJumpBranch(newNodes.get(node),
                        branchNode.getType(), newNodes.get(jump));
            } else {
                throw new AssertionError("Unexpected node type for " + node);
            }
        }

        flowGraphBuilder.setStart(newNodes.get(beginning));
        flowGraphBuilder.setEnd(newNodes.get(end));

        return flowGraphBuilder.build();
    }

    private Node<Instruction> newNode(ControlFlowNode node) {
        if (node instanceof SequentialControlFlowNode) {
            SequentialControlFlowNode seqNode = (SequentialControlFlowNode) node;
            if (seqNode.hasInstruction()) {
                return Node.of(seqNode.getInstruction());
            } else {
                return Node.nop();
            }
        } else if (node instanceof BranchingControlFlowNode) {
            return Node.nop();
        } else {
            throw new AssertionError("Unexpected node type for " + node);
        }
    }
}
