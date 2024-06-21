package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.ApiService;
import org.metavm.task.AddFieldTask;
import org.metavm.task.DirectTaskRunner;
import org.metavm.task.Scheduler;
import org.metavm.task.Worker;
import org.metavm.util.*;

import java.util.Map;

public class DDLTest extends TestCase {

    private TypeManager typeManager;
    private EntityContextFactory entityContextFactory;
    private ApiClient apiClient;
    private Scheduler scheduler;
    private Worker worker;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var commonManagers = TestUtils.createCommonManagers(bootResult);
        typeManager = commonManagers.typeManager();
        entityContextFactory = bootResult.entityContextFactory();
        apiClient = new ApiClient(new ApiService(bootResult.entityContextFactory()));
        var transactionOps = new MockTransactionOperations();
        scheduler = new Scheduler(entityContextFactory, transactionOps);
        worker = new Worker(entityContextFactory, transactionOps, new DirectTaskRunner());
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
        TestUtils.waitForTaskDone(scheduler, worker, t -> {
            if(t instanceof AddFieldTask addFieldTask)
                return addFieldTask.getField().getName().equals("version");
            else
                return false;
        });
        var product = apiClient.getInstance(id);
        Assert.assertEquals(0L, product.get("version"));
    }

}
