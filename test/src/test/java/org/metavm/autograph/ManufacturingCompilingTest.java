package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.util.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.metavm.util.TestUtils.doInTransaction;
import static org.metavm.util.TestUtils.doInTransactionWithoutResult;

@Slf4j
public class ManufacturingCompilingTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "manufacturing";

    public static final String USER_NAME = "leen";
    public static final String PASSWORD = "123456";
    public static final String qualityInspectionStateKlass = "org.metavm.manufacturing.material.QualityInspectionState";
    public static final String inventoryKlass = "org.metavm.manufacturing.storage.Inventory";
    public static final ApiNamedObject QUALIFIED = new ApiNamedObject("org.metavm.manufacturing.material.QualityInspectionState", "QUALIFIED", "QUALIFIED");
    public static final ApiNamedObject INITIAL = new ApiNamedObject("org.metavm.manufacturing.storage.InventoryBizState", "INITIAL", "INITIAL");
    public static final ApiNamedObject ADJUSTMENT = new ApiNamedObject("org.metavm.manufacturing.storage.InventoryOp", "ADJUSTMENT", "ADJUSTMENT");
    public static final ApiNamedObject PURCHASE = new ApiNamedObject("org.metavm.manufacturing.storage.InboundBizType", "PURCHASE", "PURCHASE");
    public static final ApiNamedObject STORAGE = new ApiNamedObject("org.metavm.manufacturing.storage.TransferBizType", "STORAGE", "STORAGE");
    public static final ApiNamedObject ENABLED = new ApiNamedObject("org.metavm.manufacturing.GeneralState", "ENABLED", "ENABLED");
    public static final ApiNamedObject DIRECT = new ApiNamedObject("org.metavm.manufacturing.production.FeedType", "DIRECT", "DIRECT");
    public static final ApiNamedObject NORMAL = new ApiNamedObject("org.metavm.manufacturing.material.MaterialKind", "NORMAL", "NORMAL");
    public static final ApiNamedObject YEAR = new ApiNamedObject("org.metavm.manufacturing.material.TimeUnit", "YEAR", "YEAR");
    public static final ApiNamedObject ROUND_HALF_UP = new ApiNamedObject("org.metavm.manufacturing.material.RoundingRule", "ROUND_HALF_UP", "ROUND_HALF_UP");
    public static final ApiNamedObject ON_DEMAND = new ApiNamedObject("org.metavm.manufacturing.production.PickMethod", "ON_DEMAND", "ON_DEMAND");
    public static final ApiNamedObject USER_SERVICE = ApiNamedObject.of("userService");
    public static final ApiNamedObject MATERIAL_SERVICE = ApiNamedObject.of("materialService");
    public static final ApiNamedObject ROUTING_SERVICE = ApiNamedObject.of("routingService");
    public static final ApiNamedObject BOM_SERVICE = ApiNamedObject.of("bomService");

    @Override
    protected void setUp() {
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
                        Arrays.asList("meter", "meter", ROUND_HALF_UP
                                , 2, null)
                ));
                var materialId = (Id) doInTransaction(() -> apiClient.callMethod(
                        MATERIAL_SERVICE,
                        "save",
                        List.of(
                                Map.of(
                                        "code", "sheet metal",
                                        "name", "sheet metal",
                                        "kind", NORMAL,
                                        "unit", unitId,
                                        "storageValidPeriod", 1,
                                        "storageValidPeriodUnit", YEAR
                                )
                        )
                ));
                // get QualityInspectionState type
                // get QualityInspectionState.QUALIFIED constant
                // invoke material.setFeedQualityInspectionStates with a list containing the QUALIFIED constant
                callMethod(
                        materialId, "setFeedQualityInspectionStates",
                        List.of(
                                List.of(QUALIFIED)
                        )
                );
                // reload the material object
                var material = getObject(materialId);
                // assert that the feedQualityInspectionStates field of the material object contains the QUALIFIED constant
                var feedQualityInspectionStates = material.getArray("feedQualityInspectionStates");
                Assert.assertEquals(1, feedQualityInspectionStates.size());
                Assert.assertEquals(QUALIFIED, feedQualityInspectionStates.getFirst());


                // reload the material view object
                var reloadedMaterial = getObject(materialId);
                // check the feedQualityInspectionStates field and assert that it didn't change
                var reloadedFeedQualityInspectionStates = reloadedMaterial.getArray("feedQualityInspectionStates");
                Assert.assertEquals(1, reloadedFeedQualityInspectionStates.size());
                Assert.assertEquals(QUALIFIED, reloadedFeedQualityInspectionStates.getFirst());

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
        TestUtils.doInTransaction(() -> apiClient.callMethod(USER_SERVICE, "signup",
                List.of(USER_NAME, PASSWORD)));
    }

    private void login() {
        TestUtils.doInTransaction(() -> apiClient.callMethod(
                USER_SERVICE, "login",
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
            ApiObject warehouse,
            ApiObject area,
            ApiObject position,
            ApiObject position2
    ) {

    }


    private void processInventory(ApiObject material,
                                  ApiObject position,
                                  ApiObject unit
    ) {
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryKlass,
                Arrays.asList(
                        material.id(),
                        position.id(),
                        QUALIFIED,
                        INITIAL,
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
        waitForAllTasksDone();
        var qualifiedInspectionStateId = typeManager.getEnumConstantId(qualityInspectionStateKlass, "QUALIFIED");
        var initialBizStateId = typeManager.getEnumConstantId("org.metavm.manufacturing.storage.InventoryBizState", "INITIAL");
        // query the inventory object by condition
        var queryResp = apiClient.search(
                inventoryKlass,
                Map.of(
                        "material", material.id(),
                        "position", position.id(),
                        "qualityInspectionState", qualifiedInspectionStateId,
                        "bizState", initialBizStateId
                ),
                1,
                20
        ) ;
        Assert.assertEquals(1, queryResp.total());
        var queriedInventory = queryResp.items().getFirst();
        Assert.assertEquals(inventoryId, queriedInventory.id());

        // decrease the inventory by 100 and asserts that the inventory is removed
        doInTransaction(() -> apiClient.callMethod(
                inventoryKlass,
                "decreaseQuantity",
                Arrays.asList(
                        material.id(),
                        position.id(),
                        QUALIFIED,
                        INITIAL,
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
                        ADJUSTMENT
                )
        ));
//        try {
//            getObject(inventoryId);
//            Assert.fail("Inventory should be removed");
//        } catch (BusinessException e) {
//            Assert.assertEquals(String.format("Object '%s' not found", inventoryId), e.getMessage());
//        }
    }

    private void processInbound(StorageObjects storageObjects, ApiObject material, ApiObject unit) {
        var inboundOrderId = doInTransaction(() -> apiClient.newInstance(
                "org.metavm.manufacturing.storage.InboundOrder",
                Arrays.asList(
                        "inboundOrder1",
                        PURCHASE,
                        storageObjects.warehouse.id(),
                        null
                )
        ));
        var inboundOrderItemId = (Id) doInTransaction(() -> apiClient.callMethod(
                inboundOrderId,
                "createItem",
                Arrays.asList(
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
                                Map.entry(ApiClient.KEY_DOLLAR_CLASS, qcByBoundInboundRequestType),
                                Map.entry("bizType", PURCHASE),
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
                                Map.entry(ApiClient.KEY_DOLLAR_CLASS, qcBySpecInboundRequestType),
                                Map.entry("bizType", PURCHASE),
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


    private void processTransfer(StorageObjects storageObjects, ApiObject material, ApiObject unit) {
        // get transfer order type
        var qcTransferOrder = "org.metavm.manufacturing.storage.TransferOrder";
        // create a transfer order
        var transferOrderId = doInTransaction(() -> apiClient.newInstance(
                qcTransferOrder,
                List.of("transferOrder1", STORAGE, storageObjects.warehouse.id(), storageObjects.warehouse.id())
        ));
        // create a transfer order item
        var transferOrderItemId = doInTransaction(() -> apiClient.callMethod(
                transferOrderId,
                "createItem",
                Arrays.asList(material.id(), 100, unit.id(), null, null)
        ));
        // create an inventory
        var inventoryId = doInTransaction(() -> apiClient.newInstance(
                inventoryKlass,
                Arrays.asList(
                        material.id(),
                        storageObjects.position.id(),
                        QUALIFIED,
                        INITIAL,
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
            ApiObject routing,
            ApiObject routingProcess
    ) {

    }

    private RoutingObjects processRouting(ApiObject material, ApiObject unit) {
        var qcWorkCenter = "org.metavm.manufacturing.production.WorkCenter";
        var workCenterId = doInTransaction(() -> apiClient.newInstance(qcWorkCenter, List.of()));
        var qcProcess = "org.metavm.manufacturing.production.Process";
        var processId = doInTransaction(() -> apiClient.newInstance(qcProcess, List.of("process1")));
        var routingId = (Id) doInTransaction(() -> apiClient.callMethod(
                ROUTING_SERVICE,
                "save",
                List.of(
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
                )
        ));
        Assert.assertNotNull(routingId);
        // reload routing
//        var routingId = TestUtils.getSourceId(routingViewId);
//        var reloadedRoutingView = instanceManager.getDefaultView(routingId).instance();
//        var viewId = (DefaultViewId) Id.parse(reloadedRoutingView.id());
//        Assert.assertEquals(viewId.getSourceId(), Id.parse(routingId));
        var routing = TestUtils.doInTransaction(() ->
                (ApiObject) Objects.requireNonNull(apiClient.callMethod(ROUTING_SERVICE, "get", List.of(routingId)))
        );
        var routingProcess = (ApiObject) routing.getArray("processes").getFirst();
//        var processListView = reloadedRoutingView.getInstance("processes");
//        var itemView = processListView.getElementInstance(0);
//        var successionListView = reloadedRoutingView.getInstance("successions");
        doInTransactionWithoutResult(() -> apiClient.callMethod(
                ROUTING_SERVICE,
                "save",
                List.of(
                    Map.of(
                            "entity", routingId,
                            "name", "routing001",
                            "product", material.id(),
                            "unit", unit.id(),
                            "processes", List.of(
                                    routingProcess.getFields(),
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
                )
        ));
        return new RoutingObjects(routing, routingProcess);
    }

    private void processBOM(ApiObject material, ApiObject unit, ApiObject routing, ApiObject routingProcess) {
        var bomId = (Id) callMethod(
                BOM_SERVICE,
                "create",
                List.of(
                    Map.of(
                            "product", material.id(),
                            "unit", unit.id(),
                            "routing", routing.getId("entity"),
                            "reportingProcess", routingProcess.getId("entity"),
                            "state", ENABLED,
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
                                            Map.entry("pickMethod", ON_DEMAND),
                                            Map.entry("routingSpecified", false),
                                            Map.entry("process", routingProcess.getId("entity")),
                                            Map.entry("qualityInspectionState", QUALIFIED),
                                            Map.entry("feedType", DIRECT),
                                            Map.entry("items", List.of(
                                                    Map.of(
                                                            "sequence", 1,
                                                            "numerator", 1,
                                                            "denominator", 1,
                                                            "process", routingProcess.getId("entity"),
                                                            "qualityInspectionState", QUALIFIED,
                                                            "feedType", DIRECT
                                                    )
                                            ))
                                    )
                            )
                    )
                )
        );
//        var bomId = TestUtils.getSourceId(bomViewId);
        // create production order
        long startTime = System.currentTimeMillis();
        var productionOrderId = (Id) doInTransaction(() -> apiClient.callMethod(
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
