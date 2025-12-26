//package org.metavm.entity;
//
//import junit.framework.TestCase;
//import org.hamcrest.CoreMatchers;
//import org.hamcrest.MatcherAssert;
//import org.junit.Assert;
//import org.metavm.api.ValueObject;
//import org.metavm.flow.Function;
//import org.metavm.object.instance.InstanceStore;
//import org.metavm.object.instance.MockInstanceLogService;
//import org.metavm.object.instance.cache.LocalCache;
//import org.metavm.object.instance.cache.MockCache;
//import org.metavm.object.instance.core.Instance;
//import org.metavm.object.type.*;
//import org.metavm.util.ContextUtil;
//import org.metavm.util.Instances;
//import org.metavm.util.MockIdProvider;
//import org.metavm.util.Utils;
//import org.slf4j.Logger;
//
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//
//import static org.metavm.util.TestUtils.doInTransactionWithoutResult;
//
//public class BootstrapTest extends TestCase {
//
//    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(BootstrapTest.class);
//
//    private ColumnStore columnStore;
//    private TypeTagStore typeTagStore;
//    private MemoryStdIdStore stdIdStore;
//    private MemAllocatorStore allocatorStore;
//    private InstanceStore instanceStore;
//    private EntityIdProvider idProvider;
//
//    @Override
//    protected void setUp() throws Exception {
//        allocatorStore = new MemAllocatorStore();
//        columnStore = new MemColumnStore();
//        typeTagStore = new MemTypeTagStore();
//        stdIdStore = new MemoryStdIdStore();
//        instanceStore = new MemInstanceStore(new LocalCache());
//        idProvider = new MockIdProvider();
//    }
//
//    @Override
//    protected void tearDown() {
//        ContextUtil.clearContextInfo();
//        columnStore = null;
//        typeTagStore = null;
//        stdIdStore = null;
//        allocatorStore = null;
//        instanceStore = null;
//        idProvider = null;
//    }
//
//    private Bootstrap newBootstrap() {
//        ModelDefRegistry.setDefContext(null);
//        var stdAllocators = new StdAllocators(allocatorStore);
//        var eventQueue = new MockEventQueue();
//        var indexEntryMapper = new MemIndexEntryMapper();
//        var instanceContextFactory = new InstanceContextFactory(instanceStore, eventQueue);
//        var entityContextFactory = new EntityContextFactory(instanceContextFactory, indexEntryMapper);
//        instanceContextFactory.setIdService(idProvider);
//        instanceContextFactory.setCache(new MockCache());
//        entityContextFactory.setInstanceLogService(new MockInstanceLogService());
//        return new Bootstrap(entityContextFactory, stdAllocators, columnStore, typeTagStore);
//    }
//
//    public void test() {
//        generateIds(allocatorStore);
//        {
//            ContextUtil.resetProfiler();
//            var profiler = ContextUtil.getProfiler();
//            var bootstrap = newBootstrap();
//            var result = bootstrap.boot();
//            Assert.assertEquals(0, result.numInstancesWithNullIds());
//            logger.info(profiler.finish(false, true).toString());
//            Assert.assertNotNull(
//                    result.defContext().selectFirstByKey(Function.UNIQUE_NAME, Instances.stringInstance("concat"))
//            );
//        }
////        allocatorStore.dump();
////        DebugEnv.bootstrapVerbose = true;
//        {
//            ContextUtil.resetProfiler();
//            var profiler = ContextUtil.getProfiler();
//            var bootstrap = newBootstrap();
//            var result = bootstrap.boot();
//            Assert.assertEquals(0, result.numInstancesWithNullIds());
//            logger.info(profiler.finish(false, true).toString());
//        }
//        // test remove field
//        {
//            ContextUtil.resetProfiler();
//            var profiler = ContextUtil.getProfiler();
//            var bootstrap = newBootstrap();
//            bootstrap.setFieldBlacklist(Set.of(ReflectionUtils.getDeclaredField(Klass.class, "dummyFlag")));
//            var result = bootstrap.boot();
//            Assert.assertEquals(0, result.numInstancesWithNullIds());
//            logger.info(profiler.finish(false, true).toString());
//        }
//        {
//            ContextUtil.resetProfiler();
//            var profiler = ContextUtil.getProfiler();
//            var originalDefContext = (SystemDefContext) ModelDefRegistry.getDefContext();
//            var entities = Utils.filter(originalDefContext.entities(), e -> !e.isEphemeral() && !(e instanceof ValueObject));
//            var modelIds = Utils.map(entities, e -> originalDefContext.getIdentityContext().getModelId(e));
//            var originalIds = Utils.map(entities, Instance::tryGetId);
//            stdIdStore = new MemoryStdIdStore();
//            instanceStore = new MemInstanceStore(new LocalCache());
//            idProvider = new MockIdProvider();
//            var bootstrap = newBootstrap();
//            doInTransactionWithoutResult(bootstrap::boot);
//            var defContext = (SystemDefContext) ModelDefRegistry.getDefContext();
//            Assert.assertEquals(
//                    originalIds,
//                    Utils.map(
//                            modelIds,
//                            modelId ->
//                                    Objects.requireNonNull(defContext.getIdentityContext().getModel(modelId),
//                                                    () -> "Failed to get model for identity " + modelId)
//                                            .tryGetId()
//                    )
//            );
//            logger.info(profiler.finish(false, true).toString());
//        }
//    }
//
//    public void testPrimitiveWrapper() {
//        generateIds(allocatorStore);
//        ContextUtil.resetProfiler();
//        var profiler = ContextUtil.getProfiler();
//        var bootstrap = newBootstrap();
//        var result = bootstrap.boot();
//        Assert.assertEquals(0, result.numInstancesWithNullIds());
//        logger.info(profiler.finish(false, true).toString());
//        Assert.assertNotNull(
//                result.defContext().selectFirstByKey(Function.UNIQUE_NAME, Instances.stringInstance("concat"))
//        );
//        var defContext = ModelDefRegistry.getDefContext();
//        var type = defContext.getType(Integer.class);
//        MatcherAssert.assertThat(type, CoreMatchers.instanceOf(ClassType.class));
//        Assert.assertEquals("Byte", defContext.getKlass(Byte.class).getName());
//        var mapKlass = defContext.getKlass(Map.class);
//        Assert.assertNotNull(mapKlass.findInnerKlass(k -> k.getName().equals("Entry")));
//    }
//
//    public void testPerf() {
//        generateIds(allocatorStore);
//        {
//            ContextUtil.resetProfiler();
//            var profiler = ContextUtil.getProfiler();
//            var bootstrap = newBootstrap();
//            bootstrap.boot();
//            logger.info(profiler.finish().toString());
//        }
//    }
//
//    public void testBootWithExistingIdFiles() {
//        generateIds(allocatorStore);
//        var bootstrap1 = newBootstrap();
//        bootstrap1.boot();
//        instanceStore = new MemInstanceStore(new LocalCache());
//        idProvider = new MockIdProvider();
//        stdIdStore = new MemoryStdIdStore();
//        var bootstrap2 = newBootstrap();
//        bootstrap2.boot();
////        for (var instance : ModelDefRegistry.getDefContext()) {
////            if (!instance.isEphemeral() && instance.isRoot()) {
////                var ref = new Object() {
////                    long maxNodeId;
////                };
////                instance.forEachDescendant(i -> {
////                    if (i.tryGetId() instanceof PhysicalId id)
////                        ref.maxNodeId = Math.max(id.getNodeId(), ref.maxNodeId);
////                });
////                Assert.assertEquals(instance.getNextNodeId(), ref.maxNodeId + 1L);
////            }
////        }
//    }
//
//    public void testReboot() {
//        generateIds(allocatorStore);
//        {
//            ContextUtil.resetProfiler();
//            var profiler = ContextUtil.getProfiler();
//            var bootstrap = newBootstrap();
//            var result = bootstrap.boot();
//            Assert.assertEquals(0, result.numInstancesWithNullIds());
//            logger.info(profiler.finish(false, true).toString());
//        }
//        {
//            var bootstrap = newBootstrap();
//            bootstrap.boot();
//            var defContext = ModelDefRegistry.getDefContext();
//            for (Entity entity : defContext.entities())
//                Assert.assertNotNull(entity.tryGetId());
//        }
//    }
//
//    private static void generateIds(AllocatorStore allocatorStore) {
//        var stdAllocators = new StdAllocators(allocatorStore);
//        var idGenerator = new StdIdGenerator(() -> stdAllocators.allocate(1).getFirst());
//        idGenerator.generate();
//
//        idGenerator.getIds().forEach((identity, id) -> {
//            if (id.getNodeId() == 0L)
//                stdAllocators.putId(identity, id, idGenerator.getNextNodeId(identity));
//            else
//                stdAllocators.putId(identity, id);
//        });
//        stdAllocators.save();
//    }
//
//
//}