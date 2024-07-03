package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.common.ErrorCode;
import org.metavm.ddl.Commit;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
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
                "quantity", 100,
                "price", 100
        )));
        var boxId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Box<Product>", Map.of(
                "item", shoesId
        )));
        var shoes = apiClient.getInstance(shoesId);
        Assert.assertEquals(100L, shoes.get("price"));
        var commitId = MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_after.masm", typeManager, false, entityContextFactory);
        var hatId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Hat",
                "quantity", 100,
                "price", 20
        )));
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var commit = context.getEntity(Commit.class, commitId);
            var klass = Objects.requireNonNull(context.selectFirstByKey(Klass.UNIQUE_CODE, "Product"));
            Assert.assertNull(klass.findFieldByCode("version"));
            try (var cachingContext = entityContextFactory.newLoadedContext(TestConstants.APP_ID, commit.getWal())) {
                var hat = (ClassInstance) cachingContext.getInstanceContext().get(Id.parse(hatId));
                var ver = (ClassInstance) hat.getField("version");
                Assert.assertEquals(Instances.longInstance(0L), ver.getField("majorVersion"));
            }
        }
        TestUtils.waitForDDLDone(entityContextFactory);
        var shoes1 = apiClient.getInstance(shoesId);
        var hat = apiClient.getInstance(hatId);
        Assert.assertEquals(true, shoes1.get("available"));
        Assert.assertNull(shoes1.get("description"));
        Assert.assertEquals(100.0, shoes1.get("price"));
        //noinspection unchecked
        Assert.assertEquals(0L, ((Map<String, Object>) shoes1.get("version")).get("majorVersion"));
        //noinspection unchecked
        Assert.assertEquals(0L, ((Map<String, Object>) hat.get("version")).get("majorVersion"));
        // check that index entries have been generated
        var foundId = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Shoes")));
        Assert.assertEquals(shoesId, foundId);
        var foundId2 = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Hat")));
        Assert.assertEquals(hatId, foundId2);
        var box = apiClient.getInstance(boxId);
        Assert.assertEquals(1L, box.get("count"));
        var commitState = apiClient.getInstance((String) apiClient.getInstance(commitId).get("state"));
        Assert.assertEquals("FINISHED", commitState.get("name"));
    }

    public void testDDLRollback() {
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_before.masm", typeManager, entityContextFactory);
        var shoesId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
                "name", "Shoes",
                "quantity", 100,
                "price", 100
        )));
        var shoes = apiClient.getInstance(shoesId);
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
}
