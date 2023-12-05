package tech.metavm.user;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.MockEmailService;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.EntityQueryService;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.user.rest.dto.RoleDTO;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

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

        InstanceSearchService instanceSearchService = new MemInstanceSearchService();
        IInstanceStore instanceStore = new MemInstanceStore();
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore, new MockEventQueue())
                .setIdService(idProvider);

        InstanceContextFactory.setStdContext(MockRegistry.getInstanceContext());

        instanceContextFactory.setPlugins(List.of(
                new IndexConstraintPlugin(new MemIndexEntryMapper()),
                new CheckConstraintPlugin(),
                new ChangeLogPlugin(
                        new InstanceLogServiceImpl(instanceSearchService, instanceContextFactory, instanceStore, List.of())
                )
        ));

        InstanceQueryService instanceQueryService = new InstanceQueryService(instanceSearchService);
        var loginService = new LoginService(instanceContextFactory);
        platformUserManager = new PlatformUserManager(instanceQueryService, instanceContextFactory, loginService, new EntityQueryService(instanceQueryService), new MockEventQueue(),
                new VerificationCodeService(instanceContextFactory, new MockEmailService()));
        roleManager = new RoleManager(instanceContextFactory, instanceQueryService);
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