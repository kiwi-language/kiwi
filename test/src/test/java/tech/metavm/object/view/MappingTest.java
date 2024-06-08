package tech.metavm.object.view;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.core.DefaultViewId;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.TypeExpressions;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.ClassTypeKey;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.view.rest.dto.ObjectMappingRefDTO;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MappingTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(MappingTest.class);

    private TypeManager typeManager;
    private FlowManager flowManager;
    private InstanceManager instanceManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        TransactionOperations transactionOperations = new MockTransactionOperations();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        EntityQueryService entityQueryService = new EntityQueryService(instanceQueryService);
        typeManager = new TypeManager(
                bootResult.entityContextFactory(), entityQueryService,
                new TaskManager(bootResult.entityContextFactory(), transactionOperations)
        );
        instanceManager = new InstanceManager(
                bootResult.entityContextFactory(), bootResult.instanceStore(), instanceQueryService
        );
        typeManager.setInstanceManager(instanceManager);
        flowManager = new FlowManager(bootResult.entityContextFactory(), new MockTransactionOperations());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        var flowExecutionService = new FlowExecutionService(bootResult.entityContextFactory());
        typeManager.setFlowExecutionService(flowExecutionService);
        FlowSavingContext.initConfig();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        FlowSavingContext.clearConfig();
        typeManager = null;
        flowManager = null;
        instanceManager = null;
    }

    private TypeDTO getType(String id) {
        return typeManager.getType(new GetTypeRequest(id, false)).type();
    }

    private TypeDTO getTypeFromExpr(String expr) {
        var typeKey = (ClassTypeKey) TypeKey.fromExpression(expr);
        return getType(typeKey.id().toString());
    }

    public void test() {
//        DebugEnv.printMapping = true;
        var typeIds = MockUtils.createShoppingTypes(typeManager);
        var productTypeDTO = getType(typeIds.productTypeId());
        var skuTypeDTO = getType(typeIds.skuTypeId());
        var productDefaultMapping = NncUtils.findRequired(productTypeDTO.getClassParam().mappings(),
                m -> Objects.equals(m.id(), productTypeDTO.getClassParam().defaultMappingId()));
        var productViewTypeDTO = getTypeFromExpr(productDefaultMapping.targetType());
        var productViewTitleFieldId = TestUtils.getFieldIdByCode(productViewTypeDTO, "name");
        var productViewSkuListFieldId = TestUtils.getFieldIdByCode(productViewTypeDTO, "skuList");
        var skuDefaultMapping = NncUtils.findRequired(
                skuTypeDTO.getClassParam().mappings(),
                m -> Objects.equals(skuTypeDTO.getClassParam().defaultMappingId(), m.id()));
        var skuViewTypeDTO = getTypeFromExpr(skuDefaultMapping.targetType());
        var skuViewTitleFieldId = TestUtils.getFieldIdByCode(skuViewTypeDTO, "name");
        var skuViewPriceFieldId = TestUtils.getFieldIdByCode(skuViewTypeDTO, "price");
        var skuViewAmountFieldId = TestUtils.getFieldIdByCode(skuViewTypeDTO, "quantity");
        var skuViewChildListType = TypeExpressions.getChildListType(TypeExpressions.getClassType(skuViewTypeDTO));

        var productId = saveInstance(InstanceDTO.createClassInstance(
                TypeExpressions.getClassType(typeIds.productTypeId()),
                List.of(
                        InstanceFieldDTO.create(
                                typeIds.productTitleFieldId(),
                                PrimitiveFieldValue.createString("Shoes")
                        ),
                        InstanceFieldDTO.create(
                                typeIds.productSkuListFieldId(),
                                InstanceFieldValue.of(
                                        InstanceDTO.createListInstance(
                                                typeIds.skuChildListType(),
                                                true,
                                                List.of(
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TypeExpressions.getClassType(typeIds.skuTypeId()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuTitleFieldId(),
                                                                                        PrimitiveFieldValue.createString("40")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuPriceFieldId(),
                                                                                        PrimitiveFieldValue.createDouble(100.0)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuAmountFieldId(),
                                                                                        PrimitiveFieldValue.createLong(100)
                                                                                )
                                                                        )
                                                                )
                                                        ),
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TypeExpressions.getClassType(typeIds.skuTypeId()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuTitleFieldId(),
                                                                                        PrimitiveFieldValue.createString("41")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuPriceFieldId(),
                                                                                        PrimitiveFieldValue.createDouble(100.0)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuAmountFieldId(),
                                                                                        PrimitiveFieldValue.createLong(80)
                                                                                )
                                                                        )
                                                                )
                                                        ),
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TypeExpressions.getClassType(typeIds.skuTypeId()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuTitleFieldId(),
                                                                                        PrimitiveFieldValue.createString("42")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuPriceFieldId(),
                                                                                        PrimitiveFieldValue.createDouble(100.0)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        typeIds.skuAmountFieldId(),
                                                                                        PrimitiveFieldValue.createLong(90)
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));

        var productViews = instanceManager.query(new InstanceQueryDTO(
                TypeExpressions.getClassType(productViewTypeDTO.id()),
                productDefaultMapping.id(),
                null,
                null,
                List.of(),
                1,
                20,
                true,
                false,
                List.of()
        )).page().data();

        Assert.assertEquals(1, productViews.size());
        var productView = productViews.get(0);
        var viewId = (DefaultViewId) Id.parse(productView.id());
        Assert.assertEquals(Id.parse(productId), viewId.getSourceId());

        var skuListView = ((InstanceFieldValue) (productView.getFieldValue(productViewSkuListFieldId))).getInstance();
        productView = InstanceDTO.createClassInstance(
                viewId.toString(),
                TypeExpressions.getClassType(productViewTypeDTO.id()),
                List.of(
                        InstanceFieldDTO.create(
                                productViewTitleFieldId,
                                PrimitiveFieldValue.createString("Shoes")
                        ),
                        InstanceFieldDTO.create(
                                productViewSkuListFieldId,
                                InstanceFieldValue.of(
                                        InstanceDTO.createListInstance(
                                                skuListView.id(),
                                                skuViewChildListType,
                                                true,
                                                List.of(
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TestUtils.getId(skuListView.getElement(0)),
                                                                        TypeExpressions.getClassType(skuViewTypeDTO.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewTitleFieldId,
                                                                                        PrimitiveFieldValue.createString("40")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewPriceFieldId,
                                                                                        PrimitiveFieldValue.createDouble(100.0)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewAmountFieldId,
                                                                                        PrimitiveFieldValue.createLong(99)
                                                                                )
                                                                        )
                                                                )
                                                        ),
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TestUtils.getId(skuListView.getElement(1)),
                                                                        TypeExpressions.getClassType(skuViewTypeDTO.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewTitleFieldId,
                                                                                        PrimitiveFieldValue.createString("41")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewPriceFieldId,
                                                                                        PrimitiveFieldValue.createDouble(101.0)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewAmountFieldId,
                                                                                        PrimitiveFieldValue.createLong(89)
                                                                                )
                                                                        )
                                                                )
                                                        ),
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        null,
                                                                        TypeExpressions.getClassType(skuViewTypeDTO.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewTitleFieldId,
                                                                                        PrimitiveFieldValue.createString("42")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewPriceFieldId,
                                                                                        PrimitiveFieldValue.createDouble(100.0)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        skuViewAmountFieldId,
                                                                                        PrimitiveFieldValue.createLong(100)
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
//        DebugEnv.DEBUG_ON = true;
        saveInstance(productView);
        var loadedProductView = instanceManager.get(viewId.toString(), 1).instance();
        skuListView = ((InstanceFieldValue) (loadedProductView.getFieldValue(productViewSkuListFieldId))).getInstance();
        Assert.assertEquals(3, skuListView.getListSize());
        var newSkuId = Objects.requireNonNull(skuListView.getElement(2).underlyingInstance().id());
        MatcherAssert.assertThat(loadedProductView, new InstanceDTOMatcher(productView, Set.of(newSkuId)));

        var sku = skuListView.getElement(1);
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.delete(TestUtils.getId(sku)));
        // assert that the sku is actually removed
        loadedProductView = instanceManager.get(viewId.toString(), 1).instance();
        skuListView = ((InstanceFieldValue) (loadedProductView.getFieldValue(productViewSkuListFieldId))).getInstance();
        Assert.assertEquals(2, skuListView.getListSize());

        // test removing product view
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.delete(viewId.toString()));
    }

    public void testOrderQuery() {
        var shoppingTypeIds = MockUtils.createShoppingTypes(typeManager);
        var skuId = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, TypeExpressions.getClassType(shoppingTypeIds.skuTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(shoppingTypeIds.skuTitleFieldId(), PrimitiveFieldValue.createString("Shoes 40")),
                                InstanceFieldDTO.create(shoppingTypeIds.skuAmountFieldId(), PrimitiveFieldValue.createLong(100L)),
                                InstanceFieldDTO.create(shoppingTypeIds.skuPriceFieldId(), PrimitiveFieldValue.createDouble(100.0))
                        )
                )
        )));
        var coupon1Id = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, TypeExpressions.getClassType(shoppingTypeIds.couponTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(shoppingTypeIds.couponTitleFieldId(), PrimitiveFieldValue.createString("Shoes reduced by 5 Yuan")),
                                InstanceFieldDTO.create(shoppingTypeIds.couponDiscountFieldId(), PrimitiveFieldValue.createDouble(5.0)),
                                InstanceFieldDTO.create(shoppingTypeIds.couponStateFieldId(),
                                        ReferenceFieldValue.create(shoppingTypeIds.couponNormalStateId()))
                        )
                )
        )));
        var coupon2Id = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, TypeExpressions.getClassType(shoppingTypeIds.couponTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(shoppingTypeIds.couponTitleFieldId(), PrimitiveFieldValue.createString("Shoes reduced by 10 Yuan")),
                                InstanceFieldDTO.create(shoppingTypeIds.couponDiscountFieldId(), PrimitiveFieldValue.createDouble(10.0)),
                                InstanceFieldDTO.create(shoppingTypeIds.couponStateFieldId(),
                                        ReferenceFieldValue.create(shoppingTypeIds.couponNormalStateId())
                                )
                        )
                ))));
        TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, TypeExpressions.getClassType(shoppingTypeIds.orderTypeId()),
                null,
                null,
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.orderCodeFieldId(), PrimitiveFieldValue.createString("Shoes001")
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.orderPriceFieldId(), PrimitiveFieldValue.createDouble(85.0)
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.orderAmountFieldId(), PrimitiveFieldValue.createLong(1L)
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.orderSkuFieldId(), ReferenceFieldValue.create(skuId)
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.orderCouponsFieldId(), new ListFieldValue(
                                                null, false,
                                                List.of(
                                                        ReferenceFieldValue.create(coupon1Id),
                                                        ReferenceFieldValue.create(coupon2Id)
                                                )
                                        )
                                ),
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.orderTimeFieldId(), PrimitiveFieldValue.createTime(System.currentTimeMillis())
                                )
                        )
                )
        )));
        var productTypeDTO = typeManager.getType(new GetTypeRequest(shoppingTypeIds.orderTypeId(), false)).type();
        var mapping = TestUtils.getDefaultMapping(productTypeDTO);
        var productViewType = TestUtils.getDefaultViewType(productTypeDTO);
        var total = instanceManager.query(new InstanceQueryDTO(
                productViewType, mapping.id(), null, null, List.of(), 1, 20,
                true, false, List.of()
        )).page().total();
        Assert.assertEquals(1, total);
    }

    public void testNewRootView() {
        var shoppingTypeIds = MockUtils.createShoppingTypes(typeManager);
        var skuTypeDTO = getType(shoppingTypeIds.skuTypeId());
        var skuDefaultMapping = NncUtils.findRequired(
                skuTypeDTO.getClassParam().mappings(),
                m -> Objects.equals(skuTypeDTO.getClassParam().defaultMappingId(), m.id()));
        var skuViewTypeDTO = getType(TypeExpressions.extractKlassId(skuDefaultMapping.targetType()));
        var skuViewId = saveInstance(
                new InstanceDTO(
                        null,
                        TypeExpressions.getClassType(skuViewTypeDTO.id()),
                        null,
                        null,
                        new ObjectMappingRefDTO(TypeExpressions.getClassType(skuTypeDTO.id()), skuDefaultMapping.id()),
                        new ClassInstanceParam(
                                List.of(
                                        InstanceFieldDTO.create(
                                                TestUtils.getFieldIdByCode(skuViewTypeDTO, "name"),
                                                PrimitiveFieldValue.createString("40")
                                        ),
                                        InstanceFieldDTO.create(
                                                TestUtils.getFieldIdByCode(skuViewTypeDTO, "price"),
                                                PrimitiveFieldValue.createDouble(100.0)
                                        ),
                                        InstanceFieldDTO.create(
                                                TestUtils.getFieldIdByCode(skuViewTypeDTO, "quantity"),
                                                PrimitiveFieldValue.createLong(100)
                                        )
                                )
                        )
                )
        );
        Assert.assertNotNull(skuViewId);
    }

