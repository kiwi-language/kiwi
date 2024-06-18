package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.core.DefaultViewId;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.TypeExpressions;
import org.metavm.object.type.rest.dto.GetTypeRequest;
import org.metavm.object.type.rest.dto.TypeDTO;
import org.metavm.object.view.rest.dto.DirectMappingKey;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.TestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.metavm.util.TestUtils.doInTransaction;
import static org.metavm.util.TestUtils.doInTransactionWithoutResult;

public class ManufacturingCompileTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/manufacturing";

    public void test() {
        compileTwice(SOURCE_ROOT);
//        compile(SOURCE_ROOT);
        submit(() -> {
            var profiler = ContextUtil.getProfiler();
            try (var ignored = profiler.enter("submit")) {
                var roundingRuleType = getClassTypeByCode("org.metavm.manufacturing.material.RoundingRule");
                var roundHalfUp = TestUtils.getEnumConstantByName(roundingRuleType, "ROUND_HALF_UP");
                var unitType = getClassTypeByCode("org.metavm.manufacturing.material.Unit");
                var unitId = doInTransaction(() -> apiClient.newInstance(
                        unitType.getCodeRequired(),
                        Arrays.asList("meter", "meter", roundHalfUp.getIdRequired(), 2, null)
                ));
                var materialKindType = getClassTypeByCode("org.metavm.manufacturing.material.MaterialKind");
                var normal = TestUtils.getEnumConstantByName(materialKindType, "NORMAL");

                var timeUnitType = getClassTypeByCode("org.metavm.manufacturing.material.TimeUnit");
                var year = TestUtils.getEnumConstantByName(timeUnitType, "YEAR");

                // create a material
                var materialType = getClassTypeByCode("org.metavm.manufacturing.material.Material");
                var materialId = doInTransaction(() -> apiClient.newInstance(
                        materialType.getCodeRequired(),
                        Arrays.asList("sheet metal", "sheet metal", normal.getIdRequired(), unitId, 1, year.getIdRequired())
                ));
                // get QualityInspectionState type
                var qualityInspectionStateType = getClassTypeByCode("org.metavm.manufacturing.material.QualityInspectionState");
                // get QualityInspectionState.QUALIFIED constant
                var qualified = TestUtils.getEnumConstantByName(qualityInspectionStateType, "QUALIFIED");
                // invoke material.setFeedQualityInspectionStates with a list containing the QUALIFIED constant
                doInTransaction(() -> apiClient.callMethod(
                        materialId, "setFeedQualityInspectionStates",
                        List.of(
                                List.of(qualified.getIdRequired())
                        )
                ));
                // reload the material object
                var material = instanceManager.get(materialId, 1).instance();
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
                        new DefaultViewId(false, new DirectMappingKey(Id.parse(materialDefaultMapping.id())), Id.parse(materialId)).toString(),
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
                var inventoryAttributesType = getClassTypeByCode("org.metavm.manufacturing.material.InventoryAttributes");
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
                var utilsType = getClassTypeByCode("org.metavm.manufacturing.utils.Utils");
                Assert.assertEquals(0, utilsType.getClassParam().errors().size());

                var storageObjects = createPosition();
                var unit = instanceManager.get(unitId, 2).instance();
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
                            getClassTypeByCode("org.metavm.manufacturing.production.FeedType"),
                            getClassTypeByCode("org.metavm.manufacturing.production.PickMethod"),
                            getClassTypeByCode("org.metavm.manufacturing.GeneralState"),
                            qualityInspectionStateType
                    );
                }
            }
            System.out.println(profiler.finish(false, true).output());
        });
    }

    private StorageObjects createPosition() {
        var qcWarehouse = "org.metavm.manufacturing.storage.Warehouse";
        var warehouseId = doInTransaction(() -> apiClient.newInstance(
                qcWarehouse,
                List.of("warehouse1", "warehouse1")
        ));
        var qcArea = "org.metavm.manufacturing.storage.Area";
        var areaId = doInTransaction(() -> apiClient.newInstance(
                qcArea,
                Arrays.asList("area1", "area1", warehouseId, null)
        ));
        var qcPosition = "org.metavm.manufacturing.storage.Position";
        var positionId = doInTransaction(() -> apiClient.newInstance(
                qcPosition,
                List.of("position1", "position1", areaId)
        ));
        var position2Id = doInTransaction(() -> apiClient.newInstance(
                qcPosition,
                List.of("position2", "position2", areaId)
        ));
        var warehouse = instanceManager.get(warehouseId, 2).instance();
        var area = instanceManager.get(areaId, 2).instance();
        var position = instanceManager.get(positionId, 2).instance();
        var position2 = instanceManager.get(position2Id, 2).instance();
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
        var inventoryType = getClassTypeByCode("org.metavm.manufacturing.storage.Inventory");
        // get inventoryBizState type
        var inventoryBizStateType = getClassTypeByCode("org.metavm.manufacturing.storage.InventoryBizState");
        // get InventoryBizState.INITIAL constant
        var initialBizState = TestUtils.getEnumConstantByName(inventoryBizStateType, "INITIAL");
        // create an inventory object
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryType.getCodeRequired(),
                Arrays.asList(
                        material.getIdRequired(),
                        position.getIdRequired(),
                        qualifiedInspectionState.getIdRequired(),
                        initialBizState.getIdRequired(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        100
                )
        ));
        // query the inventory object by condition
        var queryResp = instanceManager.query(
                new InstanceQueryDTO(
                        TypeExpressions.getClassType(inventoryType.id()),
                        null,
                        null,
                        String.format("material = $$%s and position = $$%s and qualityInspectionState = $$%s and bizState = $$%s",
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
        Assert.assertEquals(inventoryId, queriedInventory.id());

        var inventoryOpType = getClassTypeByCode("org.metavm.manufacturing.storage.InventoryOp");
        var adjustment = TestUtils.getEnumConstantByName(inventoryOpType, "ADJUSTMENT");

        // decrease the inventory by 100 and asserts that the inventory is removed
        doInTransaction(() -> apiClient.callMethod(
                Objects.requireNonNull(inventoryType.code()),
                "decreaseQuantity",
                Arrays.asList(
                        material.getIdRequired(),
                        position.getIdRequired(),
                        qualifiedInspectionState.getIdRequired(),
                        initialBizState.getIdRequired(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        100L,
                        unit.getIdRequired(),
                        adjustment.getIdRequired()
                )
        ));
        try {
            instanceManager.get(inventoryId, 1);
            Assert.fail("Inventory should be removed");
        } catch (BusinessException e) {
            Assert.assertEquals(String.format("Object '%s' does not exist", inventoryId), e.getMessage());
        }
    }

    private void processInbound(StorageObjects storageObjects, InstanceDTO material, InstanceDTO unit) {
        // get InboundBizType type
        var inboundBizTypeType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundBizType");
        // get InboundBizType.PURCHASE constant
        var purchase = TestUtils.getEnumConstantByName(inboundBizTypeType, "PURCHASE");

        // get InboundOrder type
        var inboundOrderType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundOrder");
        // create an inbound order
        var inboundOrderId = doInTransaction(() -> apiClient.newInstance(
                inboundOrderType.getCodeRequired(),
                Arrays.asList(
                        "inboundOrder1",
                        purchase.getIdRequired(),
                        storageObjects.warehouse.getIdRequired(),
                        null
                )
        ));
        var inboundOrderItemType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundOrderItem");
        var inboundOrderItemId = doInTransaction(() -> apiClient.newInstance(
                inboundOrderItemType.getCodeRequired(),
                Arrays.asList(
                        inboundOrderId,
                        material.getIdRequired(),
                        storageObjects.position.getIdRequired(),
                        100,
                        unit.getIdRequired(),
                        null
                )
        ));

        // get the InboundRequest type
        var inboundRequestType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundRequest");
        // assert that the InboundRequest type is abstract
        Assert.assertTrue(inboundRequestType.getClassParam().isAbstract());
        // get the ByAmountInboundRequest type
        var qcByBoundInboundRequestType = "org.metavm.manufacturing.storage.ByAmountInboundRequest";
        // invoke InboundOrderItem.inbound with the inboundRequest object
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                inboundOrderItemId,
                "inbound",
                List.of(
                        Map.ofEntries(
                                Map.entry(ApiService.KEY_CLASS, qcByBoundInboundRequestType),
                                Map.entry("bizType", purchase.getIdRequired()),
                                Map.entry("position", storageObjects.position.getIdRequired()),
                                Map.entry("material", material.getIdRequired()),
                                Map.entry("unit", unit.getIdRequired()),
                                Map.entry("amount", 100L)
                        )
                )
        ));
        // reload the inbound order item and check the actualQuantity field
        var reloadedInboundOrderItem = instanceManager.get(inboundOrderItemId, 1).instance();
        var actualQuantity = reloadedInboundOrderItem.getFieldValue(TestUtils.getFieldIdByCode(inboundOrderItemType, "actualQuantity"));
        Assert.assertEquals(100L, ((PrimitiveFieldValue) actualQuantity).getValue());

        // inbound by spec
        var qcBySpecInboundRequestType = "org.metavm.manufacturing.storage.BySpecInboundRequest";
        // invoke InboundOrderItem.inbound with the inboundRequest object
        doInTransaction(() -> apiClient.callMethod(
                inboundOrderItemId,
                "inbound",
                List.of(
                        Map.ofEntries(
                                Map.entry(ApiService.KEY_CLASS, qcBySpecInboundRequestType),
                                Map.entry("bizType", purchase.getIdRequired()),
                                Map.entry("position", storageObjects.position.getIdRequired()),
                                Map.entry("material", material.getIdRequired()),
                                Map.entry("unit", unit.getIdRequired()),
                                Map.entry("bySpecItems", List.of(
                                        Map.ofEntries(
                                                Map.entry("qrCodeAmount", 2),
                                                Map.entry("inboundAmount", 10)
                                        ),
                                        Map.ofEntries(
                                                Map.entry("qrCodeAmount", 3),
                                                Map.entry("inboundAmount", 5)
                                        )
                                ))
                        )
                )
        ));

        reloadedInboundOrderItem = instanceManager.get(inboundOrderItemId, 1).instance();
        actualQuantity = reloadedInboundOrderItem.getFieldValue(TestUtils.getFieldIdByCode(inboundOrderItemType, "actualQuantity"));
        Assert.assertEquals(135L, ((PrimitiveFieldValue) actualQuantity).getValue());
    }


    private void processTransfer(StorageObjects storageObjects, InstanceDTO material, InstanceDTO unit) {
        // get TransferBizType type
        var transferBizTypeType = getClassTypeByCode("org.metavm.manufacturing.storage.TransferBizType");
        // get TransferBizType.STORAGE constant
        var storage = TestUtils.getEnumConstantByName(transferBizTypeType, "STORAGE");

        // get transfer order type
        var qcTransferOrder = "org.metavm.manufacturing.storage.TransferOrder";
        // create a transfer order
        var transferOrderId = doInTransaction(() -> apiClient.newInstance(
                qcTransferOrder,
                List.of("transferOrder1", storage.getIdRequired(), storageObjects.warehouse.getIdRequired(), storageObjects.warehouse.getIdRequired())
        ));
        // create a transfer order item
        var qcTransferOrderItem = "org.metavm.manufacturing.storage.TransferOrderItem";
        var transferOrderItemId = doInTransaction(() -> apiClient.newInstance(
                qcTransferOrderItem,
                Arrays.asList(transferOrderId, material.getIdRequired(), 100, unit.getIdRequired(), null, null)
        ));
        // create an inventory
        var inventoryType = getClassTypeByCode("org.metavm.manufacturing.storage.Inventory");
        var qualityInspectionStateType = getClassTypeByCode("org.metavm.manufacturing.material.QualityInspectionState");
        var qualifiedInspectionState = TestUtils.getEnumConstantByName(qualityInspectionStateType, "QUALIFIED");
        var InventoryBizStateType = getClassTypeByCode("org.metavm.manufacturing.storage.InventoryBizState");
        var initialBizState = TestUtils.getEnumConstantByName(InventoryBizStateType, "INITIAL");
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryType.getCodeRequired(),
                Arrays.asList(
                        material.getIdRequired(),
                        storageObjects.position.getIdRequired(),
                        qualifiedInspectionState.getIdRequired(),
                        initialBizState.getIdRequired(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        100
                )
        ));
        // invoke TransferOrderItem.transfer with storageObjects.position2 as the target position
        doInTransaction(() -> apiClient.callMethod(
                transferOrderId,
                "transfer",
                List.of(
                        Map.of(
                                "to", storageObjects.position2.getIdRequired(),
                                "items", List.of(
                                        Map.of(
                                                "transferOrderItem", transferOrderItemId,
                                                "subItems", List.of(
                                                        Map.of(
                                                                "inventory", inventoryId,
                                                                "amount", 20,
                                                                "unit", unit.getIdRequired()
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));
        // assert that the transfer has taken place
        var reloadedInventory = instanceManager.get(inventoryId, 1).instance();
        assertEquals(PrimitiveFieldValue.createLong(80L), reloadedInventory.getFieldValue(TestUtils.getFieldIdByCode(inventoryType, "quantity")));
    }

    private record RoutingObjects(
            InstanceDTO routing,
            InstanceDTO routingProcess
    ) {

    }

    private RoutingObjects processRouting(InstanceDTO material, InstanceDTO unit) {
        var routingKlass = getClassTypeByCode("org.metavm.manufacturing.production.Routing");
        var routingViewKlass = TestUtils.getViewKlass(routingKlass, typeManager);
        var qcWorkCenter = "org.metavm.manufacturing.production.WorkCenter";
        var workCenterId = doInTransaction(() -> apiClient.newInstance(qcWorkCenter, List.of()));
        var qcProcess = "org.metavm.manufacturing.production.Process";
        var processId = doInTransaction(() -> apiClient.newInstance(qcProcess, List.of("process1")));
        var routingId = (String) doInTransaction(() -> apiClient.saveInstance(
                routingKlass.getCodeRequired(),
                Map.of(
                        "name", "routing001",
                        "product", material.getIdRequired(),
                        "unit", unit.getIdRequired(),
                        "processes", List.of(
                                Map.of(
                                        "processCode", "process1",
                                        "processDescription", "process1",
                                        "sequence", 1,
                                        "process", processId,
                                        "workCenter", workCenterId,
                                        "items", List.of()
                                )
                        ),
                        "successions", List.of()
                )
        ));
        Assert.assertNotNull(routingId);
        // reload routing
//        var routingId = TestUtils.getSourceId(routingViewId);
//        var reloadedRoutingView = instanceManager.getDefaultView(routingId).instance();
//        var viewId = (DefaultViewId) Id.parse(reloadedRoutingView.id());
//        Assert.assertEquals(viewId.getSourceId(), Id.parse(routingId));
        var routing = instanceManager.get(routingId, 2).instance();
        var routingProcess = routing.getInstance("processes").getElementInstance(0);
//        var processListView = reloadedRoutingView.getInstance("processes");
//        var itemView = processListView.getElementInstance(0);
//        var successionListView = reloadedRoutingView.getInstance("successions");
        doInTransactionWithoutResult(() -> apiClient.saveInstance(
                routingKlass.getCodeRequired(),
                Map.of(
                        ApiService.KEY_ID, routing.getIdRequired(),
                        "name", "routing001",
                        "product", material.getIdRequired(),
                        "unit", unit.getIdRequired(),
                        "processes", List.of(
                                routingProcess.toJson(),
                                Map.of(
                                        "processCode", "process2",
                                        "processDescription", "process2",
                                        "sequence", 1,
                                        "workCenter", workCenterId,
                                        "process", processId,
                                        "items", List.of()
                                )
                        ),
                        "successions", List.of()
                )
        ));
        // assert that the update is successful
        var reloadedRoutingView2 = instanceManager.getDefaultView(routing.id()).instance();
        var itemListView2 = (InstanceFieldValue) reloadedRoutingView2.getFieldValue(TestUtils.getFieldIdByCode(routingViewKlass, "processes"));
        var itemView2 = ((InstanceFieldValue) TestUtils.getListElement(itemListView2, 0)).getInstance();
        var itemView3 = ((InstanceFieldValue) TestUtils.getListElement(itemListView2, 1)).getInstance();
        Assert.assertEquals(2, ((ListInstanceParam) itemListView2.getInstance().param()).elements().size());
        Assert.assertEquals("process1", itemView2.getPrimitiveValue("processCode"));
        Assert.assertEquals("process2", itemView3.getPrimitiveValue("processCode"));

        return new RoutingObjects(routing, routingProcess);
    }

    private void processBOM(InstanceDTO material, InstanceDTO unit, InstanceDTO routing, InstanceDTO routingProcess,
                            TypeDTO feedTypeType, TypeDTO pickMethodType, TypeDTO generalStateType, TypeDTO qualityInspectionStateType) {
        var bomKlass = getClassTypeByCode("org.metavm.manufacturing.production.BOM");
//        var bomViewKlass = typeManager.getType(new GetTypeRequest(TestUtils.getDefaultViewKlassId(bomKlass), false)).type();
        var directFeedType = TestUtils.getEnumConstantByName(feedTypeType, "DIRECT");
        var onDemandPickMethod = TestUtils.getEnumConstantByName(pickMethodType, "ON_DEMAND");
        var enabledGeneralState = TestUtils.getEnumConstantByName(generalStateType, "ENABLED");
        var qualifiedInspectionState = TestUtils.getEnumConstantByName(qualityInspectionStateType, "QUALIFIED");
        var bomId = doInTransaction(() -> apiClient.saveInstance(
                bomKlass.getCodeRequired(),
                Map.of(
                        "product", material.getIdRequired(),
                        "unit", unit.getIdRequired(),
                        "routing", routing.getIdRequired(),
                        "reportingProcess", routingProcess.getIdRequired(),
                        "state", enabledGeneralState.getIdRequired(),
                        "inbound", true,
                        "autoInbound", true,
                        "secondaryOutputs", List.of(),
                        "components", List.of(
                                Map.ofEntries(
                                        Map.entry("sequence", 1),
                                        Map.entry("material", material.getIdRequired()),
                                        Map.entry("unit", unit.getIdRequired()),
                                        Map.entry("numerator", 1),
                                        Map.entry("denominator", 1),
                                        Map.entry("attritionRate", 0.0),
                                        Map.entry("pickMethod", onDemandPickMethod.getIdRequired()),
                                        Map.entry("routingSpecified", false),
                                        Map.entry("process", routingProcess.getIdRequired()),
                                        Map.entry("qualityInspectionState", qualifiedInspectionState.getIdRequired()),
                                        Map.entry("feedType", directFeedType.getIdRequired()),
                                        Map.entry("items", List.of(
                                                Map.of(
                                                        "sequence", 1,
                                                        "numerator", 1,
                                                        "denominator", 1,
                                                        "process", routingProcess.getIdRequired(),
                                                        "qualityInspectionState", qualifiedInspectionState.getIdRequired(),
                                                        "feedType", directFeedType.getIdRequired()
                                                )
                                        ))
                                )
                        )
                )
        ));
//        var bomId = TestUtils.getSourceId(bomViewId);
        // create production order
        long startTime = System.currentTimeMillis();
        var productionOrderId = (String) doInTransaction(() -> apiClient.callMethod(
                bomId,
                "createProductionOrder",
                List.of(startTime, startTime + 3 * 24 * 60 * 60 * 1000, 10))
        );
        var productionOrder = instanceManager.get(productionOrderId, 2).instance();
        var productionOrderType = getClassTypeByCode("org.metavm.manufacturing.production.ProductionOrder");
        var orderStartTimeFieldId = TestUtils.getFieldIdByCode(productionOrderType, "plannedStartTime");
        var orderIngredientsFieldId = TestUtils.getFieldIdByCode(productionOrderType, "ingredients");
        var loadedStartTime = (long) ((PrimitiveFieldValue) productionOrder.getFieldValue(orderStartTimeFieldId)).getValue();
        var ingredients = ((InstanceFieldValue) productionOrder.getFieldValue(orderIngredientsFieldId)).getInstance();
        Assert.assertEquals(startTime, loadedStartTime);
        Assert.assertEquals(1, ingredients.getListSize());
    }

}
