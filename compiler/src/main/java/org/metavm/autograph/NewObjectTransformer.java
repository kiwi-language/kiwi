package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNewExpression;

import static java.util.Objects.requireNonNull;

public class NewObjectTransformer extends VisitorBase {

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (!expression.isArrayCreation()) {
            var classRef = expression.getClassReference();
            if (classRef != null && TranspileUtils.isObjectClass((PsiClass) requireNonNull(classRef.resolve()))) {
                replace(
                        expression,
                        TranspileUtils.createExpressionFromText("new org.metavm.api.entity.MvObject()")
                );
            }
        }
    }
}
