package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.util.MockUtils;

import java.util.List;

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

    public void testIsConvertible() {
        var doubleType = Types.getDoubleType();
        var longType = Types.getLongType();
        Assert.assertTrue(doubleType.isConvertibleFrom(longType));
        Assert.assertTrue(doubleType.isConvertibleFrom(doubleType));
        var unionType = Types.getUnionType(List.of(doubleType, longType));
        Assert.assertTrue(doubleType.isConvertibleFrom(unionType));

        var unionType1 = Types.getUnionType(List.of(doubleType, Types.getNullType()));
        Assert.assertTrue(unionType1.isConvertibleFrom(doubleType));
        Assert.assertTrue(unionType1.isConvertibleFrom(longType));
        Assert.assertTrue(unionType1.isConvertibleFrom(unionType1));
    }

}
