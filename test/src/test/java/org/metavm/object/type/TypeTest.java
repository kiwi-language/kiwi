package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;

import java.util.List;

public class TypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
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

        var anyType = Types.getAnyType();
        var nullableAnyType = Types.getUnionType(List.of(anyType, Types.getNullType()));
        Assert.assertFalse(anyType.isAssignableFrom(nullableAnyType));
        Assert.assertFalse(anyType.isConvertibleFrom(nullableAnyType));
    }

    public void testArrayTypeAssignability() {
        var t1 = new ArrayType(Types.getUncertainType(Types.getNeverType(), Types.getNullableAnyType()), ArrayKind.READ_WRITE);
        var t2 = new ArrayType(Types.getNullableStringType(), ArrayKind.READ_WRITE);
        Assert.assertTrue(t1.isAssignableFrom(t2));
    }

}
