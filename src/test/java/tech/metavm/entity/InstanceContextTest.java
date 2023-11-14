package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.InternalErrorCode;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Index;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import static tech.metavm.util.InstanceUtils.getAllInstances;
import static tech.metavm.util.InstanceUtils.stringInstance;
import static tech.metavm.util.MockRegistry.getField;
import static tech.metavm.util.MockRegistry.getNewFooInstance;
import static tech.metavm.util.TestConstants.TENANT_ID;

public class InstanceContextTest extends TestCase {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceContextTest.class);

    private MemInstanceStore instanceStore;
    private MemIndexEntryMapper indexItemMapper;
    private TypeResolver typeResolver;
    private MockIdProvider idProvider;
    private IInstanceContext context;
    private MockModelInstanceMap modelInstanceMap;

    @Override
    protected void setUp() {
        idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        indexItemMapper = new MemIndexEntryMapper();
        instanceStore = new MemInstanceStore(indexItemMapper);
        typeResolver = (context, typeId) -> MockRegistry.getType(typeId);
        context = newContext();
        modelInstanceMap = new MockModelInstanceMap(MockRegistry.getDefContext());
    }

    private IInstanceContext newContext() {
        return newContext(null, typeResolver);
    }

    private IInstanceContext newContext(IInstanceContext parent) {
        return newContext(parent, typeResolver);
    }

    private IInstanceContext newContextWithIndexSupport() {
        return newContext(
                MockRegistry.getInstanceContext(), typeResolver,
                List.of(new IndexConstraintPlugin(indexItemMapper))
        );
    }

    private IInstanceContext newContext(IInstanceContext parent, TypeResolver typeResolver) {
        return newContext(parent, typeResolver, List.of());
    }

    private IInstanceContext newContext(IInstanceContext parent, TypeResolver typeResolver, List<ContextPlugin> plugins) {
        return new InstanceContextBuilder(instanceStore, Executors.newSingleThreadExecutor(), parent, idProvider)
                .tenantId(TENANT_ID)
                .plugins(plugins)
                .typeResolver(typeResolver).buildInstanceContext();
    }

    public void testSmoking() {
        Instance instance = getNewFooInstance("Foo1", "Bar001");
        context.bind(instance);
        context.finish();

        assertPersisted(instance);
        IInstanceContext context2 = newContext();

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

        IInstanceContext context2 = newContext();

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

    public void testGet() {
        Instance foo = getNewFooInstance();
        context.bind(foo);
        context.finish();

        IInstanceContext context1 = newContext();
        Instance loadedFoo = context1.get(foo.getId());
        // should be lazily initialized
        Assert.assertFalse(InstanceUtils.isInitialized(loadedFoo));
        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    public void testHierarchy() {
        Instance instance = getNewFooInstance();
        context.bind(instance);
        context.finish();

        long fooId = instance.getId();

        IInstanceContext childContext = newContext(context);

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

    public void testTypeResolution() {
        ModelDef<Foo, ?> fooDef = MockRegistry.getDef(Foo.class);
        IInstanceContext context1 = newContext(null, typeResolver);

        Foo foo = new Foo("big foo", new Bar("little bar"));
        Instance fooInstance = fooDef.createInstance(foo, modelInstanceMap);
        context1.bind(fooInstance);
        context1.finish();

        assertPersisted(fooInstance);

        foo.initId(fooInstance.getIdRequired());
        IInstanceContext context2 = newContext(null, typeResolver);
        Instance loadedFooInstance = context2.get(foo.getId());
        Foo loadedFoo = modelInstanceMap.getModel(Foo.class, loadedFooInstance);
        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    public void testBind() {
        Instance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        IInstanceContext context2 = newContext();
        Instance loadedFoo = context2.get(foo.getId());
        MatcherAssert.assertThat(loadedFoo, InstanceMatcher.of(foo));
    }

    public void testUpdate() {
        Instance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        IInstanceContext context2 = newContext();
        ClassInstance loadedFoo = (ClassInstance) context2.get(foo.getId());

        Field fooNameField = MockRegistry.getField(Foo.class, "name");
        Field fooQuxField = MockRegistry.getField(Foo.class, "qux");
        Field fooBazListField = MockRegistry.getField(Foo.class, "bazList");
        Field quxAmountField = MockRegistry.getField(Qux.class, "amount");
        Field bazBarsField = MockRegistry.getField(Baz.class, "bars");
        Field barCodeField = MockRegistry.getField(Bar.class, "code");

        loadedFoo.setField(fooNameField, stringInstance("Not a foo"));
        ClassInstance loadedQux = loadedFoo.getClassInstance(fooQuxField);
        loadedQux.setField(quxAmountField, loadedQux.getLongField(quxAmountField).inc(1L));
        ArrayInstance loadedBazList = loadedFoo.getInstanceArray(fooBazListField);
        ClassInstance loadedBaz = (ClassInstance) loadedBazList.getInstance(0);
        ClassInstance loadedBar = (ClassInstance) loadedBaz.getInstanceArray(bazBarsField).getInstance(0);
        loadedBar.setField(barCodeField, stringInstance("Bar1001"));
        context2.finish();

        IInstanceContext context3 = newContext();
        Instance reloadedFoo = context3.get(foo.getId());
        MatcherAssert.assertThat(reloadedFoo, InstanceMatcher.of(loadedFoo));
    }

    public void testRemove() {
        Instance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        IInstanceContext context2 = newContext(MockRegistry.getInstanceContext());
        context2.remove(context2.get(foo.getId()));
        context2.finish();

        IInstanceContext context3 = newContext();
        try {
            Instance loaded = context3.get(foo.getId());
            loaded.getTitle();
        } catch (BusinessException e) {
            Assert.assertEquals(ErrorCode.INSTANCE_NOT_FOUND, e.getErrorCode());
        }
    }

    public void testRemoveReferenceTarget() {
        Field fooBarField = MockRegistry.getField(Foo.class, "bar");
        Field fooQuxField = MockRegistry.getField(Foo.class, "qux");
        Field fooBazListField = MockRegistry.getField(Foo.class, "bazList");

        // new instance with a strong reference
        {
            IInstanceContext context1 = newContext();
            ClassInstance foo = MockRegistry.getNewFooInstance();
            context1.bind(foo);
            try {
                context1.remove(foo.getField(fooBarField));
                Assert.fail("Can not remove a strongly referenced instance");
            } catch (BusinessException e) {
                Assert.assertEquals(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, e.getErrorCode());
            }
        }

        // new instance with a weak reference
        {
            IInstanceContext context1 = newContext();
            ClassInstance foo = MockRegistry.getNewFooInstance();
            context1.bind(foo);
            context1.remove(foo.getField(fooQuxField));
            context1.finish();

            IInstanceContext context2 = newContext();
            foo = (ClassInstance) context2.get(foo.getId());
            Assert.assertTrue(foo.getField(fooQuxField).isNull());
        }

        // new instance with a strong reference from an array
        {
            IInstanceContext context1 = newContext();
            ClassInstance foo = MockRegistry.getNewFooInstance();
            context1.bind(foo);
            ArrayInstance bazList = foo.getInstanceArray(fooBazListField);
            Instance baz = bazList.getInstance(0);
            try {
                context1.remove(baz);
                Assert.fail("Can not remove a strongly referenced instance (by array)");
            } catch (BusinessException e) {
                Assert.assertEquals(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, e.getErrorCode());
            }
            bazList.remove(baz);
            Assert.assertFalse(bazList.contains(baz));
            context1.remove(baz);
            context1.finish();

            IInstanceContext context2 = newContext();
            ArrayInstance loadedBazList = (ArrayInstance) context2.get(bazList.getId());
            Assert.assertEquals(bazList.size(), loadedBazList.size());
        }

        ClassInstance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        Long barId = foo.getInstanceField(fooBarField).getId();
        Assert.assertNotNull(barId);
        Long quxId = foo.getInstanceField(fooQuxField).getId();
        Assert.assertNotNull(quxId);

        // old instance with strong references
        try {
            IInstanceContext context2 = newContext(MockRegistry.getInstanceContext());
            context2.remove(context2.get(barId));
            context2.finish();
            Assert.fail("Can not a remove instance with strong reference pointing to it");
        } catch (BusinessException e) {
            Assert.assertEquals(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, e.getErrorCode());
        }

        // old instance with weak references
        {
            IInstanceContext context3 = newContext(MockRegistry.getInstanceContext());
            context3.remove(context3.get(quxId));
            context3.finish();
            IInstanceContext context4 = newContext();
            foo = (ClassInstance) context4.get(foo.getId());
            Assert.assertTrue(foo.getField(fooQuxField).isNull());
        }
    }

    public void testIndex() {
        Index uniqueConstraint = MockRegistry.getIndexConstraint(Foo.IDX_NAME);
        MockEntityContext entityContext = new MockEntityContext(
                MockRegistry.getDefContext(), idProvider, MockRegistry.getDefContext()
        );

        InstanceContext context = (InstanceContext) newContext(
                MockRegistry.getInstanceContext(),
                typeResolver,
                List.of(
                        new IndexConstraintPlugin(instanceStore.getIndexEntryMapper())
                )
        );

        context.setEntityContext(entityContext);

        ClassInstance fooInstance = getNewFooInstance("Foo1", "Bar001");
        context.bind(fooInstance);

        context.finish();

        String fooName = fooInstance.getStringField(getField(Foo.class, "name")).getValue();
        List<Instance> selectedInstances = context.selectByKey(
                uniqueConstraint.createIndexKeyByModels(List.of(fooName), context.getEntityContext())
        );

        Assert.assertEquals(1, selectedInstances.size());
        MatcherAssert.assertThat(fooInstance.toPO(TENANT_ID), PojoMatcher.of(selectedInstances.get(0).toPO(TENANT_ID)));
    }

    public void test_finish_with_uninitialized_proxy() {
        Instance foo = MockRegistry.getNewFooInstance();
        context.bind(foo);
        context.finish();

        IInstanceContext context2 = newContext();
        Instance loadedFoo = context2.get(foo.getId());
        Assert.assertFalse(InstanceUtils.isInitialized(loadedFoo));
        context2.finish();
    }

    public void testInstanceLog() {
        MockInstanceLogService mockInstanceLogService = new MockInstanceLogService();
        var context = newContext(
                null, typeResolver, List.of(new ChangeLogPlugin(mockInstanceLogService))
        );

        Instance fooInst = getNewFooInstance("Big Foo", "Bar001");
        context.bind(fooInst);
        context.finish();

        Set<Instance> allInstances = getAllInstances(List.of(fooInst), i -> !i.isValue());

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

        ClassInstance bazInst = MockRegistry.getNewBazInstance();

        context.bind(bazInst);
        context.finish();

        InstancePO bazInstancePO = instanceStore.get(bazInst.getId());
        Assert.assertFalse(EntityUtils.isPojoDifferent(bazInst.toPO(TENANT_ID), bazInstancePO));
        Long barsArrayId = bazInst.getInstanceField(bazBarsField).getId();
        NncUtils.requireNonNull(barsArrayId);
        InstanceArrayPO barsArrayPO = (InstanceArrayPO) instanceStore.get(barsArrayId);
        MatcherAssert.assertThat(bazInst.getInstanceField(bazBarsField).toPO(TENANT_ID), PojoMatcher.of(barsArrayPO));

        IInstanceContext context2 = newContext();

        Instance loadedBazInst = context2.get(bazInst.getId());
        ArrayInstance barsArray = (ArrayInstance) context2.get(barsArrayId);

        MatcherAssert.assertThat(bazInst.toPO(TENANT_ID), PojoMatcher.of(loadedBazInst.toPO(TENANT_ID)));
        MatcherAssert.assertThat(bazInst.getInstanceField(bazBarsField).toPO(TENANT_ID), PojoMatcher.of(barsArray.toPO(TENANT_ID)));
    }

    public void testQuery() {
        IInstanceContext context1 = newContextWithIndexSupport();
        ClassInstance foo = MockRegistry.getNewFooInstance();
        context1.bind(foo);
        context1.finish();

        Field fooQuxField = MockRegistry.getField(Foo.class, "qux");
        ClassInstance qux = foo.getClassInstance(fooQuxField);

        context1 = newContextWithIndexSupport();
        Index indexConstraint = MockRegistry.getIndexConstraint(Qux.IDX_AMOUNT);
        List<Instance> queryResult = context1.query(
                new InstanceIndexQuery(
                        indexConstraint,
                        List.of(
                                new InstanceIndexQueryItem(
                                        indexConstraint.getFieldByTypeField(
                                                MockRegistry.getField(Qux.class, "amount")
                                        ),
                                        InstanceUtils.longInstance(90)
                                )
                        ),
                        IndexQueryOperator.GT,
                        false,
                        1
                )
        );
        Assert.assertEquals(1, queryResult.size());
        Instance queriedQux = queryResult.get(0);
        Assert.assertEquals(qux.getId(), queriedQux.getId());
    }

    public void test_get_with_nonexistent_id() {
        try {
            context.get(100000000000000L);
            Assert.fail();
        } catch (InternalException e) {
            Assert.assertEquals(InternalErrorCode.INVALID_ID, e.getErrorCode());
        }
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

}