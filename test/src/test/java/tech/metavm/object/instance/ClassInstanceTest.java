package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.ClassTypeKey;
import tech.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassInstanceTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClassInstanceTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testToFieldValueDTO_for_reference() {
        var fooType = MockUtils.createFooTypes(true);
        var foo = ClassInstanceBuilder.newBuilder(fooType.fooType())
                .data(Map.of(
                        fooType.fooNameField(),
                        Instances.stringInstance("foo"),
                        fooType.fooBarsField(),
                        new ArrayInstance(
                                fooType.barChildArrayType(),
                                List.of()
                        ),
                        fooType.fooBazListField(),
                        new ArrayInstance(fooType.bazArrayType())
                ))
                .build();
        foo.initId(DefaultPhysicalId.ofObject(100000L, 0L, new ClassTypeKey("1")));
        FieldValue fieldValueDTO = foo.toFieldValueDTO();
        Assert.assertEquals(foo.getTitle(), fieldValueDTO.getDisplayValue());
        Assert.assertTrue(fieldValueDTO instanceof ReferenceFieldValue);
        ReferenceFieldValue refFieldValueDTO = (ReferenceFieldValue) fieldValueDTO;
        Assert.assertEquals(foo.tryGetId(), Id.parse(refFieldValueDTO.getId()));
    }

    public void testToDTO() {
        var fooType = MockUtils.createFooTypes(true);
        var foo = MockUtils.createFoo(fooType,true);
        InstanceDTO instanceDTO = foo.toDTO();
        Assert.assertTrue(instanceDTO.param() instanceof ClassInstanceParam);
        ClassInstanceParam paramDTO = (ClassInstanceParam) instanceDTO.param();
        Assert.assertEquals(foo.getKlass().getReadyFields().size(), paramDTO.fields().size());
        TestUtils.logJSON(LOGGER, instanceDTO);
    }

    public void testIsChild() {
        var fooTypes = MockUtils.createFooTypes(true);
        ClassInstance foo = ClassInstanceBuilder.newBuilder(fooTypes.fooType())
                .data(Map.of(
                        fooTypes.fooNameField(),
                        Instances.stringInstance("foo"),
                        fooTypes.fooBarsField(),
                        new ArrayInstance(fooTypes.barChildArrayType(), List.of()),
                        fooTypes.fooBazListField(),
                        new ArrayInstance(fooTypes.bazArrayType())
                ))
                .build();
        var barArray = (DurableInstance) foo.getField(fooTypes.fooBarsField());
        Assert.assertTrue(foo.isChild(barArray));
    }

    public void test_add_not_null_field() {
        Klass type = ClassTypeBuilder.newBuilder("Lab", null).build();
        Field titleField = FieldBuilder
                .newBuilder("title", null, type, StandardTypes.getStringType())
                .build();
        type.setTitleField(titleField);
        Field statusField = FieldBuilder
                .newBuilder("status", null, type, StandardTypes.getLongType())
                .defaultValue(Instances.longInstance(0L))
                .state(MetadataState.READY)
                .build();
        ClassInstance instance = ClassInstance.create(
                Map.of(
                        titleField,
                        Instances.stringInstance("Big Foo")
                ),
                type
        );
        instance.initId(DefaultPhysicalId.ofObject(10001L, 0L, new ClassTypeKey("1")));
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
        var flowType = ClassTypeBuilder.newBuilder("Flow", "Flow").build();
        var scopeType = ClassTypeBuilder.newBuilder("Scope", "Scope").build();
        var nullableScopeType = new UnionType(null, Set.of(StandardTypes.getNullType(), scopeType.getType()));
        var rootScopeField = FieldBuilder.newBuilder("rootScope", "rootScope", flowType, nullableScopeType)
                .isChild(true)
                .build();

        TestUtils.initEntityIds(flowType);
        var flow = ClassInstanceBuilder.newBuilder(flowType)
                .data(Map.of(
                        rootScopeField,
                        ClassInstanceBuilder.newBuilder(scopeType)
                                .ephemeral(true)
                                .build()
                ))
                .build();

        TestUtils.initInstanceIds(flow);
        Map<Id, DurableInstance> id2instance = new HashMap<>();
        flow.accept(new StructuralVisitor() {

            @Override
            public Void visitDurableInstance(DurableInstance instance) {
                id2instance.put(instance.tryGetId(), instance);
                return super.visitDurableInstance(instance);
            }
        });

        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeValue(flow);

        var bin = new ByteArrayInputStream(bout.toByteArray());
        var input = new InstanceInput(bin, id -> {
            var inst = id2instance.get(id);
            if(inst instanceof ClassInstance classInst)
                return new ClassInstance(id, classInst.getKlass(), classInst.isEphemeral(), null);
            else
                throw new RuntimeException("Unexpected instance: " + inst);
        });
        var loadedFlow = (ClassInstance) input.readInstance();
        Assert.assertTrue(loadedFlow.getField(rootScopeField).isNull());
    }

}