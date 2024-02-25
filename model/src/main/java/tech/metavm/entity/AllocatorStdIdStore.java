package tech.metavm.entity;

import tech.metavm.object.type.StdAllocators;

import java.util.Map;

public class AllocatorStdIdStore implements StdIdStore {

    private final StdAllocators allocators;

    public AllocatorStdIdStore(StdAllocators allocators) {
        this.allocators = allocators;
    }

    @Override
    public void save(Map<String, Long> ids) {
    }

    @Override
    public Map<String, Long> load() {
        return allocators.getIdMap();
    }
}
