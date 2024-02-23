package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class ListContainsResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtil.createClassType(List.class), "contains",
                            TranspileUtil.createClassType(Object.class)
                    )
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression,
                              ExpressionResolver expressionResolver,
                              MethodGenerator methodGenerator) {
        var array = expressionResolver.resolve(
                requireNonNull(methodCallExpression.getMethodExpression().getQualifierExpression())
        );
        var value = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        return new FunctionExpression(Func.ARRAY_CONTAINS, List.of(array, value));
    }
}
