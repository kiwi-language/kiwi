package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Foo;
import tech.metavm.object.instance.rest.*;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.PojoMatcher;
import tech.metavm.util.TestUtils;

public class ClassInstanceTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClassInstanceTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testToFieldValueDTO_for_reference() {
        ClassInstance foo = MockRegistry.getFooInstance();
        FieldValueDTO fieldValueDTO = foo.toFieldValueDTO();
        Assert.assertEquals(foo.getTitle(), fieldValueDTO.getDisplayValue());
        Assert.assertTrue(fieldValueDTO instanceof ReferenceFieldValueDTO);
        ReferenceFieldValueDTO refFieldValueDTO = (ReferenceFieldValueDTO) fieldValueDTO;
        Assert.assertEquals((long) foo.getId(), refFieldValueDTO.getId());
    }

    public void testToFieldValueDTO_for_value() {
        ClassInstance foo = MockRegistry.getFooInstance();
        ClassInstance bar = foo.getClassInstance(MockRegistry.getField(Foo.class, "bar"));
        FieldValueDTO fieldValueDTO = bar.toFieldValueDTO();
        Assert.assertEquals(bar.getTitle(), fieldValueDTO.getDisplayValue());
        if(bar.isValue()) {
            Assert.assertTrue(fieldValueDTO instanceof InstanceFieldValueDTO);
            InstanceFieldValueDTO instFieldValueDTO = (InstanceFieldValueDTO) fieldValueDTO;
            MatcherAssert.assertThat(instFieldValueDTO.getInstance(), PojoMatcher.of(bar.toDTO()));
        }
        else {
            Assert.assertTrue(fieldValueDTO instanceof ReferenceFieldValueDTO);
            ReferenceFieldValueDTO refFieldValueDTO = (ReferenceFieldValueDTO) fieldValueDTO;
            Assert.assertEquals((long)bar.getId(), refFieldValueDTO.getId());
        }
    }

    public void testToDTO() {
        ClassInstance foo = MockRegistry.getFooInstance();
        InstanceDTO instanceDTO = foo.toDTO();
        Assert.assertTrue(instanceDTO.param() instanceof ClassInstanceParamDTO);
        ClassInstanceParamDTO paramDTO = (ClassInstanceParamDTO) instanceDTO.param();
        Assert.assertEquals(foo.getType().getFields().size(), paramDTO.fields().size());
        TestUtils.logJSON(LOGGER, instanceDTO);
    }

}