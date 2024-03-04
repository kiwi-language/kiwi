package tech.metavm.autograph;

import org.junit.Assert;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.core.DefaultViewId;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.util.BusinessException;
import tech.metavm.util.TestUtils;

import java.util.List;

import static tech.metavm.util.TestUtils.doInTransaction;
import static tech.metavm.util.TestUtils.doInTransactionWithoutResult;

public class ManufacturingCompileTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/manufacturing";

    public void test() {
        compileTwice(SOURCE_ROOT);
        submit(() -> {
            var roundingRuleType = getClassTypeByCode("tech.metavm.manufacturing.material.RoundingRule");
            var roundHalfUp = TestUtils.getEnumConstantByName(roundingRuleType, "四舍五入");
            var unitType = getClassTypeByCode("tech.metavm.manufacturing.material.Unit");
            var unitConstructorId = TestUtils.getMethodIdByCode(unitType, "Unit");
            var unit = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    unitConstructorId,
                    null,
                    List.of(
                            PrimitiveFieldValue.createString("米"),
                            PrimitiveFieldValue.createString("meter"),
                            ReferenceFieldValue.create(roundHalfUp),
                            PrimitiveFieldValue.createLong(2L),
                            PrimitiveFieldValue.createNull()
                    )
            )));

            var materialKindType = getClassTypeByCode("tech.metavm.manufacturing.material.MaterialKind");
            var normal = TestUtils.getEnumConstantByName(materialKindType, "普通");

            var timeUnitType = getClassTypeByCode("tech.metavm.manufacturing.material.TimeUnit");
            var year = TestUtils.getEnumConstantByName(timeUnitType, "年");

            // create a material
            var materialType = getClassTypeByCode("tech.metavm.manufacturing.material.Material");
            var materialConstructorId = TestUtils.getMethodIdByCode(materialType, "Material");
            var material = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    materialConstructorId,
                    null,
                    List.of(
                            PrimitiveFieldValue.createString("sheet metal"),
                            PrimitiveFieldValue.createString("钢板"),
                            ReferenceFieldValue.create(normal),
                            ReferenceFieldValue.create(unit),
                            PrimitiveFieldValue.createLong(1L),
                            ReferenceFieldValue.create(year)
                    )
            )));
            Assert.assertNotNull(material);

            var materialId = material.id();
            // get QualityInspectionState type
            var qualityInspectionStateType = getClassTypeByCode("tech.metavm.manufacturing.material.QualityInspectionState");
            // get QualityInspectionState.QUALIFIED constant
            var qualified = TestUtils.getEnumConstantByName(qualityInspectionStateType, "合格");
            // invoke material.setFeedQualityInspectionStates with a list containing the QUALIFIED constant
            var setFeedQualityInspectionStatesId = TestUtils.getMethodIdByCode(materialType, "setFeedQualityInspectionStates");
            doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    setFeedQualityInspectionStatesId,
                    materialId,
                    List.of(
                            new ListFieldValue(null, false, List.of(ReferenceFieldValue.create(qualified)))
                    )
            )));

            // reload the material object
            material = instanceManager.get(materialId, 1).instance();
            // assert that the feedQualityInspectionStates field of the material object contains the QUALIFIED constant
            var feedQualityInspectionStates = material.getFieldValue(TestUtils.getFieldIdByCode(materialType, "feedQualityInspectionStates"));
            Assert.assertTrue(feedQualityInspectionStates instanceof InstanceFieldValue);
            var feedQualityInspectionStatesList = ((InstanceFieldValue) feedQualityInspectionStates).getInstance().getElements();
            Assert.assertEquals(1, feedQualityInspectionStatesList.size());
            Assert.assertEquals(qualified.id(), ((ReferenceFieldValue) feedQualityInspectionStatesList.get(0)).getId());


            // get default mapping of material type
            var materialDefaultMapping = TestUtils.getDefaultMapping(materialType);
            // get material view
            var materialViewType = typeManager.getType(new GetTypeRequest(materialDefaultMapping.targetTypeRef().id(), false)).type();
            // load material view object
            var materialView = instanceManager.get(
                    new DefaultViewId(materialDefaultMapping.id(), Id.parse(materialId)).toString(),
                    1
            ).instance();
            // save material view
            doInTransactionWithoutResult(() -> instanceManager.update(materialView));
            // reload the material view object
            var reloadedMaterialView = instanceManager.get(materialView.id(), 1).instance();
            // check the feedQualityInspectionStates field and assert that it didn't change
            var reloadedFeedQualityInspectionStates = reloadedMaterialView.getFieldValue(TestUtils.getFieldIdByCode(materialViewType, "feedQualityInspectionStates"));
            Assert.assertTrue(reloadedFeedQualityInspectionStates instanceof InstanceFieldValue);
            var reloadedFeedQualityInspectionStatesList = ((InstanceFieldValue) reloadedFeedQualityInspectionStates).getInstance().getElements();
            Assert.assertEquals(1, reloadedFeedQualityInspectionStatesList.size());
            Assert.assertEquals(qualified.id(), ((ReferenceFieldValue) reloadedFeedQualityInspectionStatesList.get(0)).getId());


            // get InventoryAttributes type
            var inventoryAttributesType = getClassTypeByCode("tech.metavm.manufacturing.material.InventoryAttributes");
            // get default mapping
            var defaultMapping = TestUtils.getDefaultMapping(inventoryAttributesType);
            Assert.assertNotNull(defaultMapping);
            // get target type of default mapping
            var targetTypeRef = defaultMapping.targetTypeRef();
            var inventoryAttrViewType = typeManager.getType(new GetTypeRequest(targetTypeRef.id(), false)).type();
            // assert that the view type and the source type have the same number of fields
            Assert.assertEquals(inventoryAttributesType.getClassParam().fields().size(),
                    inventoryAttrViewType.getClassParam().fields().size());

            // get Utils type
            var utilsType = getClassTypeByCode("tech.metavm.manufacturing.utils.Utils");
            Assert.assertEquals(0, utilsType.getClassParam().errors().size());

            var position = createPosition();
            processInventory(material, position, qualified, unit);

            processInbound();
        });
    }

    private InstanceDTO createPosition() {
        var warehouseType = getClassTypeByCode("tech.metavm.manufacturing.storage.Warehouse");
        var warehouseConstructorId = TestUtils.getMethodIdByCode(warehouseType, "Warehouse");
        var warehouse = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                warehouseConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("warehouse1"),
                        PrimitiveFieldValue.createString("仓库1")
                )
        )));
        var areaType = getClassTypeByCode("tech.metavm.manufacturing.storage.Area");
        var areaConstructorId = TestUtils.getMethodIdByCode(areaType, "Area");
        var area = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                areaConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("area1"),
                        PrimitiveFieldValue.createString("库区1"),
                        ReferenceFieldValue.create(warehouse),
                        PrimitiveFieldValue.createNull(
                        )
                ))));
        var positionType = getClassTypeByCode("tech.metavm.manufacturing.storage.Position");
        var positionConstructorId = TestUtils.getMethodIdByCode(positionType, "Position");
        return doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                positionConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("position1"),
                        PrimitiveFieldValue.createString("库位1"),
                        ReferenceFieldValue.create(area)
                )
        )));
    }


    private void processInventory(InstanceDTO material,
                                  InstanceDTO position,
                                  InstanceDTO qualifiedInspectionState,
                                  InstanceDTO unit
    ) {
        // get inventory type
        var inventoryType = getClassTypeByCode("tech.metavm.manufacturing.storage.Inventory");
        // get inventoryBizState type
        var inventoryBizStateType = getClassTypeByCode("tech.metavm.manufacturing.storage.InventoryBizState");
        // get InventoryBizState.INITIAL constant
        var initialBizState = TestUtils.getEnumConstantByName(inventoryBizStateType, "初始");

        // create an inventory object
        var inventoryConstructorId = TestUtils.getMethodIdByCode(inventoryType, "Inventory");
        var inventory = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                inventoryConstructorId,
                null,
                List.of(
                        ReferenceFieldValue.create(material),
                        ReferenceFieldValue.create(position),
                        ReferenceFieldValue.create(qualifiedInspectionState),
                        ReferenceFieldValue.create(initialBizState),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createLong(100L)
                )
        )));

        // query the inventory object by condition
        var queryResp = instanceManager.query(
                new InstanceQueryDTO(
                        inventoryType.id(),
                        null,
                        null,
                        String.format("物料 = $$%s and 库位 = $$%s and 质检状态 = $$%s and 业务状态 = $$%s",
                                material.id(), position.id(), qualifiedInspectionState.id(), initialBizState.id()),
                        List.of(),
                        1,
                        20,
                        false,
                        false,
                        List.of()
                )
        );
        Assert.assertEquals(1, queryResp.page().total());
        var queriedInventory = queryResp.page().data().get(0);
        Assert.assertEquals(inventory.id(), queriedInventory.id());

        // decrease the inventory by 100 and asserts that the inventory is removed
        var decreaseInventoryId = TestUtils.getStaticMethodIdByCode(inventoryType, "decreaseQuantity");
        doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                decreaseInventoryId,
                null,
                List.of(
                        ReferenceFieldValue.create(material),
                        ReferenceFieldValue.create(position),
                        ReferenceFieldValue.create(qualifiedInspectionState),
                        ReferenceFieldValue.create(initialBizState),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createLong(100L),
                        ReferenceFieldValue.create(unit)
                )
        )));
        try {
            instanceManager.get(inventory.id(), 1);
            Assert.fail("Inventory should be removed");
        } catch (BusinessException e) {
            Assert.assertEquals(String.format("对象'%s'不存在", Id.parse(inventory.id()).getPhysicalId()), e.getMessage());
        }
    }

    private void processInbound() {
        var inboundRequestType = getClassTypeByCode("tech.metavm.manufacturing.storage.InboundRequest");
        Assert.assertTrue(inboundRequestType.getClassParam().struct());
    }

}
