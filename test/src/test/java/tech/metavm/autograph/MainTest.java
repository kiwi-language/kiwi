package tech.metavm.autograph;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.entity.natives.ThreadLocalNativeFunctionsHolder;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.flow.rest.GetFlowRequest;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.GetParameterizedTypeRequest;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeQuery;
import tech.metavm.object.version.VersionManager;
import tech.metavm.system.BlockManager;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static tech.metavm.util.TestUtils.getFieldIdByCode;

public class MainTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/java";

    public static final String SHOPPING_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/shopping";

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public static final String METAVM_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/metavm";

    public static final String USERS_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/users";

    public static final String AUTH_FILE = "/Users/leen/workspace/object/test/src/test/resources/auth";

    public static final String HOME = "/Users/leen/workspace/object/test/src/test/resources/home";

    private TypeClient typeClient;
    private ExecutorService executor;
    private TypeManager typeManager;
    private InstanceManager instanceManager;
    private AllocatorStore allocatorStore;
    private FlowExecutionService flowExecutionService;
    private FlowManager flowManager;

    @Override
    protected void setUp() throws ExecutionException, InterruptedException {
        StandardTypes.setHolder(new ThreadLocalStandardTypesHolder());
        NativeFunctions.setHolder(new ThreadLocalNativeFunctionsHolder());
        ModelDefRegistry.setHolder(new ThreadLocalDefContextHolder());
        Instances.setHolder(new ThreadLocalBuiltinInstanceHolder());
        TestUtils.clearDirectory(new File(HOME));
        executor = Executors.newSingleThreadExecutor();
        var bootResult = submit(() -> {
            FlowSavingContext.initConfig();
            return BootstrapUtils.bootstrap();
        });
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
        flowManager = new FlowManager(bootResult.entityContextFactory());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        flowExecutionService = new FlowExecutionService(bootResult.entityContextFactory());
        typeManager.setFlowExecutionService(flowExecutionService);
        var blockManager = new BlockManager(bootResult.blockMapper());
        typeClient = new MockTypeClient(typeManager, blockManager, instanceManager, executor, new MockTransactionOperations());
        FlowSavingContext.initConfig();
        typeManager.setVersionManager(new VersionManager(bootResult.entityContextFactory()));
    }

    @Override
    protected void tearDown() throws Exception {
        typeClient = null;
        executor.close();
    }

    public void test() throws ExecutionException, InterruptedException {
        compile(SOURCE_ROOT);
        var ref = new Object() {
            long productTypeId;
        };
        submit(() -> {
            var productType = queryClassType("商品");
            ref.productTypeId = productType.id();
            Assert.assertNotNull(NncUtils.find(productType.getClassParam().flows(), f -> "setSkus".equals(f.code())));
            var skuType = queryClassType("SKU");
            var skuListType = typeManager.getParameterizedType(
                    new GetParameterizedTypeRequest(
                            StandardTypes.getChildListType().getRef(),
                            List.of(skuType.getRef()),
                            List.of()
                    )
            ).type().id();
            var product = InstanceDTO.createClassInstance(
                    productType.getRef(),
                    List.of(
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "title"),
                                    PrimitiveFieldValue.createString("鞋子")
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "skus"),
                                    InstanceFieldValue.of(
                                            InstanceDTO.createListInstance(
                                                    RefDTO.fromId(skuListType),
                                                    true,
                                                    List.of(
                                                            InstanceFieldValue.of(
                                                                    InstanceDTO.createClassInstance(
                                                                            skuType.getRef(),
                                                                            List.of(
                                                                                    InstanceFieldDTO.create(
                                                                                            getFieldIdByCode(skuType, "title"),
                                                                                            PrimitiveFieldValue.createString("40")
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            getFieldIdByCode(skuType, "amount"),
                                                                                            PrimitiveFieldValue.createLong(100)
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            getFieldIdByCode(skuType, "price"),
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
                                                                                            getFieldIdByCode(skuType, "title"),
                                                                                            PrimitiveFieldValue.createString("41")
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            getFieldIdByCode(skuType, "amount"),
                                                                                            PrimitiveFieldValue.createLong(100)
                                                                                    ),
                                                                                    InstanceFieldDTO.create(
                                                                                            getFieldIdByCode(skuType, "price"),
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
                    getFieldIdByCode(productViewType, "price"));
            Assert.assertEquals(95.0, (double) priceFieldValue.getValue(), 0.0);
        });
        CompilerConfig.setMethodBlacklist(Set.of("tech.metavm.lab.Product.setSkus"));
        compile(SOURCE_ROOT);
        submit(() -> {
            var productType = typeManager.getType(new GetTypeRequest(ref.productTypeId, false)).type();
            Assert.assertNull(NncUtils.find(productType.getClassParam().flows(), f -> "setSkus".equals(f.code())));
        });
    }

    private void submit(Runnable task) {
        try {
            executor.submit(() -> {
                ContextUtil.resetProfiler();
                task.run();
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T submit(Callable<T> task) {
        try {
            return executor.submit(() -> {
                ContextUtil.resetProfiler();
                return task.call();
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }



    public void testShopping() {
        compileTwice(SHOPPING_SOURCE_ROOT);
        submit(() -> {
            var productStateType = queryClassType("商品状态");
            var productNormalStateId = TestUtils.getEnumConstantIdByName(productStateType, "正常");
            var productType = queryClassType("AST产品");
            var product = TestUtils.createInstanceWithCheck(instanceManager, InstanceDTO.createClassInstance(
                    productType.getRef(),
                    List.of(
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "title"),
                                    PrimitiveFieldValue.createString("鞋子")
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "orderCount"),
                                    PrimitiveFieldValue.createLong(0L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "price"),
                                    PrimitiveFieldValue.createLong(100L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "inventory"),
                                    PrimitiveFieldValue.createLong(100L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "state"),
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
                                    getFieldIdByCode(directCouponType, "discount"),
                                    PrimitiveFieldValue.createLong(5L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(directCouponType, "state"),
                                    ReferenceFieldValue.create(couponNormalStateId)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(directCouponType, "product"),
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
            var price = (long) ((PrimitiveFieldValue) order.getFieldValue(getFieldIdByCode(orderType, "price"))).getValue();
            var orderCoupons = ((InstanceFieldValue) order.getFieldValue(getFieldIdByCode(orderType, "coupons"))).getInstance();
            Assert.assertEquals(1, orderCoupons.getListSize());
            Assert.assertEquals(95, price);
        });
    }

    public void testLab() {
        compile(USERS_SOURCE_ROOT);
        submit(() -> {
            // create an UserLab instance
            var userLabType = queryClassType("UserLab");
            var userLabId = TestUtils.doInTransaction(() -> instanceManager.create(InstanceDTO.createClassInstance(
                    userLabType.getRef(),
                    List.of(
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(userLabType, "label"),
                                    PrimitiveFieldValue.createString("实验室")
                            )
                    )
            )));

            // call UserLab.createRole
            var createRoleMethodId = TestUtils.getMethodIdByCode(userLabType, "createRole");
            var role = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            createRoleMethodId,
                            userLabId,
                            List.of(PrimitiveFieldValue.createString("admin"))
                    )
            ));
            var roleType = queryClassType("LabRole");
            var roleNameFieldId = getFieldIdByCode(roleType, "name");
            Assert.assertEquals("admin", ((PrimitiveFieldValue) role.getFieldValue(roleNameFieldId)).getValue());
            // call UserLab.createPlatformUser
            var roleReadWriteListType = typeManager.getParameterizedType(
                    new GetParameterizedTypeRequest(
                            StandardTypes.getReadWriteListType().getRef(),
                            List.of(roleType.getRef()),
                            List.of()
                    )
            ).type();
            var createPlatformUserMethodId = TestUtils.getMethodIdByCode(userLabType, "createPlatformUser");
            var platformUser = TestUtils.doInTransaction(() -> {
//                ContextUtil.resetProfiler();
                InstanceDTO result;
//                try(var ignored = ContextUtil.getProfiler().enter("createPlatformUser")) {
                    result =  flowExecutionService.execute(
                            new FlowExecutionRequest(
                                    createPlatformUserMethodId,
                                    userLabId,
                                    List.of(
                                            PrimitiveFieldValue.createString("lyq"),
                                            PrimitiveFieldValue.createString("123456"),
                                            PrimitiveFieldValue.createString("lyq"),
                                            new InstanceFieldValue(
                                                    null,
                                                    InstanceDTO.createListInstance(
                                                            roleReadWriteListType.getRef(),
                                                            false,
                                                            List.of(ReferenceFieldValue.create(role.id()))
                                                    )
                                            )
                                    )
                            )
                    );
//                }
//                LOGGER.info(ContextUtil.getProfiler().finish(false, true).output());
                return result;
            });
            var userType = queryClassType("LabUser", List.of(TypeCategory.CLASS.code()));
//            var platformUserType = queryClassType("LabPlatformUser");
            var userLoginNameFieldId = getFieldIdByCode(userType, "loginName");
            var userNameFieldId = getFieldIdByCode(userType, "name");
            Assert.assertEquals("lyq", ((PrimitiveFieldValue) platformUser.getFieldValue(userLoginNameFieldId)).getValue());
            Assert.assertEquals("lyq", ((PrimitiveFieldValue) platformUser.getFieldValue(userNameFieldId)).getValue());
        });
    }

    public void testMetavm() {
        compile(METAVM_SOURCE_ROOT);
        var ref = new Object() {
            long getCodeMethodId;
            int numNodes;
        };
        submit(() -> {
            var typeType = queryClassType("类型", List.of(TypeCategory.CLASS.code()));
            Assert.assertTrue(typeType.getClassParam().errors().isEmpty());
            ref.getCodeMethodId = TestUtils.getMethodIdByCode(typeType, "getCode");
            var getCodeMethod = flowManager.get(new GetFlowRequest(ref.getCodeMethodId, true)).flow();
            ref.numNodes = Objects.requireNonNull(getCodeMethod.rootScope()).nodes().size();

            var typeCategoryType = queryClassType("类型分类", List.of(TypeCategory.ENUM.code()));
            var firstEnumConstant = typeCategoryType.getClassParam().enumConstants().get(0);
            Assert.assertEquals("类", firstEnumConstant.title());
        });

        // test recompile
        compile(METAVM_SOURCE_ROOT);

        // assert that the number of nodes doesn't change after recompilation
        submit(() -> {
            var getCodeMethod = flowManager.get(new GetFlowRequest(ref.getCodeMethodId, true)).flow();
            int numNodes = Objects.requireNonNull(getCodeMethod.rootScope()).nodes().size();
            Assert.assertEquals(ref.numNodes, numNodes);
        });
    }

    public void testUsers() {
        compileTwice(USERS_SOURCE_ROOT);
//        compile(USERS_SOURCE_ROOT);
        submit(() -> {
            var roleType = queryClassType("LabRole");
            var roleReadWriteListType = typeManager.getParameterizedType(
                    new GetParameterizedTypeRequest(
                            StandardTypes.getReadWriteListType().getRef(),
                            List.of(roleType.getRef()),
                            List.of()
                    )
            ).type();
            var roleNameFieldId = getFieldIdByCode(roleType, "name");
            var roleConstructorId = TestUtils.getMethodId(roleType, "LabRole", StandardTypes.getStringType().getId());
            var role = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            roleConstructorId,
                            null,
                            List.of(PrimitiveFieldValue.createString("admin"))
                    )
            ));
            Assert.assertEquals(
                    "admin",
                    ((PrimitiveFieldValue) role.getFieldValue(roleNameFieldId)).getValue()
            );
            var userType = queryClassType("LabUser", List.of(TypeCategory.CLASS.code()));
            assertNoError(userType);
            var userLoginNameFieldId = getFieldIdByCode(userType, "loginName");
            var userNameFieldId = getFieldIdByCode(userType, "name");
            var userPasswordFieldId = getFieldIdByCode(userType, "password");
            var userRolesFieldId = getFieldIdByCode(userType, "roles");
            var userConstructorId = TestUtils.getMethodIdByCode(userType, "LabUser");

            var platformUserType = queryClassType("LabPlatformUser", List.of(TypeCategory.CLASS.code()));
            assertNoError(platformUserType);
            var platformUserConstructorId = TestUtils.getMethodIdByCode(platformUserType, "LabPlatformUser");
            var platformUser = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            platformUserConstructorId,
                            null,
                            List.of(
                                    PrimitiveFieldValue.createString("lyq"),
                                    PrimitiveFieldValue.createString("123456"),
                                    PrimitiveFieldValue.createString("lyq"),
                                    new InstanceFieldValue(
                                            null,
                                            InstanceDTO.createListInstance(
                                                    roleReadWriteListType.getRef(),
                                                    false,
                                                    List.of(ReferenceFieldValue.create(role.id())
                                                    )
                                            )
                                    )
                            )
                    )
            ));
            Assert.assertEquals(
                    "lyq", ((PrimitiveFieldValue) platformUser.getFieldValue(userLoginNameFieldId)).getValue()
            );
            Assert.assertEquals(
                    "lyq", ((PrimitiveFieldValue) platformUser.getFieldValue(userNameFieldId)).getValue()
            );
            var platformUserRoles = ((InstanceFieldValue) platformUser.getFieldValue(userRolesFieldId)).getInstance();
            Assert.assertEquals(1, platformUserRoles.getListSize());
            Assert.assertEquals(role.id(), platformUserRoles.getElement(0).referenceId());
            var platformUserApplicationsFieldId = getFieldIdByCode(platformUserType, "applications");
            var platformUserApplications = ((InstanceFieldValue) platformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
            Assert.assertEquals(0, platformUserApplications.getListSize());

            // test platform user view list
            var platformUserMapping = TestUtils.getDefaultMapping(platformUserType);
            var platformUserViewType = typeManager.getType(new GetTypeRequest(platformUserMapping.targetTypeRef().id(), false)).type();
            var platformUserViewList = instanceManager.query(
                    new InstanceQueryDTO(
                            platformUserViewType.id(),
                            platformUserMapping.id(),
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
            Assert.assertEquals(1, platformUserViewList.size());
            // test platform user view update
            var platformUserView = platformUserViewList.get(0);
            TestUtils.doInTransactionWithoutResult(() -> instanceManager.update(platformUserView));
            // reload platform user view and check its roles field
            var reloadedPlatformUserView = instanceManager.get(platformUserView.id(), 1).instance();
            var userViewRolesFieldId = TestUtils.getFieldIdByCode(platformUserViewType, "roles");
            var reloadedPlatformUserRoles = ((InstanceFieldValue) reloadedPlatformUserView.getFieldValue(userViewRolesFieldId)).getInstance();
            Assert.assertEquals(1, reloadedPlatformUserRoles.getListSize());

            // test join application
            var userApplicationType = queryClassType("UserApplication");
            var applicationConstructorId = TestUtils.getMethodIdByCode(userApplicationType, "UserApplication");
            var application = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            applicationConstructorId,
                            null,
                            List.of(
                                    PrimitiveFieldValue.createString("lab"),
                                    ReferenceFieldValue.create(platformUser.id())
                            )
                    )
            ));
            var applicationType = queryClassType("LabApplication", List.of(TypeCategory.CLASS.code()));
            var joinApplicationMethodId = TestUtils.getStaticMethod(platformUserType, "joinApplication",
                    platformUserType.getRef(), applicationType.getRef());
            TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            joinApplicationMethodId,
                            null,
                            List.of(
                                    ReferenceFieldValue.create(platformUser.id()),
                                    ReferenceFieldValue.create(application.id())
                            )
                    )
            ));
            var reloadedPlatformUser = instanceManager.get(platformUser.id(), 1).instance();
            var joinedApplications = ((InstanceFieldValue) reloadedPlatformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
            Assert.assertEquals(1, joinedApplications.getListSize());
            Assert.assertEquals(application.id(), joinedApplications.getElement(0).referenceId());

            // enter application
            var enterApplicationMethodId = TestUtils.getStaticMethod(platformUserType, "enterApp",
                    platformUserType.getRef(), userApplicationType.getRef());
            var loginResult = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            enterApplicationMethodId,
                            null,
                            List.of(
                                    ReferenceFieldValue.create(platformUser.id()),
                                    ReferenceFieldValue.create(application.id())
                            )
                    )
            ));
            var loginResultType = queryClassType("LabLoginResult");
            var token = (String) ((PrimitiveFieldValue) loginResult.getFieldValue(getFieldIdByCode(loginResultType, "token"))).getValue();
            Assert.assertNotNull(token);

            // test leave application
            var platformUserListType = typeManager.getParameterizedType(
                    new GetParameterizedTypeRequest(
                            StandardTypes.getListType().getRef(),
                            List.of(platformUserType.getRef()),
                            List.of()
                    )
            ).type();
            var platformUserReadWriteListType = typeManager.getParameterizedType(
                    new GetParameterizedTypeRequest(
                            StandardTypes.getReadWriteListType().getRef(),
                            List.of(platformUserType.getRef()),
                            List.of()
                    )
            ).type();
            var leaveApplicationMethodId = TestUtils.getStaticMethod(platformUserType, "leaveApp",
                    platformUserListType.getRef(), userApplicationType.getRef());
            try {
                TestUtils.doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                leaveApplicationMethodId,
                                null,
                                List.of(
                                        new InstanceFieldValue(
                                                null,
                                                InstanceDTO.createListInstance(
                                                        platformUserReadWriteListType.getRef(),
                                                        false,
                                                        List.of(ReferenceFieldValue.create(platformUser.id()))
                                                )
                                        ),
                                        ReferenceFieldValue.create(application.id()))
                        )
                ));
                Assert.fail("应用所有人无法退出应用");
            } catch (FlowExecutionException e) {
                Assert.assertEquals("应用所有人无法退出应用", e.getMessage());
            }

            // create a platform user to join the application and then leave
            var anotherPlatformUser = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            platformUserConstructorId,
                            null,
                            List.of(
                                    PrimitiveFieldValue.createString("lyq2"),
                                    PrimitiveFieldValue.createString("123456"),
                                    PrimitiveFieldValue.createString("lyq2"),
                                    new InstanceFieldValue(
                                            null,
                                            InstanceDTO.createListInstance(
                                                    roleReadWriteListType.getRef(),
                                                    false,
                                                    List.of(ReferenceFieldValue.create(role.id()))
                                            )
                                    )
                            )
                    )
            ));

            TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            joinApplicationMethodId,
                            null,
                            List.of(
                                    ReferenceFieldValue.create(anotherPlatformUser.id()),
                                    ReferenceFieldValue.create(application.id())
                            )
                    )
            ));
            // assert that the user has joined the application
            var reloadedAnotherPlatformUser = instanceManager.get(anotherPlatformUser.id(), 1).instance();
            var anotherJoinedApplications = ((InstanceFieldValue) reloadedAnotherPlatformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
            Assert.assertEquals(1, anotherJoinedApplications.getListSize());
            loginResult = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            enterApplicationMethodId,
                            null,
                            List.of(
                                    ReferenceFieldValue.create(anotherPlatformUser.id()),
                                    ReferenceFieldValue.create(application.id())
                            )
                    )
            ));
            loginResultType = queryClassType("LabLoginResult");
            token = (String) ((PrimitiveFieldValue) loginResult.getFieldValue(getFieldIdByCode(loginResultType, "token"))).getValue();
            Assert.assertNotNull(token);

            // test leaving the application
            TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            leaveApplicationMethodId,
                            null,
                            List.of(
                                    InstanceFieldValue.of(
                                            InstanceDTO.createListInstance(
                                                    platformUserReadWriteListType.getRef(),
                                                    false,
                                                    List.of(ReferenceFieldValue.create(anotherPlatformUser.id()))
                                            )
                                    ),
                                    ReferenceFieldValue.create(application.id()))
                    )
            ));

            // assert that the user has left the application
            var reloadedAnotherPlatformUser2 = instanceManager.get(anotherPlatformUser.id(), 1).instance();
            var anotherJoinedApplications2 = ((InstanceFieldValue) reloadedAnotherPlatformUser2.getFieldValue(platformUserApplicationsFieldId)).getInstance();
            Assert.assertEquals(0, anotherJoinedApplications2.getListSize());
            try {
                TestUtils.doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                enterApplicationMethodId,
                                null,
                                List.of(
                                        ReferenceFieldValue.create(anotherPlatformUser.id()),
                                        ReferenceFieldValue.create(application.id())
                                )
                        )
                ));
                Assert.fail("用户未加入应用无法进入");
            } catch (FlowExecutionException e) {
                Assert.assertEquals("用户未加入应用无法进入", e.getMessage());
            }

            // test application view list
            var applicationMapping = TestUtils.getDefaultMapping(userApplicationType);
            var applicationViewType = typeManager.getType(new GetTypeRequest(applicationMapping.targetTypeRef().id(), false)).type();
            var applicationViewList = instanceManager.query(
                    new InstanceQueryDTO(
                            applicationViewType.id(),
                            applicationMapping.id(),
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
            Assert.assertEquals(1, applicationViewList.size());

            // test update application view
            var applicationView = applicationViewList.get(0);
            TestUtils.doInTransactionWithoutResult(() -> instanceManager.update(applicationView));

            // assert that fields of LabToken type has been generated correctly
            var tokenType = queryClassType("LabToken");
            var tokenReadWriteListType = typeManager.getParameterizedType(
                    new GetParameterizedTypeRequest(
                            StandardTypes.getReadWriteListType().getRef(),
                            List.of(tokenType.getRef()),
                            List.of()
                    )
            ).type();
            Assert.assertTrue(tokenType.ephemeral());
            Assert.assertEquals(2, tokenType.getClassParam().fields().size());

            // create an ordinary user
            var user = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            userConstructorId,
                            null,
                            List.of(
                                    PrimitiveFieldValue.createString("leen"),
                                    PrimitiveFieldValue.createString("123456"),
                                    PrimitiveFieldValue.createString("leen"),
                                    InstanceFieldValue.of(
                                            InstanceDTO.createListInstance(
                                                    roleReadWriteListType.getRef(),
                                                    false,
                                                    List.of(ReferenceFieldValue.create(role.id()))
                                            )
                                    ),
                                    ReferenceFieldValue.create(application.id())
                            )
                    )
            ));

            Assert.assertEquals(
                    "leen", ((PrimitiveFieldValue) user.getFieldValue(userLoginNameFieldId)).getValue()
            );
            Assert.assertEquals(
                    "leen", ((PrimitiveFieldValue) user.getFieldValue(userNameFieldId)).getValue()
            );
            var passwordValue = user.getFieldValue(userPasswordFieldId);
            Assert.assertTrue(passwordValue instanceof PrimitiveFieldValue primitiveFieldValue
                    && primitiveFieldValue.getPrimitiveKind() == PrimitiveKind.PASSWORD.code());
            var userRoles = ((InstanceFieldValue) user.getFieldValue(userRolesFieldId)).getInstance();
            Assert.assertEquals(1, userRoles.getListSize());
            Assert.assertEquals(role.id(), userRoles.getElement(0).referenceId());
            Assert.assertEquals(2, userType.getClassParam().constraints().size());

            // test login
            var loginMethodId = TestUtils.getMethodIdByCode(userType, "login");
            loginResult = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            loginMethodId,
                            null,
                            List.of(
                                    ReferenceFieldValue.create(application.id()),
                                    PrimitiveFieldValue.createString("leen"),
                                    PrimitiveFieldValue.createString("123456"),
                                    PrimitiveFieldValue.createString("127.0.0.1")
                            )
                    )
            ));
            // get LoginResult type
            loginResultType = queryClassType("LabLoginResult");
            token = (String) ((PrimitiveFieldValue) loginResult.getFieldValue(getFieldIdByCode(loginResultType, "token"))).getValue();
            Assert.assertNotNull(token);

            // test login with too many attempts
            var loginRequest = new FlowExecutionRequest(
                    loginMethodId,
                    null,
                    List.of(
                            ReferenceFieldValue.create(application.id()),
                            PrimitiveFieldValue.createString("leen"),
                            PrimitiveFieldValue.createString("123123"),
                            PrimitiveFieldValue.createString("127.0.0.1")
                    )
            );
            for (int i = 0; i < 5; i++) {
                try {
                    TestUtils.doInTransaction(() -> flowExecutionService.execute(loginRequest));
                    if (i == 4) {
                        Assert.fail("登录尝试次数过多，应该抛出异常");
                    }
                } catch (FlowExecutionException e) {
                    Assert.assertEquals("登录尝试次数过多，请稍后再试", e.getMessage());
                }
            }

            // execute the LabUser.verify method and check verification result
            var verifyMethodId = TestUtils.getMethodIdByCode(userType, "verify");
            var tokenValue = InstanceFieldValue.of(InstanceDTO.createClassInstance(
                    tokenType.getRef(),
                    List.of(
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(tokenType, "application"),
                                    ReferenceFieldValue.create(application.id())
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(tokenType, "token"),
                                    PrimitiveFieldValue.createString(token)
                            )
                    )
            ));
            var loginInfo = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            verifyMethodId,
                            null,
                            List.of(tokenValue)
                    )
            ));
            var loginInfoType = queryClassType("LabLoginInfo");
            assertNoError(loginInfoType);
            var applicationFieldId = getFieldIdByCode(loginInfoType, "application");
            var userFieldId = getFieldIdByCode(loginInfoType, "user");
            Assert.assertEquals(application.id(), ((ReferenceFieldValue) loginInfo.getFieldValue(applicationFieldId)).getId());
            Assert.assertEquals(user.id(), ((ReferenceFieldValue) loginInfo.getFieldValue(userFieldId)).getId());

            // test logout
            var logoutMethodId = TestUtils.getMethodIdByCode(userType, "logout");
            TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            logoutMethodId,
                            null,
                            List.of(
                                    InstanceFieldValue.of(
                                            InstanceDTO.createListInstance(
                                                    tokenReadWriteListType.getRef(),
                                                    false,
                                                    List.of(tokenValue)
                                            )
                                    )
                            )
                    )
            ));

            // verify that the token has been invalidated
            loginInfo = TestUtils.doInTransaction(() -> flowExecutionService.execute(
                    new FlowExecutionRequest(
                            verifyMethodId,
                            null,
                            List.of(tokenValue)
                    )
            ));
            Assert.assertNull(((PrimitiveFieldValue) loginInfo.getFieldValue(applicationFieldId)).getValue());
        });
    }

    private void compileTwice(String sourceRoot) {
        compile(sourceRoot);
        compile(sourceRoot);
    }

    private void compile(String sourceRoot) {
        ContextUtil.resetProfiler();
        new Main(HOME, sourceRoot, AUTH_FILE, typeClient, allocatorStore).run();
    }

    private TypeDTO queryClassType(String name) {
        return queryClassType(name, List.of(TypeCategory.CLASS.code(), TypeCategory.ENUM.code(), TypeCategory.INTERFACE.code()));
    }

    private void assertNoError(TypeDTO typeDTO) {
        Assert.assertEquals(0, typeDTO.getClassParam().errors().size());
    }

    private TypeDTO queryClassType(String name, List<Integer> categories) {
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

//    public void testResend() {
//        LoginUtils.loginWithAuthFile(AUTH_FILE, typeClient);
//        var request = NncUtils.readJsonFromFile(REQUEST_FILE, BatchSaveRequest.class);
//        HttpUtils.post("/type/batch-save", request, new TypeReference<List<Long>>() {
//        });
//    }

}