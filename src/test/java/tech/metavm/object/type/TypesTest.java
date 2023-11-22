package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.StandardTypes;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import java.util.List;
import java.util.Set;

public class TypesTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testGetCommonSuperTypes() {
        var c1 = ClassBuilder.newBuilder("c1", null).build();
        var c2 = ClassBuilder.newBuilder("c1", null)
                .superClass(c1).build();
        var c3 = ClassBuilder.newBuilder("c3", null)
                .superClass(c1).build();
        Assert.assertSame(c1, Types.getLeastUpperBound(List.of(c1, c2, c3)));
        Assert.assertSame(c1, Types.getLeastUpperBound(List.of(c2, c3)));
    }

    public void testGetCommonSuperTypes_union() {
        var c1 = ClassBuilder.newBuilder("c1", null).build();
        var c2 = ClassBuilder.newBuilder("c2", null)
                .superClass(c1).build();
        var c3 = ClassBuilder.newBuilder("c3", null)
                .superClass(c1).build();

        var u1 = new UnionType(null, Set.of(c2, c3));
        var cst = Types.getLeastUpperBound(List.of(c1, u1));
        Assert.assertSame(c1, cst);
    }

    public void testGetCommonSuperTypes_intersection() {
        var c0 = ClassBuilder.newBuilder("c0", null).build();
        var c1 = ClassBuilder.newBuilder("c1", null).superClass(c0).build();
        var c2 = ClassBuilder.newBuilder("c2", null)
                .superClass(c0).build();
        var c3 = ClassBuilder.newBuilder("c3", null)
                .superClass(c1).build();
        var intersect = new IntersectionType(null, Set.of(c2, c3));
        var cst = Types.getLeastUpperBound(List.of(c1, intersect));
        Assert.assertSame(c1, cst);
    }

    public void testGetCommonSuperTypes_interface() {
        var c1 = ClassBuilder.newBuilder("c1", null)
                .category(TypeCategory.INTERFACE).build();
        var c2 = ClassBuilder.newBuilder("c2", null)
                .interfaces(c1).build();
        var c3 = ClassBuilder.newBuilder("c3", null)
                .interfaces(c1).build();
        var cst = Types.getLeastUpperBound(List.of(c2, c3));
        Assert.assertSame(c1, cst);
    }

    public void testGetCommonSuperTypes_nullable() {
        var c1 = ClassBuilder.newBuilder("c1", null).build();
        var c2 = ClassBuilder.newBuilder("c2", null)
                .superClass(c1).build();
        var c3 = ClassBuilder.newBuilder("c3", null)
                .superClass(c1).build();
        var nullable_c3 = new UnionType(null, Set.of(StandardTypes.getNullType(), c3));
        var cst = Types.getLeastUpperBound(List.of(c2, nullable_c3));
        Assert.assertEquals(StandardTypes.getNullableObjectType(), cst);
    }

}