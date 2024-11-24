package org.metavm.autograph;

import com.intellij.psi.PsiArrayInitializerExpression;
import com.intellij.psi.PsiNewExpression;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class ArrayInitializerTransformer extends VisitorBase {

    @Override
    public void visitArrayInitializerExpression(PsiArrayInitializerExpression expression) {
        super.visitArrayInitializerExpression(expression);
        if(!(expression.getParent() instanceof PsiNewExpression newExpr && newExpr.isArrayCreation())) {
            var text = "new " + Objects.requireNonNull(expression.getType()).getCanonicalText() + expression.getText();
            replace(expression, TranspileUtils.createExpressionFromText(text));
        }
    }
}
