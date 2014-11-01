package edu.mit.compilers.optimization;

import java.util.HashSet;
import java.util.Set;

/**
 * To reflect that each basic block has a corresponding in and out, this class
 * contains both in and out sets.
 */
public class InOutSets {
    private Set<Subexpression> out;
    private Set<Subexpression> in;

    public InOutSets(BasicBlock b) {
        out = new HashSet<Subexpression>();
        in = new HashSet<Subexpression>();
    }

    public void setIn(Set<Subexpression> newIn) {
        in = newIn;
    }

    public Set<Subexpression> getIn() {
        return in;
    }

    public void setOut(Set<Subexpression> newOut) {
        out = newOut;
    }

    public Set<Subexpression> getOut() {
        return out;
    }
}
