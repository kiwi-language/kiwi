package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import static tech.metavm.util.MockRegistry.getField;
import static tech.metavm.util.TestContext.getTenantId;

public class InstanceQueryServiceTest extends TestCase {

    private MemInstanceContext context;
    private MemInstanceSearchService instanceSearchService;
    private InstanceQueryService instanceQueryService;

    @Override
    protected void setUp() throws Exception {
        EntityIdProvider idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
        context = new MemInstanceContext();
        context.setTypeProvider(MockRegistry::getType);
        instanceSearchService = new MemInstanceSearchService();
        instanceQueryService = new InstanceQueryService(instanceSearchService);
    }

    public void testEqCondition() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        Field fooNameField = getField(Foo.class, "name");
        Field fooQuxField = getField(Foo.class, "qux");
        Field fooBazListField = getField(Foo.class, "bazList");

        ClassInstance foo = addInstance(MockRegistry.getNewFooInstance());
        Instance qux = foo.getInstanceField(fooQuxField);
        Instance baz = foo.getInstanceArray(fooBazListField).getInstance(0);

        InstanceQuery query = InstanceQueryBuilder.newBuilder(fooType)
                .fields(
                        InstanceQueryField.create(
                                fooNameField,
                                foo.getStringField(fooNameField)
                        ),
                        InstanceQueryField.create(fooQuxField, qux),
                        InstanceQueryField.create(fooBazListField, baz)
                )
                .build();
        Page<Instance> page = instanceQueryService.query(query, context);
        Assert.assertEquals(1, page.total());
        Assert.assertEquals(foo.getId(), page.data().get(0).getId());
    }

    private ClassInstance addInstance(ClassInstance instance) {
        context.bind(instance);
        context.initIds();
        instanceSearchService.add(getTenantId(), instance);
        return instance;
    }

    public void testInCondition() {
        ClassType fooType = MockRegistry.getClassType(Foo.class);
        Field fooNameField = getField(Foo.class, "name");

        ClassInstance foo = addInstance(MockRegistry.getNewFooInstance());

        InstanceQuery query2 = InstanceQueryBuilder.newBuilder(fooType)
                .fields(InstanceQueryField.create(
                        fooNameField,
                        foo.getField(fooNameField)
                ))
                .build();
        Page<Instance> page2 = instanceQueryService.query(query2, context);
        Assert.assertEquals(1, page2.total());
        Assert.assertEquals(foo.getId(), page2.data().get(0).getId());
    }

}