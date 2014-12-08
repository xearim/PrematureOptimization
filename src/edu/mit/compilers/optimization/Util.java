package edu.mit.compilers.optimization;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.Condition;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.ReturnStatement;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.graph.Node;

public class Util {
    private Util() {}

    /**
     * Returns true is there is a MethodCall in any part of the
     * GeneralExpression.
     */
    public static boolean containsMethodCall(GeneralExpression ge) {
        if (ge instanceof BinaryOperation) {
            return containsMethodCall( ((BinaryOperation) ge).getLeftArgument())
                    || containsMethodCall( ((BinaryOperation) ge).getRightArgument());
        }  else if (ge instanceof MethodCall) {
            return true;
        } else if (ge instanceof TernaryOperation) {
            return containsMethodCall( ((TernaryOperation) ge).getCondition())
                    || containsMethodCall( ((TernaryOperation) ge).getTrueResult())
                    || containsMethodCall( ((TernaryOperation) ge).getFalseResult());
        } else if (ge instanceof UnaryOperation) {
            return containsMethodCall( ((UnaryOperation) ge).getArgument());
        } else {
            return false;
        }
    }

    /**
     * Get all the global variables.
     *
     * @param scope Any scope in the program.  (We find the global scope by climbing
     * this scopes lineage.)
     */
    public static Set<ScopedVariable> getGlobalVariables(Scope scope) {
        ImmutableSet.Builder<ScopedVariable> builder = ImmutableSet.builder();
        Scope globalScope = scope.getGlobalScope();
        for (FieldDescriptor descriptor : globalScope.getVariables()) {
            for (Location location : descriptor.getLocations()) {
                builder.add(new ScopedVariable(location.getVariable(), globalScope));
            }
        }
        return builder.build();
    }

    /** Get the set of variables that are potentially redefined at a node. */
    public static Set<ScopedVariable> getRedefinedVariables(Node<ScopedStatement> node) {
        if (!node.hasValue()) {
            return ImmutableSet.of();
        }

        ImmutableSet.Builder<ScopedVariable> redefinedBuilder = ImmutableSet.builder();
        ScopedStatement scopedStatement = node.value();
        StaticStatement statement = scopedStatement.getStatement();
        Scope scope = scopedStatement.getScope();
        // Add LHS.
        if (statement instanceof Assignment) {
            ScopedVariable lhs = ScopedVariable.getAssigned((Assignment) statement, scope);
            redefinedBuilder.add(lhs);
        }

        if (Util.containsMethodCall(statement.getExpression())) {
            // For now, just assume that functions can redefine every global!
            // TODO(jasonpr): Only add each function's global write set.
            redefinedBuilder.addAll(Util.getGlobalVariables(scope));
        }
        return redefinedBuilder.build();
    }

   public static Multimap<ScopedVariable, Node<ScopedStatement>>
           reachingDefsMultimap(Iterable<ReachingDefinition> reachingDefs) {
       ImmutableMultimap.Builder<ScopedVariable, Node<ScopedStatement>> builder =
               ImmutableMultimap.builder();
       for (ReachingDefinition def : reachingDefs) {
           builder.put(def.getScopedVariable(), def.getNode());
       }
       return builder.build();
   }
   
   public static NativeExpression getReplacement(
           NativeExpression statement, NativeExpression replace, Location replacement) {
	   Preconditions.checkState(!isBaseExpression(statement));
       if(statement instanceof BinaryOperation){
           return ((BinaryOperation) statement).withReplacements(replace, replacement);
       } else if(statement instanceof MethodCall){
    	   return ((MethodCall) statement).withReplacements(replace, replacement) ;
       } else if(statement instanceof TernaryOperation){
    	   return ((TernaryOperation) statement).withReplacements(replace, replacement);
       } else if(statement instanceof UnaryOperation){
    	   return ((UnaryOperation) statement).withReplacements(replace, replacement);
       } else {
           throw new AssertionError("Unexpected NativeExpression type for " + statement);
       }
   }

