package org.metavm.application;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.application.rest.dto.ApplicationCreateRequest;
import org.metavm.application.rest.dto.ApplicationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.common.MockEmailService;
import org.metavm.entity.EntityQueryService;
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
        var searchService = bootResult.instanceSearchService();
        var entityQueryService = new EntityQueryService(searchService);
        var verificationCodeService = new VerificationCodeService(bootResult.entityContextFactory(), new MockEmailService());
        var loginService = new LoginService(bootResult.entityContextFactory());
        applicationManager = new ApplicationManager(
                bootResult.entityContextFactory(),
                new RoleManager(bootResult.entityContextFactory(), entityQueryService),
                new PlatformUserManager(
                        bootResult.entityContextFactory(),
                        new LoginService(bootResult.entityContextFactory()),
                        entityQueryService,
                        verificationCodeService
                ),
                verificationCodeService,
                bootResult.idProvider(),
                entityQueryService,
                bootResult.schemaManager(),
                searchService
        );
        platformUserManager = new PlatformUserManager(
                bootResult.entityContextFactory(),
                loginService,
                entityQueryService,
                verificationCodeService
        );
        ContextUtil.setAppId(Constants.PLATFORM_APP_ID);
        ContextUtil.setUserId(bootResult.userId());
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
                        "metavm", "metavm","123456", ContextUtil.getUserId().toString()
        ))).appId();
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        var page = applicationManager.list(1, 20, "metavm", ContextUtil.getUserId(), null);
        Assert.assertEquals(1, page.items().size());
        Assert.assertEquals(id, (long) page.items().getFirst().id());

        var page1 = applicationManager.list(1, 20, null, ContextUtil.getUserId(),null);
        Assert.assertEquals(2, page1.items().size());
    }

    public void testDelete() {
        var userId = getUserId();
        ContextUtil.setUserId(Id.parse(userId));
        var id = TestUtils.doInTransaction(() ->
                applicationManager.save(new ApplicationDTO(null, "metavm", ContextUtil.getUserId().toString())));
//        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = applicationManager.list(1, 20, "metavm", ContextUtil.getUserId(),null);
        assertEquals(1, r.total());
        TestUtils.doInTransactionWithoutResult(() -> applicationManager.delete(id));
        var r1 = applicationManager.list(1, 20, "metavm", ContextUtil.getUserId(),null);
        assertEquals(0, r1.total());
        TestUtils.waitForAllTasksDone(schedulerAndWorker);
        try {
            applicationManager.get(id);
            fail("Should have been removed");
        }
        catch (BusinessException e) {
            assertSame(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
        var r2 = applicationManager.list(1, 20, "metavm", ContextUtil.getUserId(),null);
        assertEquals(0, r2.total());
    }

    public void testSearchWithMultipleNewlyCreated() {
        ContextUtil.setUserId(Id.parse(getUserId()));
        for (int i = 0; i < 3; i++) {
            var i_ = i;
            TestUtils.doInTransaction(() ->
                    applicationManager.save(
                            new ApplicationDTO(
                                    null, "leen-" + i_, ContextUtil.getUserId().toString()
                            )
                    ));
        }
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = applicationManager.list(1, 2, null, ContextUtil.getUserId(),null);
        assertEquals(2, r.items().size());
    }

    public void testSearchSecondPage() {
        ContextUtil.setUserId(Id.parse(getUserId()));
        for (int i = 0; i < 8; i++) {
            var i_ = i;
            TestUtils.doInTransaction(() ->
                    applicationManager.save(
                            new ApplicationDTO(
                                    null, "leen-" + i_, ContextUtil.getUserId().toString()
                            )
                    ));
        }
        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = applicationManager.list(2, 5, null, ContextUtil.getUserId(),null);
        assertEquals(9, r.total());
    }

    public void testSearchText() {
        var id = TestUtils.doInTransaction(() ->
                applicationManager.createBuiltin(new ApplicationCreateRequest(
                        "Metavm App", "metavm","123456", getUserId()
                ))).appId();
        var r0 = applicationManager.list(1, 20, "metavm", ContextUtil.getUserId(), id);
        assertEquals(1, r0.total());

        TestUtils.waitForEsSync(schedulerAndWorker);
        var r = applicationManager.list(1, 20, "metavm", ContextUtil.getUserId(),null);
        assertEquals(1, r.total());
        assertEquals("Metavm App", r.items().getFirst().name());

        var r1 = applicationManager.list(1, 20, "meta", ContextUtil.getUserId(),null);
        assertEquals(1, r1.total());
    }

    private String getUserId() {
        TestUtils.waitForEsSync(schedulerAndWorker);
        return platformUserManager.list(1, 20, null).items().getFirst().id();
    }

}
