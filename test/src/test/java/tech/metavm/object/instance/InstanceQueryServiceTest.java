package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.*;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.*;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.type.mocks.MockTypeRepository;
import tech.metavm.util.*;

public class InstanceQueryServiceTest extends TestCase {

    private MemInstanceSearchServiceV2 instanceSearchService;
    private InstanceQueryService instanceQueryService;
    private MockInstanceRepository instanceRepository;
    private ParameterizedFlowProvider parameterizedFlowProvider;
    private TypeRepository typeRepository;
    private ArrayTypeProvider arrayTypeProvider;

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
        typeRepository = new MockTypeRepository();
        instanceSearchService = new MemInstanceSearchServiceV2();
        instanceQueryService = new InstanceQueryService(instanceSearchService);
        instanceRepository = new MockInstanceRepository();
        var compositeTypeProviders = new TypeProviders();
        parameterizedFlowProvider = compositeTypeProviders.parameterizedFlowProvider;
        arrayTypeProvider = compositeTypeProviders.arrayTypeProvider;
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    public void testEqCondition() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooType = fooTypes.fooType();
        var fooNameField = fooTypes.fooNameField();
        var fooQuxField = fooTypes.fooQuxField();
        var fooBazListField = fooTypes.fooBazListField();

        var foo = addInstance(MockUtils.createFoo(fooTypes, true));
        var qux = foo.getInstanceField(fooQuxField);
        var baz = foo.getInstanceArray(fooBazListField).getInstance(0);

        var query = InstanceQueryBuilder.newBuilder(fooType)
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
        instanceSearchService.add(TestConstants.APP_ID, instance);
        return instance;
    }

    public void testInCondition() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooType = fooTypes.fooType();
        var fooNameField = fooTypes.fooNameField();
        var foo = addInstance(MockUtils.createFoo(fooTypes, true));
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