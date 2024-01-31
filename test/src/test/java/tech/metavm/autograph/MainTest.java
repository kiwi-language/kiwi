package tech.metavm.autograph;

import junit.framework.TestCase;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.ThreadLocalStandardTypesHolder;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.entity.natives.ThreadLocalNativeFunctionsHolder;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.type.TypeManager;
import tech.metavm.system.BlockManager;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MainTest extends TestCase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/java";

    public static final String AUTH_FILE = "/Users/leen/workspace/object/compiler/src/test/resources/auth";

    public static final String REQUEST_FILE =
            "/Users/leen/workspace/object/compiler/src/test/resources/requests/request.2023-12-05 10:44:23.json";

    public static final String AUTH_FIle = "/Users/leen/workspace/object/compiler/src/test/resources/auth";

    public static final String HOME = "/Users/leen/workspace/object/test/src/test/resources/home";

    public static final String HOME_1 = System.getProperty("user.home") + File.separator + ".metavm_1";

    private Main main;
    private TypeClient typeClient;

    @Override
    protected void setUp() throws ExecutionException, InterruptedException {
        StandardTypes.setHolder(new ThreadLocalStandardTypesHolder());
        NativeFunctions.setHolder(new ThreadLocalNativeFunctionsHolder());
        TestUtils.clearDirectory(new File(HOME));
        var executor = Executors.newSingleThreadExecutor();
        var bootResult = executor.submit(() -> {
            FlowSavingContext.initConfig();
            return BootstrapUtils.bootstrap();
        }).get();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        var typeManager = new TypeManager(
                bootResult.entityContextFactory(),
                new EntityQueryService(instanceQueryService),
                new TaskManager(bootResult.entityContextFactory(), new MockTransactionOperations()),
                new MockTransactionOperations()
        );
        var instanceManager = new InstanceManager(bootResult.entityContextFactory(),
                bootResult.instanceStore(), instanceQueryService);
        typeManager.setInstanceManager(instanceManager);
        var flowManager = new FlowManager(bootResult.entityContextFactory());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        typeManager.setFlowExecutionService(new FlowExecutionService(bootResult.entityContextFactory()));
        var blockManager = new BlockManager(bootResult.blockMapper());
        typeClient = new MockTypeClient(typeManager, blockManager, instanceManager, executor, new MockTransactionOperations());
        main = new Main(HOME, SOURCE_ROOT, AUTH_FILE, typeClient, bootResult.allocatorStore());
        FlowSavingContext.initConfig();
    }

    @Override
    protected void tearDown() throws Exception {
        main = null;
        typeClient = null;
    }

    public void test() {
        main.run();
    }

//    public void testResend() {
//        LoginUtils.loginWithAuthFile(AUTH_FILE, typeClient);
//        var request = NncUtils.readJsonFromFile(REQUEST_FILE, BatchSaveRequest.class);
//        HttpUtils.post("/type/batch-save", request, new TypeReference<List<Long>>() {
//        });
//    }

}