package edu.mit.compilers.optimization;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class Availability {
    private final ImmutableSet<Subexpression> E;
    private final BasicBlock entryBlock;
    private Set<BasicBlock> changed;
    private Map<BasicBlock, Set<Subexpression>> OUT;
    private Map<BasicBlock, Set<Subexpression>> IN;
    private Map<BasicBlock, Set<Subexpression>> GEN; // GEN and KILL should go to immutable sets
    private Map<BasicBlock, Set<Subexpression>> KILL;
    private Map<Subexpression, GeneratedVariable> Subexpression2ContainerMap; // Not sure when this comes into play

    public Availability (BasicBlock entry) {
        // All of these statements set up the algorithm
        this.entryBlock = entry;
        this.E = getAllSubexpressions();
        calculateGenKillSets();

        // These two functions run the algorithm
        initializeInOutSets();
        calculateInOutSets();
    }

    private ImmutableSet<Subexpression> getAllSubexpressions() {
        throw new RuntimeException("Availability.java: getAllSubexpressions unimplemented");
    }

    private Set<BasicBlock> getAllBasicBlocks() {
        throw new RuntimeException("Availability.java: getAllBlocks unimplemented");
    }

    private void calculateGenKillSets() {
        throw new RuntimeException("Availability.java: calculateGenKillSets unimplemented");
    }

    private void initializeInOutSets() {
        /*
         * for all nodes n in N
         *   OUT[n] = E;
         */
        for (BasicBlock b : OUT.keySet()) {
            OUT.put(b, new HashSet<Subexpression>(E));
        }
        // IN[Entry] = emptyset;
        IN.put(this.entryBlock, new HashSet<Subexpression>());
        // OUT[Entry] = GEN[Entry]
        OUT.put(this.entryBlock, new HashSet<Subexpression>(GEN.get(this.entryBlock)));
        // Changed = N - {Entry}
        this.changed = getAllBasicBlocks();
        this.changed.remove(this.entryBlock);
    }

    private void calculateInOutSets() {
        // While (Changed != emptyset)
        while (this.changed.size() > 0) {
            // Choose a node n in Changed
            /*
             * TODO(Manny): Find a better way to do this. Maybe this.changed
             * shouldn't be a set. This is a case where we want both uniqueness
             * and ordering. Should consider changed this to a map or list.
             */
            BasicBlock n = this.changed.iterator().next();
            // Changed = N - {n}
            this.changed.remove(n);
            
            // IN[n] = E
            this.IN.put(n, new HashSet<Subexpression>(E));
            // for all nodes p in predecessors(n)
            for (BasicBlock p : n.getPredecessorBlocks()) {
                // IN[n] = intersection( IN[n], OUT[p] )
                this.IN.get(n).retainAll(OUT.get(p));
            }
            
            // Save old OUT[n] for later comparison
            Set<Subexpression> oldOut = new HashSet<Subexpression>(OUT.get(n));
            
            // OUT[n] = union( GEN[n], IN[n] - KILL[n] )
            // Need to create tempset to hold IN[n] - KILL[n]
            Set<Subexpression> tempSet = new HashSet<Subexpression>(IN.get(n));
            tempSet.removeAll(KILL.get(n));
            OUT.put(n, new HashSet<Subexpression>(GEN.get(n)));
            OUT.get(n).addAll(tempSet);
            
            // If (OUT[n] changed)
            if (setsAreDifferent(oldOut,OUT.get(n))) {
                /*
                 * for all nodes s in successors(n)
                 *   Changed = union( Changed, {s} )
                 */ 
                this.changed.addAll(n.getSuccessorBlocks());
            }
        }
    }
    
    private boolean setsAreDifferent(Set<Subexpression> oldSet, Set<Subexpression> newSet) {
        // TODO(Manny): verify that size comparison is enough
        return oldSet.size() != newSet.size();
    }

    public Set<Subexpression> getIn(BasicBlock b) {
        return IN.get(b);
    }

    public Set<Subexpression> getOut(BasicBlock b) {
        return OUT.get(b);
    }
}
