package org.metavm.util;

import org.metavm.entity.EntityIdProvider;

import java.util.ArrayList;
import java.util.List;

public class MockIdProvider implements EntityIdProvider {

    public static final long INITIAL_NEXT_ID = 1000000L;
    private long nextId = INITIAL_NEXT_ID;

    @Override
    public List<Long> allocate(long appId, int count) {
        var ids = new ArrayList<Long>();
        for (int i = 0; i < count; i++) {
            ids.add(nextId++);
        }
        return ids;
    }

    public void clear() {
        nextId = INITIAL_NEXT_ID;
    }

    public MockIdProvider copy() {
        var copy = new MockIdProvider();
        copy.nextId = nextId;
        return copy;
    }

}
