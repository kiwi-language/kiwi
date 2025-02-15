package org.metavm.application;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.application.rest.dto.ApplicationCreateRequest;
import org.metavm.common.MockEmailService;
import org.metavm.entity.EntityQueryService;
import org.metavm.event.MockEventQueue;
import org.metavm.user.LoginService;
import org.metavm.user.PlatformUserManager;
import org.metavm.user.RoleManager;
import org.metavm.user.VerificationCodeService;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.SchedulerAndWorker;
import org.metavm.util.TestUtils;

public class ApplicationManagerTest extends TestCase {

    private ApplicationManager applicationManager;
    private SchedulerAndWorker schedulerAndWorker;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        var entityQueryService = new EntityQueryService(bootResult.instanceSearchService());
        var verificationCodeService = new VerificationCodeService(bootResult.entityContextFactory(), new MockEmailService());
        applicationManager = new ApplicationManager(
                bootResult.entityContextFactory(),
                new RoleManager(bootResult.entityContextFactory(), entityQueryService),
                new PlatformUserManager(
                        bootResult.entityContextFactory(),
                        new LoginService(bootResult.entityContextFactory()),
                        entityQueryService,
                        new MockEventQueue(),
                        verificationCodeService
                ),
                verificationCodeService,
                bootResult.idProvider(),
                entityQueryService,
                bootResult.schemaManager()
        );
    }

    @Override
    protected void tearDown() throws Exception {
        applicationManager = null;
        schedulerAndWorker = null;
    }

    public void test() {
        var id = TestUtils.doInTransaction(() ->
                applicationManager.createBuiltin(new ApplicationCreateRequest(
                        "metavm", "metavm","123456", null
        ))).appId();
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        var page = applicationManager.list(1, 20, "metavm");
        Assert.assertEquals(1, page.data().size());
        Assert.assertEquals(id, (long) page.data().getFirst().id());

        var page1 = applicationManager.list(1, 20, null);
        Assert.assertEquals(2, page1.data().size());
    }

}
