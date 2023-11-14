package tech.metavm.object.instance.log;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.List;

import static tech.metavm.util.TestConstants.TENANT_ID;

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
        instanceStore.save(ChangeList.inserts(List.of(fooInstance.toPO(TENANT_ID))));

        List<InstanceLog> logs = new ArrayList<>();
        logs.add(new InstanceLog(
                TENANT_ID, fooInstance.getId(), ChangeType.INSERT, 1L
        ));

        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore);
        instanceContextFactory.setIdService(idProvider);

        MemInstanceSearchService instanceSearchService = new MemInstanceSearchService();
        InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                instanceSearchService, instanceContextFactory, instanceStore
        );

        instanceLogService.process(logs);
        Assert.assertTrue(instanceSearchService.contains(fooInstance.getId()));
    }

}