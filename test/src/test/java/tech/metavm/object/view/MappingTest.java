package tech.metavm.object.view;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.core.DefaultViewId;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.GetParameterizedTypeRequest;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MappingTest extends TestCase {

    private TypeManager typeManager;
    private FlowManager flowManager;
    private InstanceManager instanceManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var entityContextFactory = bootResult.entityContextFactory();
        TransactionOperations transactionOperations = new MockTransactionOperations();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        EntityQueryService entityQueryService = new EntityQueryService(instanceQueryService);
        typeManager = new TypeManager(
                bootResult.entityContextFactory(), entityQueryService,
                new TaskManager(bootResult.entityContextFactory(), transactionOperations),
                transactionOperations);
        instanceManager = new InstanceManager(
                bootResult.entityContextFactory(), bootResult.instanceStore(), instanceQueryService
        );
        typeManager.setInstanceManager(instanceManager);
        flowManager = new FlowManager(bootResult.entityContextFactory());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        FlowSavingContext.initConfig();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        FlowSavingContext.clearConfig();
    }

    private TypeDTO getType(long id) {
        return typeManager.getType(new GetTypeRequest(id, false)).type();
    }

    private long getArrayTypeId(long id, int kind) {
        return typeManager.getArrayType(id, kind).type().id();
    }

    public void test() {
        var typeIds = MockUtils.createShoppingTypes(typeManager, flowManager);
        var productTypeDTO = getType(typeIds.productTypeId());
        var skuTypeDTO = getType(typeIds.skuTypeId());
        var productDefaultMapping = NncUtils.findRequired(productTypeDTO.getClassParam().mappings(),
                m -> Objects.equals(m.getRef(), productTypeDTO.getClassParam().defaultMappingRef()));
        var productViewTypeDTO = getType(productDefaultMapping.targetTypeRef().id());
        var productViewTitleFieldId = TestUtils.getFieldIdByCode(productViewTypeDTO, "title");
        var productViewSkuListFieldId = TestUtils.getFieldIdByCode(productViewTypeDTO, "skuList");
        var skuDefaultMapping = NncUtils.findRequired(
                skuTypeDTO.getClassParam().mappings(),
                m -> Objects.equals(skuTypeDTO.getClassParam().defaultMappingRef(), m.getRef()));
        var skuViewTypeDTO = getType(skuDefaultMapping.targetTypeRef().id());
        var skuViewTitleFieldId = TestUtils.getFieldIdByCode(skuViewTypeDTO, "title");
        var skuViewPriceFieldId = TestUtils.getFieldIdByCode(skuViewTypeDTO, "price");
        var skuViewAmountFieldId = TestUtils.getFieldIdByCode(skuViewTypeDTO, "amount");
        var skuViewChildArrayTypeId = getArrayTypeId(skuViewTypeDTO.id(), ArrayKind.CHILD.code());

        var productId = saveInstance(InstanceDTO.createClassInstance(
                RefDTO.fromId(typeIds.productTypeId()),
                List.of(
                        InstanceFieldDTO.create(
                                typeIds.productTitleFieldId(),
                                PrimitiveFieldValue.createString("鞋子")
                        ),
                        InstanceFieldDTO.create(
                                typeIds.productSkuListFieldId(),
                                InstanceFieldValue.of(
                                        InstanceDTO.createArrayInstance(
                                                RefDTO.fromId(typeIds.skuChildArrayTypeId()),
                                                true,
                                                List.of(
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        RefDTO.fromId(typeIds.skuTypeId()),
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
                                                                        RefDTO.fromId(typeIds.skuTypeId()),
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
                                                                        RefDTO.fromId(typeIds.skuTypeId()),
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
                productViewTypeDTO.id(),
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
                RefDTO.fromId(productViewTypeDTO.id()),
                List.of(
                        InstanceFieldDTO.create(
                                productViewTitleFieldId,
                                PrimitiveFieldValue.createString("皮鞋")
                        ),
                        InstanceFieldDTO.create(
                                productViewSkuListFieldId,
                                InstanceFieldValue.of(
                                        InstanceDTO.createArrayInstance(
                                                skuListView.id(),
                                                RefDTO.fromId(skuViewChildArrayTypeId),
                                                true,
                                                List.of(
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TestUtils.getId(skuListView.getElement(0)),
                                                                        RefDTO.fromId(skuViewTypeDTO.id()),
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
                                                                        RefDTO.fromId(skuViewTypeDTO.id()),
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
                                                                        RefDTO.fromId(skuViewTypeDTO.id()),
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
        saveInstance(productView);
        var loadedProductView = instanceManager.get(viewId.toString(), 1).instance();
        skuListView = ((InstanceFieldValue) (loadedProductView.getFieldValue(productViewSkuListFieldId))).getInstance();
        Assert.assertEquals(3, skuListView.getArraySize());
        var newSkuId = Objects.requireNonNull(skuListView.getElement(2).underlyingInstance().id());
        MatcherAssert.assertThat(loadedProductView, new InstanceDTOMatcher(productView, Set.of(newSkuId)));

        var sku = skuListView.getElement(1);
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.delete(TestUtils.getId(sku)));
        // assert that the sku is actually removed
        loadedProductView = instanceManager.get(viewId.toString(), 1).instance();
        skuListView = ((InstanceFieldValue) (loadedProductView.getFieldValue(productViewSkuListFieldId))).getInstance();
        Assert.assertEquals(2, skuListView.arraySize());

        // test removing product view
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.delete(viewId.toString()));
    }

    public void testOrderQuery() {
        var shoppingTypeIds = MockUtils.createShoppingTypes(typeManager, flowManager);
        var skuId = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, RefDTO.fromId(shoppingTypeIds.skuTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(shoppingTypeIds.skuTitleFieldId(), PrimitiveFieldValue.createString("鞋子40")),
                                InstanceFieldDTO.create(shoppingTypeIds.skuAmountFieldId(), PrimitiveFieldValue.createLong(100L)),
                                InstanceFieldDTO.create(shoppingTypeIds.skuPriceFieldId(), PrimitiveFieldValue.createDouble(100.0))
                        )
                )
        )));
        var coupon1Id = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, RefDTO.fromId(shoppingTypeIds.couponTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(shoppingTypeIds.couponTitleFieldId(), PrimitiveFieldValue.createString("鞋子减5元")),
                                InstanceFieldDTO.create(shoppingTypeIds.couponDiscountFieldId(), PrimitiveFieldValue.createDouble(5.0)),
                                InstanceFieldDTO.create(shoppingTypeIds.couponStateFieldId(), ReferenceFieldValue.create(shoppingTypeIds.couponNormalStateId()))
                        )
                )
        )));
        var coupon2Id = TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, RefDTO.fromId(shoppingTypeIds.couponTypeId()), null, null, null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(shoppingTypeIds.couponTitleFieldId(), PrimitiveFieldValue.createString("鞋子减10元")),
                                InstanceFieldDTO.create(shoppingTypeIds.couponDiscountFieldId(), PrimitiveFieldValue.createDouble(10.0)),
                                InstanceFieldDTO.create(shoppingTypeIds.couponStateFieldId(), ReferenceFieldValue.create(shoppingTypeIds.couponNormalStateId()))
                        )
                )
        )));
        TestUtils.doInTransaction(() -> instanceManager.create(new InstanceDTO(
                null, RefDTO.fromId(shoppingTypeIds.orderTypeId()),
                null,
                null,
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        shoppingTypeIds.orderCodeFieldId(), PrimitiveFieldValue.createString("鞋子001")
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
                                        shoppingTypeIds.orderCouponsFieldId(), new ArrayFieldValue(
                                                null, false,
                                                List.of(
                                                        ReferenceFieldValue.create(coupon1Id), ReferenceFieldValue.create(coupon2Id)
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
        var productViewTypeId = TestUtils.getDefaultViewTypeId(productTypeDTO);
        var total = instanceManager.query(new InstanceQueryDTO(
                productViewTypeId,  mapping.id(), null, null, List.of(), 1, 20,
                true, false, List.of()
        )).page().total();
        Assert.assertEquals(1, total);
    }

    public void testNewRootView() {
        var shoppingTypeIds = MockUtils.createShoppingTypes(typeManager, flowManager);
        var skuTypeDTO = getType(shoppingTypeIds.skuTypeId());
        var skuDefaultMapping = NncUtils.findRequired(
                skuTypeDTO.getClassParam().mappings(),
                m -> Objects.equals(skuTypeDTO.getClassParam().defaultMappingRef(), m.getRef()));
        var skuViewTypeDTO = getType(skuDefaultMapping.targetTypeRef().id());
        var skuViewId = saveInstance(
                new InstanceDTO(
                        null,
                        RefDTO.fromId(skuViewTypeDTO.id()),
                        null,
                        null,
                        skuDefaultMapping.id(),
                        new ClassInstanceParam(
                                List.of(
                                        InstanceFieldDTO.create(
                                                TestUtils.getFieldIdByCode(skuViewTypeDTO, "title"),
                                                PrimitiveFieldValue.createString("40")
                                        ),
                                        InstanceFieldDTO.create(
                                                TestUtils.getFieldIdByCode(skuViewTypeDTO, "price"),
                                                PrimitiveFieldValue.createDouble(100.0)
                                        ),
                                        InstanceFieldDTO.create(
                                                TestUtils.getFieldIdByCode(skuViewTypeDTO, "amount"),
                                                PrimitiveFieldValue.createLong(100)
                                        )
                                )
                        )
                )
        );
        Assert.assertNotNull(skuViewId);
    }

    public void testGeneric() {
        var listTypeIds = MockUtils.createListType(typeManager, flowManager);
        var nodeTypeIds = listTypeIds.nodeTypeIds();
        var listOfStringType = typeManager.getParameterizedType(new GetParameterizedTypeRequest(
                RefDTO.fromId(listTypeIds.listTypeId()),
                List.of(StandardTypes.getStringType().getRef()),
                List.of()
        )).type();
        var nodeOfStringType = typeManager.getParameterizedType(new GetParameterizedTypeRequest(
                RefDTO.fromId(nodeTypeIds.nodeTypeId()),
                List.of(StandardTypes.getStringType().getRef()),
                List.of()
        )).type();
        var listDefaultMapping = TestUtils.getDefaultMapping(listOfStringType);
        var listViewTypeId = listDefaultMapping.targetTypeRef().id();
        var listViewType = typeManager.getType(new GetTypeRequest(listViewTypeId, false)).type();
        var nodeDefaultMapping = TestUtils.getDefaultMapping(nodeOfStringType);
        var nodeViewTypeId = nodeDefaultMapping.targetTypeRef().id();
        var nodeViewType = typeManager.getType(new GetTypeRequest(nodeViewTypeId, false)).type();
        var nodeViewChildArrayTypeId = typeManager.getArrayType(nodeViewTypeId, ArrayKind.CHILD.code()).type().id();
        var listView = new InstanceDTO(
                null,
                RefDTO.fromId(listViewTypeId),
                null,
                null,
                listDefaultMapping.id(),
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(listViewType, "label"),
                                        PrimitiveFieldValue.createString("list001")
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(listViewType, "nodes"),
                                        InstanceFieldValue.of(
                                                InstanceDTO.createArrayInstance(
                                                        RefDTO.fromId(nodeViewChildArrayTypeId),
                                                        true,
                                                        List.of(
                                                                InstanceFieldValue.of(
                                                                        InstanceDTO.createClassInstance(
                                                                                RefDTO.fromId(nodeViewTypeId),
                                                                                List.of(
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewType, "label"),
                                                                                                PrimitiveFieldValue.createString("node001")
                                                                                        ),
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewType, "value"),
                                                                                                PrimitiveFieldValue.createString("hello")
                                                                                        )
                                                                                )
                                                                        )
                                                                ),
                                                                InstanceFieldValue.of(
                                                                        InstanceDTO.createClassInstance(
                                                                                RefDTO.fromId(nodeViewTypeId),
                                                                                List.of(
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewType, "label"),
                                                                                                PrimitiveFieldValue.createString("node002")
                                                                                        ),
                                                                                        InstanceFieldDTO.create(
                                                                                                TestUtils.getFieldIdByCode(nodeViewType, "value"),
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

    private String saveInstance(InstanceDTO instanceDTO) {
        String id;
        if (instanceDTO.id() == null)
            id = TestUtils.doInTransaction(() -> instanceManager.create(instanceDTO));
        else {
            id = instanceDTO.id();
            TestUtils.doInTransactionWithoutResult(() -> instanceManager.update(instanceDTO));
        }
        return id;
    }


}
