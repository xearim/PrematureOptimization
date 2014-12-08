package edu.mit.compilers.optimization;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.ScopeType;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.graph.BasicFlowGraph;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.graph.Node;

/**
 * Preforms CSE (common subexpression elimination).
 *
 * <p>Currently, subexpressions aren't eliminated-- only full expressions are eliminated.
 * That is, if (x + y) is available, we will not (yet!) optimize (1 + x + y).
 *
 * <p>This should really be a non-instantiable class with a static optimize method, but
 * it needs to be instantiable to conform to DataFlowOptimizer's interface.
 */
public class CommonExpressionEliminator implements DataFlowOptimizer {

    private static final String TEMP_VAR_PREFIX = "cse_temp";

    @Override
    public DataFlowIntRep optimized(DataFlowIntRep ir) {
        return new Eliminator(ir).optimized();
    }

    // TODO(jasonpr): Figure out why this helper class feels so hacky, and unhack it.
    /**
     * Helper class for actually performing the optimization.
     *
     * <p>It allows us to store the AvailabilityCalculator and some other
     * values in instance variables, so we don't need to keep passing them
     * around as parameters.
     *
     */
    private static final class Eliminator {
        private final DataFlowIntRep ir;
        private final BcrFlowGraph<ScopedStatement> dataFlowGraph;
        // TODO(jasonpr): Use ScopedExpression, not NativeExpression.
        private final Map<NativeExpression, Variable> tempVars;
        private final Multimap<Node<ScopedStatement>, ScopedExpression> inSets;

        public Eliminator(DataFlowIntRep ir) {
            this.ir = ir;
            this.dataFlowGraph = ir.getDataFlowGraph();
            this.tempVars = tempVars(expressions(dataFlowGraph));
            inSets = DataFlowAnalyzer.AVAILABLE_EXPRESSIONS.calculate(
                    ir.getDataFlowGraph());
        }

        public DataFlowIntRep optimized() {
            // When we make an optimized DFG, we produce modified copies of some original scopes.
            // When we make those copies, we need modify the children of those scopes, etc.
            Set<Node<ScopedStatement>> replaceable = replacable(dataFlowGraph.getNodes());
            Set<Scope> allScopes = Util.reachableScopes(replaceable);
            Multimap<Scope, Scope> scopeTree = Util.scopeTree(allScopes);
            Multimap<Scope, Variable> scopeAugmentations =
                    scopeAugmentations(tempVars.values(), ir.getScope());
            // Maps each scope to the "new" version of itself.  The new version may have
            // temp space allocated, and its parent pointer points to the new version of
            // its parent.
            Map<Scope, Scope> augmentedScopes =
                    newScopes(scopeTree, scopeAugmentations, ir.getScope());

            BcrFlowGraph.Builder<ScopedStatement> statementBuilder =
                    BcrFlowGraph.builderOf(dataFlowGraph);
            
            for (Node<ScopedStatement> node : replaceable) {
                Set<NativeExpression> toUseTemp = new HashSet<NativeExpression>();
                Set<NativeExpression> toFillAndUseTemp = new HashSet<NativeExpression>();
                /*
                for (NativeExpression expr : Util.nodeExprs(node.value())) {
                    if (!isComplexEnough(expr)) {
                        continue;
                    }
                    if (isAvailable(expr, node)) {
                        toUseTemp.add(expr);
                    } else {
                        // For now, we always generate if it's not available.
                        toFillAndUseTemp.add(expr);
                    } 
                }*/
                statementBuilder.replace(node, doReplacements(node.value(), toUseTemp,
                        toFillAndUseTemp, augmentedScopes.get(node.value().getScope())));
            }

            return new DataFlowIntRep(
                    statementBuilder.build(), ir.getScope());
        }

        /** Return whether an expression is available at a DataFlowNode. */
        private boolean isAvailable(GeneralExpression expr, Node<ScopedStatement> node) {
            if (!(expr instanceof NativeExpression)) {
                // Only NativeExpressions are ever available.
                return false;
            }

            if (!node.hasValue()) {
                return false;
            }

            ScopedExpression scopedExpr = new ScopedExpression(
                    (NativeExpression) expr, node.value().getScope());

            for(ScopedExpression ex : inSets.get(node)){
                if(ex.equals(scopedExpr)){
                    return true;
                }
            }
            return false;
        }

