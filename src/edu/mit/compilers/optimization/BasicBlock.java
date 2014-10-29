package edu.mit.compilers.optimization;

import java.util.List;
import java.util.Map;

public abstract class BasicBlock {
    public abstract List<String> getSubexpressions();
    public abstract boolean[] getAvailabilityBitVector(
            Map<String,Integer> bitVectorMapping);
    public abstract List<BasicBlock> getNextBlocks();
}
