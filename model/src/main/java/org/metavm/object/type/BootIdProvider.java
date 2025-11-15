package org.metavm.object.type;

import org.metavm.entity.EntityIdProvider;

import javax.annotation.Nullable;
import java.util.List;

public class BootIdProvider implements EntityIdProvider {

    private final StdAllocators allocators;

    public BootIdProvider(StdAllocators allocators) {
        this.allocators = allocators;
    }

    @Override
    public List<Long> allocate(long appId, int count) {
        return allocators.allocate(count);
    }

    public @Nullable Long getNextNodeId(Object model) {
        return allocators.getNextNodeId(model);
    }

}
