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
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.MockSchemaManager;
import org.metavm.object.type.*;
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

    public static final String HOME = TestUtils.getResourcePath("home");

    protected TypeClient typeClient;
    protected ExecutorService executor;
    protected TypeManager typeManager;
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
    protected void setUp() {
        setUp0();
    }

    protected void setUp0() {
        AUTH_CONFIG = AuthConfig.fromFile(TestUtils.getResourcePath("auth"));
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
        typeManager = new TypeManager(bootResult.entityContextFactory(), new BeanManager(), new MockSchemaManager(bootResult.mapperRegistry()));
        typeClient = new MockTypeClient(typeManager, executor, new MockTransactionOperations());
        FlowSavingContext.initConfig();
        var entityQueryService = new EntityQueryService(bootResult.instanceSearchService());
        var roleManager = new RoleManager(entityContextFactory, entityQueryService);
        loginService = new LoginService(bootResult.entityContextFactory());
        var verificationCodeService = new VerificationCodeService(entityContextFactory, new MockEmailService());
        platformUserManager = new PlatformUserManager(entityContextFactory,
                loginService, entityQueryService, new MockEventQueue(), verificationCodeService);
        applicationManager = new ApplicationManager(entityContextFactory, roleManager, platformUserManager,
                verificationCodeService, bootResult.idProvider(), entityQueryService, bootResult.schemaManager());
        var apiService = new ApiService(entityContextFactory, bootResult.metaContextCache(), instanceQueryService);
        apiClient = new ApiClient(apiService);
        ContextUtil.resetProfiler();
    }

    @SuppressWarnings("unused")
    protected void repeatUntilFailure(Runnable action) {
        //noinspection InfiniteLoopStatement
        for (;;) {
            action.run();
            BootstrapUtils.clearState();
            setUp0();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        executor.close();
        entityContextFactory = null;
        typeClient = null;
        executor = null;
        typeManager = null;
        schedulerAndWorker = null;
        allocatorStore = null;
        columnStore = null;
        typeTagStore = null;
        applicationManager = null;
        loginService = null;
        platformUserManager = null;
        apiClient = null;
        metaContextCache = null;
//        SystemConfig.setHybridMode();
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
        new Main(HOME, TestUtils.getResourcePath(sourceRoot), TestConstants.TARGET, APP_ID, "__fake_token__", typeClient, allocatorStore, columnStore, typeTagStore).run();
        submit(() -> TestUtils.waitForDDLPrepared(schedulerAndWorker));
    }

    protected void clearHome() {
        Utils.clearDirectory(HOME);
    }

    protected void waitForAllTasksDone() {
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
    }

    protected Id saveInstance(String className, Map<String, Object> fields) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, fields));
    }

    protected Object callMethod(Object receiver, String methodName, List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(receiver, methodName, arguments));
    }

    protected ApiSearchResult search(String className, Map<String, Object> query, int page, int pageSize) {
        return apiClient.search(className, query, page, pageSize);
    }

    protected ApiObject getObject(Id id) {
        return apiClient.getObject(id);
    }

    protected Object getStatic(String className, String fieldName) {
        return apiClient.getStatic(className, fieldName);
    }

}
