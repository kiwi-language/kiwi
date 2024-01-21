package tech.metavm.util;

import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.MemInstanceSearchServiceV2;
import tech.metavm.object.instance.search.InstanceSearchService;

public record BootstrapResult(
        EntityContextFactory entityContextFactory,
        EntityIdProvider idProvider,
        MemInstanceStore instanceStore,
        MemInstanceSearchServiceV2 instanceSearchService
) {
}
