package org.metavm.autograph;

import com.intellij.psi.PsiBreakStatement;
import com.intellij.psi.PsiContinueStatement;
import com.intellij.psi.PsiEmptyStatement;

import java.util.*;

public abstract class GraphVisitor<S> {

    private final Map<CfgNode, S> in = new HashMap<>();
    private final Map<CfgNode, S> out = new HashMap<>();

    private final Graph graph;

    protected GraphVisitor(Graph graph) {
        this.graph = graph;
        for (CfgNode node : graph.nodeIndex().values()) {
            in.put(node, initState(node));
            out.put(node, initState(node));
        }
    }

    public final boolean canIgnore(CfgNode node) {
        var astNode = node.getElement();
        return astNode instanceof PsiBreakStatement || astNode instanceof PsiContinueStatement
                || astNode instanceof PsiEmptyStatement;
    }

    public final void visitForward() {
        visit(true);
    }

    public final void visitReverse() {
        visit(false);
    }

    public final S getOut(CfgNode node) {
        return out.get(node);
    }

    public final S getIn(CfgNode node) {
        return in.get(node);
    }

    public final void setIn(CfgNode node, S state) {
        in.put(node, state);
    }

    public final void setOut(CfgNode node, S state) {
        out.put(node, state);
    }

    protected abstract S initState(CfgNode node);

    protected abstract boolean visitNode(CfgNode node);

    public Graph getGraph() {
        return graph;
    }

    private void visit(boolean forward) {
        visitDFS(forward);
    }

    private void visitDFS(boolean forward) {
        LinkedList<StackFrame> stack = new LinkedList<>();
        Set<CfgNode> closed = new HashSet<>();
        if (forward) stack.push(new StackFrame(graph.entry(), true));
        else graph.exit().forEach(exit -> stack.push(new StackFrame(exit, false)));
        while (!stack.isEmpty()) {
            StackFrame frame = stack.peek();
            if (!frame.processed) {
                frame.processed = true;
                closed.add(frame.node);
                frame.shouldRevisit = visitNode(frame.node);
            }
            if (frame.hasNextChild()) {
                var child = frame.nextChild();
                if (frame.shouldRevisit || !closed.contains(child)) stack.push(new StackFrame(child, forward));
            } else stack.pop();
        }
    }

    private void dfs(CfgNode node) {
    }

    private static class StackFrame {

        private final CfgNode node;
        private final Iterator<CfgNode> childIterator;
        boolean processed;
        boolean shouldRevisit;

        private StackFrame(CfgNode node, boolean forward) {
            this.node = node;
            List<CfgNode> children = new ArrayList<>(forward ? node.getNext() : node.getPrev());
            children.sort((n1, n2) -> Boolean.compare(node.isBackTarget(n2), node.isBackTarget(n1)));
            childIterator = children.iterator();
        }

        boolean hasNextChild() {
            return childIterator.hasNext();
        }

        CfgNode nextChild() {
            return childIterator.next();
        }

    }

    @SuppressWarnings("unused")
    private void visitBFS(boolean forward) {
        Queue<CfgNode> open = new LinkedList<>();
        Set<CfgNode> closed = new HashSet<>();
        if (forward) open.offer(graph.entry());
        else open.addAll(graph.exit());
        while (!open.isEmpty()) {
            var node = open.poll();
            closed.add(node);
            boolean shouldRevisit = visitNode(node);
            List<CfgNode> children = forward ? node.getNext() : node.getPrev();
            for (CfgNode child : children) {
                if (shouldRevisit || !closed.contains(child)) open.offer(child);
            }
        }
    }

}
