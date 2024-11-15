package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.TypeExpressions;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.util.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.metavm.util.TestUtils.doInTransaction;
import static org.metavm.util.TestUtils.doInTransactionWithoutResult;

public class ManufacturingCompilingTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/manufacturing";

    public static final String USER_NAME = "leen";
    public static final String PASSWORD = "123456";

    @Override
    protected void setUp() throws ExecutionException, InterruptedException {
        super.setUp();
        Constants.SESSION_TIMEOUT = 3000;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Constants.SESSION_TIMEOUT = Constants.DEFAULT_SESSION_TIMEOUT;
    }

    public void test() {
        compileTwice(SOURCE_ROOT);
//        compile(SOURCE_ROOT);
        submit(() -> {
            var profiler = ContextUtil.getProfiler();
            try (var ignored = profiler.enter("submit")) {
                signup();
                login();
                var roundingRuleType = getClassTypeByCode("org.metavm.manufacturing.material.RoundingRule");
                var roundHalfUp = typeManager.getEnumConstant(roundingRuleType.id(), "ROUND_HALF_UP");
                var unitType = getClassTypeByCode("org.metavm.manufacturing.material.Unit");
                var unitId = doInTransaction(() -> apiClient.newInstance(
                        unitType.qualifiedName(),
                        Arrays.asList("meter", "meter", roundHalfUp.getIdNotNull(), 2, null)
                ));
                var materialKindType = getClassTypeByCode("org.metavm.manufacturing.material.MaterialKind");
                var normal = typeManager.getEnumConstant(materialKindType.id(), "NORMAL");

                var timeUnitType = getClassTypeByCode("org.metavm.manufacturing.material.TimeUnit");
                var year = typeManager.getEnumConstant(timeUnitType.id(), "YEAR");

                // create a material
                var materialType = getClassTypeByCode("org.metavm.manufacturing.material.Material");
//                var materialId = doInTransaction(() -> apiClient.newInstance(
//                        materialType.getCodeRequired(),
//                        Arrays.asList("sheet metal",
//                                "sheet metal",
//                                normal.getIdRequired(),
//                                unitId,
//                                1,
//                                year.getIdRequired())
//                ));
                var materialId = (String) doInTransaction(() -> apiClient.callMethod(
                        "materialService",
                        "save",
                        List.of(
                                Map.of(
                                        "code", "sheet metal",
                                        "name", "sheet metal",
                                        "kind", normal.getIdNotNull(),
                                        "unit", unitId,
                                        "storageValidPeriod", 1,
                                        "storageValidPeriodUnit", year.getIdNotNull()
                                )
                        )
                ));
                // get QualityInspectionState type
                var qualityInspectionStateType = getClassTypeByCode("org.metavm.manufacturing.material.QualityInspectionState");
                // get QualityInspectionState.QUALIFIED constant
                var qualified = typeManager.getEnumConstant(qualityInspectionStateType.id(), "QUALIFIED");
                // invoke material.setFeedQualityInspectionStates with a list containing the QUALIFIED constant
                doInTransaction(() -> apiClient.callMethod(
                        materialId, "setFeedQualityInspectionStates",
                        List.of(
                                List.of(qualified.getIdNotNull())
                        )
                ));
                // reload the material object
                var material = instanceManager.get(materialId, 1).instance();
                // assert that the feedQualityInspectionStates field of the material object contains the QUALIFIED constant
                var feedQualityInspectionStates = material.getFieldValue(TestUtils.getFieldIdByName(materialType, "feedQualityInspectionStates"));
                Assert.assertTrue(feedQualityInspectionStates instanceof InstanceFieldValue);
                var feedQualityInspectionStatesList = ((InstanceFieldValue) feedQualityInspectionStates).getInstance().getElements();
                Assert.assertEquals(1, feedQualityInspectionStatesList.size());
                Assert.assertEquals(qualified.id(), ((ReferenceFieldValue) feedQualityInspectionStatesList.get(0)).getId());


                // load material view object
                var loadedMaterial = instanceManager.get(materialId, 1).instance();
                // save material view
                doInTransactionWithoutResult(() -> instanceManager.update(loadedMaterial));
                // reload the material view object
                var reloadedMaterialView = instanceManager.get(loadedMaterial.id(), 1).instance();
                // check the feedQualityInspectionStates field and assert that it didn't change
                var reloadedFeedQualityInspectionStates = reloadedMaterialView.getFieldValue(TestUtils.getFieldIdByName(materialType, "feedQualityInspectionStates"));
                Assert.assertTrue(reloadedFeedQualityInspectionStates instanceof InstanceFieldValue);
                var reloadedFeedQualityInspectionStatesList = ((InstanceFieldValue) reloadedFeedQualityInspectionStates).getInstance().getElements();
                Assert.assertEquals(1, reloadedFeedQualityInspectionStatesList.size());
                Assert.assertEquals(qualified.id(), ((ReferenceFieldValue) reloadedFeedQualityInspectionStatesList.get(0)).getId());

                // get Utils type
                var utilsType = getClassTypeByCode("org.metavm.manufacturing.utils.Utils");
                Assert.assertEquals(0, utilsType.errors().size());

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

    private void signup() {
        TestUtils.doInTransaction(() -> apiClient.callMethod("userService", "signup",
                List.of(USER_NAME, PASSWORD)));
    }

    private void login() {
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                "userService", "login",
                List.of(USER_NAME, PASSWORD)
        ));
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
        var initialBizState = typeManager.getEnumConstant(inventoryBizStateType.id(), "INITIAL");
        // create an inventory object
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryType.qualifiedName(),
                Arrays.asList(
                        material.getIdNotNull(),
                        position.getIdNotNull(),
                        qualifiedInspectionState.getIdNotNull(),
                        initialBizState.getIdNotNull(),
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
        DebugEnv.stringId = inventoryId;
        waitForAllTasksDone();
        // query the inventory object by condition
        var queryResp = instanceManager.query(
                new InstanceQueryDTO(
                        TypeExpressions.getClassType(inventoryType.id()),
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
        var adjustment = typeManager.getEnumConstant(inventoryOpType.id(), "ADJUSTMENT");

        // decrease the inventory by 100 and asserts that the inventory is removed
        doInTransaction(() -> apiClient.callMethod(
                inventoryType.qualifiedName(),
                "decreaseQuantity",
                Arrays.asList(
                        material.getIdNotNull(),
                        position.getIdNotNull(),
                        qualifiedInspectionState.getIdNotNull(),
                        initialBizState.getIdNotNull(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        100L,
                        unit.getIdNotNull(),
                        adjustment.getIdNotNull()
                )
        ));
        try {
            instanceManager.get(inventoryId, 1);
            Assert.fail("Inventory should be removed");
        } catch (BusinessException e) {
            Assert.assertEquals(String.format("Object '%s' not found", inventoryId), e.getMessage());
        }
    }

    private void processInbound(StorageObjects storageObjects, InstanceDTO material, InstanceDTO unit) {
        // get InboundBizType type
        var inboundBizTypeType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundBizType");
        // get InboundBizType.PURCHASE constant
        var purchase = typeManager.getEnumConstant(inboundBizTypeType.id(), "PURCHASE");

        // get InboundOrder type
        var inboundOrderType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundOrder");
        // create an inbound order
        var inboundOrderId = doInTransaction(() -> apiClient.newInstance(
                inboundOrderType.qualifiedName(),
                Arrays.asList(
                        "inboundOrder1",
                        purchase.getIdNotNull(),
                        storageObjects.warehouse.getIdNotNull(),
                        null
                )
        ));
        var inboundOrderItemType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundOrderItem");
        var inboundOrderItemId = doInTransaction(() -> apiClient.newInstance(
                inboundOrderItemType.qualifiedName(),
                Arrays.asList(
                        inboundOrderId,
                        material.getIdNotNull(),
                        storageObjects.position.getIdNotNull(),
                        100,
                        unit.getIdNotNull(),
                        null
                )
        ));

        // get the InboundRequest type
        var inboundRequestType = getClassTypeByCode("org.metavm.manufacturing.storage.InboundRequest");
        // assert that the InboundRequest type is abstract
        Assert.assertTrue(inboundRequestType.isAbstract());
        // get the ByAmountInboundRequest type
        var qcByBoundInboundRequestType = "org.metavm.manufacturing.storage.ByAmountInboundRequest";
        // invoke InboundOrderItem.inbound with the inboundRequest object
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                inboundOrderItemId,
                "inbound",
                List.of(
                        Map.ofEntries(
                                Map.entry(ApiService.KEY_CLASS, qcByBoundInboundRequestType),
                                Map.entry("bizType", purchase.getIdNotNull()),
                                Map.entry("position", storageObjects.position.getIdNotNull()),
                                Map.entry("material", material.getIdNotNull()),
                                Map.entry("unit", unit.getIdNotNull()),
                                Map.entry("amount", 100L)
                        )
                )
        ));
        // reload the inbound order item and check the actualQuantity field
        var reloadedInboundOrderItem = instanceManager.get(inboundOrderItemId, 1).instance();
        var actualQuantity = reloadedInboundOrderItem.getFieldValue(TestUtils.getFieldIdByName(inboundOrderItemType, "actualQuantity"));
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
                                Map.entry("bizType", purchase.getIdNotNull()),
                                Map.entry("position", storageObjects.position.getIdNotNull()),
                                Map.entry("material", material.getIdNotNull()),
                                Map.entry("unit", unit.getIdNotNull()),
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
        actualQuantity = reloadedInboundOrderItem.getFieldValue(TestUtils.getFieldIdByName(inboundOrderItemType, "actualQuantity"));
        Assert.assertEquals(135L, ((PrimitiveFieldValue) actualQuantity).getValue());
    }


    private void processTransfer(StorageObjects storageObjects, InstanceDTO material, InstanceDTO unit) {
        // get TransferBizType type
        var transferBizTypeType = getClassTypeByCode("org.metavm.manufacturing.storage.TransferBizType");
        // get TransferBizType.STORAGE constant
        var storage = typeManager.getEnumConstant(transferBizTypeType.id(), "STORAGE");

        // get transfer order type
        var qcTransferOrder = "org.metavm.manufacturing.storage.TransferOrder";
        // create a transfer order
        var transferOrderId = doInTransaction(() -> apiClient.newInstance(
                qcTransferOrder,
                List.of("transferOrder1", storage.getIdNotNull(), storageObjects.warehouse.getIdNotNull(), storageObjects.warehouse.getIdNotNull())
        ));
        // create a transfer order item
        var qcTransferOrderItem = "org.metavm.manufacturing.storage.TransferOrderItem";
        var transferOrderItemId = doInTransaction(() -> apiClient.newInstance(
                qcTransferOrderItem,
                Arrays.asList(transferOrderId, material.getIdNotNull(), 100, unit.getIdNotNull(), null, null)
        ));
        // create an inventory
        var inventoryType = getClassTypeByCode("org.metavm.manufacturing.storage.Inventory");
        var qualityInspectionStateType = getClassTypeByCode("org.metavm.manufacturing.material.QualityInspectionState");
        var qualifiedInspectionState = typeManager.getEnumConstant(qualityInspectionStateType.id(), "QUALIFIED");
        var InventoryBizStateType = getClassTypeByCode("org.metavm.manufacturing.storage.InventoryBizState");
        var initialBizState = typeManager.getEnumConstant(InventoryBizStateType.id(), "INITIAL");
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryType.qualifiedName(),
                Arrays.asList(
                        material.getIdNotNull(),
                        storageObjects.position.getIdNotNull(),
                        qualifiedInspectionState.getIdNotNull(),
                        initialBizState.getIdNotNull(),
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
                                "to", storageObjects.position2.getIdNotNull(),
                                "items", List.of(
                                        Map.of(
                                                "transferOrderItem", transferOrderItemId,
                                                "subItems", List.of(
                                                        Map.of(
                                                                "inventory", inventoryId,
                                                                "amount", 20,
                                                                "unit", unit.getIdNotNull()
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));
        // assert that the transfer has taken place
        var reloadedInventory = instanceManager.get(inventoryId, 1).instance();
        assertEquals(PrimitiveFieldValue.createLong(80L), reloadedInventory.getFieldValue(TestUtils.getFieldIdByName(inventoryType, "quantity")));
    }

    private record RoutingObjects(
            InstanceDTO routing,
            InstanceDTO routingProcess
    ) {

    }

    private RoutingObjects processRouting(InstanceDTO material, InstanceDTO unit) {
        var routingKlass = getClassTypeByCode("org.metavm.manufacturing.production.Routing");
        var qcWorkCenter = "org.metavm.manufacturing.production.WorkCenter";
        var workCenterId = doInTransaction(() -> apiClient.newInstance(qcWorkCenter, List.of()));
        var qcProcess = "org.metavm.manufacturing.production.Process";
        var processId = doInTransaction(() -> apiClient.newInstance(qcProcess, List.of("process1")));
        var routingId = (String) doInTransaction(() -> apiClient.saveInstance(
                routingKlass.qualifiedName(),
                Map.of(
                        "name", "routing001",
                        "product", material.getIdNotNull(),
                        "unit", unit.getIdNotNull(),
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
                routingKlass.qualifiedName(),
                Map.of(
                        ApiService.KEY_ID, routing.getIdNotNull(),
                        "name", "routing001",
                        "product", material.getIdNotNull(),
                        "unit", unit.getIdNotNull(),
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
        return new RoutingObjects(routing, routingProcess);
    }

    private void processBOM(InstanceDTO material, InstanceDTO unit, InstanceDTO routing, InstanceDTO routingProcess,
                            KlassDTO feedTypeType, KlassDTO pickMethodType, KlassDTO generalStateType, KlassDTO qualityInspectionStateType) {
        var bomKlass = getClassTypeByCode("org.metavm.manufacturing.production.BOM");
//        var bomViewKlass = typeManager.getType(new GetTypeRequest(TestUtils.getDefaultViewKlassId(bomKlass), false)).type();
        var directFeedType = typeManager.getEnumConstant(feedTypeType.id(), "DIRECT");
        var onDemandPickMethod = typeManager.getEnumConstant(pickMethodType.id(), "ON_DEMAND");
        var enabledGeneralState = typeManager.getEnumConstant(generalStateType.id(), "ENABLED");
        var qualifiedInspectionState = typeManager.getEnumConstant(qualityInspectionStateType.id(), "QUALIFIED");
        var bomId = doInTransaction(() -> apiClient.saveInstance(
                bomKlass.qualifiedName(),
                Map.of(
                        "product", material.getIdNotNull(),
                        "unit", unit.getIdNotNull(),
                        "routing", routing.getIdNotNull(),
                        "reportingProcess", routingProcess.getIdNotNull(),
                        "state", enabledGeneralState.getIdNotNull(),
                        "inbound", true,
                        "autoInbound", true,
                        "secondaryOutputs", List.of(),
                        "components", List.of(
                                Map.ofEntries(
                                        Map.entry("sequence", 1),
                                        Map.entry("material", material.getIdNotNull()),
                                        Map.entry("unit", unit.getIdNotNull()),
                                        Map.entry("numerator", 1),
                                        Map.entry("denominator", 1),
                                        Map.entry("attritionRate", 0.0),
                                        Map.entry("pickMethod", onDemandPickMethod.getIdNotNull()),
                                        Map.entry("routingSpecified", false),
                                        Map.entry("process", routingProcess.getIdNotNull()),
                                        Map.entry("qualityInspectionState", qualifiedInspectionState.getIdNotNull()),
                                        Map.entry("feedType", directFeedType.getIdNotNull()),
                                        Map.entry("items", List.of(
                                                Map.of(
                                                        "sequence", 1,
                                                        "numerator", 1,
                                                        "denominator", 1,
                                                        "process", routingProcess.getIdNotNull(),
                                                        "qualityInspectionState", qualifiedInspectionState.getIdNotNull(),
                                                        "feedType", directFeedType.getIdNotNull()
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
        var orderStartTimeFieldId = TestUtils.getFieldIdByName(productionOrderType, "plannedStartTime");
        var orderIngredientsFieldId = TestUtils.getFieldIdByName(productionOrderType, "ingredients");
        var loadedStartTime = (long) ((PrimitiveFieldValue) productionOrder.getFieldValue(orderStartTimeFieldId)).getValue();
        var ingredients = ((InstanceFieldValue) productionOrder.getFieldValue(orderIngredientsFieldId)).getInstance();
        Assert.assertEquals(startTime, loadedStartTime);
        Assert.assertEquals(1, ingredients.getListSize());
    }

}
