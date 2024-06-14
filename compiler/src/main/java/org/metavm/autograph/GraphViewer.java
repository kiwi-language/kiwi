package org.metavm.autograph;

import com.intellij.psi.*;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GraphViewer {

    private final org.graphstream.graph.Graph gsGraph = new SingleGraph("Graph");
    private int nextId;

    private final Map<CfgNode, Node> nodeMap = new HashMap<>();

    public void display(Graph graph) {
        System.setProperty("org.graphstream.ui", "swing");
        gsGraph.setAttribute("ui.title", graph.title());
        getNode(graph.entry());
        gsGraph.display();
    }

    private Node getNode(CfgNode cfgNode) {
        Node node;
        if ((node = nodeMap.get(cfgNode)) != null) return node;
        node = gsGraph.addNode(nextId());
        nodeMap.put(cfgNode, node);
        node.setAttribute("ui.label", getLabel(cfgNode));
        node.setAttribute("ui.style", "fill-color: #ffffff; text-size:24px;");
        for (CfgNode next : cfgNode.getNext()) {
            var nextNode = getNode(next);
            gsGraph.addEdge(nextId(), node.getId(), nextNode.getId(), true);
        }
        return node;
    }

    private String getLabel(CfgNode node) {
        var element = node.getElement();
        switch (element) {
            case PsiExpressionStatement exprStmt:
                return extractContent(exprStmt.getExpression());
            case PsiExpression expr:
                return extractContent(expr);
            case PsiParameterList paramList:
                return "parameters " + extractContent(paramList);
            case PsiDeclarationStatement declStmt:
                StringBuilder builder = new StringBuilder("declare ");
                for (int i = 0; i < declStmt.getDeclaredElements().length; i++) {
                    if (i > 0) builder.append(",");
                    var declEle = declStmt.getDeclaredElements()[i];
                    builder.append(extractContent(declEle));
                }
                return builder.toString();
            case PsiReturnStatement returnStmt:
                var returnValue = returnStmt.getReturnValue();
                return "return" + (returnValue != null ? " " + extractContent(returnValue) : "");
            case PsiThrowStatement throwStmt:
                return "throw " + extractContent(Objects.requireNonNull(throwStmt.getException()));
            case null: return "null";
            default: return element.toString();
        }
    }

    private String extractContent(PsiElement element) {
        var str = element.toString();
        int colonIdx;
        if((colonIdx = str.indexOf(':')) != -1) return str.substring(colonIdx + 1);
        else return str;
    }

    private String nextId() {
        return (nextId++) + "";
    }

}
