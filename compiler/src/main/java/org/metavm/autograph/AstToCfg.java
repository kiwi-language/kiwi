package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class AstToCfg extends SkipDiscardedVisitor {

    private final LinkedList<GraphBuilder> builderStack = new LinkedList<>();
    private final Map<PsiParameterListOwner, Graph> graphs = new HashMap<>();
    private final LinkedList<PsiElement> lexicalScopes = new LinkedList<>();
    private GraphBuilder builder;

    @Override
    public void visitMethod(PsiMethod method) {
        processFunction(method.getName(), method);
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        processFunction("<lambda>", expression);
    }

    private void processFunction(String name, PsiParameterListOwner function) {
//     commented
//        if (builder != null) builder.addOrdinaryNode(function);
        builderStack.push(builder);
        builder = new GraphBuilder(name);
        enterLexicalScope(function);
        builder.enterSection(function);
        processBasicElement(function.getParameterList());
        if (function.getBody() != null) {
            if(function.getBody() instanceof PsiCodeBlock codeBlock) {
                for (PsiStatement statement : codeBlock.getStatements()) {
                    statement.accept(this);
                }
            }
            else {
                processExitStatement(function.getBody(), Set.of(PsiLambdaExpression.class), false, null);
            }
        }
        builder.exitSection(function);
        exitLexicalScope();
        graphs.put(function, builder.build());
        builder = builderStack.pop();
    }

    @Override
    public void visitSwitchLabeledRuleStatement(PsiSwitchLabeledRuleStatement statement) {
        var switchStmt = TranspileUtils.findParent(statement, Set.of(PsiSwitchStatement.class, PsiSwitchExpression.class));
        builder.newCondBranch(switchStmt);
        if (statement.getCaseLabelElementList() != null) {
            statement.getCaseLabelElementList().accept(this);
        }
        requireNonNull(statement.getBody()).accept(this);
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        processExitStatement(statement, Set.of(PsiSwitchExpression.class), false, null);
    }

    private NodeAndBlocks tryGetFinallyScopes(Set<Class<? extends PsiElement>> stopAt, @Nullable PsiIdentifier label) {
        List<PsiCodeBlock> included = new ArrayList<>();
        for (PsiElement scope : lexicalScopes) {
            if (scope instanceof PsiTryStatement tryStmt && tryStmt.getFinallyBlock() != null) {
                included.add(tryStmt.getFinallyBlock());
            }
            for (Class<? extends PsiElement> stopClass : stopAt) {
                if (stopClass.isInstance(scope) && (label == null || matchLabel(scope, label))) {
                    return new NodeAndBlocks(scope, included);
                }
            }
        }
        return new NodeAndBlocks(null, included);
    }

    private boolean matchLabel(PsiElement element, PsiIdentifier label) {
        if (element.getParent() instanceof PsiLabeledStatement labeledStatement) {
            return labeledStatement.getLabelIdentifier().getText().equals(label.getText());
        } else return false;
    }

    private List<PsiCodeBlock> tryGetCatchScopes(Set<Class<? extends PsiElement>> stopAt) {
        List<PsiCodeBlock> included = new ArrayList<>();
        for (PsiElement scope : lexicalScopes) {
            if (scope instanceof PsiTryStatement tryStmt && tryStmt.getCatchBlocks().length > 0) {
                included.addAll(Arrays.asList(tryStmt.getCatchBlocks()));
            }
            for (Class<? extends PsiElement> stopClass : stopAt) {
                if (stopClass.isInstance(scope)) break;
            }
        }
        return included;
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        builder.enterSection(statement);
        enterLexicalScope(statement);
        builder.beginBlockStatement(statement);
        builder.enterCondSection(statement);
        processBasicElement(requireNonNull(statement.getExpression()));
        requireNonNull(statement.getBody()).accept(this);
        builder.exitCondSection(statement);
        builder.endBlockStatement(statement);
        exitLexicalScope();
        builder.exitSection(statement);
    }

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        builder.enterSection(expression);
        enterLexicalScope(expression);
        builder.beginBlockStatement(expression);
        builder.enterCondSection(expression);
        processBasicElement(requireNonNull(expression.getExpression()));
        requireNonNull(expression.getBody()).accept(this);
        builder.exitCondSection(expression);
        builder.endBlockStatement(expression);
        exitLexicalScope();
        builder.exitSection(expression);
    }

    @Override
    public void visitSwitchLabelStatement(PsiSwitchLabelStatement statement) {
        var switchStmt = TranspileUtils.findParent(statement, Set.of(PsiSwitchStatement.class, PsiSwitchExpression.class));
        builder.newCondBranch(switchStmt, true);
        if (statement.isDefaultCase()) builder.setDefaultCaseFlag(switchStmt);
        else requireNonNull(statement.getCaseLabelElementList()).accept(this);
    }

    private <E extends PsiElement> E getEnclosingScope(Class<E> klass) {
        for (PsiElement scope : lexicalScopes) {
            if (klass.isInstance(scope)) return klass.cast(scope);
        }
        throw new RuntimeException("Can not find enclosing scope of type " + klass.getName());
    }

    private PsiSwitchStatement getEnclosingSwitchStatement(PsiElement element) {
        for (PsiElement scope : lexicalScopes) {
            if (scope instanceof PsiSwitchStatement switchStatement) return switchStatement;
        }
        throw new RuntimeException("Can not find enclosing switch statement for " + element);
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        builder.beginBlockStatement(statement);
        enterLexicalScope(statement);
        builder.enterLoopSection(statement, statement.getIteratedValue());
        if (statement.getBody() != null) {
            for (PsiElement child : statement.getBody().getChildren()) {
                child.accept(this);
            }
        }
        builder.exitLoopSection(statement);
        exitLexicalScope();
        builder.endBlockStatement(statement);
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        builder.beginBlockStatement(statement);
        enterLexicalScope(statement);
        if (statement.getInitialization() != null) {
            statement.getInitialization().accept(this);
        }
        List<PsiElement> statements = extractStatementsFromForLoop(statement);
        if (!statements.isEmpty()) {
            builder.enterLoopSection(statement, statements.getFirst());
            for (int i = 1; i < statements.size(); i++) {
                statements.get(i).accept(this);
            }
            builder.exitLoopSection(statement);
        }
        exitLexicalScope();
        builder.endBlockStatement(statement);
    }

    private List<PsiElement> extractStatementsFromForLoop(PsiForStatement forLoop) {
        List<PsiElement> statements = new ArrayList<>();
        if (forLoop.getCondition() != null) statements.add(forLoop.getCondition());
        if (forLoop.getBody() != null) {
            if (forLoop.getBody() instanceof PsiBlockStatement blockStatement) {
                statements.addAll(Arrays.asList(blockStatement.getCodeBlock().getStatements()));
            } else statements.add(forLoop.getBody());
        }
        if (forLoop.getUpdate() != null) statements.add(forLoop.getUpdate());
        return statements;
    }

    private void processExitStatement(PsiElement statement,
                                      Set<Class<? extends PsiElement>> exitsNodesOfType,
                                      boolean mayExitViaExcept,
                                      @Nullable PsiIdentifier label) {
        var nodeAndFinallyBlocks = tryGetFinallyScopes(exitsNodesOfType, label);
        var tryNode = Objects.requireNonNull(nodeAndFinallyBlocks.node);
        var guards = nodeAndFinallyBlocks.blocks;
        var node = builder.addExitNode(statement, tryNode, guards);
        if (mayExitViaExcept)
            connectThrowNode(node, exitsNodesOfType);
    }

    private void connectThrowNode(CfgNode node, Set<Class<? extends PsiElement>> exitsNodesOfType) {
        var exceptGuards = tryGetCatchScopes(exitsNodesOfType);
        builder.connectThrowNode(node, exceptGuards);
    }

    //    @Override
