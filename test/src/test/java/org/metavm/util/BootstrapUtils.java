package org.metavm.util;

import org.metavm.application.Application;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.ddl.CommitService;
import org.metavm.entity.*;
import org.metavm.entity.natives.StdFunction;
import org.metavm.event.MockEventQueue;
import org.metavm.object.instance.ChangeLogManager;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.log.InstanceLogServiceImpl;
import org.metavm.object.instance.persistence.MemMapperRegistry;
import org.metavm.object.instance.persistence.MockSchemaManager;
import org.metavm.object.type.*;
import org.metavm.system.IdGenerator;
import org.metavm.system.IdService;
import org.metavm.system.MemoryBlockRepository;
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
            var state = BootstrapUtils.state.copy();
            var mapperRegistry = state.instanceMapperRegistry();
            var idProvider = new IdService(new IdGenerator(state.blockRepository()));
            var instanceSearchService = state.instanceSearchService();
            Hooks.SEARCH_BULK = instanceSearchService::bulk;
            var instanceContextFactory =
                    TestUtils.getInstanceContextFactory(idProvider, mapperRegistry);
            var entityContextFactory = new EntityContextFactory(instanceContextFactory);
            var metaContextCache = new MetaContextCache(entityContextFactory);
            entityContextFactory.setInstanceLogService(
                    new InstanceLogServiceImpl(entityContextFactory, new MockTransactionOperations(), metaContextCache)
            );
            entityContextFactory.setDefContext(defContext);
            var changeLogManager = new ChangeLogManager(entityContextFactory);
            var taskManager = new TaskManager(entityContextFactory, new MockTransactionOperations());
            new MockEventQueue();
            var userId = createApp(entityContextFactory, mapperRegistry);
            var transactionOps = new MockTransactionOperations();
            var schemaManager = new MockSchemaManager(mapperRegistry);
            return new BootstrapResult(
                    defContext,
                    entityContextFactory,
                    idProvider,
                    instanceSearchService,
                    state.allocatorStore(),
                    state.columnStore(),
                    state.stdIdStore(),
                    state.typeTagStore(),
                    metaContextCache,
                    changeLogManager,
                    taskManager,
                    new SchedulerAndWorker(new Scheduler(entityContextFactory, transactionOps),
                    new Worker(entityContextFactory, transactionOps, new DirectTaskRunner(), metaContextCache), metaContextCache, entityContextFactory),
                    mapperRegistry,
                    schemaManager,
                    new CommitService(schemaManager, entityContextFactory),
                    userId
            );
        } else {
            return create(true, new MemAllocatorStore(), new MemColumnStore(), new MemTypeTagStore(), Set.of(), Set.of());
        }
    }

    public static BootstrapResult create(boolean saveState,
                                         MemAllocatorStore allocatorStore,
                                         MemColumnStore columnStore,
                                         MemTypeTagStore typeTagStore,
                                         Set<Class<?>> classBlacklist,
                                         Set<Field> fieldBlacklist) {
        generateIds(allocatorStore);
        StdFunction.setEmailSender(MockEmailSender.INSTANCE);
        var blockRepository = new MemoryBlockRepository();
        var idProvider = new IdService(new IdGenerator(blockRepository));
        var mapperRegistry = new MemMapperRegistry();
        var instanceSearchService = new MemInstanceSearchServiceV2();
        var instanceContextFactory =
                TestUtils.getInstanceContextFactory(idProvider, mapperRegistry);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory);
        var metaContextCache = new MetaContextCache(entityContextFactory);
        entityContextFactory.setInstanceLogService(
                new InstanceLogServiceImpl(entityContextFactory, new MockTransactionOperations(), metaContextCache)
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
        var defContext = copyDefContext(entityContextFactory, idProvider, (SystemDefContext) ModelDefRegistry.getDefContext());
        if(saveState) {
            state = new BootState(
                    defContext,
                    blockRepository.copy(),
                    columnStore.copy(),
                    typeTagStore.copy(),
                    stdIdStore.copy(),
                    allocatorStore.copy(),
                    instanceSearchService.copy(),
                    mapperRegistry.copy()
            );
        }
        var userId = createApp(entityContextFactory, mapperRegistry);
        Hooks.SEARCH_BULK = instanceSearchService::bulk;
        var transactionOps = new MockTransactionOperations();
        var schemaManager = new MockSchemaManager(mapperRegistry);
        return new BootstrapResult(
                ModelDefRegistry.getDefContext(),
                entityContextFactory,
                idProvider,
                instanceSearchService,
                allocatorStore,
                columnStore,
                stdIdStore,
                typeTagStore,
                metaContextCache,
                changeLogManager,
                taskManager,
                new SchedulerAndWorker(new Scheduler(entityContextFactory, transactionOps), new Worker(entityContextFactory, transactionOps, new DirectTaskRunner(), metaContextCache), metaContextCache, entityContextFactory),
                mapperRegistry,
                schemaManager,
                new CommitService(schemaManager, entityContextFactory),
                userId
        );
    }

    private static Id createApp(EntityContextFactory entityContextFactory, MemMapperRegistry mapperRegistry) {
        return TestUtils.doInTransaction(() -> {
            try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                SchedulerRegistry.initialize(platformContext);
                var globalTagAssigner = GlobalKlassTagAssigner.initialize(platformContext);
                var user = new PlatformUser(platformContext.allocateRootId(), "demo", "123456", "demo", List.of());
                var app = new Application(platformContext.allocateRootId(), "demo", user);
                platformContext.bind(app);
                TestConstants.APP_ID = app.getId().getTreeId();
                mapperRegistry.createInstanceMapper(TestConstants.APP_ID, "instance");
                mapperRegistry.createIndexEntryMapper(TestConstants.APP_ID, "index_entry");
                try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                    BeanDefinitionRegistry.initialize(context);
                    KlassTagAssigner.initialize(context, globalTagAssigner);
                    KlassSourceCodeTagAssigner.initialize(context);
                    context.finish();
                }
                platformContext.finish();
                return user.getId();
            }
        });
    }

    private static void generateIds(AllocatorStore allocatorStore) {
        var stdAllocators = new StdAllocators(allocatorStore);
        var idGenerator = new StdIdGenerator(() -> stdAllocators.allocate(1).getFirst());
        idGenerator.generate();

        idGenerator.getIds().forEach((identity, id) -> {
            if (id.getNodeId() == 0L)
                stdAllocators.putId(identity, id, idGenerator.getNextNodeId(identity));
            else
                stdAllocators.putId(identity, id);
        });
        stdAllocators.save();
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

    public static void clearState() {
        state = null;
    }

}
