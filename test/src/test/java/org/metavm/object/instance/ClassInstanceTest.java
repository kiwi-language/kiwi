package org.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
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
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

//    public void testToDTO() {
//        var fooType = MockUtils.createFooTypes(true);
//        var foo = MockUtils.createFoo(fooType, true);
//        InstanceDTO instanceDTO = foo.toDTO();
//        Assert.assertTrue(instanceDTO.param() instanceof ClassInstanceParam);
//        ClassInstanceParam paramDTO = (ClassInstanceParam) instanceDTO.param();
//        Assert.assertEquals(foo.getInstanceKlass().getReadyFields().size(), paramDTO.fields().size());
//        TestUtils.logJSON(logger, instanceDTO);
//    }

    public void testIsChild() {
        var fooTypes = MockUtils.createFooTypes(true);
        ClassInstance foo = ClassInstanceBuilder.newBuilder(fooTypes.fooType().getType(), TmpId.random())
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
        Assert.assertFalse(barArray.isChildOf(foo));
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
                PhysicalId.of(10001L, 0L),
                Map.of(
                        titleField,
                        Instances.stringInstance("Big Foo")
                ),
                type.getType()
        );
        Assert.assertEquals(statusField.getDefaultValue(), instance.getField(statusField));
    }

    public void testTitle() {
        var type = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(type, TmpId::random);
        Assert.assertEquals("foo", foo.getTitle());
//        var dto = foo.toDTO();
//        Assert.assertEquals("foo", dto.title());
    }

    public void testEphemeral() {
        var flowKlass = TestUtils.newKlassBuilder("Flow", "Flow").build();
        var codeKlass = TestUtils.newKlassBuilder("Code", "Code").build();
        var nullableScopeType = new UnionType(Set.of(Types.getNullType(), codeKlass.getType()));
        var codeField = FieldBuilder.newBuilder("code", flowKlass, nullableScopeType).build();

        flowKlass.resetHierarchy();
        Assert.assertEquals(1, flowKlass.getSortedKlasses().size());
        Assert.assertEquals(1, flowKlass.getSortedFields().size());

        Assert.assertNotNull(codeKlass.tryGetId());


        var instanceMap = new HashMap<Id, Instance>();
        flowKlass.forEachDescendant(i -> instanceMap.put(i.getId(), i));
        codeKlass.forEachDescendant(i -> instanceMap.put(i.getId(), i));

        var flow = ClassInstanceBuilder.newBuilder(flowKlass.getType(), PhysicalId.of(1000L, 0))
                .data(Map.of(
                        codeField,
                        ClassInstanceBuilder.newBuilder(codeKlass.getType(), TmpId.random())
                                .ephemeral(true)
                                .build().getReference()
                ))
                .build();

        flow.forEachDescendant(instance -> instanceMap.put(instance.tryGetId(), instance));
        var bin = new ByteArrayInputStream(InstanceOutput.toBytes(flow));
        var input = new InstanceInput(bin, instanceMap::get,
                i -> {},
                id -> null);
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
        var inst = new MvClassInstance(
                null,
                Map.of(
                        f1, Instances.longInstance(1),
                        f2, Instances.longInstance(2),
                        f3, Instances.longInstance(3)
                ),
                derivedKlass,
                false
        );
        Assert.assertEquals(Instances.longInstance(1), inst.getField(f1));
        Assert.assertEquals(Instances.longInstance(2), inst.getField(f2));
        Assert.assertEquals(Instances.longInstance(3), inst.getField(f3));
    }

}