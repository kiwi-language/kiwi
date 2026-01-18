package org.manul.util;

import org.manul.application.ApplicationManager;
import org.manul.application.rest.dto.ApplicationDTO;
import org.manul.common.MockEmailService;
import org.manul.ddl.CommitService;
import org.manul.ddl.DeployService;
import org.manul.entity.*;
import org.manul.entity.natives.StdFunction;
import org.manul.object.instance.ChangeLogManager;
import org.manul.object.instance.MemInstanceSearchServiceV2;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.log.InstanceLogServiceImpl;
import org.manul.object.instance.persistence.MemMapperRegistry;
import org.manul.object.instance.persistence.MockSchemaManager;
import org.manul.object.instance.persistence.SchemaManager;
import org.manul.object.type.*;
import org.manul.system.IdGenerator;
import org.manul.system.IdService;
import org.manul.system.MemoryBlockRepository;
import org.manul.task.DirectTaskRunner;
import org.manul.task.Scheduler;
import org.manul.task.TaskManager;
import org.manul.task.Worker;
import org.manul.user.*;

import java.util.Objects;

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
        var entityQueryService = new EntityQueryService(instanceSearchService);
        var verificationCodeService = new VerificationCodeService(entityContextFactory, new MockEmailService());
        var transactionOps = new MockTransactionOperations();
        var schemaManager = new MockSchemaManager(mapperRegistry);
        // set up Hooks.CREATE_INDEX_REBUILD_TASK
        new DeployService(schemaManager, instanceSearchService, entityContextFactory);
        var roleManager = new RoleManager(entityContextFactory, entityQueryService);
        var userManager = new PlatformUserManager(entityContextFactory, new LoginService(entityContextFactory), entityQueryService,
                verificationCodeService);
        var appManager = new ApplicationManager(entityContextFactory,
                roleManager,
                userManager,
                verificationCodeService,
                idProvider,
                entityQueryService,
                schemaManager,
                instanceSearchService
        );
        initPlatform(entityContextFactory, schemaManager, userManager, appManager);
        var userId = getDefaultUserId(entityContextFactory);
        TestConstants.APP_ID = appManager.save(new ApplicationDTO(null, "demo", userId.toString()));

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

    private static void initPlatform(EntityContextFactory entityContextFactory, SchemaManager schemaManager, PlatformUserManager userManager, ApplicationManager appManager) {
        var initializer = new PlatformInitializer(entityContextFactory, schemaManager, userManager, appManager);
        TestUtils.doInTransactionWithoutResult(initializer::init);
    }

    private static Id getDefaultUserId(EntityContextFactory entityContextFactory) {
        try (var platformContext = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
            var user = (PlatformUser) platformContext.selectFirstByKey(PlatformUser.IDX_LOGIN_NAME, Instances.stringInstance(Constants.DEFAULT_USER));
            Objects.requireNonNull(user, () -> "Default user " + Constants.DEFAULT_USER + " not found");
            return user.getId();
        }
    }

    private static SystemDefContext copyDefContext(SystemDefContext sysDefContext) {
        return sysDefContext;
    }

}
