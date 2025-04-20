package org.metavm.compiler.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.compiler.util.List;

public class TypesTest extends TestCase {

    public void test() {
        var types = Types.instance;
        var ft1 = types.getFuncType(List.of(Types.instance.getStringType(), Types.instance.getStringType()), PrimitiveType.INT);
        var ft2 = types.getFuncType(List.of(Types.instance.getStringType(), Types.instance.getStringType()), PrimitiveType.INT);
        Assert.assertSame(ft1, ft2);

        var ut1 = types.getUnionType(List.of(PrimitiveType.NULL, PrimitiveType.INT));
        var ut2 = types.getUnionType(List.of(PrimitiveType.NULL, PrimitiveType.INT));
        Assert.assertTrue(List.isSorted(ut1.alternatives(), Types.instance::compare));
        Assert.assertSame(ut1, ut2);
    }

    public void testSort() {
        Assert.assertEquals(
                List.of(PrimitiveType.NULL, Types.instance.getStringType()),
                Types.instance.sorted(List.of(Types.instance.getStringType(), PrimitiveType.NULL))
        );
    }

}
