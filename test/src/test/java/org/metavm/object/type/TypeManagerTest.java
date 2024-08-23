package org.metavm.object.type;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.flow.FlowSavingContext;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.MemInstanceSearchServiceV2;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.rest.ClassInstanceParam;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceFieldDTO;
import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.rest.dto.*;
import org.metavm.task.AddFieldTask;
import org.metavm.task.Scheduler;
import org.metavm.task.Worker;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TypeManagerTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(TypeManagerTest.class);

    private MemInstanceSearchServiceV2 instanceSearchService;
    private TypeManager typeManager;
    private InstanceManager instanceManager;
    private EntityContextFactory entityContextFactory;
    private Scheduler scheduler;
    private Worker worker;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        instanceSearchService = bootResult.instanceSearchService();
        var managers = TestUtils.createCommonManagers(bootResult);
        typeManager = managers.typeManager();
        instanceManager = managers.instanceManager();
        entityContextFactory = bootResult.entityContextFactory();
        scheduler = managers.scheduler();
        worker = managers.worker();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() {
        typeManager = null;
        instanceSearchService = null;
        instanceManager = null;
        entityContextFactory = null;
        scheduler = null;
        worker = null;
    }

    public void test() {
        KlassDTO klassDTO = ClassTypeDTOBuilder.newBuilder("Bat")
                .tmpId(NncUtils.randomNonNegative())
                .addField(
                        FieldDTOBuilder.newBuilder("name", "string")
                                .build()
                )
                .build();
        KlassDTO savedKlassDTO = TestUtils.doInTransaction(() -> typeManager.saveType(klassDTO));
        KlassDTO loadedKlassDTO = typeManager.getType(new GetTypeRequest(savedKlassDTO.id(), true)).type();
        MatcherAssert.assertThat(loadedKlassDTO, PojoMatcher.of(savedKlassDTO));
        KlassDTO updatedKlassDTO = ClassTypeDTOBuilder.newBuilder("Bat Update")
                .id(savedKlassDTO.id())
                .fields(null)
                .methods(null)
                .mappings(null)
                .build();

        TestUtils.doInTransactionWithoutResult(() -> typeManager.saveType(updatedKlassDTO));
        loadedKlassDTO = typeManager.getType(new GetTypeRequest(savedKlassDTO.id(), true)).type();
        TestUtils.logJSON(logger, loadedKlassDTO);
        TestUtils.logJSON(logger, updatedKlassDTO);
        Assert.assertEquals(loadedKlassDTO.name(), updatedKlassDTO.name());
    }

    public void testRemove() {
        KlassDTO klassDTO = ClassTypeDTOBuilder.newBuilder("Bat")
                .tmpId(NncUtils.randomNonNegative())
                .addField(
                        FieldDTOBuilder.newBuilder("name", "string")
                                .build()
                )
                .build();
        KlassDTO savedKlassDTO = TestUtils.doInTransaction(() -> typeManager.saveType(klassDTO));
        TestUtils.waitForAllTasksDone(entityContextFactory);
        Assert.assertTrue(instanceSearchService.contains(TestUtils.getTypeId(savedKlassDTO)));
        TestUtils.doInTransactionWithoutResult(() -> typeManager.remove(savedKlassDTO.id()));
        TestUtils.waitForAllTasksDone(entityContextFactory);
        Assert.assertFalse(instanceSearchService.contains(TestUtils.getTypeId(savedKlassDTO)));
    }

    public void testShopping() {
        var typeIds = MockUtils.createShoppingTypes(typeManager, entityContextFactory);
        var productTypeDTO = typeManager.getType(new GetTypeRequest(typeIds.productTypeId(), false)).type();
        Assert.assertEquals(2, productTypeDTO.fields().size());
        var couponStateType = typeManager.getType(new GetTypeRequest(typeIds.couponStateTypeId(), false)).type();
        Assert.assertEquals(2, couponStateType.enumConstants().size());
        FlowSavingContext.initConfig();
        TestUtils.doInTransaction(() -> typeManager.batchSave(
                new BatchSaveRequest(
                        List.of(productTypeDTO),
                        List.of(),
                        false
                )
        ));
    }

    public void testAddFieldWithDefaultValueToTemplate() {
//        var nodeTypeIds = MockUtils.createNodeTypes(typeManager);
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/Node.masm", typeManager, entityContextFactory);
        var nodeType = typeManager.getTypeByCode("Node").type();
//        var nodeType = typeManager.getType(new GetTypeRequest(nodeTypeIds.nodeTypeId(), false)).type();
        var pNodeType = TypeExpressions.getParameterizedType(nodeType.id(), "string");
        var labelFieldId = TestUtils.getFieldIdByCode(nodeType, "label");
        var valueFieldId = TestUtils.getFieldIdByCode(nodeType, "value");
        TestUtils.doInTransactionWithoutResult(() -> instanceManager.create(new InstanceDTO(
                null,
                pNodeType,
                null,
                null,
                null,
                new ClassInstanceParam(
                        List.of(
                                InstanceFieldDTO.create(labelFieldId, PrimitiveFieldValue.createString("node001")),
                                InstanceFieldDTO.create(valueFieldId, PrimitiveFieldValue.createString("hello"))
                        )
                )
        )));
        TestUtils.doInTransactionWithoutResult(() -> typeManager.saveField(FieldDTOBuilder.newBuilder("code", "string")
                .declaringTypeId(nodeType.id())
                .code("code")
                .defaultValue(PrimitiveFieldValue.createString("000"))
                .build()));
    }

    public void testUpdateField() {
        var typeDTO = ClassTypeDTOBuilder.newBuilder("Bat")
                .tmpId(NncUtils.randomNonNegative())
                .addField(
                        FieldDTOBuilder.newBuilder("name", "string")
                                .build()
                )
                .build();
        var savedTypeDTO = TestUtils.doInTransaction(() -> typeManager.saveType(typeDTO));
        var updatedFieldDTO = FieldDTOBuilder.newBuilder("name", "string")
                .id(savedTypeDTO.fields().get(0).id())
                .declaringTypeId(savedTypeDTO.id())
                .code("name")
                .build();
        TestUtils.doInTransactionWithoutResult(() -> typeManager.saveField(updatedFieldDTO));
        var loadedTypeDTO = typeManager.getType(new GetTypeRequest(savedTypeDTO.id(), true)).type();
        Assert.assertEquals("name", loadedTypeDTO.fields().get(0).code());
    }

    public void testAddField() {
        var titleFieldId = TmpId.random().toString();
        var skuType = TestUtils.doInTransaction(() -> typeManager.saveType(ClassTypeDTOBuilder.newBuilder("SKU")
                .id(TmpId.random().toString())
                .addField(FieldDTOBuilder.newBuilder("title", "string")
                        .id(titleFieldId)
                        .build())
                .titleFieldId(titleFieldId)
                .build()));

        TestUtils.doInTransaction(() -> typeManager.saveField(
                FieldDTOBuilder.newBuilder("price", "double")
                        .id(TmpId.random().toString())
                        .declaringTypeId(skuType.id())
                        .build()
        ));

        TestUtils.doInTransaction(() -> typeManager.saveField(
                FieldDTOBuilder.newBuilder("quantity", "long")
                        .id(TmpId.random().toString())
                        .declaringTypeId(skuType.id())
                        .build()
        ));

        var skuChildArrayType = TypeExpressions.getChildArrayType(TypeExpressions.getClassType(skuType.id()));

        var productType = TestUtils.doInTransaction(() -> typeManager.saveType(ClassTypeDTOBuilder.newBuilder("Product")
                .id(TmpId.random().toString())
                .addField(FieldDTOBuilder.newBuilder("title", "string")
                        .id(titleFieldId)
                        .build())
                .titleFieldId(titleFieldId)
                .build()));

        TestUtils.doInTransaction(() -> typeManager.saveField(
                FieldDTOBuilder.newBuilder("sku", skuChildArrayType)
                        .id(TmpId.random().toString())
                        .declaringTypeId(productType.id())
                        .isChild(true)
                        .build()
        ));
        TestUtils.waitForTaskDone(scheduler, worker, t ->
                t instanceof AddFieldTask addFieldTask && addFieldTask.getField().getName().equals("sku")
        );
        var skuViewType = TestUtils.getViewKlass(skuType, typeManager);
        var productViewType = TestUtils.getViewKlass(productType, typeManager);
        var skuViewChildArrayType = TypeExpressions.getChildArrayType(TypeExpressions.getClassType(skuViewType.id()));
        var productViewSkuField = TestUtils.getFieldByName(productViewType, "sku");
        Assert.assertEquals(skuViewChildArrayType, productViewSkuField.type());
        var fieldId = TestUtils.doInTransaction(() -> typeManager.saveField(
                FieldDTOBuilder.newBuilder("desc", "string")
                        .id(TmpId.random().toString())
                        .declaringTypeId(productType.id())
                        .build()
        ));
        TestUtils.waitForTaskDone(scheduler, worker, t ->
                t instanceof AddFieldTask addFieldTask && addFieldTask.getField().getName().equals("desc")
        );
        var reloadedProductViewType = TestUtils.getViewKlass(productType, typeManager);
        var reloadedProductViewSkuField = TestUtils.getFieldByName(reloadedProductViewType, "sku");
        Assert.assertEquals(skuViewChildArrayType, reloadedProductViewSkuField.type());
    }

    public void testSynchronizeSearch() {
        var fooKlassId = TestUtils.doInTransaction(() -> {
            try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var fooKlass = TestUtils.newKlassBuilder("SynchronizeFoo").build();
                context.bind(fooKlass);
                context.finish();
                Assert.assertTrue(((ClassInstance)context.getInstance(fooKlass)).isSearchable());
                return fooKlass.getId();
            }
        });
        DebugEnv.id = fooKlassId;
//        TestUtils.waitForTaskDone(t -> t instanceof SynchronizeSearchTask, entityContextFactory);
        DebugEnv.flag = true;
        TestUtils.waitForAllTasksDone(entityContextFactory);
        var queryResult = typeManager.query(new TypeQuery(
                "SynchronizeFoo",
                List.of(ClassKind.CLASS.code()),
                null,
                false,
                false,
                null,
                List.of(),
                1,
                20
        ));
        Assert.assertEquals(1, queryResult.data().size());
    }

}