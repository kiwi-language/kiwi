package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.util.MockUtils;

public class TypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testDTO() {
        var fooType = MockUtils.createFooTypes(true).fooType();
        var typeDTO = fooType.toDTO();
        for (FieldDTO field : typeDTO.fields()) {
            Assert.assertNotNull("typeId is missing for field " + field.name() , field.type());
        }
    }

}
