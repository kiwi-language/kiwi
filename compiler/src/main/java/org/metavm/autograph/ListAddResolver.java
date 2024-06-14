package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;

import java.util.List;
import java.util.Objects;

public class ListAddResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtil.createClassType(List.class), "add",
                            TranspileUtil.createTypeVariableType(List.class, 0))
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression,
                              ExpressionResolver expressionResolver,
                              MethodGenerator methodGenerator) {
        var qualifier = Objects.requireNonNull(methodCallExpression.getMethodExpression().getQualifierExpression());
        var array = expressionResolver.resolve(qualifier);
        var value = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        methodGenerator.createAddElement(array, value);
        return Expressions.trueExpression();
    }
}
