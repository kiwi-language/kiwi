package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;

import java.util.Date;
import java.util.List;

public class DateBeforeResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.create(TranspileUtil.createClassType(Date.class),
                    "before", TranspileUtil.createClassType(Date.class))
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.DATE_BEFORE,
                List.of(expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()),
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]))
        );
    }
}
