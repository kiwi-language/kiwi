package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.InternalErrorCode;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.Index;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.task.IndexRebuildGlobalTask;
import tech.metavm.task.ReferenceCleanupTask;
import tech.metavm.task.Task;
import tech.metavm.task.TaskSignal;
import tech.metavm.util.*;

import java.util.List;
import java.util.concurrent.Executors;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;
import static tech.metavm.util.NncUtils.*;
import static tech.metavm.util.TestConstants.TENANT_ID;

public class EntityContextTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(EntityContextTest.class);

    private MemInstanceStore instanceStore;
    private EntityIdProvider idProvider;
    private MemInstanceContext instanceContext;
    private InstanceContextFactory instanceContextFactory;
    private EntityContext context;
    private ClassType fooType;
    private Field fooNameField;
    private Field fooBarField;
    private Field barCodeField;

    @Override
    protected void setUp() {
        idProvider = new MockIdProvider();
        instanceStore = new MemInstanceStore();
        MockRegistry.setUp(idProvider, instanceStore);
        instanceContextFactory = TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        instanceContext = new MemInstanceContext(
                TENANT_ID,
                idProvider,
                instanceStore,
                InstanceContextFactory.getStdContext()
        );
        context = new EntityContext(instanceContext, MockRegistry.getDefContext(), MockRegistry.getDefContext());
        instanceContext.setEntityContext(context);

        fooType = MockRegistry.getClassType(Foo.class);
        fooNameField = MockRegistry.getField(Foo.class, "name");
        fooBarField = MockRegistry.getField(Foo.class, "bar");
        barCodeField = MockRegistry.getField(Bar.class, "code");
    }

    public void testSmoking() {
        ClassInstance fooInst = MockRegistry.getFooInstance();
        instanceContext.replace(fooInst);

        Foo foo = context.get(Foo.class, fooInst.getIdRequired());
        Assert.assertNotNull(foo);
        Assert.assertEquals(fooInst.getId(), foo.getId());
        Assert.assertEquals(fooInst.getStringField(fooNameField).getValue(), foo.getName());
        Assert.assertNotNull(foo.getBar());
        Assert.assertEquals(foo.getBar().code(), fooInst.getClassInstance(fooBarField).getStringField(barCodeField).getValue());
    }

    public void testInitIds() {
        Foo foo = new Foo("Little Foo", new Bar("Bar 002"));
        context.bind(foo);
        context.initIds();
        Assert.assertNotNull(foo.getId());
    }

    public void testGet() {
        Instance fooInst = MockRegistry.getNewFooInstance();
        fooInst.initId(idProvider.allocateOne(-1L, fooType));
        instanceContext.replace(fooInst);

        Foo foo = context.get(Foo.class, fooInst.getIdRequired());
        Assert.assertNotNull(foo);
        Instance loadedFooInst = context.getInstance(foo);
        Assert.assertSame(fooInst, loadedFooInst);
    }

    public void testBind() {
        Foo foo = MockRegistry.getFoo();
        context.bind(foo);

        Assert.assertTrue(context.containsModel(foo));
        Assert.assertTrue(context.containsModel(foo.getBazList()));
        requireNonNull(foo.getBazList());
        Assert.assertTrue(context.containsModel(foo.getBazList().get(0)));
        Assert.assertTrue(context.containsModel(foo.getBazList().get(1)));
        Assert.assertTrue(context.containsModel(foo.getQux()));
    }

    public void testUpdateAfterBinding() {
        Foo foo = MockRegistry.getFoo();
        EntityContext context1 = newContext(instanceContext);
        context1.bind(foo);
        foo.setName("A genius actually");
        requireNonNull(foo.getBazList()).remove(1);
        context1.finish();
        Assert.assertNotNull(foo.getId());

        EntityContext context2 = newContext(instanceContext);
        Foo loadedFoo = context2.get(Foo.class, foo.getId());
        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    public void testRemove() {
        Foo foo = MockRegistry.getFoo();
        context.bind(foo);
        Assert.assertTrue(context.containsModel(foo));
        context.remove(foo);
        Assert.assertFalse(context.containsModel(foo));
    }

    public void testRemovePersisted() {
        Instance inst = MockRegistry.getFooInstance();
        instanceContext.replace(inst);

        Foo foo = context.getEntity(Foo.class, inst.getIdRequired());
        context.remove(foo);
        Assert.assertFalse(context.containsModel(foo));
        Assert.assertFalse(context.containsInstance(inst));
        Assert.assertFalse(instanceContext.containsInstance(inst));
        Assert.assertFalse(instanceContext.containsId(inst.getIdRequired()));
    }

    public void test_remove() {
        EntityContext context = newIntegratedContext();
        Bar bar = new Bar("Bar001");
        Foo foo = new Foo("Big Foo", bar);
        context.bind(foo);
        context.finish();

        context = newIntegratedContext();
        context.remove(context.get(Foo.class, foo.getIdRequired()));
        context.finish();

        context = newIntegratedContext();
        List<ReferenceCleanupTask> jobs = context.getByType(ReferenceCleanupTask.class, null, 100);
        Assert.assertFalse(jobs.isEmpty());
        ReferenceCleanupTask jobForFoo = NncUtils.find(jobs, j -> j.getTargetId() == foo.getIdRequired());
        Assert.assertNotNull(jobForFoo);
        Assert.assertTrue(jobForFoo.isRunnable());

        IEntityContext rootContext = newIntegratedRootContext();
        TaskSignal signal = rootContext.selectByUniqueKey(TaskSignal.IDX_TENANT_ID, TENANT_ID);
        Assert.assertNotNull(signal);
        Assert.assertEquals(2, signal.getUnfinishedCount());
    }

    public void test_remove_with_strong_reference() {
        EntityContext context = newIntegratedContext();
        Bar bar = new Bar("Bar001");
        Foo foo = new Foo("Big Foo", bar);
        context.bind(foo);
        context.finish();

        long barId = context.getInstance(bar).getIdRequired();

        try {
            context = newIntegratedContext();
            context.remove(context.get(Bar.class, barId));
            context.finish();
            Assert.fail("Strongly referenced instance can not be removed");
        } catch (BusinessException e) {
            Assert.assertEquals(ErrorCode.STRONG_REFS_PREVENT_REMOVAL, e.getErrorCode());
        }
    }

    public void test_remove_with_child() {
        EntityContext context = newIntegratedContext();
        Bar bar = new Bar("001");
        Foo foo = new Foo("Big Foo", bar);
        context.bind(foo);
        context.finish();

        long fooId = foo.getIdRequired();
        long barId = context.getInstance(bar).getIdRequired();

        context = newIntegratedContext();
        context.remove(context.get(Foo.class, fooId));
        context.finish();

        Assert.assertNull(instanceStore.get(fooId));
        Assert.assertNull(instanceStore.get(barId));
    }

    public void test_remove_unique_field() {
        EntityContext context = newIntegratedContext();
        ClassType productType = ClassBuilder.newBuilder("Product", null).build();
        Field field = FieldBuilder
                .newBuilder("code", null, productType, StandardTypes.getStringType())
                .asTitle(true)
                .unique(true)
                .build();
        context.bind(productType);
        context.finish();

        context = newIntegratedContext();
        field = context.get(Field.class, field.getIdRequired());
        context.remove(field);
        context.finish();
    }

    public void testGetModel() {
        Foo foo = MockRegistry.getFoo();
        context.bind(foo);
        Instance instance = context.getInstance(foo);
        Assert.assertNotNull(instance);
        Foo gotFoo = context.getModel(Foo.class, instance);
        Assert.assertSame(gotFoo, foo);
    }

    public void testGetEntity() {
        Instance inst = MockRegistry.getFooInstance();
        instanceContext.replace(inst);

        Foo foo = context.getEntity(Foo.class, inst.getIdRequired());
        Assert.assertNotNull(foo);
        Assert.assertEquals(inst.getId(), foo.getId());

        Foo gotFoo = context.getModel(Foo.class, inst);
        Assert.assertSame(foo, gotFoo);

        Instance gotInst = context.getInstance(foo);
        Assert.assertSame(inst, gotInst);
    }

    public void testHierarchy() {
        Foo foo = MockRegistry.getFoo();
        context.bind(foo);
        EntityContext subContext = newContext(context);
        Assert.assertTrue(subContext.containsModel(foo));
        Instance fooInst = subContext.getInstance(foo);
        Assert.assertNotNull(fooInst);
        Assert.assertTrue(subContext.containsInstance(fooInst));
        Assert.assertTrue(context.containsInstance(fooInst));
    }

    public void testSelectByKey() {
        ClassInstance fooInst = MockRegistry.getNewFooInstance();
        ClassInstance barInst = fooInst.getClassInstance(fooBarField);
        fooInst.initId(idProvider.allocateOne(-1L, fooType));
        instanceContext.replace(fooInst);

        Index constraint = MockRegistry.getIndexConstraint(Foo.IDX_NAME);

        instanceStore.addIndex(
                TENANT_ID,
                constraint.createIndexKey(List.of(fooInst.getStringField(fooNameField))).toPO(),
                fooInst.getId()
        );

        List<Foo> selectedFooList = context.selectByKey(Foo.IDX_NAME, fooInst.getStringField(fooNameField).getValue());
        Assert.assertNotNull(selectedFooList);
        Assert.assertEquals(1, selectedFooList.size());

        Foo selectedFoo = selectedFooList.get(0);
        Assert.assertEquals(fooInst.getId(), selectedFoo.getId());
        Assert.assertEquals(fooInst.getStringField(fooNameField).getValue(), selectedFoo.getName());

        Bar selectedBar = selectedFoo.getBar();
        Assert.assertNotNull(selectedBar);
        Assert.assertEquals(barInst.getStringField(barCodeField).getValue(), selectedBar.code());
    }

    public void test_get_entity_with_wrong_type() {
        Instance fooInst = MockRegistry.getFooInstance();
        instanceContext.replace(fooInst);

        Foo foo = context.getEntity(Foo.class, fooInst.getIdRequired());
        Assert.assertNotNull(foo);

        try {
            context.getEntity(Baz.class, fooInst.getIdRequired());
            Assert.fail("Should throw an exception");
        } catch (InternalException e) {
            Assert.assertEquals(InternalErrorCode.MODEL_TYPE_MISMATCHED, e.getErrorCode());
        }
    }

    public void test_array_with_generic_element_type() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);

        ReadWriteArray<Constraint<?>> constraints = new ReadWriteArray<>(
                new TypeReference<>() {
                },
                List.of(
                        ConstraintFactory.createFromDTO(
                                new ConstraintDTO(
                                        null,
                                        null,
                                        ConstraintKind.CHECK.code(),
                                        fooType.getIdRequired(),
                                        null,
                                        new CheckConstraintParamDTO(
                                                ValueDTO.exprValue("名称 = 'Big Foo'")
                                        )
                                ),
                                context
                        ),
                        ConstraintFactory.createFromDTO(
                                new ConstraintDTO(
                                        null,
                                        null,
                                        ConstraintKind.UNIQUE.code(),
                                        fooType.getIdRequired(),
                                        null,
                                        UniqueConstraintParamDTO.create(
                                                "名称唯一",
                                                ValueDTO.refValue("名称")
                                        )
                                ),
                                context
                        )
                )
        );

        context.bind(constraints);

        Instance instance = context.getInstance(constraints);
        Assert.assertNotNull(instance);
        Assert.assertTrue(instance instanceof ArrayInstance);
        ArrayInstance arrayInstance = (ArrayInstance) instance;
        Assert.assertEquals(arrayInstance.size(), constraints.size());
        Assert.assertEquals(MockRegistry.getType(CheckConstraint.class), arrayInstance.get(0).getType());
        Assert.assertEquals(MockRegistry.getType(Index.class), arrayInstance.get(1).getType());
    }

    public void testIntegrationWithInstanceContext() {
        EntityContext entityContext1 = newEntityContextWitIntegration();
        Foo foo = MockRegistry.getFoo();
        entityContext1.bind(foo);

        entityContext1.finish();

        EntityContext entityContext2 = newEntityContextWitIntegration();
        Foo loadedFoo = entityContext2.getEntity(Foo.class, foo.getIdRequired());

        TestUtils.logJSON(LOGGER, loadedFoo);

        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    public void testGetRecord() {
        Column column = new Column("x1", SQLType.INT64);
        context.bind(column);
        context.finish();

        Instance instance = context.getInstance(column);
        Assert.assertNotNull(instance.getId());

        EntityContext context2 = newContext(instanceContext);
        Column column1 = context2.get(Column.class, instance.getId());
        Assert.assertEquals(column, column1);
    }

    public void testGetEnum() {
        Instance instance = context.getInstance(TypeCategory.CLASS);
        Assert.assertNotNull(instance.getId());

        TypeCategory typeCategory = context.get(TypeCategory.class, instance.getId());
        Assert.assertSame(TypeCategory.CLASS, typeCategory);
    }

    public void test_add_not_null_field_without_default_value() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        Foo foo = MockRegistry.getFoo();
        context.bind(foo);
        Field field = FieldBuilder
                .newBuilder("testNotNull", null, fooType, InstanceUtils.getStringType())
                .build();
        try {
            context.bind(field);
            Assert.fail("Should not succeed");
        } catch (BusinessException e) {
            Assert.assertEquals(ErrorCode.INVALID_FIELD, e.getErrorCode());
        }
    }

    public void test_init_id_manually() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        Foo foo = new Foo("Big Foo", new Bar("001"));
        IEntityContext context = newIntegratedContext();
        context.bind(foo);
        long fooId = idProvider.allocateOne(TENANT_ID, fooType);
        context.initIdManually(foo, fooId);
        context.finish();

        context = newIntegratedContext();
        Foo loadedFoo = context.getEntity(Foo.class, fooId);
        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    public void test_multi_level_inheritance() {
        ClassType type = MockRegistry.getClassType(IndexRebuildGlobalTask.class);

        IEntityContext context = newIntegratedContext();
        IndexRebuildGlobalTask job = new IndexRebuildGlobalTask();
        context.bind(job);
        Instance instance = context.getInstance(job);
        Assert.assertEquals(type, instance.getType());
        context.finish();

        context = newIntegratedContext();
        Task loadedJob = context.getEntity(Task.class, job.getIdRequired());
        Assert.assertTrue(loadedJob instanceof IndexRebuildGlobalTask);
    }

    public EntityContext newIntegratedContext() {
        return newIntegratedContext(TENANT_ID);
    }

    public EntityContext newIntegratedRootContext() {
        return newIntegratedContext(ROOT_TENANT_ID);
    }

    public EntityContext newIntegratedContext(long tenantId) {
        return (EntityContext) instanceContextFactory.newContext(tenantId).getEntityContext();
    }

    private EntityContext newContext(IInstanceContext instanceContext) {
        return newContext(instanceContext, MockRegistry.getDefContext());
    }

    private EntityContext newContext(IEntityContext parent) {
        MemInstanceContext instanceContext = new MemInstanceContext(
                TENANT_ID, idProvider, instanceStore, parent.getInstanceContext()
        );
        return newContext(instanceContext, parent);
    }

    private EntityContext newContext(IInstanceContext instanceContext, IEntityContext parent) {
        return new EntityContext(instanceContext, parent, MockRegistry.getDefContext());
    }

    private EntityContext newEntityContextWitIntegration() {
        InstanceContext instanceContext = new InstanceContext(
                TENANT_ID, instanceStore, idProvider, Executors.newSingleThreadExecutor(),
                false, List.of(), MockRegistry.getInstanceContext()
        );
        EntityContext entityContext =
                new EntityContext(instanceContext, MockRegistry.getDefContext(), MockRegistry.getDefContext());
        instanceContext.setEntityContext(entityContext);
        return entityContext;
    }

}