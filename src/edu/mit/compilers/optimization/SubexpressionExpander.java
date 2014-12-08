package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

public class SubexpressionExpander implements DataFlowOptimizer {
	private static final String TEMP_VAR_PREFIX = "see_temp";

    @Override
    public DataFlowIntRep optimized(DataFlowIntRep ir) {
        return new Expander(ir).optimized();
    }

    private static final class Expander {
        private final DataFlowIntRep ir;
        private final BcrFlowGraph<ScopedStatement> dataFlowGraph;
        private int tempNumber = 0;

        public Expander(DataFlowIntRep ir) {
            this.ir = ir;
            this.dataFlowGraph = ir.getDataFlowGraph();
        }
        
        private boolean isExpandable(NativeExpression expr){
        	if(expr instanceof BinaryOperation){
        		return Util.isNested(expr) &&
        			   !((((BinaryOperation) expr).getOperator() == BinaryOperator.AND) ||
        			   	 (((BinaryOperation) expr).getOperator() == BinaryOperator.OR));
        	} else {
        		return Util.isNested(expr);
        	}
  
        }
        
        private Queue<Node<ScopedStatement>> expandable(Iterable<Node<ScopedStatement>> nodes) {
            Queue<Node<ScopedStatement>> expandable = new LinkedList<Node<ScopedStatement>>();
            for (Node<ScopedStatement> node : nodes) {
                // NOPs are not expandable
            	// And we only care to expand nodes which both
            	// Contain expressions
            	// And who's expressions are in some form nested
                if (node.hasValue() && node.value().getStatement().hasExpression() && 
                		isExpandable(node.value().getStatement().getExpression())){
                    expandable.add(node);
                }
            }
            return expandable;
        }
        
        /** Produces a preliminary map from old scopes to new scopes */
        private Map<Scope, Scope> newScopes(Multimap<Scope, Scope> oldScopeTree, Scope root) {
        	Map<Scope, Scope> oldToNewScopes = new HashMap<Scope, Scope>();
        	Scope parameterScope = root.getParent().get();
            checkState(parameterScope.getScopeType() == ScopeType.PARAMETER);
            Scope globalScope = parameterScope.getParent().get();
            checkState(globalScope.getScopeType() == ScopeType.GLOBAL);

            // We don't duplicate these higher-than-method scopes.  Map them to themselves.
            oldToNewScopes.put(parameterScope, parameterScope);
            oldToNewScopes.put(globalScope, globalScope);

            newScopesHelper(oldScopeTree, oldToNewScopes, root);
            return oldToNewScopes;
        }
        
        /** A helper function for recursively building the scope map */
        private void newScopesHelper(
        		Multimap<Scope, Scope> oldScopeTree, Map<Scope, Scope> oldToNewScopes, Scope current) {
        	oldToNewScopes.put(current, 
        			new Scope(current.getVariables(), oldToNewScopes.get(current.getParent().get()), current.isLoop()));
        	for(Scope child: oldScopeTree.get(current)){
        		newScopesHelper(oldScopeTree, oldToNewScopes, child);
        	}
        }
        
        private Map<Scope, Scope> fixScopes(
        		Multimap<Scope, Scope> oldScopeTree, Map<Scope, Scope> scopeSet, Scope root) {
        	Map<Scope, Scope> oldToNewScopes = new HashMap<Scope, Scope>();
        	Scope parameterScope = root.getParent().get();
            checkState(parameterScope.getScopeType() == ScopeType.PARAMETER);
            Scope globalScope = parameterScope.getParent().get();
            checkState(globalScope.getScopeType() == ScopeType.GLOBAL);

            // We don't duplicate these higher-than-method scopes.  Map them to themselves.
            oldToNewScopes.put(parameterScope, parameterScope);
            oldToNewScopes.put(globalScope, globalScope);

            fixScopesHelper(oldScopeTree, oldToNewScopes, scopeSet, root);
            return oldToNewScopes;
        }
        
        private void fixScopesHelper(
        		Multimap<Scope, Scope> oldScopeTree, Map<Scope, Scope> oldToNewScopes, 
        		Map<Scope, Scope> scopeSet, Scope current) {
        	oldToNewScopes.put(current, 
        			new Scope(scopeSet.get(current).getVariables(), oldToNewScopes.get(current.getParent().get()), scopeSet.get(current).isLoop()));
        	for(Scope child: oldScopeTree.get(current)){
        		fixScopesHelper(oldScopeTree, oldToNewScopes, scopeSet, child);
        	}
        }

