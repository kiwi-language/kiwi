package org.metavm.flow;

import junit.framework.TestCase;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Assert;
import org.metavm.flow.DominatorUtils.Node;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DominatorUtilsTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        nodeMap.clear();
    }

    public void testComputeDominators() {
        Node<String> r = new Node<>("r");
        Node<String> a = new Node<>("a");
        Node<String> b = new Node<>("b");
        Node<String> c = new Node<>("c");
        Node<String> d = new Node<>("d");
        Node<String> e = new Node<>("e");
        Node<String> f = new Node<>("f");
        Node<String> g = new Node<>("g");
        Node<String> h = new Node<>("h");
        Node<String> i = new Node<>("i");
        Node<String> j = new Node<>("j");
        Node<String> k = new Node<>("k");
        Node<String> l = new Node<>("l");

//        List<Node<String>> nodes = List.of(r, a, b, c, d, e, f, g, h, i, j, k);

        r.addNext(List.of(a, b, c));
        a.addNext(d);
        b.addNext(List.of(a, d, e));
        c.addNext(List.of(f, g));
        d.addNext(l);
        e.addNext(h);
        f.addNext(i);
        g.addNext(List.of(i, j));
        h.addNext(List.of(k, e));
        i.addNext(k);
        j.addNext(i);
        k.addNext(List.of(r, i));
        l.addNext(h);

//        display(r);

        DominatorUtils.computeDominators(r);
        Assert.assertEquals(r, a.dom);
        Assert.assertEquals(r, b.dom);
        Assert.assertEquals(r, c.dom);
        Assert.assertEquals(r, d.dom);
        Assert.assertEquals(r, e.dom);
        Assert.assertEquals(r, h.dom);
        Assert.assertEquals(r, i.dom);
        Assert.assertEquals(r, k.dom);
        Assert.assertEquals(c, f.dom);
        Assert.assertEquals(c, g.dom);
        Assert.assertEquals(g, j.dom);
        Assert.assertEquals(d, l.dom);
    }

    private final org.graphstream.graph.Graph gsGraph = new SingleGraph("Graph");

    public void display(Node<String> node) {
        System.setProperty("org.graphstream.ui", "swing");
        gsGraph.setAttribute("ui.title", "Dominator Test");
        getNode(node);
        gsGraph.display();
    }

    private final Map<Node<String>, org.graphstream.graph.Node> nodeMap = new IdentityHashMap<>();

    private int nextId;

    private org.graphstream.graph.Node getNode(Node<String> node) {
        org.graphstream.graph.Node gsNode;
        if ((gsNode = nodeMap.get(node)) != null) return gsNode;
        gsNode = gsGraph.addNode(nextId());
        nodeMap.put(node, gsNode);
        gsNode.setAttribute("ui.label", node.value);
        gsNode.setAttribute("ui.style", "fill-color: #ffffff; text-size:24px;");
        for (var next : node.next) {
            var nextNode = getNode(next);
            gsGraph.addEdge(nextId(), gsNode.getId(), nextNode.getId(), true);
        }
        return gsNode;
    }

    private String nextId() {
        return (nextId++) + "";
    }


}