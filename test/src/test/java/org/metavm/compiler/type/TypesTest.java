package org.metavm.compiler.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.compiler.util.List;

public class TypesTest extends TestCase {

    public void test() {
        var types = Types.instance;
        var ft1 = types.getFunctionType(List.of(PrimitiveType.STRING, PrimitiveType.STRING), PrimitiveType.INT);
        var ft2 = types.getFunctionType(List.of(PrimitiveType.STRING, PrimitiveType.STRING), PrimitiveType.INT);
        Assert.assertSame(ft1, ft2);

        var ut1 = types.getUnionType(List.of(PrimitiveType.NULL, PrimitiveType.INT));
        var ut2 = types.getUnionType(List.of(PrimitiveType.NULL, PrimitiveType.INT));
        Assert.assertTrue(List.isSorted(ut1.alternatives(), Types.instance::compare));
        Assert.assertSame(ut1, ut2);
    }

    public void testSort() {
        Assert.assertEquals(
                List.of(PrimitiveType.STRING, PrimitiveType.NULL),
                Types.instance.sorted(List.of(PrimitiveType.STRING, PrimitiveType.NULL))
        );
    }

}
