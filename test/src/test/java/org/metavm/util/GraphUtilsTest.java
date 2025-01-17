package org.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class GraphUtilsTest extends TestCase {

    public void test() {
        Node a = new Node("a"), b = new Node("b"), c = new Node("c"), d = new Node("d");
        a.addDep(b, d);
        b.addDep(c);
        c.addDep(a);

        var sccs = GraphUtils.tarjan(List.of(a,b,c,d));
        Assert.assertEquals(2, sccs.size());
        System.out.println(sccs.getFirst());
        System.out.println(sccs.get(1));
    }

    private static class Node extends GraphUtils.TarjaNode<Node> {

        final String label;
        final List<Node> dependencies = new ArrayList<>();

        private Node(String label) {
            this.label = label;
        }

        void addDep(Node...deps) {
            dependencies.addAll(List.of(deps));
        }

        @Override
        List<Node> getDependencies() {
            return dependencies;
        }

        @Override
        public String toString() {
            return label;
        }
    }

}