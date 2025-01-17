package org.metavm.autograph;

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
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.metavm.util.InternalException;
import org.metavm.util.KeyValue;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

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

    private final Map<PsiElement, List<PsiElement>> insertsBefore = new HashMap<>();

    public static final String EXTRA_LOOP_TEST = "__extraLoopTest__";

    protected final Namer namer = new Namer();

    // Do not remove, for debug
    @SuppressWarnings("unused")
    protected boolean containsDescendant(PsiElement ancestor, PsiElement descendant) {
        return TranspileUtils.containsDescendant(ancestor, descendant);
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
            return (PsiStatement) addBefore0(cb, statement, anchor);
        } else {
            PsiBlockStatement blockStmt = convertToBlockStatement(anchor);
            anchor = blockStmt.getCodeBlock().getStatements()[0];
            return (PsiStatement) addBefore0(blockStmt.getCodeBlock(), statement, anchor);
        }
    }

    private PsiElement addBefore0(PsiElement parent, PsiElement element, PsiElement anchor) {
        var inserted = parent.addBefore(element, anchor);
        insertsBefore.computeIfAbsent(element, k -> new ArrayList<>()).add(inserted);
        return inserted;
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

    protected @Nullable String getLabel(PsiElement element) {
        if (element.getParent() instanceof PsiLabeledStatement labeledStmt) {
            return labeledStmt.getLabelIdentifier().getText();
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected PsiStatement prependBody(PsiStatement body, PsiStatement toInsert) {
        var blockStmt = convertToBlockStatement(body);
        var block = blockStmt.getCodeBlock();
        var firstStmt = block.getStatementCount() > 0 ? block.getStatements()[0] : null;
        addBefore0(blockStmt.getCodeBlock(), toInsert, firstStmt);
        return blockStmt;
    }

    protected void visitBlock(PsiElement body,
                              @Nullable Consumer<PsiStatement> beforeVisit,
                              @Nullable Function<PsiStatement, KeyValue<PsiStatement, PsiCodeBlock>> afterVisit) {
        Utils.require(body instanceof PsiCodeBlock || body instanceof PsiStatement);
        PsiCodeBlock block;
        if (body instanceof PsiCodeBlock codeBlock) {
            block = codeBlock;
        } else {
            var blockStmt = convertToBlockStatement((PsiStatement) body);
            block = blockStmt.getCodeBlock();
        }
        List<PsiStatement> statements = extractBody(block);
        List<PsiCodeBlock> copyBlocks = new ArrayList<>();
        for (PsiStatement stmt : statements) {
            if (beforeVisit != null) {
                beforeVisit.consume(stmt);
            }
            for (PsiCodeBlock copyBlock : copyBlocks) {
                copyBlock.add(stmt);
            }
            stmt.accept(this);
            stmt = (PsiStatement) getReplacement(stmt);
            PsiCodeBlock newDest = null;
            if (afterVisit != null) {
                var afterVisitRs = afterVisit.apply(stmt);
                newDest = afterVisitRs.value();
            }
            if (newDest != null) {
                copyBlocks.add(newDest);
            }
        }
    }

    protected void simplifyBlock(PsiCodeBlock block) {
        if (block.getParent() instanceof PsiBlockStatement blockStmt
                && blockStmt.getParent() instanceof PsiIfStatement) {
            if (block.getStatementCount() == 1 && block.getStatements()[0] instanceof PsiIfStatement childIf) {
                replace(blockStmt, childIf);
            }
        }
    }

    private List<PsiStatement> extractBody(@Nullable PsiElement body) {
        return switch (body) {
            case PsiCodeBlock block -> List.of(block.getStatements());
            case PsiBlockStatement blockStmt -> List.of(blockStmt.getCodeBlock().getStatements());
            case PsiStatement stmt -> List.of(stmt);
            case null -> List.of();
            default -> throw new InternalException("Invalid body: " + body);
        };
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
            blockStatement = (PsiBlockStatement) TranspileUtils.createStatementFromText("{}");
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
            visitChain(element.getFirstChild());
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

    protected PsiElement add(PsiElement parent, PsiElement child) {
        var newChild = parent.add(child);
        replaceMap.put(child, newChild);
        return newChild;
    }

    protected PsiElement replace(PsiElement replaced, PsiElement replacement) {
        replacement = replaced.replace(replacement);
        replaceMap.put(replaced, replacement);
        return replacement;
    }

    protected PsiElement getReplacement(PsiElement element) {
        return replaceMap.getOrDefault(element, element);
    }

    protected List<PsiElement> getInsertsBefore(PsiElement element) {
        return insertsBefore.getOrDefault(element, List.of());
    }

    protected void visitChain(PsiElement start) {
        PsiElement element = start;
        while (element != null) {
            var sibling = element.getNextSibling();
            element.accept(this);
            element = replaceMap.getOrDefault(element, element);
            element = element.isValid() ? element.getNextSibling() : sibling;
        }
    }

    @Override
    public void visitComment(@NotNull PsiComment comment) {
    }
}
