package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.InstanceQuery;
import tech.metavm.entity.InstanceQueryBuilder;
import tech.metavm.entity.InstanceQueryField;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.mocks.MockInstanceRepository;
import tech.metavm.object.type.TypeDefRepository;
import tech.metavm.object.type.mocks.MockTypeDefRepository;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestConstants;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class InstanceQueryServiceTest extends TestCase {

    private MemInstanceSearchServiceV2 instanceSearchService;
    private InstanceQueryService instanceQueryService;
    private MockInstanceRepository instanceRepository;
    private TypeDefRepository typeRepository;

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
        typeRepository = new MockTypeDefRepository();
        instanceSearchService = new MemInstanceSearchServiceV2();
        instanceQueryService = new InstanceQueryService(instanceSearchService);
        instanceRepository = new MockInstanceRepository();
        ContextUtil.setAppId(TestConstants.APP_ID);
    }

    public void testEqCondition() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooKlass = fooTypes.fooType();
        var fooNameField = fooTypes.fooNameField();
        var fooQuxField = fooTypes.fooQuxField();
        var fooBazListField = fooTypes.fooBazListField();

        var foo = addInstance(MockUtils.createFoo(fooTypes, true));
        var qux = foo.getInstanceField(fooQuxField);
        var baz = foo.getInstanceArray(fooBazListField).getInstance(0);

        var query = InstanceQueryBuilder.newBuilder(fooKlass)
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
                typeRepository
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
        var fooKlas = fooTypes.fooType();
        var fooNameField = fooTypes.fooNameField();
        var foo = addInstance(MockUtils.createFoo(fooTypes, true));
        InstanceQuery query2 = InstanceQueryBuilder.newBuilder(fooKlas)
                .fields(InstanceQueryField.create(
                        fooNameField,
                        foo.getField(fooNameField)
                ))
                .build();
        var page2 = instanceQueryService.query(query2,
                instanceRepository, typeRepository);
        Assert.assertEquals(1, page2.total());
        Assert.assertEquals(foo.tryGetPhysicalId(), page2.data().get(0).tryGetPhysicalId());
    }

    public void testCreatedIds() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooKlas = fooTypes.fooType();
        var fooNameField = fooTypes.fooNameField();
        var fooQuxField = fooTypes.fooQuxField();
        var foo = addInstance(MockUtils.createFoo(fooTypes, true));
        var qux = (ClassInstance) foo.getField(fooQuxField);
        addInstance(qux);

        var page = instanceQueryService.query(
                InstanceQueryBuilder.newBuilder(fooKlas)
                        .fields(
                                InstanceQueryField.create(fooNameField, foo.getField(fooNameField)),
                                InstanceQueryField.create(fooQuxField, qux)
                        )
                        .newlyCreated(List.of(requireNonNull(qux.tryGetId())))
                        .build(),
                instanceRepository,
                typeRepository
        );
        Assert.assertEquals(1, page.total());
        Assert.assertEquals(foo.tryGetPhysicalId(), page.data().get(0).tryGetPhysicalId());
    }

}