package org.manul.task;

import junit.framework.TestCase;
import org.manul.application.ApplicationManager;
import org.manul.application.rest.dto.ApplicationDTO;
import org.manul.entity.EntityContextFactory;
import org.manul.mocks.ReindexFoo;
import org.manul.util.*;

import java.util.ArrayList;

public class GlobalReindexTaskTest extends TestCase {

    private ApplicationManager applicationManager;
    private EntityContextFactory entityContextFactory;
    private SchedulerAndWorker schedulerAndWorker;

    @Override
    protected void setUp() throws Exception {
        var r = BootstrapUtils.bootstrap();
        applicationManager = r.applicationManager();
        entityContextFactory = r.entityContextFactory();
        schedulerAndWorker = r.schedulerAndWorker();
    }

    public void test() {
        var appIds = new ArrayList<Long>();
        TestUtils.doInTransactionWithoutResult(() -> {
            var n = 10;
            for (int i = 0; i < n; i++) {
                var appId =applicationManager.save(new ApplicationDTO(null, "reindex-app-" + i, TestConstants.USER_ID.toString()));
                appIds.add(appId);
            }
            var i = 0;
            for (Long appId : appIds) {
                try (var context = entityContextFactory.newContext(appId)) {
                    context.bind(new ReindexFoo(context.allocateRootId(), "foo-" + i++));
                    context.finish();
                }
            }
        });
        var i = 0;
        for (Long appId : appIds) {
            try (var context = entityContextFactory.newContext(appId)) {
                var foo = context.selectFirstByKey(ReindexFoo.NAME_IDX, Instances.stringInstance("foo-" + i++));
                assertNull(foo);
            }
        }
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                context.bind(new GlobalReindexTask(context.allocateRootId(), "Global Reindex"));
                context.finish();
            }
        });
        ReindexFoo.indexOn = true;
        TestUtils.waitForTaskDone(schedulerAndWorker.scheduler(), schedulerAndWorker.worker(), t -> t instanceof GlobalReindexTask);
        for (;;) {
            try {
                TestUtils.waitForTaskDone(schedulerAndWorker.scheduler(), schedulerAndWorker.worker(), t -> t instanceof ReindexTask);
            } catch (IllegalStateException ignored) {
                // Wait until all tasks are done
                break;
            }
        }
        i = 0;
        for (Long appId : appIds) {
            try (var context = entityContextFactory.newContext(appId)) {
                var foo = context.selectFirstByKey(ReindexFoo.NAME_IDX, Instances.stringInstance("foo-" + i++));
                assertNotNull(foo);
            }
        }
    }

}