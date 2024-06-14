package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.BinaryExpression;
import org.metavm.expression.BinaryOperator;
import org.metavm.expression.Expression;

import java.util.List;

public class EqualsResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
        MethodSignature.create(
                TranspileUtil.createClassType(Object.class),
                "equals",
                TranspileUtil.createClassType(Object.class)
        )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new BinaryExpression(
                BinaryOperator.EQ,
                expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()),
                expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0])
        );
    }
}
