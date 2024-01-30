package tech.metavm.util;

import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.object.instance.MemInstanceSearchServiceV2;
import tech.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import tech.metavm.object.instance.persistence.mappers.MemReferenceMapper;
import tech.metavm.object.type.MemAllocatorStore;
import tech.metavm.object.type.MemColumnStore;

public record BootState(
        MemInstanceMapper instanceMapper,
        MemReferenceMapper referenceMapper,
        MemIndexEntryMapper indexEntryMapper,
        MockIdProvider idProvider,
        MemColumnStore columnStore,
        MemAllocatorStore allocatorStore,
        MemInstanceSearchServiceV2 instanceSearchService
) {

    public BootState copy() {
        // deep copy
        return new BootState(
                instanceMapper.copy(),
                referenceMapper.copy(),
                indexEntryMapper.copy(),
                idProvider.copy(),
                columnStore.copy(),
                allocatorStore.copy(),
                instanceSearchService.copy()
        );
    }

}
