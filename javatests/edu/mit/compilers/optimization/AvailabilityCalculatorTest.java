package edu.mit.compilers.optimization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.AssignmentOperation;
import edu.mit.compilers.ast.BaseType;
import edu.mit.compilers.ast.BinaryOperation;
import edu.mit.compilers.ast.BinaryOperator;
import edu.mit.compilers.ast.FieldDescriptor;
import edu.mit.compilers.ast.IntLiteral;
import edu.mit.compilers.ast.LocationDescriptor;
import edu.mit.compilers.ast.ScalarLocation;
import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.codegen.AssignmentDataFlowNode;
import edu.mit.compilers.codegen.BranchSourceDataFlowNode;
import edu.mit.compilers.codegen.DataFlowNode;
import edu.mit.compilers.codegen.NopDataFlowNode;
import edu.mit.compilers.codegen.SequentialDataFlowNode;
import edu.mit.compilers.codegen.asm.instructions.JumpType;
import edu.mit.compilers.common.Variable;
import edu.mit.compilers.optimization.AvailabilityCalculator;

public class AvailabilityCalculatorTest {
    private AvailabilityCalculator calculator;

    @Test
    public void testSingleAssignmentOfInt() {
        calculator = calculatorForAssignments(
                new Scope(ImmutableList.of(fieldDescriptor("x"))),
                setEqual("x", 1));
        // Do nothing. Just check that making the calculator is successful.
    }

    @Test
    public void testSingleAssignmentOfSum() {
        calculator = calculatorForAssignments(
                new Scope(ImmutableList.of(fieldDescriptor("x"))),
                setEqualToLongSum("x", 1, 2));
        // Do nothing. Just check that a simple assignment doesn't break calculator.
    }

    @Test
    public void testSimpleAvailability() {
        Scope scope = new Scope(ImmutableList.of(
                fieldDescriptor("x"),
                fieldDescriptor("y") ));

        Assignment assignment1 = setEqualToLongSum("x", 1, 2);
        AssignmentDataFlowNode assignmentDFN1 = new AssignmentDataFlowNode(assignment1,scope);

        Assignment assignment2 = setEqualToLongSum("y", 1, 2);
        AssignmentDataFlowNode assignmentDFN2 = new AssignmentDataFlowNode(assignment2,scope);

        calculator = calculatorForNodes(scope, assignmentDFN1, assignmentDFN2);

        assertTrue(calculator.isAvailable(assignment2.getExpression(), assignmentDFN2));
    }

    @Test
    public void testDoubleAvailability() {
        Scope scope = new Scope(ImmutableList.of(
                fieldDescriptor("x"),
                fieldDescriptor("y") ));

        Assignment assignment1 = setEqualToLongSum("x", 1, 2);
        AssignmentDataFlowNode assignmentDFN1 = new AssignmentDataFlowNode(assignment1,scope);

        Assignment assignment2 = setEqualToLongSum("y", 2, 2);
        AssignmentDataFlowNode assignmentDFN2 = new AssignmentDataFlowNode(assignment2,scope);
        
        Assignment assignment3 = setEqual("x",2);
        AssignmentDataFlowNode assignmentDFN3 = new AssignmentDataFlowNode(assignment3,scope);

        calculator = calculatorForNodes(scope, assignmentDFN1, assignmentDFN2, assignmentDFN3);

        assertTrue(calculator.isAvailable(assignment1.getExpression(), assignmentDFN2));
        assertTrue(calculator.isAvailable(assignment1.getExpression(), assignmentDFN3));
        assertTrue(calculator.isAvailable(assignment2.getExpression(), assignmentDFN3));
        
    }

    @Test
    public void testSimpleKill() {
        Scope scope = new Scope(ImmutableList.of(fieldDescriptor("x")));

        Assignment assign = setEqualToLongSum("x", 1, 2);
        AssignmentDataFlowNode assignNode = new AssignmentDataFlowNode(assign, scope);

        Assignment kill = setEqualToMixedSum("x", "x", 3);
        AssignmentDataFlowNode killNode = new AssignmentDataFlowNode(kill, scope);

        NopDataFlowNode nop = NopDataFlowNode.nop();

        Subexpression sub = new Subexpression(kill.getExpression(), scope);
        calculator = calculatorForNodes(scope, assignNode, killNode, nop);

        assertTrue(calculator.isAvailable(assign.getExpression(), killNode));
        assertFalse(calculator.availableSubexpressionsAt(nop).contains(sub));
    }

