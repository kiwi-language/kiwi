package tech.metavm.transpile.ir.gen;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.*;

import java.io.Serializable;
import java.util.List;

public class TypeGraphTest extends TestCase {

    private TypeGraph graph;

    @Override
    protected void setUp() throws Exception {
        graph = new TypeGraphImpl();
    }

    public void test() {
        var x = new MockXType("X");
        var y = new MockXType("Y");
        var number = IRTestUtil.createClass(Number.class);

        var wn = IRTestUtil.createClass(WretchedNumber.class);
        var charSequence = IRTestUtil.createClass(CharSequence.class);
        var str = IRTestUtil.createClass(String.class);
        var list = IRTestUtil.createClass(List.class);
        var list_x = PType.create(list, x);
        var list_range = PType.create(list, TypeRange.extensionOf(number));
        var serializable = IRTestUtil.createClass(Serializable.class);

        // X <= WN
        Assert.assertTrue(graph.addRelation(TypeRelation.extends_(wn, x)));
        // X <= Y
        Assert.assertTrue(graph.addRelation(TypeRelation.extends_(x, y)));
        // Y <= CS
        Assert.assertTrue(graph.addRelation(TypeRelation.extends_(y, charSequence)));
        // List<X> <= List<? extends Number>
        Assert.assertTrue(graph.addRelation(TypeRelation.extends_(list_x, list_range)));
        // X|Y <= Ser
        Assert.assertTrue(
                graph.addRelation(
                        TypeRelation.extends_(TypeUnion.of(x, y), serializable)
                )
        );
        // X|Y >= Str
        Assert.assertTrue(
                graph.addRelation(
                        TypeRelation.super_(TypeUnion.of(x, y), str)
                )
        );

        var solution = graph.getSolution();

        Assert.assertEquals(wn, solution.get(x));
        Assert.assertEquals(TypeUnion.of(str, wn), solution.get(y));
    }

    public void testContains() {
        var number = IRTestUtil.createClass(Number.class);
        var wn = IRTestUtil.createClass(WretchedNumber.class);
        var range = TypeRange.between(wn, number);
        var x = new MockXType("X");
        var list = IRTestUtil.createClass(List.class);
        var listRange = PType.create(list, range);
        var listX = PType.create(list, x);
        Assert.assertTrue(graph.addRelation(TypeRelation.super_(listX, listRange)));
        Assert.assertTrue(graph.addRelation(TypeRelation.super_(x, wn)));
        var xSolution = graph.getSolution().get(x);
        Assert.assertEquals(TypeRange.between(wn, number), xSolution);
    }

}

@SuppressWarnings("NullableProblems")
class WretchedNumber extends Number implements CharSequence {

    @Override
    public int length() {
        return 0;
    }

    @Override
    public char charAt(int index) {
        return 0;
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        //noinspection ConstantConditions
        return null;
    }

    @Override
    public int intValue() {
        return 0;
    }

    @Override
    public long longValue() {
        return 0;
    }

    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }
}

class MockXType extends XType {

    public MockXType(String name) {
        super(name);
    }
}