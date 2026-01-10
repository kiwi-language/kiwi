package org.manul.util;

import org.manul.entity.MemoryStdIdStore;
import org.manul.entity.SystemDefContext;
import org.manul.object.instance.MemInstanceSearchServiceV2;
import org.manul.object.instance.persistence.MemMapperRegistry;
import org.manul.object.type.AllocatorStore;
import org.manul.object.type.MemTypeTagStore;
import org.manul.system.MemoryBlockRepository;

public record BootState(
        SystemDefContext defContext,
        MemoryBlockRepository blockRepository,
        MemTypeTagStore typeTagStore,
        MemoryStdIdStore stdIdStore,
        AllocatorStore allocatorStore,
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
                allocatorStore,
                instanceSearchService.copy(),
                instanceMapperRegistry.copy()
        );
    }

}
