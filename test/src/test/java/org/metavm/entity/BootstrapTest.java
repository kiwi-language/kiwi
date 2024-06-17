package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.api.Value;
import org.metavm.event.MockEventQueue;
import org.metavm.object.instance.InstanceStore;
import org.metavm.object.instance.MockInstanceLogService;
import org.metavm.object.instance.cache.MockCache;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.StructuralVisitor;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;

import java.util.Set;

import static org.metavm.util.TestUtils.doInTransactionWithoutResult;

public class BootstrapTest extends TestCase {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BootstrapTest.class);

    private ColumnStore columnStore;
    private TypeTagStore typeTagStore;
    private StdIdStore stdIdStore;
    private MemAllocatorStore allocatorStore;
    private InstanceStore instanceStore;
    private EntityIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        allocatorStore = new MemAllocatorStore();
        columnStore = new MemColumnStore();
        typeTagStore = new MemTypeTagStore();
        stdIdStore = new MemoryStdIdStore();
        instanceStore = new MemInstanceStore();
        idProvider = new MockIdProvider();
    }

    @Override
    protected void tearDown() {
        ContextUtil.clearContextInfo();
        columnStore = null;
        typeTagStore = null;
        stdIdStore = null;
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
        return new Bootstrap(entityContextFactory, stdAllocators, columnStore, typeTagStore, stdIdStore);
    }

    public void test() {
        {
            ContextUtil.resetProfiler();
            var profiler = ContextUtil.getProfiler();
            var bootstrap = newBootstrap();
            var result = bootstrap.boot();
            Assert.assertTrue(result.numInstancesWithNullIds() > 0);
            TestUtils.doInTransactionWithoutResult(() -> bootstrap.save(true));
            LOGGER.info(profiler.finish(false, true).toString());
        }
//        allocatorStore.dump();
//        DebugEnv.bootstrapVerbose = true;
        {
            ContextUtil.resetProfiler();
            var profiler = ContextUtil.getProfiler();
            var bootstrap = newBootstrap();
            var result = bootstrap.boot();
            Assert.assertEquals(0, result.numInstancesWithNullIds());
            TestUtils.doInTransactionWithoutResult(() -> bootstrap.save(true));
            LOGGER.info(profiler.finish(false, true).toString());
        }
        // test remove field
        {
            ContextUtil.resetProfiler();
            var profiler = ContextUtil.getProfiler();
            var bootstrap = newBootstrap();
            bootstrap.setFieldBlacklist(Set.of(ReflectionUtils.getDeclaredField(Klass.class, "dummyFlag")));
            var result = bootstrap.boot();
            Assert.assertEquals(0, result.numInstancesWithNullIds());
            TestUtils.doInTransactionWithoutResult(() -> bootstrap.save(true));
            LOGGER.info(profiler.finish(false, true).toString());
        }
        {
            ContextUtil.resetProfiler();
            var profiler = ContextUtil.getProfiler();
            var originalDefContext = ModelDefRegistry.getDefContext();
            var entities = NncUtils.filter(originalDefContext.getEntities(), e -> !EntityUtils.isEphemeral(e) && !(e instanceof Value));
            var modelIds = NncUtils.map(entities, e -> originalDefContext.getIdentityContext().getModelId(e));
            var originalIds = NncUtils.map(entities, e -> originalDefContext.getInstance(e).tryGetId());
            stdIdStore = new MemoryStdIdStore();
            instanceStore = new MemInstanceStore();
            idProvider = new MockIdProvider();
            var bootstrap = newBootstrap();
            doInTransactionWithoutResult(bootstrap::bootAndSave);
            var defContext = ModelDefRegistry.getDefContext();
            Assert.assertEquals(
                    originalIds,
                    NncUtils.map(
                            modelIds,
                            modelId ->
                                    defContext.getInstance(defContext.getIdentityContext().getModel(modelId)).tryGetId()
                    )
            );
            LOGGER.info(profiler.finish(false, true).toString());
        }
    }

    public void testPerf() {
        {
            ContextUtil.resetProfiler();
            var profiler = ContextUtil.getProfiler();
            var bootstrap = newBootstrap();
            bootstrap.boot();
            LOGGER.info(profiler.finish().toString());
        }
    }

    public void testBootWithExistingIdFiles() {
        var bootstrap1 = newBootstrap();
        bootstrap1.boot();
        TestUtils.doInTransactionWithoutResult(() -> bootstrap1.save(true));
        instanceStore = new MemInstanceStore();
        idProvider = new MockIdProvider();
        stdIdStore = new MemoryStdIdStore();
        var bootstrap2 = newBootstrap();
        bootstrap2.boot();
        TestUtils.doInTransactionWithoutResult(() -> bootstrap2.save(true));
        for (var instance : ModelDefRegistry.getDefContext().getInstanceContext()) {
            if (!instance.isEphemeral() && instance.isRoot()) {
                var ref = new Object() {
                    long maxNodeId;
                };
                instance.accept(new StructuralVisitor() {

                    @Override
                    public Void visitDurableInstance(DurableInstance instance) {
                        if (instance.tryGetId() instanceof PhysicalId id)
                            ref.maxNodeId = Math.max(id.getNodeId(), ref.maxNodeId);
                        return super.visitDurableInstance(instance);
                    }
                });
                Assert.assertEquals(instance.getNextNodeId(), ref.maxNodeId + 1L);
            }
        }
    }

}