//    public void visitExpression(PsiExpression expression) {
//        processBasicElement(expression);
//    }


    @Override
    public void visitTryStatement(PsiTryStatement statement) {
        builder.beginBlockStatement(statement);
        enterLexicalScope(statement);
        requireNonNull(statement.getTryBlock()).accept(this);
        if (statement.getCatchSections().length > 0) {
            var representative = statement.getCatchSections()[0];
            builder.enterCondSection(representative);
            for (var catchSection : statement.getCatchSections()) {
                builder.newCondBranch(representative);
                builder.beginBlockStatement(catchSection);
                enterLexicalScope(catchSection);
                builder.enterExceptSection(catchSection.getCatchBlock());
                processBasicElement(Objects.requireNonNull(catchSection.getParameter()));
                Objects.requireNonNull(catchSection.getCatchBlock()).accept(this);
                exitLexicalScope();
                builder.endBlockStatement(catchSection);
            }
            builder.newCondBranch(representative);
            builder.exitCondSection(representative);
        }
        if (statement.getFinallyBlock() != null) {
            builder.enterFinallySection(statement.getFinallyBlock());
            statement.getFinallyBlock().accept(this);
            builder.exitFinallySection(statement.getFinallyBlock());
        }
        exitLexicalScope();
        builder.endBlockStatement(statement);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        builder.beginBlockStatement(statement);
        builder.enterCondSection(statement);
        processBasicElement(requireNonNull(statement.getCondition()));
        builder.newCondBranch(statement);
        if (statement.getThenBranch() != null) {
            statement.getThenBranch().accept(this);
        }
        builder.newCondBranch(statement);
        if (statement.getElseBranch() != null) {
            statement.getElseBranch().accept(this);
        }
        builder.exitCondSection(statement);
        builder.endBlockStatement(statement);
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        builder.beginBlockStatement(statement);
        enterLexicalScope(statement);
        builder.enterSection(statement);
        requireNonNull(statement.getCondition()).accept(this);
        builder.enterLoopSection(statement, statement.getCondition());
        if (statement.getBody() != null) statement.getBody().accept(this);
        builder.exitLoopSection(statement);
        builder.exitSection(statement);
        exitLexicalScope();
        builder.endBlockStatement(statement);
    }

    @Override
    public void visitContinueStatement(PsiContinueStatement statement) {
        processContinueStatement(statement);
    }

    private void processContinueStatement(PsiContinueStatement statement) {
        var nodeAndGuards = tryGetFinallyScopes(
                Set.of(PsiForStatement.class, PsiWhileStatement.class),
                statement.getLabelIdentifier()
        );
        builder.addContinueNode(statement, nodeAndGuards.node, nodeAndGuards.blocks);
    }

    @Override
    public void visitCatchSection(PsiCatchSection section) {
        builder.beginBlockStatement(section);
        builder.enterExceptSection(section.getCatchBlock());
        requireNonNull(section.getParameter()).accept(this);
        requireNonNull(section.getCatchBlock()).accept(this);
        builder.endBlockStatement(section);
    }

    @Override
    public void visitLabeledStatement(PsiLabeledStatement statement) {
        requireNonNull(statement.getStatement()).accept(this);
    }

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
        if (statement.getReturnValue() != null) {
            statement.getReturnValue().accept(this);
        }
        processExitStatement(statement, Set.of(PsiMethod.class, PsiLambdaExpression.class), false, null);
    }

    @Override
    public void visitThrowStatement(PsiThrowStatement statement) {
        processExitStatement(statement, Set.of(PsiMethod.class), true, null);
        builder.addError(statement);
    }

    @Override
    public void visitBreakStatement(PsiBreakStatement statement) {
        processExitStatement(
                statement,
                Set.of(PsiWhileStatement.class, PsiForeachStatement.class, PsiForStatement.class, PsiSwitchStatement.class),
                false,
                statement.getLabelIdentifier()
        );
    }

    @Override
    public void visitBlockStatement(PsiBlockStatement statement) {
        for (PsiStatement psiStatement : statement.getCodeBlock().getStatements()) {
            psiStatement.accept(this);
        }
    }

    @Override
    public void visitStatement(PsiStatement statement) {
        processBasicElement(statement);
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        processBasicElement(statement);
    }

    private void processBasicElement(PsiElement element) {
        element.acceptChildren(this);
        if (builder != null) {
            var node = builder.addOrdinaryNode(element);
            PsiExpression expr = switch (element) {
                case PsiExpressionStatement exprStmt -> exprStmt.getExpression();
                case PsiExpression expr1 -> expr1;
                default -> null;
            };
            if(expr != null) {
                if(mayThrow(expr))
                    connectThrowNode(node, Set.of(PsiMethod.class));
            }
        }
    }

    private boolean mayThrow(PsiExpression expression) {
        var visitor = new VisitorBase() {

            boolean mayThrow;

            @Override
            public void visitElement(@NotNull PsiElement element) {
                if(mayThrow)
                    return;
                super.visitElement(element);
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                mayThrow = true;
            }

            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
               if(expression.resolve() instanceof PsiField field && !TranspileUtils.isStatic(field)) {
                   if(expression.getQualifierExpression() != null
                           && !(expression.getQualifierExpression() instanceof PsiThisExpression))
                       mayThrow = true;
               }
            }
        };
        expression.accept(visitor);
        return visitor.mayThrow;
    }

    public Map<PsiParameterListOwner, Graph> getGraphs() {
        return graphs;
    }

    private void enterLexicalScope(PsiElement node) {
        lexicalScopes.push(node);
    }

    private void exitLexicalScope() {
        lexicalScopes.pop();
    }

    private record NodeAndBlocks(@Nullable PsiElement node, List<PsiCodeBlock> blocks) {
    }

}
