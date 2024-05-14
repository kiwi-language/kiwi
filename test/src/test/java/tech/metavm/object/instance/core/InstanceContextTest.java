package tech.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.event.EventQueue;
import tech.metavm.event.MockEventQueue;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.cache.Cache;
import tech.metavm.object.instance.cache.MockCache;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.type.rest.dto.ClassTypeKey;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InstanceContextTest extends TestCase {

    private EntityRepository entityRepository;
    private IInstanceStore instanceStore;
    private Cache cache;
    private EventQueue eventQueue;
    private EntityIdProvider idProvider;
    private Executor executor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockStandardTypesInitializer.init();
        instanceStore = new MemInstanceStore();
        entityRepository = new MockEntityRepository(new MemTypeRegistry());
        cache = new MockCache();
        eventQueue = new MockEventQueue();
        idProvider = new MockIdProvider();
        executor = Executors.newSingleThreadExecutor();
    }

    private IInstanceContext newContext() {
        return new InstanceContext(
                TestConstants.APP_ID,
                instanceStore,
                new DefaultIdInitializer(idProvider),
                executor,
                false,
                List.of(),
                null,
                entityRepository,
                entityRepository,
                false,
                cache,
                eventQueue, false);
    }

    public void test() {
        var fooType = ClassTypeBuilder.newBuilder("Foo", "Foo").build();
        fooType.initId(DefaultPhysicalId.ofObject(101L, 0L, TestUtils.mockClassTypeKey()));
        var fooNameField = FieldBuilder.newBuilder("name", "name", fooType, StandardTypes.getStringType())
                .build();
        fooNameField.initId(DefaultPhysicalId.ofObject(111L, 0L, TestUtils.mockClassTypeKey()));

        entityRepository.bind(fooType);
        var tmpId = TmpId.of(10001L);
        String name = "foo";
        Id id;
        try (var context = newContext()) {
            var instance = ClassInstanceBuilder.newBuilder(fooType.getType())
                    .id(tmpId)
                    .data(Map.of(fooNameField, Instances.stringInstance(name)))
                    .build();
            context.bind(instance);
            Assert.assertSame(instance, context.get(tmpId));
            context.finish();
            id = instance.tryGetId();
        }
        try (var context = newContext()) {
            var instance = (ClassInstance) context.get(id);
            Assert.assertEquals(Instances.stringInstance(name), instance.getField(fooNameField));
        }
    }

    public void testMapping() {

    }

    public void testOnChange() {
        Id fooId;
        Id bazId;
        var fooTypes = MockUtils.createFooTypes(true);
        EntityUtils.visitGraph(fooTypes.fooType(), object -> {
            if (object instanceof Entity entity && entity.isIdNotNull())
                entityRepository.bind(entity);
        });
        try (var context = newContext()) {
            var foo = MockUtils.createFoo(fooTypes);
            var bars = (ArrayInstance) foo.getField(fooTypes.fooBarsField());
            var bar001 = (DurableInstance) (bars.get(0));
            var baz = ClassInstanceBuilder.newBuilder(fooTypes.bazType().getType())
                    .data(Map.of(fooTypes.bazBarsField(), new ArrayInstance(fooTypes.barArrayType(), List.of(bar001))))
                    .build();
            context.bind(foo);
            context.bind(baz);
            context.finish();
            fooId = foo.tryGetId();
            bazId = baz.tryGetId();
        }
        TestUtils.doInTransactionWithoutResult(() -> {
            try (var context = newContext()) {
                var foo = (ClassInstance) context.get(fooId);
                var baz = (ClassInstance) context.get(bazId);
                var bars = (ArrayInstance) foo.getField(fooTypes.fooBarsField());
                var bar001 = (DurableInstance) (bars.get(0));
                baz.ensureLoaded();
                context.remove(bar001);
                final boolean[] onChangeCalled = new boolean[1];
                context.addListener(new ContextListener() {
                    @Override
                    public boolean onChange(Instance instance) {
                        if (instance == foo) {
                            bars.removeElement(bar001);
                            ((ArrayInstance) baz.getField(fooTypes.bazBarsField())).removeElement(bar001);
                            onChangeCalled[0] = true;
                            return true;
                        } else
                            return false;
                    }
                });
                context.finish();
                Assert.assertTrue(onChangeCalled[0]);
            }
        });
    }


}