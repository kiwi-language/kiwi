package tech.metavm.autograph;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.common.RefDTO;
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
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.instance.rest.InstanceFieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.TypeQuery;
import tech.metavm.system.BlockManager;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
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
    private ExecutorService executor;
    private TypeManager typeManager;
    private InstanceManager instanceManager;

    @Override
    protected void setUp() throws ExecutionException, InterruptedException {
        StandardTypes.setHolder(new ThreadLocalStandardTypesHolder());
        NativeFunctions.setHolder(new ThreadLocalNativeFunctionsHolder());
        TestUtils.clearDirectory(new File(HOME));
        executor = Executors.newSingleThreadExecutor();
        var bootResult = executor.submit(() -> {
            FlowSavingContext.initConfig();
            return BootstrapUtils.bootstrap();
        }).get();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        typeManager = new TypeManager(
                bootResult.entityContextFactory(),
                new EntityQueryService(instanceQueryService),
                new TaskManager(bootResult.entityContextFactory(), new MockTransactionOperations()),
                new MockTransactionOperations()
        );
        instanceManager = new InstanceManager(bootResult.entityContextFactory(),
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
        executor.close();
    }

    public void test() throws ExecutionException, InterruptedException {
        main.run();
        executor.submit(() -> {
            var types = typeManager.query(new TypeQuery(
                    "商品",
                    List.of(TypeCategory.CLASS.code()),
                    false,
                    false,
                    false,
                    null,
                    List.of(),
                    1
                    , 20
            )).data();
            Assert.assertEquals(1, types.size());
            var productType = types.get(0);
            types = typeManager.query(new TypeQuery(
                    "SKU",
                    List.of(TypeCategory.CLASS.code()),
                    false,
                    false,
                    false,
                    null,
                    List.of(),
                    1
                    , 20
            )).data();
            Assert.assertEquals(1, types.size());
            var skuType = types.get(0);
            var skuChildArrayTypeId = typeManager.getArrayType(skuType.id(), ArrayKind.CHILD.code()).type().id();
            var product = InstanceDTO.createClassInstance(
                    productType.getRef(),
                    List.of(
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(productType, "title"),
                                    PrimitiveFieldValue.createString("鞋子")
                            ),
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(productType, "skus"),
                                    InstanceFieldValue.of(
                                            InstanceDTO.createArrayInstance(
                                                    RefDTO.fromId(skuChildArrayTypeId),
                                                    true,
                                                    List.of(
                                                            InstanceFieldValue.of(
                                                                    InstanceDTO.createClassInstance(
                                                                            skuType.getRef(),
                                                                            List.of(
                                                                                    InstanceFieldDTO.create(
                                                                                            TestUtils.getFieldIdByCode(skuType, "title"),
                                                                                            PrimitiveFieldValue.createString("40")
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            TestUtils.getFieldIdByCode(skuType, "amount"),
                                                                                            PrimitiveFieldValue.createLong(100)
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            TestUtils.getFieldIdByCode(skuType, "price"),
                                                                                            PrimitiveFieldValue.createDouble(100.0)
                                                                                    )
                                                                            )
                                                                    )
                                                            ),
                                                            InstanceFieldValue.of(
                                                                    InstanceDTO.createClassInstance(
                                                                            skuType.getRef(),
                                                                            List.of(
                                                                                    InstanceFieldDTO.create(
                                                                                            TestUtils.getFieldIdByCode(skuType, "title"),
                                                                                            PrimitiveFieldValue.createString("41")
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            TestUtils.getFieldIdByCode(skuType, "amount"),
                                                                                            PrimitiveFieldValue.createLong(100)
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            TestUtils.getFieldIdByCode(skuType, "price"),
                                                                                            PrimitiveFieldValue.createDouble(100.0)
                                                                                    )
                                                                            )
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            );
            var productId = TestUtils.doInTransaction(() -> instanceManager.create(product));
            var loadedProduct = instanceManager.get(productId, 1).instance();
            MatcherAssert.assertThat(loadedProduct, new InstanceDTOMatcher(product, TestUtils.extractDescendantIds(loadedProduct)));
        }).get();
    }

//    public void testResend() {
//        LoginUtils.loginWithAuthFile(AUTH_FILE, typeClient);
//        var request = NncUtils.readJsonFromFile(REQUEST_FILE, BatchSaveRequest.class);
//        HttpUtils.post("/type/batch-save", request, new TypeReference<List<Long>>() {
//        });
//    }

}