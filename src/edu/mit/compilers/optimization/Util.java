package edu.mit.compilers.optimization;

import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
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

   public static Multimap<ScopedVariable, Node<ScopedStatement>>
           reachingDefsMultimap(Iterable<ReachingDefinition> reachingDefs) {
       ImmutableMultimap.Builder<ScopedVariable, Node<ScopedStatement>> builder =
               ImmutableMultimap.builder();
       for (ReachingDefinition def : reachingDefs) {
           builder.put(def.getScopedLocation(), def.getNode());
       }
       return builder.build();
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
}
