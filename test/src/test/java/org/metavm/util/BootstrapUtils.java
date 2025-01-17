package org.metavm.util;

import org.metavm.application.Application;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.entity.*;
import org.metavm.entity.natives.StdFunction;
import org.metavm.event.MockEventQueue;
import org.metavm.object.instance.ChangeLogManager;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.cache.LocalCache;
import org.metavm.object.instance.log.InstanceLogServiceImpl;
import org.metavm.object.type.*;
import org.metavm.system.IdGenerator;
import org.metavm.system.IdService;
import org.metavm.system.MemoryBlockRepository;
import org.metavm.system.RegionManager;
import org.metavm.system.persistence.MemBlockMapper;
import org.metavm.system.persistence.MemRegionMapper;
import org.metavm.task.*;
import org.metavm.user.PlatformUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class BootstrapUtils {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapUtils.class);

    private static volatile BootState state;

    public static BootstrapResult bootstrap() {
        ContextUtil.resetProfiler();
        if (state != null) {
            var defContext = state.defContext();
            ModelDefRegistry.setDefContext(defContext);
            StdFunction.setEmailSender(MockEmailSender.INSTANCE);
//            defContext.buildMemoryIndex();
//            StdFunction.initializeFromDefContext(defContext, false);
//            PrimitiveKind.initialize(defContext);
//            StdKlass.initialize(defContext, false);
//            StdMethod.initialize(defContext, false);
//            StdField.initialize(defContext, false);
            ParameterizedStore.getMap().clear();
            var state = BootstrapUtils.state.copy();
            var instanceStore = new MemInstanceStore(
                    state.instanceMapper(),
                    state.indexEntryMapper(),
                    state.referenceMapper(),
                    new LocalCache()
            );
            var idProvider = new IdService(new IdGenerator(state.blockRepository()));
            var instanceSearchService = state.instanceSearchService();
            Hooks.SEARCH_BULK = instanceSearchService::bulk;
            var instanceContextFactory =
                    TestUtils.getInstanceContextFactory(idProvider, instanceStore);
            var entityContextFactory = new EntityContextFactory(instanceContextFactory, instanceStore.getIndexEntryMapper());
            var metaContextCache = new MetaContextCache(entityContextFactory);
            entityContextFactory.setInstanceLogService(
                    new InstanceLogServiceImpl(entityContextFactory, instanceStore, new MockTransactionOperations(), metaContextCache)
            );
            entityContextFactory.setDefContext(defContext);
            var changeLogManager = new ChangeLogManager(entityContextFactory);
            var taskManager = new TaskManager(entityContextFactory, new MockTransactionOperations());
            new MockEventQueue();
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
            var transactionOps = new MockTransactionOperations();
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
                    state.typeTagStore(),
                    metaContextCache,
                    changeLogManager,
                    taskManager,
                    new SchedulerAndWorker(new Scheduler(entityContextFactory, transactionOps),
                            new Worker(entityContextFactory, transactionOps, new DirectTaskRunner(), metaContextCache), metaContextCache, entityContextFactory)
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
        var blockRepository = new MemoryBlockRepository();
        var idProvider = new IdService(new IdGenerator(blockRepository));
        var instanceStore = new MemInstanceStore(new LocalCache());
        var instanceSearchService = new MemInstanceSearchServiceV2();
        var instanceContextFactory =
                TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, instanceStore.getIndexEntryMapper());
        var metaContextCache = new MetaContextCache(entityContextFactory);
        entityContextFactory.setInstanceLogService(
                new InstanceLogServiceImpl(entityContextFactory, instanceStore, new MockTransactionOperations(), metaContextCache)
        );
        var stdIdStore = new MemoryStdIdStore();
        var bootstrap = new Bootstrap(
                entityContextFactory,
                new StdAllocators(allocatorStore),
                columnStore,
                typeTagStore
        );
        bootstrap.setClassBlacklist(classBlacklist);
        bootstrap.setFieldBlacklist(fieldBlacklist);
        bootstrap.boot();
        var changeLogManager = new ChangeLogManager(entityContextFactory);
        var taskManager = new TaskManager(entityContextFactory, new MockTransactionOperations());
        new MockEventQueue();
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
                    blockRepository.copy(),
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
        Hooks.SEARCH_BULK = instanceSearchService::bulk;
        var transactionOps = new MockTransactionOperations();
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
                typeTagStore,
                metaContextCache,
                changeLogManager,
                taskManager,
                new SchedulerAndWorker(new Scheduler(entityContextFactory, transactionOps),
                        new Worker(entityContextFactory, transactionOps, new DirectTaskRunner(), metaContextCache), metaContextCache, entityContextFactory)
        );
    }

    private static SystemDefContext copyDefContext(EntityContextFactory entityContextFactory, EntityIdProvider idProvider, SystemDefContext sysDefContext) {
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
