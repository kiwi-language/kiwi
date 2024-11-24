package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNewExpression;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
public class NewObjectTransformer extends VisitorBase {

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (!expression.isArrayCreation()) {
            var classRef = expression.getClassReference();
            if(classRef != null) {
                if (TranspileUtils.isObjectClass((PsiClass) requireNonNull(classRef.resolve(),
                        () -> "Failed to resolve class " + classRef.getText()))) {
                    replace(
                            expression,
                            TranspileUtils.createExpressionFromText("new org.metavm.api.entity.MvObject()")
                    );
                }
            }
        }
    }
}
