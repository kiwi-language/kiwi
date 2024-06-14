package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;

import java.util.List;

public class ListClearResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(MethodSignature.create(TranspileUtil.createClassType(List.class), "clear"));

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var array = expressionResolver.resolve((PsiExpression) methodCallExpression.getMethodExpression().getQualifier());
        methodGenerator.createClearArray(array);
        return null;
    }

}