public void testGeneric() {
        var listTypeIds = MockUtils.createListType(typeManager);
        var nodeTypeIds = listTypeIds.nodeTypeIds();
        var listKlass = typeManager.getType(new GetTypeRequest(listTypeIds.listTypeId(), false)).type();
        var nodeKlass = typeManager.getType(new GetTypeRequest(nodeTypeIds.nodeTypeId(), false)).type();
        var listOfStringType = TypeExpressions.getParameterizedType(listKlass.id(), "string");
        var listDefaultMapping = TestUtils.getDefaultMapping(listKlass);
        var listViewType = listDefaultMapping.targetType();
        var listViewOfStrType = TypeExpressions.substitute(listViewType, Map.of(listKlass.typeParameterIds().get(0), "string"));
        var listViewKlass = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(listViewType), false)).type();
        var nodeDefaultMapping = TestUtils.getDefaultMapping(nodeKlass);
        var nodeViewType = nodeDefaultMapping.targetType();
        var nodeViewKlass = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(nodeViewType), false)).type();
        var nodeViewOfStrType = TypeExpressions.substitute(nodeViewType, Map.of(nodeKlass.typeParameterIds().get(0), "string"));
        var nodeViewOfStrChildListType = TypeExpressions.getChildListType(nodeViewOfStrType);
        var listView = new InstanceDTO(
                null,
                listViewOfStrType,
                null,
                null,
                new ObjectMappingRefDTO(
                        listOfStringType,
                        listDefaultMapping.id()
                ),
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(listViewKlass, "label"),
                                        PrimitiveFieldValue.createString("list001")
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(listViewKlass, "nodes"),
                                        InstanceFieldValue.of(
                                                InstanceDTO.createListInstance(
                                                        nodeViewOfStrChildListType,
                                                        true,
                                                        List.of(
                                                                InstanceFieldValue.of(
                                                                        InstanceDTO.createClassInstance(
                                                                                nodeViewOfStrType,
                                                                                List.of(
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewKlass, "label"),
                                                                                                PrimitiveFieldValue.createString("node001")
                                                                                        ),
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewKlass, "value"),
                                                                                                PrimitiveFieldValue.createString("hello")
                                                                                        )
                                                                                )
                                                                        )
                                                                ),
                                                                InstanceFieldValue.of(
                                                                        InstanceDTO.createClassInstance(
                                                                                nodeViewOfStrType,
                                                                                List.of(
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewKlass, "label"),
                                                                                                PrimitiveFieldValue.createString("node002")
                                                                                        ),
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewKlass, "value"),
                                                                                                PrimitiveFieldValue.createString("world")
                                                                                        )
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
        var id = TestUtils.doInTransaction(() -> instanceManager.create(listView));
        var loadedView = instanceManager.get(id, 1).instance();
        MatcherAssert.assertThat(listView, new InstanceDTOMatcher(loadedView, TestUtils.extractDescendantIds(loadedView)));
    }

    public void testUser() {
        var userTypeIds = MockUtils.createUserTypes(typeManager);
        // save user instance
        var userId = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, TypeExpressions.getClassType(userTypeIds.platformUserTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(userTypeIds.platformUserLoginNameFieldId(), PrimitiveFieldValue.createString("user001")),
                                InstanceFieldDTO.create(userTypeIds.platformUserPasswordFieldId(), PrimitiveFieldValue.createPassword("123456"))
                        )
                )
        )));
        // save application instance
        var applicationId = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, TypeExpressions.getClassType(userTypeIds.applicationTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(userTypeIds.applicationNameFieldId(), PrimitiveFieldValue.createString("test")),
                                InstanceFieldDTO.create(userTypeIds.applicationOwnerFieldId(), ReferenceFieldValue.create(userId)
                                )
                        )
                ))));
        // query application view list
        var applicationViewTypeDTO = typeManager.getType(new GetTypeRequest(userTypeIds.applicationTypeId(), true)).type();
        var applicationDefaultMapping = TestUtils.getDefaultMapping(applicationViewTypeDTO);
        var applicationViewType = applicationDefaultMapping.targetType();
        var applicationViews = instanceManager.query(new InstanceQueryDTO(
                applicationViewType, applicationDefaultMapping.id(), null, null, List.of(), 1, 20,
                true, false, List.of()
        )).page().data();
        Assert.assertEquals(1, applicationViews.size());
        var applicationView = applicationViews.get(0);
        var viewId = (DefaultViewId) Id.parse(applicationView.id());
        Assert.assertEquals(Id.parse(applicationId), viewId.getSourceId());
    }

    private String saveInstance(InstanceDTO instanceDTO) {
        String id;
        if (instanceDTO.isNew())
            id = TestUtils.doInTransaction(() -> instanceManager.create(instanceDTO));
        else {
            id = instanceDTO.id();
            TestUtils.doInTransactionWithoutResult(() -> instanceManager.update(instanceDTO));
        }
        return id;
    }


}
