package org.metavm.util;

import org.metavm.entity.*;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.type.AllocatorStore;
import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeTagStore;
import org.metavm.system.persistence.BlockMapper;
import org.metavm.system.persistence.RegionMapper;

public record BootstrapResult(
        DefContext defContext,
        EntityContextFactory entityContextFactory,
        EntityIdProvider idProvider,
        BlockMapper blockMapper,
        RegionMapper regionMapper,
        MemInstanceStore instanceStore,
        MemInstanceSearchServiceV2 instanceSearchService,
        AllocatorStore allocatorStore,
        ColumnStore columnStore,
        MemoryStdIdStore stdIdStore,
        TypeTagStore typeTagStore
) {
}
