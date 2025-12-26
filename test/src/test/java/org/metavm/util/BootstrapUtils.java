package org.metavm.util;

import org.metavm.application.Application;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.ddl.CommitService;
import org.metavm.ddl.DeployService;
import org.metavm.entity.*;
import org.metavm.entity.natives.StdFunction;
import org.metavm.object.instance.ChangeLogManager;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.log.InstanceLogServiceImpl;
import org.metavm.object.instance.persistence.MemMapperRegistry;
import org.metavm.object.instance.persistence.MockSchemaManager;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.object.type.*;
import org.metavm.system.IdGenerator;
import org.metavm.system.IdService;
import org.metavm.system.MemoryBlockRepository;
import org.metavm.task.*;
import org.metavm.user.PlatformUser;

import java.util.List;
import java.util.UUID;

public class BootstrapUtils {

    private static volatile BootState state;

    public static BootstrapResult bootstrap() {
        ContextUtil.resetProfiler();
        if (state == null)
            createState(new DirectoryAllocatorStore("nonexistent"), new MemTypeTagStore());
        return fromState();
    }

    private static BootstrapResult fromState() {
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
        var taskManager = new TaskManager(entityContextFactory);
        createPlatformApp(entityContextFactory);
        var userId = createApp(entityContextFactory, mapperRegistry, instanceSearchService);
        var transactionOps = new MockTransactionOperations();
        var schemaManager = new MockSchemaManager(mapperRegistry);
        new DeployService(schemaManager, instanceSearchService, entityContextFactory);

        var typeManager = new TypeManager(entityContextFactory, new BeanManager(), schemaManager, instanceSearchService);
        return new BootstrapResult(
                defContext,
                entityContextFactory,
                idProvider,
                instanceSearchService,
                state.allocatorStore(),
                state.stdIdStore(),
                state.typeTagStore(),
                metaContextCache,
                changeLogManager,
                taskManager,
                new SchedulerAndWorker(new Scheduler(entityContextFactory, transactionOps),
                        new Worker(entityContextFactory, transactionOps, new DirectTaskRunner(), metaContextCache), metaContextCache, entityContextFactory),
                mapperRegistry,
                schemaManager,
                new CommitService(schemaManager, instanceSearchService, entityContextFactory),
                typeManager,
                userId
        );
    }

    public static void createState(DirectoryAllocatorStore allocatorStore,
                                   MemTypeTagStore typeTagStore) {
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
                new StdAllocators(allocatorStore)
        );
        bootstrap.boot();
        var defContext = copyDefContext((SystemDefContext) ModelDefRegistry.getDefContext());
        state = new BootState(
                defContext,
                blockRepository.copy(),
                typeTagStore.copy(),
                stdIdStore.copy(),
                allocatorStore,
                instanceSearchService.copy(),
                mapperRegistry.copy()
        );
    }

    private static void createPlatformApp(EntityContextFactory entityContextFactory) {
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                SchedulerRegistry.initialize(platformContext);
                GlobalKlassTagAssigner.initialize(platformContext);
                var platformUser = new PlatformUser(platformContext.allocateRootId(), "platform", UUID.randomUUID().toString(), "platform", List.of());
                platformContext.bind(platformUser);
                platformContext.bind(new Application(PhysicalId.of(Constants.PLATFORM_APP_ID, 0), "platform", platformUser));
                platformContext.finish();
            }
        });
    }

    private static Id createApp(EntityContextFactory entityContextFactory, MemMapperRegistry mapperRegistry, InstanceSearchService instanceSearchService) {
        return TestUtils.doInTransaction(() -> {
            try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                var globalTagAssigner = GlobalKlassTagAssigner.getInstance(platformContext);
                var user = new PlatformUser(platformContext.allocateRootId(), "demo", "123456", "demo", List.of());
                var app = new Application(platformContext.allocateRootId(), "demo", user);
                platformContext.bind(app);
                TestConstants.APP_ID = app.getId().getTreeId();
                mapperRegistry.createInstanceMapper(TestConstants.APP_ID, "instance");
                mapperRegistry.createIndexEntryMapper(TestConstants.APP_ID, "index_entry");
                instanceSearchService.createIndex(TestConstants.APP_ID, false);
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

    private static SystemDefContext copyDefContext(SystemDefContext sysDefContext) {
        return sysDefContext;
    }

}