        public DataFlowIntRep optimized() {
        	Queue<Node<ScopedStatement>> expandable = expandable(dataFlowGraph.getNodes());
            Set<Scope> allScopes = Util.reachableScopes(dataFlowGraph.getNodes());
            Multimap<Scope, Scope> scopeTree = Util.scopeTree(allScopes);
            // Maps each scope to the "new" version of itself. This map will be updated as we expand subexpressions
            // This means that this map is not immutable, this makes the process a bit easier for this part
            Map<Scope, Scope> oldToNewScopes = newScopes(scopeTree, ir.getScope());

            BcrFlowGraph.Builder<ScopedStatement> statementBuilder =
            		BcrFlowGraph.builderOf(dataFlowGraph);
            
            while ( !expandable.isEmpty() ) {
            	Node<ScopedStatement> node = expandable.remove();
            	// expandable only contains nodes whos statements are existant and expandable
                NativeExpression expr = node.value().getStatement().getExpression();
                for(GeneralExpression subexpr : expr.getChildren()){
                	if(Util.isNested(subexpr) || !Util.isBaseExpression(subexpr)){
                		FlowGraph<ScopedStatement> replacedSubexpr =
                				replaceSubexpr(node.value(), (NativeExpression) subexpr, oldToNewScopes);
                		statementBuilder.replace(node, replacedSubexpr);
                		expandable.addAll(expandable(replacedSubexpr.getNodes()));
                		break;
                	}
                }
            }
            oldToNewScopes = fixScopes(scopeTree, oldToNewScopes, ir.getScope());
            BcrFlowGraph<ScopedStatement> expandedGraph = statementBuilder.build();
            BcrFlowGraph.Builder<ScopedStatement> statementFinalizer =
            		BcrFlowGraph.builderOf(expandedGraph);
            for(Node<ScopedStatement> node : expandedGraph.getNodes()){
            	if(node.hasValue()){
            		statementFinalizer.replace(node, 
                			BasicFlowGraph.<ScopedStatement>builder()
                            .append(
                            		new ScopedStatement(node.value().getStatement(), oldToNewScopes.get(node.value().getScope())))
                            .build());
            	}
            }

            return new DataFlowIntRep(
                    statementFinalizer.build(), oldToNewScopes.get(ir.getScope()));
        }

        private FlowGraph<ScopedStatement> replaceSubexpr(ScopedStatement scopedStatement,
                NativeExpression replacementTarget, Map<Scope, Scope> oldToNewScopes) {
        	// Create a temp variable for this subexpression
        	Variable tempVar = Variable.forCompiler(TEMP_VAR_PREFIX + tempNumber++);
        	// Create the location to store this temp variable
        	Location temp = new ScalarLocation(tempVar, LocationDescriptor.machineCode());
        	// Create the new scope with this temp variable injected
        	Scope newScope = Util.augmented(
        			oldToNewScopes.get(scopedStatement.getScope()), 
        			ImmutableSet.of(tempVar), 
        			oldToNewScopes.get(scopedStatement.getScope().getParent().get()));
        	// Make sure that we create the chain reflecting that this scope is the new version of the old scope
        	oldToNewScopes.put(scopedStatement.getScope(), newScope);

        	// Construct the new 2 part assignment with injected temp
            Assignment newTemp = Assignment.compilerAssignment(temp, replacementTarget);

            // Replace the sub expression with the temp to get a new expression
            NativeExpression newExpression = Util.getReplacement(scopedStatement.getStatement().getExpression(), replacementTarget, temp);

            // Use that new expression in the statement, in the place of the old expression
            StaticStatement newStatement = Util.getReplacement(scopedStatement.getStatement(), newExpression);
            
            // Note we are still going to use the old scopes, we will do a big replacement at the end to update all the scopes
            return BasicFlowGraph.<ScopedStatement>builder()
                    .append(new ScopedStatement(newTemp, scopedStatement.getScope()))
                    .append(new ScopedStatement(newStatement, scopedStatement.getScope()))
                    .build();
        }


    }
   
}

