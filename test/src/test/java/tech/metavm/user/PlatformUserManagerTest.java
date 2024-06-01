package tech.metavm.user;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.MockEmailService;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.user.rest.dto.RoleDTO;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;

import java.util.List;

import static tech.metavm.util.TestUtils.doInTransaction;

public class PlatformUserManagerTest extends TestCase {

    private PlatformUserManager platformUserManager;
    private RoleManager roleManager;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var entityContextFactory = bootResult.entityContextFactory();
        var loginService = new LoginService(entityContextFactory);
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        var entityQueryService = new EntityQueryService(instanceQueryService);
        platformUserManager = new PlatformUserManager(entityContextFactory,  loginService, new EntityQueryService(instanceQueryService), new MockEventQueue(),
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
        var roleId = doInTransaction(() -> roleManager.save(new RoleDTO(null, "admin")));
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