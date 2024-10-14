package org.metavm.autograph;

import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import org.metavm.util.KeyValue;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.Keys.*;
import static org.metavm.autograph.TranspileUtils.createTemplateType;
import static org.metavm.util.NncUtils.invokeIfNotNull;

public class ActivityAnalyzer extends JavaRecursiveElementVisitor {

    private Scope scope;
    private final StateStack stateStack = new StateStack();
    private final LinkedList<PsiClass> classes = new LinkedList<>();

    private void processNameElement(PsiElement element) {
        QnAndMode qnAndMode;
        if ((qnAndMode = QnFactory.getQnAndMode(element)) != null) {
            trackSymbol(qnAndMode);
        }
        super.visitElement(element);
    }

    private boolean inConstructor() {
        if (stateStack.has(MethodState.class)) {
            MethodState state = stateStack.get(MethodState.class);
            return state.getMethod().isConstructor();
        } else return false;
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        processNameElement(expression);
    }

    @Override
    public void visitArrayAccessExpression(PsiArrayAccessExpression expression) {
        processNameElement(expression);
    }

    @Override
    public void visitThisExpression(PsiThisExpression expression) {
        processNameElement(expression);
    }

    @Override
    public void visitVariable(PsiVariable variable) {
        processNameElement(variable);
    }

    @Override
    public void visitMethod(PsiMethod method) {
        try (MethodState state = stateStack.create(MethodState.class)) {
            state.setMethod(method);
            enterScope();
            for (PsiAnnotation annotation : method.getAnnotations()) annotation.accept(this);
            if (method.getReturnTypeElement() != null) {
                method.getReturnTypeElement().accept(this);
            }
            for (PsiParameter parameter : method.getParameterList().getParameters()) {
                requireNonNull(parameter.getTypeElement()).accept(this);
            }
            exitAndRecordScope(method);

            enterScope(method.getName());

            enterScope(method.getName());
            visitParameterDeclarations(method);
            exitAndRecordScope(method.getParameterList());

            enterScope(method.getName());
            if (method.getBody() != null) method.getBody().accept(this);
            exitAndRecordScope(method, BODY_SCOPE);

            exitAndRecordScope(method, ARGS_BODY_SCOPE);
        }
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        enterIsolatedScope();
        {
            enterScope();
            expression.getParameterList().accept(this);
            exitAndRecordScope(expression.getParameterList());
        }
        {
            enterScope();
            if (expression.getBody() != null)
                expression.getBody().accept(this);
            exitAndRecordScope(expression, BODY_SCOPE);
        }
        exitAndRecordScope(expression, ARGS_BODY_SCOPE);
    }

    private void defineSelfParameter() {
        trackSymbol(new QnAndMode(
                new AtomicQualifiedName("this", createTemplateType(currentClass())),
                AccessMode.DEFINE_WRITE
        ));
    }

    private PsiClass currentClass() {
        return requireNonNull(classes.peek());
    }

    @Override
    public void visitClass(PsiClass aClass) {
        if(TranspileUtils.isDiscarded(aClass))
            return;
        try (ClassState clsState = stateStack.create(ClassState.class)) {
            clsState.setNode(aClass);
            classes.push(aClass);
            enterScope(aClass.getName());
            for (PsiAnnotation annotation : aClass.getAnnotations()) {
                annotation.accept(this);
            }
            exitAndRecordScope(aClass);

            enterScope(aClass.getName());
            super.visitClass(aClass);
            exitAndRecordScope(aClass, BODY_SCOPE);
            classes.pop();
        }
    }

    private void visitParameterDeclarations(PsiMethod method) {
        if (!method.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
            PsiParameter firstParam = method.getParameterList().getParameter(0);
            if (firstParam == null || !firstParam.getName().equals("this")) {
                defineSelfParameter();
            }
        }
        method.getParameterList().accept(this);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        enterScope();
        requireNonNull(statement.getCondition()).accept(this);
        var scope = exitAndRecordScope(statement.getCondition());
        statement.putUserData(COND_SCOPE, scope);
        processParallelBlocks(statement,
                List.of(new KeyValue<>(Keys.BODY_SCOPE, statement.getThenBranch()),
                        new KeyValue<>(Keys.ELSE_SCOPE, statement.getElseBranch()))
        );
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        enterScope();
        requireNonNull(statement.getCondition()).accept(this);
        var scope = exitAndRecordScope(statement.getCondition());
        statement.putUserData(COND_SCOPE, scope);
        processParallelBlocks(statement, List.of(new KeyValue<>(BODY_SCOPE, statement.getBody())));
    }

    @Override
    public void visitTryStatement(PsiTryStatement statement) {
        if (statement.getResourceList() != null) {
            enterScope();
            statement.getResourceList().accept(this);
            exitAndRecordScope(statement, RESOURCE_SCOPE);
        }
        var tryBlock = NncUtils.requireNonNull(statement.getTryBlock());
        enterScope();
        tryBlock.accept(this);
        exitAndRecordScope(statement, BODY_SCOPE);
        for (PsiCatchSection catchSection : statement.getCatchSections()) {
            enterScope();
            enterScope();
            processCatchParameter(Objects.requireNonNull(catchSection.getParameter()));
            exitAndRecordScope(catchSection, ARGS_SCOPE);
            enterScope();
            Objects.requireNonNull(catchSection.getCatchBlock()).accept(this);
            exitAndRecordScope(catchSection, BODY_SCOPE);
            exitAndRecordScope(catchSection, ARGS_BODY_SCOPE);
        }
        if (statement.getFinallyBlock() != null) {
            enterScope();
            statement.getFinallyBlock().accept(this);
            exitAndRecordScope(statement, FINALLY_SCOPE);
        }
    }

