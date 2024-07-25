package org.metavm.util;

import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.entity.*;
import org.metavm.entity.natives.StdFunction;
import org.metavm.event.MockEventQueue;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.log.InstanceLogServiceImpl;
import org.metavm.object.instance.log.TaskHandler;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.object.type.*;
import org.metavm.system.IdService;
import org.metavm.system.RegionManager;
import org.metavm.system.persistence.MemBlockMapper;
import org.metavm.system.persistence.MemRegionMapper;
import org.metavm.task.SchedulerRegistry;

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
                new InstanceLogServiceImpl(entityContextFactory, instanceSearchService, instanceStore, new MockTransactionOperations(), List.of(
                        new TaskHandler(entityContextFactory, new MockTransactionOperations())
//                        new VersionHandler(new MockEventQueue())
                ), new MockEventQueue())
        );
        entityContextFactory.setDefaultAsyncLogProcess(false);
        return entityContextFactory;
    }

    public static BootstrapResult bootstrap() {
        if (state != null) {
            var defContext = state.defContext();
            ModelDefRegistry.setDefContext(defContext);
            StdFunction.initializeFromDefContext(defContext);
            StdFunction.setEmailSender(MockEmailSender.INSTANCE);
            StdKlass.initialize(defContext);
            StdMethod.initialize(defContext);
            StdField.initialize(defContext);
            var state = BootstrapUtils.state.copy();
            var instanceStore = new MemInstanceStore(
                    state.instanceMapper(),
                    state.indexEntryMapper(),
                    state.referenceMapper()
            );
            var idProvider = new IdService(state.blockMapper(), new RegionManager(state.regionMapper()));
            var instanceSearchService = state.instanceSearchService();
            var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
            entityContextFactory.setDefContext(defContext);
            TestUtils.doInTransactionWithoutResult(() -> {
                try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    SchedulerRegistry.initialize(platformContext);
                    var globalTagAssigner = GlobalKlassTagAssigner.initialize(platformContext);
                    try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                        BeanDefinitionRegistry.initialize(context);
                        KlassTagAssigner.initialize(context, globalTagAssigner);
                        context.finish();
                    }
                    platformContext.finish();
                }
            });
            return new BootstrapResult(
                    defContext,
                    entityContextFactory,
                    idProvider,
                    state.blockMapper(),
                    state.regionMapper(),
                    instanceStore,
                    instanceSearchService,
                    state.allocatorStore(),
                    state.columnStore(),
                    state.stdIdStore(),
                    state.typeTagStore()
            );
        } else {
            StdFunction.setEmailSender(MockEmailSender.INSTANCE);
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
            var typeTagStore = new MemTypeTagStore();
            var stdIdStore = new MemoryStdIdStore();
            var bootstrap = new Bootstrap(
                    entityContextFactory,
                    new StdAllocators(allocatorStore),
                    columnStore,
                    typeTagStore,
                    stdIdStore
            );
            bootstrap.boot();
            TestUtils.doInTransactionWithoutResult(() -> bootstrap.save(true));
            state = new BootState(
                    ModelDefRegistry.getDefContext(),
                    instanceStore.getInstanceMapper().copy(),
                    instanceStore.getReferenceMapper().copy(),
                    instanceStore.getIndexEntryMapper().copy(),
                    regionMapper.copy(),
                    blockMapper.copy(),
                    columnStore.copy(),
                    typeTagStore.copy(),
                    stdIdStore.copy(),
                    allocatorStore.copy(),
                    instanceSearchService.copy()
            );
            TestUtils.doInTransactionWithoutResult(() -> {
                try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    SchedulerRegistry.initialize(platformContext);
                    var globalTagAssigner = GlobalKlassTagAssigner.initialize(platformContext);
                    try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                        BeanDefinitionRegistry.initialize(context);
                        KlassTagAssigner.initialize(context, globalTagAssigner);
                        context.finish();
                    }
                    platformContext.finish();
                }
            });
            return new BootstrapResult(
                    ModelDefRegistry.getDefContext(),
                    entityContextFactory,
                    idProvider,
                    blockMapper,
                    regionMapper,
                    instanceStore,
                    instanceSearchService,
                    allocatorStore,
                    columnStore,
                    stdIdStore,
                    typeTagStore
            );
        }
    }

}