        private FlowGraph<ScopedStatement> doReplacements(ScopedStatement scopedStatement,
                Collection<NativeExpression> toUseTemp,
                Collection<NativeExpression> toFillAndUseTemp, Scope newScope) {
            // For now, only allow one temp use or one temp fill.
            // There are some added complexities for doing multiple uses and/or fills on a single
            // statement.  Since the CommonExpressionElminator doesn't ever request multiple
            // uses/fills, we don't want to devote time to unraveling those complexities, yet.
            checkState(toUseTemp.size() == 0 || toFillAndUseTemp.size() == 0);
            checkState(toUseTemp.size() <= 1);
            checkState(toFillAndUseTemp.size() <= 1);
            if (toUseTemp.size() == 1) {
                return useTemp(scopedStatement, Iterables.getOnlyElement(toUseTemp), newScope); 
            } else if (toFillAndUseTemp.size() == 1) {
                return fillAndUseTemp(scopedStatement,
                        Iterables.getOnlyElement(toFillAndUseTemp), newScope);
            } else {
                return BasicFlowGraph.<ScopedStatement>builder()
                        .append(new ScopedStatement(scopedStatement.getStatement(), newScope))
                        .build();
            }
        }


        /**
         * Return a replacement for 'node' that does not call for 'expr' to be
         * evaluated, but instead loads its value from the corresponding temp
         * variable.
         *
         * <p>Requires that the expression is available at the node.
         */
        private FlowGraph<ScopedStatement> useTemp(
                ScopedStatement node, GeneralExpression expr, Scope newScope) {
            Preconditions.checkState(tempVars.containsKey(expr));
            Preconditions.checkState(node.getStatement().hasExpression());

            Location temp = new ScalarLocation(tempVars.get(expr), LocationDescriptor.machineCode());
            StaticStatement newStatement = Util.getReplacement(node.getStatement(), temp);

            return BasicFlowGraph.<ScopedStatement>builder()
                    .append(new ScopedStatement(newStatement, newScope))
                    .build();
        }

        /**
         * Return a replacement for 'node' that stores the value of 'expr' in
         * its temp variable, and uses that temp variable when executing the
         * node's statement.
         */
        private FlowGraph<ScopedStatement>
                fillAndUseTemp(ScopedStatement node, NativeExpression expr, Scope newScope) {
            // The node to replace should actually contain statements
            Preconditions.checkState(tempVars.containsKey(expr));
            Preconditions.checkState(node.getStatement().hasExpression());

            Location temp = new ScalarLocation(
                    tempVars.get(expr), LocationDescriptor.machineCode());

            Assignment newTemp = Assignment.compilerAssignment(temp, expr);
            StaticStatement newStatement = Util.getReplacement(node.getStatement(), temp);

            return BasicFlowGraph.<ScopedStatement>builder()
                    .append(new ScopedStatement(newTemp, newScope))
                    .append(new ScopedStatement(newStatement, newScope))
                    .build();
        }


    }

    /** Reject nodes that can never have their (sub)expressions replaced. */
    private static Set<Node<ScopedStatement>> replacable(Iterable<Node<ScopedStatement>> nodes) {
        ImmutableSet.Builder<Node<ScopedStatement>> replacable = ImmutableSet.builder();
        for (Node<ScopedStatement> node : nodes) {
            // NOPs are unoptimizable!
            // We do not optimize method calls, as they may not be idempotent.
            if (node.hasValue() && !(node.value().getStatement() instanceof MethodCall)) {
                replacable.add(node);
            }
        }
        return replacable.build();
    }

