package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.LinkedList;

import static java.util.Objects.requireNonNull;

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
                var k = currentClass();
                var targetKlass = requireNonNull(klass.getContainingClass());
                while (!TranspileUtils.isAssignable(targetKlass, k)) {
                    k = requireNonNull(requireNonNull(k).getContainingClass());
                }
                var replacement = TranspileUtils.createExpressionFromText(
                        k.getName() + ".this." + expression.getText()
                );
                replace(expression, replacement);
            }
        }
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        super.visitReferenceExpression(expression);
        if (expression.resolve() instanceof PsiMember member
                && !(member instanceof PsiMethod method && method.isConstructor())
                && expression.getQualifierExpression() == null
                && member.getContainingClass() != null) {
                expression.setQualifierExpression(getQualifier(currentClass(), member));
        }
    }

    private static PsiExpression getQualifier(PsiClass currentKlass, PsiMember member) {
        var declaringClass = requireNonNull(member.getContainingClass());
        if (TranspileUtils.isStatic(member))
            return TranspileUtils.createExpressionFromText(TranspileUtils.getClassName(declaringClass));
        else {
            if (!TranspileUtils.isAssignable(declaringClass, currentKlass)) {
                var k = requireNonNull(TranspileUtils.getProperParent(currentKlass, PsiClass.class));
                while (!TranspileUtils.isAssignable(declaringClass, k))
                    k = requireNonNull(TranspileUtils.getProperParent(k, PsiClass.class));
                return TranspileUtils.createExpressionFromText(k.getName() + ".this");
            } else
                return TranspileUtils.createExpressionFromText("this");
        }
    }

    private PsiClass currentClass() {
        return requireNonNull(classes.peek());
    }

}
