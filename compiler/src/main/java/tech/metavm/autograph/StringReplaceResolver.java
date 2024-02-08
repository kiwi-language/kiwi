
package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.createType;

public class StringReplaceResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(createType(String.class), "replace",
                            createType(CharSequence.class), createType(CharSequence.class)
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
        return new FunctionExpression(Func.REPLACE, List.of(str, target, replacement));
    }
}
