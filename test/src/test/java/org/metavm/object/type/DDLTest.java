package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.task.ReferenceMarkingTask;
import org.metavm.task.ReferenceRedirectTask;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DDLTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DDLTest.class);

    private TypeManager typeManager;
    private EntityContextFactory entityContextFactory;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var commonManagers = TestUtils.createCommonManagers(bootResult);
        typeManager = commonManagers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        apiClient = new ApiClient(new ApiService(bootResult.entityContextFactory()));
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        typeManager = null;
        entityContextFactory = null;
        apiClient = null;
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
        TestUtils.waitForTaskDone(t -> t instanceof ReferenceMarkingTask, entityContextFactory);
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
        TestUtils.waitForTaskDone(t -> t instanceof ReferenceRedirectTask, entityContextFactory);
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

    private IEntityContext newContext() {
        return entityContextFactory.newContext(TestConstants.APP_ID, builder -> builder.asyncPostProcess(false));
    }

}