    private void processCatchParameter(PsiParameter parameter) {
        enterScope();
        parameter.accept(this);
        exitAndRecordScope(parameter);
    }

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        enterScope();
        requireNonNull(expression.getExpression()).accept(this);
        exitAndRecordScope(expression.getExpression());
        processParallelBlocks(expression, List.of(new KeyValue<>(BODY_SCOPE, expression.getBody())));
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        enterScope();
        invokeIfNotNull(statement.getInitialization(), init -> init.accept(this));
        invokeIfNotNull(statement.getCondition(), cond -> cond.accept(this));
        invokeIfNotNull(statement.getUpdate(), upd -> upd.accept(this));
        exitAndRecordScope(statement, ITERATE_SCOPE);

        enterScope();
        invokeIfNotNull(statement.getBody(), body -> body.accept(this));
        exitAndRecordScope(statement, BODY_SCOPE);
    }

    @Override
    public void visitCallExpression(PsiCallExpression callExpression) {
        if (callExpression.getArgumentList() != null) {
            enterScope();
            requireNonNull(callExpression.getArgumentList()).accept(this);
            exitAndRecordScope(callExpression, ARGS_SCOPE);
        }
        super.visitCallExpression(callExpression);
    }

    @Override
    public void visitAssertStatement(PsiAssertStatement statement) {
        processStatement(statement);
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        enterScope();
        statement.getIterationParameter().accept(this);
        requireNonNull(statement.getIteratedValue()).accept(this);
        exitAndRecordScope(statement.getIteratedValue());
        enterScope();
        statement.getIterationParameter().accept(this);
        exitAndRecordScope(statement.getIterationParameter(), ITERATE_SCOPE);
        processParallelBlocks(statement, List.of(new KeyValue<>(BODY_SCOPE, statement.getBody())));
    }

    private void processParallelBlocks(PsiElement parent, List<KeyValue<Key<Scope>, PsiElement>> children) {
        var beforeParent = Scope.copyOf(scope);
        List<Scope> afterChildren = new ArrayList<>();
        for (var child : children) {
            if (child == null) {
                continue;
            }
            scope.copyFrom(beforeParent);
            processBlockElement(parent, child.value(), child.key());
            afterChildren.add(scope.copy());
        }
        for (Scope afterChild : afterChildren) {
            scope.mergeFrom(afterChild);
        }
    }

    private void processBlockElement(PsiElement element, PsiElement block, Key<Scope> scopeKey) {
        enterScope();
        if (block != null) block.accept(this);
        exitAndRecordScope(element, scopeKey);
    }

    private void processStatement(PsiElement statement) {
        enterScope();
        statement.acceptChildren(this);
        exitAndRecordScope(statement);
    }

    private void enterScope() {
        enterScope(null);
    }

    private void enterScope(String methodName) {
        scope = new Scope(scope, methodName, false);
    }

    private void enterIsolatedScope() {
        scope = new Scope(scope, null, true);
    }

    private Scope exitScope() {
        Scope curScope = scope;
        scope.finish();
        scope = scope.getParent();
        return curScope;
    }

    private Scope exitAndRecordScope(PsiElement statement) {
        return exitAndRecordScope(statement, SCOPE);
    }

    private Scope exitAndRecordScope(PsiElement statement, Key<Scope> scopeKey) {
        var scope = exitScope();
        statement.putUserData(scopeKey, scope);
        return scope;
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
        processStatement(statement);
    }

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
        processStatement(statement);
    }

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        enterScope();
        requireNonNull(statement.getExpression()).accept(this);
        exitAndRecordScope(statement.getExpression());
        processParallelBlocks(statement, List.of(new KeyValue<>(BODY_SCOPE, statement.getBody())));
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        processStatement(statement);
    }

    @Override
    public void visitField(PsiField field) {
        processStatement(field);
    }

    @Override
    public void visitThrowStatement(PsiThrowStatement statement) {
        processStatement(statement);
    }

    @Override
    public void visitDeclarationStatement(PsiDeclarationStatement statement) {
        for (PsiElement declaredElement : statement.getDeclaredElements()) {
            if(!(declaredElement instanceof PsiClass)) {
                var qnAndMode = NncUtils.requireNonNull(declaredElement.getUserData(QN_AND_MODE));
                scope.addIsolatedName(qnAndMode.qualifiedName());
            }
        }
        processStatement(statement);
    }

    @Override
    public void visitBlockStatement(PsiBlockStatement statement) {
        enterScope();
        super.visitBlockStatement(statement);
        exitAndRecordScope(statement, BODY_SCOPE);
    }

    private void trackSymbol(QnAndMode qnAndMode) {
        var qn = qnAndMode.qualifiedName();
        var mode = qnAndMode.accessMode();
        if (mode.isDefinition) scope.addDefined(qn);
        if (mode.isWrite) scope.addModified(qn);
        if (mode.isRead) scope.addRead(qn);
    }

}
