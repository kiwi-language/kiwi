package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class VariableIndexAssigner extends VisitorBase {

    private CallableInfo currentCallable;

    @Override
    public void visitMethod(PsiMethod method) {
        if(TranspileUtils.isAbstract(method))
            return;
        var c = enterCallable(method);
        if(!TranspileUtils.isStatic(method)) {
            if(method.isConstructor() && Objects.requireNonNull(method.getContainingClass()).isEnum())
                c.nextIndex = 3;
            else
                c.nextIndex = 1;
        }
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            assignIndex(parameter);
        }
        Objects.requireNonNull(method.getBody()).accept(this);
        exitCallable();
        method.putUserData(Keys.MAX_LOCALS, c.nextIndex);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        super.visitElement(element);
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        enterCallable(expression);
        super.visitLambdaExpression(expression);
        expression.putUserData(Keys.MAX_LOCALS, exitCallable().nextIndex);
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        super.visitLocalVariable(variable);
        assignIndex(variable);
    }

    @Override
    public void visitParameter(PsiParameter parameter) {
        super.visitParameter(parameter);
        assignIndex(parameter);
    }

    private void assignIndex(PsiVariable variable) {
        currentCallable().assignIndex(variable);
    }

    private CallableInfo enterCallable(PsiElement element) {
        return currentCallable = new CallableInfo(element, currentCallable);
    }

    private CallableInfo exitCallable() {
        var c = currentCallable;
        currentCallable = currentCallable.parent;
        return c;
    }

    private CallableInfo currentCallable() {
        return Objects.requireNonNull(currentCallable);
    }

    private static class CallableInfo {
        @Nullable final CallableInfo parent;
        final PsiElement element;
        final List<CallableInfo> children = new ArrayList<>();
        int nextIndex;
        final List<PsiVariable> variables = new ArrayList<>();

        private CallableInfo(PsiElement element, @Nullable CallableInfo parent) {
            this.element = element;
            this.parent = parent;
            if(parent != null)
                parent.children.add(this);
        }

        public int nextIndex() {
            return nextIndex++;
        }

        public void assignIndex(PsiVariable variable) {
            variable.putUserData(Keys.VARIABLE_INDEX, nextIndex());
            variables.add(variable);
       }
    }

}
