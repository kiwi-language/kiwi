package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StandardTypes;

import java.util.List;
import java.util.Set;

public class TypesTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testGetCommonSuperTypes() {
        var c1 = KlassBuilder.newBuilder("c1", null).build();
        var c2 = KlassBuilder.newBuilder("c1", null)
                .superClass(c1.getType()).build();
        var c3 = KlassBuilder.newBuilder("c3", null)
                .superClass(c1.getType()).build();
        Assert.assertEquals(c1.getType(), Types.getLeastUpperBound(List.of(c1.getType(), c2.getType(), c3.getType())));
        Assert.assertEquals(c1.getType(), Types.getLeastUpperBound(List.of(c2.getType(), c3.getType())));
    }

    public void testGetCommonSuperTypes_union() {
        var c1 = KlassBuilder.newBuilder("c1", null).build();
        var c2 = KlassBuilder.newBuilder("c2", null)
                .superClass(c1.getType()).build();
        var c3 = KlassBuilder.newBuilder("c3", null)
                .superClass(c1.getType()).build();
        var u1 = new UnionType(Set.of(c2.getType(), c3.getType()));
        var cst = Types.getLeastUpperBound(List.of(c1.getType(), u1));
        Assert.assertEquals(c1.getType(), cst);
    }

    public void testGetCommonSuperTypes_intersection() {
        var c0 = KlassBuilder.newBuilder("c0", null).build();
        var c1 = KlassBuilder.newBuilder("c1", null).superClass(c0.getType()).build();
        var c2 = KlassBuilder.newBuilder("c2", null)
                .superClass(c0.getType()).build();
        var c3 = KlassBuilder.newBuilder("c3", null)
                .superClass(c1.getType()).build();
        var intersect = new IntersectionType(Set.of(c2.getType(), c3.getType()));
        var cst = Types.getLeastUpperBound(List.of(c1.getType(), intersect));
        Assert.assertEquals(c1.getType(), cst);
    }

    public void testGetCommonSuperTypes_interface() {
        var c1 = KlassBuilder.newBuilder("c1", null)
                .kind(ClassKind.INTERFACE).build();
        var c2 = KlassBuilder.newBuilder("c2", null)
                .interfaces(c1.getType()).build();
        var c3 = KlassBuilder.newBuilder("c3", null)
                .interfaces(c1.getType()).build();
        var cst = Types.getLeastUpperBound(List.of(c2.getType(), c3.getType()));
        Assert.assertEquals(c1.getType(), cst);
    }

    public void testGetCommonSuperTypes_nullable() {
        var c1 = KlassBuilder.newBuilder("c1", null).build();
        var c2 = KlassBuilder.newBuilder("c2", null)
                .superClass(c1.getType()).build();
        var c3 = KlassBuilder.newBuilder("c3", null)
                .superClass(c1.getType()).build();
        var nullable_c3 = new UnionType(Set.of(StandardTypes.getNullType(), c3.getType()));
        var cst = Types.getLeastUpperBound(List.of(c2.getType(), nullable_c3));
        Assert.assertEquals(StandardTypes.getNullableAnyType(), cst);
    }

}