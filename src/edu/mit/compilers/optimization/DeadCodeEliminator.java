package edu.mit.compilers.optimization;

import java.util.Collection;

import com.google.common.collect.Multimap;

import edu.mit.compilers.ast.Assignment;
import edu.mit.compilers.ast.StaticStatement;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.BcrFlowGraph;
import edu.mit.compilers.graph.Node;

public class DeadCodeEliminator implements DataFlowOptimizer {

    @Override
    public DataFlowIntRep optimized(DataFlowIntRep intRep) {
        BcrFlowGraph<ScopedStatement> dataFlowGraph = intRep.getDataFlowGraph();
        Multimap<Node<ScopedStatement>, ScopedVariable> liveVars =
                DataFlowAnalyzer.LIVE_VARIABLES.calculate(dataFlowGraph);
        return new DataFlowIntRep(deadCodeEliminated(dataFlowGraph, liveVars), intRep.getScope());
    }

    private BcrFlowGraph<ScopedStatement> deadCodeEliminated(
            BcrFlowGraph<ScopedStatement> original,
            Multimap<Node<ScopedStatement>, ScopedVariable> liveVars) {
        BcrFlowGraph.Builder<ScopedStatement> builder = BcrFlowGraph.builderOf(original);
        for (Node<ScopedStatement> node : original.getNodes()) {
            if (!node.hasValue()) {
                continue;
            }
            if (!isLive(node.value(), liveVars.get(node))) {
                builder.replace(node, Node.<ScopedStatement>nop());
            }
        }
        return builder.build();
    }

    /**
     * Returns whether this statement is live.
     *
     * A statement is live if:
     *  * it is not an assignment, or
     *  * it's an assignment whose assigned variable is live.
     */
    private boolean isLive(ScopedStatement scopedStatment, Collection<ScopedVariable> liveVars) {
        StaticStatement statement = scopedStatment.getStatement();
        if (!(statement instanceof Assignment)) {
            // Non-assignments are always live.
            return true;
        }
        Assignment assignment = (Assignment) statement;
        ScopedVariable assignedVar =
                ScopedVariable.getAssigned(assignment, scopedStatment.getScope());
        return liveVars.contains(assignedVar);
    }
}
