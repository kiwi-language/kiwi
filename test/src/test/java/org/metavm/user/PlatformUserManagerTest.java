package org.metavm.user;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.common.MockEmailService;
import org.metavm.entity.EntityQueryService;
import org.metavm.event.MockEventQueue;
import org.metavm.object.instance.core.TmpId;
import org.metavm.user.rest.dto.RoleDTO;
import org.metavm.user.rest.dto.UserDTO;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;

import java.util.List;

import static org.metavm.util.TestUtils.doInTransaction;

public class PlatformUserManagerTest extends TestCase {

    private PlatformUserManager platformUserManager;
    private RoleManager roleManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var entityContextFactory = bootResult.entityContextFactory();
        var loginService = new LoginService(entityContextFactory);
        var entityQueryService = new EntityQueryService(bootResult.instanceSearchService());
        platformUserManager = new PlatformUserManager(entityContextFactory,  loginService, entityQueryService, new MockEventQueue(),
                new VerificationCodeService(entityContextFactory, new MockEmailService()));
        roleManager = new RoleManager(entityContextFactory, entityQueryService);
        ContextUtil.setAppId(Constants.PLATFORM_APP_ID);
    }

    @Override
    protected void tearDown() throws Exception {
        platformUserManager = null;
        roleManager = null;
    }

    public void testSave() {
        var roleId = doInTransaction(() -> roleManager.save(new RoleDTO(TmpId.randomString(), "admin")));
        UserDTO user = new UserDTO(
                null, "leen", "Twodogs Li", "123456",
                List.of(roleId)
        );

        var userId = doInTransaction(() -> platformUserManager.save(user));
        UserDTO loadedUser = platformUserManager.get(userId);
        Assert.assertEquals(userId, loadedUser.id());
        Assert.assertEquals(user.name(), loadedUser.name());
        Assert.assertNull(loadedUser.password());
        Assert.assertEquals(user.roleIds(), loadedUser.roleIds());
    }


}