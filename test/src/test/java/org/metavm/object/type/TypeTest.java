package org.metavm.object.type;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;

@Slf4j
public class TypeTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testArrayTypeAssignability() {
        var t1 = new ArrayType(Types.getUncertainType(Types.getNeverType(), Types.getNullableAnyType()), ArrayKind.DEFAULT);
        var t2 = new ArrayType(Types.getNullableStringType(), ArrayKind.DEFAULT);
        Assert.assertTrue(t1.isAssignableFrom(t2));
    }

    public void testNullAssignable() {
        var t = Types.getNullableType(new ArrayType(Types.getNullableAnyType(), ArrayKind.DEFAULT));
        Assert.assertTrue(t.isAssignableFrom(t));
    }

}
