package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.core.ClassInstanceWrap;
import org.metavm.object.instance.rest.InstanceQueryDTO;
import org.metavm.util.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.metavm.util.TestUtils.doInTransaction;
import static org.metavm.util.TestUtils.doInTransactionWithoutResult;

@Slf4j
public class ManufacturingCompilingTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/manufacturing";

    public static final String USER_NAME = "leen";
    public static final String PASSWORD = "123456";
    public static final String qualityInspectionStateKlass = "org.metavm.manufacturing.material.QualityInspectionState";
    public static final String inventoryKlass = "org.metavm.manufacturing.storage.Inventory";

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
                var unitId = doInTransaction(() -> apiClient.newInstance(
                        "org.metavm.manufacturing.material.Unit",
                        Arrays.asList("meter", "meter", "ROUND_HALF_UP", 2, null)
                ));
                var materialId = (String) doInTransaction(() -> apiClient.callMethod(
                        "materialService",
                        "save",
                        List.of(
                                Map.of(
                                        "code", "sheet metal",
                                        "name", "sheet metal",
                                        "kind", "NORMAL",
                                        "unit", unitId,
                                        "storageValidPeriod", 1,
                                        "storageValidPeriodUnit", "YEAR"
                                )
                        )
                ));
                // get QualityInspectionState type
                // get QualityInspectionState.QUALIFIED constant
                // invoke material.setFeedQualityInspectionStates with a list containing the QUALIFIED constant
                callMethod(
                        materialId, "setFeedQualityInspectionStates",
                        List.of(
                                List.of("QUALIFIED")
                        )
                );
                // reload the material object
                var material = getObject(materialId);
                // assert that the feedQualityInspectionStates field of the material object contains the QUALIFIED constant
                var feedQualityInspectionStates = material.getArray("feedQualityInspectionStates");
                Assert.assertEquals(1, feedQualityInspectionStates.size());
                Assert.assertEquals("QUALIFIED", feedQualityInspectionStates.get(0));


                // reload the material view object
                var reloadedMaterial = getObject(materialId);
                // check the feedQualityInspectionStates field and assert that it didn't change
                var reloadedFeedQualityInspectionStates = reloadedMaterial.getArray("feedQualityInspectionStates");
                Assert.assertEquals(1, reloadedFeedQualityInspectionStates.size());
                Assert.assertEquals("QUALIFIED", reloadedFeedQualityInspectionStates.get(0));

                // get Utils type
                var storageObjects = createPosition();
                var unit = getObject(unitId);
                try (var ignored2 = profiler.enter("processInventory")) {
                    processInventory(material, storageObjects.position, unit);
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
                            routingObjects.routingProcess
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
        var warehouse = getObject(warehouseId);
        var area = getObject(areaId);
        var position = getObject(positionId);
        var position2 = getObject(position2Id);
        return new StorageObjects(warehouse, area, position, position2);
    }

    private record StorageObjects(
            ClassInstanceWrap warehouse,
            ClassInstanceWrap area,
            ClassInstanceWrap position,
            ClassInstanceWrap position2
    ) {

    }


    private void processInventory(ClassInstanceWrap material,
                                  ClassInstanceWrap position,
                                  ClassInstanceWrap unit
    ) {
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryKlass,
                Arrays.asList(
                        material.id(),
                        position.id(),
                        "QUALIFIED",
                        "INITIAL",
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
        var qualifiedInspectionStateId = typeManager.getEnumConstantId(qualityInspectionStateKlass, "QUALIFIED");
        var initialBizStateId = typeManager.getEnumConstantId("org.metavm.manufacturing.storage.InventoryBizState", "INITIAL");
        // query the inventory object by condition
        var queryResp = instanceManager.query(
                new InstanceQueryDTO(
                        inventoryKlass,
                        null,
                        String.format("material = $$%s and position = $$%s and qualityInspectionState = $$%s and bizState = $$%s",
                                material.id(), position.id(), qualifiedInspectionStateId, initialBizStateId),
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
        Assert.assertEquals(inventoryId, queriedInventory);

        // decrease the inventory by 100 and asserts that the inventory is removed
        doInTransaction(() -> apiClient.callMethod(
                inventoryKlass,
                "decreaseQuantity",
                Arrays.asList(
                        material.id(),
                        position.id(),
                        "QUALIFIED",
                        "INITIAL",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        100L,
                        unit.id(),
                        "ADJUSTMENT"
                )
        ));
        try {
            getObject(inventoryId);
            Assert.fail("Inventory should be removed");
        } catch (BusinessException e) {
            Assert.assertEquals(String.format("Object '%s' not found", inventoryId), e.getMessage());
        }
    }

    private void processInbound(StorageObjects storageObjects, ClassInstanceWrap material, ClassInstanceWrap unit) {
        var inboundOrderId = doInTransaction(() -> apiClient.newInstance(
                "org.metavm.manufacturing.storage.InboundOrder",
                Arrays.asList(
                        "inboundOrder1",
                        "PURCHASE",
                        storageObjects.warehouse.id(),
                        null
                )
        ));
        var inboundOrderItemId = doInTransaction(() -> apiClient.newInstance(
                "org.metavm.manufacturing.storage.InboundOrderItem",
                Arrays.asList(
                        inboundOrderId,
                        material.id(),
                        storageObjects.position.id(),
                        100,
                        unit.id(),
                        null
                )
        ));

        // get the InboundRequest type
        // get the ByAmountInboundRequest type
        var qcByBoundInboundRequestType = "org.metavm.manufacturing.storage.ByAmountInboundRequest";
        // invoke InboundOrderItem.inbound with the inboundRequest object
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                inboundOrderItemId,
                "inbound",
                List.of(
                        Map.ofEntries(
                                Map.entry(ApiService.KEY_CLASS, qcByBoundInboundRequestType),
                                Map.entry("bizType", "PURCHASE"),
                                Map.entry("position", storageObjects.position.id()),
                                Map.entry("material", material.id()),
                                Map.entry("unit", unit.id()),
                                Map.entry("amount", 100L)
                        )
                )
        ));
        // reload the inbound order item and check the actualQuantity field
        var reloadedInboundOrderItem = getObject(inboundOrderItemId);
        Assert.assertEquals(100L, reloadedInboundOrderItem.get("actualQuantity"));

        // inbound by spec
        var qcBySpecInboundRequestType = "org.metavm.manufacturing.storage.BySpecInboundRequest";
        // invoke InboundOrderItem.inbound with the inboundRequest object
        doInTransaction(() -> apiClient.callMethod(
                inboundOrderItemId,
                "inbound",
                List.of(
                        Map.ofEntries(
                                Map.entry(ApiService.KEY_CLASS, qcBySpecInboundRequestType),
                                Map.entry("bizType", "PURCHASE"),
                                Map.entry("position", storageObjects.position.id()),
                                Map.entry("material", material.id()),
                                Map.entry("unit", unit.id()),
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

        reloadedInboundOrderItem = getObject(inboundOrderItemId);
        Assert.assertEquals(135L, reloadedInboundOrderItem.get("actualQuantity"));
    }


    private void processTransfer(StorageObjects storageObjects, ClassInstanceWrap material, ClassInstanceWrap unit) {
        // get transfer order type
        var qcTransferOrder = "org.metavm.manufacturing.storage.TransferOrder";
        // create a transfer order
        var transferOrderId = doInTransaction(() -> apiClient.newInstance(
                qcTransferOrder,
                List.of("transferOrder1", "STORAGE", storageObjects.warehouse.id(), storageObjects.warehouse.id())
        ));
        // create a transfer order item
        var qcTransferOrderItem = "org.metavm.manufacturing.storage.TransferOrderItem";
        var transferOrderItemId = doInTransaction(() -> apiClient.newInstance(
                qcTransferOrderItem,
                Arrays.asList(transferOrderId, material.id(), 100, unit.id(), null, null)
        ));
        // create an inventory
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryKlass,
                Arrays.asList(
                        material.id(),
                        storageObjects.position.id(),
                        "QUALIFIED",
                        "INITIAL",
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
                                "to", storageObjects.position2.id(),
                                "items", List.of(
                                        Map.of(
                                                "transferOrderItem", transferOrderItemId,
                                                "subItems", List.of(
                                                        Map.of(
                                                                "inventory", inventoryId,
                                                                "amount", 20,
                                                                "unit", unit.id()
                                                        )
                                                )
                                        )
                                )
                        )
                )
        ));
        // assert that the transfer has taken place
        var reloadedInventory = getObject(inventoryId);
        assertEquals(80L, reloadedInventory.get("quantity"));
    }

    private record RoutingObjects(
            ClassInstanceWrap routing,
            ClassInstanceWrap routingProcess
    ) {

    }

    private RoutingObjects processRouting(ClassInstanceWrap material, ClassInstanceWrap unit) {
        var qcWorkCenter = "org.metavm.manufacturing.production.WorkCenter";
        var workCenterId = doInTransaction(() -> apiClient.newInstance(qcWorkCenter, List.of()));
        var qcProcess = "org.metavm.manufacturing.production.Process";
        var processId = doInTransaction(() -> apiClient.newInstance(qcProcess, List.of("process1")));
        var routingId = (String) doInTransaction(() -> apiClient.saveInstance(
                "org.metavm.manufacturing.production.Routing",
                Map.of(
                        "name", "routing001",
                        "product", material.id(),
                        "unit", unit.id(),
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
        var routing = getObject(routingId);
        var routingProcess = routing.getArray("processes").getObject(0);
//        var processListView = reloadedRoutingView.getInstance("processes");
//        var itemView = processListView.getElementInstance(0);
//        var successionListView = reloadedRoutingView.getInstance("successions");
        doInTransactionWithoutResult(() -> apiClient.saveInstance(
                "org.metavm.manufacturing.production.Routing",
                Map.of(
                        ApiService.KEY_ID, routing.id(),
                        "name", "routing001",
                        "product", material.id(),
                        "unit", unit.id(),
                        "processes", List.of(
                                routingProcess.getMap(),
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

    private void processBOM(ClassInstanceWrap material, ClassInstanceWrap unit, ClassInstanceWrap routing, ClassInstanceWrap routingProcess) {
        var bomId = saveInstance(
                "org.metavm.manufacturing.production.BOM",
                Map.of(
                        "product", material.id(),
                        "unit", unit.id(),
                        "routing", routing.id(),
                        "reportingProcess", routingProcess.id(),
                        "state", "ENABLED",
                        "inbound", true,
                        "autoInbound", true,
                        "secondaryOutputs", List.of(),
                        "components", List.of(
                                Map.ofEntries(
                                        Map.entry("sequence", 1),
                                        Map.entry("material", material.id()),
                                        Map.entry("unit", unit.id()),
                                        Map.entry("numerator", 1),
                                        Map.entry("denominator", 1),
                                        Map.entry("attritionRate", 0.0),
                                        Map.entry("pickMethod", "ON_DEMAND"),
                                        Map.entry("routingSpecified", false),
                                        Map.entry("process", routingProcess.id()),
                                        Map.entry("qualityInspectionState", "QUALIFIED"),
                                        Map.entry("feedType", "DIRECT"),
                                        Map.entry("items", List.of(
                                                Map.of(
                                                        "sequence", 1,
                                                        "numerator", 1,
                                                        "denominator", 1,
                                                        "process", routingProcess.id(),
                                                        "qualityInspectionState", "QUALIFIED",
                                                        "feedType", "DIRECT"
                                                )
                                        ))
                                )
                        )
                )
        );
//        var bomId = TestUtils.getSourceId(bomViewId);
        // create production order
        long startTime = System.currentTimeMillis();
        var productionOrderId = (String) doInTransaction(() -> apiClient.callMethod(
                bomId,
                "createProductionOrder",
                List.of(startTime, startTime + 3 * 24 * 60 * 60 * 1000, 10))
        );
        var productionOrder = getObject(productionOrderId);
        var ingredients = productionOrder.getArray("ingredients");
        Assert.assertEquals(startTime, productionOrder.getLong("plannedStartTime"));
        Assert.assertEquals(1, ingredients.size());
    }

}