    @Test
    public void testReavailable() {
        Scope scope = new Scope(ImmutableList.of(fieldDescriptor("x"), fieldDescriptor("y")));

        Assignment assign = setEqualToMixedSum("y", "x", 3);
        AssignmentDataFlowNode assignNode = new AssignmentDataFlowNode(assign, scope);
        Subexpression assignSub = new Subexpression(assign.getExpression(), scope);

        Assignment kill = setEqual("x", 1);
        AssignmentDataFlowNode killNode = new AssignmentDataFlowNode(kill, scope);

        Assignment reassign = setEqualToMixedSum("y", "x", 3);
        AssignmentDataFlowNode reassignNode = new AssignmentDataFlowNode(assign, scope);

        NopDataFlowNode nop = NopDataFlowNode.nop();

        calculator = calculatorForNodes(scope, assignNode, killNode, reassignNode, nop);

        assertFalse(calculator.isAvailable(assign.getExpression(), reassignNode));
        assertTrue(calculator.availableSubexpressionsAt(nop).contains(assignSub));
    }
    
//    @Test
//    public void testIfKill() {
//        Scope scope = new Scope(ImmutableList.of(fieldDescriptor("x"), fieldDescriptor("y")));
//        
//        Assignment assign = setEqualToMixedSum("y", "x", 2);
//        AssignmentDataFlowNode assignNode = new AssignmentDataFlowNode(assign, scope);
//        Subexpression assignSub = new Subexpression(assign.getExpression(), scope);
//        
//        BranchSourceDataFlowNode branch = new BranchSourceDataFlowNode(JumpType.JE);
//        assignNode.setNext(branch);
//        
//        Assignment kill = setEqual("x",2);
//        AssignmentDataFlowNode killNode = new AssignmentDataFlowNode(kill, scope);
//        branch.setTrueBranch(killNode);
//
//        NopDataFlowNode nop = NopDataFlowNode.nop();
//        branch.setFalseBranch(killNode);
//        killNode.setNext(nop);
//        
//        calculator = new AvailabilityCalculator(assignNode);
//        
//        assertTrue(calculator.isAvailable(assign.getExpression(), killNode));
//        assertTrue(!calculator.availableSubexpressionsAt(nop).contains(assignSub));
//    }

    private static AvailabilityCalculator calculatorForNodes(Scope sharedScope, SequentialDataFlowNode... nodes) {
        NopDataFlowNode head = NopDataFlowNode.nop();
        SequentialDataFlowNode tail = head;

        for (SequentialDataFlowNode node : nodes) {
            SequentialDataFlowNode.link(tail, node);
            tail = node;
        }

        return new AvailabilityCalculator(head);
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
        return new FieldDescriptor(Variable.forUser(name), BaseType.INTEGER, LocationDescriptor.machineCode());
    }

    private static Assignment setEqual(String name, long value) {
        return new Assignment(
                varLocation(name),
                AssignmentOperation.SET_EQUALS,
                new IntLiteral(value),
                LocationDescriptor.machineCode());
    }

    private static Assignment setEqualToLongSum(String name, long left, long right) {
        return new Assignment(
                varLocation(name),
                AssignmentOperation.SET_EQUALS,
                new BinaryOperation(BinaryOperator.PLUS, new IntLiteral(left),
                        new IntLiteral(right), LocationDescriptor.machineCode()),
                        LocationDescriptor.machineCode());
    }

    private static Assignment setEqualToVarSum(String name, String left, String right) {
        return new Assignment(
                varLocation(name),
                AssignmentOperation.SET_EQUALS,
                new BinaryOperation(BinaryOperator.PLUS, varLocation(left),
                        varLocation(right), LocationDescriptor.machineCode()),
                        LocationDescriptor.machineCode());
    }

    private static Assignment setEqualToMixedSum(String name, String left, long right) {
        return new Assignment(
                varLocation(name),
                AssignmentOperation.SET_EQUALS,
                new BinaryOperation(BinaryOperator.PLUS, varLocation(left),
                        new IntLiteral(right), LocationDescriptor.machineCode()),
                        LocationDescriptor.machineCode());
    }

    private static ScalarLocation varLocation(String name) {
        return new ScalarLocation(Variable.forUser(name), LocationDescriptor.machineCode());
    }
}
