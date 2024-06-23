package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.ApiService;
import org.metavm.util.*;

import java.util.Map;

public class DDLTest extends TestCase {

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
        var id = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
              "name", "Shoes",
              "quantity", 100,
              "price", 100
        )));
        MockUtils.assemble("/Users/leen/workspace/object/test/src/test/resources/asm/ddl_after.masm", typeManager, entityContextFactory);
//        var hatId = TestUtils.doInTransaction(() -> apiClient.saveInstance("Product", Map.of(
//                "name", "Hat",
//                "quantity", 100,
//                "price", 20
//        )));
//        try(var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
//            var hat = (ClassInstance) context.getInstanceContext().get(Id.parse(hatId));
//            Assert.assertEquals(Instances.longInstance(0L), hat.getField("version"));
//        }
        var product = apiClient.getInstance(id);
        Assert.assertEquals(0L, product.get("version"));
    }

}
