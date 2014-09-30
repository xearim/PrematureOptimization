package edu.mit.compilers.ast;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

/**
 * A "dummy node" that exists only to group its children.
 *
 * <p>A NodeSequence<T> is basically a List<T> that implements Node.
 */
public class NodeSequence<T extends Node> implements Node {

    private ImmutableList<? extends T> sequence;
    private final String name;
    
    public NodeSequence(List<? extends T> sequence, String name) {
        this.sequence = ImmutableList.copyOf(sequence);
        this.name = name;
    }
    
    @Override
    public Iterable<? extends Node> getChildren() {
        return sequence;
    }

    @Override
    public String getName() {
        return name;
    }

    // A node sequence is for all intents and purposes equivalent to a block in terms of return determination
	@Override
	public boolean canReturn(Optional<BaseType> type) {
		return mustReturn(type);
	}

	// A node sequence is for all intents and purposes equivalent to a block in terms of return determination
	@Override
	public boolean mustReturn(Optional<BaseType> type) {
		UnmodifiableIterator<? extends T> sequenceItr = sequence.iterator();
		for(T t = sequenceItr.next(); sequenceItr.hasNext();){
			if((t.mustReturn(Optional.of(BaseType.BOOLEAN)) || 
				t.mustReturn(Optional.of(BaseType.INTEGER)) ||
				t.mustReturn(Optional.<BaseType>absent()) ) && !t.mustReturn(type))
				return false;
			else if(t.mustReturn(type))
				return true;
		}
		if(!type.isPresent())
			return true;
		return false;
	}
}
