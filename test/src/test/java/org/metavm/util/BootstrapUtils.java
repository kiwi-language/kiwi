package org.metavm.util;

import org.metavm.application.Application;
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
import org.metavm.user.PlatformUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class BootstrapUtils {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapUtils.class);

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
                    var app = new Application("demo",
                            new PlatformUser("demo", "123456", "demo", List.of()));
                    platformContext.bind(app);
                    platformContext.initIds();
                    TestConstants.APP_ID = app.getId().getTreeId();
                    try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                        BeanDefinitionRegistry.initialize(context);
                        KlassTagAssigner.initialize(context, globalTagAssigner);
                        KlassSourceCodeTagAssigner.initialize(context);
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
            return create(true, true, new MemAllocatorStore(), new MemColumnStore(), new MemTypeTagStore(), Set.of(), Set.of());
        }
    }

    public static BootstrapResult create(boolean saveState,
                                         boolean saveIds,
                                         MemAllocatorStore allocatorStore,
                                         MemColumnStore columnStore,
                                         MemTypeTagStore typeTagStore,
                                         Set<Class<?>> classBlacklist,
                                         Set<Field> fieldBlacklist) {
        StdFunction.setEmailSender(MockEmailSender.INSTANCE);
        var regionMapper = new MemRegionMapper();
        var regionManager = new RegionManager(regionMapper);
        regionManager.initialize();
        var blockMapper = new MemBlockMapper();
        var idProvider = new IdService(blockMapper, regionManager);
        var instanceStore = new MemInstanceStore();
        var instanceSearchService = new MemInstanceSearchServiceV2();
        var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
        var stdIdStore = new MemoryStdIdStore();
        var bootstrap = new Bootstrap(
                entityContextFactory,
                new StdAllocators(allocatorStore),
                columnStore,
                typeTagStore,
                stdIdStore
        );
        bootstrap.setClassBlacklist(classBlacklist);
        bootstrap.setFieldBlacklist(fieldBlacklist);
        bootstrap.boot();
        TestUtils.doInTransactionWithoutResult(() -> bootstrap.save(saveIds));
        var defContext = copyDefContext(entityContextFactory, idProvider, (SystemDefContext) ModelDefRegistry.getDefContext());
        if(saveState) {
            state = new BootState(
                    defContext,
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
        }
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                SchedulerRegistry.initialize(platformContext);
                var globalTagAssigner = GlobalKlassTagAssigner.initialize(platformContext);
                var app = new Application("demo",
                        new PlatformUser("demo", "123456", "demo", List.of()));
                platformContext.bind(app);
                platformContext.initIds();
                TestConstants.APP_ID = app.getId().getTreeId();
                try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                    BeanDefinitionRegistry.initialize(context);
                    KlassTagAssigner.initialize(context, globalTagAssigner);
                    KlassSourceCodeTagAssigner.initialize(context);
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

    private static DefContext copyDefContext(EntityContextFactory entityContextFactory, EntityIdProvider idProvider, SystemDefContext sysDefContext) {
//        var bridge = new EntityInstanceContextBridge();
//        var standardInstanceContext = (InstanceContext) entityContextFactory.newBridgedInstanceContext(
//                ROOT_APP_ID, false, null, null,
//                new DefaultIdInitializer(idProvider), bridge, null, null, null, false,
//                builder -> builder.timeout(0L).typeDefProvider(sysDefContext)
//        );
//        var defContext = new ReversedDefContext(standardInstanceContext, sysDefContext);
//        bridge.setEntityContext(defContext);
//        defContext.initializeFrom(sysDefContext);
//        ModelDefRegistry.setDefContext(defContext);
//        return defContext;
        return sysDefContext;
    }

}
