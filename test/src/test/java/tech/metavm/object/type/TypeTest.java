package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.object.type.rest.dto.ClassTypeParam;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.util.MockUtils;

public class TypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testDTO() {
        var fooType = MockUtils.createFooTypes(true).fooType();
        var typeDTO = fooType.toDTO();
        var param = (ClassTypeParam) typeDTO.param();
        for (FieldDTO field : param.fields()) {
            Assert.assertNotNull("字段" + field.name() + "的typeId为空", field.typeId());
        }
    }

}
