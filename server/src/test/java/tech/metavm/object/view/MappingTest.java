package tech.metavm.object.view;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.event.MockEventQueue;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.core.DefaultViewId;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.log.TaskHandler;
import tech.metavm.object.instance.log.VersionHandler;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.TypeManager;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.List;
import java.util.Objects;

public class MappingTest extends TestCase {

    private TypeManager typeManager;
    private InstanceManager instanceManager;

    @Override
    protected void setUp() throws Exception {
        MemInstanceStore instanceStore = new MemInstanceStore();
        EntityIdProvider idProvider = new MockIdProvider();
        MemInstanceSearchService instanceSearchService = new MemInstanceSearchService();
        InstanceContextFactory instanceContextFactory =
                TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, instanceStore.getIndexEntryMapper());
        entityContextFactory.setInstanceLogService(
                new InstanceLogServiceImpl(entityContextFactory, instanceSearchService, instanceStore, List.of(
                        new TaskHandler(entityContextFactory, new MockTransactionOperations()),
                        new VersionHandler(new MockEventQueue())
                ))
        );
        entityContextFactory.setDefaultAsyncLogProcess(false);

        BootstrapUtils.bootstrap(entityContextFactory);

        TransactionOperations transactionOperations = new MockTransactionOperations();

        EntityQueryService entityQueryService = new EntityQueryService(new InstanceQueryService(instanceSearchService));
        typeManager = new TypeManager(
                entityContextFactory, entityQueryService,
                new TaskManager(entityContextFactory, transactionOperations),
                transactionOperations);
        instanceManager = new InstanceManager(
                entityContextFactory, instanceStore, new InstanceQueryService(instanceSearchService)
        );
        typeManager.setInstanceManager(instanceManager);
        var flowManager = new FlowManager(entityContextFactory);
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowManager(flowManager);
        FlowSavingContext.initConfig();
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
        var typeIds = MockUtils.createShoppingTypes(typeManager);
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
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        saveInstance(productView);
        var loadedProductView = instanceManager.get(viewId.toString(), 1).instance();
        MatcherAssert.assertThat(loadedProductView, InstanceDTOMatcher.of(productView));

        // test removing an SKU
        skuListView = ((InstanceFieldValue) (loadedProductView.getFieldValue(productViewSkuListFieldId))).getInstance();
        var sku = skuListView.getElement(1);
        TestUtils.doInTransaction(() -> instanceManager.delete(TestUtils.getId(sku)));
        // assert that the sku is actually removed
        loadedProductView = instanceManager.get(viewId.toString(), 1).instance();
        skuListView = ((InstanceFieldValue) (loadedProductView.getFieldValue(productViewSkuListFieldId))).getInstance();
        Assert.assertEquals(1, skuListView.arraySize());
    }

    private String saveInstance(InstanceDTO instanceDTO) {
        TestUtils.startTransaction();
        String id;
        if (instanceDTO.id() == null)
            id = instanceManager.create(instanceDTO);
        else {
            id = instanceDTO.id();
            instanceManager.update(instanceDTO);
        }
        TestUtils.commitTransaction();
        return id;
    }


}
