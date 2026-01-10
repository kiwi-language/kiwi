package org.manul.entity;

import org.manul.object.instance.core.Id;
import org.manul.object.type.StdAllocators;

import java.util.Map;

public class AllocatorStdIdStore implements StdIdStore {

    private final StdAllocators allocators;

    public AllocatorStdIdStore(StdAllocators allocators) {
        this.allocators = allocators;
    }

    @Override
    public void save(Map<String, Id> ids) {
    }

    @Override
    public Map<String, Id> load() {
        return allocators.getIdMap();
    }
}
