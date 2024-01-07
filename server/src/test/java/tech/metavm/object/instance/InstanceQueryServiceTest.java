package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.InstanceQuery;
import tech.metavm.entity.InstanceQueryBuilder;
import tech.metavm.entity.InstanceQueryField;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestConstants;

import static tech.metavm.util.MockRegistry.getField;
import static tech.metavm.util.TestContext.getAppId;

public class InstanceQueryServiceTest extends TestCase {

    private MemInstanceSearchService instanceSearchService;
    private InstanceQueryService instanceQueryService;
    private InstanceRepository instanceRepository;
    private ParameterizedFlowProvider parameterizedFlowProvider;
    private TypeRepository typeRepository;
    private ArrayTypeProvider arrayTypeProvider;
    private EntityIdProvider idProvider;

    @Override
    protected void setUp() throws Exception {
        idProvider = new MockIdProvider();
        typeRepository = new MockTypeRepository();
        instanceSearchService = new MemInstanceSearchService();
        instanceQueryService = new InstanceQueryService(instanceSearchService);
        instanceRepository = new MockInstanceRepository();
        var compositeTypeProviders = new TypeProviders();
        parameterizedFlowProvider = compositeTypeProviders.parameterizedFlowProvider;
        arrayTypeProvider = compositeTypeProviders.arrayTypeProvider;
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
        var page = instanceQueryService.query(query,
                instanceRepository,
                parameterizedFlowProvider,
                typeRepository,
                arrayTypeProvider
        );
        Assert.assertEquals(1, page.total());
        Assert.assertEquals(foo.tryGetPhysicalId(), page.data().get(0).tryGetPhysicalId());
    }

    private ClassInstance addInstance(ClassInstance instance) {
        instanceRepository.bind(instance);
        instance.initId(PhysicalId.of(idProvider.allocateOne(TestConstants.APP_ID, instance.getType())));
        instanceSearchService.add(getAppId(), instance);
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
        var page2 = instanceQueryService.query(query2,
                instanceRepository, parameterizedFlowProvider, typeRepository, arrayTypeProvider);
        Assert.assertEquals(1, page2.total());
        Assert.assertEquals(foo.tryGetPhysicalId(), page2.data().get(0).tryGetPhysicalId());
    }

}