package org.metavm.application;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.application.rest.dto.ApplicationCreateRequest;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.common.MockEmailService;
import org.metavm.entity.EntityQueryService;
import org.metavm.event.MockEventQueue;
import org.metavm.object.instance.core.Id;
import org.metavm.user.LoginService;
import org.metavm.user.PlatformUserManager;
import org.metavm.user.RoleManager;
import org.metavm.user.VerificationCodeService;
import org.metavm.util.*;

@Slf4j
public class ApplicationManagerTest extends TestCase {

    private ApplicationManager applicationManager;
    private SchedulerAndWorker schedulerAndWorker;
    private PlatformUserManager platformUserManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        schedulerAndWorker = bootResult.schedulerAndWorker();
        var entityQueryService = new EntityQueryService(bootResult.instanceSearchService());
        var verificationCodeService = new VerificationCodeService(bootResult.entityContextFactory(), new MockEmailService());
        var loginService = new LoginService(bootResult.entityContextFactory());
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
        platformUserManager = new PlatformUserManager(
                bootResult.entityContextFactory(),
                loginService,
                entityQueryService,
                new MockEventQueue(),
                verificationCodeService
        );
        ContextUtil.setAppId(Constants.PLATFORM_APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        applicationManager = null;
        schedulerAndWorker = null;
        platformUserManager = null;
    }

    public void test() {
        var id = TestUtils.doInTransaction(() ->
                applicationManager.createBuiltin(new ApplicationCreateRequest(
                        "metavm", "metavm","123456", null
        ))).appId();
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        var page = applicationManager.list(1, 20, "metavm");
        Assert.assertEquals(1, page.items().size());
        Assert.assertEquals(id, (long) page.items().getFirst().id());

        var page1 = applicationManager.list(1, 20, null);
        Assert.assertEquals(2, page1.items().size());
    }

    public void testDelete() {
        TestUtils.waitForEsSync(schedulerAndWorker);
        var userId = platformUserManager.list(1, 20, null).items().getFirst().id();
        var id = TestUtils.doInTransaction(() ->
                applicationManager.createBuiltin(new ApplicationCreateRequest(
                        "metavm", "metavm","123456", userId
                ))).appId();
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        ContextUtil.setUserId(Id.parse(userId));
        TestUtils.doInTransactionWithoutResult(() -> applicationManager.delete(id));
        var r = applicationManager.list(1, 20, "metavm");
        assertEquals(0, r.total());
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        TestUtils.waitForEsSync(schedulerAndWorker);
        try {
            applicationManager.get(id);
            fail("Should have been removed");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
        var r1 = applicationManager.list(1, 20, "metavm");
        assertEquals(0, r1.total());
    }

    public void testSearchWithMultipleNewlyCreated() {
        ContextUtil.setUserId(Id.parse(getUserId()));
        for (int i = 0; i < 3; i++) {
            var i_ = i;
            TestUtils.doInTransaction(() ->
                    applicationManager.save(
                            new ApplicationDTO(
                                    null, "leen-" + i_, null
                            )
                    ));
        }
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = applicationManager.list(1, 2, null);
        assertEquals(2, r.items().size());
    }

    public void testSearchSecondPage() {
        ContextUtil.setUserId(Id.parse(getUserId()));
        for (int i = 0; i < 8; i++) {
            var i_ = i;
            TestUtils.doInTransaction(() ->
                    applicationManager.save(
                            new ApplicationDTO(
                                    null, "leen-" + i_, null
                            )
                    ));
        }
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = applicationManager.list(2, 5, null);
        assertEquals(9, r.total());
    }

    public void testSearchText() {
        TestUtils.doInTransaction(() ->
                applicationManager.createBuiltin(new ApplicationCreateRequest(
                        "metavm app", "metavm","123456", getUserId()
                )));
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = applicationManager.list(1, 20, "metavm");
        assertEquals(1, r.total());
        assertEquals("metavm app", r.items().getFirst().name());
    }

    private String getUserId() {
        TestUtils.waitForEsSync(schedulerAndWorker);
        return platformUserManager.list(1, 20, null).items().getFirst().id();
    }

}
