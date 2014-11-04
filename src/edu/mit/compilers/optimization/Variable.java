package edu.mit.compilers.optimization;

import java.util.Set;

import edu.mit.compilers.ast.Location;
import edu.mit.compilers.ast.NativeExpression;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;

public class Variable {
    Location location;
    Scope scope;

    private Variable (Location loc, Scope scope) {
        this.location = loc;
        this.scope = scope;
    }

    public static Variable getAssigned(AssignmentDataFlowNode assignmentNode) {
        Location loc = assignmentNode.getAssignment().getLocation();
        return new Variable(loc, getScopeOf(loc, assignmentNode.getScope()));
    }

    public static Scope getScopeOf(Location loc, Scope scope) {
        throw new UnsupportedOperationException("Variable#getScopeOf unimplemented.");
    }

    public static Set<Variable> getVariablesOf(NativeExpression ne, Scope scope) {
        throw new UnsupportedOperationException("Variable#getVariablesOf unimplemented");
    }

}
