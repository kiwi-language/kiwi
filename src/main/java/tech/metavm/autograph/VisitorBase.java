package tech.metavm.autograph;

/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * @author max
 */

import com.intellij.psi.*;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.util.InternalException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * a JavaElementVisitor which also visits all children elements
 * in a tree pre-order, see <a href="https://en.wikipedia.org/wiki/Tree_traversal#Pre-order">Tree traversal:Pre-order</a> for details.
 * <p>
 * <b>Note</b>: This visitor handles all elements recursively, so it can consume a large amount of stack space for very deep trees.
 * For such deep trees please consider using {@link JavaRecursiveElementWalkingVisitor} instead.
 */
public abstract class VisitorBase extends JavaElementVisitor implements PsiRecursiveVisitor {
    // This stack thing is intended to prevent exponential child traversing due to visitReferenceExpression calls both visitRefElement
    // and visitExpression.
    private final Stack<PsiReferenceExpression> myRefExprsInVisit = new Stack<>();

    private final Map<PsiElement, PsiElement> replaceMap = new HashMap<>();

    public static final String EXTRA_LOOP_TEST = "__extraLoopTest__";

    // Do not remove, for debug
    @SuppressWarnings("unused")
    protected boolean containsDescendant(PsiElement ancestor, PsiElement descendant) {
        return TranspileUtil.containsDescendant(ancestor, descendant);
    }

    @SuppressWarnings("UnusedReturnValue")
    protected PsiStatement insertAfter(PsiStatement statement, PsiStatement anchor) {
        if (anchor.getParent() instanceof PsiCodeBlock cb) {
            return (PsiStatement) cb.addAfter(statement, anchor);
        } else {
            PsiBlockStatement blockStmt = convertToBlockStatement(anchor);
            return (PsiStatement) blockStmt.getCodeBlock().add(statement);
        }
    }

    protected PsiStatement getFirstStatement(PsiStatement statement) {
        if (statement instanceof PsiBlockStatement blockStatement) {
            var codeBlock = blockStatement.getCodeBlock();
            return codeBlock.getStatementCount() > 0 ? codeBlock.getStatements()[0] : null;
        } else {
            return statement;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected PsiStatement insertBefore(PsiStatement statement, PsiStatement anchor) {
        if (anchor.getParent() instanceof PsiLabeledStatement labeledStatement) {
            anchor = labeledStatement;
        }
        if (anchor.getParent() instanceof PsiCodeBlock cb) {
            return (PsiStatement) cb.addBefore(statement, anchor);
        } else {
            PsiBlockStatement blockStmt = convertToBlockStatement(anchor);
            anchor = blockStmt.getCodeBlock().getStatements()[0];
            return (PsiStatement) blockStmt.getCodeBlock().addBefore(statement, anchor);
        }
    }

    protected boolean isExtraLoopTest(PsiStatement statement) {
        return isMethodCall(statement, EXTRA_LOOP_TEST);
    }

    protected boolean isMethodCall(PsiStatement statement, String methodName) {
        if (statement instanceof PsiExpressionStatement exprStmt) {
            return isMethodCall(exprStmt.getExpression(), methodName);
        }
        return false;
    }

    protected boolean isMethodCall(PsiExpression expression, String methodName) {
        if (expression instanceof PsiMethodCallExpression methodCallExpr) {
            return Objects.equals(methodCallExpr.getMethodExpression().getReferenceName(), methodName);
        }
        return false;
    }


    @SuppressWarnings("UnusedReturnValue")
    protected PsiStatement prependBody(PsiStatement body, PsiStatement toInsert) {
        var blockStmt = convertToBlockStatement(body);
        var block = blockStmt.getCodeBlock();
        var firstStmt = block.getStatementCount() > 0 ? block.getStatements()[0] : null;
        blockStmt.getCodeBlock().addBefore(toInsert, firstStmt);
        return blockStmt;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected PsiStatement appendBody(PsiStatement body, PsiStatement toInsert) {
        var blockStmt = convertToBlockStatement(body);
        blockStmt.getCodeBlock().add(toInsert);
        return blockStmt;
    }

    protected PsiBlockStatement convertToBlockStatement(PsiStatement body) {
        PsiBlockStatement blockStatement;
        if (body instanceof PsiBlockStatement blockStmt) {
            blockStatement = blockStmt;
        } else {
            blockStatement = (PsiBlockStatement) TranspileUtil.createStatementFromText("{}");
            var bodyCopy = body.copy();
            blockStatement = (PsiBlockStatement) replace(body, blockStatement);
            blockStatement.getCodeBlock().add(bodyCopy);
        }
        return blockStatement;
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (!myRefExprsInVisit.isEmpty() && myRefExprsInVisit.peek() == element) {
            myRefExprsInVisit.pop();
            myRefExprsInVisit.push(null);
        } else {
            PsiElement child = element.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = replaceMap.getOrDefault(child, child);
                child = child.getNextSibling();
            }
            replaceMap.clear();
        }
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        myRefExprsInVisit.push(expression);
        try {
            visitExpression(expression);
            visitReferenceElement(expression);
        } finally {
            myRefExprsInVisit.pop();
        }
    }

    protected PsiElement replace(PsiElement replaced, PsiElement replacement) {
        replacement = replaced.replace(replacement);
        replaceMap.put(replaced, replacement);
        return replacement;
    }

    protected PsiElement getReplacement(PsiElement element) {
        return replaceMap.getOrDefault(element, element);
    }

}
