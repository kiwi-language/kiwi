package tech.metavm.autograph;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import tech.metavm.util.InternalException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static tech.metavm.util.NncUtils.diffSet;
import static tech.metavm.util.NncUtils.unionSet;

public class LivenessAnalyzer extends JavaRecursiveElementVisitor {

    private Analyzer currentAnalyzer;
    private final Map<PsiMethod, Graph> graphMap;

    public LivenessAnalyzer(Map<PsiMethod, Graph> graphMap) {
        this.graphMap = new HashMap<>(graphMap);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        super.visitElement(element);
        if (currentAnalyzer != null && element instanceof PsiStatement) {
            element.putUserData(Keys.LIVE_VARS_IN,
                    currentAnalyzer.getIn(currentAnalyzer.getGraph().nodeIndex().get(element)));
        }
    }

    @Override
    public void visitMethod(PsiMethod method) {
        var parentAnalyzer = currentAnalyzer;
        currentAnalyzer = new Analyzer(graphMap.get(method));
        currentAnalyzer.visitReverse();
        super.visitMethod(method);
        currentAnalyzer = parentAnalyzer;
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        super.visitIfStatement(statement);
        blockStatementLiveOut(statement);
        blockStatementLiveIn(statement, statement.getCondition());
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        super.visitWhileStatement(statement);
        blockStatementLiveOut(statement);
        blockStatementLiveIn(statement, statement.getCondition());
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        super.visitForeachStatement(statement);
        blockStatementLiveOut(statement);
        blockStatementLiveIn(statement, statement.getIteratedValue());
    }

    @Override
    public void visitTryStatement(PsiTryStatement statement) {
        super.visitTryStatement(statement);
        PsiElement entry = TranspileUtil.getTryStatementEntry(statement);
        if (entry == null) return;
        blockStatementLiveOut(statement);
        blockStatementLiveIn(statement, entry);
    }

    @Override
    public void visitCatchSection(PsiCatchSection section) {
        super.visitCatchSection(section);
        PsiElement entry = TranspileUtil.getCatchSectionEntry(section);
        if(entry == null) return;
        blockStatementLiveOut(section);
        blockStatementLiveIn(section, entry);
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        Set<QualifiedName> liveOut = currentAnalyzer.getOut(currentAnalyzer.getGraph().nodeIndex().get(statement));
        statement.putUserData(Keys.LIVE_VARS_OUT, liveOut);
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        super.visitForStatement(statement);
        var entry = TranspileUtil.getForStatementEntry(statement);
        if (entry == null) return;
        blockStatementLiveOut(statement);
        blockStatementLiveIn(statement, entry);
    }

    private void blockStatementLiveIn(PsiElement statement, PsiElement entry) {
        Set<QualifiedName> liveIn;
        CfgNode entryNode;
        if ((entryNode = currentAnalyzer.getGraph().nodeIndex().get(entry)) != null) {
            liveIn = new HashSet<>(currentAnalyzer.getIn(entryNode));
        } else {
            Set<QualifiedName> entryLiveIn;
            if ((entryLiveIn = entry.getUserData(Keys.LIVE_VARS_IN)) == null) {
                throw new InternalException("If no matching cfg node, must be a block statement: " + entry);
            }
            liveIn = new HashSet<>(entryLiveIn);
        }
        statement.putUserData(Keys.LIVE_VARS_IN, liveIn);
    }

    private void blockStatementLiveOut(PsiElement statement) {
        Set<QualifiedName> liveOut = new HashSet<>();
        for (CfgNode next : currentAnalyzer.getGraph().stmtNext().get(statement)) {
            liveOut.addAll(currentAnalyzer.getIn(next));
        }
        statement.putUserData(Keys.LIVE_VARS_OUT, liveOut);
    }

    private static class Analyzer extends GraphVisitor<Set<QualifiedName>> {

        protected Analyzer(Graph graph) {
            super(graph);
        }

        @Override
        protected Set<QualifiedName> initState(CfgNode node) {
            return new HashSet<>();
        }

        private boolean canIgnore(PsiElement element) {
            return element instanceof PsiBreakStatement
                    || element instanceof PsiContinueStatement
                    || element instanceof PsiThisExpression;
        }

        @Override
        protected boolean visitNode(CfgNode node) {
            var prevLiveIn = getIn(node);
            Scope scope;
            Set<QualifiedName> liveOut, liveIn;
            if ((scope = node.getElement().getUserData(Keys.SCOPE)) != null) {
                liveOut = new HashSet<>();
                Set<QualifiedName> gen = new HashSet<>(scope.getRead()),
                        kill = new HashSet<>(scope.getModified());
                for (CfgNode next : node.getNext()) liveOut.addAll(getIn(next));
                liveIn = unionSet(gen, diffSet(liveOut, kill));
            } else {
                if (!canIgnore(node.getElement())) {
                    throw new InternalException("Missing scope for element " + node.getElement());
                }
                liveIn = new HashSet<>();
                liveOut = new HashSet<>();
            }
            setIn(node, liveIn);
            setOut(node, liveOut);
            return prevLiveIn.equals(liveIn);
        }
    }

}
