package org.metavm.util;

import org.metavm.entity.DefContext;
import org.metavm.entity.MemIndexEntryMapper;
import org.metavm.entity.MemoryStdIdStore;
import org.metavm.entity.SystemDefContext;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import org.metavm.object.instance.persistence.mappers.MemReferenceMapper;
import org.metavm.object.type.MemAllocatorStore;
import org.metavm.object.type.MemColumnStore;
import org.metavm.object.type.MemTypeTagStore;
import org.metavm.system.MemoryBlockRepository;
import org.metavm.system.persistence.MemBlockMapper;
import org.metavm.system.persistence.MemRegionMapper;

public record BootState(
        SystemDefContext defContext,
        MemInstanceMapper instanceMapper,
        MemReferenceMapper referenceMapper,
        MemIndexEntryMapper indexEntryMapper,
        MemRegionMapper regionMapper,
        MemBlockMapper blockMapper,
        MemoryBlockRepository blockRepository,
        MemColumnStore columnStore,
        MemTypeTagStore typeTagStore,
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
                blockRepository.copy(),
                columnStore.copy(),
                typeTagStore.copy(),
                stdIdStore.copy(),
                allocatorStore.copy(),
                instanceSearchService.copy()
        );
    }

}
