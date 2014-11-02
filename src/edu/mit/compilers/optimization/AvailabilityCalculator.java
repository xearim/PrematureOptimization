package edu.mit.compilers.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Given a basic block, the AvailabilityCalculator computes all available
 * subexpressions at each block accessible from the input basic block.
 */
public class AvailabilityCalculator {
    private Map<BasicBlock, Set<Subexpression>> inSets;

    public AvailabilityCalculator (BasicBlock entryBlock) {
        calculateAvailability(entryBlock);
    }

    /**
     * Runs the fixed-point algorithm for available expressions when created.
     * Afterwards, can be asked what the available expressions are at each
     * basic block.
     */
    private void calculateAvailability(BasicBlock entryBlock) {
        /*
         * Set up variables for algorithm:
         * - IN and OUT
         * - GEN and KILL
         * - changed
         * - all subexpressions (E)
         */
        createInSets(entryBlock);
        Set<BasicBlock> allBlocks = ImmutableSet.copyOf(this.inSets.keySet());
        Map<BasicBlock, Set<Subexpression>> outSets =
                new HashMap<BasicBlock, Set<Subexpression>>();
        Map<BasicBlock, Set<Subexpression>> genSets = calculateGenSets(allBlocks);
        Map<BasicBlock, Set<Subexpression>> killSets = calculateKillSets(allBlocks);
        Set<BasicBlock> changed;
        Set<Subexpression> allSubexpressions = getAllSubexpressions(allBlocks);

        // Run algorithm
        // for all nodes n in N
        for (BasicBlock block : inSets.keySet()) {
            // OUT[n] = E;
            outSets.put(block, new HashSet<Subexpression>(allSubexpressions));
        }
        // IN[Entry] = emptySet;
        // All IN sets are already initialized to the emptySet.

        // OUT[Entry] = GEN[Entry];
        outSets.put(entryBlock, new HashSet<Subexpression>(
                genSets.get(entryBlock)));

        // Changed = N - {Entry};
        changed = copyOfBasicBlocksSet();
        Preconditions.checkState(changed.remove(entryBlock),
                "entryBlock is not in set of all blocks.");

        // While (Changed != emptyset)
        while (!changed.isEmpty()) {
            BasicBlock block;
            Set<Subexpression> newOut;

            // Choose a node n in Changed
            block = changed.iterator().next();
            // Changed = N - {n}
            changed.remove(block);

            // IN[n] = E
            this.inSets.put(block, new HashSet<Subexpression>(allSubexpressions));
            // for all nodes p in predecessors(n)
            for (BasicBlock predecessor : block.getPredecessorBlocks()) {
                // IN[n] = intersection( IN[n], OUT[p] )
                this.inSets.get(block).retainAll(outSets.get(predecessor));
            }

            // OUT[n] = union( GEN[n], IN[n] - KILL[n] )
            newOut = new HashSet<Subexpression>(this.inSets.get(block));
            newOut.removeAll(killSets.get(block));
            newOut.addAll(genSets.get(block));

            // If (OUT[n] changed)
            if (!newOut.equals(outSets.get(block))) {
                // Store new OUT[n], note: not explicitly in algorithm
                outSets.put(block, newOut);

                /*
                 * for all nodes s in successors(n)
                 *   Changed = union( Changed, {s} )
                 */
                changed.addAll(block.getSuccessorBlocks());
            }
        }
    }

    /**
     * Creates IN sets for all blocks. Does not initialize any values. Makes
     * sure that each block is included only once.
     */
    private void createInSets(BasicBlock entryBlock) {
        Set<BasicBlock> visited = new HashSet<BasicBlock>();
        Set<BasicBlock> toBeVisited = new HashSet<BasicBlock>();
        toBeVisited.add(entryBlock);
        this.inSets = new HashMap<BasicBlock, Set<Subexpression>>();

        while (!toBeVisited.isEmpty()) {
            BasicBlock block = toBeVisited.iterator().next();
            toBeVisited.remove(block);

            for (BasicBlock newBlock : block.getSuccessorBlocks()) {
                // In case there are multiple paths to that block
                if (!visited.contains(newBlock)) {
                    visited.add(newBlock);
                    this.inSets.put(newBlock, new HashSet<Subexpression>());
                }
            }
        }
    }

    private Map<BasicBlock, Set<Subexpression>> calculateGenSets(
            Set<BasicBlock> blocks) {
        for (BasicBlock block : blocks) {
        }

        throw new UnsupportedOperationException(
                "AvailabilityCalculator#calculateGen unimplemented.");
    }

    private Map<BasicBlock, Set<Subexpression>> calculateKillSets(
            Set<BasicBlock> blocks) {
        for (BasicBlock block : blocks) {
        }

        throw new UnsupportedOperationException(
                "AvailabilityCalculator#calculateKill unimplemented.");
    }

    /**
     * Returns shallow copy. Want this to be modifiable but to not mess up the
     * original keyset.
     */
    private Set<BasicBlock> copyOfBasicBlocksSet() {
        return new HashSet<BasicBlock>(this.inSets.keySet());
    }

    private ImmutableSet<Subexpression> getAllSubexpressions(Set<BasicBlock> blocks) {
        throw new UnsupportedOperationException("Availability.java: getAllSubexpressions unimplemented");
    }

    public Set<Subexpression> getAvailableSubexpressionsOfBasicBlock(BasicBlock b) {
        return inSets.get(b);
    }
}
