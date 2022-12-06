package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.dto.ErrorCode;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import static tech.metavm.util.MockRegistry.getField;
import static tech.metavm.util.MockRegistry.getNewFooInstance;
import static tech.metavm.util.TestConstants.TENANT_ID;

public class InstanceContextTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceContextTest.class);

    private MemInstanceStore instanceStore;
    private TypeResolver manualTypeResolver;
    private TypeResolver automaticTypeResolver;
    private MockIdProvider idProvider;
    private InstanceContext context;
    private MockModelInstanceMap modelInstanceMap;
    private DefContext defContext;

    @Override
    protected void setUp() {
        idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        instanceStore = new MemInstanceStore();
        context = newContext();
        defContext = new DefContext(o -> null, context);
        modelInstanceMap = new MockModelInstanceMap(defContext);
        automaticTypeResolver = (ctx, typeId) -> defContext.getType(ctx.get(typeId));
        manualTypeResolver = (context, typeId) -> MockRegistry.getType(typeId);
    }

    private InstanceContext newContext() {
        return newContext(null, manualTypeResolver);
    }

    private InstanceContext newContext(InstanceContext parent) {
        return newContext(parent, manualTypeResolver);
    }

    private InstanceContext newContext(InstanceContext parent, TypeResolver typeResolver) {
        return newContext(parent, typeResolver, List.of());
    }

    private InstanceContext newContext(InstanceContext parent, TypeResolver typeResolver, List<ContextPlugin> plugins) {
        return new InstanceContext(
                TENANT_ID,
                instanceStore,
                idProvider,
                Executors.newSingleThreadExecutor(),
                false,
                plugins,
                parent,
                typeResolver
        );
    }

    public void testSmoking() {
        Instance instance = getNewFooInstance("Foo1", "Bar001");
        context.bind(instance);
        context.finish();

        assertPersisted(instance);
        InstanceContext context2 = newContext();

        Instance loadedInstance = context2.get(instance.getId());
        Assert.assertFalse(EntityUtils.isPojoDifferent(instance.toPO(TENANT_ID), loadedInstance.toPO(TENANT_ID)));
    }

    public void testReplace() {
        Instance instance = getNewFooInstance();
        context.replace(instance);
        context.finish();
        assertPersisted(instance);
    }

    public void testReplace2() {
        Instance instance = getNewFooInstance();
        context.bind(instance);
        context.finish();

        long fooId = instance.getId();

        InstanceContext context2 = newContext();

        Instance loaded = context2.get(fooId);
        Assert.assertTrue(context2.containsInstance(loaded));
        Assert.assertFalse(EntityUtils.isPojoDifferent(loaded.toPO(TENANT_ID), instance.toPO(TENANT_ID)));

        Instance replacement = getNewFooInstance();
        replacement.initId(instance.getId());

        context2.replace(replacement);
        Assert.assertFalse(context2.containsInstance(loaded));
        Assert.assertTrue(context2.containsInstance(replacement));
        Assert.assertSame(replacement, context2.get(fooId));

        context2.finish();
        assertPersisted(replacement);
    }

    public void testHierarchy() {
        Instance instance = getNewFooInstance();
        context.bind(instance);
        context.finish();

        long fooId = instance.getId();

        InstanceContext childContext = newContext(context);

        Assert.assertTrue(childContext.containsInstance(instance));
        Assert.assertSame(instance, childContext.get(fooId));

        Instance replacement = getNewFooInstance("Foo replaced", "Bar-P001(R)");
        replacement.initId(fooId);
        childContext.replace(replacement);
        Assert.assertSame(replacement, childContext.get(fooId));
        Assert.assertSame(replacement, context.get(fooId));
        Assert.assertFalse(context.containsInstance(instance));
        Assert.assertTrue(context.containsInstance(replacement));
    }

    public void testLoad() {
        Instance fooInstance = getNewFooInstance();
        context.bind(fooInstance);
        context.finish();

        InstanceContext context1 = newContext();
        Instance loadedFooInstance = context1.get(fooInstance.getId());
        Assert.assertFalse(EntityUtils.isPojoDifferent(fooInstance.toPO(TENANT_ID), loadedFooInstance.toPO(TENANT_ID)));
    }

    public void testSelfTypeResolve() {
        ModelDef<Foo, ?> fooDef = defContext.getDef(Foo.class);
        defContext.finish();

        InstanceContext context1 = newContext(null, automaticTypeResolver);

        Foo foo = new Foo("big foo", new Bar("little bar"));
        Instance fooInstance = fooDef.createInstance(foo, modelInstanceMap);
        context1.bind(fooInstance);
        context1.finish();

        assertPersisted(fooInstance);

        foo.initId(fooInstance.getId());
        InstanceContext context2 = newContext(context, automaticTypeResolver);
        Instance loadedFooInstance = context2.get(foo.getId());
        Foo loadedFoo = modelInstanceMap.getModel(Foo.class, loadedFooInstance);
        Assert.assertFalse(EntityUtils.isPojoDifferent(foo, loadedFoo));
    }

    public void testInsert() {
        Instance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        InstanceContext context2 = newContext();
        Instance loadedFoo = context2.get(foo.getId());
        MatcherAssert.assertThat(loadedFoo, InstanceMatcher.of(foo));
    }

    public void testUpdate() {
        Instance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        InstanceContext context2 = newContext();
        Instance loadedFoo = context2.get(foo.getId());

        Field fooNameField = MockRegistry.getField(Foo.class, "name");
        Field fooQuxField = MockRegistry.getField(Foo.class, "qux");
        Field fooBazListField = MockRegistry.getField(Foo.class, "bazList");
        Field quxAmountField = MockRegistry.getField(Qux.class, "amount");
        Field bazBarsField = MockRegistry.getField(Baz.class, "bars");
        Field barCodeField = MockRegistry.getField(Bar.class, "code");

        loadedFoo.set(fooNameField, "Not a foo");
        Instance loadedQux = loadedFoo.getInstance(fooQuxField);
        loadedQux.set(quxAmountField, loadedQux.getLong(quxAmountField) + 1);
        InstanceArray loadedBazList = loadedFoo.getInstanceArray(fooBazListField);
        Instance loadedBaz = loadedBazList.getInstance(0);
        Instance loadedBar = loadedBaz.getInstanceArray(bazBarsField).getInstance(0);
        loadedBar.set(barCodeField, "Bar1001");
        context2.finish();

        InstanceContext context3 = newContext();
        Instance reloadedFoo = context3.get(foo.getId());
        MatcherAssert.assertThat(reloadedFoo, InstanceMatcher.of(loadedFoo));
    }

    public void testDelete() {
        Instance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        InstanceContext context2 = newContext();
        context2.remove(context2.get(foo.getId()));
        context2.finish();

        InstanceContext context3 = newContext();
        try {
            Instance loaded = context3.get(foo.getId());
            loaded.getTitle();
        } catch (BusinessException e) {
            Assert.assertEquals(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
    }

    public void testIndex() {
        UniqueConstraintRT uniqueConstraint = MockRegistry.getUniqueConstraint(Foo.IDX_NAME);
        MockEntityContext entityContext = new MockEntityContext();
        entityContext.put(uniqueConstraint);

        InstanceContext context = newContext(
                null,
                manualTypeResolver,
                List.of(
                        new UniqueConstraintPlugin(instanceStore.getIndexItemMapper())
                )
        );

        context.setEntityContext(entityContext);

        Instance fooInstance = getNewFooInstance("Foo1", "Bar001");
        context.bind(fooInstance);

        context.finish();

        List<Instance> selectedInstances = context.selectByKey(
                new IndexKeyPO(
                        uniqueConstraint.getId(),
                        List.of(fooInstance.getString(getField(Foo.class, "name")))
                )
        );

        Assert.assertEquals(1, selectedInstances.size());
        MatcherAssert.assertThat(fooInstance.toPO(TENANT_ID), PojoMatcher.of(selectedInstances.get(0).toPO(TENANT_ID)));
    }

    public void testLog() {
        MockInstanceLogService mockInstanceLogService = new MockInstanceLogService();
        InstanceContext context = newContext(
                null, manualTypeResolver, List.of(new ChangeLogPlugin(mockInstanceLogService))
        );

        Instance fooInst = getNewFooInstance("Big Foo", "Bar001");
        context.bind(fooInst);
        context.finish();

        Set<Instance> allInstances = InstanceUtils.getAllInstances(List.of(fooInst), i -> !i.isValue());

        List<InstanceLog> logs = mockInstanceLogService.getLogs();
        Assert.assertEquals(allInstances.size(), logs.size());
        Map<Long, InstanceLog> logMap = NncUtils.toMap(logs, InstanceLog::getId);

        for (Instance instance : allInstances) {
            InstanceLog log = logMap.get(instance.getId());
            Assert.assertNotNull(log);
            Assert.assertEquals(ChangeType.INSERT, log.getChangeType());
        }
    }

    public void testArray() {
        Field bazBarsField = MockRegistry.getField(Baz.class, "bars");

        Instance bazInst = MockRegistry.getNewBazInstance();

        context.bind(bazInst);
        context.finish();

        InstancePO bazInstancePO = instanceStore.get(bazInst.getId());
        Assert.assertFalse(EntityUtils.isPojoDifferent(bazInst.toPO(TENANT_ID), bazInstancePO));
        Long barsArrayId = bazInst.getInstance(bazBarsField).getId();
        NncUtils.requireNonNull(barsArrayId);
        InstanceArrayPO barsArrayPO = (InstanceArrayPO) instanceStore.get(barsArrayId);
        MatcherAssert.assertThat(bazInst.getInstance(bazBarsField).toPO(TENANT_ID), PojoMatcher.of(barsArrayPO));

        InstanceContext context2 = newContext();

        Instance loadedBazInst = context2.get(bazInst.getId());
        InstanceArray barsArray = (InstanceArray) context2.get(barsArrayId);

        MatcherAssert.assertThat(bazInst.toPO(TENANT_ID), PojoMatcher.of(loadedBazInst.toPO(TENANT_ID)));
        MatcherAssert.assertThat(bazInst.getInstance(bazBarsField).toPO(TENANT_ID), PojoMatcher.of(barsArray.toPO(TENANT_ID)));
    }

    private void assertPersisted(Instance instance) {
        List<InstancePO> loaded = instanceStore.load(
                StoreLoadRequest.create(instance.getId()),
                context
        );
        Assert.assertFalse(loaded.isEmpty());
        InstancePO po = loaded.get(0);
        Assert.assertFalse(EntityUtils.isPojoDifferent(instance.toPO(TENANT_ID), po));
    }

    private static class MockEntityContext implements IEntityContext {

        private final Map<Long, Object> map = new HashMap<>();

        @Override
        public boolean containsInstance(Instance instance) {
            return false;
        }

        @Override
        public boolean containsModel(Object model) {
            return map.containsValue(model);
        }

        @Override
        public <T extends Entity> T getEntity(Class<T> entityType, long id) {
            return entityType.cast(NncUtils.requireNonNull(map.get(id)));
        }

        @Override
        public void finish() {

        }

        @Override
        public IInstanceContext getInstanceContext() {
            return null;
        }

        @Override
        public <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... refValues) {
            return null;
        }

        @Override
        public void remove(Object model) {

        }

        @Override
        public void initIds() {

        }

        @Override
        public void bind(Object model) {

        }

        public void put(Entity entity) {
            map.put(entity.getId(), entity);
        }

        @Override
        public Instance getInstance(Object model) {
            return null;
        }

        @Override
        public <T> T getModel(Class<T> klass, Instance instance) {
            return null;
        }
    }

}