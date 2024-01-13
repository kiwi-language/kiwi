package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import java.util.Map;

public class ClassInstanceTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClassInstanceTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testToFieldValueDTO_for_reference() {
        try (var context = MockRegistry.newContext(10L)) {
            ClassInstance foo = MockRegistry.getFooInstance();
            FieldValue fieldValueDTO = foo.toFieldValueDTO();
            Assert.assertEquals(foo.getTitle(), fieldValueDTO.getDisplayValue());
            Assert.assertTrue(fieldValueDTO instanceof ReferenceFieldValue);
            ReferenceFieldValue refFieldValueDTO = (ReferenceFieldValue) fieldValueDTO;
            Assert.assertEquals((long) foo.tryGetPhysicalId(), refFieldValueDTO.getId());
        }
    }

    public void testToFieldValueDTO_for_value() {
        try (var context = MockRegistry.newContext(10L)) {
            ClassInstance foo = MockRegistry.getFooInstance();
            ClassInstance bar = foo.getClassInstance(MockRegistry.getField(Foo.class, "bar"));
            FieldValue fieldValueDTO = bar.toFieldValueDTO();
            Assert.assertEquals(bar.getTitle(), fieldValueDTO.getDisplayValue());
            if (bar.isValue()) {
                Assert.assertTrue(fieldValueDTO instanceof InstanceFieldValue);
                InstanceFieldValue instFieldValueDTO = (InstanceFieldValue) fieldValueDTO;
                MatcherAssert.assertThat(instFieldValueDTO.getInstance(), PojoMatcher.of(bar.toDTO()));
            } else {
                Assert.assertTrue(fieldValueDTO instanceof ReferenceFieldValue);
                ReferenceFieldValue refFieldValueDTO = (ReferenceFieldValue) fieldValueDTO;
                Assert.assertEquals(bar.getPhysicalId(), refFieldValueDTO.getId());
            }
        }
    }

    public void testToDTO() {
        try (var context = MockRegistry.newContext(10L)) {
            ClassInstance foo = MockRegistry.getFooInstance();
            InstanceDTO instanceDTO = foo.toDTO();
            Assert.assertTrue(instanceDTO.param() instanceof ClassInstanceParam);
            ClassInstanceParam paramDTO = (ClassInstanceParam) instanceDTO.param();
            Assert.assertEquals(foo.getType().getReadyFields().size(), paramDTO.fields().size());
            TestUtils.logJSON(LOGGER, instanceDTO);
        }
    }

    public void testIsChild() {
        Field fooBarField = MockRegistry.getField(Foo.class, "bar");
        ClassInstance foo = MockRegistry.getFooInstance();
        ClassInstance bar = foo.getClassInstance(fooBarField);
        Assert.assertTrue(foo.isChild(bar));
    }

    public void test_add_not_null_field() {
        ClassType type = ClassTypeBuilder.newBuilder("Lab", null).build();
        Field titleField = FieldBuilder
                .newBuilder("title", null, type, Instances.getStringType())
                .build();
        type.setTitleField(titleField);
        Field statusField = FieldBuilder
                .newBuilder("status", null, type, Instances.getIntType())
                .build();
        ClassInstance instance = ClassInstance.create(
                Map.of(
                        titleField,
                        Instances.stringInstance("Big Foo")
                ),
                type
        );
        instance.initId(PhysicalId.of(10001L));
        Assert.assertEquals(statusField.getDefaultValue(), instance.getField(statusField));
    }

    public void testTitle() {
        var type = MockUtils.createFooType(false);
        var foo = MockUtils.createFoo(type);
        Assert.assertEquals("foo", foo.getTitle());
        var dto = foo.toDTO();
        Assert.assertEquals("foo", dto.title());
    }

}