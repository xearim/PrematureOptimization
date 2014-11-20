package edu.mit.compilers.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates unique IDs for a set of items.
 *
 * <p>IDs are only unique among values identified by a single UniqueIdentifier.
 *
 * <p>For any UniqueIds 'ids', ids.getId(x) == ids.getId(y) exactly when x.equals(y).
 */
public class UniqueIdentifier<T> {

    private long id = 0;
    private final Map<T, Long> ids =  new HashMap<T, Long>();
    
    public long getId(T item) {
        if (!ids.containsKey(item)) {
            ids.put(item, id);
            id++;
        }
        return ids.get(item);
    }
}
