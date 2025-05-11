package org.metavm.object.instance.core;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.entity.*;
import org.metavm.event.EventQueue;
import org.metavm.event.MockEventQueue;
import org.metavm.object.instance.IInstanceStore;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.instance.cache.MockCache;
import org.metavm.object.instance.persistence.MemMapperRegistry;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Types;
import org.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class InstanceContextTest extends TestCase {

    private DefContext entityRepository;
    private IInstanceStore instanceStore;
    private Cache cache;
    private EventQueue eventQueue;
    private EntityIdProvider idProvider;
    private Executor executor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
        var mapperReg = new MemMapperRegistry();
        mapperReg.createTables(TestConstants.APP_ID);
        instanceStore = new MemInstanceStore(mapperReg);
        entityRepository = new MockDefContext();
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
                List.of(),
                entityRepository,
                false,
                cache,
                eventQueue, false, false, false,
                0);
    }

    public void test() {
        var fooKlass = TestUtils.newKlassBuilder(PhysicalId.of(101L, 0L), "Foo", "Foo").build();
        var fooNameField = FieldBuilder.newBuilder("name", fooKlass, Types.getStringType())
                .id(PhysicalId.of(111L, 0L))
                .build();
        entityRepository.bind(fooKlass);
        String name = "foo";
        Id id;
        try (var context = newContext()) {
            id = context.allocateRootId();
            var instance = ClassInstanceBuilder.newBuilder(fooKlass.getType(), id)
                    .data(Map.of(fooNameField, Instances.stringInstance(name)))
                    .build();
            context.bind(instance);
            Assert.assertSame(instance, context.get(id));
            context.finish();
        }
        try (var context = newContext()) {
            var instance = (ClassInstance) context.get(id);
            Assert.assertEquals(name, Instances.toJavaString(instance.getField(fooNameField)));
        }
    }

    public void testOnChange() {
        BootstrapUtils.bootstrap();

        Id fooId;
        Id bazId;
        var fooTypes = MockUtils.createFooTypes(true);

        fooTypes.fooType().visitGraph(object -> {
            if (object instanceof Entity entity && entity.isIdNotNull())
                entityRepository.bind(entity);
            return true;
        });
        try (var context = newContext()) {
            var foo = MockUtils.createFoo(fooTypes, context::allocateRootId);
            var bars = foo.getField(fooTypes.fooBarsField()).resolveArray();
            var bar001 = bars.getFirst();
            var baz = ClassInstanceBuilder.newBuilder(fooTypes.bazType().getType(), context.allocateRootId())
                    .data(Map.of(fooTypes.bazBarsField(), new ArrayInstance(fooTypes.barArrayType(), List.of(bar001)).getReference()))
                    .build();
            context.bind(foo);
            context.bind(baz);
            context.finish();
            fooId = foo.getId();
            bazId = baz.getId();
        }
//        TestUtils.doInTransactionWithoutResult(() -> {
//            try (var context = newContext()) {
//                var foo = (ClassInstance) context.get(fooId);
//                var baz = (ClassInstance) context.get(bazId);
//                var bars = foo.getField(fooTypes.fooBarsField()).resolveArray();
//                var bar001 = (Reference) bars.getFirst();
//                context.remove(bar001.get());
//                final boolean[] onChangeCalled = new boolean[1];
//                context.addListener(new ContextListener() {
//
//                    @Override
//                    public boolean onRemove(Instance instance) {
//                        if (instance == bar001.resolveObject()) {
//                            bars.remove(bar001);
//                            baz.getField(fooTypes.bazBarsField()).resolveArray().remove(bar001);
//                            onChangeCalled[0] = true;
//                            return true;
//                        }
//                        else
//                            return false;
//                    }
//
//                    @Override
//                    public boolean onChange(Instance instance) {
//                        if (instance == foo) {
//                            bars.remove(bar001);
//                            baz.getField(fooTypes.bazBarsField()).resolveArray().remove(bar001);
//                            onChangeCalled[0] = true;
//                            return true;
//                        } else
//                            return false;
//                    }
//                });
//                context.finish();
//                Assert.assertTrue(onChangeCalled[0]);
//            }
//        });
    }

}