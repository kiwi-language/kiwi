package org.metavm.object.type;

import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.ddl.CommitState;
import org.metavm.entity.*;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.task.DDLTask;
import org.metavm.task.IDDLTask;
import org.metavm.task.ScanTask;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DDLTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DDLTest.class);
    public static final String SRC_DIR = "/Users/leen/workspace/object/test/src/test/resources/asm/";

    private TypeManager typeManager;
    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;
    private MemInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var commonManagers = TestUtils.createCommonManagers(bootResult);
        typeManager = commonManagers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        apiClient = new ApiClient(new ApiService(bootResult.entityContextFactory(), bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
        instanceStore = bootResult.instanceStore();
        schedulerAndWorker  = bootResult.schedulerAndWorker();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        typeManager = null;
        entityContextFactory = null;
        apiClient = null;
        instanceStore = null;
        schedulerAndWorker = null;
    }

    public void testDDL() {
        assemble("ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 100,
                "manufacturer", "AppEase"
        ));
        var shoes = apiClient.getObject(shoesId);
        Assert.assertEquals(100, shoes.get("price"));
//        var inventory = shoes.getObject("inventory");
        var inventoryId = shoes.getString("inventory");
        var inventory = apiClient.getObject(inventoryId);
        Assert.assertEquals(100, inventory.get("quantity"));
        var boxId = saveInstance("Box<Inventory>", Map.of(
                "item", inventoryId
        ));
        var commitId = assemble("ddl_after.masm", false);
        var hatId = saveInstance("Product", Map.of(
                "name", "Hat",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 20,
                "manufacturer", "AppEase"
        ));
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var commit = context.getEntity(Commit.class, commitId);
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, "Product"));
            Assert.assertNull(klass.findFieldByName("version"));
            try (var cachingContext = entityContextFactory.newLoadedContext(TestConstants.APP_ID, commit.getWal())) {
                var hat = (ClassInstance) cachingContext.getInstanceContext().get(Id.parse(hatId));
                var ver = hat.getField("version").resolveObject();
                Assert.assertEquals(Instances.intInstance(0), ver.getField("majorVersion"));
            }
        }
        TestUtils.waitForDDLPrepared(schedulerAndWorker);
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var productKlass = context.getKlassByQualifiedName("Product");
            Assert.assertTrue(productKlass.getFieldByName("inventory").isChild());
        }
        var shoes1 = apiClient.getObject(shoesId);
        var hat = apiClient.getObject(hatId);
        Assert.assertEquals(true, shoes1.get("available"));
        Assert.assertNull(shoes1.get("description"));
        Assert.assertEquals(100.0, shoes1.get("price"));
        Assert.assertEquals(100, shoes1.getObject("inventory").get("quantity"));
        Assert.assertEquals(0, shoes1.getObject("version").get("majorVersion"));
        Assert.assertEquals(0, hat.getObject("version").get("majorVersion"));
        // check that index entries have been generated
        var foundId = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Shoes")));
        Assert.assertEquals(shoesId, foundId);
        var foundId2 = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Hat")));
        Assert.assertEquals(hatId, foundId2);
        var box = apiClient.getObject(boxId);
        Assert.assertEquals(1, box.get("count"));
        var commitState = apiClient.getObject(apiClient.getObject(commitId).getString("state"));
        Assert.assertEquals(CommitState.RELOCATING.name(), commitState.get("name"));
        TestUtils.waitForDDLState(CommitState.SETTING_REFERENCE_FLAGS, schedulerAndWorker);
        try (var context = newContext()) {
            var invInst = context.getInstanceContext().get(Id.parse(inventoryId));
            Assert.assertFalse(invInst.isRoot());
        }
        TestUtils.waitForDDLState(CommitState.UPDATING_REFERENCE, schedulerAndWorker);
        var newInventorId = TestUtils.doInTransaction(() -> {
           try(var context = newContext()) {
               var invInst = context.getInstanceContext().get(Id.parse(inventoryId));
               Assert.assertEquals(invInst.getRoot().getTreeId(), invInst.getId().getTreeId());
               var boxInst = (ClassInstance) context.getInstanceContext().get(Id.parse(boxId));
               var item = (Reference) boxInst.getField("item");
               Assert.assertTrue(item.isForwarded());
               Assert.assertEquals(inventoryId, item.getStringId());
               Assert.assertNotEquals(invInst.getId(), item.getId());
               Assert.assertEquals(invInst.getReference(), item);
               return invInst.getId();
           }
        });
        TestUtils.waitForDDLCompleted(schedulerAndWorker);
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
        logger.debug("Deploying rollback metadata");
        assemble("ddl_rollback.masm", false);
        TestUtils.waitForDDLState(CommitState.SETTING_REFERENCE_FLAGS, schedulerAndWorker);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var invInst = instCtx.get(newInventorId);
            Assert.assertNotEquals(newInventorId, invInst.tryGetCurrentId());
            Assert.assertEquals(newInventorId, invInst.getId());
        }
        TestUtils.waitForDDLState(CommitState.UPDATING_REFERENCE, schedulerAndWorker);
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
        TestUtils.waitForDDLCompleted(schedulerAndWorker);
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
        assemble("ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 100,
                "manufacturer", "AppEase"
        ));
        var shoes = apiClient.getObject(shoesId);
        Assert.assertEquals(100, shoes.get("price"));
        try {
            assemble("ddl_after_failed.masm", false);
            Assert.fail("Should have thrown exception");
        } catch (BusinessException e) {
            Assert.assertEquals(
                    ResultUtil.formatMessage(ErrorCode.MISSING_FIELD_INITIALIZER, "Product.description"),
                    e.getMessage()
            );
        }

    }

    public void testDDLRollback() {
        assemble("ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "inventory", Map.of(
                        "quantity", 100
                ),
                "price", 100,
                "manufacturer", "AppEase"
        ));
        for (int i = 0; i < 16; i++) {
            saveInstance("Product", Map.of(
                    "name", "Product" + i,
                    "inventory", Map.of(
                            "quantity", 100
                    ),
                    "price", 100,
                    "manufacturer", "AppEase"
            ));
        }
        var shoes = apiClient.getObject(shoesId);
        var inventoryId = shoes.getString("inventory");
        saveInstance("Product", Map.of(
                "name", "Hat",
                "inventory", inventoryId,
                "price", 20,
                "manufacturer", "AppEase"
        ));
        var commitId = assemble("ddl_after.masm", false);
        Field availableField;
        try(var context = newContext()) {
            var commit = context.getEntity(Commit.class, commitId);
            try(var walContext = entityContextFactory.newContext(TestConstants.APP_ID, builder -> builder.readWAL(commit.getWal()))) {
                var productKlass = Objects.requireNonNull(walContext.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, "Product"));
                availableField = productKlass.getFieldByName("available");
            }
        }
        TestUtils.waitForDDLState(s -> s == CommitState.ABORTED, 16, schedulerAndWorker);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            try {
                shoesInst.getUnknownField(availableField.getDeclaringType().getTag(), availableField.getTag());
                Assert.fail("Field should have been rolled back");
            }
            catch (IllegalStateException ignored) {}
            var invInst = instCtx.get(Id.parse(inventoryId));
            Assert.assertNull(invInst.getParent());
            Assert.assertNull(invInst.getParentField());
        }
    }

    public void testEntityToValueConversion() {
        assemble("value_ddl_before.masm");
        var yuanId = typeManager.getEnumConstantId("Currency", "YUAN");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", Map.of(
                        "amount", 100,
                        "currency", yuanId
                )
        ));
        var priceId = apiClient.getObject(shoesId).getString("price");
        var productIds = new ArrayList<>(List.of(shoesId));
        for (int i = 0; i < 16; i++) {
            productIds.add(saveInstance("Product", Map.of(
                    "name", "Shoes" + i,
                    "price", priceId
            )));
        }
        assemble("value_ddl_after.masm", false);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var productKlass1 = context.getKlassByQualifiedName("Product");
                var priceKlass1 = context.getKlassByQualifiedName("Price");
                var currencyKlass1 = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, "Currency"));
                var sft = StaticFieldTable.getInstance(currencyKlass1.getType(), context);
                var yuan = sft.getEnumConstants().get(0);
                var instCtx = context.getInstanceContext();
                var price = ClassInstanceBuilder.newBuilder(priceKlass1.getType())
                        .data(Map.of(
                                priceKlass1.getFieldByName("amount"),
                                Instances.doubleInstance(100.0),
                                priceKlass1.getFieldByName("currency"),
                                yuan.getReference()
                        ))
                        .build();
                instCtx.bind(price);
                var nameField = productKlass1.getFieldByName("name");
                var priceField = productKlass1.getFieldByName("price");
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
        TestUtils.waitForDDLPrepared(schedulerAndWorker);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                for (String productId : productIds) {
                    context.getInstanceContext().get(Id.parse(productId));
                }
                context.finish();
            }
        });
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var priceKlass2 = context.getKlassByQualifiedName("Price");
            Assert.assertTrue(priceKlass2.isValue());
        }
        TestUtils.waitForDDLCompleted(schedulerAndWorker);
        for (String productId : productIds) {
            var product = apiClient.getObject(productId);
            MatcherAssert.assertThat(product.getObject("price"), CoreMatchers.instanceOf(ClassInstanceWrap.class));
            var price = (ClassInstanceWrap) product.get("price");
            Assert.assertNull(price.get("$id"));
        }
        var commitId = assemble("value_ddl_before.masm", false);
        TestUtils.runTasks(1, 16, schedulerAndWorker);
        var shoes1 = apiClient.getObject(shoesId);
        var price1 = shoes1.get("price");
        MatcherAssert.assertThat(price1, CoreMatchers.instanceOf(ClassInstanceWrap.class));
        TestUtils.waitForDDLPrepared(schedulerAndWorker);
        try(var context = newContext()) {
            var commit = context.getEntity(Commit.class, commitId);
            Assert.assertEquals(CommitState.RELOCATING, commit.getState());
            var priceKlass3 = context.getKlassByQualifiedName("Price");
            Assert.assertSame(ClassKind.CLASS, priceKlass3.getKind());
        }
        for (String productId : productIds) {
            var product = apiClient.getObject(productId);
            MatcherAssert.assertThat(product.get("price"), CoreMatchers.instanceOf(String.class));
        }
        TestUtils.waitForDDLCompleted(schedulerAndWorker);
        try (var context = newContext()){
            var commit = context.getEntity(Commit.class, commitId);
            Assert.assertEquals(CommitState.COMPLETED, commit.getState());
            var instCtx = context.getInstanceContext();
            var productKlass1 = context.getKlassByQualifiedName("Product");
            var priceField = productKlass1.getFieldByName("price");
            for (String productId : productIds) {
                var productInst = (ClassInstance) instCtx.get(Id.parse(productId));
                var priceRef = (Reference) productInst.getField(priceField);
                Assert.assertFalse(priceRef.isResolved());
                Assert.assertFalse(priceRef.isEager());
                Assert.assertFalse(priceRef.isValueReference());
            }
        }
    }

    public void testRollbackEntityToValueConversion() {
        assemble("value_ddl_before.masm");
        var yuanId = typeManager.getEnumConstantId("Currency", "YUAN");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", Map.of(
                        "amount", 100,
                        "currency", yuanId
                )
        ));
        var commitId = assemble("value_ddl_after.masm", false);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var commit = context.getCommit(commitId);
                commit.cancel();
                context.finish();
            }
        });
        TestUtils.waitForDDLState(CommitState.ABORTING, schedulerAndWorker);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var priceRef = (Reference) shoesInst.getField("price");
            Assert.assertTrue(priceRef.isEager());
        }
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var priceRef = (Reference) shoesInst.getField("price");
            Assert.assertFalse(priceRef.isEager());
        }
    }

    public void testRollbackValueToEntityConversion() {
        assemble("value_ddl_after.masm");
        var yuanId = typeManager.getEnumConstantId("Currency", "YUAN");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "price", Map.of(
                        "amount", 100,
                        "currency", yuanId
                )
        ));
        var commitId = assemble("value_ddl_before.masm", false);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var commit = context.getCommit(commitId);
                commit.cancel();
                context.finish();
            }
        });
        TestUtils.waitForDDLState(CommitState.ABORTING, schedulerAndWorker);
        Id priceId;
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var priceRef = (Reference) shoesInst.getField("price");
            Assert.assertTrue(priceRef.isValueReference());
            priceId = priceRef.getId();
        }
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var priceRef = (Reference) shoesInst.getField("price");
            Assert.assertFalse(priceRef.isEager());
            Assert.assertTrue(priceRef.isValueReference());
            Assert.assertTrue(priceRef.isInlineValueReference());
            Assert.assertTrue(priceRef.isResolved());
            try {
                instCtx.get(priceId);
                Assert.fail("Should have been removed");
            }
            catch (BusinessException e) {
                Assert.assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
            }
        }
    }

    public void testEntityToEnumConversion() {
        assemble("enum_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "kind", Map.of(
                        "name", "DEFAULT",
                        "code", 0
                )
        ));
        var kindId0 = apiClient.getObject(shoesId).getString("kind");
        assemble("enum_ddl_after.masm", false);
        TestUtils.waitForDDLCompleted(schedulerAndWorker);
        var isDefaultProduct = TestUtils.doInTransaction(() -> apiClient.callMethod(shoesId, "isDefaultKind", List.of()));
        Assert.assertEquals(true, isDefaultProduct);
        var shoes = apiClient.getObject(shoesId);
        var defaultKindId = typeManager.getEnumConstantId("ProductKind", "DEFAULT");
        var hotelKind = typeManager.getEnumConstantId("ProductKind", "HOTEL");
        var kindId = shoes.getString("kind");
        Assert.assertEquals(defaultKindId, kindId);
        try {
            apiClient.getObject(kindId0);
            Assert.fail("Should have been removed");
        }
        catch (BusinessException e) {
            Assert.assertEquals(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
        assemble("enum_ddl_rollback.masm");
        try (var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var kind = shoesInst.getField("kind").resolveObject();
            Assert.assertFalse(kind.getKlass().isEnum());
            Assert.assertEquals(Instances.stringInstance("DEFAULT"), kind.getField("name"));
            Assert.assertEquals(Instances.intInstance(0), kind.getField("code"));
            try {
                instCtx.get(Id.parse(hotelKind));
                Assert.fail("Should have been removed");
            }
            catch (BusinessException e) {
                Assert.assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
            }
        }
    }

    public void testEnumConversionRollback() {
        assemble("enum_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "Shoes",
                "kind", Map.of(
                        "name", "DEFAULT",
                        "code", 0
                )
        ));
        var kindId = apiClient.getObject(shoesId).getString("kind");
        var commitId = assemble("enum_ddl_after.masm", false);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var commit = context.getCommit(commitId);
                commit.cancel();
                context.finish();
            }
        });
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var kindRef = (Reference) shoesInst.getField("kind");
            Assert.assertFalse(kindRef instanceof RedirectingReference);
            var kind = (ClassInstance) instCtx.get(Id.parse(kindId));
            Assert.assertSame(kind, kindRef.resolve());
            Assert.assertNull(kind.tryGetUnknown(StdKlass.enum_.get().getTag(), StdField.enumName.get().getTag()));
            Assert.assertNull(kind.tryGetUnknown(StdKlass.enum_.get().getTag(), StdField.enumOrdinal.get().getTag()));
        }
    }

    public void testValueToChildConversion() {
        assemble("value_to_child_ddl_before.masm");
        var yuanId = typeManager.getEnumConstantId("Currency", "YUAN");
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "price", Map.of(
                        "amount", 100,
                        "currency", yuanId
                )
        ));
        assemble("value_to_child_ddl_after.masm");
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var priceRef = (Reference) shoesInst.getField("price");
            Assert.assertFalse(priceRef.isValueReference());
            var price = (ClassInstance) priceRef.resolve();
            Assert.assertNotNull(price.tryGetId());
            Assert.assertEquals(price.getTreeId(), shoesInst.getTreeId());
        }
        assemble("value_to_child_ddl_before.masm");
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var priceRef = (Reference) shoesInst.getField("price");
            Assert.assertTrue(priceRef.isValueReference());
            var price = (ClassInstance) priceRef.resolve();
            Assert.assertNull(price.tryGetId());
            Assert.assertNull(price.getParent());
            Assert.assertNull(price.getParentField());
        }
    }

    public void testChildToEnumConversion() {
        assemble("child_to_enum_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "kind", Map.of("name", "DEFAULT")
        ));
        var kindId = apiClient.getObject(shoesId).getObject("kind").getString("$id");
        assemble("child_to_enum_ddl_after.masm");
        var defaultKindId = typeManager.getEnumConstantId("ProductKind", "DEFAULT");
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = (ClassInstance) instCtx.get(Id.parse(shoesId));
            var kindRef = (Reference) shoesInst.getField("kind");
            Assert.assertEquals(defaultKindId, kindRef.getStringId());
            try {
                instCtx.get(Id.parse(kindId));
                Assert.fail("Should have been removed");
            }
            catch (BusinessException e) {
                Assert.assertEquals(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
            }
        }
        assemble("child_to_enum_ddl_rollback.masm");
        var kind = apiClient.getObject(shoesId).getObject("kind");
        Assert.assertEquals("DEFAULT", kind.getString("name"));
        Assert.assertEquals(Id.parse(shoesId).getTreeId(), Id.parse(kind.getString("$id")).getTreeId());
    }

    public void testChildToEnumConversionAbortion() {
        assemble("child_to_enum_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "kind", Map.of("name", "DEFAULT")
        ));
        var commitId = assemble("child_to_enum_ddl_after.masm", false);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var commit = context.getCommit(commitId);
                commit.cancel();
                context.finish();
            }
        });
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        var shoes = apiClient.getObject(shoesId);
        MatcherAssert.assertThat(shoes.get("kind"), CoreMatchers.instanceOf(ClassInstanceWrap.class));
    }

    public void testEnumToChildConversionAbortion() {
        assemble("enum_to_child_ddl.masm");
        var defaultKindId = Objects.requireNonNull(typeManager.getEnumConstantId("ProductKind", "DEFAULT"));
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "kind", defaultKindId
        ));
        var commitId = assemble("child_to_enum_ddl_rollback.masm", false);
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                var commit = context.getCommit(commitId);
                commit.cancel();
                context.finish();
            }
        });
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        var kindId = apiClient.getObject(shoesId).getString("kind");
        Assert.assertEquals(defaultKindId, kindId);
    }

    public void testValueToEnumConversion() {
        assemble("value_to_enum_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "kind", Map.of(
                        "name", "DEFAULT",
                        "code", 0
                )
        ));
        assemble("value_to_enum_ddl_after.masm");
        var defaultKindId = typeManager.getEnumConstantId("ProductKind", "DEFAULT");
        var shoes = apiClient.getObject(shoesId);
        var kind = shoes.get("kind");
        MatcherAssert.assertThat(kind, CoreMatchers.instanceOf(String.class));
        Assert.assertEquals(defaultKindId, kind);
        assemble("value_to_enum_ddl_rollback.masm");
        var shoes1 = apiClient.getObject(shoesId);
        var kind1 = shoes1.get("kind");
        MatcherAssert.assertThat(kind1, CoreMatchers.instanceOf(ClassInstanceWrap.class));
        var kind1Obj = (ClassInstanceWrap) kind1;
        Assert.assertNull(kind1Obj.get("$id"));
    }

    public void testAbortingValueToEnumConversion() {
        assemble("value_to_enum_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "kind", Map.of(
                        "name", "DEFAULT",
                        "code", 0
                )
        ));
        var commitId = assemble("value_to_enum_ddl_after.masm", false);
        doInContext(context -> {
           var commit = context.getCommit(commitId);
           commit.cancel();
        });
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        var kind = apiClient.getObject(shoesId).get("kind");
        MatcherAssert.assertThat(kind, CoreMatchers.instanceOf(ClassInstanceWrap.class));
        var kindObj = (ClassInstanceWrap) kind;
        Assert.assertNull(kindObj.get("$id"));
    }

    public void testAbortingEnumToValueConversion() {
        assemble("enum_to_value_ddl.masm");
        var defaultKindId = Objects.requireNonNull(typeManager.getEnumConstantId("ProductKind", "DEFAULT"));
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "kind", defaultKindId
                )
        );
        var commitId = assemble("value_to_enum_ddl_rollback.masm", false);
        doInContext(context -> {
            var commit = context.getCommit(commitId);
            commit.cancel();
        });
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        var kind = apiClient.getObject(shoesId).get("kind");
        MatcherAssert.assertThat(kind, CoreMatchers.instanceOf(String.class));
        Assert.assertEquals(defaultKindId, kind);
    }

    public void testChildFieldRemoval() {
        assemble("remove_child_field_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "inventory", Map.of("quantity", 100)
        ));
        var inventoryId = apiClient.getObject(shoesId).getObject("inventory").getString("$id");
        assemble("remove_child_field_ddl_after.masm");
        try {
            apiClient.getObject(inventoryId);
            Assert.fail("Should have been removed");
        }
        catch (BusinessException e) {
            Assert.assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
    }

    public void testCancelChildFieldRemoval() {
        assemble("remove_child_field_ddl_before.masm");
        var shoesId = saveInstance("Product", Map.of(
                "name", "shoes",
                "inventory", Map.of("quantity", 100)
        ));
        var inventoryId = apiClient.getObject(shoesId).getObject("inventory").getString("$id");
        assemble("remove_child_field_ddl_after.masm", false);
        TestUtils.waitForDDLState(CommitState.SUBMITTING, schedulerAndWorker);
        saveInstance("Box<Inventory>", Map.of(
                "item", inventoryId
        ));
        TestUtils.waitForDDLAborted(schedulerAndWorker);
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var shoesInst = instCtx.get(Id.parse(inventoryId));
            Assert.assertFalse(shoesInst.isRemoving());
        }
    }

    public void testRaceCondition() throws InterruptedException {
        try {
            assemble("ddl_before.masm");
            var shoesId = saveInstance("Product", Map.of(
                    "name", "Shoes",
                    "inventory", Map.of(
                            "quantity", 100
                    ),
                    "price", 100,
                    "manufacturer", "AppEase"
            ));
            var shoes = apiClient.getObject(shoesId);
            var inventoryId = shoes.getString("inventory");
            var boxIds = new ArrayList<String>();
            for (int i = 0; i < 16; i++) {
                var boxId = saveInstance("Box<Inventory>", Map.of(
                        "item", inventoryId
                ));
                boxIds.add(boxId);
            }
            assemble("ddl_after.masm", false);
            TestUtils.waitForDDLState(CommitState.SETTING_REFERENCE_FLAGS, schedulerAndWorker);
            var newInventoryId = TestUtils.doInTransaction(() -> {
                try (var context = newContext()) {
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
                    var boxKlass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, "Box"));
                    var boxOfInvKlass = ClassType.create(boxKlass, List.of(invInst.getType()));
                    var boxInst = ClassInstanceBuilder.newBuilder(boxOfInvKlass)
                            .data(Map.of(
                                    boxOfInvKlass.getKlass().getFieldByName("item"), invInst.getReference(),
                                    boxOfInvKlass.getKlass().getFieldByName("count"), Instances.intInstance(1)
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
                    } catch (SessionTimeoutException e) {
                        ref.timeout = true;
                    }
                }
            }));
            thread.start();
            DDLTask.DISABLE_DELAY = false;
            Constants.SESSION_TIMEOUT = 200L;
            TestUtils.waitForTaskDone(
                    t -> t instanceof IDDLTask ddlTask && ddlTask.getCommit().getState() == CommitState.COMPLETED,
                    20L,
                    ScanTask.DEFAULT_BATCH_SIZE,
                    schedulerAndWorker
            );
            synchronized (monitor) {
                monitor.notify();
            }
            for (String boxId : boxIds) {
                var box = apiClient.getObject(boxId);
                Assert.assertEquals(newInventoryId, box.getString("item"));
            }
            thread.join();
            Assert.assertTrue(ref.timeout);
        }
        finally {
            DDLTask.DISABLE_DELAY = true;
            Constants.SESSION_TIMEOUT = Constants.DEFAULT_SESSION_TIMEOUT;
        }
    }

    public void testCreateBeans() {
        assemble("bean_ddl_before.masm");
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = context.getKlassByQualifiedName("FooService");
            Assert.assertEquals(BeanKinds.COMPONENT, klass.getAttribute(AttributeNames.BEAN_KIND));
            Assert.assertEquals(NamingUtils.firstCharToLowerCase(klass.getName()), klass.getAttribute(AttributeNames.BEAN_NAME));
        }
        Id fooServiceId;
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var k = context.getKlassByQualifiedName("FooService");
            var index = k.getAllIndices().get(0);
            var bean = instCtx.selectFirstByKey(new IndexKeyRT(
                index,
                Map.of(
                     index.getFields().get(0),
                     Instances.trueInstance()
                )
            ));
            Assert.assertNotNull(bean);
            fooServiceId = bean.getId();
        }
        assemble("bean_ddl_after.masm");
        try(var context = newContext()) {
            var instCtx = context.getInstanceContext();
            var fooService = (ClassInstance) instCtx.get(fooServiceId);
            Assert.assertTrue(fooService.getField("barService").isNotNull());
            Assert.assertTrue(fooService.getField("idService").isNotNull());
        }
    }

    public void testEnumAddField() {
        assemble("enum_add_field_before.masm");
        assemble("enum_add_field_after.masm");
        try(var context = newContext()) {
            var currencyKlass = context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, "Currency");
            Assert.assertNotNull(currencyKlass);
            var sft = StaticFieldTable.getInstance(currencyKlass.getType(), context);
            var yuan = sft.getEnumConstants().get(0);
            Assert.assertEquals(Instances.doubleInstance(0.14), yuan.getField("rate"));
        }
    }

    public void testCustomRunner() {
        assemble("custom_runner_before.masm");
        var fieldId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Field", Map.of(
                "name", "count"
        )));
        TestUtils.doInTransaction(() -> apiClient.callMethod(fieldId, "setValue", List.of(1)));
        var value = apiClient.getObject(fieldId).get("value");
        Assert.assertEquals(1, value);
        assemble("custom_runner_after.masm");
        var value1 = TestUtils.doInTransaction(() -> apiClient.callMethod("lab", "getFieldValue", List.of(fieldId)));
        Assert.assertEquals(value, value1);
    }

    public void testAddIndex() {
        assemble("add_index_before.masm");
        var id = saveInstance("Product", Map.of("name", "Shoes"));
        assemble("add_index_after.masm");
        Assert.assertEquals(id, callMethod("productService", "findByName", List.of("Shoes")));
    }

    private void assemble(String fileName) {
        assemble(fileName, true);
    }

    private String assemble(String fileName, boolean waitForDDLCompleted) {
        return MockUtils.assemble(SRC_DIR + fileName, typeManager, waitForDDLCompleted, schedulerAndWorker);
    }

    private String saveInstance(String className, Map<String, Object> value) {
        return TestUtils.doInTransaction(() -> apiClient.saveInstance(className, value));
    }

    private Object callMethod(String qualifier, String methodName,  List<Object> arguments) {
        return TestUtils.doInTransaction(() -> apiClient.callMethod(qualifier, methodName, arguments));
    }

    private void doInContext(Consumer<IEntityContext> action) {
        TestUtils.doInTransactionWithoutResult(() -> {
            try(var context = newContext()) {
                action.accept(context);
                context.finish();
            }
        });
    }

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID);
    }

}
