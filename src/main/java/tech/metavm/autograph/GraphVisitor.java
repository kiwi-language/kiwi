package tech.metavm.autograph;

import com.intellij.psi.PsiBreakStatement;
import com.intellij.psi.PsiContinueStatement;

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
        return astNode instanceof PsiBreakStatement || astNode instanceof PsiContinueStatement;
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
        Queue<CfgNode> open = new LinkedList<>();
        Set<CfgNode> closed = new HashSet<>();
        if(forward) open.offer(graph.entry());
        else open.addAll(graph.exit());
        while (!open.isEmpty()) {
            var node = open.poll();
            closed.add(node);
            boolean shouldRevisit = visitNode(node);
            List<CfgNode> children = forward ? node.getNext() : node.getPrev();
            for (CfgNode child : children) {
                if(shouldRevisit || !closed.contains(child)) open.offer(child);
            }
        }
    }

}
