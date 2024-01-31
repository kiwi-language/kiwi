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
import tech.metavm.system.IdService;
import tech.metavm.system.RegionManager;
import tech.metavm.system.persistence.MemBlockMapper;
import tech.metavm.system.persistence.MemRegionMapper;
import tech.metavm.task.JobSchedulerStatus;
import tech.metavm.task.TaskSignal;

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
            var idProvider = new IdService(state.blockMapper(), new RegionManager(state.regionMapper()));
            var instanceSearchService = state.instanceSearchService();
            var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
            var bootstrap = new Bootstrap(
                    entityContextFactory,
                    new StdAllocators(state.allocatorStore()),
                    state.columnStore()
            );
            bootstrap.boot();
            TestUtils.doInTransactionWithoutResult(() -> {
                try(var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    context.bind(new JobSchedulerStatus());
                    context.bind(new TaskSignal(TestConstants.APP_ID));
                    context.finish();
                }
            });
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    state.blockMapper(),
                    instanceStore,
                    instanceSearchService,
                    state.allocatorStore()
            );
        }
        else {
            var regionMapper = new MemRegionMapper();
            var regionManager = new RegionManager(regionMapper);
            regionManager.initialize();
            var blockMapper = new MemBlockMapper();
            var idProvider = new IdService(blockMapper, regionManager);
            var instanceStore = new MemInstanceStore();
            var instanceSearchService = new MemInstanceSearchServiceV2();
            var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
            var allocatorStore = new MemAllocatorStore();
            var columnStore = new MemColumnStore();
            var bootstrap = new Bootstrap(
                    entityContextFactory,
                    new StdAllocators(allocatorStore),
                    columnStore
            );
            bootstrap.boot();
            TestUtils.doInTransactionWithoutResult(() -> bootstrap.save(true));
            state = new BootState(
                    instanceStore.getInstanceMapper().copy(),
                    instanceStore.getReferenceMapper().copy(),
                    instanceStore.getIndexEntryMapper().copy(),
                    regionMapper.copy(),
                    blockMapper.copy(),
                    columnStore.copy(),
                    allocatorStore.copy(),
                    instanceSearchService.copy()
            );
            TestUtils.doInTransactionWithoutResult(() -> {
                try(var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    context.bind(new JobSchedulerStatus());
                    context.bind(new TaskSignal(TestConstants.APP_ID));
                    context.finish();
                }
            });
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    blockMapper,
                    instanceStore,
                    instanceSearchService,
                    allocatorStore
            );
        }
    }

}
