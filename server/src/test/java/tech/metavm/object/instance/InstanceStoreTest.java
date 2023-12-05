package tech.metavm.object.instance;

import junit.framework.TestCase;
import tech.metavm.entity.MemIndexEntryMapper;
import tech.metavm.entity.MemInstanceContext;
import tech.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import tech.metavm.object.instance.persistence.mappers.MemReferenceMapper;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import static tech.metavm.util.TestConstants.APP_ID;

public class InstanceStoreTest extends TestCase {

    private MemInstanceContext context;
    private InstanceStore instanceStore;
    private MemInstanceMapper instanceMapper;
    private MockIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        instanceMapper = new MemInstanceMapper();
        instanceStore = new InstanceStore(instanceMapper, new MemIndexEntryMapper(), new MemReferenceMapper());
        context = new MemInstanceContext(APP_ID, idProvider, instanceStore, null);
        context.setTypeProvider(MockRegistry::getType);
    }


}