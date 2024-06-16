package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;
import org.metavm.api.lang.NumberUtils;

import java.util.List;

public class NumberFormatMatcher implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(NumberUtils.class),
                    "format",
                    TranspileUtil.createClassType(String.class),
                    TranspileUtil.createPrimitiveType(long.class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.NUMBER_FORMAT, List.of(
                expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]),
                expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[1])
        ));
    }
}
