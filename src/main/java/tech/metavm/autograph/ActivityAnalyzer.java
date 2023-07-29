package tech.metavm.autograph;

import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import tech.metavm.util.KeyValue;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.Keys.*;
import static tech.metavm.util.NncUtils.invokeIfNotNull;

public class ActivityAnalyzer extends JavaRecursiveElementVisitor {

    private Scope scope;

    @Override
    public void visitElement(@NotNull PsiElement element) {
        QnAndMode qnAndMode;
        if ((qnAndMode = element.getUserData(QN_AND_MODE)) != null) trackSymbol(qnAndMode);
        super.visitElement(element);
    }

    @Override
    public void visitMethod(PsiMethod method) {
        enterScope();
        for (PsiAnnotation annotation : method.getAnnotations()) annotation.accept(this);
        var methodQn = QnFactory.getMethodQn(method);
        if (method.getReturnTypeElement() != null) {
            method.getReturnTypeElement().accept(this);
        }
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            requireNonNull(parameter.getTypeElement()).accept(this);
        }
        scope.addDefined(methodQn);
        scope.addModified(methodQn);
        exitAndRecordScope(method);

        enterScope(method.getName());

        enterScope(method.getName());
        visitArgumentDeclarations(method);
        exitAndRecordScope(method.getParameterList());

        enterScope(method.getName());
        if (method.getBody() != null) method.getBody().accept(this);
        exitAndRecordScope(method, BODY_SCOPE);

        exitAndRecordScope(method, ARGS_BODY_SCOPE);
    }

    @Override
    public void visitClass(PsiClass aClass) {
        enterScope(aClass.getName());
        for (PsiAnnotation annotation : aClass.getAnnotations()) {
            annotation.accept(this);
        }
        scope.addDefined(QnFactory.getClassQn(aClass));
        scope.addModified(QnFactory.getClassQn(aClass));
        exitAndRecordScope(aClass);

        enterScope(aClass.getName());
        super.visitClass(aClass);
        exitScope();
    }

    private void visitArgumentDeclarations(PsiMethod method) {
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
        block.accept(this);
        exitAndRecordScope(element, scopeKey);
    }

    private void processStatement(PsiElement statement) {
        if (QnFactory.getQn(statement) != null) return;
        enterScope();
        statement.acceptChildren(this);
        exitAndRecordScope(statement);
    }

    private void enterScope() {
        enterScope(null);
    }

    private void enterScope(String methodName) {
        scope = new Scope(scope, methodName);
    }

    private Scope exitScope() {
        Scope curScope = scope;
        scope = scope.getParent();
        return curScope;
    }

    private Scope exitAndRecordScope(PsiElement statement) {
        return exitAndRecordScope(statement, STATIC_SCOPE);
    }

    private Scope exitAndRecordScope(PsiElement statement, Key<Scope> scopeKey) {
        var scope = exitScope();
        statement.putUserData(scopeKey, scope);
        return scope;
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        processStatement(variable);
    }

    @Override
    public void visitExpression(PsiExpression expression) {
        processStatement(expression);
    }

    @Override
    public void visitParameter(PsiParameter parameter) {
        processStatement(parameter);
    }

    @Override
    public void visitParameterList(PsiParameterList list) {
        processStatement(list);
    }

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
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
    public void visitImportStatement(PsiImportStatement statement) {
        processStatement(statement);
    }

    private void trackSymbol(QnAndMode qnAndMode) {
        var qn = qnAndMode.qualifiedName();
        var mode = qnAndMode.accessMode();
        if (mode.isDefinition) scope.addDefined(qn);
        if (mode.isWrite) scope.addModified(qn);
        if (mode.isRead) scope.addRead(qn);
    }

}
