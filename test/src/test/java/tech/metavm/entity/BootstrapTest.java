package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.instance.cache.MockCache;
import tech.metavm.object.type.*;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.TestUtils;

import java.util.Set;

public class BootstrapTest extends TestCase {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BootstrapTest.class);

    private ColumnStore columnStore;
    private MemAllocatorStore allocatorStore;
    private InstanceStore instanceStore;
    private EntityIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        allocatorStore = new MemAllocatorStore();
        columnStore = new MemColumnStore();
        instanceStore = new MemInstanceStore();
        idProvider = new MockIdProvider();
    }

    @Override
    protected void tearDown() {
        ContextUtil.clearContextInfo();
        columnStore = null;
        allocatorStore = null;
        instanceStore = null;
        idProvider = null;
    }

    private Bootstrap newBootstrap() {
        ModelDefRegistry.setDefContext(null);
        var stdAllocators = new StdAllocators(allocatorStore);
        var eventQueue = new MockEventQueue();
        var indexEntryMapper = new MemIndexEntryMapper();
        var instanceContextFactory = new InstanceContextFactory(instanceStore, eventQueue);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, indexEntryMapper);
        instanceContextFactory.setIdService(idProvider);
        instanceContextFactory.setCache(new MockCache());
        entityContextFactory.setInstanceLogService(new MockInstanceLogService());
        return new Bootstrap(entityContextFactory, stdAllocators, columnStore);
    }

    public void test() {
        {
            var bootstrap = newBootstrap();
            var result = bootstrap.boot();
            Assert.assertTrue(result.numInstancesWithNullIds() > 0);
            TestUtils.beginTransaction();
            bootstrap.save(true);
            TestUtils.commitTransaction();
        }
        {
            var bootstrap = newBootstrap();
            var result = bootstrap.boot();
            Assert.assertEquals(0, result.numInstancesWithNullIds());
            TestUtils.beginTransaction();
            bootstrap.save(true);
            TestUtils.commitTransaction();
        }
        // test remove field
        {
            var bootstrap = newBootstrap();
            bootstrap.setFieldBlacklist(Set.of(ReflectionUtils.getDeclaredField(Type.class, "dummyFlag")));
            var result = bootstrap.boot();
            Assert.assertEquals(0, result.numInstancesWithNullIds());
            TestUtils.beginTransaction();
            bootstrap.save(true);
            TestUtils.commitTransaction();
        }
    }

    public void testPerf() {
        {
            var profiler = ContextUtil.getProfiler();
            var bootstrap = newBootstrap();
            bootstrap.boot();
            LOGGER.info(profiler.finish().toString());
        }
    }

}