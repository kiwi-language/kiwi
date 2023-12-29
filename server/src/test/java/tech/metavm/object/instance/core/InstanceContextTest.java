package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.event.EventQueue;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.cache.MockCache;
import tech.metavm.object.type.ClassBuilder;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.util.Instances;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.TestConstants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InstanceContextTest extends TestCase {

    private EntityRepository entityRepository;
    private IInstanceStore instanceStore;
    private TypeProviders typeProviders;
    private Cache cache;
    private EventQueue eventQueue;
    private EntityIdProvider idProvider;
    private Executor executor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockStandardTypesInitializer.init();
        instanceStore = new MemInstanceStore();
        entityRepository = new MockEntityRepository();
        typeProviders = new TypeProviders();
        cache = new MockCache();
        eventQueue = new MockEventQueue();
        idProvider = new MockIdProvider();
        executor = Executors.newSingleThreadExecutor();
    }

    private IInstanceContext newContext() {
        return new InstanceContext(
                TestConstants.APP_ID,
                instanceStore,
                idProvider,
                executor,
                false,
                List.of(),
                null,
                entityRepository,
                entityRepository,
                typeProviders.parameterizedFlowProvider,
                false,
                cache,
                eventQueue,
                false);
    }

    public void test() {
        var fooType = ClassBuilder.newBuilder("Foo", "Foo").build();
        fooType.initId(101L);
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, StandardTypes.getStringType())
                .build();
        fooNameField.initId(111L);

        entityRepository.bind(fooType);
        long tmpId = 10001L;
        String name = "foo";
        long id;
        try (var context = newContext()) {
            var instance = ClassInstanceBuilder.newBuilder(fooType)
                    .tmpId(tmpId)
                    .data(Map.of(fooNameField, Instances.stringInstance(name)))
                    .build();
            context.bind(instance);
            Assert.assertSame(instance, context.get(RefDTO.fromTmpId(tmpId)));
            context.finish();
            id = instance.getIdRequired();
        }
        try (var context = newContext()) {
            var instance = (ClassInstance) context.get(id);
            Assert.assertEquals(Instances.stringInstance(name), instance.getField(fooNameField));
        }
    }

    public void testMapping() {

    }

}