    /**
     * Generate a map from expression to temporary variable, for some expressions.
     *
     * <p>Non-native expressions are ignored-- we can't store them in variables!
     */
    private static Map<NativeExpression, Variable> tempVars(Iterable<NativeExpression> exprs) {
        ImmutableMap.Builder<NativeExpression, Variable> builder = ImmutableMap.builder();
        int tempNumber = 0;
        for (NativeExpression expr : exprs) {
            builder.put(expr, Variable.forCompiler(TEMP_VAR_PREFIX + tempNumber++));
        }
        return builder.build();
    }

    /** Maps each scope to the new temp variables it will need to contain. */
    private static Multimap<Scope, Variable>
            scopeAugmentations(Iterable<Variable> variables, Scope methodScope) {
        // For now, all temps are stored in the method scope.  It's easier that way.
        // We could do something fancier, but it only buys us a smaller stack.
        return ImmutableMultimap.<Scope,Variable>builder().putAll(methodScope, variables).build();
    }

    /**
     * Makes a map of original scopes to new, augmented scopes.
     *
     * @param oldScopeTree The (parent -> child) edges of the original tree of scopes.
     * @param scopeAugmentations The new temp variables to add to each scope.
     * @param methodScope The original method scope.  (The scope right under the PARAMETER scope.)
     */
    private static Map<Scope, Scope> newScopes(Multimap<Scope, Scope> oldScopeTree,
            Multimap<Scope, Variable> scopeAugmentations, Scope methodScope) {
        Map<Scope, Scope> newScopes = new HashMap<Scope, Scope>();
        Scope parameterScope = methodScope.getParent().get();
        checkState(parameterScope.getScopeType() == ScopeType.PARAMETER);
        Scope globalScope = parameterScope.getParent().get();
        checkState(globalScope.getScopeType() == ScopeType.GLOBAL);

        // We don't duplicate these higher-than-method scopes.  Map them to themselves.
        newScopes.put(parameterScope, parameterScope);
        newScopes.put(globalScope, globalScope);
        
        // We are going to put our variables in a scope between the method and the body
        ImmutableList.Builder<FieldDescriptor> fieldDescs =
                ImmutableList.<FieldDescriptor>builder();
        for (Variable newVar : scopeAugmentations.get(methodScope)) {
            fieldDescs.add(new FieldDescriptor(newVar, BaseType.WILDCARD));
        }
        newScopes.put(methodScope, new Scope(
                fieldDescs.build(),
                methodScope));

        for (Scope methodChild : oldScopeTree.get(methodScope)) {
            addScopeTree(oldScopeTree, newScopes, scopeAugmentations, methodChild);
        }
        return ImmutableMap.copyOf(newScopes);
    }

    /**
     * Adds new mappings for the scope tree under 'current' to the 'newScopes' map.
     *
     * <p>This is a helper method for newScopes.
     */
    private static void addScopeTree(Multimap<Scope, Scope> oldScopeTree,
            Map<Scope, Scope> newScopes, Multimap<Scope, Variable> scopeAugmentations,
            Scope current) {
    	newScopes.put(current, 
    			new Scope(current.getVariables(), newScopes.get(current.getParent().get()), current.isLoop()));
        for (Scope methodChild : oldScopeTree.get(current)) {
            addScopeTree(oldScopeTree, newScopes, scopeAugmentations, methodChild);
        }
    }

    /** Get all the optimizable expressions from some nodes. */
    private static Iterable<NativeExpression> expressions(FlowGraph<ScopedStatement> dataFlowGraph) {
        ImmutableSet.Builder<NativeExpression> builder = ImmutableSet.builder();
        for (Node<ScopedStatement> node : dataFlowGraph.getNodes()) {
            if (node.hasValue()) {
                builder.addAll(Util.nodeExprs(node.value()));
            }
        }
        return builder.build();
    }

    /**
     * Determines if a NativeExpression is complex enough to be worth saving.
     *
     * <p>Does not check for MethodCalls inside of GeneralExpression.
     *
     * <p>Any GeneralExpression that passes this check may be considered a
     * NativeExpression.
     */
    private static boolean isComplexEnough(GeneralExpression ge) {
        return (ge instanceof BinaryOperation)
                || (ge instanceof MethodCall)
                || (ge instanceof TernaryOperation)
                || (ge instanceof UnaryOperation);
    }
}
