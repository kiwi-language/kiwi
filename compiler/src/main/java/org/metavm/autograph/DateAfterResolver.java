package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;

import java.util.Date;
import java.util.List;

public class DateAfterResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.create(TranspileUtil.createClassType(Date.class),
                    "after", TranspileUtil.createClassType(Date.class))
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.DATE_AFTER,
                List.of(expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()),
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]))
        );
    }
}
