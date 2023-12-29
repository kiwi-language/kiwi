package tech.metavm.user;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.MockEmailService;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.InstanceQueryService;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.user.rest.dto.RoleDTO;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestUtils;

import java.util.List;

public class PlatformUserManagerTest extends TestCase {

    private PlatformUserManager platformUserManager;
    private RoleManager roleManager;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);

        ContextUtil.setAppId(1L);
        ContextUtil.setUserId(1L);

        var instanceSearchService = new MemInstanceSearchService();
        var instanceStore = new MemInstanceStore();
        InstanceContextFactory.setDefContext(MockRegistry.getDefContext());

        var instanceQueryService = new InstanceQueryService(instanceSearchService);
        var entityQueryService = new EntityQueryService(instanceQueryService);
        var entityContextFactory = TestUtils.getEntityContextFactory(idProvider, instanceStore, new MockInstanceLogService(), new MemIndexEntryMapper());
        var loginService = new LoginService(entityContextFactory);
        platformUserManager = new PlatformUserManager(entityContextFactory,  loginService, new EntityQueryService(instanceQueryService), new MockEventQueue(),
                new VerificationCodeService(entityContextFactory, new MockEmailService()));
        roleManager = new RoleManager(entityContextFactory, entityQueryService);
    }

    public void testSave() {
        long roleId = roleManager.save(new RoleDTO(null, null, "admin"));

        UserDTO user = new UserDTO(
                null, "leen", "Twodogs Li", "123456",
                List.of(RefDTO.fromId(roleId))
        );

        long userId = platformUserManager.save(user);
        UserDTO loadedUser = platformUserManager.get(userId);
        Assert.assertEquals(userId, (long) loadedUser.id());
        Assert.assertEquals(user.name(), loadedUser.name());
        Assert.assertNull(loadedUser.password());
        Assert.assertEquals(user.roleRefs(), loadedUser.roleRefs());
    }


}