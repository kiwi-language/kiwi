package tech.metavm.object.instance.log;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.MemInstanceStore;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.ChangeType;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.persistence.PersistenceUtils;
import tech.metavm.util.ChangeList;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.ArrayList;
import java.util.List;

import static tech.metavm.util.TestConstants.APP_ID;

public class InstanceLogServiceImplTest extends TestCase {

    private MockIdProvider idProvider;
    private IInstanceStore instanceStore;

    @Override
    protected void setUp() throws Exception {
        idProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider);
    }

    public void test() {
        Instance fooInstance = MockRegistry.getFooInstance();
        instanceStore.save(ChangeList.inserts(List.of(PersistenceUtils.toInstancePO(fooInstance, APP_ID))));

        List<InstanceLog> logs = new ArrayList<>();
        logs.add(new InstanceLog(
                APP_ID, fooInstance.getIdRequired(), fooInstance.getType().getIdRequired(),
                ChangeType.INSERT, 1L
        ));

        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore, new MockEventQueue());
        instanceContextFactory.setIdService(idProvider);

        MemInstanceSearchService instanceSearchService = new MemInstanceSearchService();
        InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                instanceSearchService, instanceContextFactory, instanceStore,
                List.of());

        instanceLogService.process(logs, null);
        Assert.assertTrue(instanceSearchService.contains(fooInstance.getIdRequired()));
    }

}