package org.metavm.util;

import java.util.ArrayList;
import java.util.List;

class GraphUtils {

    public static <T extends TarjaNode<T>> List<List<T>> tarjan(List<T> nodes) {
        var tarjan = new Tarjan<T>();
        tarjan.findSCC(nodes);
        return tarjan.sccs;
    }

    private static class Tarjan<T extends TarjaNode<T>> {

        private int index;
        private final LinkedList<T> stack = new LinkedList<>();
        private final List<List<T>> sccs = new ArrayList<>();

        private void findSCC(List<T> nodes) {
            for (T node : nodes) {
                if (node.index == -1) {
                    findSCC(node);
                }
            }
        }

        private void findSCC(T v) {
            visitNode(v);
            for (T n : v.getDependencies()) {
                if (n.index == -1) {
                    findSCC(n);
                    v.lowLink = Math.min(v.lowLink, n.lowLink);
                } else if (stack.contains(n)) {
                    v.lowLink = Math.min(v.lowLink, n.index);
                }
            }
            if (v.lowLink == v.index) {
                addSCC(v);
            }
        }

        private void visitNode(T node) {
            node.index = node.lowLink = index++;
            stack.addFirst(node);
        }

        private void addSCC(TarjaNode<T> v) {
            List<T> cycle = new ArrayList<>();
            T n;
            do {
                n = stack.removeFirst();
                cycle.add(n);
            } while (n != v);
            sccs.add(cycle);
        }
    }

    public abstract static class TarjaNode<T extends TarjaNode<T>> {
        int index = -1;
        int lowLink;

        abstract List<T> getDependencies();
    }
}


