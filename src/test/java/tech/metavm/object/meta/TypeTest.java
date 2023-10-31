package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.meta.rest.dto.ClassTypeParam;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

public class TypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testDTO() {
        ClassType fooType = MockRegistry.getClassType(ClassType.class);
        TypeDTO typeDTO = fooType.toDTO();
        ClassTypeParam param = (ClassTypeParam) typeDTO.param();
        for (FieldDTO field : param.fields()) {
            Assert.assertNotNull("字段" + field.name() + "的typeId为空", field.typeId());
        }
    }

}
