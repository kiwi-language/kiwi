package tech.metavm.user;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.user.rest.dto.RoleDTO;
import tech.metavm.user.rest.dto.UserDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.List;

public class UserManagerTest extends TestCase {

    private UserManager userManager;
    private RoleManager roleManager;

    @Override
    protected void setUp() throws Exception {
        MockIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);

        ContextUtil.setLoginInfo(1L, 1L);

        InstanceSearchService instanceSearchService = new MemInstanceSearchService();
        IInstanceStore instanceStore = new MemInstanceStore();
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore)
                .setIdService(idProvider);

        InstanceContextFactory.setStdContext(MockRegistry.getInstanceContext());

        instanceContextFactory.setPlugins(List.of(
                new IndexConstraintPlugin(new MemIndexEntryMapper()),
                new CheckConstraintPlugin(),
                new ChangeLogPlugin(
                        new InstanceLogServiceImpl(instanceSearchService, instanceContextFactory, instanceStore)
                )
        ));

        InstanceQueryService instanceQueryService = new InstanceQueryService(instanceSearchService);

        userManager = new UserManager(instanceQueryService, instanceContextFactory);
        roleManager = new RoleManager(instanceContextFactory, instanceQueryService);
    }

    public void testSave() {
        long roleId = roleManager.save(new RoleDTO(null, "admin"));

        UserDTO user = new UserDTO(
                null, "leen", "Twodogs Li", "123456",
                List.of(roleId)
        );

        long userId = userManager.save(user);
        UserDTO loadedUser = userManager.get(userId);
        Assert.assertEquals(userId, (long) loadedUser.id());
        Assert.assertEquals(user.name(), loadedUser.name());
        Assert.assertNull(loadedUser.password());
        Assert.assertEquals(user.roleIds(), loadedUser.roleIds());
    }


}