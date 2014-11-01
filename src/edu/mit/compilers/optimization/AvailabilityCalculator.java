package edu.mit.compilers.optimization;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/*
 * TODO(Manny): Consider making these TreeSets instead of Set or HashSets
 * Provides ordering, so retrieval of first element wouldn't require an iterator.
 */
public class AvailabilityCalculator {
    private Map<BasicBlock, InOutSets> basicBlock2inOutSets; // basic block -> (in, out)

    public AvailabilityCalculator (BasicBlock entryBlock) {
        calculateInOutSets(entryBlock);
    }

    private void setInOfBlock(BasicBlock b, Set<Subexpression> inSet) {
        basicBlock2inOutSets.get(b).setIn(inSet);
    }
    
    // TODO(Manny): Is this necessary?
    private Set<Subexpression> getIn(BasicBlock b) {
        return basicBlock2inOutSets.get(b).getIn();
    }

    private void setOutOfBlock(BasicBlock b, Set<Subexpression> outSet) {
        basicBlock2inOutSets.get(b).setOut(outSet);
    }
    
    private Set<Subexpression> getOut(BasicBlock b) {
        return basicBlock2inOutSets.get(b).getOut();
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

    /**
     * Creates in and out sets for all blocks. Does not initialize any values.
     */
    private void createInOutSets(BasicBlock entryBlock) {
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
        // Initialize variables
        Map<BasicBlock, Set<Subexpression>> block2gen;
        Map<BasicBlock, Set<Subexpression>> block2kill;
        Set<BasicBlock> changed;
        
        createInOutSets(entryBlock);
        block2gen = calculateGen(copyOfBasicBlocksSet());
        block2kill = calculateKill(copyOfBasicBlocksSet());
        
        // Begin running algorithm 
        /*
         * for all nodes n in N
         *   OUT[n] = E;
         * IN[Entry] = emptySet;
         * OUT[Entry] = GEN[Entry];
         */
        initializeInOutSets(entryBlock, block2gen.get(entryBlock));
        
        // Changed = N - {Entry};
        changed = copyOfBasicBlocksSet();
        Preconditions.checkState(changed.remove(entryBlock),
                "entryBlock not in set of all blocks");

        // While (Changed != emptyset)
        while (!changed.isEmpty()) {
            BasicBlock block;
            Set<Subexpression> temp;
            
            // Choose a node n in Changed
            block = changed.iterator().next();
            // Changed = N - {n}
            changed.remove(block);
            
            // IN[n] = E
            temp = getAllSubexpressions();
            // for all nodes p in predecessors(n)
            for (BasicBlock predecessor : block.getPredecessorBlocks()) {
                // IN[n] = intersection( IN[n], OUT[p] )
                temp.retainAll(getOut(predecessor));
            }
            
            // Store new IN[n]
            setInOfBlock(block, temp);
            
            // OUT[n] = union( GEN[n], IN[n] - KILL[n] )
            temp.removeAll(block2kill.get(block));
            temp.addAll(block2gen.get(block));
            
            // If (OUT[n] changed)
            if (!temp.equals(getOut(block))) {
                // Store new OUT[n]
                setOutOfBlock(block, temp);
                
                /*
                 * for all nodes s in successors(n)
                 *   Changed = union( Changed, {s} )
                 */
                changed.addAll(block.getSuccessorBlocks());
            }
        }
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
