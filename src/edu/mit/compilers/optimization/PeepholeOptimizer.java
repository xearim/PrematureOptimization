package edu.mit.compilers.optimization;

import java.util.ArrayDeque;
import java.util.Queue;

import edu.mit.compilers.codegen.asm.Value;
import edu.mit.compilers.codegen.asm.instructions.Add;
import edu.mit.compilers.codegen.asm.instructions.And;
import edu.mit.compilers.codegen.asm.instructions.Call;
import edu.mit.compilers.codegen.asm.instructions.Compare;
import edu.mit.compilers.codegen.asm.instructions.CompareFlagged;
import edu.mit.compilers.codegen.asm.instructions.Decrement;
import edu.mit.compilers.codegen.asm.instructions.Enter;
import edu.mit.compilers.codegen.asm.instructions.Increment;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.asm.instructions.Instructions;
import edu.mit.compilers.codegen.asm.instructions.Jump;
import edu.mit.compilers.codegen.asm.instructions.JumpTyped;
import edu.mit.compilers.codegen.asm.instructions.Leave;
import edu.mit.compilers.codegen.asm.instructions.Modulo;
import edu.mit.compilers.codegen.asm.instructions.Move;
import edu.mit.compilers.codegen.asm.instructions.MoveFromMemory;
import edu.mit.compilers.codegen.asm.instructions.MovePointer;
import edu.mit.compilers.codegen.asm.instructions.MoveScalar;
import edu.mit.compilers.codegen.asm.instructions.MoveToMemory;
import edu.mit.compilers.codegen.asm.instructions.Negate;
import edu.mit.compilers.codegen.asm.instructions.Not;
import edu.mit.compilers.codegen.asm.instructions.Or;
import edu.mit.compilers.codegen.asm.instructions.Pop;
import edu.mit.compilers.codegen.asm.instructions.Push;
import edu.mit.compilers.codegen.asm.instructions.Return;
import edu.mit.compilers.codegen.asm.instructions.SignedDivide;
import edu.mit.compilers.codegen.asm.instructions.SignedMultiply;
import edu.mit.compilers.codegen.asm.instructions.Subtract;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

public class PeepholeOptimizer {
	
	private FlowGraph<Instruction> ig;
	
	public PeepholeOptimizer(FlowGraph<Instruction> ig){
		this.ig = ig;
	}

	public FlowGraph<Instruction> optimize(){
		// Make a new builder for our optimizer
		BasicFlowGraph.Builder<Instruction> optimizedGraph = BasicFlowGraph.builder();
		// Add the original graph on in
		optimizedGraph.append(ig);
		optimizedGraph.removeNops();
		
		Queue<Node<Instruction>> instructionSet = new ArrayDeque<Node<Instruction>>();
		instructionSet.add(ig.getStart());
		
		Queue<Node<Instruction>> instructionWindow = new ArrayDeque<Node<Instruction>>();
		
		while(!instructionSet.isEmpty()){
			Node<Instruction> node = instructionSet.remove();
			instructionWindow.clear();
			instructionWindow.addAll(ig.getSuccessors(node));
			if(node.hasValue()){
				
				if(isPushPop(node, instructionWindow)){
					optimizedGraph.replace(node, 
							BasicFlowGraph.<Instruction>builder()
	                        .append(Instructions.move(((Push)node.value()).getArguement(),
	                        		((Pop)instructionWindow.peek().value()).getArguement()))
	                        .build());
					optimizedGraph.replace(instructionWindow.peek(), 
							BasicFlowGraph.<Instruction>builder()
	                        .append(Node.<Instruction>nop())
	                        .build());
					optimizedGraph.removeNops();
				}
				
				if(isReflexiveMove(node)){
					optimizedGraph.replace(instructionWindow.peek(), 
							BasicFlowGraph.<Instruction>builder()
	                        .append(Node.<Instruction>nop())
	                        .build());
					optimizedGraph.removeNops();
				}
				
			}
		}
		
		return optimizedGraph.build();
	}
	
	private boolean isPushPop(Node<Instruction> node, Queue<Node<Instruction>> iWindow){
		if(iWindow.size() == 1){
			if((node.value() instanceof Push) && (iWindow.peek().value() instanceof Pop)){
				return true;
			} else if((node.value() instanceof Push)){
				iWindow.addAll(ig.getSuccessors(iWindow.remove()));
				for(Node<Instruction> followingNode : iWindow){
					if(targetsValue(((Push) node.value()).getArguement(), followingNode)){
						return false;
					}
				}
				return isPushPop(node, iWindow);
			}
		}
		return false;
	}
	
	private boolean isReflexiveMove(Node<Instruction> node){
		if(node.value() instanceof Move){
			return ((Move) node.value()).getSource().equals(((Move) node.value()).getDest());
		}
		return false;
	}
	
	private boolean targetsValue(Value value, Node<Instruction> node){
		if(node.hasValue()){
			if(node.value() instanceof Add){
				return ((Add) node.value()).getRightArgument().equals(value);
			} else if(node.value() instanceof And){
				return ((And) node.value()).getRightArgument().equals(value);
			} else if(node.value() instanceof Call){
				return true;
			} else if(node.value() instanceof Compare){
				return ((Compare) node.value()).getRightArgument().equals(value);
			} else if(node.value() instanceof CompareFlagged){
				return false;
			} else if(node.value() instanceof Decrement){
				return ((Decrement) node.value()).getArgument().equals(value);
			} else if(node.value() instanceof Enter){
				return true;
			} else if(node.value() instanceof Increment){
				return ((Increment) node.value()).getArgument().equals(value);
			} else if(node.value() instanceof Jump){
				return false;
			} else if(node.value() instanceof JumpTyped){
				return false;
			} else if(node.value() instanceof Leave){
				return true;
			} else if(node.value() instanceof Modulo){
				return ((Modulo) node.value()).getRightArgument().equals(value);
			} else if(node.value() instanceof Move){
				return ((Move) node.value()).getDest().equals(value);
			} else if(node.value() instanceof MoveFromMemory){
				return ((MoveFromMemory) node.value()).getDestination().equals(value);
			} else if(node.value() instanceof MovePointer){
				return ((MovePointer) node.value()).getDest().equals(value);
			} else if(node.value() instanceof MoveScalar){
				return ((MoveScalar) node.value()).getTarget().equals(value);
			} else if(node.value() instanceof MoveToMemory){
				return false;
			} else if(node.value() instanceof Negate){
				return ((Negate) node.value()).getArgument().equals(value);
			} else if(node.value() instanceof Not){
				return ((Not) node.value()).getArgument().equals(value);
			} else if(node.value() instanceof Or){
				return ((Or) node.value()).getRightArgument().equals(value);
			} else if(node.value() instanceof Pop){
				return false;
			} else if(node.value() instanceof Push){
				return false;
			} else if(node.value() instanceof Return){
				return true;
			} else if(node.value() instanceof SignedDivide){
				return ((SignedDivide) node.value()).getRightArgument().equals(value);
			} else if(node.value() instanceof SignedMultiply){
				return ((SignedMultiply) node.value()).getRightArgument().equals(value);
			} else if(node.value() instanceof Subtract){
				return ((Subtract) node.value()).getRightArgument().equals(value);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}
