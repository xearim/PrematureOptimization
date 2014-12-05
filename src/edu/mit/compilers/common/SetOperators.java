package edu.mit.compilers.common;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class SetOperators {
    private SetOperators() {}

    public static <T> Set<T> intersection(Iterable<Collection<T>> sets) {
        Set<T> intersect = Sets.newHashSet(
                Iterables.getFirst(sets, ImmutableSet.<T>of()));
        for (Collection<T> set : sets) {
            intersect.retainAll(set);
        }
        return ImmutableSet.copyOf(intersect);
    }
    
    public static <T> Set<T> intersection(Collection<T>... sets) {
        return intersection(ImmutableList.copyOf(sets));
    }
    
    public static <T> Set<T> union(Iterable<Collection<T>> sets) {
        Set<T> union = Sets.newHashSet(
                Iterables.getFirst(sets, ImmutableSet.<T>of()));
        for (Collection<T> set : sets) {
            union.addAll(set);
        }
        return ImmutableSet.copyOf(union);
    }

    @SafeVarargs
    public static <T> Set<T> union(Collection<T>... sets) {
        return union(ImmutableList.copyOf(sets));
    }
    
    public static <T> Set<T> difference(Collection<T> start, Collection<T> toRemove) {
        Set<T> difference = Sets.newHashSet(start);
        difference.removeAll(toRemove);
        return ImmutableSet.copyOf(difference);
    }
}
