package tech.metavm.autograph;

import org.junit.Assert;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.core.DefaultViewId;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.TypeExpressions;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.view.rest.dto.DirectMappingKey;
import tech.metavm.object.view.rest.dto.ObjectMappingRefDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.TestUtils;

import java.util.List;

import static tech.metavm.util.TestUtils.doInTransaction;
import static tech.metavm.util.TestUtils.doInTransactionWithoutResult;

public class ManufacturingCompileTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/manufacturing";

    public void test() {
        compileTwice(SOURCE_ROOT);
//        compile(SOURCE_ROOT);
        submit(() -> {
            var profiler = ContextUtil.getProfiler();
            try (var ignored = profiler.enter("submit")) {
                var roundingRuleType = getClassTypeByCode("tech.metavm.manufacturing.material.RoundingRule");
                var roundHalfUp = TestUtils.getEnumConstantByName(roundingRuleType, "四舍五入");
                var unitType = getClassTypeByCode("tech.metavm.manufacturing.material.Unit");
                var unit = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(unitType, "Unit"),
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
                var material = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(materialType, "Material"),
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
                doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(materialType, "setFeedQualityInspectionStates"),
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
                var materialViewType = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(materialDefaultMapping.targetType()), false)).type();
                // load material view object
                var materialView = instanceManager.get(
                        new DefaultViewId(false, new DirectMappingKey(materialDefaultMapping.id()), Id.parse(materialId)).toString(),
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
                var targetTypeId = TypeExpressions.extractKlassId(defaultMapping.targetType());
                var inventoryAttrViewType = typeManager.getType(new GetTypeRequest(targetTypeId, false)).type();
                // assert that the view type and the source type have the same number of fields
                Assert.assertEquals(inventoryAttributesType.getClassParam().fields().size(),
                        inventoryAttrViewType.getClassParam().fields().size());

                // get Utils type
                var utilsType = getClassTypeByCode("tech.metavm.manufacturing.utils.Utils");
                Assert.assertEquals(0, utilsType.getClassParam().errors().size());

                var storageObjects = createPosition();
                try (var ignored2 = profiler.enter("processInventory")) {
                    processInventory(material, storageObjects.position, qualified, unit);
                }

                try (var ignored2 = profiler.enter("processInbound")) {
                    processInbound(storageObjects, material, unit);
                }

                try (var ignored2 = profiler.enter("processTransfer")) {
                    processTransfer(storageObjects, material, unit);
                }

                RoutingObjects routingObjects;
                try (var ignored2 = profiler.enter("processObjects")) {
                    routingObjects = processRouting(material, unit);
                }

                try (var ignored2 = profiler.enter("processBOM")) {
                    processBOM(
                            material,
                            unit,
                            routingObjects.routing,
                            routingObjects.routingProcess,
                            getClassTypeByCode("tech.metavm.manufacturing.production.FeedType"),
                            getClassTypeByCode("tech.metavm.manufacturing.production.PickMethod"),
                            getClassTypeByCode("tech.metavm.manufacturing.GeneralState"),
                            qualityInspectionStateType
                    );
                }
            }
            System.out.println(profiler.finish(false, true).output());
        });
    }

    private StorageObjects createPosition() {
        var warehouseType = getClassTypeByCode("tech.metavm.manufacturing.storage.Warehouse");
        var warehouse = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                TestUtils.getMethodRefByCode(warehouseType, "Warehouse"),
                null,
                List.of(
                        PrimitiveFieldValue.createString("warehouse1"),
                        PrimitiveFieldValue.createString("仓库1")
                )
        )));
        var areaType = getClassTypeByCode("tech.metavm.manufacturing.storage.Area");
        var area = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                TestUtils.getMethodRefByCode(areaType, "Area"),
                null,
                List.of(
                        PrimitiveFieldValue.createString("area1"),
                        PrimitiveFieldValue.createString("库区1"),
                        ReferenceFieldValue.create(warehouse),
                        PrimitiveFieldValue.createNull(
                        )
                ))));
        var positionType = getClassTypeByCode("tech.metavm.manufacturing.storage.Position");
        var positionConstructorId = TestUtils.getMethodRefByCode(positionType, "Position");
        var position = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                positionConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("position1"),
                        PrimitiveFieldValue.createString("库位1"),
                        ReferenceFieldValue.create(area)
                )
        )));
        var position2 = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                positionConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("position2"),
                        PrimitiveFieldValue.createString("库位2"),
                        ReferenceFieldValue.create(area)
                )
        )));

        return new StorageObjects(warehouse, area, position, position2);
    }

    private record StorageObjects(
            InstanceDTO warehouse,
            InstanceDTO area,
            InstanceDTO position,
            InstanceDTO position2
    ) {

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
        var inventoryConstructorId = TestUtils.getMethodRefByCode(inventoryType, "Inventory");
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
                        TypeExpressions.getClassType(inventoryType.id()),
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

        var inventoryOpType = getClassTypeByCode("tech.metavm.manufacturing.storage.InventoryOp");
        var adjustment = TestUtils.getEnumConstantByName(inventoryOpType, "库存调整");

        // decrease the inventory by 100 and asserts that the inventory is removed
        var decreaseInventoryId = TestUtils.getStaticMethodRefByCode(inventoryType, "decreaseQuantity");
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
                        ReferenceFieldValue.create(unit),
                        ReferenceFieldValue.create(adjustment)
                )
        )));
        try {
            instanceManager.get(inventory.id(), 1);
            Assert.fail("Inventory should be removed");
        } catch (BusinessException e) {
            Assert.assertEquals(String.format("对象'库存-%s'不存在", inventory.id()), e.getMessage());
        }
    }

    private void processInbound(StorageObjects storageObjects, InstanceDTO material, InstanceDTO unit) {
        // get InboundBizType type
        var inboundBizTypeType = getClassTypeByCode("tech.metavm.manufacturing.storage.InboundBizType");
        // get InboundBizType.PURCHASE constant
        var purchase = TestUtils.getEnumConstantByName(inboundBizTypeType, "采购");

        // get InboundOrder type
        var inboundOrderType = getClassTypeByCode("tech.metavm.manufacturing.storage.InboundOrder");
        // create an inbound order
        var inboundOrderConstructorId = TestUtils.getMethodRefByCode(inboundOrderType, "InboundOrder");
        var inboundOrder = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                inboundOrderConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("inboundOrder1"),
                        ReferenceFieldValue.create(purchase),
                        ReferenceFieldValue.create(storageObjects.warehouse),
                        PrimitiveFieldValue.createNull()
                )
        )));
        // create an inbound order item
        var inboundOrderItemType = getClassTypeByCode("tech.metavm.manufacturing.storage.InboundOrderItem");
        var inboundOrderItemConstructorId = TestUtils.getMethodRefByCode(inboundOrderItemType, "InboundOrderItem");
        var inboundOrderItem = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                inboundOrderItemConstructorId,
                null,
                List.of(
                        ReferenceFieldValue.create(inboundOrder),
                        ReferenceFieldValue.create(material),
                        ReferenceFieldValue.create(storageObjects.position),
                        PrimitiveFieldValue.createLong(100L),
                        ReferenceFieldValue.create(unit),
                        PrimitiveFieldValue.createNull()
                )
        )));

        // get the InboundRequest type
        var inboundRequestType = getClassTypeByCode("tech.metavm.manufacturing.storage.InboundRequest");

        // assert that the InboundRequest type is abstract
        Assert.assertTrue(inboundRequestType.getClassParam().isAbstract());

        // get the ByAmountInboundRequest type
        var byAmountInboundRequestType = getClassTypeByCode("tech.metavm.manufacturing.storage.ByAmountInboundRequest");
        // assert that the ByAmountInboundRequest type is a struct
        Assert.assertTrue(byAmountInboundRequestType.getClassParam().struct());

        // invoke InboundOrderItem.inbound with the inboundRequest object
        var inboundId = TestUtils.getMethodRefByCode(inboundOrderItemType, "inbound");
        doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                inboundId,
                inboundOrderItem.id(),
                List.of(
                        InstanceFieldValue.of(
                                InstanceDTO.createClassInstance(
                                        TypeExpressions.getClassType(byAmountInboundRequestType.id()),
                                        List.of(
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "bizType"),
                                                        ReferenceFieldValue.create(purchase)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "position"),
                                                        ReferenceFieldValue.create(storageObjects.position)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "material"),
                                                        ReferenceFieldValue.create(material)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "batch"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // supplier
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "supplier"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // supplier batch no
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "supplierBatchNo"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // client
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "client"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // arrival date
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "arrivalDate"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // production date
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "productionDate"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // expiration date
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "expirationDate"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "unit"),
                                                        ReferenceFieldValue.create(unit)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(byAmountInboundRequestType, "amount"),
                                                        PrimitiveFieldValue.createLong(100L)
                                                )
                                        )
                                )
                        )
                )
        )));

        // reload the inbound order item and check the actualQuantity field
        var reloadedInboundOrderItem = instanceManager.get(inboundOrderItem.id(), 1).instance();
        var actualQuantity = reloadedInboundOrderItem.getFieldValue(TestUtils.getFieldIdByCode(inboundOrderItemType, "actualQuantity"));
        Assert.assertEquals(100L, ((PrimitiveFieldValue) actualQuantity).getValue());

        // inbound by spec

        // get the ByAmountInboundRequest type
        var bySpecInboundRequestType = getClassTypeByCode("tech.metavm.manufacturing.storage.BySpecInboundRequest");
        var bySpecInboundRequestItemType = getClassTypeByCode("tech.metavm.manufacturing.storage.BySpecInboundRequestItem");
        // assert that the ByAmountInboundRequest type is a struct
        Assert.assertTrue(byAmountInboundRequestType.getClassParam().struct());

        // invoke InboundOrderItem.inbound with the inboundRequest object
        doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                inboundId,
                inboundOrderItem.id(),
                List.of(
                        InstanceFieldValue.of(
                                InstanceDTO.createClassInstance(
                                        TypeExpressions.getClassType(bySpecInboundRequestType.id()),
                                        List.of(
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "bizType"),
                                                        ReferenceFieldValue.create(purchase)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "position"),
                                                        ReferenceFieldValue.create(storageObjects.position)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "material"),
                                                        ReferenceFieldValue.create(material)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "batch"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // supplier
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "supplier"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // supplier batch no
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "supplierBatchNo"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // client
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "client"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // arrival date
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "arrivalDate"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // production date
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "productionDate"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                // expiration date
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "expirationDate"),
                                                        PrimitiveFieldValue.createNull()
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(inboundRequestType, "unit"),
                                                        ReferenceFieldValue.create(unit)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(bySpecInboundRequestType, "bySpecItems"),
                                                        new ListFieldValue(
                                                                null,
                                                                true,
                                                                List.of(
                                                                        InstanceFieldValue.of(
                                                                                InstanceDTO.createClassInstance(
                                                                                        TypeExpressions.getClassType(bySpecInboundRequestItemType.id()),
                                                                                        List.of(
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(bySpecInboundRequestItemType, "qrCodeAmount"),
                                                                                                        PrimitiveFieldValue.createLong(2)
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(bySpecInboundRequestItemType, "inboundAmount"),
                                                                                                        PrimitiveFieldValue.createLong(10)
                                                                                                )
                                                                                        )
                                                                                )
                                                                        ),
                                                                        InstanceFieldValue.of(
                                                                                InstanceDTO.createClassInstance(
                                                                                        TypeExpressions.getClassType(bySpecInboundRequestItemType.id()),
                                                                                        List.of(
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(bySpecInboundRequestItemType, "qrCodeAmount"),
                                                                                                        PrimitiveFieldValue.createLong(3)
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(bySpecInboundRequestItemType, "inboundAmount"),
                                                                                                        PrimitiveFieldValue.createLong(5)
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
                )
        )));

        reloadedInboundOrderItem = instanceManager.get(inboundOrderItem.id(), 1).instance();
        actualQuantity = reloadedInboundOrderItem.getFieldValue(TestUtils.getFieldIdByCode(inboundOrderItemType, "actualQuantity"));
        Assert.assertEquals(135L, ((PrimitiveFieldValue) actualQuantity).getValue());
    }


    private void processTransfer(StorageObjects storageObjects, InstanceDTO material, InstanceDTO unit) {
        // get TransferBizType type
        var transferBizTypeType = getClassTypeByCode("tech.metavm.manufacturing.storage.TransferBizType");
        // get TransferBizType.STORAGE constant
        var storage = TestUtils.getEnumConstantByName(transferBizTypeType, "仓储调拨");

        // get transfer order type
        var transferOrderType = getClassTypeByCode("tech.metavm.manufacturing.storage.TransferOrder");
        // create a transfer order
        var transferOrderConstructorId = TestUtils.getMethodRefByCode(transferOrderType, "TransferOrder");
        var transferOrder = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                transferOrderConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("transferOrder1"),
                        ReferenceFieldValue.create(storage),
                        ReferenceFieldValue.create(storageObjects.warehouse),
                        ReferenceFieldValue.create(storageObjects.warehouse)
                )
        )));

        // create a transfer order item
        var transferOrderItemType = getClassTypeByCode("tech.metavm.manufacturing.storage.TransferOrderItem");
        var transferOrderItemConstructorId = TestUtils.getMethodRefByCode(transferOrderItemType, "TransferOrderItem");
        var transferOrderItem = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                transferOrderItemConstructorId,
                null,
                List.of(
                        ReferenceFieldValue.create(transferOrder),
                        ReferenceFieldValue.create(material),
                        PrimitiveFieldValue.createLong(100L),
                        ReferenceFieldValue.create(unit),
                        PrimitiveFieldValue.createNull(),
                        PrimitiveFieldValue.createNull()
                )
        )));

        // create an inventory
        var inventoryType = getClassTypeByCode("tech.metavm.manufacturing.storage.Inventory");
        var inventoryConstructorId = TestUtils.getMethodRefByCode(inventoryType, "Inventory");
        var qualityInspectionStateType = getClassTypeByCode("tech.metavm.manufacturing.material.QualityInspectionState");
        var qualifiedInspectionState = TestUtils.getEnumConstantByName(qualityInspectionStateType, "合格");
        var InventoryBizStateType = getClassTypeByCode("tech.metavm.manufacturing.storage.InventoryBizState");
        var initialBizState = TestUtils.getEnumConstantByName(InventoryBizStateType, "初始");
        var inventory = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                inventoryConstructorId,
                null,
                List.of(
                        ReferenceFieldValue.create(material),
                        ReferenceFieldValue.create(storageObjects.position),
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

        // invoke TransferOrderItem.transfer with storageObjects.position2 as the target position
        var transferRequestType = getClassTypeByCode("tech.metavm.manufacturing.storage.TransferRequest");
        var transferRequestItemType = getClassTypeByCode("tech.metavm.manufacturing.storage.TransferRequestItem");
        var transferRequestSubItemType = getClassTypeByCode("tech.metavm.manufacturing.storage.TransferRequestSubItem");

        var transferId = TestUtils.getMethodRefByCode(transferOrderType, "transfer");
        doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                                transferId,
                                transferOrder.id(),
                                List.of(
                                        InstanceFieldValue.of(
                                                InstanceDTO.createClassInstance(
                                                        TypeExpressions.getClassType(transferRequestType.id()),
                                                        List.of(
                                                                InstanceFieldDTO.create(
                                                                        TestUtils.getFieldIdByCode(transferRequestType, "to"),
                                                                        ReferenceFieldValue.create(storageObjects.position2)
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        TestUtils.getFieldIdByCode(transferRequestType, "items"),
                                                                        new ListFieldValue(
                                                                                null,
                                                                                true,
                                                                                List.of(
                                                                                        InstanceFieldValue.of(
                                                                                                InstanceDTO.createClassInstance(
                                                                                                        TypeExpressions.getClassType(transferRequestItemType.id()),
                                                                                                        List.of(
                                                                                                                InstanceFieldDTO.create(
                                                                                                                        TestUtils.getFieldIdByCode(transferRequestItemType, "transferOrderItem"),
                                                                                                                        ReferenceFieldValue.create(transferOrderItem)
                                                                                                                ),
                                                                                                                InstanceFieldDTO.create(
                                                                                                                        TestUtils.getFieldIdByCode(transferRequestItemType, "subItems"),
                                                                                                                        new ListFieldValue(
                                                                                                                                null,
                                                                                                                                true,
                                                                                                                                List.of(
                                                                                                                                        InstanceFieldValue.of(
                                                                                                                                                InstanceDTO.createClassInstance(
                                                                                                                                                        TypeExpressions.getClassType(transferRequestSubItemType.id()),
                                                                                                                                                        List.of(
                                                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                                                        TestUtils.getFieldIdByCode(transferRequestSubItemType, "inventory"),
                                                                                                                                                                        ReferenceFieldValue.create(inventory)
                                                                                                                                                                ),
                                                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                                                        TestUtils.getFieldIdByCode(transferRequestSubItemType, "amount"),
                                                                                                                                                                        PrimitiveFieldValue.createLong(20L)
                                                                                                                                                                ),
                                                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                                                        TestUtils.getFieldIdByCode(transferRequestSubItemType, "unit"),
                                                                                                                                                                        ReferenceFieldValue.create(unit)
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
        // assert that the transfer has taken place
        var reloadedInventory = instanceManager.get(inventory.id(), 1).instance();
        assertEquals(PrimitiveFieldValue.createLong(80L), reloadedInventory.getFieldValue(TestUtils.getFieldIdByCode(inventoryType, "quantity")));
    }

    private record RoutingObjects(
            InstanceDTO routing,
            InstanceDTO routingProcess
    ) {

    }

    private RoutingObjects processRouting(InstanceDTO material, InstanceDTO unit) {
        var routingType = getClassTypeByCode("tech.metavm.manufacturing.production.Routing");
        var defaultMapping = TestUtils.getDefaultMapping(routingType);
        var routingViewType = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(defaultMapping.targetType()), false)).type();
        var fromViewMethodId = TestUtils.getStaticMethodRefByCode(routingType, "fromView");
        var routingProcessType = getClassTypeByCode("tech.metavm.manufacturing.production.RoutingProcess");
        var routingProcessViewType = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(TestUtils.getDefaultMapping(routingProcessType).targetType()), false)).type();

        var workCenterType = getClassTypeByCode("tech.metavm.manufacturing.production.WorkCenter");
        var workCenterConstructorId = TestUtils.getMethodRefByCode(workCenterType, "WorkCenter");
        var workCenter = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                workCenterConstructorId,
                null,
                List.of(
//                        PrimitiveFieldValue.createString("workCenter1"),
//                        PrimitiveFieldValue.createString("工作中心1")
                )
        )));

        var processType = getClassTypeByCode("tech.metavm.manufacturing.production.Process");
        var processConstructorId = TestUtils.getMethodRefByCode(processType, "Process");
        var process = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                processConstructorId,
                null,
                List.of(
                        PrimitiveFieldValue.createString("工序1")
                )
        )));

        var routing = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                fromViewMethodId,
                null,
                List.of(
                        InstanceFieldValue.of(
                                InstanceDTO.createClassInstance(
                                        TmpId.random().toString(),
                                        TypeExpressions.getClassType(routingViewType.id()),
                                        List.of(
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(routingViewType, "name"),
                                                        PrimitiveFieldValue.createString("routing001")
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(routingViewType, "product"),
                                                        ReferenceFieldValue.create(material)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(routingViewType, "unit"),
                                                        ReferenceFieldValue.create(unit)
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(routingViewType, "processes"),
                                                        new ListFieldValue(
                                                                null,
                                                                true,
                                                                List.of(
                                                                        // create a RoutingProcess
                                                                        InstanceFieldValue.of(
                                                                                InstanceDTO.createClassInstance(
                                                                                        TypeExpressions.getClassType(routingProcessViewType.id()),
                                                                                        List.of(
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "processCode"),
                                                                                                        PrimitiveFieldValue.createString("process1")
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "processDescription"),
                                                                                                        PrimitiveFieldValue.createString("工序1")
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "workCenter"),
                                                                                                        ReferenceFieldValue.create(workCenter)
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "sequence"),
                                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "process"),
                                                                                                        ReferenceFieldValue.create(process)
                                                                                                ),
                                                                                                InstanceFieldDTO.create(
                                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "items"),
                                                                                                        new ListFieldValue(
                                                                                                                null,
                                                                                                                true,
                                                                                                                List.of()
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                ),
                                                InstanceFieldDTO.create(
                                                        TestUtils.getFieldIdByCode(routingViewType, "successions"),
                                                        new ListFieldValue(
                                                                null,
                                                                true,
                                                                List.of()
                                                        )
                                                )
                                        )
                                )
                        )
                )
        )));

        Assert.assertNotNull(routing.id());
        // reload routing
        var reloadedRoutingView = instanceManager.getDefaultView(routing.id()).instance();
        var viewId = (DefaultViewId) Id.parse(reloadedRoutingView.id());
        Assert.assertEquals(viewId.getSourceId(), Id.parse(routing.id()));
        var routingProcesses = (InstanceFieldValue) routing.getFieldValue(TestUtils.getFieldIdByCode(routingType, "processes"));
        var routingProcess = ((InstanceFieldValue) TestUtils.getListElement(routingProcesses, 0)).getInstance();

        var processListView = (InstanceFieldValue) reloadedRoutingView.getFieldValue(TestUtils.getFieldIdByCode(routingViewType, "processes"));
        var itemView = ((InstanceFieldValue) TestUtils.getListElement(processListView, 0)).getInstance();
        var successionListView = (InstanceFieldValue) reloadedRoutingView.getFieldValue(TestUtils.getFieldIdByCode(routingViewType, "successions"));

        doInTransactionWithoutResult(() -> instanceManager.update(
                InstanceDTO.createClassInstance(
                        reloadedRoutingView.id(),
                        TypeExpressions.getClassType(routingViewType.id()),
                        List.of(
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(routingViewType, "name"),
                                        PrimitiveFieldValue.createString("routing001")
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(routingViewType, "product"),
                                        ReferenceFieldValue.create(material)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(routingViewType, "unit"),
                                        ReferenceFieldValue.create(unit)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(routingViewType, "processes"),
                                        new ListFieldValue(
                                                processListView.getInstance().id(),
                                                true,
                                                List.of(
                                                        InstanceFieldValue.of(
                                                                itemView
                                                        ),
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TmpId.random().toString(),
                                                                        TypeExpressions.getClassType(routingProcessViewType.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "processCode"),
                                                                                        PrimitiveFieldValue.createString("process2")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "processDescription"),
                                                                                        PrimitiveFieldValue.createString("工序2")
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "workCenter"),
                                                                                        ReferenceFieldValue.create(workCenter)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "sequence"),
                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "process"),
                                                                                        ReferenceFieldValue.create(process)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(routingProcessViewType, "items"),
                                                                                        new ListFieldValue(
                                                                                                null,
                                                                                                true,
                                                                                                List.of()
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(routingViewType, "successions"),
                                        new ListFieldValue(
                                                successionListView.getInstance().id(),
                                                true,
                                                List.of()
                                        )
                                )
                        )
                )
        ));
        // assert that the update is successful
        var reloadedRoutingView2 = instanceManager.getDefaultView(routing.id()).instance();
        var itemListView2 = (InstanceFieldValue) reloadedRoutingView2.getFieldValue(TestUtils.getFieldIdByCode(routingViewType, "processes"));
        var itemView2 = ((InstanceFieldValue) TestUtils.getListElement(itemListView2, 0)).getInstance();
        var itemView3 = ((InstanceFieldValue) TestUtils.getListElement(itemListView2, 1)).getInstance();
        Assert.assertEquals(2, ((ListInstanceParam) itemListView2.getInstance().param()).elements().size());
        Assert.assertEquals("process1", itemView2.getFieldValue(TestUtils.getFieldIdByCode(routingProcessViewType, "processCode")).getDisplayValue());
        Assert.assertEquals("process2", itemView3.getFieldValue(TestUtils.getFieldIdByCode(routingProcessViewType, "processCode")).getDisplayValue());

        return new RoutingObjects(routing, routingProcess);
    }

    private void processBOM(InstanceDTO material, InstanceDTO unit, InstanceDTO routing, InstanceDTO routingProcess,
                            TypeDTO feedTypeType, TypeDTO pickMethodType, TypeDTO generalStateType, TypeDTO qualityInspectionStateType) {
        var bomKlass = getClassTypeByCode("tech.metavm.manufacturing.production.BOM");
        var bomDefaultMapping = TestUtils.getDefaultMapping(bomKlass);
        var bomViewKlass = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(bomDefaultMapping.targetType()), false)).type();

        var componentMaterialType = getClassTypeByCode("tech.metavm.manufacturing.production.ComponentMaterial");
        var componentMaterialViewType = typeManager.getType(new GetTypeRequest(TestUtils.getDefaultViewKlassId(componentMaterialType), false)).type();

        var componentMaterialItemType = getClassTypeByCode("tech.metavm.manufacturing.production.ComponentMaterialItem");
        var componentMaterialItemViewType = typeManager.getType(new GetTypeRequest(TestUtils.getDefaultViewKlassId(componentMaterialItemType), false)).type();

        var directFeedType = TestUtils.getEnumConstantByName(feedTypeType, "直接投料");
        var onDemandPickMethod = TestUtils.getEnumConstantByName(pickMethodType, "按需领料");

        var enabledGeneralState = TestUtils.getEnumConstantByName(generalStateType, "启用中");
        var qualifiedInspectionState = TestUtils.getEnumConstantByName(qualityInspectionStateType, "合格");

        var bomView = doInTransaction(() -> instanceManager.create(
                InstanceDTO.createClassInstance(
                        null,
                        TypeExpressions.getClassType(bomViewKlass.id()),
                        new ObjectMappingRefDTO(
                                TypeExpressions.getClassType(bomKlass.id()),
                                bomDefaultMapping.id()
                        ),
                        List.of(
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "product"),
                                        ReferenceFieldValue.create(material)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "unit"),
                                        ReferenceFieldValue.create(unit)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "routing"),
                                        ReferenceFieldValue.create(routing)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "reportingProcess"),
                                        ReferenceFieldValue.create(routingProcess)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "state"),
                                        ReferenceFieldValue.create(enabledGeneralState)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "inbound"),
                                        PrimitiveFieldValue.createBoolean(true)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "autoInbound"),
                                        PrimitiveFieldValue.createBoolean(true)
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "components"),
                                        new ListFieldValue(
                                                null,
                                                true,
                                                List.of(
                                                        // create a ComponentMaterial
                                                        InstanceFieldValue.of(
                                                                InstanceDTO.createClassInstance(
                                                                        TypeExpressions.getClassType(componentMaterialViewType.id()),
                                                                        List.of(
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "sequence"),
                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "material"),
                                                                                        ReferenceFieldValue.create(material)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "unit"),
                                                                                        ReferenceFieldValue.create(unit)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "numerator"),
                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "denominator"),
                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "attritionRate"),
                                                                                        PrimitiveFieldValue.createDouble(0.0)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "version"),
                                                                                        PrimitiveFieldValue.createNull()
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "pickMethod"),
                                                                                        ReferenceFieldValue.create(onDemandPickMethod)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "routingSpecified"),
                                                                                        PrimitiveFieldValue.createBoolean(false)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "process"),
                                                                                        ReferenceFieldValue.create(routingProcess)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "qualityInspectionState"),
                                                                                        ReferenceFieldValue.create(qualifiedInspectionState)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "feedType"),
                                                                                        ReferenceFieldValue.create(directFeedType)
                                                                                ),
                                                                                InstanceFieldDTO.create(
                                                                                        TestUtils.getFieldIdByCode(componentMaterialViewType, "items"),
                                                                                        new ListFieldValue(
                                                                                                null,
                                                                                                true,
                                                                                                List.of(
                                                                                                        // create a ComponentMaterialItem
                                                                                                        InstanceFieldValue.of(
                                                                                                                InstanceDTO.createClassInstance(
                                                                                                                        TypeExpressions.getClassType(componentMaterialItemViewType.id()),
                                                                                                                        List.of(
                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                        TestUtils.getFieldIdByCode(componentMaterialItemViewType, "sequence"),
                                                                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                                                                ),
                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                        TestUtils.getFieldIdByCode(componentMaterialItemViewType, "numerator"),
                                                                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                                                                ),
                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                        TestUtils.getFieldIdByCode(componentMaterialItemViewType, "denominator"),
                                                                                                                                        PrimitiveFieldValue.createLong(1L)
                                                                                                                                ),
                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                        TestUtils.getFieldIdByCode(componentMaterialItemViewType, "process"),
                                                                                                                                        ReferenceFieldValue.create(routingProcess)
                                                                                                                                ),
                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                        TestUtils.getFieldIdByCode(componentMaterialItemViewType, "qualityInspectionState"),
                                                                                                                                        ReferenceFieldValue.create(qualifiedInspectionState)
                                                                                                                                ),
                                                                                                                                InstanceFieldDTO.create(
                                                                                                                                        TestUtils.getFieldIdByCode(componentMaterialItemViewType, "feedType"),
                                                                                                                                        ReferenceFieldValue.create(directFeedType)
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
                                                )
                                        )
                                ),
                                InstanceFieldDTO.create(
                                        TestUtils.getFieldIdByCode(bomViewKlass, "secondaryOutputs"),
                                        new ListFieldValue(
                                                null,
                                                true,
                                                List.of()
                                        )
                                )
                        )
                )
        ));

        // create production order
        var bomId = ((DefaultViewId) Id.parse(bomView)).getSourceId().toString();
        var createProductionOrderMethodId = TestUtils.getMethodRefByCode(bomKlass, "createProductionOrder");
        long startTime = System.currentTimeMillis();
        var productionOrder = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                createProductionOrderMethodId,
                bomId,
                List.of(
                        PrimitiveFieldValue.createTime(startTime),
                        PrimitiveFieldValue.createTime(startTime + 3 * 24 * 60 * 60 * 1000),
                        PrimitiveFieldValue.createLong(10)
                )
        )));
        var productionOrderType = getClassTypeByCode("tech.metavm.manufacturing.production.ProductionOrder");
        var orderStartTimeFieldId = TestUtils.getFieldIdByCode(productionOrderType, "plannedStartTime");
        var orderIngredientsFieldId = TestUtils.getFieldIdByCode(productionOrderType, "ingredients");
        var loadedStartTime = (long) ((PrimitiveFieldValue) productionOrder.getFieldValue(orderStartTimeFieldId)).getValue();
        var ingredients = ((InstanceFieldValue) productionOrder.getFieldValue(orderIngredientsFieldId)).getInstance();
        Assert.assertEquals(startTime, loadedStartTime);
        Assert.assertEquals(1, ingredients.getListSize());
    }

}
