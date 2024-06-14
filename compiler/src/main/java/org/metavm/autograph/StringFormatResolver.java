package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;

import java.util.List;

public class StringFormatResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(String.class),
                    "format",
                    TranspileUtil.createType(String.class),
                    TranspileUtil.createEllipsisType(Object[].class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(
                Func.STRING_FORMAT,
                List.of(
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]),
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[1])
                )
        );
    }
}
