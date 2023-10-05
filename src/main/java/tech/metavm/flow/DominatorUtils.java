package tech.metavm.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DominatorUtils {

    public static void computeDominators(Node<?> root) {
        computeDominatorsHelper(root);
    }

    private static <T> void computeDominatorsHelper(Node<T> root) {
        var nodes = new ArrayList<Node<T>>();
        root.dfs(null, nodes);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            var u = nodes.get(i);
            if (u.parent != null) {
                for (Node<T> p : u.prev) {
                    var newSemiDom = p.find().semiDom;
                    if (newSemiDom.order < u.semiDom.order) u.semiDom = newSemiDom;
                }
                u.semiDom.bucket.add(u);
            }
            for (Node<T> w : u.bucket) {
                var v = w.find();
                if (v.semiDom == w.semiDom) w.dom = w.semiDom;
                else w.dom = v;
            }
            if (u.parent != null) u.parent.union(u);
        }
        for (int i = 1; i < nodes.size(); i++) {
            var u = nodes.get(i);
            if (u.dom != u.semiDom) u.dom = u.dom.dom;
        }
    }

    public static class Node<T> {
        int order;
        final T value;
        boolean visited;
        Node<T> semiDom = this, label = this, dsu = this, parent, dom;
        final List<Node<T>> bucket = new ArrayList<>(), prev = new ArrayList<>(), next = new ArrayList<>();

        public Node(T value) {
            this.value = value;
        }

        void addNext(List<Node<T>> next) {
            next.forEach(this::addNext);
        }

        void addNext(Node<T> next) {
            this.next.add(next);
            next.prev.add(this);
        }

        void dfs(Node<T> parent, List<Node<T>> nodes) {
            if (visited) return;
            visited = true;
            order = nodes.size();
            this.parent = parent;
            nodes.add(this);
            for (Node<T> suc : next) {
                suc.dfs(this, nodes);
            }
        }

        Node<T> find() {
            return Objects.requireNonNull(find(0));
        }

        private Node<T> find(int x) {
            if (dsu == this) return x > 0 ? null : dsu;
            var v = dsu.find(x + 1);
            if (v == null) return this;
            if (dsu.label.semiDom.order < label.semiDom.order) label = dsu.label;
            dsu = v;
            return x > 0 ? v : label;
        }

        void union(Node<T> that) {
            that.dsu = this;
        }

        @Override
        public String toString() {
            return "Node [value=" + value + "]";
        }
    }

}