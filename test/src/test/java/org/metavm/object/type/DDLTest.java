package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
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
        var shoes = apiClient.getInstance(shoesId);
        var hat = apiClient.getInstance(hatId);
        //noinspection unchecked
        Assert.assertEquals(0L, ((Map<String, Object>) shoes.get("version")).get("majorVersion"));
        //noinspection unchecked
        Assert.assertEquals(0L, ((Map<String, Object>) hat.get("version")).get("majorVersion"));
        // check that index entries have been generated
        var foundId = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Shoes")));
        Assert.assertEquals(shoesId, foundId);
        var foundId2 = (String) TestUtils.doInTransaction(() -> apiClient.callMethod("Product", "findByName", List.of("Hat")));
        Assert.assertEquals(hatId, foundId2);
        var commitState = apiClient.getInstance((String) apiClient.getInstance(commitId).get("state"));
        Assert.assertEquals("FINISHED", commitState.get("name"));
    }

}
