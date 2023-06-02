package tech.metavm.transpile.ir.gen2;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.*;

import java.io.Serializable;
import java.util.List;

public class GraphTest extends TestCase {

    private Graph graph;

    @Override
    protected void setUp() throws Exception {
        graph = new GraphImpl();
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
        Assert.assertTrue(graph.addExtension(wn, x));
        // X <= Y
        Assert.assertTrue(graph.addExtension(x, y));
        // Y <= CS
        Assert.assertTrue(graph.addExtension(y, charSequence));
        // List<X> <= List<? extends Number>
        Assert.assertTrue(graph.addExtension(list_x, list_range));
        // X <= Ser
        Assert.assertTrue(graph.addExtension(x, serializable));
        // Y <= Ser
        Assert.assertTrue(graph.addExtension(y, serializable));
        // Y >= Str
        Assert.assertTrue(graph.addExtension(str, y));

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
        Assert.assertTrue(graph.addExtension(listRange, listX));
        Assert.assertTrue(graph.addExtension(wn, x ));
        var xSolution = graph.getSolution().get(x);
        Assert.assertEquals(new CaptureType((IRWildCardType) range), xSolution);
    }

    public void testConstructorResolve() {

    }

}

