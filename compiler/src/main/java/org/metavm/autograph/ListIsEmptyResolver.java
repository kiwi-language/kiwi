package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.*;

import java.util.List;
import java.util.Objects;

public class ListIsEmptyResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(MethodSignature.create(TranspileUtil.createClassType(List.class), "isEmpty"));

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var qualifier = Objects.requireNonNull(methodCallExpression.getMethodExpression().getQualifierExpression());
        var array = expressionResolver.resolve(qualifier);
        return new BinaryExpression(
                BinaryOperator.EQ,
                new FunctionExpression(Func.LEN, List.of(array)),
                Expressions.constantLong(0L)
        );
    }
}
