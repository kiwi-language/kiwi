package org.metavm.racecondition;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.ApiService;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.type.TypeManager;
import org.metavm.util.*;

import java.util.List;

public class RaceConditionTest extends TestCase {

    private EntityContextFactory entityContextFactory;
    private TypeManager typeManager;
    private SchedulerAndWorker schedulerAndWorker;
    private ApiClient apiClient;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        entityContextFactory = bootResult.entityContextFactory();
        typeManager = TestUtils.createCommonManagers(bootResult).typeManager();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        apiClient = new ApiClient(new ApiService(entityContextFactory, bootResult.metaContextCache(),
                new InstanceQueryService(bootResult.instanceSearchService())));
    }

    @Override
    protected void tearDown() throws Exception {
        entityContextFactory = null;
        typeManager = null;
        schedulerAndWorker = null;
        apiClient = null;
    }

    public void test() throws InterruptedException {
        MockUtils.assemble("kiwi/race_condition.kiwi",
                typeManager, schedulerAndWorker);
        long size = (int) TestUtils.doInTransaction(() -> apiClient.callMethod("Utils", "size", List.of(
                List.of()
        )));
        Assert.assertEquals(0, size);
        var ref = new Object() {
            Throwable exception;
        };
        var t = new Thread(() -> {
            ContextUtil.setAppId(TestConstants.APP_ID);
            try {
                TestUtils.doInTransaction(() ->
                        apiClient.callMethod("Utils", "size", List.of(List.of()))
                );
            }
            catch (Throwable e) {
                ref.exception = e;
            }
        });
        t.start();
        t.join();
        Assert.assertNull(ref.exception);
    }

}
