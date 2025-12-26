package org.metavm.util;

import org.metavm.entity.MemoryStdIdStore;
import org.metavm.entity.SystemDefContext;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.persistence.MemMapperRegistry;
import org.metavm.object.type.MemAllocatorStore;
import org.metavm.object.type.MemTypeTagStore;
import org.metavm.system.MemoryBlockRepository;

public record BootState(
        SystemDefContext defContext,
        MemoryBlockRepository blockRepository,
        MemTypeTagStore typeTagStore,
        MemoryStdIdStore stdIdStore,
        MemAllocatorStore allocatorStore,
        MemInstanceSearchServiceV2 instanceSearchService,
        MemMapperRegistry instanceMapperRegistry
) {

    public BootState copy() {
        // deep copy
        return new BootState(
                defContext,
                blockRepository.copy(),
                typeTagStore.copy(),
                stdIdStore.copy(),
                allocatorStore.copy(),
                instanceSearchService.copy(),
                instanceMapperRegistry.copy()
        );
    }

}
