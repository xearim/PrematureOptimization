package edu.mit.compilers.optimization;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.NopDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.common.Variable;

public class AvailabilityCalculatorTest {

    @Test
    public void testSingleAssignmentOfInt() {
        AvailabilityCalculator calculator = calculatorForAssignments(
                new Scope(ImmutableList.of(fieldDescriptor("x"))),
                setEqual("x", 1));
        // Do nothing. Just check that making the calculator is successful.
    }
    
    private static AvailabilityCalculator
            calculatorForAssignments(Scope sharedScope, Assignment... assignments) {
        List<AssignmentDataFlowNode> nodes = new ArrayList<AssignmentDataFlowNode>();
        for (Assignment assignment : assignments) {
            nodes.add(new AssignmentDataFlowNode(assignment, sharedScope));
        }

        NopDataFlowNode head = NopDataFlowNode.nop();
        SequentialDataFlowNode tail = head;
        for (AssignmentDataFlowNode node : nodes) {
            SequentialDataFlowNode.link(tail, node);
            tail = node;
        }
        
        return new AvailabilityCalculator(head);
    }
    
    private static FieldDescriptor fieldDescriptor(String name) {
        // TODO(jasonpr): Don't use machineCode for non-compiler-gen'd code!
        return new FieldDescriptor(Variable.forUser("x"), BaseType.INTEGER, LocationDescriptor.machineCode());
    }
    
    private static Assignment setEqual(String name, long value) {
        return new Assignment(
                new ScalarLocation(Variable.forUser(name), LocationDescriptor.machineCode()),
                AssignmentOperation.SET_EQUALS,
                new IntLiteral(value),
                LocationDescriptor.machineCode());
    }
}
