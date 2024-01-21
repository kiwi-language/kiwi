package tech.metavm.util;

import tech.metavm.entity.Bootstrap;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.MemInstanceSearchServiceV2;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.log.TaskHandler;
import tech.metavm.object.instance.log.VersionHandler;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.type.MemAllocatorStore;
import tech.metavm.object.type.MemColumnStore;
import tech.metavm.object.type.StdAllocators;

import java.util.List;

public class BootstrapUtils {

    private static volatile BootState state;

    private static EntityContextFactory createEntityContextFactory(EntityIdProvider idProvider,
                                                                   MemInstanceStore instanceStore,
                                                                   InstanceSearchService instanceSearchService) {
        var instanceContextFactory =
                TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, instanceStore.getIndexEntryMapper());
        entityContextFactory.setInstanceLogService(
                new InstanceLogServiceImpl(entityContextFactory, instanceSearchService, instanceStore, List.of(
                        new TaskHandler(entityContextFactory, new MockTransactionOperations()),
                        new VersionHandler(new MockEventQueue())
                ))
        );
        entityContextFactory.setDefaultAsyncLogProcess(false);
        return entityContextFactory;
    }

    public static BootstrapResult bootstrap() {
        if(state != null) {
            var state = BootstrapUtils.state.copy();
            var instanceStore = new MemInstanceStore(
                    state.instanceMapper(),
                    state.indexEntryMapper(),
                    state.referenceMapper()
            );
            var idProvider = state.idProvider();
            var instanceSearchService = state.instanceSearchService();
            var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
            var bootstrap = new Bootstrap(
                    entityContextFactory, entityContextFactory.getInstanceContextFactory(),
                    new StdAllocators(state.allocatorStore()),
                    state.columnStore()
            );
            bootstrap.boot();
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    instanceStore,
                    instanceSearchService
            );
        }
        else {
            var instanceStore = new MemInstanceStore();
            var idProvider = new MockIdProvider();
            var instanceSearchService = new MemInstanceSearchServiceV2();
            var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
            var allocatorStore = new MemAllocatorStore();
            var columnStore = new MemColumnStore();
            var bootstrap = new Bootstrap(
                    entityContextFactory, entityContextFactory.getInstanceContextFactory(),
                    new StdAllocators(allocatorStore),
                    columnStore
            );
            bootstrap.boot();
            TestUtils.beginTransaction();
            bootstrap.save(true);
            TestUtils.commitTransaction();
            state = new BootState(
                    instanceStore.getInstanceMapper().copy(),
                    instanceStore.getReferenceMapper().copy(),
                    instanceStore.getIndexEntryMapper().copy(),
                    idProvider.copy(),
                    columnStore.copy(),
                    allocatorStore.copy(),
                    instanceSearchService.copy()
            );
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    instanceStore,
                    instanceSearchService
            );
        }
    }

}
