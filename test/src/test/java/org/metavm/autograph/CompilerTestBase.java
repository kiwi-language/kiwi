package org.metavm.autograph;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.application.ApplicationManager;
import org.metavm.common.MockEmailService;
import org.metavm.entity.*;
import org.metavm.entity.natives.NativeFunctions;
import org.metavm.event.MockEventQueue;
import org.metavm.flow.FlowExecutionService;
import org.metavm.flow.FlowManager;
import org.metavm.flow.FlowSavingContext;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.TypeDTO;
import org.metavm.object.type.rest.dto.TypeQuery;
import org.metavm.object.version.VersionManager;
import org.metavm.system.BlockManager;
import org.metavm.system.IdService;
import org.metavm.task.TaskManager;
import org.metavm.user.LoginService;
import org.metavm.user.PlatformUserManager;
import org.metavm.user.RoleManager;
import org.metavm.user.VerificationCodeService;
import org.metavm.util.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class CompilerTestBase extends TestCase  {
    protected AuthConfig AUTH_CONFIG;
    protected long APP_ID = 100L;

    public static final String HOME = "/Users/leen/workspace/object/test/src/test/resources/home";

    protected TypeClient typeClient;
    protected ExecutorService executor;
    protected TypeManager typeManager;
    protected InstanceManager instanceManager;
    protected AllocatorStore allocatorStore;
    protected ColumnStore columnStore;
    protected TypeTagStore typeTagStore;
    protected FlowExecutionService flowExecutionService;
    protected FlowManager flowManager;
    protected ApplicationManager applicationManager;
    protected LoginService loginService;
    protected PlatformUserManager platformUserManager;
    protected ApiClient apiClient;

    @Override
    protected void setUp() throws ExecutionException, InterruptedException {
        AUTH_CONFIG = AuthConfig.fromFile("/Users/leen/workspace/object/test/src/test/resources/auth");
        StandardTypes.setHolder(new ThreadLocalStandardTypesHolder());
        NativeFunctions.setThreadLocalMode();
        ModelDefRegistry.setHolder(new ThreadLocalDefContextHolder());
        TestUtils.clearDirectory(new File(HOME));
        executor = Executors.newSingleThreadExecutor();
        var bootResult = submit(() -> {
            FlowSavingContext.initConfig();
            return BootstrapUtils.bootstrap();
        });
        allocatorStore = bootResult.allocatorStore();
        columnStore = bootResult.columnStore();
        typeTagStore = bootResult.typeTagStore();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        typeManager = new TypeManager(
                bootResult.entityContextFactory(),
                new EntityQueryService(instanceQueryService),
                new TaskManager(bootResult.entityContextFactory(), new MockTransactionOperations()),
                new BeanManager());
        instanceManager = new InstanceManager(bootResult.entityContextFactory(),
                bootResult.instanceStore(), instanceQueryService);
        typeManager.setInstanceManager(instanceManager);
        flowManager = new FlowManager(bootResult.entityContextFactory(), new MockTransactionOperations());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        flowExecutionService = new FlowExecutionService(bootResult.entityContextFactory());
        typeManager.setFlowExecutionService(flowExecutionService);
        var blockManager = new BlockManager(bootResult.blockMapper());
        typeClient = new MockTypeClient(typeManager, blockManager, instanceManager, executor, new MockTransactionOperations());
        FlowSavingContext.initConfig();
        typeManager.setVersionManager(new VersionManager(bootResult.entityContextFactory()));

        var entityQueryService = new EntityQueryService(instanceQueryService);
        var roleManager = new RoleManager(bootResult.entityContextFactory(), entityQueryService);
        loginService = new LoginService(bootResult.entityContextFactory());
        var verificationCodeService = new VerificationCodeService(bootResult.entityContextFactory(), new MockEmailService());
        platformUserManager = new PlatformUserManager(bootResult.entityContextFactory(),
                loginService, entityQueryService, new MockEventQueue(), verificationCodeService);
        applicationManager = new ApplicationManager(bootResult.entityContextFactory(), roleManager, platformUserManager,
                verificationCodeService, (IdService) bootResult.idProvider(), entityQueryService);
        var apiService = new ApiService(bootResult.entityContextFactory());
        apiClient = new ApiClient(apiService);
        ContextUtil.resetProfiler();
    }

    @Override
    protected void tearDown() throws Exception {
        executor.close();
        typeClient = null;
        executor = null;
        typeManager = null;
        instanceManager = null;
        allocatorStore = null;
        columnStore = null;
        typeTagStore = null;
        flowExecutionService = null;
        flowManager = null;
        applicationManager = null;
        loginService = null;
        platformUserManager = null;
        apiClient = null;
        StandardTypes.setHolder(new GlobalStandardTypesHolder());
        NativeFunctions.setDirectMode();
        ModelDefRegistry.setHolder(new GlobalDefContextHolder());
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


    protected TypeDTO queryClassType(String name) {
        return queryClassType(name, List.of(ClassKind.CLASS.code(), ClassKind.ENUM.code(), ClassKind.INTERFACE.code(), ClassKind.VALUE.code()));
    }

    protected void assertNoError(TypeDTO typeDTO) {
        Assert.assertEquals(0, typeDTO.getClassParam().errors().size());
    }

    protected TypeDTO queryClassType(String name, List<Integer> categories) {
        var types = typeManager.query(new TypeQuery(
                name,
                categories,
                false,
                false,
                false,
                null,
                List.of(),
                1
                , 20
        )).data();
        Assert.assertEquals(1, types.size());
        return types.get(0);
    }

    protected TypeDTO getClassTypeByCode(String code) {
        return typeManager.getTypeByCode(code).type();
    }

    protected void compileTwice(String sourceRoot) {
        compile(sourceRoot);
//        DebugEnv.buildPatchLog = true;
        compile(sourceRoot);
//        DebugEnv.buildPatchLog = false;
    }

    protected List<String> compile(String sourceRoot) {
        ContextUtil.resetProfiler();
        return new Main(HOME, sourceRoot, APP_ID, "__fake_token__", typeClient, allocatorStore, columnStore, typeTagStore).run();
    }


}
