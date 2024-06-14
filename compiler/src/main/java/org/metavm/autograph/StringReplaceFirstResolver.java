
package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class StringReplaceFirstResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtil.createClassType(String.class), "replaceFirst",
                            TranspileUtil.createClassType(String.class), TranspileUtil.createClassType(String.class)
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
        var str = expressionResolver.resolve(
                requireNonNull(methodCallExpression.getMethodExpression().getQualifierExpression())
        );
        var target = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        var replacement = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[1]);
        return new FunctionExpression(Func.REPLACE_FIRST, List.of(str, target, replacement));
    }
}
