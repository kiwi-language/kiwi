package org.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.InstanceQuery;
import org.metavm.entity.InstanceQueryBuilder;
import org.metavm.entity.InstanceQueryField;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.EntityReference;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.mocks.MockInstanceRepository;
import org.metavm.object.type.TypeDefRepository;
import org.metavm.object.type.mocks.MockTypeDefRepository;
import org.metavm.util.ContextUtil;
import org.metavm.util.MockUtils;
import org.metavm.util.TestConstants;
import org.metavm.util.TestUtils;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class InstanceQueryServiceTest extends TestCase {

    private MemInstanceSearchServiceV2 instanceSearchService;
    private InstanceQueryService instanceQueryService;
    private MockInstanceRepository instanceRepository;
    private TypeDefRepository typeRepository;
    private long nextTreeId;

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
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
        var foo = addInstance(MockUtils.createFoo(fooTypes, this::nextRootId));
        var qux = foo.getField(fooQuxField);
        var baz = foo.getInstanceArray(fooBazListField).getInstance(0);

        var query = InstanceQueryBuilder.newBuilder(fooKlass)
                .fields(
                        InstanceQueryField.create(
                                fooNameField,
                                foo.getField(fooNameField)
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
        Assert.assertEquals(foo.tryGetTreeId(), ((EntityReference) page.items().getFirst()).tryGetTreeId());
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
        var foo = addInstance(MockUtils.createFoo(fooTypes, this::nextRootId));
        InstanceQuery query2 = InstanceQueryBuilder.newBuilder(fooKlas)
                .fields(InstanceQueryField.create(
                        fooNameField,
                        foo.getField(fooNameField)
                ))
                .build();
        var page2 = instanceQueryService.query(query2,
                instanceRepository, typeRepository);
        Assert.assertEquals(1, page2.total());
        Assert.assertEquals(foo.tryGetTreeId(), ((EntityReference) page2.items().getFirst()).tryGetTreeId());
    }

    public void testCreatedIds() {
        var fooTypes = MockUtils.createFooTypes(true);
        var fooKlas = fooTypes.fooType();
        var fooNameField = fooTypes.fooNameField();
        var fooQuxField = fooTypes.fooQuxField();
        var foo = addInstance(MockUtils.createFoo(fooTypes, this::nextRootId));
        var qux = foo.getField(fooQuxField).resolveObject();
        addInstance(qux);

        var page = instanceQueryService.query(
                InstanceQueryBuilder.newBuilder(fooKlas)
                        .fields(
                                InstanceQueryField.create(fooNameField, foo.getField(fooNameField)),
                                InstanceQueryField.create(fooQuxField, qux.getReference())
                        )
                        .newlyCreated(List.of(requireNonNull(qux.tryGetId())))
                        .build(),
                instanceRepository,
                typeRepository
        );
        Assert.assertEquals(1, page.total());
        Assert.assertEquals(foo.tryGetTreeId(), ((EntityReference) page.items().getFirst()).tryGetTreeId());
    }

    private Id nextRootId() {
        return PhysicalId.of(nextTreeId++, 0);
    }

}