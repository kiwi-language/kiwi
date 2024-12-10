package org.metavm.autograph;

import junit.framework.TestCase;
import org.metavm.application.ApplicationManager;
import org.metavm.common.MockEmailService;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityQueryService;
import org.metavm.entity.MetaContextCache;
import org.metavm.event.MockEventQueue;
import org.metavm.flow.FlowSavingContext;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.ClassInstanceWrap;
import org.metavm.object.instance.rest.SearchResult;
import org.metavm.object.type.*;
import org.metavm.object.version.VersionManager;
import org.metavm.system.BlockManager;
import org.metavm.system.IdService;
import org.metavm.user.LoginService;
import org.metavm.user.PlatformUserManager;
import org.metavm.user.RoleManager;
import org.metavm.user.VerificationCodeService;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class CompilerTestBase extends TestCase  {

    private static final Logger logger = LoggerFactory.getLogger(CompilerTestBase.class);

    protected AuthConfig AUTH_CONFIG;
    protected long APP_ID;

    public static final String HOME = "/Users/leen/workspace/object/test/src/test/resources/home";

    protected TypeClient typeClient;
    protected ExecutorService executor;
    protected TypeManager typeManager;
    protected InstanceManager instanceManager;
    protected AllocatorStore allocatorStore;
    protected ColumnStore columnStore;
    protected TypeTagStore typeTagStore;
    protected ApplicationManager applicationManager;
    protected LoginService loginService;
    protected PlatformUserManager platformUserManager;
    protected ApiClient apiClient;
    protected EntityContextFactory entityContextFactory;
    protected SchedulerAndWorker schedulerAndWorker;
    protected MetaContextCache metaContextCache;

    @Override
    protected void setUp() throws ExecutionException, InterruptedException {
        AUTH_CONFIG = AuthConfig.fromFile("/Users/leen/workspace/object/test/src/test/resources/auth");
        SystemConfig.setThreadLocalMode();
        TestUtils.clearDirectory(new File(HOME));
        executor = Executors.newSingleThreadExecutor();
        var bootResult = submit(() -> {
            FlowSavingContext.initConfig();
            return BootstrapUtils.bootstrap();
        });
        APP_ID = TestConstants.APP_ID;
        allocatorStore = bootResult.allocatorStore();
        columnStore = bootResult.columnStore();
        typeTagStore = bootResult.typeTagStore();
        entityContextFactory = bootResult.entityContextFactory();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        metaContextCache = bootResult.metaContextCache();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        typeManager = new TypeManager(bootResult.entityContextFactory(), new BeanManager());
        instanceManager = new InstanceManager(entityContextFactory,
                bootResult.instanceStore(), instanceQueryService, bootResult.metaContextCache());
        var blockManager = new BlockManager(bootResult.blockMapper());
        typeClient = new MockTypeClient(typeManager, blockManager, instanceManager, executor, new MockTransactionOperations());
        FlowSavingContext.initConfig();
        typeManager.setVersionManager(new VersionManager(entityContextFactory));
        var entityQueryService = new EntityQueryService(instanceQueryService);
        var roleManager = new RoleManager(entityContextFactory, entityQueryService);
        loginService = new LoginService(bootResult.entityContextFactory());
        var verificationCodeService = new VerificationCodeService(entityContextFactory, new MockEmailService());
        platformUserManager = new PlatformUserManager(entityContextFactory,
                loginService, entityQueryService, new MockEventQueue(), verificationCodeService);
        applicationManager = new ApplicationManager(entityContextFactory, roleManager, platformUserManager,
                verificationCodeService, (IdService) bootResult.idProvider(), entityQueryService);
        var apiService = new ApiService(entityContextFactory, bootResult.metaContextCache(), instanceQueryService);
        apiClient = new ApiClient(apiService);
        ContextUtil.resetProfiler();
    }

    @Override
    protected void tearDown() throws Exception {
        executor.close();
        entityContextFactory = null;
        typeClient = null;
        executor = null;
        typeManager = null;
        schedulerAndWorker = null;
        instanceManager = null;
        allocatorStore = null;
        columnStore = null;
        typeTagStore = null;
        applicationManager = null;
        loginService = null;
        platformUserManager = null;
        apiClient = null;
        metaContextCache = null;
        SystemConfig.setHybridMode();
    }

    protected void submit(Runnable task) {
        try {
            executor.submit(() -> {
                ContextUtil.resetProfiler();
                task.run();
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T submit(Callable<T> task) {
        try {
            return executor.submit(() -> {
                ContextUtil.resetProfiler();
                return task.call();
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void compileTwice(String sourceRoot) {
        compile(sourceRoot);
//        DebugEnv.buildPatchLog = true;
        compile(sourceRoot);
//        DebugEnv.buildPatchLog = false;
    }

    protected void compile(String sourceRoot) {
        ContextUtil.resetProfiler();
        new Main(HOME, sourceRoot, TestConstants.TARGET, APP_ID, "__fake_token__", typeClient, allocatorStore, columnStore, typeTagStore).run();
        submit(() -> TestUtils.waitForDDLPrepared(schedulerAndWorker));
    }

    protected void clearHome() {
        NncUtils.clearDirectory(HOME);
    }

    protected void waitForAllTasksDone() {
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
    }

    protected String saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    protected Object callMethod(String qualifier, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    protected SearchResult search(String className, Map<String, Object> query, int page, int pageSize) {
        return apiClient.search(className, query, page, pageSize);
    }

    protected ClassInstanceWrap getObject(String id) {
        return apiClient.getObject(id);
    }

    protected Object getStatic(String className, String fieldName) {
        return apiClient.getStatic(className, fieldName);
    }

    protected void deleteObject(String id) {
        TestUtils.doInTransactionWithoutResult(() -> apiClient.deleteInstance(id));
    }

}
