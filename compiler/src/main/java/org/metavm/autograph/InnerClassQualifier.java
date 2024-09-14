package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.LinkedList;

import java.util.Objects;

@Slf4j
public class InnerClassQualifier extends VisitorBase {

    private final LinkedList<PsiClass> classes = new LinkedList<>();

    @Override
    public void visitClass(PsiClass aClass) {
        if(TranspileUtils.isDiscarded(aClass))
            return;
        classes.push(aClass);
        super.visitClass(aClass);
        classes.pop();
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (expression.getQualifier() == null) {
            var classRef = expression.getClassOrAnonymousClassReference();
            if (classRef != null
                    && classRef.resolve() instanceof PsiClass klass
                    && TranspileUtils.isNonStaticInnerClass(klass)
            ) {
                var replacement = TranspileUtils.createExpressionFromText(
                        Objects.requireNonNull(klass.getContainingClass()).getName() + ".this."
                                + expression.getText()
                );
                replace(expression, replacement);
            }
        }
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        super.visitReferenceExpression(expression);
        if (expression.resolve() instanceof PsiField field && !TranspileUtils.isStatic(field)) {
            var declaringClass = Objects.requireNonNull(field.getContainingClass());
            if (expression.getQualifierExpression() == null) {
                var currentKlass = currentClass();
                if (!TranspileUtils.isAssignable(declaringClass, currentKlass)) {
                    var k = Objects.requireNonNull(currentKlass.getContainingClass());
                    while (!TranspileUtils.isAssignable(declaringClass, k))
                        k = Objects.requireNonNull(k.getContainingClass());
                    replace(
                            expression,
                            TranspileUtils.createExpressionFromText(k.getName() + ".this." + field.getName())
                    );
                }
            }
        }
    }

    private PsiClass currentClass() {
        return Objects.requireNonNull(classes.peek());
    }

}
