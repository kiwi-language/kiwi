package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.dto.InternalErrorCode;
import tech.metavm.flow.ValueKind;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.*;

import java.util.List;
import java.util.concurrent.Executors;

import static tech.metavm.util.NncUtils.*;
import static tech.metavm.util.TestConstants.TENANT_ID;

public class EntityContextTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(EntityContextTest.class);

    private final IInstanceStore instanceStore = new MemInstanceStore();
    private final EntityIdProvider idProvider = new MockIdProvider();
    private MemInstanceContext instanceContext;
    private EntityContext context;
    private ClassType fooType;
    private ClassType barType;
    private Field fooNameField;
    private Field fooBarField;
    private Field barCodeField;

    @Override
    protected void setUp() {
        MockRegistry.setUp(idProvider);
        instanceContext = new MemInstanceContext(
                TENANT_ID,
                idProvider,
                instanceStore,
                InstanceContextFactory.getStdContext()
        );
        context = new EntityContext(instanceContext, MockRegistry.getDefContext(), MockRegistry.getDefContext());
        instanceContext.setEntityContext(context);

        fooType = MockRegistry.getClassType(Foo.class);
        barType = MockRegistry.getClassType(Bar.class);
        fooNameField = MockRegistry.getField(Foo.class, "name");
        fooBarField = MockRegistry.getField(Foo.class, "bar");
        barCodeField = MockRegistry.getField(Bar.class, "code");
//        MockRegistry.initIds();
    }

    public void testSmoking() {
        ClassInstance fooInst = MockRegistry.getFooInstance();
        instanceContext.replace(fooInst);

        Foo foo = context.get(Foo.class, fooInst.getId());
        Assert.assertNotNull(foo);
        Assert.assertEquals(fooInst.getId(), foo.getId());
        Assert.assertEquals(fooInst.getString(fooNameField).getValue(), foo.getName());
        Assert.assertNotNull(foo.getBar());
        Assert.assertEquals(foo.getBar().code(), fooInst.getClassInstance(fooBarField).getString(barCodeField).getValue());
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

        Foo foo = context.get(Foo.class, fooInst.getId());
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

        Foo foo = context.getEntity(Foo.class, inst.getId());
        context.remove(foo);
        Assert.assertFalse(context.containsModel(foo));
        Assert.assertFalse(context.containsInstance(inst));
        Assert.assertFalse(instanceContext.containsInstance(inst));
        Assert.assertFalse(instanceContext.containsId(inst.getId()));
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

        Foo foo = context.getEntity(Foo.class, inst.getId());
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

        UniqueConstraintRT constraint = MockRegistry.getUniqueConstraint(Foo.IDX_NAME);

        instanceContext.addIndex(
                new IndexKeyPO(constraint.getId(), List.of(fooInst.getString(fooNameField).getValue())),
                fooInst.getId()
        );

        List<Foo> selectedFooList = context.selectByKey(Foo.IDX_NAME, fooInst.getString(fooNameField));
        Assert.assertNotNull(selectedFooList);
        Assert.assertEquals(1, selectedFooList.size());

        Foo selectedFoo = selectedFooList.get(0);
        Assert.assertEquals(fooInst.getId(), selectedFoo.getId());
        Assert.assertEquals(fooInst.getString(fooNameField).getValue(), selectedFoo.getName());

        Bar selectedBar = selectedFoo.getBar();
        Assert.assertNotNull(selectedBar);
        Assert.assertEquals(barInst.getString(barCodeField).getValue(), selectedBar.code());
    }

    public void test_get_entity_with_wrong_type() {
        Instance fooInst = MockRegistry.getFooInstance();
        instanceContext.replace(fooInst);

        Foo foo = context.getEntity(Foo.class, fooInst.getId());
        Assert.assertNotNull(foo);

        try {
            context.getEntity(Baz.class, fooInst.getId());
            Assert.fail("Should throw an exception");
        }
        catch (InternalException e) {
            Assert.assertEquals(InternalErrorCode.MODEL_TYPE_MISMATCHED, e.getErrorCode());
        }
    }

    public void test_array_with_generic_element_type() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);

        Table<ConstraintRT<?>> constraints = new Table<>(
                new TypeReference<>() {},
                List.of(
                        ConstraintFactory.createFromDTO(
                                new ConstraintDTO(
                                        null,
                                        ConstraintKind.CHECK.code(),
                                        fooType.getId(),
                                        null,
                                        new CheckConstraintParam(
                                                new ValueDTO(
                                                        ValueKind.EXPRESSION.code(),
                                                        "名称 = 'Big Foo'",
                                                        null
                                                )
                                        )
                                ),
                                fooType
                        ),
                        ConstraintFactory.createFromDTO(
                                new ConstraintDTO(
                                        null,
                                        ConstraintKind.UNIQUE.code(),
                                        fooType.getId(),
                                        null,
                                        UniqueConstraintParam.create(
                                                "名称唯一",
                                                new ValueDTO(
                                                        ValueKind.REFERENCE.code(),
                                                        "名称",
                                                        null
                                                )
                                        )
                                ),
                                fooType
                        )
                )
        );

        context.bind(constraints);

        Instance instance = context.getInstance(constraints);
        Assert.assertNotNull(instance);
        Assert.assertTrue(instance instanceof ArrayInstance);
        ArrayInstance arrayInstance = (ArrayInstance) instance;
        Assert.assertEquals(arrayInstance.size(), constraints.size());
        Assert.assertEquals(MockRegistry.getType(CheckConstraintRT.class), arrayInstance.get(0).getType());
        Assert.assertEquals(MockRegistry.getType(UniqueConstraintRT.class), arrayInstance.get(1).getType());
    }

    public void testIntegrationWithInstanceContext() {
        EntityContext entityContext1 = newEntityContextWitIntegration();
        Foo foo = MockRegistry.getFoo();
        entityContext1.bind(foo);

        entityContext1.finish();

        EntityContext entityContext2 = newEntityContextWitIntegration();
        Foo loadedFoo = entityContext2.getEntity(Foo.class, foo.getId());

        TestUtils.logJSON(LOGGER, loadedFoo);

        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    private EntityContext newContext() {
        return newContext(MockRegistry.getDefContext());
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