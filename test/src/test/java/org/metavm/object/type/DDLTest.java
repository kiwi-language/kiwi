package org.metavm.object.type;

import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.MemInstanceStore;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.core.*;
import org.metavm.task.DDLFinalizationTask;
import org.metavm.task.DDLRollbackTaskGroup;
import org.metavm.task.ForwardedFlagSetter;
import org.metavm.task.ReferenceRedirector;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DDLTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DDLTest.class);

    private TypeManager typeManager;
    private EntityContextFactory entityContextFactory;
    private ApiClient apiClient;
    private MemInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var commonManagers = TestUtils.createCommonManagers(bootResult);
        typeManager = commonManagers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        apiClient = new ApiClient(new ApiService(bootResult.entityContextFactory()));
        instanceStore = bootResult.instanceStore();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        typeManager = null;
        entityContextFactory = null;
        apiClient = null;
        instanceStore = null;
    }

    public void testDDL() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_before.masm", typeManager, entityContextFactory);
        var shoesId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Shoes",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 100
        )));
        var shoes = apiClient.getObject(shoesId);
        Assert.assertEquals(100L, shoes.get("price"));
//        var inventory = shoes.getObject("inventory");
        var inventoryId = shoes.getString("inventory");
        var inventory = apiClient.getObject(inventoryId);
        Assert.assertEquals(100L, inventory.get("quantity"));
        var boxId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Box<Inventory>", Map.of(
                "item", inventoryId
        )));
        var commitId = MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_after.masm", typeManager, false, entityContextFactory);
        var hatId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Hat",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 20
        )));
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var commit = context.getEntity(Commit.class, commitId);
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_CODE, "Product"));
            Assert.assertNull(klass.findFieldByCode("version"));
            try (var cachingContext = entityContextFactory.newLoadedContext(TestConstants.APP_ID, commit.getWal())) {
                var hat = (ClassInstance) cachingContext.getInstanceContext().get(Id.parse(hatId));
                var ver = hat.getField("version").resolveObject();
                Assert.assertEquals(Instances.longInstance(0L), ver.getField("majorVersion"));
            }
        }
        TestUtils.waitForDDLDone(entityContextFactory);
        var productType = typeManager.getTypeByCode("Product").type();
        Assert.assertTrue(productType.getFieldByName("inventory").isChild());
        var shoes1 = apiClient.getObject(shoesId);
        var hat = apiClient.getObject(hatId);
        Assert.assertEquals(true, shoes1.get("available"));
        Assert.assertNull(shoes1.get("description"));
        Assert.assertEquals(100.0, shoes1.get("price"));
        Assert.assertEquals(100L, shoes1.getObject("inventory").get("quantity"));
        Assert.assertEquals(0L, shoes1.getObject("version").get("majorVersion"));
        Assert.assertEquals(0L, hat.getObject("version").get("majorVersion"));
        // check that index entries have been generated
        var foundId = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Shoes")));
        Assert.assertEquals(shoesId, foundId);
        var foundId2 = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Hat")));
        Assert.assertEquals(hatId, foundId2);
        var box = apiClient.getObject(boxId);
        Assert.assertEquals(1L, box.get("count"));
        var commitState = apiClient.getObject(apiClient.getObject(commitId).getString("state"));
        Assert.assertEquals("FINISHED", commitState.get("name"));
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var invInst = context.getInstanceContext().get(Id.parse(inventoryId));
                context.finish();
                Assert.assertFalse(invInst.isRoot());
            }
        });
        TestUtils.waitForTaskDone(t -> t instanceof ForwardedFlagSetter, 60L, entityContextFactory);
        var newInventorId = TestUtils.doInTransaction(() -> {
           try(var context = newContext()) {
               var invInst = context.getInstanceContext().get(Id.parse(inventoryId));
               Assert.assertEquals(invInst.getRoot().getTreeId(), invInst.getId().getTreeId());
               var boxInst = (ClassInstance) context.getInstanceContext().get(Id.parse(boxId));
               var item = (InstanceReference) boxInst.getField("item");
               Assert.assertTrue(item.isForwarded());
               Assert.assertEquals(inventoryId, item.getStringId());
               Assert.assertNotEquals(invInst.getId(), item.getId());
               Assert.assertEquals(invInst.getReference(), item);
               return invInst.getId();
           }
        });
        TestUtils.waitForTaskDone(t -> t instanceof ReferenceRedirector, 60L, entityContextFactory);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var boxInst = (ClassInstance) instCtx.get(Id.parse(boxId));
            Assert.assertEquals(newInventorId, boxInst.getField("item").resolveObject().getId());
            try {
                instCtx.get(Id.parse(inventoryId));
                Assert.fail("Should have been migrated");
            }
            catch (BusinessException e) {
                Assert.assertEquals(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
            }
            Assert.assertNull(
                "Forwarding pointer should have been removed",
                    instanceStore.get(TestConstants.APP_ID, Id.parse(inventoryId).getTreeId())
            );
        }
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_rollback.masm", typeManager, entityContextFactory);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var instCtx = context.getInstanceContext();
                var invInst = instCtx.get(newInventorId);
                context.finish();
                Assert.assertNotEquals(newInventorId, invInst.tryGetCurrentId());
            }
        });
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var instCtx = context.getInstanceContext();
                var invInst = instCtx.get(newInventorId);
                Assert.assertEquals(newInventorId, invInst.getId());
            }
        });
        TestUtils.waitForTaskDone(t -> t instanceof ForwardedFlagSetter, 60L, entityContextFactory);
        // Ensure removal of a referenced object is prevented even when the object is migrating.
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var instCtx = context.getInstanceContext();
                instCtx.remove(instCtx.get(Id.parse(shoesId)));
                var invInst = instCtx.get(newInventorId);
                instCtx.remove(invInst);
                try {
                    context.finish();
                    Assert.fail("The inventory object is referenced and the removal should have failed");
                }
                catch (BusinessException e) {
                    Assert.assertEquals(ErrorCode.STRONG_REFS_PREVENT_REMOVAL2, e.getErrorCode());
                }
            }
        });
        TestUtils.waitForTaskDone(t -> t instanceof ReferenceRedirector, 60L, entityContextFactory);
        try(var context = newContext()) {
            try {
                context.getInstanceContext().get(newInventorId);
                Assert.fail();
            }
            catch (BusinessException e) {
                Assert.assertEquals(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
            }
            var productTree = instanceStore.get(TestConstants.APP_ID, Id.parse(shoesId).getTreeId());
            var visitor = new StreamVisitor(new ByteArrayInputStream(productTree.getData())) {

                private int numFps;

                @Override
                public void visitForwardingPointer() {
                    numFps++;
                    super.visitForwardingPointer();
                }
            };
            visitor.visitGrove();
            Assert.assertEquals(0, visitor.numFps);
        }
    }

    public void testCheck() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_before.masm", typeManager, entityContextFactory);
        var shoesId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Shoes",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 100
        )));
        var shoes = apiClient.getObject(shoesId);
        Assert.assertEquals(100L, shoes.get("price"));
        try {
            MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_after_failed.masm", typeManager, false, entityContextFactory);
            Assert.fail("Should have thrown exception");
        } catch (BusinessException e) {
            Assert.assertEquals(
                    ResultUtil.formatMessage(ErrorCode.MISSING_FIELD_INITIALIZER, "Product.description"),
                    e.getMessage()
            );
        }

    }

    public void testDDLRollback() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_before.masm", typeManager, entityContextFactory);
        var shoesId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Shoes",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 100
        )));
        var shoes = apiClient.getObject(shoesId);
        var inventoryId = shoes.getString("inventory");
        TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Hat",
                "inventory", inventoryId,
                "price", 20
        )));
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_after.masm", typeManager, false, entityContextFactory);
        TestUtils.waitForTaskGroupDone(g -> g instanceof DDLRollbackTaskGroup, entityContextFactory);
    }

    public void testEntityToValueConversion() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/value_ddl_before.masm", typeManager, entityContextFactory);
        var currencyKlass = typeManager.getTypeByCode("Currency").type();
        var priceKlass = typeManager.getTypeByCode("Price").type();
        var productKlass = typeManager.getTypeByCode("Product").type();
        var yuanId = TestUtils.getEnumConstantByName(currencyKlass, "YUAN").getIdNotNull();
        var shoesId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", Map.of(
                        "amount", 100,
                        "currency", yuanId
                )
        )));
        var priceId = apiClient.getObject(shoesId).getString("price");
        var productIds = new ArrayList<>(List.of(shoesId));
        for (int i = 0; i < 16; i++) {
            var _i = i;
            productIds.add(TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                    "name", "Shoes" + _i,
                    "price", priceId
            ))));
        }
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/value_ddl_after.masm", typeManager, false, entityContextFactory);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var productKlass1 = context.getKlass(productKlass.id());
                var priceKlass1 = context.getKlass(priceKlass.id());
                var currencyKlass1 = context.getKlass(currencyKlass.id());
                var yuan = currencyKlass1.getEnumConstants().get(0);
                var instCtx = context.getInstanceContext();
                var price = ClassInstanceBuilder.newBuilder(priceKlass1.getType())
                        .data(Map.of(
                                priceKlass1.getFieldByCode("amount"),
                                Instances.doubleInstance(100.0),
                                priceKlass1.getFieldByCode("currency"),
                                yuan.getReference()
                        ))
                        .build();
                instCtx.bind(price);
                var nameField = productKlass1.getFieldByCode("name");
                var priceField = productKlass1.getFieldByCode("price");
                var products = new ArrayList<ClassInstance>();
                for (int i = 0; i < 16; i++) {
                    var product = ClassInstanceBuilder.newBuilder(productKlass1.getType())
                            .data(Map.of(
                                    nameField,
                                    Instances.stringInstance("Hat" + i),
                                    priceField,
                                    price.getReference()
                            ))
                            .build();
                    products.add(product);
                    instCtx.bind(product);
                }
                context.finish();
                products.forEach(p -> productIds.add(p.getStringId()));
            }
        });
        TestUtils.waitForDDLDone(entityContextFactory);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                for (String productId : productIds) {
                    context.getInstanceContext().get(Id.parse(productId));
                }
                context.finish();
            }
        });
        var priceKlass2 = typeManager.getTypeByCode("Price").type();
        Assert.assertEquals(ClassKind.VALUE.code(), priceKlass2.kind());
        for (String productId : productIds) {
            var product = apiClient.getObject(productId);
            MatcherAssert.assertThat(product.getObject("price"), CoreMatchers.instanceOf(ClassInstanceWrap.class));
            var price = (ClassInstanceWrap) product.get("price");
            Assert.assertNull(price.get("$id"));
        }
        var commitId = MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/value_ddl_before.masm", typeManager, false, entityContextFactory);
        TestUtils.runTasks(1, 16, entityContextFactory);
        var shoes1 = apiClient.getObject(shoesId);
        var price1 = shoes1.get("price");
        MatcherAssert.assertThat(price1, CoreMatchers.instanceOf(ClassInstanceWrap.class));
        TestUtils.waitForDDLDone(entityContextFactory);
        try(var context = newContext()) {
            var commit = context.getEntity(Commit.class, commitId);
            Assert.assertEquals(CommitState.CLEANING_UP, commit.getState());
        }
        var priceKlass3 = typeManager.getTypeByCode("Price").type();
        Assert.assertEquals(ClassKind.CLASS.code(), priceKlass3.kind());
        for (String productId : productIds) {
            var product = apiClient.getObject(productId);
            MatcherAssert.assertThat(product.get("price"), CoreMatchers.instanceOf(String.class));
        }
        TestUtils.waitForTaskDone(t -> t instanceof DDLFinalizationTask, entityContextFactory);
        try (var context = newContext()){
            var commit = context.getEntity(Commit.class, commitId);
            Assert.assertEquals(CommitState.FINISHED, commit.getState());
            var instCtx = context.getInstanceContext();
            var productKlass1 = context.getKlass(productKlass.id());
            var priceField = productKlass1.getFieldByCode("price");
            for (String productId : productIds) {
                var productInst = (ClassInstance) instCtx.get(Id.parse(productId));
                var priceRef = (InstanceReference) productInst.getField(priceField);
                Assert.assertFalse(priceRef.isResolved());
                Assert.assertFalse(priceRef.isEager());
                Assert.assertFalse(priceRef.isValueReference());
            }
        }
    }

    public void testRaceCondition() throws InterruptedException {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_before.masm", typeManager, entityContextFactory);
        var shoesId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Shoes",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 100
        )));
        var shoes = apiClient.getObject(shoesId);
        var inventoryId = shoes.getString("inventory");
        var boxIds = new ArrayList<String>();
        for (int i = 0; i < 16; i++) {
            var boxId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Box<Inventory>", Map.of(
                    "item", inventoryId
            )));
            boxIds.add(boxId);
        }
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_after.masm", typeManager, entityContextFactory);
//        Constants.SESSION_TIMEOUT = 300;
        var newInventoryId = TestUtils.doInTransaction(() -> {
            try(var context = newContext()) {
                var instCtx = context.getInstanceContext();
                var invInst = instCtx.get(Id.parse(inventoryId));
                context.finish();
                return invInst.getCurrentId().toString();
            }
        });
        Assert.assertNotEquals(inventoryId, newInventoryId);
        var monitor = new Object();
        var ref = new Object() {
          boolean timeout;
        };
        var thread = new Thread(() -> TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var instCtx = context.getInstanceContext();
                var invInst = instCtx.get(Id.parse(inventoryId));
                var boxKlass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_CODE, "Box"));
                var boxOfInvKlass = boxKlass.getParameterized(List.of(invInst.getType()));
                var boxInst = ClassInstanceBuilder.newBuilder(boxOfInvKlass.getType())
                        .data(Map.of(
                                boxOfInvKlass.getFieldByCode("item"), invInst.getReference(),
                                boxOfInvKlass.getFieldByCode("count"), Instances.longInstance(1)
                        ))
                        .build();
                instCtx.bind(boxInst);
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    context.finish();
                }
                catch (SessionTimeoutException e) {
                    ref.timeout = true;
                    throw e;
                }
            }
        }));
        thread.start();
        TestUtils.waitForTaskDone(t -> t instanceof ReferenceRedirector, 100L, entityContextFactory);
        synchronized (monitor) {
            monitor.notify();
        }
        for (String boxId : boxIds) {
            var box = apiClient.getObject(boxId);
            Assert.assertEquals(newInventoryId, box.getString("item"));
        }
//        Constants.SESSION_TIMEOUT = -1;
        thread.join();
        Assert.assertTrue(ref.timeout);
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID, builder -> builder.asyncPostProcess(false));
    }

}
