package org.metavm.entity;

import java.util.List;

public interface EntityIdProvider {

    List<Long> allocate(long appId, int count);

    default Long allocateOne(long appId) {
        return allocate(appId, 1).getFirst();
    }

}
