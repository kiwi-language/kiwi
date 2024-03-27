package tech.metavm.util;

import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.object.instance.MemInstanceSearchServiceV2;
import tech.metavm.object.type.AllocatorStore;
import tech.metavm.system.persistence.BlockMapper;
import tech.metavm.system.persistence.RegionMapper;

public record BootstrapResult(
        EntityContextFactory entityContextFactory,
        EntityIdProvider idProvider,
        BlockMapper blockMapper,
        RegionMapper regionMapper,
        MemInstanceStore instanceStore,
        MemInstanceSearchServiceV2 instanceSearchService,
        AllocatorStore allocatorStore
) {
}
