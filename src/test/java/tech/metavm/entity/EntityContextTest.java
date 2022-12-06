package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class EntityContextTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(EntityContextTest.class);

    private final IInstanceStore instanceStore = new MemInstanceStore();
    private final EntityIdProvider idProvider = new MockIdProvider();
    private MemInstanceContext instanceContext;
    private EntityContext entityContext;
    private Type fooType;
    private Type barType;
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
        entityContext = new EntityContext(instanceContext, MockRegistry.getDefContext(), MockRegistry.getDefContext());
        instanceContext.setEntityContext(entityContext);

        fooType = MockRegistry.getType(Foo.class);
        barType = MockRegistry.getType(Bar.class);
        fooNameField = MockRegistry.getField(Foo.class, "name");
        fooBarField = MockRegistry.getField(Foo.class, "bar");
        barCodeField = MockRegistry.getField(Bar.class, "code");
//        MockRegistry.initIds();
    }

    public void testSmoking() {
        Instance fooInst = MockRegistry.getFooInstance();
        instanceContext.replace(fooInst);

        Foo foo = entityContext.get(Foo.class, fooInst.getId());
        Assert.assertNotNull(foo);
        Assert.assertEquals(fooInst.getId(), foo.getId());
        Assert.assertEquals(fooInst.getString(fooNameField), foo.getName());
        Assert.assertNotNull(foo.getBar());
        Assert.assertEquals(foo.getBar().code(), fooInst.getInstance(fooBarField).getString(barCodeField));
    }

    public void testInitIds() {
        Foo foo = new Foo("Little Foo", new Bar("Bar 002"));
        entityContext.bind(foo);
        entityContext.initIds();
        Assert.assertNotNull(foo.getId());
    }

    public void testGetById() {
        Instance fooInst = getFooInstance();
        fooInst.initId(idProvider.allocateOne(-1L, fooType));
        instanceContext.replace(fooInst);

        Foo foo = entityContext.get(Foo.class, fooInst.getId());
        Assert.assertNotNull(foo);
        Instance loadedFooInst = entityContext.getInstance(foo);
        Assert.assertSame(fooInst, loadedFooInst);
    }

    public void testSelectByKey() {
        Instance fooInst = getFooInstance();
        Instance barInst = fooInst.getInstance(fooBarField);
        fooInst.initId(idProvider.allocateOne(-1L, fooType));
        instanceContext.replace(fooInst);

        UniqueConstraintRT constraint = MockRegistry.getUniqueConstraint(Foo.IDX_NAME);

        instanceContext.addIndex(
                new IndexKeyPO(constraint.getId(), List.of(fooInst.getString(fooNameField))),
                fooInst.getId()
        );

        List<Foo> selectedFooList = entityContext.selectByKey(Foo.IDX_NAME, fooInst.getString(fooNameField));
        Assert.assertNotNull(selectedFooList);
        Assert.assertEquals(1, selectedFooList.size());

        Foo selectedFoo = selectedFooList.get(0);
        Assert.assertEquals(fooInst.getId(), selectedFoo.getId());
        Assert.assertEquals(fooInst.getString(fooNameField), selectedFoo.getName());

        Bar selectedBar = selectedFoo.getBar();
        Assert.assertNotNull(selectedBar);
        Assert.assertEquals(barInst.getString(barCodeField), selectedBar.code());
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

    public void testIntegrationWithInstanceContext() {
        EntityContext entityContext1 = newEntityContextWitIntegration();
        Foo foo = MockRegistry.getComplexFoo();
        entityContext1.bind(foo);

        entityContext1.finish();

        EntityContext entityContext2 = newEntityContextWitIntegration();
        Foo loadedFoo = entityContext2.getEntity(Foo.class, foo.getId());

        TestUtils.logJSON(LOGGER, loadedFoo);

        MatcherAssert.assertThat(loadedFoo, PojoMatcher.of(foo));
    }

    private Instance getFooInstance() {
        return new Instance(
                Map.of(
                        fooNameField,
                        "Big Foo",
                        fooBarField,
                        new Instance(
                                Map.of(barCodeField, "Bar001"),
                                barType
                        )
                ),
                fooType
        );
    }

}