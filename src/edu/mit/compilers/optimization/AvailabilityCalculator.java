package edu.mit.compilers.optimization;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class AvailabilityCalculator {
    private Map<BasicBlock, InOutSets> basicBlock2inOutSets; // basic block -> (in, out)

    public AvailabilityCalculator (BasicBlock entryBlock) {
        calculateInOutSets(entryBlock);
    }

    private ImmutableSet<Subexpression> getAllSubexpressions() {
        throw new RuntimeException("Availability.java: getAllSubexpressions unimplemented");
    }

    private Set<BasicBlock> copyOfBasicBlocksSet() {
        /*
         * Returns shallow copy. Want this to be modifiable but to not mess up
         * the original keyset.
         */
        return new HashSet<BasicBlock>(basicBlock2inOutSets.keySet());
    }

//    private Map<BasicBlock, GenKillSets> calculateGenKillSets(Set<BasicBlock> blocks) {
//        throw new RuntimeException("Availability.java#calculateGenKillSets unimplemented");
//    }

    /**
     * Creates in and out sets for all blocks. Does not initialize any values.
     */
    private void createInOutSets(BasicBlock entryBlock) {
        // TODO(Manny): Consider making these TreeSets
        Set<BasicBlock> visited = new HashSet<BasicBlock>();
        Set<BasicBlock> toBeVisited = new HashSet<BasicBlock>();
        toBeVisited.add(entryBlock);
        
        while (!toBeVisited.isEmpty()) {
            BasicBlock block = toBeVisited.iterator().next();
            toBeVisited.remove(block);
            
            for (BasicBlock newBlock : block.getSuccessorBlocks()) {
                // In case there are multiple paths to that block
                if (!visited.contains(newBlock)) {
                    visited.add(newBlock);
                    this.basicBlock2inOutSets.put(newBlock,
                            new InOutSets(newBlock));
                }
            }
        }
    }

    
    
    private void calculateInOutSets(BasicBlock entryBlock) {
        Map<BasicBlock, Set<Subexpression>> block2gen;
        Map<BasicBlock, Set<Subexpression>> block2kill;
        
        createInOutSets(entryBlock);
        block2gen = calculateGen(copyOfBasicBlocksSet());
        block2kill = calculateKill(copyOfBasicBlocksSet());
        
        /*
         * for all nodes n in N
         *   OUT[n] = E;
         * IN[Entry] = emptySet;
         * OUT[Entry] = GEN[Entry];
         * Changed = N - {Entry};
         */
        initializeInOutSets(entryBlock, block2gen.get(entryBlock));
        
        

        // While (Changed != emptyset)
            // Choose a node n in Changed
            // Changed = N - {n}
            
            // IN[n] = E
            // for all nodes p in predecessors(n)
                // IN[n] = intersection( IN[n], OUT[p] )

        // Save old OUT[n] for later comparison
            
            // OUT[n] = union( GEN[n], IN[n] - KILL[n] )
            
            // If (OUT[n] changed)
                /*
                 * for all nodes s in successors(n)
                 *   Changed = union( Changed, {s} )
                 */ 
    }

    private Map<BasicBlock, Set<Subexpression>> calculateGen(
            Set<BasicBlock> copyOfBasicBlocksSet) {
        throw new UnsupportedOperationException(
                "AvailabilityCalculator#calculateGen unimplemented.");
    }

    private Map<BasicBlock, Set<Subexpression>> calculateKill(
            Set<BasicBlock> copyOfBasicBlocksSet) {
        throw new UnsupportedOperationException(
                "AvailabilityCalculator#calculateKill unimplemented.");
    }

    private void initializeInOutSets(BasicBlock entryBlock,
            Set<Subexpression> set) {
        throw new UnsupportedOperationException(
                "AvailabilityCalculator#initializeInOutSets unimplemented.");
    }

    public Set<Subexpression> getInOfBasicBlock(BasicBlock b) {
        return basicBlock2inOutSets.get(b).getIn();
    }

    public Set<Subexpression> getOutOfBasicBlock(BasicBlock b) {
        return basicBlock2inOutSets.get(b).getOut();
    }
}
