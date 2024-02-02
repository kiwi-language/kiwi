package tech.metavm.autograph;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.entity.natives.ThreadLocalNativeFunctionsHolder;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.AllocatorStore;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeQuery;
import tech.metavm.object.version.VersionManager;
import tech.metavm.system.BlockManager;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainTest extends TestCase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/java";

    public static final String SHOPPING_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/shopping";

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

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
    private AllocatorStore allocatorStore;
    private FlowExecutionService flowExecutionService;

    @Override
    protected void setUp() throws ExecutionException, InterruptedException {
        StandardTypes.setHolder(new ThreadLocalStandardTypesHolder());
        NativeFunctions.setHolder(new ThreadLocalNativeFunctionsHolder());
        ModelDefRegistry.setHolder(new ThreadLocalDefContextHolder());
        TestUtils.clearDirectory(new File(HOME));
        executor = Executors.newSingleThreadExecutor();
        var bootResult = executor.submit(() -> {
            FlowSavingContext.initConfig();
            return BootstrapUtils.bootstrap();
        }).get();
        allocatorStore = bootResult.allocatorStore();
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
        FlowManager flowManager = new FlowManager(bootResult.entityContextFactory());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        flowExecutionService = new FlowExecutionService(bootResult.entityContextFactory());
        typeManager.setFlowExecutionService(flowExecutionService);
        var blockManager = new BlockManager(bootResult.blockMapper());
        typeClient = new MockTypeClient(typeManager, blockManager, instanceManager, executor, new MockTransactionOperations());
        main = new Main(HOME, SOURCE_ROOT, AUTH_FILE, typeClient, bootResult.allocatorStore());
        FlowSavingContext.initConfig();
        typeManager.setVersionManager(new VersionManager(bootResult.entityContextFactory()));
    }

    @Override
    protected void tearDown() throws Exception {
        main = null;
        typeClient = null;
        executor.close();
    }

    public void test() throws ExecutionException, InterruptedException {
        main.run();
        var ref = new Object() {
            long productTypeId;
        };
        executor.submit(() -> {
            var productType = queryClassType("商品");
            ref.productTypeId = productType.id();
            Assert.assertNotNull(NncUtils.find(productType.getClassParam().flows(), f -> "setSkus".equals(f.code())));
            var skuType = queryClassType("SKU");
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
                                                                                            PrimitiveFieldValue.createDouble(90.0)
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
            var productMapping = TestUtils.getDefaultMapping(productType);
            var productViewType = typeManager.getType(new GetTypeRequest(productMapping.targetTypeRef().id(), false)).type();
            var productViews = instanceManager.query(
                    new InstanceQueryDTO(
                            productViewType.id(),
                            productMapping.id(),
                            null,
                            null,
                            List.of(),
                            1,
                            20,
                            false,
                            false,
                            List.of()
                    )
            ).page().data();
            Assert.assertEquals(1, productViews.size());
            var productView = productViews.get(0);
            var priceFieldValue = (PrimitiveFieldValue) productView.getFieldValue(
                    TestUtils.getFieldIdByCode(productViewType, "price"));
            Assert.assertEquals(95.0, (double) priceFieldValue.getValue(), 0.0);
        }).get();
        CompilerConfig.setMethodBlacklist(Set.of("tech.metavm.lab.Product.setSkus"));
        main = new Main(HOME, SOURCE_ROOT, AUTH_FILE, typeClient, allocatorStore);
        main.run();
        executor.submit(() -> {
            var productType = typeManager.getType(new GetTypeRequest(ref.productTypeId, false)).type();
            Assert.assertNull(NncUtils.find(productType.getClassParam().flows(), f -> "setSkus".equals(f.code())));
        }).get();
    }

    public void testShopping() throws ExecutionException, InterruptedException {
        main = new Main(HOME, SHOPPING_SOURCE_ROOT, AUTH_FILE, typeClient, allocatorStore);
        main.run();
        executor.submit(() -> {
            var productStateType = queryClassType("商品状态");
            var productNormalStateId = TestUtils.getEnumConstantIdByName(productStateType, "正常");
            var productType = queryClassType("AST产品");
            var product = TestUtils.createInstanceWithCheck(instanceManager, InstanceDTO.createClassInstance(
                    productType.getRef(),
                    List.of(
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(productType, "title"),
                                    PrimitiveFieldValue.createString("鞋子")
                            ),
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(productType, "orderCount"),
                                    PrimitiveFieldValue.createLong(0L)
                            ),
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(productType, "price"),
                                    PrimitiveFieldValue.createLong(100L)
                            ),
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(productType, "inventory"),
                                    PrimitiveFieldValue.createLong(100L)
                            ),
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(productType, "state"),
                                    ReferenceFieldValue.create(productNormalStateId)
                            )
                    )
            ));
            var directCouponType = queryClassType("AST立减优惠券");
            var couponStateType = queryClassType("优惠券状态");
            var couponNormalStateId = TestUtils.getEnumConstantIdByName(couponStateType, "未使用");
            var coupon = TestUtils.createInstanceWithCheck(instanceManager, InstanceDTO.createClassInstance(
                    directCouponType.getRef(),
                    List.of(
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(directCouponType, "discount"),
                                    PrimitiveFieldValue.createLong(5L)
                            ),
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(directCouponType, "state"),
                                    ReferenceFieldValue.create(couponNormalStateId)
                            ),
                            InstanceFieldDTO.create(
                                    TestUtils.getFieldIdByCode(directCouponType, "product"),
                                    ReferenceFieldValue.create(product.id())
                            )
                    )
            ));
            var buyMethodId = TestUtils.getMethodIdByCode(productType, "buy");
            var couponType = queryClassType("AST优惠券");
            var couponArrayType = typeManager.getArrayType(couponType.id(), ArrayKind.READ_WRITE.code()).type();
            var order = TestUtils.doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    buyMethodId,
                    product.id(),
                    List.of(
                            PrimitiveFieldValue.createLong(1L),
                            InstanceFieldValue.of(InstanceDTO.createArrayInstance(
                                    couponArrayType.getRef(),
                                    false,
                                    List.of(ReferenceFieldValue.create(coupon.id()))
                            ))
                    )
            )));
            var orderType = queryClassType("AST订单");
            var price = (long) ((PrimitiveFieldValue) order.getFieldValue(TestUtils.getFieldIdByCode(orderType, "price"))).getValue();
            var orderCoupons = ((InstanceFieldValue) order.getFieldValue(TestUtils.getFieldIdByCode(orderType, "coupons"))).getInstance();
            Assert.assertEquals(1, orderCoupons.getArraySize());
            Assert.assertEquals(95, price);
        }).get();
    }

    public void _testLab() {
        main = new Main(HOME, LAB_SOURCE_ROOT, AUTH_FILE, typeClient, allocatorStore);
        main.run();
    }

    private TypeDTO queryClassType(String name) {
        var types = typeManager.query(new TypeQuery(
                name,
                List.of(TypeCategory.CLASS.code(), TypeCategory.ENUM.code(),TypeCategory.INTERFACE.code()),
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

//    public void testResend() {
//        LoginUtils.loginWithAuthFile(AUTH_FILE, typeClient);
//        var request = NncUtils.readJsonFromFile(REQUEST_FILE, BatchSaveRequest.class);
//        HttpUtils.post("/type/batch-save", request, new TypeReference<List<Long>>() {
//        });
//    }

}