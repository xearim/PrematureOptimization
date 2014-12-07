package edu.mit.compilers.common.variableordering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import edu.mit.compilers.common.Variable;

public class VariableOrdering {

	private final Set<OrderedVariable> variables;
	private  Map<Variable,Integer> order;
	private OrderedVariable tail;
	
	private class OrderedVariable{
		
		private final Variable variable;
		private final Optional<OrderedVariable> next;
		
		private OrderedVariable(Variable variable, Optional<OrderedVariable> next){
			this.variable = variable;
			this.next = next;
		}
		
		public OrderedVariable(Variable variable, OrderedVariable next){
			this.variable = variable;
			this.next = Optional.of(next);
		}
		
		public OrderedVariable(Variable variable){
			this(variable, Optional.<OrderedVariable>absent());
		}
		
		public Variable getVariable(){
			return this.variable;
		}
		
		public boolean isHead(){
			return !next.isPresent();
		}
		
		public OrderedVariable getNext(){
			Preconditions.checkState(!isHead());
			return this.next.get();
		}
	}
	
	private VariableOrdering(){
		this.variables = new HashSet<OrderedVariable>();
		this.order = new HashMap<Variable,Integer>();
	}
	
	public static VariableOrdering BasicOrdering(){
		VariableOrdering variableOrder = new VariableOrdering();
		return variableOrder;
	}
	
	public static VariableOrdering BasicOrdering(VariableOrdering order){
		VariableOrdering variableOrder = new VariableOrdering();
		for(OrderedVariable variable : order.getVariables()){
			variableOrder.addVariable(variable.getVariable());
		}
		variableOrder.buildOrdering();
		return variableOrder;
	}
	
	private void addVariable(Variable variable){
		OrderedVariable orderedVariable = variables.isEmpty()
										? new OrderedVariable(variable)
										: new OrderedVariable(variable, tail);
		variables.add(orderedVariable);
		tail = orderedVariable;
	}
	
	private void buildOrdering(){
		int value = 0;
		for(OrderedVariable orderedVariable = tail; 
				!orderedVariable.isHead(); orderedVariable = orderedVariable.getNext()){
			order.put(orderedVariable.getVariable(), value++);
		}
	}
	
	protected Set<OrderedVariable> getVariables(){
		return variables;
	}
	
	public void addToOrdering(Variable variable){
		OrderedVariable orderedVariable = variables.isEmpty()
										? new OrderedVariable(variable)
										: new OrderedVariable(variable, tail);
		variables.add(orderedVariable);
		if(tail == null){
			order.put(variable, 0);
		} else {
			order.put(variable, order.get(tail.getVariable()) + 1);
		}
		tail = orderedVariable;
	}
	
	public int compare(Variable x, Variable y){
		if(x.equals(y)){
			return 0;
		} else {
			return order.get(x) < order.get(y) ? -1 : 1;
		}
	}
	
}