   public static StaticStatement getReplacement(
           StaticStatement statement, NativeExpression replacement) {
       if(statement instanceof Assignment){
           return Assignment.assignmentWithReplacementExpr(
                   (Assignment) statement, replacement);
       } else if(statement instanceof Condition){
           return new Condition(replacement);
       } else if(statement instanceof MethodCall){
           // A method call can only be replaced with a method call.
           return (MethodCall) replacement;
       } else if(statement instanceof ReturnStatement){
           return ReturnStatement.compilerReturn(replacement);
       } else {
           throw new AssertionError("Unexpected StaticStatement type for " + statement);
       }
   }
   
   /** Gets a copy of a scope, but with some variables added, and with a new parent pointer. */
   public static Scope augmented(Scope original, Collection<Variable> augmentations, Scope newParent) {
       ImmutableList.Builder<FieldDescriptor> fieldDescs =
               ImmutableList.<FieldDescriptor>builder().addAll(original.getVariables());
       for (Variable newVar : augmentations) {
           fieldDescs.add(new FieldDescriptor(newVar, BaseType.WILDCARD));
       }
       return new Scope(fieldDescs.build(), newParent, original.isLoop());
   }
   
   /**
    * Get all scopes that are reachable from some node.
    *
    * <p> A scope is reachable if it is the scope some node, or if it is
    * the ancestor of a reachable node.
    */
   public static Set<Scope> reachableScopes(Iterable<Node<ScopedStatement>> scopedStatements) {
       ImmutableSet.Builder<Scope> reachable = ImmutableSet.builder();
       for (Node<ScopedStatement> node : scopedStatements) {
    	   if(node.hasValue()){
	           Scope scope = node.value().getScope();
	           reachable.addAll(scope.lineage());
    	   }
       }
       return reachable.build();
   }

   /**
    * Get a tree representing all the scopes.
    *
    * Requires that, if a scope is in 'scopes', then its ancestors are also in 'scopes'.
    *
    * @returns The edges of the tree.
    */
   public static Multimap<Scope, Scope> scopeTree(Iterable<Scope> scopes) {
       ImmutableMultimap.Builder<Scope, Scope> tree = ImmutableMultimap.builder();
       for (Scope scope : scopes) {
           if (scope.hasParent()) {
               tree.put(scope.getParent().get(), scope);
           }
       }
       return tree.build();
   }
   
   /** Determines if a GeneralExpression contains expressions which are themselves recursive expressions */
   public static boolean isNested(GeneralExpression ge) {
   	for(GeneralExpression subexpr: ge.getChildren()){
   		if(!isBaseExpression(subexpr)){
   			return true;
   		}
   	}
   	return false;
   }

   /** Determines if a GeneralExpression cannot recurse further */
   public static boolean isBaseExpression(GeneralExpression ge) {
       return !((ge instanceof BinaryOperation)
               || (ge instanceof MethodCall)
               || (ge instanceof TernaryOperation)
               || (ge instanceof UnaryOperation));
   }
   
   /**
    * Get all the expressions in the node.
    */
   public static Collection<NativeExpression> nodeExprs(ScopedStatement scopedStatement) {
       StaticStatement statement = scopedStatement.getStatement();
       return statement.hasExpression()
               ? ImmutableList.of(statement.getExpression())
               : ImmutableList.<NativeExpression>of();
   }

   /** Gets all the variables that this statement can read. */
   public static Set<ScopedVariable> dependencies(ScopedStatement scopedStatement) {
       StaticStatement statement = scopedStatement.getStatement();
       Scope scope = scopedStatement.getScope();

       ImmutableSet.Builder<ScopedVariable> dependencies = ImmutableSet.builder();

       // The LHS is a dependency for statements like x += 1.
       if (statement instanceof Assignment) {
           Assignment assignment = (Assignment) statement;
           if (!assignment.getOperation().isAbsolute()) {
               dependencies.add(ScopedVariable.getAssigned(assignment, scope));
           }
       }

       // All the variables that are a part of the expression are dependencies.
       dependencies.addAll(ScopedVariable.getVariablesOf(scopedStatement));

       // A global method call depends on all the globals that that function reads.
       if (Util.containsMethodCall(statement.getExpression())) {
           // For now, just assume that functions can read every global!
           // TODO(jasonpr): Only add each function's global read set.
           dependencies.addAll(Util.getGlobalVariables(scope));
       }

       return dependencies.build();
   }
}
