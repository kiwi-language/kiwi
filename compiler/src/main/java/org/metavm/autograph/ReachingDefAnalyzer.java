package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class ReachingDefAnalyzer extends SkipDiscardedVisitor {

    private Analyzer currentAnalyzer;
    private CfgNode currentCfgNode;
    private final Map<PsiParameterListOwner, Graph> graphs;

    public ReachingDefAnalyzer(Map<PsiParameterListOwner, Graph> graphs) {
        this.graphs = new HashMap<>(graphs);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        CfgNode parent = currentCfgNode;
        CfgNode cfgNode;
        if (currentAnalyzer != null && (cfgNode = currentAnalyzer.getGraph().nodeIndex().get(element)) != null) {
            currentCfgNode = cfgNode;
        }
        super.visitElement(element);
        currentCfgNode = parent;
    }

    private void processNameElement(PsiElement element) {
        var qnAndMode = QnFactory.getQnAndMode(element);
        if (qnAndMode != null) processName(element, qnAndMode);
        super.visitElement(element);
    }

    @Override
    public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
        processNameElement(reference);
    }

    @Override
    public void visitArrayAccessExpression(PsiArrayAccessExpression expression) {
        processNameElement(expression);
    }

    @Override
    public void visitVariable(PsiVariable variable) {
        processNameElement(variable);
    }

    @Override
    public void visitMethod(PsiMethod method) {
        var parentAnalyzer = currentAnalyzer;
        var subgraph = graphs.get(method);
        var analyzer = new Analyzer(subgraph);
        analyzer.visitForward();
        currentAnalyzer = analyzer;
        method.getParameterList().accept(this);
        if (method.getBody() != null) method.getBody().accept(this);
        currentAnalyzer = parentAnalyzer;
    }

    private void processName(PsiElement element, QnAndMode qnAndMode) {
        if (currentAnalyzer == null) return;
        if (currentCfgNode == null) return;
        var qn = qnAndMode.qualifiedName();
        if(qn instanceof CompositeQualifiedName) return;
        Definition def;
        if (qnAndMode.isRead()) {
            def = currentAnalyzer.getIn(currentCfgNode).getDef(qn);
        } else if (qnAndMode.isWrite()) {
            def = currentAnalyzer.getOut(currentCfgNode).getDef(qn);
        } else def = null;
        if (def != null) element.putUserData(Keys.DEFINITIONS, List.of(def));
    }

    private void aggregatePredecessorsDefinedIn(PsiElement element) {
        var graph = currentAnalyzer.getGraph();
        var prev = graph.stmtPrev().get(element);
        Set<QualifiedName> definedIn = new HashSet<>();
        for (CfgNode p : prev) {
            var defOut = currentAnalyzer.getOut(p);
            definedIn.addAll(defOut.value.keySet());
        }
        element.putUserData(Keys.DEFINED_VARS_IN, definedIn);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        aggregatePredecessorsDefinedIn(statement);
        super.visitIfStatement(statement);
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        aggregatePredecessorsDefinedIn(statement);
        super.visitForStatement(statement);
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        aggregatePredecessorsDefinedIn(statement);
        super.visitWhileStatement(statement);
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        aggregatePredecessorsDefinedIn(statement);
        var parent = currentCfgNode;
        currentCfgNode = currentAnalyzer.getGraph().nodeIndex().get(statement.getIteratedValue());
        statement.getIterationParameter().accept(this);
        currentCfgNode = parent;
        Objects.requireNonNull(statement.getIteratedValue()).accept(this);
        Objects.requireNonNull(statement.getBody()).accept(this);
    }

    @Override
    public void visitTryStatement(PsiTryStatement statement) {
        aggregatePredecessorsDefinedIn(statement);
        super.visitTryStatement(statement);
    }

    @Override
    public void visitCatchSection(PsiCatchSection section) {
        aggregatePredecessorsDefinedIn(section);
        Objects.requireNonNull(section.getCatchBlock()).accept(this);
    }

    private record NodeState(Map<QualifiedName, Definition> value) {

        static NodeState createEmpty() {
            return new NodeState(Map.of());
        }

        static NodeState createFrom(NodeState state) {
            return new NodeState(state.value);
        }

        NodeState subtract(Set<QualifiedName> names) {
            Map<QualifiedName, Definition> value = new HashMap<>(this.value);
            for (QualifiedName name : names) {
                value.remove(name);
            }
            return new NodeState(value);
        }

        NodeState merge(NodeState that) {
            var mergedValue = new HashMap<>(value);
            mergedValue.putAll(that.value);
            return new NodeState(mergedValue);
        }

        Definition getDef(QualifiedName qn) {
            return value.get(qn);
        }

    }

    private static class Analyzer extends GraphVisitor<NodeState> {

        private final Map<CfgNode, NodeState> genMap = new HashMap<>();

        protected Analyzer(Graph graph) {
            super(graph);
        }

        @Override
        protected NodeState initState(CfgNode node) {
            return new NodeState(Map.of());
        }

        @Override
        protected boolean visitNode(CfgNode node) {
            var prefDevOut = getOut(node);
            var defIn = NodeState.createEmpty();
            for (CfgNode prev : node.getPrev()) {
                defIn = defIn.merge(getOut(prev));
            }
            NodeState defOut;
            Scope scope = node.getElement().getUserData(Keys.SCOPE);
            if (scope != null) {
                if (!genMap.containsKey(node)) {
                    var symbols = new HashMap<QualifiedName, Definition>();
                    var newlyDefined = new HashSet<>(scope.getDefined());
                    newlyDefined.addAll(scope.getModified());
                    for (QualifiedName name : newlyDefined) {
                        symbols.put(name, new Definition(node.getElement(), name));
                    }
                    genMap.put(node, new NodeState(symbols));
                }
                defOut = NodeState.createFrom(genMap.get(node)).merge(defIn.subtract(scope.getModified()));
            } else {
                if(!canIgnore(node)) {
                    throw new RuntimeException("Missing scope from " + node.getElement());
                }
                defOut = defIn;
            }
            setIn(node, defIn);
            setOut(node, defOut);
            return !prefDevOut.equals(defOut);
        }
    }

}
