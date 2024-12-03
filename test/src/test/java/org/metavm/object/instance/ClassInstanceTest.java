package org.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.ClassInstanceParam;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassInstanceTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(ClassInstanceTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testToDTO() {
        var fooType = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(fooType, true);
        InstanceDTO instanceDTO = foo.toDTO();
        Assert.assertTrue(instanceDTO.param() instanceof ClassInstanceParam);
        ClassInstanceParam paramDTO = (ClassInstanceParam) instanceDTO.param();
        Assert.assertEquals(foo.getKlass().getReadyFields().size(), paramDTO.fields().size());
        TestUtils.logJSON(logger, instanceDTO);
    }

    public void testIsChild() {
        var fooTypes = MockUtils.createFooTypes(true);
        ClassInstance foo = ClassInstanceBuilder.newBuilder(fooTypes.fooType().getType())
                .data(Map.of(
                        fooTypes.fooNameField(),
                        Instances.stringInstance("foo"),
                        fooTypes.fooBarsField(),
                        new ArrayInstance(fooTypes.barChildArrayType(), List.of()).getReference(),
                        fooTypes.fooBazListField(),
                        new ArrayInstance(fooTypes.bazArrayType()).getReference()
                ))
                .build();
        var barArray = foo.getField(fooTypes.fooBarsField()).resolveDurable();
        Assert.assertTrue(barArray.isChildOf(foo, fooTypes.fooBarsField()));
    }

    public void test_add_not_null_field() {
        Klass type = TestUtils.newKlassBuilder("Lab", null).build();
        Field titleField = FieldBuilder
                .newBuilder("title", type, Types.getStringType())
                .build();
        type.setTitleField(titleField);
        Field statusField = FieldBuilder
                .newBuilder("status", type, Types.getIntType())
                .defaultValue(Instances.intInstance(0))
                .state(MetadataState.READY)
                .build();
        ClassInstance instance = ClassInstance.create(
                Map.of(
                        titleField,
                        Instances.stringInstance("Big Foo")
                ),
                type.getType()
        );
        instance.initId(PhysicalId.of(10001L, 0L, TestUtils.mockClassType()));
        Assert.assertEquals(statusField.getDefaultValue(), instance.getField(statusField));
    }

    public void testTitle() {
        var type = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(type, true);
        Assert.assertEquals("foo", foo.getTitle());
        var dto = foo.toDTO();
        Assert.assertEquals("foo", dto.title());
    }

    public void testEphemeral() {
        var flowKlass = TestUtils.newKlassBuilder("Flow", "Flow").build();
        var codeKlass = TestUtils.newKlassBuilder("Code", "Code").build();
        var nullableScopeType = new UnionType(Set.of(Types.getNullType(), codeKlass.getType()));
        var codeField = FieldBuilder.newBuilder("code", flowKlass, nullableScopeType)
                .isChild(true)
                .build();

        TestUtils.initEntityIds(flowKlass);
        var flow = ClassInstanceBuilder.newBuilder(flowKlass.getType())
                .data(Map.of(
                        codeField,
                        ClassInstanceBuilder.newBuilder(codeKlass.getType())
                                .ephemeral(true)
                                .build().getReference()
                ))
                .build();

        TestUtils.initInstanceIds(flow);
        Map<Id, Instance> id2instance = new HashMap<>();
        flow.forEachDescendant(instance -> id2instance.put(instance.tryGetId(), instance));
        var bin = new ByteArrayInputStream(InstanceOutput.toBytes(flow));
        var input = new InstanceInput(bin, id -> {
            var inst = id2instance.get(id);
            if (inst instanceof ClassInstance)
                return ClassInstance.allocateUninitialized(id);
            else
                throw new RuntimeException("Unexpected instance: " + inst);
        },
                i -> {},
                id -> {
                    if (flowKlass.idEquals(id))
                        return flowKlass;
                    if (codeKlass.idEquals(id))
                        return codeKlass;
                    throw new NullPointerException("Can not find type def for id: " + id);
                }, id -> null);
        var loadedFlow = (ClassInstance) input.readSingleMessageGrove();
        loadedFlow.logFields();
        Assert.assertTrue(loadedFlow.getField(codeField).isNull());
    }

    public void testReadWrite() {
        var bootResult = BootstrapUtils.bootstrap();
        var entityContextFactory = bootResult.entityContextFactory();
        var klassId = TestUtils.doInTransaction(() -> {
            try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
                var klass = context.bind(TestUtils.newKlassBuilder("Foo", "Foo")
                        .superType(StdKlass.entity.type())
                        .build());
                context.finish();
                return klass.getId();
            }
        });
        try (var context = entityContextFactory.newContext(TestConstants.APP_ID)) {
            var klass = context.getKlass(klassId);
            System.out.println(klass.getSuperType());
        }
    }

    public void testFieldTable() {
        var baseKlass = TestUtils.newKlassBuilder("Base").build();
        var derivedKlass = TestUtils.newKlassBuilder("Derived")
                .superType(baseKlass.getType())
                .build();
        var f1 = FieldBuilder.newBuilder("f1", baseKlass, PrimitiveType.longType).build();
        var f2 = FieldBuilder.newBuilder("f2", derivedKlass, PrimitiveType.longType).build();
        var f3 = FieldBuilder.newBuilder("f3", baseKlass, PrimitiveType.longType).build();
        Assert.assertEquals(0, f1.getOffset());
        Assert.assertEquals(2, f2.getOffset());
        Assert.assertEquals(1, f3.getOffset());
        var inst = new ClassInstance(
                null,
                Map.of(
                        f1, Instances.longInstance(1),
                        f2, Instances.longInstance(2),
                        f3, Instances.longInstance(3)
                ),
                derivedKlass
        );
        Assert.assertEquals(Instances.longInstance(1), inst.getField(f1));
        Assert.assertEquals(Instances.longInstance(2), inst.getField(f2));
        Assert.assertEquals(Instances.longInstance(3), inst.getField(f3));
    }

}