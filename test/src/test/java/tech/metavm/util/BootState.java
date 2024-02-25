package tech.metavm.util;

import tech.metavm.entity.DefContext;
import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.entity.MemoryStdIdStore;
import tech.metavm.object.instance.MemInstanceSearchServiceV2;
import tech.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import tech.metavm.object.instance.persistence.mappers.MemReferenceMapper;
import tech.metavm.object.type.MemAllocatorStore;
import tech.metavm.object.type.MemColumnStore;
import tech.metavm.system.persistence.MemBlockMapper;
import tech.metavm.system.persistence.MemRegionMapper;

public record BootState(
        DefContext defContext,
        MemInstanceMapper instanceMapper,
        MemReferenceMapper referenceMapper,
        MemIndexEntryMapper indexEntryMapper,
        MemRegionMapper regionMapper,
        MemBlockMapper blockMapper,
        MemColumnStore columnStore,
        MemoryStdIdStore stdIdStore,
        MemAllocatorStore allocatorStore,
        MemInstanceSearchServiceV2 instanceSearchService
) {

    public BootState copy() {
        // deep copy
        return new BootState(
                defContext,
                instanceMapper.copy(),
                referenceMapper.copy(),
                indexEntryMapper.copy(),
                regionMapper.copy(),
                blockMapper.copy(),
                columnStore.copy(),
                stdIdStore.copy(),
                allocatorStore.copy(),
                instanceSearchService.copy()
        );
    }

}
