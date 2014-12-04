package edu.mit.compilers.optimization;

import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.GeneralExpression;
import edu.mit.compilers.ast.MethodCall;
import edu.mit.compilers.ast.TernaryOperation;
import edu.mit.compilers.ast.UnaryOperation;

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
}
